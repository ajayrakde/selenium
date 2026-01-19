# User Journey: WebDriver BiDi Signals (Priority #4)

This user journey documents the **facts-only** signal collection flow. It is intentionally
separate from synchronization and readiness decisions (Priority #3).

## Journey flow

1. **Create configuration**
   - Enable only the trackers you need (network, console, navigation).
   - Provide ring buffer sizes and ignore rules.
2. **Create signals instance**
   - Use `SeleniumBidiSignalsFactory` with a `WebDriver`.
3. **Start collection**
   - Call `signals.start()` after driver/session creation.
4. **Consume snapshots**
   - Call `snapshot()` on individual trackers or `snapshotAll()` on the facade.
5. **Teardown**
   - Call `signals.stop()` (or `close()`), typically alongside driver cleanup.

## Non-goals (explicit)

- No waits, timeouts, polling, or idle heuristics
- No readiness judgments
- No unbounded logs

## User-impact check

- Existing locator engine journeys are unaffected; BiDi signals are opt-in and do not alter
  WebDriver behavior until explicitly created and started.
