package org.openqa.selenium.locator.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.stability.api.StabilityEvaluator;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.Rect;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.model.VisibilityFlags;
import org.openqa.selenium.locator.sync.policy.ConsolePolicy;
import org.openqa.selenium.locator.sync.policy.Gate;
import org.openqa.selenium.locator.sync.policy.NetworkPolicy;
import org.openqa.selenium.locator.sync.policy.QuietWindows;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.policy.StabilityPolicy;
import org.openqa.selenium.locator.sync.selenium.gates.TargetStableGate;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;

class TargetStableGateTest {
  @Test
  void usesStabilityEvaluator() {
    FakeStabilityService stability = new FakeStabilityService();
    StabilityEvaluator evaluator = new FakeStabilityEvaluator();
    TargetStableGate gate = new TargetStableGate(stability, evaluator);
    ReadinessProfile profile = profile();

    GateCheckResult first =
        gate.check(Intent.CLICK, new WaitTargetElement(new Object()), profile, 1000L);
    assertThat(first.isTrueNow()).isFalse();

    GateCheckResult second =
        gate.check(Intent.CLICK, new WaitTargetElement(new Object()), profile, 1100L);
    assertThat(second.isTrueNow()).isTrue();
  }

  private ReadinessProfile profile() {
    return new ReadinessProfile(
        Set.of(Gate.TARGET_STABLE),
        new QuietWindows(Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ofMillis(300)),
        new ConsolePolicy(false, false, List.of(), List.of()),
        new NetworkPolicy(false, Set.of(), List.of(), true),
        new StabilityPolicy(true, true, 0.5));
  }

  private static class FakeStabilityService implements StabilityService {
    private long counter = 0;

    @Override
    public StabilitySnapshot sample(Object elementHandle) {
      return sample(elementHandle, StabilityConfig.defaultConfig());
    }

    @Override
    public StabilitySnapshot sample(Object elementHandle, StabilityConfig cfg) {
      counter += 1;
      return new StabilitySnapshot(counter, new Rect(0, 0, 10, 10), new VisibilityFlags(true, true, true), false, "");
    }

    @Override
    public GlobalStabilitySnapshot sampleGlobal() {
      return new GlobalStabilitySnapshot(counter, true, false, "");
    }

    @Override
    public GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg) {
      return sampleGlobal();
    }

    @Override
    public StabilityHistory append(
        StabilityHistory history, StabilitySnapshot snapshot, StabilityConfig cfg) {
      List<StabilitySnapshot> samples = new ArrayList<>(history.samples());
      samples.add(snapshot);
      return new StabilityHistory(samples);
    }
  }

  private static class FakeStabilityEvaluator implements StabilityEvaluator {
    @Override
    public boolean isRectStable(StabilityHistory history, long windowMs, double tolerancePx) {
      return history.samples().size() >= 2;
    }

    @Override
    public boolean isVisibilityStable(StabilityHistory history, long windowMs) {
      return history.samples().size() >= 2;
    }

    @Override
    public boolean isStable(StabilityHistory history, long windowMs, StabilityConfig cfg) {
      return history.samples().size() >= 2;
    }
  }
}
