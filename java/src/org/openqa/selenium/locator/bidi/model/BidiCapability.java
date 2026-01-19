package org.openqa.selenium.locator.bidi.model;

import java.util.Objects;
import java.util.Set;

public record BidiCapability(
    BidiCapabilityStatus status,
    String reason,
    Set<BidiFeature> features) {
  public BidiCapability {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(features, "features");
  }
}
