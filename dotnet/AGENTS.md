<!-- Guidance for AI agents working in Selenium .NET Bindings -->

## Where code lives
- Core: `dotnet/src/webdriver/`
- Support: `dotnet/src/support/`
- Tests: `dotnet/test/common/`

## Build / Dependencies
- Build: `bazel build //dotnet/...`
- Dependency Management: 

## Testing
See `dotnet/TESTING.md`

## Tests (Bazel)
.NET tests require pinned browsers:
- All: `bazel test //dotnet/test/common:AllTests --pin_browsers=true`
- Single class: `bazel test //dotnet/test/common:ElementFindingTest --pin_browsers=true`
- Multi-browser module example: `bazel test //dotnet/test/common:ElementFindingTest-edge --pin_browsers=true`  [oai_citation:19‡GitHub](https://github.com/SeleniumHQ/selenium)

