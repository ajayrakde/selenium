package org.openqa.selenium.locator.stability.model;

import java.util.List;

public record StabilityHistory(List<StabilitySnapshot> samples) {
  public StabilityHistory {
    samples = List.copyOf(samples);
  }
}
