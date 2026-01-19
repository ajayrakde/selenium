package org.openqa.selenium.locator;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Structured diagnostics for locator resolution.
 */
public final class ResolutionDiagnostics {
  public static final class CandidatePreview {
    private final String tagName;
    private final String id;
    private final String className;
    private final String textSnippet;
    private final String fingerprint;

    public CandidatePreview(
        String tagName,
        String id,
        String className,
        String textSnippet,
        String fingerprint) {
      this.tagName = tagName;
      this.id = id;
      this.className = className;
      this.textSnippet = textSnippet;
      this.fingerprint = fingerprint;
    }

    public String tagName() {
      return tagName;
    }

    public String id() {
      return id;
    }

    public String className() {
      return className;
    }

    public String textSnippet() {
      return textSnippet;
    }

    public String fingerprint() {
      return fingerprint;
    }
  }

  private final String locatorName;
  private final String planDescription;
  private final Strictness strictness;
  private final URI url;
  private final String title;
  private final Duration elapsed;
  private final int polls;
  private final int matchCount;
  private final List<CandidatePreview> candidates;
  private final List<String> suggestions;

  public ResolutionDiagnostics(
      String locatorName,
      String planDescription,
      Strictness strictness,
      URI url,
      String title,
      Duration elapsed,
      int polls,
      int matchCount,
      List<CandidatePreview> candidates,
      List<String> suggestions) {
    this.locatorName = Objects.requireNonNull(locatorName, "locatorName");
    this.planDescription = Objects.requireNonNull(planDescription, "planDescription");
    this.strictness = Objects.requireNonNull(strictness, "strictness");
    this.url = Objects.requireNonNull(url, "url");
    this.title = Objects.requireNonNull(title, "title");
    this.elapsed = Objects.requireNonNull(elapsed, "elapsed");
    this.polls = polls;
    this.matchCount = matchCount;
    this.candidates = Collections.unmodifiableList(List.copyOf(Objects.requireNonNull(candidates, "candidates")));
    this.suggestions = Collections.unmodifiableList(List.copyOf(Objects.requireNonNull(suggestions, "suggestions")));
  }

  public String locatorName() {
    return locatorName;
  }

  public String planDescription() {
    return planDescription;
  }

  public Strictness strictness() {
    return strictness;
  }

  public URI url() {
    return url;
  }

  public String title() {
    return title;
  }

  public Duration elapsed() {
    return elapsed;
  }

  public int polls() {
    return polls;
  }

  public int matchCount() {
    return matchCount;
  }

  public List<CandidatePreview> candidates() {
    return candidates;
  }

  public List<String> suggestions() {
    return suggestions;
  }
}
