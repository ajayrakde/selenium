package org.openqa.selenium.locator.stability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.locator.stability.api.Clock;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.selenium.js.JsResultParser;

@Tag("UnitTests")
class JsResultParserTest {
  private final Clock clock = () -> 1234L;

  @Test
  void parsesElementSample() {
    Map<String, Object> rect = new HashMap<>();
    rect.put("x", 1.0);
    rect.put("y", 2.0);
    rect.put("w", 3.0);
    rect.put("h", 4.0);

    Map<String, Object> payload = new HashMap<>();
    payload.put("ts", 1000L);
    payload.put("attached", true);
    payload.put("displayed", true);
    payload.put("hasBox", true);
    payload.put("rect", rect);
    payload.put("animRunning", Boolean.TRUE);

    StabilitySnapshot snapshot = JsResultParser.parseElementSample(payload, clock);

    assertThat(snapshot.timestampEpochMs()).isEqualTo(1000L);
    assertThat(snapshot.rect().x()).isEqualTo(1.0);
    assertThat(snapshot.rect().y()).isEqualTo(2.0);
    assertThat(snapshot.rect().width()).isEqualTo(3.0);
    assertThat(snapshot.rect().height()).isEqualTo(4.0);
    assertThat(snapshot.visibility().attached()).isTrue();
    assertThat(snapshot.animationsRunning()).isTrue();
  }

  @Test
  void parsesGlobalSample() {
    Map<String, Object> payload = new HashMap<>();
    payload.put("ts", 2000L);
    payload.put("animSupported", true);
    payload.put("animRunning", Boolean.FALSE);

    GlobalStabilitySnapshot snapshot = JsResultParser.parseGlobalSample(payload, clock);

    assertThat(snapshot.timestampEpochMs()).isEqualTo(2000L);
    assertThat(snapshot.animationsSupported()).isTrue();
    assertThat(snapshot.animationsRunning()).isFalse();
  }
}
