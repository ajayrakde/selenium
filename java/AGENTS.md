<!-- Guidance for AI agents working in Selenium Java Bindings and Grid. -->

## Where code lives
- Java Bindings: `java/src/`, `java/test/`
- Grid Server: `java/src/org/openqa/selenium/grid/`, `java/test/org/openqa/selenium/grid`

## Build / install / update
- Build: `bazel build //java/...`
- Dependencies live in `MODULE.bazel`; after edits:
  - `RULES_JVM_EXTERNAL_REPIN=1 bazel run @maven//:pin`

## Test Examples
- Unit: `bazel test //java/... --test_size_filters=small`
- Browser: `bazel test //java/... --test_tag_filters=<browser>`
- Single test example: `bazel test //java/test/org/openqa/selenium/chrome:ChromeDriverFunctionalTest`

## Test conventions
See `java/TESTING.md`.
