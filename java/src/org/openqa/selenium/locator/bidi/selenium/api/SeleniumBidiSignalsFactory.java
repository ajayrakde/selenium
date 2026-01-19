package org.openqa.selenium.locator.bidi.selenium.api;

import java.util.Objects;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.locator.DefaultDiagnosticsSink;
import org.openqa.selenium.locator.DiagnosticsSink;
import org.openqa.selenium.locator.bidi.api.BidiSignals;
import org.openqa.selenium.locator.bidi.api.Clock;
import org.openqa.selenium.locator.bidi.api.SystemClock;
import org.openqa.selenium.locator.bidi.config.BidiTrackingConfig;
import org.openqa.selenium.locator.bidi.selenium.impl.DefaultBidiSignals;

public class SeleniumBidiSignalsFactory {
  private final Clock clock;
  private final DiagnosticsSink diagnosticsSink;

  public SeleniumBidiSignalsFactory() {
    this(new SystemClock(), new DefaultDiagnosticsSink());
  }

  public SeleniumBidiSignalsFactory(Clock clock, DiagnosticsSink diagnosticsSink) {
    this.clock = Objects.requireNonNull(clock, "clock");
    this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink");
  }

  public BidiSignals create(WebDriver driver, BidiTrackingConfig config) {
    Objects.requireNonNull(driver, "driver");
    Objects.requireNonNull(config, "config");
    return new DefaultBidiSignals(driver, config, clock, diagnosticsSink);
  }
}
