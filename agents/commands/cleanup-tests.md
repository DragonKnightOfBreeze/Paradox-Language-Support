---
description: Cleanup existing tests
agent: build
model: deepseek/deepseek-v4-pro
---

For all existing tests in this project:
- If a test class has clear test target(s) but don't document them in doc comments, add `@see TestTarget` KDoc.
- If a test class is placed in an incorrect location relative to its test target(s), move it to the correct location.
- Complete this task flexibly and efficiently.