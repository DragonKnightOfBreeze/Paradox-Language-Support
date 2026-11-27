# Guidance to the Paradox Chronicle

## Overview

This repository contains **Paradox Chronicle** (formerly **Paradox Language Support**), the IntelliJ IDEA plugin designed for mod developers of Paradox Interactive games.

At the narrative level, this plugin symbolizes the book also titled **Paradox Chronicle**. While playing on the double meaning of **Chronicle** and **Prophecy Book**, it is, indeed, also the guide book, to the paradox universe.

Given the large codebase (over 120k lines of Kotlin production code) and high complexity (platform & domain & architecture), make this project a challenging undertaking.

As you embark on this journey, keep serious, cautious, and curious.

## Project quick orientation

### What the plugin supports

Following custom languages are supported by the plugin:
- **Paradox Script** (`PARADOX_SCRIPT`, mainly `*.txt`) - used for providing game data and writing game logic.
- **Paradox Localisation** (`PARADOX_LOCALISATION`, `*.yml`) - used for providing i18n text.
- **Paradox CSV** (`PARADOX_CSV`, `*.csv`) - used for describing table data.
- **CWT** (`CWT`, `*.cwt`) - used for writing CWT config files which drive semantics (completion, inspections, navigation, docs, etc.).

In addition to language features, the plugin also includes:
- **Image support** (e.g., DDS, TGA) with optional tool-based rendering and conversion.
- **Tool integrations** (e.g., texconv, ImageMagick, Tiger lint, Translation plugin).
- **AI integration** (LangChain4j-based) focused on localisation translation/polishing workflows.
- An internal **extension point (EP)** architecture.
- An internal **code injection** subsystem.

### Key points

- The plugin is written in Kotlin and PSI-based (not LSP-based).
- Many language features are powered by the **config system** based on **CWT config files**.
- The relationship between Paradox script files and CWT config files is roughly like **JSON vs JSON Schema**.

### Project structure (high level)

- `README.md` and `README_zh.md`: overview and usage for this project.
- `CONTRIBUTING.md`: guidance for contributors (external PR & community perspective).
- `AGENTS.md`: guidance for agents.
- `.agents/`: agent workflow files (e.g., commands, skills).
- `cwt/`: CWT config directories (core and per-game repositories, see `cwt/README.md`).
- `docs/`: end-user reference documentation (see `docs/README.md` and `docs/README_zh.md`).
- `documents/`: maintainer documentation (see `documents/README.md` and `documents/README_zh.md`).
- `src/main/kotlin/`, `src/main/java/`, `src/main/resources/`: plugin source.
- `src/test/kotlin/`, `src/test/java/`, `src/test/resources/`, `src/test/testData/`: tests and test data.
- `src/jmh/kotlin/`: JMH microbenchmarks.
- `src/main/resources/META-INF/plugin.xml`: plugin entry (registrations are split into `META-INF/chronicle-*.xml`).

## Setup and build commands

This project uses **Gradle** and the **IntelliJ Platform Gradle Plugin** (**JDK 25** + Gradle wrapper).

### Common commands

- Build the plugin ZIP: `./gradlew buildPlugin --no-daemon --console=plain`
- Run targeted tests: `./gradlew test --tests "<fully.qualified.TestClass>" --no-daemon --console=plain`
- Run tests by keyword: `./gradlew test --tests "*SomeKeyword*" --no-daemon --console=plain`
- Enable optional assume-gated tests (e.g., `-Dchronicle.test.include.local.env=true`, also supports `ai`, `remote`, `config.generator`, `external.tools`, `all`)
- Run JMH microbenchmarks: `./gradlew benchmark --no-daemon --console=plain` (optional: `--benchmarks=<pattern>`)
- Generate JMH benchmark reports: `./gradlew benchmarkReport --no-daemon --console=plain`
- Avoid running the full `./gradlew test` suite casually during iterative development, as it takes tens of minutes.

### Generating lexer and parser (Grammar-Kit & JFlex)

- Use the registered Gradle tasks in `build.gradle.kts` directly from the CLI:
  - `./gradlew generateGrammars --no-daemon --console=plain` - generate all lexers and parsers.
  - `./gradlew generateLexers --no-daemon --console=plain` - generate all lexers only.
  - `./gradlew generateParsers --no-daemon --console=plain` - generate all parsers only.
