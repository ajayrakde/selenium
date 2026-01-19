package org.openqa.selenium.locator.bidi.selenium.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.DiagnosticsSink;
import org.openqa.selenium.locator.bidi.api.Clock;
import org.openqa.selenium.locator.bidi.api.NavigationTracker;
import org.openqa.selenium.locator.bidi.config.NavigationTrackingConfig;
import org.openqa.selenium.locator.bidi.model.BidiDiagnostics;
import org.openqa.selenium.locator.bidi.model.NavEventType;
import org.openqa.selenium.locator.bidi.model.NavigationEvent;
import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;
import org.openqa.selenium.locator.bidi.selenium.util.RingBuffer;

final class DefaultNavigationTracker implements NavigationTracker {

  private final NavigationTrackingConfig config;
  private final Clock clock;
  private final DiagnosticsSink diagnosticsSink;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicReference<String> currentUrl = new AtomicReference<>(null);
  private final AtomicLong lastActivityEpochMs = new AtomicLong(0L);
  private final RingBuffer<NavigationEvent> lastEvents;

  DefaultNavigationTracker(
      NavigationTrackingConfig config, Clock clock, DiagnosticsSink diagnosticsSink) {
    this.config = config;
    this.clock = clock;
    this.diagnosticsSink = diagnosticsSink;
    this.lastEvents = new RingBuffer<>(Math.max(1, config.ringBufferSize()));
  }

  @Override
  public void start() {
    running.set(true);
  }

  @Override
  public void stop() {
    running.set(false);
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public NavigationSnapshot snapshot() {
    long ts = clock.nowEpochMs();
    List<NavigationEvent> events = lastEvents.snapshot();
    return new NavigationSnapshot(ts, currentUrl.get(), lastActivityEpochMs.get(), events);
  }

  void onNavigationEvent(NavigationEvent event) {
    if (!running.get() || event == null) {
      return;
    }
    if (shouldIgnore(event.url())) {
      return;
    }

    lastActivityEpochMs.set(event.tsEpochMs());
    if (event.type() == NavEventType.URL_CHANGED) {
      currentUrl.set(event.url());
      emitDiagnostics();
    }
    lastEvents.add(event);
  }

  private boolean shouldIgnore(String url) {
    if (url == null || config.ignoredUrlPatterns().isEmpty()) {
      return false;
    }
    for (Pattern pattern : config.ignoredUrlPatterns()) {
      if (pattern != null && pattern.matcher(url).find()) {
        return true;
      }
    }
    return false;
  }

  private void emitDiagnostics() {
    if (diagnosticsSink == null) {
      return;
    }
    diagnosticsSink.record(
        new BidiDiagnostics(
            "bidi.navigation.changed",
            clock.nowEpochMs(),
            java.util.Map.of(
                "url", currentUrl.get(), "lastActivity", lastActivityEpochMs.get())));
  }
}
