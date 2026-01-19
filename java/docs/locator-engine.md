# Locator Engine (Priority #1)

This document defines the Priority #1 Locator Engine surface for Selenium Java. It covers the public API skeleton, immutable plan model, strictness defaults, and diagnostics expectations. The implementation is intentionally additive: existing Selenium usage remains unchanged while new tests can opt into locator-based flows.

## Goals

- Live locators (re-resolve at action time; no `WebElement` escapes).
- Strict-by-default resolution with rich diagnostics (default Strict ONE, `.all()` for MANY).
- Scoped and composable queries (`within`, `nth`, `filter`).
- Governance via selector tiers and policy (default `data-testid`).

## Core Types (Skeleton)

- `Locator`, `Locators`, `LocatorFactory` for public usage.
- `LocatorPlan`, `ScopePlan`, `SelectorStep`, `FilterSpec` as immutable plan elements.
- `LocatorResolver` for resolution, with `ResolveOptions` and `ResolutionDiagnostics`.
- Exception hierarchy: `LocatorException` + specialized subclasses.
- `AutomationContext` and `Unsafe` to support additive adoption without regression.

## Strictness Model

- Default strictness is `ONE`.
- `all()` is the explicit opt-in to `MANY`.
- Ambiguous or missing matches raise dedicated exceptions that include diagnostics.

## Governance

Selector tiers are modeled in `SelectorTier`, with a policy defined by `TierPolicy`. Tiered usage is a required part of diagnostics and telemetry.

## Next Steps

- Implement `LocatorFactory` and `LocatorResolver` with Selenium integration.
- Add plan-to-selector compilation and filter evaluation.
- Provide diagnostics formatting for logs/CI.
