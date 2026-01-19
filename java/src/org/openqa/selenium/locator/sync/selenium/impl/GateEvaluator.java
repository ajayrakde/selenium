package org.openqa.selenium.locator.sync.selenium.impl;

import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;

public interface GateEvaluator {
  GateCheckResult check(Intent intent, WaitTarget target, ReadinessProfile profile, long nowEpochMs);
}
