package org.openqa.selenium.locator.bidi.selenium.impl;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.DiagnosticsSink;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.api.Clock;
import org.openqa.selenium.locator.bidi.api.ConsoleTracker;
import org.openqa.selenium.locator.bidi.api.NavigationTracker;
import org.openqa.selenium.locator.bidi.api.NetworkActivityTracker;
import org.openqa.selenium.locator.bidi.config.BidiTrackingConfig;
import org.openqa.selenium.locator.bidi.config.ConsoleTrackingConfig;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.BidiSnapshotBundle;
import org.openqa.selenium.locator.bidi.model.ConsoleEvent;
import org.openqa.selenium.locator.bidi.model.NetworkEvent;
import org.openqa.selenium.locator.bidi.model.NavigationEvent;
import org.openqa.selenium.locator.bidi.selenium.adapter.BidiCapabilityDetector;
import org.openqa.selenium.locator.bidi.selenium.adapter.BidiEventMapper;
import org.openqa.selenium.locator.bidi.selenium.adapter.BidiEventSource;
import org.openqa.selenium.locator.bidi.selenium.adapter.RawBidiEvent;
import org.openqa.selenium.locator.bidi.selenium.adapter.SeleniumBidiEventSource;

public final class DefaultBidiSignals implements BidiSignals {

  private final WebDriver driver;
  private final BidiTrackingConfig config;
  private final Clock clock;
  private final DiagnosticsSink diagnosticsSink;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final DefaultNetworkActivityTracker networkTracker;
  private final DefaultConsoleTracker consoleTracker;
  private final DefaultNavigationTracker navigationTracker;
  private final BidiCapabilityDetector capabilityDetector = new BidiCapabilityDetector();
  private final BidiEventMapper mapper = new BidiEventMapper();

  private volatile BidiCapability capability;
  private BidiEventSource eventSource;

  public DefaultBidiSignals(
      WebDriver driver, BidiTrackingConfig config, Clock clock, DiagnosticsSink diagnosticsSink) {
    this.driver = Objects.requireNonNull(driver, "driver");
    this.config = Objects.requireNonNull(config, "config");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.diagnosticsSink = diagnosticsSink;
    this.networkTracker =
        new DefaultNetworkActivityTracker(config.network(), clock, diagnosticsSink);
    this.consoleTracker = new DefaultConsoleTracker(config.console(), clock, diagnosticsSink);
    this.navigationTracker =
        new DefaultNavigationTracker(config.navigation(), clock, diagnosticsSink);
    this.capability =
        new BidiCapability(BidiCapabilityStatus.UNSUPPORTED, "Not started", Set.of());
  }

  @Override
  public void start() {
    if (!running.compareAndSet(false, true)) {
      return;
    }

    capability = capabilityDetector.detect(driver);
    if (capability.status() == BidiCapabilityStatus.UNSUPPORTED) {
      running.set(false);
      return;
    }

    if (!config.network().enabled()
        && !config.console().enabled()
        && !config.navigation().enabled()) {
      running.set(false);
      return;
    }

    eventSource = new SeleniumBidiEventSource(driver);
    Set<BidiFeature> desiredFeatures = capability.features();

    if (config.network().enabled() && desiredFeatures.contains(BidiFeature.NETWORK)) {
      networkTracker.start();
      eventSource.subscribeNetwork(this::handleNetwork);
    }

    if (config.console().enabled() && desiredFeatures.contains(BidiFeature.CONSOLE)) {
      consoleTracker.start();
      eventSource.subscribeConsole(this::handleConsole);
    }

    if (config.navigation().enabled() && desiredFeatures.contains(BidiFeature.NAVIGATION)) {
      navigationTracker.start();
      eventSource.subscribeNavigation(this::handleNavigation);
    }

    eventSource.start();
    Set<BidiFeature> supportedFeatures = eventSource.supportedFeatures();
    reconcileCapability(supportedFeatures);
    adjustTrackers(supportedFeatures);
  }

  @Override
  public void stop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }
    if (eventSource != null) {
      eventSource.stop();
      eventSource = null;
    }
    networkTracker.stop();
    consoleTracker.stop();
    navigationTracker.stop();
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public BidiCapability capability() {
    return capability;
  }

  @Override
  public NetworkActivityTracker network() {
    return networkTracker;
  }

  @Override
  public ConsoleTracker console() {
    return consoleTracker;
  }

  @Override
  public NavigationTracker navigation() {
    return navigationTracker;
  }

  @Override
  public BidiSnapshotBundle snapshotAll() {
    return new BidiSnapshotBundle(
        networkTracker.snapshot(), consoleTracker.snapshot(), navigationTracker.snapshot());
  }

  private void handleNetwork(RawBidiEvent rawEvent) {
    NetworkEvent event = mapper.mapNetwork(rawEvent, clock.nowEpochMs());
    if (event != null) {
      networkTracker.onNetworkEvent(event);
    }
  }

  private void handleConsole(RawBidiEvent rawEvent) {
    ConsoleEvent event =
        mapper.mapConsole(
            (org.openqa.selenium.bidi.log.ConsoleLogEntry) rawEvent.payload(),
            config.console().maxTextLength(),
            clock.nowEpochMs());
    if (event == null) {
      return;
    }
    boolean readinessRelevant = isReadinessRelevant(event, config.console());
    consoleTracker.onConsoleEvent(event, readinessRelevant);
  }

  private void handleNavigation(RawBidiEvent rawEvent) {
    NavigationEvent event = mapper.mapNavigation(rawEvent, clock.nowEpochMs());
    if (event != null) {
      navigationTracker.onNavigationEvent(event);
    }
  }

  private void reconcileCapability(Set<BidiFeature> supportedFeatures) {
    if (supportedFeatures == null || supportedFeatures.isEmpty()) {
      capability =
          new BidiCapability(
              BidiCapabilityStatus.UNSUPPORTED,
              "BiDi subscriptions failed",
              Set.of());
      return;
    }
    BidiCapabilityStatus status =
        supportedFeatures.size() == BidiFeature.values().length
            ? BidiCapabilityStatus.SUPPORTED
            : BidiCapabilityStatus.DEGRADED;
    capability =
        new BidiCapability(
            status,
            status == BidiCapabilityStatus.SUPPORTED
                ? "BiDi subscriptions active"
                : "BiDi subscriptions partially active",
            EnumSet.copyOf(supportedFeatures));
  }

  private void adjustTrackers(Set<BidiFeature> supportedFeatures) {
    if (supportedFeatures == null) {
      networkTracker.stop();
      consoleTracker.stop();
      navigationTracker.stop();
      return;
    }
    if (!supportedFeatures.contains(BidiFeature.NETWORK)) {
      networkTracker.stop();
    }
    if (!supportedFeatures.contains(BidiFeature.CONSOLE)) {
      consoleTracker.stop();
    }
    if (!supportedFeatures.contains(BidiFeature.NAVIGATION)) {
      navigationTracker.stop();
    }
  }

  private boolean isReadinessRelevant(ConsoleEvent event, ConsoleTrackingConfig cfg) {
    if (event == null || event.text() == null) {
      return false;
    }
    if (matchesAny(cfg.readinessDenylist(), event.text())) {
      return true;
    }
    if (!cfg.readinessAllowlist().isEmpty() && matchesAny(cfg.readinessAllowlist(), event.text())) {
      return true;
    }
    return false;
  }

  private boolean matchesAny(Iterable<Pattern> patterns, String text) {
    for (Pattern pattern : patterns) {
      if (pattern != null && pattern.matcher(text).find()) {
        return true;
      }
    }
    return false;
  }
}
