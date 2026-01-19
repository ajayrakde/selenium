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
```

## Impact Check

- No legacy Selenium usage is impacted; locator usage is additive.
- User journey remains valid as long as locators remain factory-created and strictness defaults are preserved.
