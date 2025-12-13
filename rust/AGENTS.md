<!-- Guidance for AI agents working in Selenium Manager code -->

## Code location
- `rust/src` and `rust/tests`

## Tests
- `bazel test //rust/...`
- recommended flags: `--test_env=RUST_BACKTRACE=full` `--test_env=RUST_TEST_NOCAPTURE=1`

## Dependency management
Keep `Cargo.Bazel.lock` synchronized with `Cargo.lock`:
- `CARGO_BAZEL_REPIN=true bazel sync --only=crates`
