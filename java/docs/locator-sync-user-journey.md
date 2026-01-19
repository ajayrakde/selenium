# Locator Sync Kernel User Journey

This document describes the user journey for adopting the Sync Kernel readiness engine in Selenium Java.
It is opt-in and only applies when framework actions/assert helpers call the Sync Kernel APIs.

## Journey: Action Readiness via SyncKernel

1. **Create or obtain a SyncKernel**
   - Use `SeleniumSyncKernelFactory` with a WebDriver, policy, and optional BiDi + stability services.
2. **Hook into action lifecycle**
   - Framework calls `onBeforeAction` to capture baseline signals.
   - Framework calls `onAfterAction` with action outcome to trigger readiness waits.
3. **Wait for intent-specific readiness**
   - `waitReady` enforces gates based on intent (CLICK, TYPE, NAVIGATE, ASSERT, MODAL).
   - BiDi-dependent gates are skipped when unsupported, with diagnostics noting degradation.
4. **Handle diagnostics on timeout**
   - Failures return `SyncDiagnostics` that include gate states and last signal snapshots.

## Example Flow

```java
SyncKernel kernel = SeleniumSyncKernelFactory.create(driver, DefaultSyncPolicy.create(), bidi, stability);

ActionContext ctx = new ActionContext(
    ActionType.CLICK,
    new WaitTargetElement(buttonElement),
    null,
    WaitOptions.defaults(),
    false);

kernel.onBeforeAction(ctx);
// perform click here
kernel.onAfterAction(ctx, ActionOutcome.SUCCESS);
```

## Impact Check

- The sync kernel does not intercept WebDriver globally; it is only used where actions are routed through it.
- Existing locator usage and diagnostics remain valid without changes.
- Readiness waits are bounded by policy timeouts and can be disabled per call using custom policy or options.
