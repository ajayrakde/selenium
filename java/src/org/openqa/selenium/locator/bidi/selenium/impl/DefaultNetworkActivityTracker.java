package org.openqa.selenium.locator.bidi.selenium.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.openqa.selenium.locator.DiagnosticsSink;
import org.openqa.selenium.locator.bidi.api.Clock;
import org.openqa.selenium.locator.bidi.api.NetworkActivityTracker;
import org.openqa.selenium.locator.bidi.config.NetworkTrackingConfig;
import org.openqa.selenium.locator.bidi.model.BidiDiagnostics;
import org.openqa.selenium.locator.bidi.model.NetworkEvent;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.bidi.model.NetworkStage;
import org.openqa.selenium.locator.bidi.selenium.util.RingBuffer;

final class DefaultNetworkActivityTracker implements NetworkActivityTracker {

  private final NetworkTrackingConfig config;
  private final Clock clock;
  private final DiagnosticsSink diagnosticsSink;
  private final NetworkIgnoreMatcher ignoreMatcher;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicInteger inflightTotal = new AtomicInteger();
  private final ConcurrentHashMap<String, RequestInfo> inflightByRequest =
      new ConcurrentHashMap<>();
  private final EnumMap<NetworkResourceType, AtomicInteger> inflightByType;
  private final AtomicLong lastActivityEpochMs = new AtomicLong(0L);
  private final AtomicInteger failuresCount = new AtomicInteger();
  private final RingBuffer<NetworkEvent> lastEvents;

  DefaultNetworkActivityTracker(
      NetworkTrackingConfig config, Clock clock, DiagnosticsSink diagnosticsSink) {
    this.config = Objects.requireNonNull(config, "config");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.diagnosticsSink = diagnosticsSink;
    this.ignoreMatcher = new NetworkIgnoreMatcher(config);
    this.lastEvents = new RingBuffer<>(Math.max(1, config.ringBufferSize()));
    this.inflightByType = new EnumMap<>(NetworkResourceType.class);
    for (NetworkResourceType type : NetworkResourceType.values()) {
      inflightByType.put(type, new AtomicInteger());
    }
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
  public NetworkSnapshot snapshot() {
    long ts = clock.nowEpochMs();
    Map<NetworkResourceType, Integer> inflightCopy = new EnumMap<>(NetworkResourceType.class);
    for (Map.Entry<NetworkResourceType, AtomicInteger> entry : inflightByType.entrySet()) {
      inflightCopy.put(entry.getKey(), entry.getValue().get());
    }
    List<NetworkEvent> events = lastEvents.snapshot();
    return new NetworkSnapshot(
        ts,
        inflightTotal.get(),
        lastActivityEpochMs.get(),
        inflightCopy,
        failuresCount.get(),
        events);
  }

  void onNetworkEvent(NetworkEvent event) {
    if (!running.get() || event == null) {
      return;
    }

    if (ignoreMatcher.shouldIgnore(event.type(), event.url())) {
      return;
    }

    lastActivityEpochMs.set(event.tsEpochMs());

    switch (event.stage()) {
      case REQUEST -> handleRequest(event);
      case RESPONSE -> handleResponse(event);
      case FINISHED -> handleFinished(event, false);
      case FAILED -> handleFinished(event, true);
    }

    lastEvents.add(event);
  }

  private void handleRequest(NetworkEvent event) {
    if (event.requestId() != null) {
      inflightByRequest.putIfAbsent(
          event.requestId(), new RequestInfo(event.url(), event.method(), event.type()));
    }
    inflightTotal.incrementAndGet();
    inflightByType.getOrDefault(event.type(), inflightByType.get(NetworkResourceType.OTHER))
        .incrementAndGet();
    emitDiagnostics();
  }

  private void handleResponse(NetworkEvent event) {
    emitDiagnostics();
  }

  private void handleFinished(NetworkEvent event, boolean failed) {
    NetworkResourceType type = event.type();
    if (event.requestId() != null) {
      RequestInfo info = inflightByRequest.remove(event.requestId());
      if (info != null && info.type != null) {
        type = info.type;
      }
    }

    decrementIfPositive(inflightTotal);
    decrementIfPositive(
        inflightByType.getOrDefault(type, inflightByType.get(NetworkResourceType.OTHER)));

    if (failed && config.recordFailures()) {
      failuresCount.incrementAndGet();
    }
    emitDiagnostics();
  }

  private void emitDiagnostics() {
    if (diagnosticsSink == null) {
      return;
    }
    diagnosticsSink.record(
        new BidiDiagnostics(
            "bidi.network.activity",
            clock.nowEpochMs(),
            Map.of(
                "inflight",
                inflightTotal.get(),
                "failures",
                failuresCount.get(),
                "lastActivity",
                lastActivityEpochMs.get())));
  }

  private void decrementIfPositive(AtomicInteger counter) {
    counter.updateAndGet(value -> value > 0 ? value - 1 : value);
  }

  private static final class RequestInfo {
    private final String url;
    private final String method;
    private final NetworkResourceType type;

    private RequestInfo(String url, String method, NetworkResourceType type) {
      this.url = url;
      this.method = method;
      this.type = type;
    }
  }
}
