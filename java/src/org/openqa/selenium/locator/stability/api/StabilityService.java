package org.openqa.selenium.locator.stability.api;

import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;

public interface StabilityService {
  StabilitySnapshot sample(Object elementHandle);

  StabilitySnapshot sample(Object elementHandle, StabilityConfig cfg);

  GlobalStabilitySnapshot sampleGlobal();

  GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg);

  StabilityHistory append(StabilityHistory history, StabilitySnapshot snapshot, StabilityConfig cfg);
}
