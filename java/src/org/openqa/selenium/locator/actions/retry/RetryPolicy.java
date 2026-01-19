package org.openqa.selenium.locator.actions.retry;

import org.openqa.selenium.locator.actions.hittest.HitTestResult;
import org.openqa.selenium.locator.actions.probe.ElementState;

public interface RetryPolicy {
  boolean isRetryable(Exception exception);

  boolean isEligibleForJsFallback(Exception exception, ElementState state, HitTestResult hitTest);
}
