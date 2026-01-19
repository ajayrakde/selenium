package org.openqa.selenium.locator;

/**
 * Raised when scope resolution fails.
 */
public class LocatorScopeException extends LocatorException {
  public LocatorScopeException(String message, ResolutionDiagnostics diagnostics) {
    super(message, diagnostics);
  }

  public LocatorScopeException(String message, Throwable cause, ResolutionDiagnostics diagnostics) {
    super(message, cause, diagnostics);
  }
}
