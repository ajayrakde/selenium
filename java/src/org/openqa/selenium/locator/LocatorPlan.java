package org.openqa.selenium.locator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable plan describing how to resolve a locator.
 */
public record LocatorPlan(
    String name,
    ScopePlan scope,
    List<SelectorStep> steps,
    List<FilterSpec> filters,
    Strictness strictness,
    SelectorTier tier,
    Map<String, Object> tags) {
  public LocatorPlan {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(scope, "scope");
    Objects.requireNonNull(steps, "steps");
    if (steps.isEmpty()) {
      throw new IllegalArgumentException("steps must contain at least one selector step");
    }
    Objects.requireNonNull(filters, "filters");
    Objects.requireNonNull(strictness, "strictness");
    Objects.requireNonNull(tier, "tier");
    Objects.requireNonNull(tags, "tags");
    steps = List.copyOf(steps);
    filters = List.copyOf(filters);
    tags = Map.copyOf(tags);
  }

  public LocatorPlan withStrictness(Strictness strictness) {
    return new LocatorPlan(name, scope, steps, filters, strictness, tier, tags);
  }

  public LocatorPlan withScope(ScopePlan scope) {
    return new LocatorPlan(name, scope, steps, filters, strictness, tier, tags);
  }

  public LocatorPlan withFilters(List<FilterSpec> filters) {
    return new LocatorPlan(name, scope, steps, filters, strictness, tier, tags);
  }

  public LocatorPlan withName(String name) {
    return new LocatorPlan(name, scope, steps, filters, strictness, tier, tags);
  }

  public LocatorPlan withTags(Map<String, Object> tags) {
    return new LocatorPlan(name, scope, steps, filters, strictness, tier, tags);
  }
}
