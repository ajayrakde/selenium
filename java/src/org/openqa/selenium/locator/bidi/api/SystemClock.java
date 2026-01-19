package org.openqa.selenium.locator.bidi.api;

public class SystemClock implements Clock {
  @Override
  public long nowEpochMs() {
    return System.currentTimeMillis();
  }
}
