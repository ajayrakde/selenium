package org.openqa.selenium.locator.sync;

import java.time.Duration;

public record WaitOptions(
    Duration timeoutOverride,
    boolean includeDiagnostics,
    boolean strictConsoleErrors) {
  public static WaitOptions defaults() {
    return new WaitOptions(null, true, false);
  }
}
