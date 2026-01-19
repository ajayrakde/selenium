package org.openqa.selenium.locator;

import java.time.Duration;

/**
 * Configuration policy for locator behavior and governance.
 */
public interface Policy {
  String testIdAttribute();

  Duration defaultTimeout();

  Duration defaultPollInterval();

  TierPolicy tierPolicy();

  int maxCandidatesInDiagnostics();

  TextNormalization textNormalization();
}
