package org.openqa.selenium.locator;

/**
 * Base exception for locator resolution failures.
 */
public abstract class LocatorException extends RuntimeException {
  private final ResolutionDiagnostics diagnostics;

  protected LocatorException(String message, ResolutionDiagnostics diagnostics) {
    super(message);
    this.diagnostics = diagnostics;
  }

  protected LocatorException(String message, Throwable cause, ResolutionDiagnostics diagnostics) {
    super(message, cause);
    this.diagnostics = diagnostics;
  }

  public ResolutionDiagnostics diagnostics() {
    return diagnostics;
  }
}