- Do not invoke the unconfigured default `generateLexer` / `generateParser` tasks directly (they fail without a single `sourceFile`).
- Alternative standalone JFlex CLI: `java -jar jflex-1.10.17.jar --skel idea-flex.skeleton -d src/main/gen/<package_dir> <flex_file_path>`.

### Gradle execution environment

- **Best practice**: run Gradle tasks directly through the **agent's built-in shell**, appending `--no-daemon --console=plain` and a bounded timeout.
- **Always pass `--no-daemon`.** Default daemon mode lets the long-lived daemon inherit stdout/stderr, making the shell appear to hang after the build already finished.
- Confirm results via `build/test-results/**/*.xml` and `build/reports/**` rather than treating a shell timeout alone as failure.
- When diagnosing a suspected hang, inspect existing Gradle and Java processes first, and clean up only daemons spawned during diagnosis.
- Never terminate unrelated processes or leave long-lived child processes holding stdout/stderr.

### CWT config repositories

- Local config repos live under `cwt/<repoDir>`, with helper scripts in `cwt/scripts/`.
- The plugin bundles configs under `config/<gameTypeId>`. When absent (e.g., in CI), Gradle downloads archives into `build/generated/cwt/<repoDir>`.

## Architecture, conventions, and testing

### Guidelines and focused guides

Detailed conventions (e.g., module dependencies, `Service`/`Manager`/`Util` layering, naming, imports, KDoc, caching, indexing, terminology, testing, and benchmarking) are maintained in **`documents/notes/guidelines.md`** and its focused guides:

- **Core architecture and coding conventions**: `documents/notes/guidelines.md`
- **Testing guide**: `documents/notes/guidelines/testing.md`
- **Benchmarking guide**: `documents/notes/guidelines/benchmarking.md`

### Testing quick pointers

- Tests use JUnit4 and the IntelliJ Platform test framework (e.g., `BasePlatformTestCase`, `ParsingTestCase`).
- Config-driven tests mix in `ChronicleTestScope` via interface composition (there is no shared base test class).
- Test data lives under `src/test/testData/`. Local test configs inside `.config/` must sit in subdirectories (e.g., `core/`, `stellaris/`), never directly under `.config/`.
- Read `documents/notes/guidelines/testing.md` before writing or debugging non-trivial tests for `ChronicleTestScope` helpers, test DSLs (`icu.windea.pls.test.dsl`), snapshot tests, `ChronicleAssume`, and binary fixture gotchas.

### Common code patterns (quick pointers)

- **Coroutine scope**: `ChronicleFacade.getCoroutineScope(...)`.
- **Config group**: `ChronicleFacade.getConfigGroup(...)`.
- **Settings, options, and capabilities**: follow `...Settings` (platform service), `...Options` (immutable data class), and `ChronicleCapabilities`.
- **Data formats**: handle structured data formats (e.g., JSON, Markdown) through `JsonService`, `MarkdownService`, etc., rather than bypassing them.
- **Config context and matched configs**: `ParadoxConfigManager.getConfigContext(element)` and `ParadoxConfigManager.getConfigs(element, options)`.
- **Query and select APIs**: e.g., `icu.windea.pls.config.select`, `icu.windea.pls.lang.select`, `ParadoxDefinitionSearch` (and sibling `Paradox...Search` classes).
- **Reuse before reinventing**: search existing extensions (e.g., in `icu.windea.pls.core`), components (e.g., `AntMatcher`, `QuotePattern`, `ProcessExecutor`), utilities (e.g., `Caches`, `RecursionGuard` in `icu.windea.pls.core.util`), services, managers, and EPs.

### Language & config reference anchors

- Syntax reference and examples: `docs/en/ref-syntax.md`, `src/test/testData/{cwt,script,localisation,csv}/example.test.*`, etc.
- Config system reference and examples: `docs/en/config.md`, `docs/en/ref-config-format.md`, `cwt/` (e.g., `cwt/cwtools-stellaris-config/`), `src/test/testData/`.

## Maintainer documentation (IMPORTANT)

### Lifecycle model

