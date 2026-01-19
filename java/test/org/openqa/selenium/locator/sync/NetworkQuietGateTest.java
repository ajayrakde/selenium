package org.openqa.selenium.locator.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.api.ConsoleTracker;
import org.openqa.selenium.locator.bidi.api.NavigationTracker;
import org.openqa.selenium.locator.bidi.api.NetworkActivityTracker;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.sync.policy.ConsolePolicy;
import org.openqa.selenium.locator.sync.policy.Gate;
import org.openqa.selenium.locator.sync.policy.NetworkPolicy;
import org.openqa.selenium.locator.sync.policy.QuietWindows;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.policy.StabilityPolicy;
import org.openqa.selenium.locator.sync.selenium.gates.NetworkQuietGate;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;

class NetworkQuietGateTest {
  @Test
  void waitsForNetworkQuietWindow() {
    FakeNetworkTracker tracker = new FakeNetworkTracker();
    BidiSignals bidi = new FakeBidiSignals(tracker);
    NetworkQuietGate gate = new NetworkQuietGate(bidi);
    ReadinessProfile profile = profile(Duration.ofMillis(100));

    long now = 1000L;
    tracker.setSnapshot(new NetworkSnapshot(now, 1, now, Map.of(), 0, List.of()));
    GateCheckResult inflight = gate.check(Intent.NAVIGATE, new WaitTargetNone(), profile, now + 10);
    assertThat(inflight.isTrueNow()).isFalse();

    tracker.setSnapshot(new NetworkSnapshot(now + 50, 0, now + 50, Map.of(), 0, List.of()));
    GateCheckResult early = gate.check(Intent.NAVIGATE, new WaitTargetNone(), profile, now + 120);
    assertThat(early.isTrueNow()).isFalse();

    GateCheckResult quiet = gate.check(Intent.NAVIGATE, new WaitTargetNone(), profile, now + 170);
    assertThat(quiet.isTrueNow()).isTrue();
  }

  private ReadinessProfile profile(Duration networkQuiet) {
    return new ReadinessProfile(
        Set.of(Gate.NETWORK_QUIET),
        new QuietWindows(Duration.ZERO, networkQuiet, Duration.ZERO, Duration.ZERO, Duration.ZERO),
        new ConsolePolicy(false, false, List.of(), List.of()),
        new NetworkPolicy(true, Set.of(), List.of(), true),
        new StabilityPolicy(false, false, 0.5));
  }

  private static class FakeBidiSignals implements BidiSignals {
    private final NetworkActivityTracker tracker;

    FakeBidiSignals(NetworkActivityTracker tracker) {
      this.tracker = tracker;
    }

    @Override
    public BidiCapability capability() {
      return new BidiCapability(
          BidiCapabilityStatus.SUPPORTED,
          "ok",
          Set.of(BidiFeature.NETWORK));
    }

    @Override
    public NetworkActivityTracker network() {
      return tracker;
    }

    @Override
    public ConsoleTracker console() {
      throw new UnsupportedOperationException("console");
    }

    @Override
    public NavigationTracker navigation() {
      throw new UnsupportedOperationException("navigation");
    }

    @Override
    public org.openqa.selenium.locator.bidi.model.BidiSnapshotBundle snapshotAll() {
      throw new UnsupportedOperationException("snapshotAll");
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
      return true;
    }
  }

  private static class FakeNetworkTracker implements NetworkActivityTracker {
    private NetworkSnapshot snapshot;

    void setSnapshot(NetworkSnapshot snapshot) {
      this.snapshot = snapshot;
    }

    @Override
    public NetworkSnapshot snapshot() {
      return snapshot;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
      return true;
    }
  }
}
