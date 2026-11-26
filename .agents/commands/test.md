---
description: Run targeted or scoped tests
agent: build
---

Run targeted tests matching the current task or explicit scope (e.g., `./gradlew test --tests "<fully.qualified.TestClass>" --no-daemon --console=plain` or `--tests "*Keyword*"`), and report any failures with suggested fixes.

- Avoid running the full `./gradlew test` suite casually during iterative development unless explicitly requested, as it takes tens of minutes.
- Always run Gradle via the agent's built-in shell with `--no-daemon --console=plain` and a bounded timeout to avoid daemon stdout/stderr hangs.
- Confirm results from `build/test-results/**/*.xml` and `build/reports/**` rather than relying on a shell timeout alone.
- See `documents/notes/guidelines/testing.md` for `ChronicleTestScope`, test DSLs, and optional `ChronicleAssume` flags.
