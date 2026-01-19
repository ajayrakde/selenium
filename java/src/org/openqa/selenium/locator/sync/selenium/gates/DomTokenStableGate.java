package org.openqa.selenium.locator.sync.selenium.gates;

import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.WaitTargetContainer;
import org.openqa.selenium.locator.sync.WaitTargetElement;
import org.openqa.selenium.locator.sync.WaitTargetNone;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.DomTokenProvider;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.js.JsDomTokenProbe;

public class DomTokenStableGate implements GateEvaluator, DomTokenProvider {
  private final JsDomTokenProbe probe;
  private String lastToken;
  private long lastChange = -1;

  public DomTokenStableGate(JsDomTokenProbe probe) {
    this.probe = probe;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    Object handle = null;
    if (target instanceof WaitTargetElement element) {
      handle = element.elementHandle();
    } else if (target instanceof WaitTargetContainer container) {
      handle = container.elementHandle();
    } else if (target instanceof WaitTargetNone) {
      handle = null;
    }

    String token = probe.readToken(handle);
    if (token == null) {
      return new GateCheckResult(false, "dom_token_unavailable");
    }

    if (lastToken == null) {
      lastToken = token;
      lastChange = now;
      return new GateCheckResult(false, "dom_token_init");
    }

    if (!token.equals(lastToken)) {
      lastToken = token;
      lastChange = now;
      return new GateCheckResult(false, "dom_token_changed");
    }

    return new GateCheckResult(true, "dom_token_stable_since=" + lastChange);
  }

  @Override
  public String lastDomToken() {
    return lastToken;
  }
}
