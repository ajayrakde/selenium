package org.openqa.selenium.locator;

/**
 * Raised when a locator expected a match but none were found.
 */
public class LocatorNotFoundException extends LocatorException {
  public LocatorNotFoundException(String message, ResolutionDiagnostics diagnostics) {
    super(message, diagnostics);
  }

  public LocatorNotFoundException(String message, Throwable cause, ResolutionDiagnostics diagnostics) {
    super(message, cause, diagnostics);
  }
}
