package org.openqa.selenium.locator;

/**
 * Diagnostics sink for locator resolution events.
 */
public interface DiagnosticsSink {
  void record(ResolutionDiagnostics diagnostics);
}
