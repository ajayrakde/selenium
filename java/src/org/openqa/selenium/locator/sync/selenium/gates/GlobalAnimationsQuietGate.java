package org.openqa.selenium.locator.sync.selenium.gates;

import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.impl.GlobalStabilitySnapshotProvider;

public class GlobalAnimationsQuietGate implements GateEvaluator, GlobalStabilitySnapshotProvider {
  private final StabilityService stability;
  private GlobalStabilitySnapshot lastSnapshot;

  public GlobalAnimationsQuietGate(StabilityService stability) {
    this.stability = stability;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    lastSnapshot = stability.sampleGlobal();
    if (!lastSnapshot.animationsSupported() || lastSnapshot.animationsRunning() == null) {
      return new GateCheckResult(true, "animations_unknown_skipped");
    }
    boolean ok = !lastSnapshot.animationsRunning();
    return new GateCheckResult(ok, "animationsRunning=" + lastSnapshot.animationsRunning());
  }

  @Override
  public GlobalStabilitySnapshot lastGlobalSnapshot() {
    return lastSnapshot;
  }
}
