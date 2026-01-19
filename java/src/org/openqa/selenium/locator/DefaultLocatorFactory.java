package org.openqa.selenium.locator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default factory for creating locator plans.
 */
public class DefaultLocatorFactory implements LocatorFactory {
  private final AutomationContext context;
  private final LocatorResolver resolver;

  public DefaultLocatorFactory(AutomationContext context, LocatorResolver resolver) {
    this.context = Objects.requireNonNull(context, "context");
    this.resolver = Objects.requireNonNull(resolver, "resolver");
  }

  @Override
  public Locator byTestId(String testId) {
    LocatorPlan plan = new LocatorPlan(
        "testId=" + testId,
        new RootScope(),
        List.of(new TestIdStep(testId)),
        List.of(),
        Strictness.ONE,
        SelectorTier.TIER0_TESTID,
        Map.of());
    return new DefaultLocator(context, resolver, plan, null);
  }

  @Override
  public Locator byCss(String css) {
    LocatorPlan plan = new LocatorPlan(
        "css=" + css,
        new RootScope(),
        List.of(new CssStep(css)),
        List.of(),
        Strictness.ONE,
        SelectorTier.TIER3_CSS,
        Map.of());
    return new DefaultLocator(context, resolver, plan, null);
  }

  @Override
  public Locator byXpath(String xpath) {
    if (context.policy().tierPolicy() == TierPolicy.STRICT) {
      ResolutionDiagnostics diagnostics = new ResolutionDiagnostics(
          "xpath",
          "xpath=" + xpath,
          Strictness.ONE,
          "",
          "",
          0,
          0,
          0,
          List.of(),
          List.of("XPath selectors are not allowed under STRICT policy."),
          Map.of());
      throw new LocatorPolicyViolationException("XPath selectors are blocked by policy.", diagnostics);
    }
    LocatorPlan plan = new LocatorPlan(
        "xpath=" + xpath,
        new RootScope(),
        List.of(new XpathStep(xpath)),
        List.of(),
        Strictness.ONE,
        SelectorTier.TIER4_XPATH,
        Map.of());
    return new DefaultLocator(context, resolver, plan, null);
  }

  @Override
  public Locator root() {
    LocatorPlan plan = new LocatorPlan(
        "root",
        new RootScope(),
        List.of(new CssStep("html")),
        List.of(),
        Strictness.ONE,
        SelectorTier.TIER3_CSS,
        Map.of());
    return new DefaultLocator(context, resolver, plan, null);
  }
}
