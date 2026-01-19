package org.openqa.selenium.locator.actions;

import java.time.Duration;

public record ClickOptions(boolean allowJsFallback, Duration timeoutOverride) {
  public static ClickOptions defaultOptions() {
    return new ClickOptions(false, null);
  }

  public static ClickOptions allowJsFallback() {
    return new ClickOptions(true, null);
  }
}
