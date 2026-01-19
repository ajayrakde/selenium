package org.openqa.selenium.locator;

import java.time.Duration;

/**
 * Configuration policy for locator behavior and governance.
 */
public interface Policy {
  Duration defaultTimeout();

  Duration defaultPollInterval();

  String testIdAttribute();

  SelectorTierPolicy tierPolicy();
}
