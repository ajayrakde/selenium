package org.openqa.selenium.locator.stability.api;

import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;

public interface StabilityEvaluator {
  boolean isRectStable(StabilityHistory history, long windowMs, double tolerancePx);

  boolean isVisibilityStable(StabilityHistory history, long windowMs);

  boolean isStable(StabilityHistory history, long windowMs, StabilityConfig cfg);
}
