package org.openqa.selenium.locator.sync.selenium.impl;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.model.BidiCapability;
import org.openqa.selenium.locator.bidi.model.BidiCapabilityStatus;
import org.openqa.selenium.locator.bidi.model.BidiFeature;
import org.openqa.selenium.locator.bidi.model.NavEventType;
import org.openqa.selenium.locator.bidi.model.NavigationEvent;
import org.openqa.selenium.locator.bidi.model.NavigationSnapshot;
import org.openqa.selenium.locator.stability.api.StabilityEvaluator;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.selenium.impl.DefaultStabilityEvaluator;
import org.openqa.selenium.locator.sync.ActionContext;
import org.openqa.selenium.locator.sync.ActionOutcome;
import org.openqa.selenium.locator.sync.ActionType;
import org.openqa.selenium.locator.sync.Intent;
import org.openqa.selenium.locator.sync.SyncKernel;
import org.openqa.selenium.locator.sync.WaitOptions;
import org.openqa.selenium.locator.sync.WaitResult;
import org.openqa.selenium.locator.sync.WaitTarget;
import org.openqa.selenium.locator.sync.WaitTargetNone;
import org.openqa.selenium.locator.sync.policy.Gate;
import org.openqa.selenium.locator.sync.policy.NavMode;
import org.openqa.selenium.locator.sync.policy.NavModeAwareSyncPolicy;
import org.openqa.selenium.locator.sync.policy.ReadinessProfile;
import org.openqa.selenium.locator.sync.policy.SyncPolicy;
import org.openqa.selenium.locator.sync.selenium.gates.ConsoleCleanGate;
import org.openqa.selenium.locator.sync.selenium.gates.DocumentReadyGate;
import org.openqa.selenium.locator.sync.selenium.gates.DomTokenStableGate;
import org.openqa.selenium.locator.sync.selenium.gates.GlobalAnimationsQuietGate;
import org.openqa.selenium.locator.sync.selenium.gates.NetworkQuietGate;
import org.openqa.selenium.locator.sync.selenium.gates.RouteEventSeenGate;
import org.openqa.selenium.locator.sync.selenium.gates.TargetStableGate;
import org.openqa.selenium.locator.sync.selenium.gates.UrlStableGate;
import org.openqa.selenium.locator.sync.selenium.js.JsDomTokenProbe;
import org.openqa.selenium.locator.sync.selenium.js.JsHistoryHook;
import org.openqa.selenium.locator.sync.selenium.js.JsHistoryHook.HistoryHookState;
import org.openqa.selenium.locator.sync.selenium.js.JsReadyStateProbe;

public final class DefaultSyncKernel implements SyncKernel {
  private final WebDriver driver;
  private final JavascriptExecutor js;
  private final SyncPolicy policy;
  private final BidiSignals bidi;
  private final StabilityService stability;
  private final StabilityEvaluator stabilityEvaluator;
  private final JsReadyStateProbe readyStateProbe;
  private final JsHistoryHook historyHook;
  private final JsDomTokenProbe domTokenProbe;

  private final Map<ActionContext, ActionBaseline> baselines = new ConcurrentHashMap<>();

  private final DocumentReadyGate docReadyGate;
  private final UrlStableGate urlStableGate;
  private final NetworkQuietGate networkQuietGate;
  private final ConsoleCleanGate consoleCleanGate;
  private final GlobalAnimationsQuietGate globalAnimationsQuietGate;
  private final TargetStableGate targetStableGate;
  private final RouteEventSeenGate routeEventSeenGate;
  private final DomTokenStableGate domTokenStableGate;

