package org.openqa.selenium.locator;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;

/**
 * Applies filter specs to candidate elements.
 */
final class FilterEngine {
  List<WebElement> apply(List<WebElement> candidates, List<FilterSpec> filters, Policy policy) {
    List<WebElement> filtered = new ArrayList<>(candidates);
    for (FilterSpec filter : filters) {
      filtered = applyFilter(filtered, filter, policy);
    }
    return filtered;
  }

  private List<WebElement> applyFilter(List<WebElement> candidates, FilterSpec filter, Policy policy) {
    List<WebElement> results = new ArrayList<>();
    for (WebElement candidate : candidates) {
      if (matches(candidate, filter, policy)) {
        results.add(candidate);
      }
    }
    return results;
  }

  private boolean matches(WebElement candidate, FilterSpec filter, Policy policy) {
    return switch (filter) {
      case TextEquals textEquals -> normalize(candidate.getText(), policy)
          .equals(normalize(textEquals.value(), policy));
      case TextContains textContains -> normalize(candidate.getText(), policy)
          .contains(normalize(textContains.value(), policy));
      case AttrEquals attrEquals -> {
        String value = candidate.getAttribute(attrEquals.attr());
        yield value != null && normalize(value, policy)
            .equals(normalize(attrEquals.value(), policy));
      }
    };
  }

  private String normalize(String value, Policy policy) {
    if (value == null) {
      return "";
    }
    if (policy.textNormalization() == TextNormalization.ON) {
      return value.trim().replaceAll("\\s+", " ");
    }
    return value;
  }
}
