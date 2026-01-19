package org.openqa.selenium.locator.bidi.model;

import java.util.Map;
import java.util.Objects;

public record BidiDiagnostics(String type, long tsEpochMs, Map<String, Object> metrics) {
  public BidiDiagnostics {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(metrics, "metrics");
  }
}
