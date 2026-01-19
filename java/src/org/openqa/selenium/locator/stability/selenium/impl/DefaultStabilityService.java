package org.openqa.selenium.locator.stability.selenium.impl;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.locator.stability.api.StabilitySampler;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;

class DefaultStabilityService implements StabilityService {
  private final StabilitySampler sampler;

  DefaultStabilityService(StabilitySampler sampler) {
    this.sampler = sampler;
  }

  @Override
  public StabilitySnapshot sample(Object elementHandle) {
    return sample(elementHandle, StabilityConfig.defaultConfig());
  }

  @Override
  public StabilitySnapshot sample(Object elementHandle, StabilityConfig cfg) {
    return sampler.sampleElement(elementHandle, cfg);
  }

  @Override
  public GlobalStabilitySnapshot sampleGlobal() {
    return sampleGlobal(StabilityConfig.defaultConfig());
  }

  @Override
  public GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg) {
    return sampler.sampleGlobal(cfg);
  }

  @Override
  public StabilityHistory append(
      StabilityHistory history, StabilitySnapshot snapshot, StabilityConfig cfg) {
    List<StabilitySnapshot> updated = new ArrayList<>();
    if (history != null) {
      updated.addAll(history.samples());
    }
    updated.add(snapshot);
    int max = cfg.maxSamplesInHistory();
    if (max > 0 && updated.size() > max) {
      updated = updated.subList(updated.size() - max, updated.size());
    }
    return new StabilityHistory(updated);
  }
}
