package org.openqa.selenium.locator.sync.selenium.gates;

import java.util.List;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.NavEventType;
import org.openqa.selenium.locator.bidi.model.NavigationEvent;
import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.selenium.impl.GateCheckResult;
import org.openqa.selenium.locator.sync.selenium.impl.GateEvaluator;
import org.openqa.selenium.locator.sync.selenium.impl.NavigationSnapshotProvider;
import org.openqa.selenium.locator.sync.selenium.js.JsHistoryHook;
import org.openqa.selenium.locator.sync.selenium.js.JsHistoryHook.HistoryHookState;

public class RouteEventSeenGate implements GateEvaluator, NavigationSnapshotProvider {
  private final BidiSignals bidi;
  private final JsHistoryHook historyHook;
  private NavigationSnapshot lastSnapshot;
  private Long baselineEpochMs;
  private Long baselineHistorySeq;

  public RouteEventSeenGate(BidiSignals bidi, JsHistoryHook historyHook) {
    this.bidi = bidi;
    this.historyHook = historyHook;
  }

  public void setBaseline(long epochMs, HistoryHookState historyState) {
    this.baselineEpochMs = epochMs;
    this.baselineHistorySeq = historyState == null ? null : historyState.seq();
  }

  @Override
  public GateCheckResult check(
      Intent intent, WaitTarget target, ReadinessProfile profile, long now) {
    boolean historySeen = false;
    if (historyHook != null && historyHook.isAvailable()) {
      HistoryHookState current = historyHook.readState();
      if (current != null && baselineHistorySeq != null) {
        historySeen = current.seq() > baselineHistorySeq;
      }
    }

    boolean navSeen = false;
    if (bidi != null
        && bidi.capability().status() == BidiCapabilityStatus.SUPPORTED
        && bidi.capability().features().contains(BidiFeature.NAVIGATION)) {
      lastSnapshot = bidi.navigation().snapshot();
      navSeen = hasUrlChangedSinceBaseline(lastSnapshot, baselineEpochMs);
    }

    boolean ok = historySeen || navSeen;
    return new GateCheckResult(ok, ok ? "route_event_seen" : "route_event_not_seen");
  }

  @Override
  public NavigationSnapshot lastNavigationSnapshot() {
    return lastSnapshot;
  }

  private boolean hasUrlChangedSinceBaseline(NavigationSnapshot snapshot, Long baselineTs) {
    if (snapshot == null || baselineTs == null) {
      return false;
    }
    List<NavigationEvent> events = snapshot.lastEvents();
    if (events == null) {
      return false;
    }
    return events.stream()
        .anyMatch(
            event ->
                event.tsEpochMs() >= baselineTs
                    && event.type() == NavEventType.URL_CHANGED);
  }
}
