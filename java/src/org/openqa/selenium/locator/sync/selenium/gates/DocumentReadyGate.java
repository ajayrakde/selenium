package org.openqa.selenium.locator.sync.selenium.gates;

import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.impl.ReadyStateProvider;
import org.openqa.selenium.locator.sync.selenium.js.JsReadyStateProbe;

public class DocumentReadyGate implements GateEvaluator, ReadyStateProvider {
  private final JsReadyStateProbe probe;
  private String lastReadyState = "unknown";

  public DocumentReadyGate(JsReadyStateProbe probe) {
    this.probe = probe;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    lastReadyState = probe.readReadyState();
    boolean ok = "interactive".equals(lastReadyState) || "complete".equals(lastReadyState);
    return new GateCheckResult(ok, "readyState=" + lastReadyState);
  }

  @Override
  public String lastReadyState() {
    return lastReadyState;
  }
}
