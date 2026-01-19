package org.openqa.selenium.locator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Structured diagnostics for locator resolution.
 */
public record ResolutionDiagnostics(
    String locatorName,
    String planDescription,
    Strictness strictness,
    String url,
    String title,
    long elapsedMillis,
    int polls,
    int matchCount,
    List<CandidatePreview> candidates,
    List<String> suggestions,
    Map<String, Object> metrics) {
  public ResolutionDiagnostics {
    Objects.requireNonNull(locatorName, "locatorName");
    Objects.requireNonNull(planDescription, "planDescription");
    Objects.requireNonNull(strictness, "strictness");
    Objects.requireNonNull(url, "url");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(candidates, "candidates");
    Objects.requireNonNull(suggestions, "suggestions");
    Objects.requireNonNull(metrics, "metrics");
  }
}

public record CandidatePreview(
    String tag,
    String id,
    String classes,
    String textSnippet,
    String attributesSnippet) {}
