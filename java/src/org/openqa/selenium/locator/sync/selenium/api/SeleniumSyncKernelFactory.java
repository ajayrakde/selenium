package org.openqa.selenium.locator.sync.selenium.api;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.sync.SyncKernel;
import org.openqa.selenium.locator.sync.policy.SyncPolicy;
import org.openqa.selenium.locator.sync.selenium.impl.DefaultSyncKernel;

public final class SeleniumSyncKernelFactory {
  private SeleniumSyncKernelFactory() {}

  public static SyncKernel create(
      WebDriver driver,
      SyncPolicy policy,
      BidiSignals bidiSignals,
      StabilityService stabilityService) {
    return new DefaultSyncKernel(driver, policy, bidiSignals, stabilityService);
  }
}
