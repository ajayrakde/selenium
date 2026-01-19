package org.openqa.selenium.locator.sync.selenium.impl;

import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.locator.bidi.model.ConsoleEvent;
import org.openqa.selenium.locator.bidi.model.ConsoleLevel;
import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.sync.policy.ConsolePolicy;

final class SyncConsoleRelevance {
  private SyncConsoleRelevance() {}

  static long lastRelevantErrorTimestamp(ConsoleSnapshot snapshot, ConsolePolicy policy) {
    long last = -1;
    if (snapshot == null || snapshot.lastEvents() == null) {
      return last;
    }
    for (ConsoleEvent event : snapshot.lastEvents()) {
      if (isRelevant(event, policy)) {
        last = Math.max(last, event.tsEpochMs());
      }
    }
    return last;
  }

  static boolean isRelevant(ConsoleEvent event, ConsolePolicy policy) {
    if (event == null) {
      return false;
    }
    ConsoleLevel level = event.level();
    if (level == null) {
      return false;
    }
    if (level != ConsoleLevel.ERROR
        && !(policy.treatWarnAsFailure() && level == ConsoleLevel.WARN)) {
      return false;
    }

    String text = event.text() == null ? "" : event.text();
    List<Pattern> allowlist = policy.readinessAllowlist();
    List<Pattern> denylist = policy.readinessDenylist();

    if (allowlist != null && !allowlist.isEmpty()) {
      return allowlist.stream().anyMatch(pattern -> pattern.matcher(text).find());
    }

    if (denylist != null && !denylist.isEmpty()) {
      return denylist.stream().anyMatch(pattern -> pattern.matcher(text).find());
    }

    return true;
  }
}
