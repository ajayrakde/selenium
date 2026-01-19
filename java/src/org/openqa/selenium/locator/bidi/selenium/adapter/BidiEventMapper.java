package org.openqa.selenium.locator.bidi.selenium.adapter;

import java.util.Locale;
import org.openqa.selenium.bidi.browsingcontext.HistoryUpdated;
import org.openqa.selenium.bidi.browsingcontext.NavigationInfo;
import org.openqa.selenium.bidi.log.ConsoleLogEntry;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.bidi.network.BeforeRequestSent;
import org.openqa.selenium.bidi.network.FetchError;
import org.openqa.selenium.bidi.network.RequestData;
import org.openqa.selenium.bidi.network.ResponseData;
import org.openqa.selenium.bidi.network.ResponseDetails;
import org.openqa.selenium.locator.bidi.model.ConsoleEvent;
import org.openqa.selenium.locator.bidi.model.ConsoleLevel;
import org.openqa.selenium.locator.bidi.model.NavEventType;
import org.openqa.selenium.locator.bidi.model.NavigationEvent;
import org.openqa.selenium.locator.bidi.model.NetworkEvent;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;
import org.openqa.selenium.locator.bidi.model.NetworkStage;

class BidiEventMapper {

  NetworkEvent mapNetwork(RawBidiEvent event, long tsEpochMs) {
    return switch (event.type()) {
      case NETWORK_BEFORE_REQUEST -> mapRequest((BeforeRequestSent) event.payload(), tsEpochMs);
      case NETWORK_RESPONSE_STARTED ->
          mapResponse((ResponseDetails) event.payload(), NetworkStage.RESPONSE, tsEpochMs);
      case NETWORK_RESPONSE_COMPLETED ->
          mapResponse((ResponseDetails) event.payload(), NetworkStage.FINISHED, tsEpochMs);
      case NETWORK_FETCH_ERROR -> mapFetchError((FetchError) event.payload(), tsEpochMs);
      default -> null;
    };
  }

  ConsoleEvent mapConsole(ConsoleLogEntry entry, int maxTextLength, long tsEpochMs) {
    if (entry == null) {
      return null;
    }
    ConsoleLevel level = mapConsoleLevel(entry.getLevel());
    String text = truncate(entry.getText(), maxTextLength);
    String source = entry.getSource() == null ? null : entry.getSource().getRealm();
    String url = null;
    return new ConsoleEvent(tsEpochMs, level, text, source, url);
  }

  NavigationEvent mapNavigation(RawBidiEvent event, long tsEpochMs) {
    return switch (event.type()) {
      case NAVIGATION_STARTED ->
          mapNavigationInfo((NavigationInfo) event.payload(), NavEventType.NAV_START, tsEpochMs);
      case NAVIGATION_COMMITTED ->
          mapNavigationInfo((NavigationInfo) event.payload(), NavEventType.NAV_COMMIT, tsEpochMs);
      case NAVIGATION_DOM_CONTENT_LOADED ->
          mapNavigationInfo(
              (NavigationInfo) event.payload(), NavEventType.NAV_DOM_CONTENT_LOADED, tsEpochMs);
      case NAVIGATION_LOADED ->
          mapNavigationInfo((NavigationInfo) event.payload(), NavEventType.NAV_LOAD, tsEpochMs);
      case HISTORY_UPDATED ->
          mapHistoryUpdated((HistoryUpdated) event.payload(), NavEventType.URL_CHANGED, tsEpochMs);
      default -> null;
    };
  }

  private NetworkEvent mapRequest(BeforeRequestSent requestSent, long tsEpochMs) {
    RequestData request = requestSent.getRequest();
    String url = request == null ? null : request.getUrl();
    return new NetworkEvent(
        tsEpochMs,
        request == null ? null : request.getRequestId(),
        url,
        request == null ? null : request.getMethod(),
        mapResourceType(url, null),
        NetworkStage.REQUEST,
        null,
        null);
  }

