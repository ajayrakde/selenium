package org.openqa.selenium.locator.stability.selenium.impl;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.locator.stability.api.StabilityEvaluator;
import org.openqa.selenium.locator.stability.model.Rect;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;

class DefaultStabilityEvaluator implements StabilityEvaluator {
  @Override
  public boolean isRectStable(StabilityHistory history, long windowMs, double tolerancePx) {
    List<StabilitySnapshot> samples = samplesInWindow(history, windowMs);
    if (samples.size() < 2) {
      return false;
    }

    Rect baseline = samples.get(0).rect();
    double maxDelta = 0;
    for (StabilitySnapshot sample : samples) {
      Rect rect = sample.rect();
      maxDelta =
          Math.max(
              maxDelta,
              Math.max(
                  Math.max(Math.abs(rect.x() - baseline.x()), Math.abs(rect.y() - baseline.y())),
                  Math.max(
                      Math.abs(rect.width() - baseline.width()),
                      Math.abs(rect.height() - baseline.height()))));
    }
    return maxDelta <= tolerancePx;
  }

  @Override
  public boolean isVisibilityStable(StabilityHistory history, long windowMs) {
    List<StabilitySnapshot> samples = samplesInWindow(history, windowMs);
    if (samples.size() < 2) {
      return false;
    }
    StabilitySnapshot baseline = samples.get(0);
    for (StabilitySnapshot sample : samples) {
      if (!sample.visibility().equals(baseline.visibility())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isStable(StabilityHistory history, long windowMs, StabilityConfig cfg) {
    List<StabilitySnapshot> samples = samplesInWindow(history, windowMs);
    if (samples.size() < 2) {
      return false;
    }
    if (!isRectStable(new StabilityHistory(samples), windowMs, cfg.rectTolerancePx())) {
      return false;
    }
    if (cfg.captureVisibilityFlag()
        && !isVisibilityStable(new StabilityHistory(samples), windowMs)) {
      return false;
    }
    if (cfg.captureAnimationsFlag()) {
      for (StabilitySnapshot sample : samples) {
        if (Boolean.TRUE.equals(sample.animationsRunning())) {
          return false;
        }
      }
    }
    return true;
  }

  private List<StabilitySnapshot> samplesInWindow(StabilityHistory history, long windowMs) {
    if (history == null || history.samples().isEmpty()) {
      return List.of();
    }
    long latest = 0;
    for (StabilitySnapshot sample : history.samples()) {
      latest = Math.max(latest, sample.timestampEpochMs());
    }
    long lowerBound = latest - Math.max(0, windowMs);
    List<StabilitySnapshot> filtered = new ArrayList<>();
    for (StabilitySnapshot sample : history.samples()) {
      long ts = sample.timestampEpochMs();
      if (ts >= lowerBound && ts <= latest) {
        filtered.add(sample);
      }
    }
    return filtered;
  }
}
