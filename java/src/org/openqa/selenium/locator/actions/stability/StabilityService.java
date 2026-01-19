package org.openqa.selenium.locator.actions.stability;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.locator.actions.probe.ElementRect;
import org.openqa.selenium.locator.actions.probe.ElementStateProbe;

public interface StabilityService {
  boolean isStable(
      WebDriver driver,
      WebElement element,
      ElementStateProbe probe,
      Duration window,
      Duration poll,
      double tolerancePx);

  boolean rectsStable(ElementRect previous, ElementRect current, double tolerancePx);
}
