---
created: 2026-07-25
updated: 2026-07-25
---

## General

- Prefer explicit imports; avoid wildcard (`*`) imports except for a short list of DSL-style packages (see below), or when the explicit imports from a single package would exceed the star-import threshold (50).
- Packages preferred for wildcard imports (including subpackages):
  - `com.intellij.ui.dsl` — IntelliJ UI DSL
  - `icu.windea.pls.lang.resolve.complexExpression.dsl` — complex expression DSL
  - `icu.windea.pls.lang.resolve.complexExpression.nodes` — complex expression nodes
  - (to be extended as more DSL-style packages emerge)

## Tips

- Formatting/ordering issues matter far less than logic and structure. It's fine for agent-authored code to leave behind unused or slightly unordered imports — these are trivial for a human (or an IDE "Optimize Imports" pass) to clean up afterward, so don't block on perfecting imports.
- When cleaning up imports intentionally (e.g. via IDE tooling), keep the wildcard-import allowlist above in mind so the pass doesn't unnecessarily expand already-preferred star imports.