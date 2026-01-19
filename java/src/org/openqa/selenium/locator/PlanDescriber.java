package org.openqa.selenium.locator;

import java.util.stream.Collectors;

/**
 * Builds human-readable plan descriptions for diagnostics.
 */
final class PlanDescriber {
  private PlanDescriber() {}

  static String describe(LocatorPlan plan) {
    String scope = switch (plan.scope()) {
      case RootScope ignored -> "root";
      case WithinScope within -> "within(" + within.container().name() + ")";
    };

    String steps = plan.steps().stream()
        .map(step -> switch (step) {
          case TestIdStep testId -> "testId=" + testId.testId();
          case CssStep css -> "css=" + css.css();
          case XpathStep xpath -> "xpath=" + xpath.xpath();
        })
        .collect(Collectors.joining(" -> "));

    String filters = plan.filters().stream()
        .map(filter -> switch (filter) {
          case TextEquals textEquals -> "text==" + textEquals.value();
          case TextContains textContains -> "text~=" + textContains.value();
          case AttrEquals attrEquals -> attrEquals.attr() + "=" + attrEquals.value();
        })
        .collect(Collectors.joining(", "));

    String filterLabel = filters.isEmpty() ? "" : " filters[" + filters + "]";
    return scope + " | " + steps + filterLabel;
  }
}
