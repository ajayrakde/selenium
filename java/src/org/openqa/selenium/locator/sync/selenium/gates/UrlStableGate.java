package org.openqa.selenium.locator.sync.selenium.gates;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;

public class UrlStableGate implements GateEvaluator {
  private final WebDriver driver;
  private String lastUrl;
  private long lastChange = -1;

  public UrlStableGate(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    String url;
    try {
      url = driver.getCurrentUrl();
    } catch (Exception e) {
      return new GateCheckResult(false, "url=error");
    }

    if (lastUrl == null) {
      lastUrl = url;
      lastChange = now;
      return new GateCheckResult(false, "url_init");
    }
    if (!url.equals(lastUrl)) {
      lastUrl = url;
      lastChange = now;
      return new GateCheckResult(false, "url_changed");
    }
    return new GateCheckResult(true, "url_stable_since=" + lastChange);
  }
}
