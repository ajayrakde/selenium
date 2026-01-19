package org.openqa.selenium.locator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.openqa.selenium.Keys;
import org.openqa.selenium.locator.actions.ActionTags;
import org.openqa.selenium.locator.actions.ClickOptions;
import org.openqa.selenium.locator.ResolutionDiagnostics;

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

  @Override
  public void click(Duration timeout) {
    click(new ClickOptions(false, timeout));
  }

  @Override
  public void click() {
    click(ClickOptions.defaultOptions());
  }

  @Override
  public void click(ClickOptions options) {
    context.actionPerformer().click(actionPlan(), options);
  }

  @Override
  public void click(Duration timeout, boolean allowJsFallback) {
    click(new ClickOptions(allowJsFallback, timeout));
  }

  @Override
  public void fill(String text, Duration timeout) {
    context.actionPerformer().fill(actionPlan(), text, timeout);
  }

  @Override
  public void fill(String text) {
    fill(text, null);
  }

  @Override
  public void clearAndFill(String text, Duration timeout) {
    context.actionPerformer().clearAndFill(actionPlan(), text, timeout);
  }

  @Override
  public void clearAndFill(String text) {
    clearAndFill(text, null);
  }

  @Override
  public void pressKey(Keys key, Duration timeout) {
    context.actionPerformer().pressKey(actionPlan(), key, timeout);
  }

  @Override
  public void pressKey(Keys key) {
    pressKey(key, null);
  }

  @Override
  public void hover(Duration timeout) {
    context.actionPerformer().hover(actionPlan(), timeout);
  }

  @Override
  public void hover() {
    hover(null);
  }

  @Override
  public void check(Duration timeout) {
    context.actionPerformer().check(actionPlan(), timeout);
  }

  @Override
  public void check() {
    check(null);
  }

  @Override
  public void uncheck(Duration timeout) {
    context.actionPerformer().uncheck(actionPlan(), timeout);
  }

  @Override
  public void uncheck() {
    uncheck(null);
  }

  @Override
  public void selectOption(String text, Duration timeout) {
    context.actionPerformer().selectOption(actionPlan(), text, timeout);
  }

  @Override
  public void selectOption(String text) {
    selectOption(text, null);
  }

  LocatorPlan plan() {
    return plan;
  }

  private LocatorPlan actionPlan() {
    if (plan.strictness() == Strictness.MANY && nthIndex == null) {
      throw new LocatorAmbiguousException(
          "Locator matched multiple elements. Use nth() or first() before performing actions.",
          new ResolutionDiagnostics(
              plan.name(),
              plan.name(),
              plan.strictness(),
              "",
              "",
              0,
              0,
              0,
              List.of(),
              List.of("Locator actions require strict ONE matches."),
              plan.tags()));
    }
    if (nthIndex == null) {
      return plan.withStrictness(Strictness.ONE);
    }
    HashMap<String, Object> tags = new HashMap<>(plan.tags());
    tags.put(ActionTags.NTH_INDEX, nthIndex);
    return plan.withTags(tags).withStrictness(Strictness.MANY);
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
