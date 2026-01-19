package org.openqa.selenium.locator;

import java.time.Duration;

/**
 * Default locator policy configuration.
 */
public class DefaultPolicy implements Policy {
  private final String testIdAttribute;
  private final Duration defaultTimeout;
  private final Duration defaultPollInterval;
  private final TierPolicy tierPolicy;
  private final int maxCandidatesInDiagnostics;
  private final TextNormalization textNormalization;

  public DefaultPolicy() {
    this("data-testid", Duration.ofSeconds(10), Duration.ofMillis(100), TierPolicy.STANDARD, 5, TextNormalization.ON);
  }

  public DefaultPolicy(
      String testIdAttribute,
      Duration defaultTimeout,
      Duration defaultPollInterval,
      TierPolicy tierPolicy,
      int maxCandidatesInDiagnostics,
      TextNormalization textNormalization) {
    this.testIdAttribute = testIdAttribute;
    this.defaultTimeout = defaultTimeout;
    this.defaultPollInterval = defaultPollInterval;
    this.tierPolicy = tierPolicy;
    this.maxCandidatesInDiagnostics = maxCandidatesInDiagnostics;
    this.textNormalization = textNormalization;
  }

  @Override
  public String testIdAttribute() {
    return testIdAttribute;
  }

  @Override
  public Duration defaultTimeout() {
    return defaultTimeout;
  }

  @Override
  public Duration defaultPollInterval() {
    return defaultPollInterval;
  }

  @Override
  public TierPolicy tierPolicy() {
    return tierPolicy;
  }

  @Override
  public int maxCandidatesInDiagnostics() {
    return maxCandidatesInDiagnostics;
  }

  @Override
  public TextNormalization textNormalization() {
    return textNormalization;
  }
}
