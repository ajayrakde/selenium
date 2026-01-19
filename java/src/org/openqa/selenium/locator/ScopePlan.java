package org.openqa.selenium.locator;

import java.util.Objects;

/**
 * Represents the root scope for a locator plan.
 */
public final class ScopePlan {
  public enum Kind {
    ROOT,
    WITHIN
  }

  private final Kind kind;
  private final LocatorPlan containerPlan;

  private ScopePlan(Kind kind, LocatorPlan containerPlan) {
    this.kind = Objects.requireNonNull(kind, "kind");
    this.containerPlan = containerPlan;
  }

  public static ScopePlan root() {
    return new ScopePlan(Kind.ROOT, null);
  }

  public static ScopePlan within(LocatorPlan containerPlan) {
    return new ScopePlan(Kind.WITHIN, Objects.requireNonNull(containerPlan, "containerPlan"));
  }

  public Kind kind() {
    return kind;
  }

  public LocatorPlan containerPlan() {
    return containerPlan;
  }
}
