---
name: grammar-kit-codegen
description: Regenerate JFlex lexers and Grammar-Kit BNF parsers into src/main/gen, clean stale generated files, and verify compilation and parsing snapshots. Use when modifying .flex or .bnf grammar files or troubleshooting generated lexer/parser/PSI sources.
---

# Grammar-Kit and JFlex Codegen Workflow

This project hosts four custom languages (Paradox Script, Paradox Localisation, Paradox CSV, and CWT) with multiple JFlex lexers and Grammar-Kit BNF parsers generated into `src/main/gen/`.

## 1. Prefer Targeted Grammar Tasks

Prefer running only the named Gradle task(s) for the `.flex` or `.bnf` file(s) you modified, rather than regenerating untouched languages:

- **Named lexer tasks** (`GenerateLexerTask` in `build.gradle.kts`):
  - `generateParadoxScriptLexer` (`src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex`)
  - `generateParadoxScriptInlineMathLexer` (`src/main/kotlin/icu/windea/pls/script/ParadoxScript.InlineMath.flex`)
  - `generateParadoxLocalisationLexer` (`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex`)
  - `generateParadoxLocalisationTextLexer` (`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex`)
  - `generateParadoxCsvLexer` (`src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.flex`)
  - `generateCwtLexer` (`src/main/kotlin/icu/windea/pls/cwt/Cwt.flex`)
- **Named parser tasks** (`GenerateParserTask` in `build.gradle.kts`):
  - `generateParadoxScriptParser` (`src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf`)
  - `generateParadoxLocalisationParser` (`src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf`)
  - `generateParadoxCsvParser` (`src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf`)
  - `generateCwtParser` (`src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf`)
- **Aggregate tasks** (use only when intentionally regenerating all grammars or a whole category):
  - `./gradlew generateLexers --no-daemon --console=plain`
  - `./gradlew generateParsers --no-daemon --console=plain`
  - `./gradlew generateGrammars --no-daemon --console=plain`

Example targeted run:

```bash
./gradlew generateParadoxLocalisationLexer generateParadoxLocalisationParser --no-daemon --console=plain
```

## 2. Guardrails

- **Never** invoke the unconfigured default Gradle tasks `generateLexer` or `generateParser` directly, as they are not bound to a single `sourceFile` and fail.
- Always run Gradle tasks via the built-in shell with `--no-daemon --console=plain`.
- **Standalone JFlex fallback**: when testing a single `.flex` file outside Gradle, run:

```bash
java -jar jflex-1.10.17.jar --skel idea-flex.skeleton -d src/main/gen/<package_dir> <flex_file_path>
```

## 3. Post-Generation Cleanup and Verification

1. **Check for stale generated files**: both `GenerateLexerTask` and `GenerateParserTask` configure `purgeOldFiles.set(false)` in `build.gradle.kts`. If you rename or delete BNF rules, PSI types, or lexers, inspect `src/main/gen/` with `git status` and remove orphaned `.java` files manually.
2. **Isolate unrelated churn**: inspect `git diff src/main/gen/` and revert accidental regeneration noise in untouched languages before committing. Keep codegen updates in a clean, identifiable commit or clearly separated from unrelated refactors.
3. **Verify compilation, parsing snapshots, and related tests**:
   - Check compilation when PSI signatures or `ElementTypes` changed (`./gradlew compileKotlin compileTestKotlin --no-daemon --console=plain`).
   - Run the affected `*ParsingTest` (snapshots live under `src/test/testData/<lang>/syntax/`) plus any related expression, syntax, or issue regression tests (e.g., `Issue*Test`):

```bash
./gradlew test --tests "icu.windea.pls.script.ParadoxScriptParsingTest" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.localisation.ParadoxLocalisationParsingTest" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.csv.ParadoxCsvParsingTest" --no-daemon --console=plain
./gradlew test --tests "icu.windea.pls.cwt.CwtParsingTest" --no-daemon --console=plain
```
