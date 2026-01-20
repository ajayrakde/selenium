---
title: Locator Failure Trace User Journey
---

# Locator Failure Trace User Journey

## Overview

This flow describes how a test failure is captured and persisted as a trace
bundle. Existing locator engine, sync, stability, and BiDi user journeys are
unchanged; the trace bundle only consumes their diagnostics and does not add
new waits or polling.

## Flow

1. **Test start**
   * The test runner calls `TraceManager.startTest(...)` to begin a trace
     session for the current test.
   * `run.json` is written once per run and `test.json` is prepared for the
     test scope.

2. **Step execution**
   * Each action, sync, or assertion opens a `StepScope` with metadata for the
     step name, type, intent, and target.
   * The step records bounded diagnostics and optional attachments during the
     operation.

3. **Failure detection**
   * When a step fails, the step scope records a failure and hands a
     `FailureBundle` to the session.
   * The session captures a canonical `screenshots/failure.png` if supported
     and available.

4. **Failure classification**
   * The session runs the deterministic `ProbableCauseClassifier` using the
     available diagnostics and exception metadata.
   * The resulting probable cause is written to both `step-XXXX.json` and
     `failure.json` to keep them consistent.

5. **Trace persistence**
   * The session writes `failure.json` with bounded stack traces, artifacts,
     and snapshots.
   * Step files are written as each scope closes, and `test.json` is finalized
     when the test ends.

## Impact on existing journeys

* **Locator Engine**: unchanged. Trace collection is read-only and does not
  alter locator resolution semantics.
* **Sync & Stability**: unchanged. The trace layer stores their diagnostics
  without adding waits or additional polling.
* **BiDi Diagnostics**: unchanged. Snapshot collection is best-effort and
  tolerant of unsupported environments (recording `{ "status": "UNSUPPORTED" }` when needed).