  private NetworkEvent mapResponse(ResponseDetails details, NetworkStage stage, long tsEpochMs) {
    RequestData request = details.getRequest();
    ResponseData response = details.getResponseData();
    String url = response == null ? null : response.getUrl();
    String mimeType = response == null ? null : response.getMimeType();
    Integer status = response == null ? null : response.getStatus();
    return new NetworkEvent(
        tsEpochMs,
        request == null ? null : request.getRequestId(),
        url,
        request == null ? null : request.getMethod(),
        mapResourceType(url, mimeType),
        stage,
        status,
        null);
  }

  private NetworkEvent mapFetchError(FetchError error, long tsEpochMs) {
    RequestData request = error.getRequest();
    String url = request == null ? null : request.getUrl();
    return new NetworkEvent(
        tsEpochMs,
        request == null ? null : request.getRequestId(),
        url,
        request == null ? null : request.getMethod(),
        mapResourceType(url, null),
        NetworkStage.FAILED,
        null,
        error.getErrorText());
  }

  private ConsoleLevel mapConsoleLevel(LogLevel level) {
    if (level == null) {
      return ConsoleLevel.LOG;
    }
    return switch (level) {
      case ERROR -> ConsoleLevel.ERROR;
      case WARN -> ConsoleLevel.WARN;
      case INFO -> ConsoleLevel.INFO;
      case DEBUG -> ConsoleLevel.DEBUG;
      default -> ConsoleLevel.LOG;
    };
  }

  private NavigationEvent mapNavigationInfo(
      NavigationInfo info, NavEventType type, long tsEpochMs) {
    if (info == null) {
      return null;
    }
    return new NavigationEvent(tsEpochMs, type, info.getUrl(), info.getNavigationId());
  }

  private NavigationEvent mapHistoryUpdated(
      HistoryUpdated update, NavEventType type, long tsEpochMs) {
    if (update == null) {
      return null;
    }
    return new NavigationEvent(tsEpochMs, type, update.getUrl(), null);
  }

  private NetworkResourceType mapResourceType(String url, String mimeType) {
    if (url != null) {
      String lower = url.toLowerCase(Locale.ROOT);
      if (lower.startsWith("ws://") || lower.startsWith("wss://")) {
        return NetworkResourceType.WS;
      }
      if (lower.endsWith(".css")) {
        return NetworkResourceType.STYLESHEET;
      }
      if (lower.endsWith(".js")) {
        return NetworkResourceType.SCRIPT;
      }
      if (lower.endsWith(".png")
          || lower.endsWith(".jpg")
          || lower.endsWith(".jpeg")
          || lower.endsWith(".gif")
          || lower.endsWith(".webp")) {
        return NetworkResourceType.IMAGE;
      }
      if (lower.endsWith(".woff") || lower.endsWith(".woff2") || lower.endsWith(".ttf")) {
        return NetworkResourceType.FONT;
      }
    }

    if (mimeType == null) {
      return NetworkResourceType.OTHER;
    }

    String lower = mimeType.toLowerCase(Locale.ROOT);
    if (lower.contains("text/html")) {
      return NetworkResourceType.DOCUMENT;
    }
    if (lower.contains("application/json")) {
      return NetworkResourceType.XHR;
    }
    if (lower.contains("javascript")) {
      return NetworkResourceType.SCRIPT;
    }
    if (lower.contains("text/css")) {
      return NetworkResourceType.STYLESHEET;
    }
    if (lower.startsWith("image/")) {
      return NetworkResourceType.IMAGE;
    }
    if (lower.startsWith("font/") || lower.contains("font")) {
      return NetworkResourceType.FONT;
    }
    if (lower.startsWith("audio/") || lower.startsWith("video/")) {
      return NetworkResourceType.MEDIA;
    }

    return NetworkResourceType.OTHER;
  }

  private String truncate(String text, int maxTextLength) {
    if (text == null || maxTextLength <= 0) {
      return "";
    }
    return text.length() <= maxTextLength ? text : text.substring(0, maxTextLength);
  }
}
