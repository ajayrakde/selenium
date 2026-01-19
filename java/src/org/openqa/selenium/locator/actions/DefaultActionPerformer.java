package org.openqa.selenium.locator.actions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.locator.AutomationContext;
import org.openqa.selenium.locator.LocatorAmbiguousException;
import org.openqa.selenium.locator.LocatorNotFoundException;
import org.openqa.selenium.locator.LocatorPlan;
import org.openqa.selenium.locator.LocatorResolver;
import org.openqa.selenium.locator.ResolveOptions;
import org.openqa.selenium.locator.ResolutionResult;
import org.openqa.selenium.locator.Strictness;
import org.openqa.selenium.locator.actions.click.ClickPointStrategy;
import org.openqa.selenium.locator.actions.click.DefaultJsClickExecutor;
import org.openqa.selenium.locator.actions.click.JsClickExecutor;
import org.openqa.selenium.locator.actions.diagnostics.ActionDiagnostics;
import org.openqa.selenium.locator.actions.exceptions.ActionFailedException;
import org.openqa.selenium.locator.actions.hittest.HitTestResult;
import org.openqa.selenium.locator.actions.hittest.HitTestService;
import org.openqa.selenium.locator.actions.hittest.JsHitTestService;
import org.openqa.selenium.locator.actions.post.MinimalSettleWaiter;
import org.openqa.selenium.locator.actions.post.PostActionWaiter;
import org.openqa.selenium.locator.actions.probe.ElementRect;
import org.openqa.selenium.locator.actions.probe.ElementState;
import org.openqa.selenium.locator.actions.probe.ElementStateProbe;
import org.openqa.selenium.locator.actions.probe.JsElementStateProbe;
import org.openqa.selenium.locator.actions.retry.DefaultRetryPolicy;
import org.openqa.selenium.locator.actions.retry.RetryPolicy;
import org.openqa.selenium.locator.actions.scroll.DefaultScrollManager;
import org.openqa.selenium.locator.actions.scroll.ScrollManager;
import org.openqa.selenium.locator.actions.stability.StabilityService;
import org.openqa.selenium.locator.actions.stability.StableRectService;

public class DefaultActionPerformer implements ActionPerformer {
  private static final Duration ZERO = Duration.ZERO;

  private final AutomationContext context;
  private final LocatorResolver resolver;
  private final ElementStateProbe stateProbe;
  private final ScrollManager scrollManager;
  private final HitTestService hitTestService;
  private final StabilityService stabilityService;
  private final RetryPolicy retryPolicy;
  private final PostActionWaiter postActionWaiter;
  private final JsClickExecutor jsClickExecutor;

  public DefaultActionPerformer(AutomationContext context, LocatorResolver resolver) {
    this(
        context,
        resolver,
        new JsElementStateProbe(),
        new DefaultScrollManager(),
        new JsHitTestService(),
        new StableRectService(),
        new DefaultRetryPolicy(),
        new MinimalSettleWaiter(),
        new DefaultJsClickExecutor());
  }

  public DefaultActionPerformer(
      AutomationContext context,
      LocatorResolver resolver,
      ElementStateProbe stateProbe,
      ScrollManager scrollManager,
      HitTestService hitTestService,
      StabilityService stabilityService,
      RetryPolicy retryPolicy,
      PostActionWaiter postActionWaiter,
      JsClickExecutor jsClickExecutor) {
    this.context = context;
    this.resolver = resolver;
    this.stateProbe = stateProbe;
    this.scrollManager = scrollManager;
    this.hitTestService = hitTestService;
    this.stabilityService = stabilityService;
    this.retryPolicy = retryPolicy;
    this.postActionWaiter = postActionWaiter;
    this.jsClickExecutor = jsClickExecutor;
  }

