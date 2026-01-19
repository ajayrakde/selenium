package org.openqa.selenium.locator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default locator implementation.
 */
public class DefaultLocator implements Locator {
  private final AutomationContext context;
  private final LocatorResolver resolver;
  private final LocatorPlan plan;
  private final Integer nthIndex;

  DefaultLocator(AutomationContext context, LocatorResolver resolver, LocatorPlan plan, Integer nthIndex) {
    this.context = Objects.requireNonNull(context, "context");
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.plan = Objects.requireNonNull(plan, "plan");
    this.nthIndex = nthIndex;
  }

  @Override
  public String name() {
    return plan.name();
  }

  @Override
  public Locator withName(String name) {
    return new DefaultLocator(context, resolver, plan.withName(name), nthIndex);
  }

  @Override
  public Locator within(Locator container) {
    if (!(container instanceof DefaultLocator defaultContainer)) {
      throw new IllegalArgumentException("Container must be a locator created by this engine.");
    }
    LocatorPlan scoped = plan.withScope(new WithinScope(defaultContainer.plan));
    return new DefaultLocator(context, resolver, scoped, nthIndex);
  }

  @Override
  public Locator filter(FilterSpec filter) {
    List<FilterSpec> filters = new ArrayList<>(plan.filters());
    filters.add(filter);
    return new DefaultLocator(context, resolver, plan.withFilters(List.copyOf(filters)), nthIndex);
  }

  @Override
  public Locator nth(int index) {
    return new DefaultLocator(context, resolver, plan.withStrictness(Strictness.MANY), index);
  }

  @Override
  public Locator first() {
    return nth(0);
  }

  @Override
  public Locators all() {
    return new DefaultLocators(context, resolver, plan.withStrictness(Strictness.MANY));
  }

  @Override
  public boolean exists(Duration timeout) {
    Duration effectiveTimeout = timeout == null ? context.policy().defaultTimeout() : timeout;
    return pollForCount(effectiveTimeout) > 0;
  }

  @Override
  public int count(Duration timeout) {
    Duration effectiveTimeout = timeout == null ? context.policy().defaultTimeout() : timeout;
    return pollForCount(effectiveTimeout);
  }

  @Override
  public String text(Duration timeout) {
    Duration effectiveTimeout = timeout == null ? context.policy().defaultTimeout() : timeout;
    LocatorPlan resolvedPlan = plan.withStrictness(nthIndex == null ? Strictness.ONE : Strictness.MANY);
    ResolutionResult result = resolver.resolve(
        context,
        resolvedPlan,
        new ResolveOptions(effectiveTimeout, context.policy().defaultPollInterval(), false));
    if (result.matches().isEmpty()) {
      throw new LocatorNotFoundException("Locator did not match any elements.", result.diagnostics());
    }
    if (nthIndex != null) {
      if (result.matches().size() <= nthIndex) {
        throw new LocatorNotFoundException("Locator nth index out of bounds.", result.diagnostics());
      }
      return result.matches().get(nthIndex).getText();
    }
    if (result.matches().size() > 1) {
      throw new LocatorAmbiguousException("Locator matched multiple elements.", result.diagnostics());
    }
    return result.matches().get(0).getText();
  }

  LocatorPlan plan() {
    return plan;
  }

  private int pollForCount(Duration timeout) {
    Instant start = Instant.now();
    int lastCount = 0;
    do {
      ResolutionResult result = resolver.resolve(
          context,
          plan.withStrictness(Strictness.MANY),
          new ResolveOptions(Duration.ZERO, context.policy().defaultPollInterval(), false));
      lastCount = result.matches().size();
      if (lastCount > 0) {
        return lastCount;
      }
      if (Duration.between(start, Instant.now()).compareTo(timeout) >= 0) {
        return lastCount;
      }
      sleep(context.policy().defaultPollInterval());
    } while (true);
  }

  private void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
