package org.openqa.selenium.locator.sync.selenium.js;

import java.util.List;
import org.openqa.selenium.JavascriptExecutor;

public class JsHistoryHook {
  private static final String INSTALL_SCRIPT =
      "if (!window.__selenium_sync_history) {"
          + "  window.__selenium_sync_history = {seq: 0, last: null};"
          + "  var record = function(type, url) {"
          + "    window.__selenium_sync_history.seq += 1;"
          + "    window.__selenium_sync_history.last = {ts: Date.now(), type: type, url: url || location.href};"
          + "  };"
          + "  var origPush = history.pushState;"
          + "  history.pushState = function() {"
          + "    var ret = origPush.apply(this, arguments);"
          + "    record('pushState');"
          + "    return ret;"
          + "  };"
          + "  var origReplace = history.replaceState;"
          + "  history.replaceState = function() {"
          + "    var ret = origReplace.apply(this, arguments);"
          + "    record('replaceState');"
          + "    return ret;"
          + "  };"
          + "  window.addEventListener('popstate', function() { record('popstate'); });"
          + "}"
          + "return true;";

  private final JavascriptExecutor js;
  private boolean available = true;

  public JsHistoryHook(JavascriptExecutor js) {
    this.js = js;
  }

  public boolean installIfNeeded() {
    if (!available) {
      return false;
    }
    try {
      js.executeScript(INSTALL_SCRIPT);
      return true;
    } catch (Exception ignored) {
      available = false;
      return false;
    }
  }

  public HistoryHookState readState() {
    if (!available) {
      return null;
    }
    try {
      Object raw =
          js.executeScript(
              "var s = window.__selenium_sync_history;"
                  + "if (!s) { return null; }"
                  + "return [s.seq, s.last ? s.last.ts : null, s.last ? s.last.url : null, s.last ? s.last.type : null];");
      if (!(raw instanceof List<?> list) || list.isEmpty()) {
        return null;
      }
      Long seq = toLong(list.get(0));
      Long lastTs = toLong(list.size() > 1 ? list.get(1) : null);
      String lastUrl = list.size() > 2 ? toString(list.get(2)) : null;
      String lastType = list.size() > 3 ? toString(list.get(3)) : null;
      return new HistoryHookState(seq == null ? 0 : seq, lastTs, lastUrl, lastType);
    } catch (Exception ignored) {
      available = false;
      return null;
    }
  }

  public boolean isAvailable() {
    return available;
  }

  private Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private String toString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  public record HistoryHookState(long seq, Long lastTs, String lastUrl, String lastType) {}
}