  public DefaultSyncKernel(
      WebDriver driver,
      SyncPolicy policy,
      BidiSignals bidi,
      StabilityService stability) {
    if (!(driver instanceof JavascriptExecutor executor)) {
      throw new IllegalArgumentException("Driver must implement JavascriptExecutor");
    }
    this.driver = driver;
    this.js = executor;
    this.policy = policy;
    this.bidi = bidi;
    this.stability = stability;
    this.stabilityEvaluator = new DefaultStabilityEvaluator();
    this.readyStateProbe = new JsReadyStateProbe(js);
    this.historyHook = new JsHistoryHook(js);
    this.domTokenProbe = new JsDomTokenProbe(js);

    this.docReadyGate = new DocumentReadyGate(readyStateProbe);
    this.urlStableGate = new UrlStableGate(driver);
    this.networkQuietGate = new NetworkQuietGate(bidi);
    this.consoleCleanGate = new ConsoleCleanGate(bidi);
    this.globalAnimationsQuietGate = new GlobalAnimationsQuietGate(stability);
    this.targetStableGate = new TargetStableGate(stability, stabilityEvaluator);
    this.routeEventSeenGate = new RouteEventSeenGate(bidi, historyHook);
    this.domTokenStableGate = new DomTokenStableGate(domTokenProbe);
  }

  @Override
  public WaitResult waitReady(Intent intent, WaitTarget target, WaitOptions options) {
    ReadinessProfile profile = resolveProfile(intent, NavMode.HARD);
    ActionBaseline baseline = ActionBaseline.forNow(historyHook);
    return waitReadyInternal(intent, target, options, profile, baseline, false);
  }

  @Override
  public void onBeforeAction(ActionContext ctx) {
    historyHook.installIfNeeded();
    ActionBaseline baseline = new ActionBaseline();
    baseline.beforeEpochMs = System.currentTimeMillis();
    baseline.beforeUrl = safeRead(driver::getCurrentUrl);
    baseline.beforeHistoryState = historyHook.readState();
    baselines.put(ctx, baseline);
  }

  @Override
  public void onAfterAction(ActionContext ctx, ActionOutcome outcome) {
    if (outcome != ActionOutcome.SUCCESS) {
      return;
    }

    ActionBaseline baseline = baselines.remove(ctx);
    if (baseline == null) {
      baseline = ActionBaseline.forNow(historyHook);
    }

    ActionType actionType = ctx.actionType();
    Intent intent = mapIntent(actionType);
    WaitTarget target = mapTarget(actionType, ctx);

    NavDecision navDecision = detectNavigation(actionType, baseline);
    if (navDecision.mode != null) {
      intent = Intent.NAVIGATE;
      target = navDecision.mode == NavMode.SPA ? mapSpaTarget(ctx) : new WaitTargetNone();
      baseline.beforeHistoryState = navDecision.historyState;
    }

    ReadinessProfile profile = resolveProfile(intent, navDecision.mode == null ? NavMode.HARD : navDecision.mode);
    boolean bidiSupported = isBidiSupported();
    if (ctx.jsFallbackUsed()) {
      profile = augmentForJsFallback(profile, bidiSupported);
    }

    WaitResult result =
        waitReadyInternal(intent, target, ctx.waitOptions(), profile, baseline, ctx.jsFallbackUsed());
    if (!result.success()) {
      throw new org.openqa.selenium.locator.sync.SyncTimeoutException(
          "Timed out waiting for readiness after action", result);
    }
  }

