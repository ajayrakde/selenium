package org.openqa.selenium.locator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable plan describing how to resolve a locator.
 */
public final class LocatorPlan {
  private final String name;
  private final ScopePlan scope;
  private final List<SelectorStep> steps;
  private final List<FilterSpec> filters;
  private final Strictness strictness;
  private final SelectorTier tier;
  private final Map<String, Object> tags;

  public LocatorPlan(
      String name,
      ScopePlan scope,
      List<SelectorStep> steps,
      List<FilterSpec> filters,
      Strictness strictness,
      SelectorTier tier,
      Map<String, Object> tags) {
    this.name = Objects.requireNonNull(name, "name");
    this.scope = Objects.requireNonNull(scope, "scope");
    this.steps = Collections.unmodifiableList(List.copyOf(Objects.requireNonNull(steps, "steps")));
    this.filters = Collections.unmodifiableList(List.copyOf(Objects.requireNonNull(filters, "filters")));
    this.strictness = Objects.requireNonNull(strictness, "strictness");
    this.tier = Objects.requireNonNull(tier, "tier");
    this.tags = Collections.unmodifiableMap(Map.copyOf(Objects.requireNonNull(tags, "tags")));
  }

  public String name() {
    return name;
  }

  public ScopePlan scope() {
    return scope;
  }

  public List<SelectorStep> steps() {
    return steps;
  }

  public List<FilterSpec> filters() {
    return filters;
  }

  public Strictness strictness() {
    return strictness;
  }

  public SelectorTier tier() {
    return tier;
  }

  public Map<String, Object> tags() {
    return tags;
  }
}
