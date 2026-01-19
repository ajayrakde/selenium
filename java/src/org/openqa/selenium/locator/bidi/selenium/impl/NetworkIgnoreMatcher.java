package org.openqa.selenium.locator.bidi.selenium.impl;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.bidi.config.NetworkTrackingConfig;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;

final class NetworkIgnoreMatcher {
  private final NetworkTrackingConfig config;
  private final List<Pattern> ignoredUrlPatterns;
  private final List<Pattern> longPollUrlPatterns;

  NetworkIgnoreMatcher(NetworkTrackingConfig config) {
    this.config = config;
    this.ignoredUrlPatterns = config.ignoredUrlPatterns();
    this.longPollUrlPatterns = config.longPollUrlPatterns();
  }

  boolean shouldIgnore(NetworkResourceType type, String url) {
    NetworkResourceType safeType = type == null ? NetworkResourceType.OTHER : type;
    String safeUrl = url == null ? "" : url;

    if (config.ignoredTypes().contains(safeType)) {
      return true;
    }

    if (config.ignoreWebSockets()
        && (safeType == NetworkResourceType.WS || looksLikeWebSocketUrl(safeUrl))) {
      return true;
    }

    if (matchesAny(ignoredUrlPatterns, safeUrl)) {
      return true;
    }

    return matchesAny(longPollUrlPatterns, safeUrl);
  }

  private boolean matchesAny(List<Pattern> patterns, String url) {
    for (Pattern pattern : patterns) {
      if (pattern != null && pattern.matcher(url).find()) {
        return true;
      }
    }
    return false;
  }

  private boolean looksLikeWebSocketUrl(String url) {
    String lower = url.toLowerCase(Locale.ROOT);
    return lower.startsWith("ws://") || lower.startsWith("wss://");
  }
}
