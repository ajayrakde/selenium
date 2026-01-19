package org.openqa.selenium.locator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.WebElement;

/**
 * Builds candidate previews for diagnostics.
 */
final class CandidateSummarizer {
  List<CandidatePreview> summarize(List<WebElement> candidates, int maxCandidates, Policy policy) {
    List<CandidatePreview> previews = new ArrayList<>();
    int limit = Math.min(maxCandidates, candidates.size());
    for (int i = 0; i < limit; i++) {
      previews.add(preview(candidates.get(i), policy));
    }
    return previews;
  }

  private CandidatePreview preview(WebElement element, Policy policy) {
    String text = trim(element.getText(), 80);
    String classes = trim(element.getAttribute("class"), 120);
    String attributes = attributeSnippet(element, policy);
    return new CandidatePreview(
        element.getTagName(),
        element.getAttribute("id"),
        classes,
        text,
        attributes);
  }

  private String trim(String value, int max) {
    if (value == null) {
      return "";
    }
    if (value.length() <= max) {
      return value;
    }
    return value.substring(0, max) + "…";
  }

  private String attributeSnippet(WebElement element, Policy policy) {
    List<String> parts = new ArrayList<>();
    addIfPresent(parts, policy.testIdAttribute(), element.getAttribute(policy.testIdAttribute()));
    addIfPresent(parts, "role", element.getAttribute("role"));
    addIfPresent(parts, "name", element.getAttribute("name"));
    addIfPresent(parts, "type", element.getAttribute("type"));
    addIfPresent(parts, "aria-label", element.getAttribute("aria-label"));
    return parts.stream().collect(Collectors.joining(" "));
  }

  private void addIfPresent(List<String> parts, String name, String value) {
    if (value != null && !value.isBlank()) {
      parts.add(name + "=\"" + value + "\"");
    }
  }
}
