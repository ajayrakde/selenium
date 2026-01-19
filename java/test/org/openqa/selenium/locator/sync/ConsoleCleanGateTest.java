package org.openqa.selenium.locator.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.api.ConsoleTracker;
import org.openqa.selenium.locator.bidi.api.NavigationTracker;
import org.openqa.selenium.locator.bidi.api.NetworkActivityTracker;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.ConsoleEvent;
import org.openqa.selenium.locator.bidi.model.ConsoleLevel;
import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.sync.policy.ConsolePolicy;
import org.openqa.selenium.locator.sync.policy.Gate;
import org.openqa.selenium.locator.sync.policy.NetworkPolicy;
import org.openqa.selenium.locator.sync.policy.QuietWindows;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.policy.StabilityPolicy;
import org.openqa.selenium.locator.sync.selenium.gates.ConsoleCleanGate;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;

class ConsoleCleanGateTest {
  @Test
  void waitsForConsoleQuietWindow() {
    FakeConsoleTracker tracker = new FakeConsoleTracker();
    BidiSignals bidi = new FakeBidiSignals(tracker);
    ConsoleCleanGate gate = new ConsoleCleanGate(bidi);
    ReadinessProfile profile = profile(Duration.ofMillis(100));

    long now = 2000L;
    ConsoleEvent event = new ConsoleEvent(now - 50, ConsoleLevel.ERROR, "TypeError", "", "");
    tracker.setSnapshot(new ConsoleSnapshot(now, Map.of(ConsoleLevel.ERROR, 1), now, List.of(event)));

    GateCheckResult early = gate.check(Intent.ASSERT, new WaitTargetNone(), profile, now);
    assertThat(early.isTrueNow()).isFalse();

    GateCheckResult quiet = gate.check(Intent.ASSERT, new WaitTargetNone(), profile, now + 120);
    assertThat(quiet.isTrueNow()).isTrue();
  }

  private ReadinessProfile profile(Duration consoleQuiet) {
    return new ReadinessProfile(
        Set.of(Gate.CONSOLE_CLEAN),
        new QuietWindows(Duration.ZERO, Duration.ZERO, consoleQuiet, Duration.ZERO, Duration.ZERO),
        new ConsolePolicy(
            true,
            false,
            List.of(Pattern.compile("TypeError")),
            List.of()),
        new NetworkPolicy(false, Set.of(), List.of(), true),
        new StabilityPolicy(false, false, 0.5));
  }

  private static class FakeBidiSignals implements BidiSignals {
    private final ConsoleTracker tracker;

    FakeBidiSignals(ConsoleTracker tracker) {
      this.tracker = tracker;
    }

    @Override
    public BidiCapability capability() {
      return new BidiCapability(
          BidiCapabilityStatus.SUPPORTED,
          "ok",
          Set.of(BidiFeature.CONSOLE));
    }

    @Override
    public NetworkActivityTracker network() {
      throw new UnsupportedOperationException("network");
    }

    @Override
    public ConsoleTracker console() {
      return tracker;
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

  private static class FakeConsoleTracker implements ConsoleTracker {
    private ConsoleSnapshot snapshot;

    void setSnapshot(ConsoleSnapshot snapshot) {
      this.snapshot = snapshot;
    }

    @Override
    public ConsoleSnapshot snapshot() {
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
