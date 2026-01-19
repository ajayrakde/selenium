package org.openqa.selenium.locator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default multi-locator view implementation.
 */
public class DefaultLocators implements Locators {
  private final AutomationContext context;
  private final LocatorResolver resolver;
  private final LocatorPlan plan;

  DefaultLocators(AutomationContext context, LocatorResolver resolver, LocatorPlan plan) {
    this.context = Objects.requireNonNull(context, "context");
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.plan = Objects.requireNonNull(plan, "plan");
  }

  @Override
  public int count(Duration timeout) {
    Duration effectiveTimeout = timeout == null ? context.policy().defaultTimeout() : timeout;
    return pollForCount(effectiveTimeout);
  }

  @Override
  public Locator nth(int index) {
    return new DefaultLocator(context, resolver, plan.withStrictness(Strictness.MANY), index);
  }

  @Override
  public List<String> texts(Duration timeout) {
    Duration effectiveTimeout = timeout == null ? context.policy().defaultTimeout() : timeout;
    ResolutionResult result = resolver.resolve(
        context,
        plan.withStrictness(Strictness.MANY),
        new ResolveOptions(effectiveTimeout, context.policy().defaultPollInterval(), false));
    return result.matches().stream().map(element -> element.getText()).collect(Collectors.toList());
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