  private WaitResult waitReadyInternal(
      Intent intent,
      WaitTarget target,
      WaitOptions options,
      ReadinessProfile profile,
      ActionBaseline baseline,
      boolean jsFallbackUsed) {
    ReadinessProfile effectiveProfile = applyWaitOptions(profile, options);
    Duration timeout =
        options.timeoutOverride() != null ? options.timeoutOverride() : policy.defaultTimeout(intent);
    Duration poll = policy.pollInterval();

    long start = System.currentTimeMillis();
    long deadline = start + timeout.toMillis();

    Map<Gate, GateRuntimeState> gateState = new EnumMap<>(Gate.class);
    SyncDiagnosticsBuilder diag =
        options.includeDiagnostics()
            ? new SyncDiagnosticsBuilder(intent, bidiCapability())
            : null;

    if (diag != null && jsFallbackUsed) {
      diag.addNote("js_fallback_strict_profile");
    }

    Map<Gate, GateEvaluator> activeGates = resolveActiveGates(effectiveProfile, baseline, diag);

    while (System.currentTimeMillis() <= deadline) {
      long now = System.currentTimeMillis();
      boolean allSatisfied = true;

      for (var entry : activeGates.entrySet()) {
        Gate gate = entry.getKey();
        GateEvaluator evaluator = entry.getValue();
        GateRuntimeState state = gateState.computeIfAbsent(gate, k -> new GateRuntimeState());

        GateCheckResult result = evaluator.check(intent, target, effectiveProfile, now);
        state.update(result.isTrueNow(), now);

        boolean quietOk = quietSatisfied(gate, effectiveProfile, state, now);
        if (diag != null) {
          diag.recordGate(gate, result, state, quietOk);
          recordDiagnostics(diag, evaluator);
        }

        if (!quietOk) {
          allSatisfied = false;
        }
      }

      if (diag != null) {
        diag.recordBasics(driver, js);
      }

      if (allSatisfied) {
        long elapsed = System.currentTimeMillis() - start;
        return new WaitResult(true, elapsed, diag != null ? diag.buildSuccess(elapsed) : null);
      }

      sleep(poll);
    }

    long elapsed = System.currentTimeMillis() - start;
    return new WaitResult(false, elapsed, diag != null ? diag.buildTimeout(elapsed) : null);
  }

  private Map<Gate, GateEvaluator> resolveActiveGates(
      ReadinessProfile profile,
      ActionBaseline baseline,
      SyncDiagnosticsBuilder diag) {
    Map<Gate, GateEvaluator> active = new EnumMap<>(Gate.class);
    boolean bidiSupported = isBidiSupported();
    boolean networkSupported = bidiSupported && bidi.capability().features().contains(BidiFeature.NETWORK);
    boolean consoleSupported = bidiSupported && bidi.capability().features().contains(BidiFeature.CONSOLE);
    boolean navigationSupported = bidiSupported && bidi.capability().features().contains(BidiFeature.NAVIGATION);

    boolean routeSignalsAvailable =
        historyHook.isAvailable() && historyHook.readState() != null || navigationSupported;

    for (Gate gate : profile.requiredGates()) {
      if (gate == Gate.NETWORK_QUIET && !networkSupported) {
        if (diag != null) {
          diag.markGateSkipped(gate, "bidi_network_unsupported");
        }
        continue;
      }
      if (gate == Gate.CONSOLE_CLEAN && !consoleSupported) {
        if (diag != null) {
          diag.markGateSkipped(gate, "bidi_console_unsupported");
        }
        continue;
      }
      if (gate == Gate.ROUTE_EVENT_SEEN && !routeSignalsAvailable) {
        if (diag != null) {
          diag.markGateSkipped(gate, "route_signals_unavailable");
        }
        active.put(Gate.DOM_TOKEN_STABLE, domTokenStableGate);
        continue;
      }
      if (gate == Gate.ROUTE_EVENT_SEEN) {
        routeEventSeenGate.setBaseline(baseline.beforeEpochMs, baseline.beforeHistoryState);
      }

      active.put(gate, evaluatorFor(gate));
    }

    return active;
  }

  private GateEvaluator evaluatorFor(Gate gate) {
    return switch (gate) {
      case DOCUMENT_READY -> docReadyGate;
      case URL_STABLE -> urlStableGate;
      case NETWORK_QUIET -> networkQuietGate;
      case CONSOLE_CLEAN -> consoleCleanGate;
      case GLOBAL_ANIMATIONS_QUIET -> globalAnimationsQuietGate;
      case TARGET_STABLE -> targetStableGate;
      case ROUTE_EVENT_SEEN -> routeEventSeenGate;
      case DOM_TOKEN_STABLE -> domTokenStableGate;
    };
  }

