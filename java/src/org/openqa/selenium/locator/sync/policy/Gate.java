package org.openqa.selenium.locator.sync.policy;

public enum Gate {
  DOCUMENT_READY,
  URL_STABLE,
  NETWORK_QUIET,
  CONSOLE_CLEAN,
  GLOBAL_ANIMATIONS_QUIET,
  TARGET_STABLE,
  ROUTE_EVENT_SEEN,
  DOM_TOKEN_STABLE
}
