package org.openqa.selenium.locator;

/**
 * Raised when a locator violates policy constraints.
 */
public class LocatorPolicyViolationException extends LocatorException {
  public LocatorPolicyViolationException(String message, ResolutionDiagnostics diagnostics) {
    super(message, diagnostics);
  }

  public LocatorPolicyViolationException(String message, Throwable cause, ResolutionDiagnostics diagnostics) {
    super(message, cause, diagnostics);
  }
}