  private boolean quietSatisfied(
      Gate gate, ReadinessProfile profile, GateRuntimeState state, long now) {
    Duration quiet =
        switch (gate) {
          case NETWORK_QUIET, CONSOLE_CLEAN, TARGET_STABLE, ROUTE_EVENT_SEEN, DOCUMENT_READY ->
              Duration.ZERO;
          case URL_STABLE -> profile.quietWindows().urlQuiet();
          case GLOBAL_ANIMATIONS_QUIET -> profile.quietWindows().animationsQuiet();
          case DOM_TOKEN_STABLE -> profile.quietWindows().urlQuiet();
        };

    long quietMs = quiet.toMillis();
    if (quietMs <= 0) {
      return state.isTrueNow();
    }
    return state.isTrueNow() && (now - state.trueSinceEpochMs()) >= quietMs;
  }

  private void recordDiagnostics(SyncDiagnosticsBuilder diag, GateEvaluator evaluator) {
    if (evaluator instanceof NetworkSnapshotProvider provider) {
      diag.recordNetworkSnapshot(provider.lastNetworkSnapshot());
    }
    if (evaluator instanceof ConsoleSnapshotProvider provider) {
      diag.recordConsoleSnapshot(provider.lastConsoleSnapshot());
    }
    if (evaluator instanceof NavigationSnapshotProvider provider) {
      diag.recordNavigationSnapshot(provider.lastNavigationSnapshot());
    }
    if (evaluator instanceof StabilityHistoryProvider provider) {
      diag.recordStabilitySamples(provider.history().samples());
    }
    if (evaluator instanceof ReadyStateProvider provider) {
      diag.recordReadyState(provider.lastReadyState());
    }
  }

  private ReadinessProfile resolveProfile(Intent intent, NavMode navMode) {
    if (policy instanceof NavModeAwareSyncPolicy navPolicy) {
      return navPolicy.profileFor(intent, navMode);
    }
    return policy.profileFor(intent);
  }

  private ReadinessProfile augmentForJsFallback(ReadinessProfile base, boolean bidiSupported) {
    Set<Gate> gates = new HashSet<>(base.requiredGates());
    gates.add(Gate.TARGET_STABLE);
    gates.add(Gate.GLOBAL_ANIMATIONS_QUIET);
    if (bidiSupported) {
      gates.add(Gate.URL_STABLE);
    }

    Duration urlQuiet =
        bidiSupported ? Duration.ofMillis(200) : base.quietWindows().urlQuiet();

    return new ReadinessProfile(
        Set.copyOf(gates),
        new org.openqa.selenium.locator.sync.policy.QuietWindows(
            urlQuiet,
            base.quietWindows().networkQuiet(),
            base.quietWindows().consoleQuiet(),
            base.quietWindows().animationsQuiet(),
            base.quietWindows().stabilityQuiet()),
        base.consolePolicy(),
        base.networkPolicy(),
        base.stabilityPolicy());
  }

  private ReadinessProfile applyWaitOptions(ReadinessProfile base, WaitOptions options) {
    if (!options.strictConsoleErrors()) {
      return base;
    }
    return new ReadinessProfile(
        base.requiredGates(),
        base.quietWindows(),
        new org.openqa.selenium.locator.sync.policy.ConsolePolicy(
            base.consolePolicy().requiredWhenSupported(),
            true,
            base.consolePolicy().readinessDenylist(),
            base.consolePolicy().readinessAllowlist()),
        base.networkPolicy(),
        base.stabilityPolicy());
  }

