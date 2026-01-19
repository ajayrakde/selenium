package org.openqa.selenium.locator.sync;

public class SyncTimeoutException extends RuntimeException {
  private final WaitResult result;

  public SyncTimeoutException(String message, WaitResult result) {
    super(message);
    this.result = result;
  }

  public WaitResult result() {
    return result;
  }
}
