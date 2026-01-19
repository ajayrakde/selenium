package org.openqa.selenium.locator.bidi.config;

import java.util.Objects;

public record BidiTrackingConfig(
    NetworkTrackingConfig network,
    ConsoleTrackingConfig console,
    NavigationTrackingConfig navigation) {
  public BidiTrackingConfig {
    Objects.requireNonNull(network, "network");
    Objects.requireNonNull(console, "console");
    Objects.requireNonNull(navigation, "navigation");
  }
}
