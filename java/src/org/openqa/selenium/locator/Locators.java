package org.openqa.selenium.locator;

import java.time.Duration;
import java.util.List;

/**
 * Collection view over multiple locator matches.
 */
public interface Locators {
  int count(Duration timeout);

  Locator nth(int index);

  List<String> texts(Duration timeout);
}
