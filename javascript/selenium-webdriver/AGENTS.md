# Guidance for contributors working in `javascript/selenium-webdriver/`

- Keep edits within this package; avoid modifying `bazel-*` outputs, `third_party/`, or shared HTML in `common/src/web/` unless the change requires it.
- Preserve Apache headers and code style; aim for small, targeted diffs instead of sweeping refactors or formatting.
- When changing WebDriver/BiDi semantics or capability handling, compare with at least one other binding (e.g., `java/` or `py/`) to maintain behavior parity.
- Tests: run via Bazel with pinned browsers, e.g. `bazel test //javascript/selenium-webdriver:small-tests` (unit) or `bazel test //javascript/selenium-webdriver:all --pin_browsers --test_output=streamed`; for a specific browser target use the existing `*-chrome`/`*-firefox` labels.
- Add tests under `javascript/selenium-webdriver/test/`; small (no-browser) tests live in `test/lib/`, browser tests in `test/`—avoid creating new broad globs or altering test harness utilities unless necessary.
- Do not hand-edit lockfiles (`pnpm-lock.yaml`, etc.); use the documented tooling for dependency changes.
