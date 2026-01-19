package org.openqa.selenium.locator;

/**
 * Raised when a locator selector is invalid.
 */
public class LocatorInvalidSelectorException extends LocatorException {
  public LocatorInvalidSelectorException(String message, ResolutionDiagnostics diagnostics) {
    super(message, diagnostics);
  }

  public LocatorInvalidSelectorException(String message, Throwable cause, ResolutionDiagnostics diagnostics) {
    super(message, cause, diagnostics);
  }
}
