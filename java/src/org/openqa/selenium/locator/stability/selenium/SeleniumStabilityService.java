package org.openqa.selenium.locator.stability.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.stability.api.Clock;
import org.openqa.selenium.locator.stability.api.StabilityEvaluator;
import org.openqa.selenium.locator.stability.api.StabilityService;
import org.openqa.selenium.locator.stability.api.SystemClock;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.selenium.impl.DefaultStabilityEvaluator;
import org.openqa.selenium.locator.stability.selenium.impl.DefaultStabilityService;
import org.openqa.selenium.locator.stability.selenium.impl.JsStabilitySampler;

public class SeleniumStabilityService implements StabilityService {
  private final StabilityService delegate;
  private final StabilityEvaluator evaluator;

  public SeleniumStabilityService(WebDriver driver) {
    this(driver, new SystemClock());
  }

  public SeleniumStabilityService(WebDriver driver, Clock clock) {
    this.delegate = new DefaultStabilityService(new JsStabilitySampler(driver, clock));
    this.evaluator = new DefaultStabilityEvaluator();
  }

  public StabilityEvaluator evaluator() {
    return evaluator;
  }

  @Override
  public StabilitySnapshot sample(Object elementHandle) {
    return delegate.sample(elementHandle);
  }

  @Override
  public StabilitySnapshot sample(Object elementHandle, StabilityConfig cfg) {
    return delegate.sample(elementHandle, cfg);
  }

  @Override
  public GlobalStabilitySnapshot sampleGlobal() {
    return delegate.sampleGlobal();
  }

  @Override
  public GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg) {
    return delegate.sampleGlobal(cfg);
  }

  @Override
  public StabilityHistory append(
      StabilityHistory history, StabilitySnapshot snapshot, StabilityConfig cfg) {
    return delegate.append(history, snapshot, cfg);
  }
}