  private NavDecision detectNavigation(ActionType actionType, ActionBaseline baseline) {
    if (actionType == ActionType.NAVIGATE_TO
        || actionType == ActionType.BACK
        || actionType == ActionType.FORWARD
        || actionType == ActionType.REFRESH) {
      return new NavDecision(NavMode.HARD, baseline.beforeHistoryState);
    }

    String url1 = safeRead(driver::getCurrentUrl);
    String readyState = readyStateProbe.readReadyState();
    NavigationSnapshot navSnapshot =
        isNavigationSupported() ? bidi.navigation().snapshot() : null;
    HistoryHookState historyState = historyHook.readState();

    boolean urlChanged =
        baseline.beforeUrl != null && url1 != null && !url1.equals(baseline.beforeUrl);
    boolean navStart = hasNavEvent(navSnapshot, NavEventType.NAV_START, baseline.beforeEpochMs);
    boolean navCommit = hasNavEvent(navSnapshot, NavEventType.NAV_COMMIT, baseline.beforeEpochMs);
    boolean urlChangedEvent =
        hasNavEvent(navSnapshot, NavEventType.URL_CHANGED, baseline.beforeEpochMs);

    boolean hardSuspected =
        (urlChanged && "loading".equals(readyState)) || navStart || navCommit;
    boolean spaSuspected =
        (historyState != null && baseline.beforeHistoryState != null
            && historyState.seq() > baseline.beforeHistoryState.seq())
            || (urlChangedEvent && !navStart)
            || (urlChanged && !"loading".equals(readyState));

    if (hardSuspected) {
      return new NavDecision(NavMode.HARD, historyState);
    }
    if (spaSuspected) {
      return new NavDecision(NavMode.SPA, historyState);
    }
    return new NavDecision(null, historyState);
  }

  private boolean hasNavEvent(NavigationSnapshot snapshot, NavEventType type, long sinceEpochMs) {
    if (snapshot == null || snapshot.lastEvents() == null) {
      return false;
    }
    for (NavigationEvent event : snapshot.lastEvents()) {
      if (event.type() == type && event.tsEpochMs() >= sinceEpochMs) {
        return true;
      }
    }
    return false;
  }

  private Intent mapIntent(ActionType actionType) {
    return switch (actionType) {
      case CLICK, DBLCLICK, RIGHT_CLICK, HOVER, SCROLL_INTO_VIEW -> Intent.CLICK;
      case TYPE, CLEAR, SELECT, PRESS_KEY -> Intent.TYPE;
      case NAVIGATE_TO, BACK, FORWARD, REFRESH -> Intent.NAVIGATE;
      case UPLOAD_FILE, CUSTOM_JS -> Intent.ASSERT;
    };
  }

  private WaitTarget mapTarget(ActionType actionType, ActionContext ctx) {
    return switch (actionType) {
      case NAVIGATE_TO, BACK, FORWARD, REFRESH, CUSTOM_JS -> new WaitTargetNone();
      case UPLOAD_FILE ->
          ctx.containerTarget() != null ? ctx.containerTarget() : new WaitTargetNone();
      default -> ctx.target() != null ? ctx.target() : new WaitTargetNone();
    };
  }

  private WaitTarget mapSpaTarget(ActionContext ctx) {
    if (ctx.containerTarget() != null) {
      return ctx.containerTarget();
    }
    return new WaitTargetNone();
  }

  private boolean isBidiSupported() {
    return bidi != null && bidi.capability().status() == BidiCapabilityStatus.SUPPORTED;
  }

  private boolean isNavigationSupported() {
    return isBidiSupported() && bidi.capability().features().contains(BidiFeature.NAVIGATION);
  }

  private BidiCapability bidiCapability() {
    return bidi == null ? null : bidi.capability();
  }

  private void sleep(Duration d) {
    try {
      Thread.sleep(d.toMillis());
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

  private <T> T safeRead(CheckedSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (Exception ignored) {
      return null;
    }
  }

  private interface CheckedSupplier<T> {
    T get() throws Exception;
  }

  private static final class ActionBaseline {
    private long beforeEpochMs;
    private String beforeUrl;
    private HistoryHookState beforeHistoryState;

    static ActionBaseline forNow(JsHistoryHook hook) {
      ActionBaseline baseline = new ActionBaseline();
      baseline.beforeEpochMs = System.currentTimeMillis();
      if (hook != null) {
        hook.installIfNeeded();
        baseline.beforeHistoryState = hook.readState();
      }
      return baseline;
    }
  }

  private static final class NavDecision {
    private final NavMode mode;
    private final HistoryHookState historyState;

    private NavDecision(NavMode mode, HistoryHookState historyState) {
      this.mode = mode;
      this.historyState = historyState;
    }
  }
}
