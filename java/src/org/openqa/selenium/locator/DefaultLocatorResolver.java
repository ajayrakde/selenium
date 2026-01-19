package org.openqa.selenium.locator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Default locator resolver implementation.
 */
public class DefaultLocatorResolver implements LocatorResolver {
  private final SelectorCompiler compiler = new SelectorCompiler();
  private final FilterEngine filterEngine = new FilterEngine();
  private final CandidateSummarizer summarizer = new CandidateSummarizer();

  @Override
  public ResolutionResult resolve(AutomationContext context, LocatorPlan plan, ResolveOptions options) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(plan, "plan");
    Objects.requireNonNull(options, "options");

    Instant start = Instant.now();
    int polls = 0;
    List<WebElement> matches = List.of();
    Strictness strictness = plan.strictness();
    Duration timeout = options.timeout();
    Duration pollInterval = options.pollInterval();

    ResolutionDiagnostics diagnostics = null;
    do {
      polls++;
      SearchContext scope = resolveScope(context, plan, options);
      By by = compiler.compile(plan, context.policy());
      matches = filterEngine.apply(scope.findElements(by), plan.filters(), context.policy());

      diagnostics = buildDiagnostics(context, plan, start, polls, matches, "success", List.of());

      if (strictness == Strictness.ONE && matches.size() == 1) {
        break;
      }

      if (strictness == Strictness.ONE && matches.size() > 1) {
        diagnostics = withCandidatePreviews(
            context,
            buildDiagnostics(context, plan, start, polls, matches, "ambiguous", suggestionsForAmbiguous(plan)),
            matches,
            options,
            plan);
        context.diagnostics().record(diagnostics);
        throw new LocatorAmbiguousException("Locator matched multiple elements.", diagnostics);
      }

      if (!timeout.isZero() && Instant.now().isBefore(start.plus(timeout))) {
        sleep(pollInterval);
      } else {
        break;
      }
    } while (true);

    diagnostics = buildDiagnostics(context, plan, start, polls, matches, "success", List.of());
    if (strictness == Strictness.ONE && matches.isEmpty()) {
      diagnostics = withCandidatePreviews(
          context,
          buildDiagnostics(context, plan, start, polls, matches, "notfound", suggestionsForNotFound(plan)),
          matches,
          options,
          plan);
      context.diagnostics().record(diagnostics);
      throw new LocatorNotFoundException("Locator did not match any elements.", diagnostics);
    }

    if (options.includeCandidatePreviews()) {
      diagnostics = withCandidatePreviews(context, diagnostics, matches, options, plan);
    }

    context.diagnostics().record(diagnostics);
    return new ResolutionResult(plan, strictness, matches, diagnostics);
  }

  private SearchContext resolveScope(AutomationContext context, LocatorPlan plan, ResolveOptions options) {
    if (plan.scope() instanceof RootScope) {
      return context.unsafe().driver();
    }

    WithinScope within = (WithinScope) plan.scope();
    LocatorPlan containerPlan = within.container().withStrictness(Strictness.ONE);
    try {
      ResolutionResult result = resolve(context, containerPlan, options);
      return result.singleOrThrow();
    } catch (LocatorException exception) {
      throw new LocatorScopeException("Failed to resolve scope container.", exception, exception.diagnostics());
    }
  }

  private ResolutionDiagnostics buildDiagnostics(
      AutomationContext context,
      LocatorPlan plan,
      Instant start,
      int polls,
      List<WebElement> matches,
      String outcome,
      List<String> suggestions) {
    long elapsed = Duration.between(start, Instant.now()).toMillis();
    String url = safeString(() -> context.unsafe().driver().getCurrentUrl());
    String title = safeString(() -> context.unsafe().driver().getTitle());
    return new ResolutionDiagnostics(
        plan.name(),
        PlanDescriber.describe(plan),
        plan.strictness(),
        url,
        title,
        elapsed,
        polls,
        matches.size(),
        List.of(),
        suggestions,
        Map.of(
            "selectorTier", plan.tier().name(),
            "matchCount", matches.size(),
            "outcome", outcome,
            "locatorId", plan.tags().getOrDefault("locatorId", "")));
  }

  private ResolutionDiagnostics withCandidatePreviews(
      AutomationContext context,
      ResolutionDiagnostics diagnostics,
      List<WebElement> matches,
      ResolveOptions options,
      LocatorPlan plan) {
    List<CandidatePreview> previews = summarizer.summarize(
        matches,
        context.policy().maxCandidatesInDiagnostics(),
        context.policy());
    return new ResolutionDiagnostics(
        diagnostics.locatorName(),
        diagnostics.planDescription(),
        diagnostics.strictness(),
        diagnostics.url(),
        diagnostics.title(),
        diagnostics.elapsedMillis(),
        diagnostics.polls(),
        diagnostics.matchCount(),
        previews,
        diagnostics.suggestions(),
        diagnostics.metrics());
  }

  private List<String> suggestionsForAmbiguous(LocatorPlan plan) {
    List<String> suggestions = new ArrayList<>();
    if (plan.scope() instanceof RootScope) {
      suggestions.add("Consider scoping with within(container) to reduce matches.");
    }
    if (plan.tier() == SelectorTier.TIER3_CSS || plan.tier() == SelectorTier.TIER4_XPATH) {
      suggestions.add("Prefer byTestId for stable selectors.");
    }
    suggestions.add("Use nth(index) to disambiguate if multiple matches are expected.");
    return suggestions;
  }

  private List<String> suggestionsForNotFound(LocatorPlan plan) {
    List<String> suggestions = new ArrayList<>();
    if (plan.scope() instanceof WithinScope) {
      suggestions.add("Verify the scoped container exists before querying within it.");
    }
    suggestions.add("Verify the selector or test id value.");
    return suggestions;
  }

  private String safeString(SupplierWithException supplier) {
    try {
      return Objects.toString(supplier.get(), "");
    } catch (RuntimeException ignored) {
      return "";
    }
  }

  private void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }

  @FunctionalInterface
  private interface SupplierWithException {
    Object get();
  }
}
