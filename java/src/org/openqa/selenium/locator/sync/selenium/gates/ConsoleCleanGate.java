package org.openqa.selenium.locator.sync.selenium.gates;

import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.ConsoleSnapshot;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.ConsoleSnapshotProvider;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.impl.SyncConsoleRelevance;

public class ConsoleCleanGate implements GateEvaluator, ConsoleSnapshotProvider {
  private final BidiSignals bidi;
  private ConsoleSnapshot lastSnapshot;

  public ConsoleCleanGate(BidiSignals bidi) {
    this.bidi = bidi;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    if (bidi == null
        || bidi.capability().status() != BidiCapabilityStatus.SUPPORTED
        || !bidi.capability().features().contains(BidiFeature.CONSOLE)) {
      return new GateCheckResult(true, "bidi_unsupported_skipped");
    }
    lastSnapshot = bidi.console().snapshot();
    long quietMs = profile.quietWindows().consoleQuiet().toMillis();
    long lastRelevantTs = SyncConsoleRelevance.lastRelevantErrorTimestamp(lastSnapshot, profile.consolePolicy());
    boolean ok = lastRelevantTs < 0 || now - lastRelevantTs >= quietMs;
    return new GateCheckResult(ok, ok ? "console_clean" : "console_relevant_error");
  }

  @Override
  public ConsoleSnapshot lastConsoleSnapshot() {
    return lastSnapshot;
  }
}