- Internal maintainer documents under `documents/` are written primarily in Simplified Chinese with English kebab-case paths.
- Documents are organized by **lifecycle and authority** rather than authorship:
  - **Living specifications**: `documents/design/` and living handbooks/trackers under `documents/notes/` (e.g., `guidelines*`, `roadmap*`, `proposals*`, `ideas.md`, `cheatsheet.md`).
  - **Immutable archives**: sealed historical slices under `documents/notes/` (e.g., `adr/`, `issues/`, `solutions/`, `analysis/`, `evaluations/`, `reports/`). Do not rewrite sealed archives merely to chase routine code renames.

### Routing (on-demand reading)

Do not read `documents/` wholesale. Read specific documents on demand:
- **Coding & architecture guidelines**: `documents/notes/guidelines.md`
- **Testing & JMH benchmarking**: `documents/notes/guidelines/testing.md`, `documents/notes/guidelines/benchmarking.md`
- **Roadmap & per-version plans**: `documents/notes/roadmap.md` (`documents/notes/roadmap/`)
- **Unsettled proposals & cross-version blueprints**: `documents/notes/proposals.md` (`documents/notes/proposals/`)
- **Ideas & brainstorming backlog**: `documents/notes/ideas.md`
- **Accepted architectural decisions (ADRs)**: `documents/notes/adr.md` (`documents/notes/adr/`)
- **Bug diagnostics & post-reports**: `documents/notes/issues.md` (`documents/notes/issues/`)
- **Environment & build workarounds**: `documents/notes/solutions.md` (`documents/notes/solutions/`)
- **Profiling, thread dumps & domain analyses**: `documents/notes/analysis.md` (`documents/notes/analysis/`)
- **Evaluations & curated reports**: `documents/notes/evaluations.md`, `documents/notes/reports.md`
- **System design specs**: `documents/README.md` (`documents/design/`)

## Agent instructions (IMPORTANT)

### Communication & Markdown

- Communicate in Chinese or English matching the conversation (the maintainer usually uses Chinese).
- Write maintainer documents in Chinese unless context dictates English.
- Follow the KDoc and comment language rules in `documents/notes/guidelines.md`.
- Prefer `-` for unordered lists and `**bold**` for emphasis.
- Do not wrap Markdown paragraphs mid-sentence to control line length.
- Break lines in Markdown prose only after `:`, `;`, or `.` (or `：`, `；`, `。` in Chinese) when needed.
- Use consistent ending punctuation across all items in a multi-line list (e.g., end every item with a period, or omit it on all items).
- When triaging or fixing a GitHub issue, re-read the full issue page at the start of the task (e.g., labels, body images, comments, etc.).
- Prefer starting from `###` (H3) in conversational responses (does not constrain documents).

### IntelliJ plugin specifics

- Many registrations live under `src/main/resources/META-INF/chronicle-*.xml` included by `plugin.xml`.
- Optional dependencies (enabled only when present): e.g., JNA, JSON, Markdown, Diagrams (Ultimate), Translation plugin.
- Avoid changing the internal code injection subsystem (`icu.windea.pls.inject`) unless you fully understand the impact.

### Be smartly and lovely

- **Keep conceptual consistency** across code, comments, documents, and design discussions.
- **Follow best practices** for Kotlin and IntelliJ Platform development.
- Keep changes minimal and localized.
- Add or update tests when feasible, and run inspections or targeted tests before finishing.

## Tooling preferences (IMPORTANT)

### General operations

- Prefer built-in file tools for plain file operations (e.g., read, write, edit, patch).
- Prefer the built-in shell with `--no-daemon --console=plain` for Gradle tasks.

### Search and navigation

- **Always** prefer **intellij** and **intellij-index** MCP tools for structured search, semantic search, and code navigation over plain text search.
- Use MCP for code exploration and verification before building or testing (e.g., declarations, usages, implementations, super methods, type hierarchies, library sources, and code inspections).
- Fall back to text search only for trivial, unambiguous targets (e.g., exact file names or plain literals).
- State explicitly when MCP is unavailable or insufficient before resorting to text-based search.

### Forbidden shortcuts

- **Never** locate or decompile library or platform JARs via shell commands.
- Inspect library sources and decompiled code through MCP only. If that is truly impossible, stop and report the limitation instead of improvising with shell-based decompilation.

### MCP server priority

- **intellij** MCP: prefer for IDE-driven and IDE-related actions.
- **intellij-index** MCP: prefer for code navigation, refactoring, and inspections.
