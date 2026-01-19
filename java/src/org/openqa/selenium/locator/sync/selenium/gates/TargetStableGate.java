package org.openqa.selenium.locator.sync.selenium.gates;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.locator.stability.api.StabilityEvaluator;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.WaitTargetContainer;
import org.openqa.selenium.locator.sync.WaitTargetElement;
import org.openqa.selenium.locator.sync.WaitTargetNone;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.impl.StabilityHistoryProvider;

public class TargetStableGate implements GateEvaluator, StabilityHistoryProvider {
  private final StabilityService stability;
  private final StabilityEvaluator evaluator;
  private StabilityHistory history = new StabilityHistory(List.of());

  public TargetStableGate(StabilityService stability, StabilityEvaluator evaluator) {
    this.stability = stability;
    this.evaluator = evaluator;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    if (target instanceof WaitTargetNone) {
      return new GateCheckResult(true, "no_target");
    }

    Object handle = null;
    if (target instanceof WaitTargetElement element) {
      handle = element.elementHandle();
    } else if (target instanceof WaitTargetContainer container) {
      handle = container.elementHandle();
    }

    if (handle == null) {
      return new GateCheckResult(false, "target_null");
    }

    StabilityConfig cfg =
        new StabilityConfig(
            profile.stabilityPolicy().rectTolerancePx(),
            true,
            true,
            StabilityConfig.defaultConfig().maxSamplesInHistory());
    StabilitySnapshot snapshot = stability.sample(handle, cfg);
    history = stability.append(history, snapshot, cfg);

    long windowMs = profile.quietWindows().stabilityQuiet().toMillis();
    boolean stable = evaluator.isStable(history, windowMs, cfg);
    return new GateCheckResult(stable, stable ? "target_stable" : "target_unstable");
  }

  @Override
  public StabilityHistory history() {
    return history;
  }

  List<StabilitySnapshot> samples() {
    return new ArrayList<>(history.samples());
  }
}
