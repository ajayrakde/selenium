package org.openqa.selenium.locator.stability.model;

public record StabilityConfig(
    double rectTolerancePx,
    boolean captureAnimationsFlag,
    boolean captureVisibilityFlag,
    int maxSamplesInHistory) {
  public static StabilityConfig defaultConfig() {
    return new StabilityConfig(0.5, true, true, 20);
  }
}
