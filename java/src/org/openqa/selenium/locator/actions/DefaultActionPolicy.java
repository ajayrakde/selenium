package org.openqa.selenium.locator.actions;

import java.time.Duration;
import org.openqa.selenium.locator.actions.click.ClickPointStrategy;
import org.openqa.selenium.locator.actions.scroll.ScrollAlignment;

public class DefaultActionPolicy implements ActionPolicy {
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration DEFAULT_POLL = Duration.ofMillis(100);
  private static final Duration DEFAULT_STABLE_WINDOW = Duration.ofMillis(400);
  private static final Duration DEFAULT_STABLE_POLL = Duration.ofMillis(100);
  private static final double DEFAULT_RECT_TOLERANCE = 0.5;
  private static final int DEFAULT_MAX_RETRIES = 1;
  private static final int MAX_ALLOWED_RETRIES = 2;

  @Override
  public Duration actionTimeout() {
    return DEFAULT_TIMEOUT;
  }

  @Override
  public Duration pollInterval() {
    return DEFAULT_POLL;
  }

  @Override
  public Duration stableRectWindow() {
    return DEFAULT_STABLE_WINDOW;
  }

  @Override
  public Duration stableRectPoll() {
    return DEFAULT_STABLE_POLL;
  }

  @Override
  public double rectTolerancePx() {
    return DEFAULT_RECT_TOLERANCE;
  }

  @Override
  public ScrollAlignment scrollAlignment() {
    return ScrollAlignment.CENTER;
  }

  @Override
  public ClickPointStrategy clickPoint() {
    return ClickPointStrategy.CENTER_VISIBLE;
  }

  @Override
  public boolean requireNotCovered() {
    return true;
  }

  @Override
  public boolean requirePointerEvents() {
    return true;
  }

  @Override
  public boolean requireEnabled() {
    return true;
  }

  @Override
  public int maxRetries() {
    return Math.min(MAX_ALLOWED_RETRIES, DEFAULT_MAX_RETRIES);
  }

  @Override
  public boolean retryOnStale() {
    return true;
  }

  @Override
  public boolean retryOnIntercepted() {
    return true;
  }

  @Override
  public boolean retryOnOutOfBounds() {
    return true;
  }

  @Override
  public PostActionWait postActionWait() {
    return PostActionWait.MINIMAL_SETTLE;
  }
}
