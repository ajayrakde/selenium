package org.openqa.selenium.locator.bidi.selenium.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.bidi.api.Clock;
import org.openqa.selenium.locator.bidi.config.NetworkTrackingConfig;
import org.openqa.selenium.locator.bidi.model.NetworkEvent;
import org.openqa.selenium.locator.bidi.model.NetworkResourceType;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.bidi.model.NetworkStage;

@Tag("UnitTests")
class DefaultNetworkActivityTrackerTest {
  @Test
  void tracksInflightAndFailures() {
    NetworkTrackingConfig config =
        new NetworkTrackingConfig(
            true,
            2,
            true,
            Set.of(),
            List.of(),
            false,
            List.of());
    TestClock clock = new TestClock();
    DefaultNetworkActivityTracker tracker = new DefaultNetworkActivityTracker(config, clock, null);

    tracker.start();
    tracker.onNetworkEvent(event(clock, "1", NetworkStage.REQUEST));
    tracker.onNetworkEvent(event(clock, "1", NetworkStage.RESPONSE));
    tracker.onNetworkEvent(event(clock, "1", NetworkStage.FINISHED));

    NetworkSnapshot snapshot = tracker.snapshot();
    assertThat(snapshot.inflight()).isZero();
    assertThat(snapshot.failuresCount()).isZero();
    assertThat(snapshot.lastEvents()).hasSize(2);

    tracker.onNetworkEvent(event(clock, "2", NetworkStage.REQUEST));
    tracker.onNetworkEvent(event(clock, "2", NetworkStage.FAILED));

    snapshot = tracker.snapshot();
    assertThat(snapshot.inflight()).isZero();
    assertThat(snapshot.failuresCount()).isEqualTo(1);
  }

  @Test
  void stopPreventsRecording() {
    NetworkTrackingConfig config =
        new NetworkTrackingConfig(
            true,
            2,
            true,
            Set.of(),
            List.of(),
            false,
            List.of());
    TestClock clock = new TestClock();
    DefaultNetworkActivityTracker tracker = new DefaultNetworkActivityTracker(config, clock, null);

    tracker.start();
    tracker.stop();

    tracker.onNetworkEvent(event(clock, "1", NetworkStage.REQUEST));
    NetworkSnapshot snapshot = tracker.snapshot();

    assertThat(snapshot.inflight()).isZero();
    assertThat(snapshot.lastEvents()).isEmpty();
  }

  private NetworkEvent event(TestClock clock, String requestId, NetworkStage stage) {
    return new NetworkEvent(
        clock.nowEpochMs(),
        requestId,
        "https://example.test/api",
        "GET",
        NetworkResourceType.XHR,
        stage,
        stage == NetworkStage.RESPONSE || stage == NetworkStage.FINISHED ? 200 : null,
        stage == NetworkStage.FAILED ? "boom" : null);
  }

  private static final class TestClock implements Clock {
    private final AtomicLong value = new AtomicLong(1000L);

    @Override
    public long nowEpochMs() {
      return value.getAndIncrement();
    }
  }
}
