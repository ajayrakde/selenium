---
title: Locator Failure Trace
---

# Locator Failure Trace

The Locator Failure Trace subsystem produces a bounded, structured bundle of
JSON files whenever a test fails. The bundle is intended to be machine-readable
first and supports optional human-readable tooling later.

## What gets captured

On failure, a trace directory is created containing:

* `run.json` (run-level environment metadata)
* `test.json` (test-level metadata and step list)
* `step-XXXX.json` files for each action, sync, or assertion step
* `failure.json` (primary failure details and snapshots)
* `screenshots/failure.png` when screenshots are enabled and supported
* `attachments/` for bounded text or JSON artifacts

## Boundedness guarantees

The trace writer enforces limits to avoid flakiness or memory growth:

* step count, stacktrace, and log line limits via `TraceOptions`
* stability sample caps
* bounded stack traces and message lengths
* attachments truncated to a maximum byte budget

## Integration points

The trace session is designed to be wired into the Locator actionability,
sync, BiDi, and stability subsystems. Callers can pass action diagnostics,
sync diagnostics, and BiDi snapshots as structured objects (maps) and the
trace writer will persist them in the step and failure JSON files. When BiDi
is unavailable, snapshots can explicitly report `{ "status": "UNSUPPORTED" }`.
