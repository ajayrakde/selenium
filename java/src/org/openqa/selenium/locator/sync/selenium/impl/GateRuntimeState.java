package org.openqa.selenium.locator.sync.selenium.impl;

final class GateRuntimeState {
  private boolean trueNow;
  private long trueSince = -1;
  private long lastChange = -1;

  void update(boolean isTrue, long now) {
    if (trueNow != isTrue) {
      lastChange = now;
      trueNow = isTrue;
      if (isTrue) {
        trueSince = now;
      } else {
        trueSince = -1;
      }
    }
  }

  boolean isTrueNow() {
    return trueNow;
  }

  long trueSinceEpochMs() {
    return trueSince;
  }

  long lastChangedAtEpochMs() {
    return lastChange;
  }
}
