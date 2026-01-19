package org.openqa.selenium.locator;

import org.openqa.selenium.WebDriver;

/**
 * Session-level context for locator resolution.
 */
public interface AutomationContext {
  WebDriver driver();

  LocatorFactory locator();

  Policy policy();

  Diagnostics diagnostics();
}
