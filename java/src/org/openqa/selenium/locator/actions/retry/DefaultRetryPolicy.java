package org.openqa.selenium.locator.actions.retry;

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.MoveTargetOutOfBoundsException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.locator.actions.hittest.HitTestResult;
import org.openqa.selenium.locator.actions.probe.ElementState;

public class DefaultRetryPolicy implements RetryPolicy {
  @Override
  public boolean isRetryable(Exception exception) {
    return exception instanceof StaleElementReferenceException
        || exception instanceof ElementClickInterceptedException
        || exception instanceof MoveTargetOutOfBoundsException
        || exception instanceof ElementNotInteractableException;
  }

  @Override
  public boolean isEligibleForJsFallback(Exception exception, ElementState state, HitTestResult hitTest) {
    if (exception instanceof StaleElementReferenceException) {
      return false;
    }
    if (exception instanceof ElementClickInterceptedException) {
      return hitTest != null && hitTest.hit();
    }
    if (exception instanceof ElementNotInteractableException) {
      return state != null && state.visible() && state.enabled();
    }
    if (exception instanceof MoveTargetOutOfBoundsException) {
      return state != null && state.inViewport();
    }
    return false;
  }
}
