package org.openqa.selenium.locator.bidi.selenium.adapter;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.BrowsingContextInspector;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.locator.bidi.model.BidiFeature;

final class SeleniumBidiEventSource implements BidiEventSource {

  private final WebDriver driver;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final EnumSet<BidiFeature> supportedFeatures = EnumSet.noneOf(BidiFeature.class);

  private Network network;
  private LogInspector logInspector;
  private BrowsingContextInspector browsingContextInspector;

  private Consumer<RawBidiEvent> networkConsumer = event -> {};
  private Consumer<RawBidiEvent> consoleConsumer = event -> {};
  private Consumer<RawBidiEvent> navigationConsumer = event -> {};

  SeleniumBidiEventSource(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public void start() {
    if (!running.compareAndSet(false, true)) {
      return;
    }

    initNetwork();
    initConsole();
    initNavigation();
  }

  @Override
  public void stop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }
    safeCloseNetwork();
    safeCloseLog();
    safeCloseNavigation();
  }

  @Override
  public Set<BidiFeature> supportedFeatures() {
    return EnumSet.copyOf(supportedFeatures);
  }

  @Override
  public void subscribeNetwork(Consumer<RawBidiEvent> handler) {
    this.networkConsumer = handler == null ? event -> {} : handler;
  }

  @Override
  public void subscribeConsole(Consumer<RawBidiEvent> handler) {
    this.consoleConsumer = handler == null ? event -> {} : handler;
  }

  @Override
  public void subscribeNavigation(Consumer<RawBidiEvent> handler) {
    this.navigationConsumer = handler == null ? event -> {} : handler;
  }

  @Override
  public void close() {
    stop();
  }

  private void initNetwork() {
    try {
      network = new Network(driver);
      network.onBeforeRequestSent(
          event -> networkConsumer.accept(new RawBidiEvent(RawBidiEventType.NETWORK_BEFORE_REQUEST, event)));
      network.onResponseStarted(
          event ->
              networkConsumer.accept(new RawBidiEvent(RawBidiEventType.NETWORK_RESPONSE_STARTED, event)));
      network.onResponseCompleted(
          event ->
              networkConsumer.accept(new RawBidiEvent(RawBidiEventType.NETWORK_RESPONSE_COMPLETED, event)));
      network.onFetchError(
          event -> networkConsumer.accept(new RawBidiEvent(RawBidiEventType.NETWORK_FETCH_ERROR, event)));
      supportedFeatures.add(BidiFeature.NETWORK);
    } catch (RuntimeException ex) {
      safeCloseNetwork();
    }
  }

  private void initConsole() {
    try {
      logInspector = new LogInspector(driver);
      logInspector.onConsoleEntry(
          entry -> consoleConsumer.accept(new RawBidiEvent(RawBidiEventType.CONSOLE, entry)));
      supportedFeatures.add(BidiFeature.CONSOLE);
    } catch (RuntimeException ex) {
      safeCloseLog();
    }
  }

  private void initNavigation() {
    try {
      browsingContextInspector = new BrowsingContextInspector(driver);
      browsingContextInspector.onNavigationStarted(
          info ->
              navigationConsumer.accept(
                  new RawBidiEvent(RawBidiEventType.NAVIGATION_STARTED, info)));
      browsingContextInspector.onNavigationCommitted(
          info ->
              navigationConsumer.accept(
                  new RawBidiEvent(RawBidiEventType.NAVIGATION_COMMITTED, info)));
      browsingContextInspector.onDomContentLoaded(
          info ->
              navigationConsumer.accept(
                  new RawBidiEvent(RawBidiEventType.NAVIGATION_DOM_CONTENT_LOADED, info)));
      browsingContextInspector.onBrowsingContextLoaded(
          info ->
              navigationConsumer.accept(
                  new RawBidiEvent(RawBidiEventType.NAVIGATION_LOADED, info)));
      browsingContextInspector.onHistoryUpdated(
          update ->
              navigationConsumer.accept(new RawBidiEvent(RawBidiEventType.HISTORY_UPDATED, update)));
      supportedFeatures.add(BidiFeature.NAVIGATION);
    } catch (RuntimeException ex) {
      safeCloseNavigation();
    }
  }

  private void safeCloseNetwork() {
    try {
      if (network != null) {
        network.close();
      }
    } catch (RuntimeException ignored) {
      // Ignore failures on shutdown.
    } finally {
      network = null;
      supportedFeatures.remove(BidiFeature.NETWORK);
    }
  }

  private void safeCloseLog() {
    try {
      if (logInspector != null) {
        logInspector.close();
      }
    } catch (RuntimeException ignored) {
      // Ignore failures on shutdown.
    } finally {
      logInspector = null;
      supportedFeatures.remove(BidiFeature.CONSOLE);
    }
  }

  private void safeCloseNavigation() {
    try {
      if (browsingContextInspector != null) {
        browsingContextInspector.close();
      }
    } catch (RuntimeException ignored) {
      // Ignore failures on shutdown.
    } finally {
      browsingContextInspector = null;
      supportedFeatures.remove(BidiFeature.NAVIGATION);
    }
  }
}
