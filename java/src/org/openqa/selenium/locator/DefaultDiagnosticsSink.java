package org.openqa.selenium.locator;

/**
 * No-op diagnostics sink.
 */
public class DefaultDiagnosticsSink implements DiagnosticsSink {
  @Override
  public void record(ResolutionDiagnostics diagnostics) {
    // Intentionally empty.
  }
}
