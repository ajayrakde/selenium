package org.openqa.selenium.locator;

/**
 * Raised when a strict locator matches multiple elements.
 */
public class LocatorAmbiguousException extends LocatorException {
  public LocatorAmbiguousException(String message, ResolutionDiagnostics diagnostics) {
    super(message, diagnostics);
  }

  public LocatorAmbiguousException(String message, Throwable cause, ResolutionDiagnostics diagnostics) {
    super(message, cause, diagnostics);
  }
}
