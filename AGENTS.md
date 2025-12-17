<!--
Guidance for AI agents working in the Selenium monorepo.
Language-specific details live in respective subdirectories.
-->

Selenium is a Bazel-built monorepo implementing the W3C WebDriver (and related) protocols,
shipping multiple language bindings plus Grid and Selenium Manager.
The repository README is aimed at contributors; end-user docs live elsewhere.

If the user is asking a question (no code change requested), answer directly—no plans/checklists.
If the user requests a patch, follow the guidance below.

## Execution model (important)
In many AI-agent environments, Bazel cannot be executed (restricted network/toolchain/browser access).
Agents MUST:
- Never claim commands/tests ran unless the user provides output.
- Provide copy/paste-ready commands for the user to run in an admin terminal.
- Ask for the exact output needed (errors, failing targets, stack traces), then iterate.

### Terminal run request format:
Goal: <why this is being run>
Run: <command>

## Invariants (don’t violate unless explicitly asked)
- Treat `bazel-*` as generated output.
- Treat `third_party/` as read-only.
- Preserve Apache 2.0 headers and NOTICE/LICENSE content.
- Avoid repo-wide refactors/formatting; prefer small, reversible diffs.

## Repo layout
Bindings:
- Java: `java/` (see `java/AGENTS.md`)
- Python: `py/` (see `py/AGENTS.md`)
- Ruby: `rb/` (see `rb/AGENTS.md`)
- JavaScript: `javascript/selenium-webdriver/` (see `javascript/selenium-webdriver/AGENTS.md`)
- .NET: `dotnet/` (see `dotnet/AGENTS.md`)
Other components:
- `rust/` (Selenium Manager, see `rust/AGENTS.md`)
- `common/` (shared build/test wiring; changes can affect multiple areas)
- `common/src/` (test HTML fixtures; changes can break tests)
- `javascript/atoms/` (shared JS atoms; very high blast radius)
- `scripts/`, `rake_tasks/`, `.github/`, `Rakefile` (tooling/build; high risk)

## Toolchain
- Expect Bazelisk + JDK 17+ (JAVA_HOME should point to a JDK)
- Prefer `./go <task>` if applicable (Rake tasks execute bazel & scripts, used by CI)
- Use targeted Bazel commands. Use `bazel query ...` to locate labels before build/test

## Cross-binding consistency checks
When changing user-visible behavior, compare with at least one other binding:
- `rg <term> java/ py/ rb/ dotnet/ javascript/selenium-webdriver/`

If behavior is shared/low-level (protocol, serialization, “remote”/transport), expect follow-up parity work or file an issue explicitly.

## Testing
Use each binding’s testing guide (see `<dir>/TESTING.md`).
Consider these flags when testing locally:
- `--test_size_filters=small` (unit tests only)
- `--test_output=all|streamed` (displays console output to debug)
- `--cache_test_results=no` (force re-running all tests)
- `--test_env FOO=bar` (if tests need environment variables)

## Dependencies & lockfiles
- Don’t hand-edit lockfiles (`pnpm-lock.yaml`, `multitool.lock.json`, `Cargo.Bazel.lock`, etc).
- Use the binding’s documented update/repin flow (see `<dir>/AGENTS.md`).

## High risk changes (request verification before modifying unless explicitly instructed)
- Everything referenced above as high risk
- WebDriver/BiDi semantics, capability parsing, wire-level behavior
- Dependency updates / `MODULE.bazel` / repin flows
- Grid routing/distributor/queue logic

## After making code changes
- Call out any high risk areas touched
- List exact commands the user ran (or needs to run) + outcomes
- Note cross-binding impact and any follow-up issues needed
- Recommend formatting/lint if applicable (e.g., `./scripts/format.sh`)
- 
