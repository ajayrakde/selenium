package org.openqa.selenium.locator;

import java.time.Duration;
import java.util.Objects;

/**
 * Options for resolving a locator plan.
 */
public record ResolveOptions(
    Duration timeout,
    Duration pollInterval,
    boolean includeCandidatePreviews) {
  public ResolveOptions {
    Objects.requireNonNull(timeout, "timeout");
    Objects.requireNonNull(pollInterval, "pollInterval");
  }
}