  @Override
  public void click(LocatorPlan plan, ClickOptions options) {
    ClickOptions effectiveOptions =
        options == null ? ClickOptions.defaultOptions() : options;
    Duration timeout = effectiveTimeout(effectiveOptions.timeoutOverride());
    ActionExecution action = new ActionExecution("click", effectiveOptions.allowJsFallback(), timeout);
    int maxRetries = context.actionPolicy().maxRetries();
    while (!action.timedOut()) {
      action.polls++;
      WebElement element = resolveElement(plan, action);
      if (element == null) {
        continue;
      }
      ActionReady ready = prepareElement(element, action, true);
      if (ready == null) {
        continue;
      }
      action.attempts++;
      try {
        element.click();
        postActionWait();
        return;
      } catch (Exception exception) {
        action.recordException(exception);
        if (shouldRetry(exception, maxRetries, action.attempts)) {
          continue;
        }
        if (effectiveOptions.allowJsFallback()
            && action.attempts > maxRetries
            && retryPolicy.isEligibleForJsFallback(exception, action.lastState, action.lastHitTest)) {
          action.usedJsFallback = true;
          jsClickExecutor.click(driver(), element);
          postActionWait();
          return;
        }
        throw actionFailed("Click failed.", action, exception);
      }
    }
    throw actionFailed("Click timed out.", action, action.lastException);
  }

  @Override
  public void fill(LocatorPlan plan, String text, Duration timeout) {
    Duration effectiveTimeout = effectiveTimeout(timeout);
    ActionExecution action = new ActionExecution("fill", false, effectiveTimeout);
    performKeyedAction(plan, text, action, false);
  }

  @Override
  public void clearAndFill(LocatorPlan plan, String text, Duration timeout) {
    Duration effectiveTimeout = effectiveTimeout(timeout);
    ActionExecution action = new ActionExecution("clearAndFill", false, effectiveTimeout);
    performKeyedAction(plan, text, action, true);
  }

  @Override
  public void pressKey(LocatorPlan plan, Keys key, Duration timeout) {
    Duration effectiveTimeout = effectiveTimeout(timeout);
    ActionExecution action = new ActionExecution("pressKey", false, effectiveTimeout);
    while (!action.timedOut()) {
      action.polls++;
      WebElement element = resolveElement(plan, action);
      if (element == null) {
        continue;
      }
      ActionReady ready = prepareElement(element, action, true);
      if (ready == null) {
        continue;
      }
      action.attempts++;
      try {
        element.click();
        element.sendKeys(key);
        postActionWait();
        return;
      } catch (Exception exception) {
        action.recordException(exception);
        if (shouldRetry(exception, context.actionPolicy().maxRetries(), action.attempts)) {
          continue;
        }
        throw actionFailed("pressKey failed.", action, exception);
      }
    }
    throw actionFailed("pressKey timed out.", action, action.lastException);
  }

  @Override
  public void hover(LocatorPlan plan, Duration timeout) {
    Duration effectiveTimeout = effectiveTimeout(timeout);
    ActionExecution action = new ActionExecution("hover", false, effectiveTimeout);
    while (!action.timedOut()) {
      action.polls++;
      WebElement element = resolveElement(plan, action);
      if (element == null) {
        continue;
      }
      ActionReady ready = prepareElement(element, action, true);
      if (ready == null) {
        continue;
      }
      action.attempts++;
      try {
        ElementRect rect = action.lastState.rect();
        int offsetX = (int) Math.round(ready.clickPoint.getX() - rect.left());
        int offsetY = (int) Math.round(ready.clickPoint.getY() - rect.top());
        new Actions(driver()).moveToElement(element, offsetX, offsetY).perform();
        postActionWait();
        return;
      } catch (Exception exception) {
        action.recordException(exception);
        if (shouldRetry(exception, context.actionPolicy().maxRetries(), action.attempts)) {
          continue;
        }
        throw actionFailed("Hover failed.", action, exception);
      }
    }
    throw actionFailed("Hover timed out.", action, action.lastException);
  }

