package org.openqa.selenium.locator.bidi.selenium.impl;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.openqa.selenium.locator.DiagnosticsSink;
import org.openqa.selenium.locator.bidi.api.Clock;
import org.openqa.selenium.locator.bidi.api.ConsoleTracker;
import org.openqa.selenium.locator.bidi.config.ConsoleTrackingConfig;
import org.openqa.selenium.locator.bidi.model.BidiDiagnostics;
import org.openqa.selenium.locator.bidi.model.ConsoleEvent;
import org.openqa.selenium.locator.bidi.model.ConsoleLevel;
import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.bidi.selenium.util.RingBuffer;

final class DefaultConsoleTracker implements ConsoleTracker {

  private final ConsoleTrackingConfig config;
  private final Clock clock;
  private final DiagnosticsSink diagnosticsSink;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final EnumMap<ConsoleLevel, AtomicInteger> countsByLevel;
  private final Set<ConsoleLevel> allowedLevels;
  private final AtomicLong lastActivityEpochMs = new AtomicLong(0L);
  private final RingBuffer<ConsoleEvent> lastEvents;

  DefaultConsoleTracker(ConsoleTrackingConfig config, Clock clock, DiagnosticsSink diagnosticsSink) {
    this.config = Objects.requireNonNull(config, "config");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.diagnosticsSink = diagnosticsSink;
    this.lastEvents = new RingBuffer<>(Math.max(1, config.ringBufferSize()));
    this.countsByLevel = new EnumMap<>(ConsoleLevel.class);
    for (ConsoleLevel level : ConsoleLevel.values()) {
      countsByLevel.put(level, new AtomicInteger());
    }
    this.allowedLevels =
        config.levels().isEmpty() ? EnumSet.allOf(ConsoleLevel.class) : config.levels();
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
  public ConsoleSnapshot snapshot() {
    long ts = clock.nowEpochMs();
    Map<ConsoleLevel, Integer> countsCopy = new EnumMap<>(ConsoleLevel.class);
    for (Map.Entry<ConsoleLevel, AtomicInteger> entry : countsByLevel.entrySet()) {
      countsCopy.put(entry.getKey(), entry.getValue().get());
    }
    List<ConsoleEvent> events = lastEvents.snapshot();
    return new ConsoleSnapshot(ts, countsCopy, lastActivityEpochMs.get(), events);
  }

  void onConsoleEvent(ConsoleEvent event, boolean readinessRelevant) {
    if (!running.get() || event == null) {
      return;
    }
    if (!allowedLevels.contains(event.level())) {
      return;
    }

    lastActivityEpochMs.set(event.tsEpochMs());
    countsByLevel.get(event.level()).incrementAndGet();
    lastEvents.add(event);

    if (event.level() == ConsoleLevel.ERROR) {
      emitDiagnostics(readinessRelevant);
    }
  }

  private void emitDiagnostics(boolean readinessRelevant) {
    if (diagnosticsSink == null) {
      return;
    }
    diagnosticsSink.record(
        new BidiDiagnostics(
            "bidi.console.error",
            clock.nowEpochMs(),
            Map.of(
                "errors",
                countsByLevel.get(ConsoleLevel.ERROR).get(),
                "lastActivity",
                lastActivityEpochMs.get(),
                "readinessRelevant",
                readinessRelevant)));
  }
}
