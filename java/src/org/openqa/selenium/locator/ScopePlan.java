package org.openqa.selenium.locator;

/**
 * Represents the root scope for a locator plan.
 */
public sealed interface ScopePlan permits RootScope, WithinScope {}

public record RootScope() implements ScopePlan {}

public record WithinScope(LocatorPlan container) implements ScopePlan {}
