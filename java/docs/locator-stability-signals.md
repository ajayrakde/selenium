# Locator Stability Signals (Priority #5)

This document describes the non-blocking stability signals introduced for the Locator Engine in
Selenium Java. These signals provide snapshots and pure evaluation helpers that higher-level
pipelines can use for actionability and synchronization decisions.

## Overview

The stability signals are split into two layers:

- **Core (signal-only)**: Records, configuration, and pure evaluators with no Selenium types.
- **Selenium integration**: JS sampling and adapters that translate Selenium data into core models.

The signal layer **does not wait** and **does not sleep**. Waiting decisions remain in higher-level
components.

## Element and container signals

Sampling returns:

- A bounding rect `{x, y, width, height}` from `getBoundingClientRect()`.
- Visibility flags: `attached`, `displayed`, `hasBox`.
- Optional animation status when supported.

Sampling uses a single JS round trip per call and never pulls outer HTML.

## Global signal

A global sample reports whether animations are supported and whether any animations are running.
If the API is unavailable, `animationsRunning` remains `null`.

## Evaluation helpers

The evaluator is pure and uses timestamped samples:

- `isRectStable` checks max rect deltas against tolerance.
- `isVisibilityStable` ensures visibility flags do not toggle.
- `isStable` combines rect + visibility + optional animation checks.

## Diagnostics integration

Actionability and synchronization pipelines can append stability samples to diagnostics without
blocking. This makes stability histories available when an action fails or a readiness evaluation
needs additional context.
