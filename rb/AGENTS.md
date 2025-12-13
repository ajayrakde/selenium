<!-- Guidance for AI agents working in Selenium Ruby Bindings -->

## Primary code location:
- Library: `rb/lib/selenium/webdriver`
- Tests: `rb/spec/unit/selenium/webdriver` and `rb/spec/integration/selenium/webdriver` 
- Bazel will build and use the version of Ruby specified in `rb/.ruby-version`

## Useful dev commands
- Bundle install `bazel build @bundle//:bundle`

## Testing conventions
See `rb/TESTING.md`.


Test targets:

| Command                                                                          | Description                                       |
|----------------------------------------------------------------------------------|---------------------------------------------------|
| `bazel test //rb/spec/...`                                                       | Run all tests                                     |
| `bazel test //rb/spec/unit/...`                                                  | Run unit tests                                    |
| `bazel test //rb/spec/integration/...`                                           | Run integration tests for all browsers            |
| `bazel test //rb/spec/integration/... --test_tag_filters firefox`                | Run integration tests for only Firefox            |
| `bazel test //rb/spec/integration/... --test_tag_filters firefox-remote`         | Run integration tests for only Firefox using Grid |
| `bazel test //rb/spec/integration/... --test_env=DEBUG=true`                     | Run integration tests with DEBUG enabled          |
|                                                                                  |                                                   |

Ruby test targets have the same name as the spec file with `_spec.rb` removed, so you can run them individually.
Integration tests targets also have a browser and remote suffix to control which browser to pick and whether to use Grid.

| Test file                                               | Test target                                                      |
| ------------------------------------------------------- | ---------------------------------------------------------------- |
| `rb/spec/unit/selenium/webdriver/proxy_spec.rb`         | `//rb/spec/unit/selenium/webdriver:proxy`                        |
| `rb/spec/integration/selenium/webdriver/driver_spec.rb` | `//rb/spec/integration/selenium/webdriver:driver-chrome`         |
| `rb/spec/integration/selenium/webdriver/driver_spec.rb` | `//rb/spec/integration/selenium/webdriver:driver-chrome-remote`  |
| `rb/spec/integration/selenium/webdriver/driver_spec.rb` | `//rb/spec/integration/selenium/webdriver:driver-firefox`        |
| `rb/spec/integration/selenium/webdriver/driver_spec.rb` | `//rb/spec/integration/selenium/webdriver:driver-firefox-remote` |

Supported browsers:

* `chrome`
* `chrome-beta`
* `edge`
* `firefox`
* `firefox-beta`
* `ie`
* `safari`
* `safari-preview`

* `--test_arg "-eTimeouts"` - test only specs which name include "Timeouts"
* `--test_arg "<any other RSpec argument>"` - pass any extra RSpec arguments

