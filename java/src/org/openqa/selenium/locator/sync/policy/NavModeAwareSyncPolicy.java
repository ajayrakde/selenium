package org.openqa.selenium.locator.sync.policy;

import org.openqa.selenium.locator.sync.Intent;

public interface NavModeAwareSyncPolicy extends SyncPolicy {
  ReadinessProfile profileFor(Intent intent, NavMode navMode);
}