  @Override
  public void check(LocatorPlan plan, Duration timeout) {
    toggleCheckState(plan, timeout, true);
  }

  @Override
  public void uncheck(LocatorPlan plan, Duration timeout) {
    toggleCheckState(plan, timeout, false);
  }

  @Override
  public void selectOption(LocatorPlan plan, String text, Duration timeout) {
    Duration effectiveTimeout = effectiveTimeout(timeout);
    ActionExecution action = new ActionExecution("selectOption", false, effectiveTimeout);
    while (!action.timedOut()) {
      action.polls++;
      WebElement element = resolveElement(plan, action);
      if (element == null) {
        continue;
      }
      ActionReady ready = prepareElement(element, action, false);
      if (ready == null) {
        continue;
      }
      action.attempts++;
      try {
        boolean matched =
            (Boolean)
                ((JavascriptExecutor) driver())
                    .executeScript(
                        "var select = arguments[0];"
                            + "var label = arguments[1];"
                            + "if (!select || !select.options) { return false; }"
                            + "for (var i = 0; i < select.options.length; i++) {"
                            + "  if (select.options[i].text === label) {"
                            + "    select.selectedIndex = i;"
                            + "    select.options[i].selected = true;"
                            + "    select.dispatchEvent(new Event('change', {bubbles: true}));"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "return false;",
                        element,
                        text);
        if (!matched) {
          throw new ElementNotInteractableException("Option not found for select element.");
        }
        postActionWait();
        return;
      } catch (Exception exception) {
        action.recordException(exception);
        if (shouldRetry(exception, context.actionPolicy().maxRetries(), action.attempts)) {
          continue;
        }
        throw actionFailed("Select option failed.", action, exception);
      }
    }
    throw actionFailed("Select option timed out.", action, action.lastException);
  }

  private void performKeyedAction(LocatorPlan plan, String text, ActionExecution action, boolean clear) {
    int maxRetries = context.actionPolicy().maxRetries();
    while (!action.timedOut()) {
      action.polls++;
      WebElement element = resolveElement(plan, action);
      if (element == null) {
        continue;
      }
      ActionReady ready = prepareElement(element, action, true);
      if (ready == null) {
        continue;
      }
      action.attempts++;
      try {
        element.click();
        if (clear) {
          element.clear();
        }
        element.sendKeys(text);
        postActionWait();
        return;
      } catch (Exception exception) {
        action.recordException(exception);
        if (shouldRetry(exception, maxRetries, action.attempts)) {
          continue;
        }
        throw actionFailed("Fill failed.", action, exception);
      }
    }
    throw actionFailed("Fill timed out.", action, action.lastException);
  }

  private void toggleCheckState(LocatorPlan plan, Duration timeout, boolean desired) {
    Duration effectiveTimeout = effectiveTimeout(timeout);
    ActionExecution action = new ActionExecution(desired ? "check" : "uncheck", false, effectiveTimeout);
    while (!action.timedOut()) {
      action.polls++;
      WebElement element = resolveElement(plan, action);
      if (element == null) {
        continue;
      }
      Boolean checked = readCheckedState(element);
      if (checked != null && checked == desired) {
        return;
      }
      ActionReady ready = prepareElement(element, action, true);
      if (ready == null) {
        continue;
      }
      action.attempts++;
      try {
        element.click();
        postActionWait();
        return;
      } catch (Exception exception) {
        action.recordException(exception);
        if (shouldRetry(exception, context.actionPolicy().maxRetries(), action.attempts)) {
          continue;
        }
        throw actionFailed("Toggle check state failed.", action, exception);
      }
    }
    throw actionFailed("Toggle check state timed out.", action, action.lastException);
  }

  private Boolean readCheckedState(WebElement element) {
    Object result =
        ((JavascriptExecutor) driver())
            .executeScript(
                "var el = arguments[0];"
                    + "if (!el) { return null; }"
                    + "if (typeof el.checked === 'boolean') { return el.checked; }"
                    + "var aria = el.getAttribute('aria-checked');"
                    + "if (aria === 'true') { return true; }"
                    + "if (aria === 'false') { return false; }"
                    + "return null;",
                element);
    if (result instanceof Boolean bool) {
      return bool;
    }
    return null;
  }

