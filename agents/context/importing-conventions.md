---
created: 2026-07-25
updated: 2026-11-03
---

## General

- Prefer explicit imports for most packages.
- Use wildcard imports (`*`) only for few special packages. For example, DSL-style packages (`com.intellij.ui.dsl`, etc.).
- If a package has more than the limit of explicit imports (50), switch to using a star import.

## Tips

- Formatting/ordering issues matter far less than logic and structure. It's fine for agent-authored code to leave behind unused or slightly unordered imports - these are trivial for a human (or an IDE "Optimize Imports" pass) to clean up afterward, so don't block on perfecting imports.
- When cleaning up imports intentionally (e.g. via IDE tooling), keep the wildcard-import allowlist above in mind so the pass doesn't unnecessarily expand already-preferred star imports.

## Examples

Packages preferred for wildcard imports (including subpackages):

- `com.intellij.ui.dsl` - IntelliJ UI DSL
- `icu.windea.pls.pls.test.dsl` - test DSL
- `icu.windea.pls.lang.resolve.complexExpression.dsl` - complex expression DSL
- `icu.windea.pls.lang.resolve.complexExpression.nodes` - complex expression nodes
- (to be completed)