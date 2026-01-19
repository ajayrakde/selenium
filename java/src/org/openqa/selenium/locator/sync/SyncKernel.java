package org.openqa.selenium.locator.sync;

public interface SyncKernel {
  WaitResult waitReady(Intent intent, WaitTarget target, WaitOptions options);

  default WaitResult waitReady(Intent intent) {
    return waitReady(intent, new WaitTargetNone(), WaitOptions.defaults());
  }

  void onBeforeAction(ActionContext ctx);

  void onAfterAction(ActionContext ctx, ActionOutcome outcome);
}
