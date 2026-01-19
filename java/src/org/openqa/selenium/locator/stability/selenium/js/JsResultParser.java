package org.openqa.selenium.locator.stability.selenium.js;

import java.util.Map;
import org.openqa.selenium.locator.stability.api.Clock;
import org.openqa.selenium.locator.stability.model.GlobalStabilitySnapshot;
import org.openqa.selenium.locator.stability.model.Rect;
import org.openqa.selenium.locator.stability.model.StabilitySnapshot;
import org.openqa.selenium.locator.stability.model.VisibilityFlags;

public final class JsResultParser {
  private JsResultParser() {}

  @SuppressWarnings("unchecked")
  public static StabilitySnapshot parseElementSample(Map<String, Object> payload, Clock clock) {
    if (payload == null) {
      return new StabilitySnapshot(
          clock.nowEpochMs(),
          new Rect(0, 0, 0, 0),
          new VisibilityFlags(false, false, false),
          null,
          "missing");
    }

    Map<String, Object> rectMap = (Map<String, Object>) payload.get("rect");
    Rect rect =
        new Rect(
            asDouble(rectMap, "x"),
            asDouble(rectMap, "y"),
            asDouble(rectMap, "w"),
            asDouble(rectMap, "h"));

    VisibilityFlags visibility =
        new VisibilityFlags(
            Boolean.TRUE.equals(payload.get("attached")),
            Boolean.TRUE.equals(payload.get("displayed")),
            Boolean.TRUE.equals(payload.get("hasBox")));

    return new StabilitySnapshot(
        asLong(payload.get("ts"), clock), rect, visibility, (Boolean) payload.get("animRunning"), null);
  }

  public static GlobalStabilitySnapshot parseGlobalSample(Map<String, Object> payload, Clock clock) {
    if (payload == null) {
      return new GlobalStabilitySnapshot(clock.nowEpochMs(), false, null, "missing");
    }

    return new GlobalStabilitySnapshot(
        asLong(payload.get("ts"), clock),
        Boolean.TRUE.equals(payload.get("animSupported")),
        (Boolean) payload.get("animRunning"),
        null);
  }

  private static double asDouble(Map<String, Object> map, String key) {
    if (map == null) {
      return 0;
    }
    Object value = map.get(key);
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return 0;
  }

  private static long asLong(Object value, Clock clock) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    return clock.nowEpochMs();
  }
}
