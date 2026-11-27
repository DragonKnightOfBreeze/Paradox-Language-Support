---
name: issue-triage
description: Triage GitHub issues, classify compatibility vs regression labels, diagnose PSI/CWT/platform root causes, add Issue<N>Test regression suites when warranted, and archive non-trivial maintainer case notes. Use when investigating, reproducing, fixing, or documenting a GitHub issue.
---

# Paradox Chronicle Issue Triage Workflow

Follow this workflow when investigating, reproducing, fixing, and documenting GitHub issues.

## 1. Canonical Language, Triage, and Label Semantics

- **Canonical language**: write public issues, PRs, commits, KDoc in regression tests, and public replies in standard technical **English** by default. Switch to Chinese only when mirroring a Chinese-speaking reporter or discussing Chinese-specific features (avoid mixing both languages arbitrarily in one post).
- **Initial triage**: distinguish actionable plugin bugs from upstream game-engine bugs/limitations, user misconfigurations, or duplicate/already-resolved reports before jumping into code changes.
- **Label classification**:
  - `regression`: self-inflicted breakage of previously working plugin behavior caused by recent code changes.
  - `game-compatibility`: breakage or drift triggered by an upstream Paradox game or engine update (e.g., Stellaris 4.6 removing BOM in `common/name_lists`, new Vic3/EU5 syntax).
  - `ide-compatibility`: breakage or deprecation triggered by an IntelliJ Platform, EAP, or Kotlin compiler/runtime upgrade.

## 2. Diagnosis and Reproduction

- Re-read the full GitHub issue page at the start of the task (e.g., labels, body images or attachments, comments, etc., via `gh issue view <N> --comments`) so no extra context is missed.
- Use **intellij** and **intellij-index** MCP tools first to trace PSI elements, CWT configs, references, inspections, and existing regression tests.
- For game-compatibility issues, verify the actual upstream game syntax or file encoding/structure before designing a fix.
- For complex or hard-to-reproduce issues (e.g., indexing races, SOFs, cache inconsistencies, freezes), inspect related entries in `documents/notes/issues.md` and `documents/notes/issues/` for prior patterns.

## 3. Regression Tests and Fixtures (When Warranted)

Add dedicated regression coverage when an issue warrants deterministic reproduction (non-trivial bugs, parser/config regressions, or compatibility edge cases), following `documents/notes/guidelines/testing.md`:

- **Test class**: place under `src/test/kotlin/icu/windea/pls/test/issues/Issue<N>Test.kt` using JUnit4 (`BasePlatformTestCase` + `ChronicleTestScope`).
  - Document `See: [#<N>](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/<N>)`, problem summary, `Cause:`, `Solution:`, and `@see` target classes in KDoc.
- **Test data**: place fixtures under `src/test/testData/issues/<N>/` (or `<N>_<shortTopic>/`) with a short `README.md` linking the issue and `Issue<N>Test`.
  - Keep local test configs inside subdirectories of `.config/` (e.g., `.config/core/`, `.config/stellaris/`), never directly inside `.config/`.
  - When copying binary fixtures (e.g., `.dds`, `.tga`, `.png`), chain `myFixture.copyFileToProject(...).injectFileInfo(...)` directly rather than relying on a pending `markFileInfo(...)`.
- **Verification**:

```bash
./gradlew test --tests "icu.windea.pls.test.issues.Issue<N>Test" --no-daemon --console=plain
```

## 4. Maintainer Archive and Closure

- **Issue case note** (selective—only for non-trivial diagnostics, subtle parser/PSI/index bugs, or reusable lessons): add a Chinese case note in `documents/notes/issues/` and link it in `documents/notes/issues.md` (and update relevant ADRs/guidelines if rules changed). Follow the Markdown formatting rules in `AGENTS.md` and `documents/notes/guidelines.md` (do not wrap lines mid-sentence to fit a line length, and use consistent ending punctuation across multi-line list items).
- **Commits and PRs**: keep commits focused (e.g., fix, regression test, docs/changelog), and reference `(#<N>)` in commit summaries and PR descriptions.
- **Issue replies**: if posting a follow-up or closure reply on GitHub, keep it concise, pragmatic, and strictly technical, linking deterministic repository artifacts (e.g., commits, PRs, `Issue<N>Test.kt`, or maintainer notes) when present without boilerplate.
