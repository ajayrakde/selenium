package org.openqa.selenium.locator.stability.selenium.impl;

import java.util.Map;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.stability.api.Clock;
import org.openqa.selenium.locator.stability.api.StabilitySampler;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.Rect;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.model.VisibilityFlags;
import org.openqa.selenium.locator.stability.selenium.js.JsResultParser;
import org.openqa.selenium.locator.stability.selenium.js.JsScripts;

class JsStabilitySampler implements StabilitySampler {
  private final JavascriptExecutor js;
  private final Clock clock;

  JsStabilitySampler(WebDriver driver, Clock clock) {
    this.js = (JavascriptExecutor) driver;
    this.clock = clock;
  }

  @Override
  public StabilitySnapshot sampleElement(Object elementHandle, StabilityConfig cfg) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> result =
          (Map<String, Object>)
              js.executeScript(
                  JsScripts.ELEMENT_SAMPLE, elementHandle, cfg.captureAnimationsFlag());
      return JsResultParser.parseElementSample(result, clock);
    } catch (StaleElementReferenceException stale) {
      return staleSnapshot("stale");
    } catch (RuntimeException error) {
      return staleSnapshot("error");
    }
  }

  @Override
  public StabilitySnapshot sampleContainer(Object elementHandle, StabilityConfig cfg) {
    return sampleElement(elementHandle, cfg);
  }

  @Override
  public GlobalStabilitySnapshot sampleGlobal(StabilityConfig cfg) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) js.executeScript(JsScripts.GLOBAL_SAMPLE);
      return JsResultParser.parseGlobalSample(result, clock);
    } catch (RuntimeException error) {
      return new GlobalStabilitySnapshot(clock.nowEpochMs(), false, null, "unsupported");
    }
  }

  private StabilitySnapshot staleSnapshot(String note) {
    return new StabilitySnapshot(
        clock.nowEpochMs(),
        new Rect(0, 0, 0, 0),
        new VisibilityFlags(false, false, false),
        null,
        note);
  }
}
