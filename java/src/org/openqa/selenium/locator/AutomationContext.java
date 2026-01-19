package org.openqa.selenium.locator;

import org.openqa.selenium.locator.actions.ActionPerformer;
import org.openqa.selenium.locator.actions.ActionPolicy;

/**
 * Session-level context for locator resolution.
 */
public interface AutomationContext {
  LocatorFactory locator();

  Policy policy();

  ActionPolicy actionPolicy();

  ActionPerformer actionPerformer();

  DiagnosticsSink diagnostics();

  Unsafe unsafe();
}
