package org.openqa.selenium.locator.sync.policy;

import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.locator.sync.Intent;

public interface SyncPolicy {
  Duration defaultTimeout(Intent intent);

  Duration pollInterval();

  Map<Intent, ReadinessProfile> profiles();

  default ReadinessProfile profileFor(Intent intent) {
    return profiles().get(intent);
  }
}
