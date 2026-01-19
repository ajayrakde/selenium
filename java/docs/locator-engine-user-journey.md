# Locator Engine User Journey

This document describes the primary user journey for adopting the Locator Engine in Selenium Java.

## Journey: Authoring a Locator-Based Test

1. **Create a session context**
   - The test obtains an `AutomationContext` from its framework or fixture.
2. **Create locators through the factory**
   - Use tiered selectors (`byTestId` is the default, with `data-testid`).
3. **Compose and scope**
   - Use `within`, `filter`, and `nth` to disambiguate.
4. **Resolve via actions/queries**
   - Read state with `exists`, `count`, or `text`.
   - Perform actions (`click`, `fill`, `clearAndFill`, `hover`, `check`, `uncheck`, `pressKey`,
     `selectOption`) which run actionability gates by default.
5. **Handle diagnostics on failure**
   - Exceptions include plan and match previews for fast debugging.

## Example Flow

```java
Locator email = context.locator().byTestId("email").withName("Email input");
Locator submit = context.locator().byTestId("submit").withName("Submit button");

if (email.exists(Duration.ofSeconds(2))) {
  email.text(Duration.ofSeconds(2));
}

submit.all().count(Duration.ofSeconds(2));
submit.click(Duration.ofSeconds(2));

Locator rememberMe = context.locator().byTestId("remember").withName("Remember checkbox");
rememberMe.check();
```

## Impact Check

- No legacy Selenium usage is impacted; locator usage is additive.
- Action pipeline behavior is opt-in via locator actions and does not affect raw WebDriver calls.
- User journey remains valid as long as locators remain factory-created and strictness defaults are preserved.
- Stability signal sampling is non-blocking and only enriches diagnostics when used, so the existing journey does not change.
