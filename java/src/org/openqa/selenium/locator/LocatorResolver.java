package org.openqa.selenium.locator;

/**
 * Resolves locator plans against a session context.
 */
public interface LocatorResolver {
  ResolutionResult resolve(AutomationContext context, LocatorPlan plan, ResolveOptions options);
}
