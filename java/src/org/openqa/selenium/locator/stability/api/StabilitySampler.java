package org.openqa.selenium.locator.stability.api;

import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;

public interface StabilitySampler {
  StabilitySnapshot sampleElement(Object elementHandle, StabilityConfig cfg);

  StabilitySnapshot sampleContainer(Object elementHandle, StabilityConfig cfg);

  GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg);
}
