package org.openqa.selenium.locator;

import java.time.Duration;

/**
 * A live, immutable locator that represents intent.
 */
public interface Locator {
  String name();

  Locator withName(String name);

  Locator within(Locator container);

  Locator filter(FilterSpec filter);

  Locator nth(int index);

  Locator first();

  Locators all();

  boolean exists(Duration timeout);

  int count(Duration timeout);

  String text(Duration timeout);
}
