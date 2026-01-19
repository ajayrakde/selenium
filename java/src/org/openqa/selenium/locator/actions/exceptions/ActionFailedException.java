package org.openqa.selenium.locator.actions.exceptions;

import java.util.List;
import java.util.Objects;
import org.openqa.selenium.locator.LocatorException;
import org.openqa.selenium.locator.ResolutionDiagnostics;
import org.openqa.selenium.locator.actions.diagnostics.ActionDiagnostics;

public class ActionFailedException extends LocatorException {
  private final ActionDiagnostics actionDiagnostics;
  private final List<String> suggestions;

  public ActionFailedException(
      String message,
      ResolutionDiagnostics diagnostics,
      ActionDiagnostics actionDiagnostics,
      List<String> suggestions) {
    super(message, diagnostics);
    this.actionDiagnostics = Objects.requireNonNull(actionDiagnostics, "actionDiagnostics");
    this.suggestions = List.copyOf(Objects.requireNonNull(suggestions, "suggestions"));
  }

  public ActionFailedException(
      String message,
      Throwable cause,
      ResolutionDiagnostics diagnostics,
      ActionDiagnostics actionDiagnostics,
      List<String> suggestions) {
    super(message, cause, diagnostics);
    this.actionDiagnostics = Objects.requireNonNull(actionDiagnostics, "actionDiagnostics");
    this.suggestions = List.copyOf(Objects.requireNonNull(suggestions, "suggestions"));
  }

  public ActionDiagnostics actionDiagnostics() {
    return actionDiagnostics;
  }

  public List<String> suggestions() {
    return suggestions;
  }
}
