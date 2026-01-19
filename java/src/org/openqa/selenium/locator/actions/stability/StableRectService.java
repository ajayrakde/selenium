package org.openqa.selenium.locator.actions.stability;

import java.time.Duration;
import java.time.Instant;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.locator.actions.probe.ElementRect;
import org.openqa.selenium.locator.actions.probe.ElementState;
import org.openqa.selenium.locator.actions.probe.ElementStateProbe;

public class StableRectService implements StabilityService {
  @Override
  public boolean isStable(
      WebDriver driver,
      WebElement element,
      ElementStateProbe probe,
      Duration window,
      Duration poll,
      double tolerancePx) {
    Instant start = Instant.now();
    Instant stableSince = Instant.now();
    ElementRect lastRect = null;
    while (Duration.between(start, Instant.now()).compareTo(window) < 0) {
      ElementState state = probe.probe(driver, element);
      ElementRect rect = state.rect();
      if (lastRect != null && !rectsStable(lastRect, rect, tolerancePx)) {
        stableSince = Instant.now();
      }
      lastRect = rect;
      if (Duration.between(stableSince, Instant.now()).compareTo(window) >= 0) {
        return true;
      }
      sleep(poll);
    }
    return false;
  }

  @Override
  public boolean rectsStable(ElementRect previous, ElementRect current, double tolerancePx) {
    return withinTolerance(previous.left(), current.left(), tolerancePx)
        && withinTolerance(previous.top(), current.top(), tolerancePx)
        && withinTolerance(previous.width(), current.width(), tolerancePx)
        && withinTolerance(previous.height(), current.height(), tolerancePx);
  }

  private boolean withinTolerance(double a, double b, double tolerance) {
    return Math.abs(a - b) <= tolerance;
  }

  private void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
