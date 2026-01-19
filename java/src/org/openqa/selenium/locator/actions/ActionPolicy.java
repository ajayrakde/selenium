package org.openqa.selenium.locator.actions;

import java.time.Duration;
import org.openqa.selenium.locator.actions.click.ClickPointStrategy;
import org.openqa.selenium.locator.actions.scroll.ScrollAlignment;

public interface ActionPolicy {
  Duration actionTimeout();

  Duration pollInterval();

  Duration stableRectWindow();

  Duration stableRectPoll();

  double rectTolerancePx();

  ScrollAlignment scrollAlignment();

  ClickPointStrategy clickPoint();

  boolean requireNotCovered();

  boolean requirePointerEvents();

  boolean requireEnabled();

  int maxRetries();

  boolean retryOnStale();

  boolean retryOnIntercepted();

  boolean retryOnOutOfBounds();

  PostActionWait postActionWait();
}