  private ActionReady prepareElement(WebElement element, ActionExecution action, boolean requireHitTest) {
    scrollManager.scrollIntoView(driver(), element, context.actionPolicy().scrollAlignment());
    ElementState state = stateProbe.probe(driver(), element);
    action.lastState = state;
    if (!state.attached() || !state.visible() || !state.inViewport()) {
      return null;
    }
    if (context.actionPolicy().requireEnabled() && !state.enabled()) {
      return null;
    }
    if (context.actionPolicy().requirePointerEvents() && !state.pointerEvents()) {
      return null;
    }
    boolean stable =
        stabilityService.isStable(
            driver(),
            element,
            stateProbe,
            context.actionPolicy().stableRectWindow(),
            context.actionPolicy().stableRectPoll(),
            context.actionPolicy().rectTolerancePx());
    if (!stable) {
      action.lastStableFailure = true;
      return null;
    }
    action.lastStableFailure = false;
    Point clickPoint = computeClickPoint(state, context.actionPolicy().clickPoint());
    HitTestResult hitTest = null;
    if (requireHitTest && context.actionPolicy().requireNotCovered()) {
      hitTest = hitTestService.hitTest(driver(), element, clickPoint);
      action.lastHitTest = hitTest;
      if (!hitTest.hit()) {
        return null;
      }
    }
    return new ActionReady(element, clickPoint, hitTest);
  }

  private WebElement resolveElement(LocatorPlan plan, ActionExecution action) {
    try {
      ResolutionResult result =
          resolver.resolve(
              context,
              plan,
              new ResolveOptions(ZERO, context.actionPolicy().pollInterval(), false));
      action.lastResolutionDiagnostics = result.diagnostics();
      return selectElement(result, plan);
    } catch (LocatorNotFoundException exception) {
      action.lastResolutionDiagnostics = exception.diagnostics();
      action.recordException(exception);
      return null;
    } catch (LocatorAmbiguousException exception) {
      action.lastResolutionDiagnostics = exception.diagnostics();
      throw exception;
    }
  }

  private WebElement selectElement(ResolutionResult result, LocatorPlan plan) {
    if (plan.strictness() == Strictness.ONE) {
      return result.singleOrThrow();
    }
    Object nth = plan.tags().get(ActionTags.NTH_INDEX);
    if (!(nth instanceof Number index)) {
      throw new LocatorAmbiguousException("Locator matched multiple elements.", result.diagnostics());
    }
    int position = index.intValue();
    if (result.matches().isEmpty()) {
      throw new LocatorNotFoundException("Locator did not match any elements.", result.diagnostics());
    }
    if (result.matches().size() <= position) {
      throw new LocatorNotFoundException("Locator nth index out of bounds.", result.diagnostics());
    }
    return result.matches().get(position);
  }

  private boolean shouldRetry(Exception exception, int maxRetries, int attempts) {
    if (!retryPolicy.isRetryable(exception)) {
      return false;
    }
    if (attempts > maxRetries) {
      return false;
    }
    if (exception instanceof StaleElementReferenceException) {
      return context.actionPolicy().retryOnStale();
    }
    if (exception instanceof org.openqa.selenium.ElementClickInterceptedException) {
      return context.actionPolicy().retryOnIntercepted();
    }
    if (exception instanceof org.openqa.selenium.MoveTargetOutOfBoundsException) {
      return context.actionPolicy().retryOnOutOfBounds();
    }
    return true;
  }

  private Duration effectiveTimeout(Duration timeoutOverride) {
    if (timeoutOverride == null) {
      return context.actionPolicy().actionTimeout();
    }
    return timeoutOverride;
  }

