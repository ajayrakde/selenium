package org.openqa.selenium.locator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of resolving a locator plan.
 */
final class ResolutionResult {
  private final List<Object> matches;
  private final Object primary;
  private final ResolutionDiagnostics diagnostics;

  ResolutionResult(List<Object> matches, Object primary, ResolutionDiagnostics diagnostics) {
    this.matches = Collections.unmodifiableList(List.copyOf(Objects.requireNonNull(matches, "matches")));
    this.primary = primary;
    this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
  }

  List<Object> matches() {
    return matches;
  }

  Object primary() {
    return primary;
  }

  ResolutionDiagnostics diagnostics() {
    return diagnostics;
  }
}
