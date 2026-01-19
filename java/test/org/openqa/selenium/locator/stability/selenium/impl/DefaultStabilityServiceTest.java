package org.openqa.selenium.locator.stability.selenium.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.stability.api.StabilitySampler;
import org.openqa.selenium.locator.stability.model.Rect;
import org.openqa.selenium.locator.stability.model.StabilityConfig;
import org.openqa.selenium.locator.stability.model.StabilityHistory;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.model.VisibilityFlags;

@Tag("UnitTests")
class DefaultStabilityServiceTest {
  @Test
  void appendBoundsHistory() {
    StabilitySampler sampler = new FakeSampler();
    DefaultStabilityService service = new DefaultStabilityService(sampler);
    StabilityConfig cfg = new StabilityConfig(0.5, true, true, 2);

    StabilityHistory history =
        service.append(null, snapshot(1L, 0), cfg);
    history = service.append(history, snapshot(2L, 1), cfg);
    history = service.append(history, snapshot(3L, 2), cfg);

    assertThat(history.samples()).hasSize(2);
    assertThat(history.samples().get(0).timestampEpochMs()).isEqualTo(2L);
    assertThat(history.samples().get(1).timestampEpochMs()).isEqualTo(3L);
  }

  private StabilitySnapshot snapshot(long ts, double x) {
    return new StabilitySnapshot(
        ts,
        new Rect(x, 0, 10, 10),
        new VisibilityFlags(true, true, true),
        null,
        null);
  }

  private static class FakeSampler implements StabilitySampler {
    @Override
    public StabilitySnapshot sampleElement(Object elementHandle, StabilityConfig cfg) {
      throw new UnsupportedOperationException("not used");
    }

    @Override
    public StabilitySnapshot sampleContainer(Object elementHandle, StabilityConfig cfg) {
      throw new UnsupportedOperationException("not used");
    }

    @Override
    public org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot sampleGlobal(
        StabilityConfig cfg) {
      throw new UnsupportedOperationException("not used");
    }
  }
}
