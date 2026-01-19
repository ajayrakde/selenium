package org.openqa.selenium.locator;

import java.util.Objects;

/**
 * Default automation context implementation.
 */
public class DefaultAutomationContext implements AutomationContext {
  private final LocatorFactory locatorFactory;
  private final Policy policy;
  private final DiagnosticsSink diagnostics;
  private final Unsafe unsafe;

  public DefaultAutomationContext(
      LocatorFactory locatorFactory,
      Policy policy,
      DiagnosticsSink diagnostics,
      Unsafe unsafe) {
    this.locatorFactory = Objects.requireNonNull(locatorFactory, "locatorFactory");
    this.policy = Objects.requireNonNull(policy, "policy");
    this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    this.unsafe = Objects.requireNonNull(unsafe, "unsafe");
  }

  @Override
  public LocatorFactory locator() {
    return locatorFactory;
  }

  @Override
  public Policy policy() {
    return policy;
  }

  @Override
  public DiagnosticsSink diagnostics() {
    return diagnostics;
  }

  @Override
  public Unsafe unsafe() {
    return unsafe;
  }
}
