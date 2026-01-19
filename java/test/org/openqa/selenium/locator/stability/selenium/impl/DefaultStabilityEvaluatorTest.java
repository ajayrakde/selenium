package org.openqa.selenium.locator.stability.selenium.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.stability.model.Rect;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.model.VisibilityFlags;

@Tag("UnitTests")
class DefaultStabilityEvaluatorTest {
  private final DefaultStabilityEvaluator evaluator = new DefaultStabilityEvaluator();

  @Test
  void rectStabilityRequiresAtLeastTwoSamples() {
    StabilityHistory history =
        new StabilityHistory(List.of(snapshot(1000L, new Rect(0, 0, 10, 10), true)));

    assertThat(evaluator.isRectStable(history, 500L, 0.5)).isFalse();
  }

  @Test
  void rectStabilityHonorsTolerance() {
    StabilityHistory history =
        new StabilityHistory(
            List.of(
                snapshot(1000L, new Rect(10, 10, 20, 20), true),
                snapshot(1200L, new Rect(10.4, 10.3, 20.2, 19.9), true)));

    assertThat(evaluator.isRectStable(history, 500L, 0.5)).isTrue();
  }

  @Test
  void rectStabilityDetectsMovementBeyondTolerance() {
    StabilityHistory history =
        new StabilityHistory(
            List.of(
                snapshot(1000L, new Rect(10, 10, 20, 20), true),
                snapshot(1200L, new Rect(12, 10, 20, 20), true)));

    assertThat(evaluator.isRectStable(history, 500L, 0.5)).isFalse();
  }

  @Test
  void visibilityStabilityRequiresNoToggles() {
    StabilityHistory history =
        new StabilityHistory(
            List.of(
                snapshot(1000L, new Rect(0, 0, 10, 10), true),
                snapshot(1200L, new Rect(0, 0, 10, 10), false)));

    assertThat(evaluator.isVisibilityStable(history, 500L)).isFalse();
  }

  @Test
  void stableChecksAnimationsWhenEnabled() {
    StabilityConfig cfg = new StabilityConfig(0.5, true, true, 10);
    StabilityHistory history =
        new StabilityHistory(
            List.of(
                snapshot(1000L, new Rect(0, 0, 10, 10), true, false),
                snapshot(1200L, new Rect(0, 0, 10, 10), true, true)));

    assertThat(evaluator.isStable(history, 500L, cfg)).isFalse();
  }

  private StabilitySnapshot snapshot(long ts, Rect rect, boolean visible) {
    return snapshot(ts, rect, visible, false);
  }

  private StabilitySnapshot snapshot(long ts, Rect rect, boolean visible, Boolean animRunning) {
    return new StabilitySnapshot(
        ts,
        rect,
        new VisibilityFlags(true, visible, true),
        animRunning,
        null);
  }
}
