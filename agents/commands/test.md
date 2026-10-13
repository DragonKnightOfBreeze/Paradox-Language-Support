---
description: Run tests
agent: build
---

Run the full test suite and show any failures.
Focus on the failing tests and suggest fixes.

Run it through the agent's built-in shell with `--no-daemon --console=plain`
(e.g. `./gradlew test --no-daemon --console=plain`) to avoid the shell hanging
after the task completes; read results from `build/test-results` / `build/reports`.