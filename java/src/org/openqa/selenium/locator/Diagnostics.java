package org.openqa.selenium.locator;

/**
 * Diagnostics sink for locator resolution events.
 */
public interface Diagnostics {
  void record(ResolutionDiagnostics diagnostics);
}
