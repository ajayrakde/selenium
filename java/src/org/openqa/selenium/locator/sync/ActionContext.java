package org.openqa.selenium.locator.sync;

import java.util.Objects;

public final class ActionContext {
  private final ActionType actionType;
  private final WaitTarget target;
  private final WaitTarget containerTarget;
  private final WaitOptions waitOptions;
  private final boolean jsFallbackUsed;

  public ActionContext(
      ActionType actionType,
      WaitTarget target,
      WaitTarget containerTarget,
      WaitOptions waitOptions,
      boolean jsFallbackUsed) {
    this.actionType = Objects.requireNonNull(actionType, "actionType");
    this.target = target;
    this.containerTarget = containerTarget;
    this.waitOptions = waitOptions == null ? WaitOptions.defaults() : waitOptions;
    this.jsFallbackUsed = jsFallbackUsed;
  }

  public ActionType actionType() {
    return actionType;
  }

  public WaitTarget target() {
    return target;
  }

  public WaitTarget containerTarget() {
    return containerTarget;
  }

  public WaitOptions waitOptions() {
    return waitOptions;
  }

  public boolean jsFallbackUsed() {
    return jsFallbackUsed;
  }
}
