---
description: Cleanup existing tests
agent: build
---

For existing tests in the target scope:
- If a test class has clear test target(s) without documentation, add `@see <TestTarget>` to the class KDoc.
- If a test class is placed in an incorrect package or directory relative to its test target(s), move it to the matching location.
- Follow the conventions in `documents/notes/guidelines/testing.md` and `documents/notes/guidelines.md`.
