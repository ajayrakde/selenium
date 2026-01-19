package org.openqa.selenium.locator;

import java.util.List;
import org.openqa.selenium.By;

/**
 * Compiles selector steps into Selenium By expressions.
 */
final class SelectorCompiler {
  By compile(LocatorPlan plan, Policy policy) {
    List<SelectorStep> steps = plan.steps();
    if (steps.isEmpty()) {
      throw new LocatorInvalidSelectorException("No selector steps provided.", emptyDiagnostics(plan));
    }

    if (steps.size() > 1) {
      boolean allCss = steps.stream().allMatch(step -> step instanceof CssStep || step instanceof TestIdStep);
      if (!allCss) {
        throw new LocatorInvalidSelectorException("Mixed selector steps are not supported in v1.", emptyDiagnostics(plan));
      }
      String combined = steps.stream()
          .map(step -> step instanceof TestIdStep testId
              ? "[" + policy.testIdAttribute() + "=\"" + CssEscaper.escapeAttributeValue(testId.testId()) + "\"]"
              : ((CssStep) step).css())
          .reduce("", (left, right) -> left + right);
      return By.cssSelector(combined);
    }

    SelectorStep step = steps.get(0);
    return switch (step) {
      case TestIdStep testId -> By.cssSelector(
          "[" + policy.testIdAttribute() + "=\"" + CssEscaper.escapeAttributeValue(testId.testId()) + "\"]");
      case CssStep css -> By.cssSelector(css.css());
      case XpathStep xpath -> By.xpath(xpath.xpath());
    };
  }

  private ResolutionDiagnostics emptyDiagnostics(LocatorPlan plan) {
    return new ResolutionDiagnostics(
        plan.name(),
        PlanDescriber.describe(plan),
        plan.strictness(),
        "",
        "",
        0,
        0,
        0,
        List.of(),
        List.of(),
        java.util.Map.of("outcome", "invalid-selector"));
  }
}
