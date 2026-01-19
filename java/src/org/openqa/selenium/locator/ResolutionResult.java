package org.openqa.selenium.locator;

import java.util.List;
import java.util.Objects;
import org.openqa.selenium.WebElement;

/**
 * Result of resolving a locator plan.
 */
public final class ResolutionResult {
  private final LocatorPlan plan;
  private final Strictness strictness;
  private final List<WebElement> matches;
  private final ResolutionDiagnostics diagnostics;

  ResolutionResult(
      LocatorPlan plan,
      Strictness strictness,
      List<WebElement> matches,
      ResolutionDiagnostics diagnostics) {
    this.plan = Objects.requireNonNull(plan, "plan");
    this.strictness = Objects.requireNonNull(strictness, "strictness");
    this.matches = List.copyOf(Objects.requireNonNull(matches, "matches"));
    this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
  }

  public LocatorPlan plan() {
    return plan;
  }

  public Strictness strictness() {
    return strictness;
  }

  public ResolutionDiagnostics diagnostics() {
    return diagnostics;
  }

  List<WebElement> matches() {
    return matches;
  }

  public WebElement singleOrThrow() {
    if (matches.isEmpty()) {
      throw new LocatorNotFoundException("Expected at least one match.", diagnostics);
    }
    if (matches.size() != 1) {
      throw new LocatorAmbiguousException("Expected exactly one match.", diagnostics);
    }
    return matches.get(0);
  }
}
