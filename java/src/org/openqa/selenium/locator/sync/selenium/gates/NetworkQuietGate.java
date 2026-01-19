package org.openqa.selenium.locator.sync.selenium.gates;

import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.NetworkSnapshot;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.impl.NetworkSnapshotProvider;

public class NetworkQuietGate implements GateEvaluator, NetworkSnapshotProvider {
  private final BidiSignals bidi;
  private NetworkSnapshot lastSnapshot;

  public NetworkQuietGate(BidiSignals bidi) {
    this.bidi = bidi;
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    if (bidi == null
        || bidi.capability().status() != BidiCapabilityStatus.SUPPORTED
        || !bidi.capability().features().contains(BidiFeature.NETWORK)) {
      return new GateCheckResult(true, "bidi_unsupported_skipped");
    }
    lastSnapshot = bidi.network().snapshot();
    long quietMs = profile.quietWindows().networkQuiet().toMillis();
    boolean inflightOk = lastSnapshot.inflight() == 0;
    boolean quietOk = now - lastSnapshot.lastActivityEpochMs() >= quietMs;
    boolean ok = inflightOk && quietOk;
    return new GateCheckResult(
        ok,
        "inflight="
            + lastSnapshot.inflight()
            + " lastAct="
            + lastSnapshot.lastActivityEpochMs());
  }

  @Override
  public NetworkSnapshot lastNetworkSnapshot() {
    return lastSnapshot;
  }
}
