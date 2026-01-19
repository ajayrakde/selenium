package org.openqa.selenium.locator;

import java.time.Duration;
import java.util.Objects;

/**
 * Options for resolving a locator plan.
 */
public final class ResolveOptions {
  private final Duration timeout;
  private final Duration pollInterval;
  private final int maxCandidatesToDiagnose;
  private final boolean allowShadowDom;
  private final boolean includeCandidatePreview;

  public ResolveOptions(
      Duration timeout,
      Duration pollInterval,
      int maxCandidatesToDiagnose,
      boolean allowShadowDom,
      boolean includeCandidatePreview) {
    this.timeout = Objects.requireNonNull(timeout, "timeout");
    this.pollInterval = Objects.requireNonNull(pollInterval, "pollInterval");
    this.maxCandidatesToDiagnose = maxCandidatesToDiagnose;
    this.allowShadowDom = allowShadowDom;
    this.includeCandidatePreview = includeCandidatePreview;
  }

  public Duration timeout() {
    return timeout;
  }

  public Duration pollInterval() {
    return pollInterval;
  }

  public int maxCandidatesToDiagnose() {
    return maxCandidatesToDiagnose;
  }

  public boolean allowShadowDom() {
    return allowShadowDom;
  }

  public boolean includeCandidatePreview() {
    return includeCandidatePreview;
  }
}
