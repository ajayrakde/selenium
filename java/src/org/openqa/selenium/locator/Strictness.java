package org.openqa.selenium.locator;

/**
 * Defines how strict a locator resolution should be.
 */
public enum Strictness {
  /**
   * Expect exactly one match.
   */
  ONE,

  /**
   * Allow any number of matches.
   */
  MANY
}
