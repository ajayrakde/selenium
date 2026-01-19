package org.openqa.selenium.locator;

import java.util.Objects;
import org.openqa.selenium.locator.actions.ActionPerformer;
import org.openqa.selenium.locator.actions.ActionPolicy;

/**
 * Default automation context implementation.
 */
public class DefaultAutomationContext implements AutomationContext {
  private final LocatorFactory locatorFactory;
  private final Policy policy;
  private final ActionPolicy actionPolicy;
  private final ActionPerformer actionPerformer;
  private final DiagnosticsSink diagnostics;
  private final Unsafe unsafe;

  public DefaultAutomationContext(
      LocatorFactory locatorFactory,
      Policy policy,
      ActionPolicy actionPolicy,
      ActionPerformer actionPerformer,
      DiagnosticsSink diagnostics,
      Unsafe unsafe) {
    this.locatorFactory = Objects.requireNonNull(locatorFactory, "locatorFactory");
    this.policy = Objects.requireNonNull(policy, "policy");
    this.actionPolicy = Objects.requireNonNull(actionPolicy, "actionPolicy");
    this.actionPerformer = Objects.requireNonNull(actionPerformer, "actionPerformer");
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
  public ActionPolicy actionPolicy() {
    return actionPolicy;
  }

  @Override
  public ActionPerformer actionPerformer() {
    return actionPerformer;
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
