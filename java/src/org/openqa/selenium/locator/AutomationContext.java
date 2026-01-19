package org.openqa.selenium.locator;

/**
 * Session-level context for locator resolution.
 */
public interface AutomationContext {
  LocatorFactory locator();

  Policy policy();

  DiagnosticsSink diagnostics();

  Unsafe unsafe();
}
