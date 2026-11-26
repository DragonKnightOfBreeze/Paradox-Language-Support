---
name: grammar-kit-codegen
description: Regenerate JFlex lexers and Grammar-Kit BNF parsers into src/main/gen, run parsing snapshot tests, and keep generated code clean. Use when modifying .flex or .bnf grammar files or troubleshooting generated lexer/parser/PSI sources.
---

# Grammar-Kit and JFlex Codegen Workflow

This project hosts four custom languages (Paradox Script, Paradox Localisation, Paradox CSV, and CWT) with multiple JFlex lexers and Grammar-Kit BNF parsers generated into `src/main/gen/`.

## 1. Grammar Sources and Registered Tasks

Grammar tasks are registered in `build.gradle.kts`:

- **Aggregate tasks**:
  - `./gradlew generateGrammars --no-daemon --console=plain` (all lexers and parsers)
  - `./gradlew generateLexers --no-daemon --console=plain` (all lexers)
  - `./gradlew generateParsers --no-daemon --console=plain` (all parsers)
- **Named lexer tasks** (`GenerateLexerTask`):
  - `generateParadoxScriptLexer` (`src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex`)
  - `generateParadoxScriptInlineMathLexer` (`src/main/kotlin/icu/windea/pls/script/ParadoxScript.InlineMath.flex`)
  - `generateParadoxLocalisationLexer` (`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex`)
  - `generateParadoxLocalisationTextLexer` (`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex`)
  - `generateParadoxCsvLexer` (`src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.flex`)
  - `generateCwtLexer` (`src/main/kotlin/icu/windea/pls/cwt/Cwt.flex`)
- **Named parser tasks** (`GenerateParserTask`):
  - `generateParadoxScriptParser` (`src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf`)
  - `generateParadoxLocalisationParser` (`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf`)
  - `generateParadoxCsvParser` (`src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf`)
  - `generateCwtParser` (`src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf`)

## 2. Guardrails

- **Never** invoke the unconfigured default Gradle tasks `generateLexer` or `generateParser` directly, as they are not bound to a single `sourceFile` and fail.
- Always run Gradle tasks via the built-in shell with `--no-daemon --console=plain`.
- **Standalone JFlex fallback**: when testing a single `.flex` file outside Gradle, run:

```bash
java -jar jflex-1.10.17.jar --skel idea-flex.skeleton -d src/main/gen/<package_dir> <flex_file_path>
```

## 3. Post-Generation Cleanup and Verification

1. **Check for stale generated files**: `GenerateParserTask` configures `purgeOldFiles.set(false)` in `build.gradle.kts`. If you rename or delete BNF rules or PSI types, inspect `src/main/gen/` with `git status` and remove orphaned `.java` files manually.
2. **Isolate unrelated churn**: inspect `git diff src/main/gen/` and revert accidental regeneration noise in untouched languages before committing. Keep codegen updates in a clean, identifiable commit or clearly separated from unrelated refactors.
3. **Run targeted parsing tests** (and update/review PSI tree snapshots under `src/test/testData/` when grammar output intentionally changes):

```bash
./gradlew test --tests "icu.windea.pls.script.ParadoxScriptParsingTest" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.localisation.ParadoxLocalisationParsingTest" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.csv.ParadoxCsvParsingTest" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.cwt.CwtParsingTest" --no-daemon --console=plain
```