  private Point computeClickPoint(ElementState state, ClickPointStrategy strategy) {
    ElementRect rect = state.rect();
    if (strategy == ClickPointStrategy.CENTER_VISIBLE) {
      double left = Math.max(rect.left(), 0);
      double top = Math.max(rect.top(), 0);
      double right = Math.min(rect.right(), state.viewportWidth());
      double bottom = Math.min(rect.bottom(), state.viewportHeight());
      if (right > left && bottom > top) {
        double centerX = left + (right - left) / 2.0;
        double centerY = top + (bottom - top) / 2.0;
        return new Point((int) Math.round(centerX), (int) Math.round(centerY));
      }
    }
    return new Point((int) Math.round(rect.centerX()), (int) Math.round(rect.centerY()));
  }

  private void postActionWait() {
    if (context.actionPolicy().postActionWait() == PostActionWait.MINIMAL_SETTLE) {
      postActionWaiter.minimalSettle(driver());
    }
  }

  private ActionFailedException actionFailed(String message, ActionExecution action, Exception exception) {
    ActionDiagnostics diagnostics = action.toDiagnostics();
    List<String> suggestions = buildSuggestions(action);
    if (exception == null) {
      return new ActionFailedException(message, action.lastResolutionDiagnostics, diagnostics, suggestions);
    }
    return new ActionFailedException(
        message, exception, action.lastResolutionDiagnostics, diagnostics, suggestions);
  }

  private List<String> buildSuggestions(ActionExecution action) {
    List<String> suggestions = new ArrayList<>();
    if (action.lastHitTest != null && !action.lastHitTest.hit()) {
      String summary =
          action.lastHitTest.topElementSummary() == null
              ? "unknown element"
              : action.lastHitTest.topElementSummary();
      suggestions.add(
          "Element is covered by: " + summary + ". Consider waiting for overlay to disappear.");
    }
    if (action.lastStableFailure) {
      suggestions.add(
          "Element keeps moving; likely animation/layout shift. Consider waiting for UI transition.");
    }
    if (action.lastState != null && !action.lastState.enabled()) {
      suggestions.add("Element disabled/aria-disabled; likely not ready.");
    }
    if (action.usedJsFallback) {
      suggestions.add(
          "JS click fallback was used; consider fixing overlay/selector/actionability. Native click is recommended.");
    }
    return suggestions;
  }

  private WebDriver driver() {
    return context.unsafe().driver();
  }

  private static class ActionExecution {
    private final String actionType;
    private final boolean allowJsFallback;
    private final Duration timeout;
    private final Instant start;

    private int attempts;
    private int polls;
    private boolean usedJsFallback;
    private Exception lastException;
    private String lastExceptionMessage;
    private ElementState lastState;
    private HitTestResult lastHitTest;
    private boolean lastStableFailure;
    private org.openqa.selenium.locator.ResolutionDiagnostics lastResolutionDiagnostics;

    private ActionExecution(String actionType, boolean allowJsFallback, Duration timeout) {
      this.actionType = actionType;
      this.allowJsFallback = allowJsFallback;
      this.timeout = timeout;
      this.start = Instant.now();
    }

    private boolean timedOut() {
      return Duration.between(start, Instant.now()).compareTo(timeout) >= 0;
    }

    private void recordException(Exception exception) {
      lastException = exception;
      lastExceptionMessage = exception == null ? null : exception.getMessage();
    }

    private ActionDiagnostics toDiagnostics() {
      String exceptionType = lastException == null ? null : lastException.getClass().getName();
      long elapsed = Duration.between(start, Instant.now()).toMillis();
      return new ActionDiagnostics(
          actionType,
          allowJsFallback,
          usedJsFallback,
          attempts,
          exceptionType,
          lastExceptionMessage,
          lastState,
          lastHitTest,
          elapsed,
          polls);
    }
  }

  private record ActionReady(WebElement element, Point clickPoint, HitTestResult hitTest) {}
}
