# Guidance to the Paradox Chronicle

## Overview

This repository contains **Paradox Language Support**, the IntelliJ IDEA plugin designed for Paradox game mod developers.

In narrative level, the plugin's name is **Paradox Chronicle**.
While playing on the double meaning of **Chronicle** and **Prophecy Book**, this book is, indeed, also the guide book, to the paradox universe.

Given the large codebase (~110k lines of Kotlin production code) and high complexity (platform & domain & architecture), make this project a challenging undertaking.
As you embark on this journey, keep serious, cautious, and curious.

## Project quick orientation

### What the plugin supports

- **Paradox Script** (`PARADOX_SCRIPT`) - used for providing game data and writing game logic.
- **Paradox Localisation** (`PARADOX_LOCALISATION`) - used for providing i18n text.
- **Paradox CSV** (`PARADOX_CSV`) - used for describing table data.
- **CWT** (`CWT`, `*.cwt`) - used for writing CWT config files which drive semantics (completion, inspections, navigation, docs, etc.).

In addition to language features, the plugin also includes:

- **Image support** (DDS/TGA) with optional tool-based rendering/conversion.
- **Tool integrations** (e.g. ImageMagick, Tiger lint, Translation plugin).
- **AI integration** (LangChain4j-based) focused on localisation translation/polishing workflows.
- A substantial internal **extension point (EP)** architecture and a **code injection** subsystem.

### Key points

- The plugin is written in Kotlin and PSI-based (not LSP-based).
- Many language features are powered by the **config system** based on **CWT config files**.
- The relationship between Paradox script files and CWT config files is roughly like **JSON vs JSON Schema**.

### Project structure (high level)

- `agents/`: agent workflow files (commands, context, rules, skills, etc.).
- `cwt/`: CWT config directories (core + per-game repositories).
- `docs/`: reference documentation (including language syntax guidance and config format guidance).
- `documents/`: maintainer documentation (including ai-generated docs and maintainer-written docs).
- `src/main/kotlin`, `src/main/java`, `src/main/resources`: plugin source.
- `src/test/kotlin`, `src/test/java`, `src/test/resources`: test codes and test resources.
- `src/test/testData/`: test data files (e.g., test-specific cwt config files and script files).
- `src/main/resources/META-INF/plugin.xml`: plugin entry (registrations are split into `META-INF/*.xml`).
- `src/main/resources/META-INF/*.xml`: plugin registrations.

## Setup and build commands (Windows)

This project uses **Gradle** and the **IntelliJ Platform Gradle Plugin**.

### Requirements

- **JDK 21** (the build uses `kotlin.jvmToolchain(21)`)
- Gradle wrapper (use `./gradlew` / `./gradlew` on Windows)

### Common commands

> On Windows PowerShell, prefer running Gradle via the wrapper:`./gradlew <task>`

- Run IDE for debugging: `./gradlew runIde`
- Build the plugin ZIP: `./gradlew buildPlugin`
- Run tests: `./gradlew test`

### CWT config repositories

The local config repos used are in the `cwt/<repoDir>` directory, and there are also some useful scripts in the `cwt/scripts` directory.

The plugin bundles CWT configs into the plugin JAR under `config/<gameTypeId>`.
If missing (common in CI), Gradle can download ZIPs and unzip them into `build/generated/cwt/<repoDir>`.

## Testing guidance

### Test taxonomy

- Prefer Kotlin for tests.
- Unit tests: for pure components/tools/extensions; usually no IntelliJ API.
- Integration tests: for PSI/index/query/semantic match and resolve/tool integrations; usually uses the IntelliJ test framework.
- Tooling: JUnit4 + IntelliJ test framework.

### Test data conventions

- Most platform/integration tests use test data under `src/test/testData`.
- Naming convention is typically `*.test.txt` / `*.test.yml` / `*.test.cwt` / `*.test.csv`.
- Case convention is typically `snake_case.test.txt`.
- Some feature tests provide a test-local `.config/` directory under test data to simulate config groups.

### IntelliJ platform test patterns

- **Parsing tests** (syntax/PSI snapshots): use `ParsingTestCase` and compare the parsed tree output.
- **Fixture-based tests**: use `BasePlatformTestCase` + `myFixture.configureByFile(...)`.

### Config-driven integration tests (config groups + context injection)

The plugin is config-driven. Many features (e.g. type inference, scope inference, macros) depend on **CWT config groups** and a simulated “game/mod context”.

Scope extensions exist to make these tests deterministic:

- `initConfigGroups(project, ...)` initializes the required built-in config groups for the specified game types.
- `markIntegrationTest()` and `clearIntegrationTest()` toggles integration-test-only behavior and cleans up injected state.
- `markRootDirectory(...)` allow integration tests to inject root directory path.
- `markConfigDirectory(...)` allow integration tests to inject config directory path.
- `markFileInfo(...)` and `VirtualFile.injectFileInfo(...)` allow integration tests to inject per-file metadata (game type, logical path, etc.) without requiring a real game installation.

Minimal setup example (typical for index/resolve tests):

```kotlin
@Before
fun doSetUp() {
    markIntegrationTest()
    markRootDirectory("features/index")
    markConfigDirectory("features/index/.config")
    initConfigGroups(project, ParadoxGameType.Stellaris)
}

@After
fun doTearDown() = clearIntegrationTest()
```

Typical per-test file arrangement pattern:

```kotlin
markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_direct_stellaris.test.txt")
myFixture.configureByFile("features/index/usage_direct_stellaris.test.txt")
```

Notes:
- The marked config directory SHOULD NOT directly contain config files, place them in the `core` (or some game type id like `stellaris`, see `ParadoxGameType` for details about game types) subdirectory.
- The marked file path DO NOT start with `game/` (see `ParadoxGameTypeMetadata` for details about root directory VS entry directory).
- Alignment between real file path and marked file path is not required.

### Optional / on-demand tests (assume-based)

Some tests are intentionally **disabled by default** and only run when explicitly enabled via system properties.

- Gatekeeping is done via `Assume` predicates (e.g. AI tests, local-environment-only tests, benchmarks).
- Enable categories using specific system properties (see `AssumePredicates` for details).

### Best practices

- Prefer **targeted** test runs during development:
  - `./gradlew test --tests "<fully.qualified.TestClass>"`
  - `./gradlew test --tests "*SomeKeyword*"`
- Prefer adding or updating tests when behavior changes:
  - Unit tests for pure logic.
  - Integration tests for syntax/semantic/PSI/index/config-driven logic.

## Coding conventions

### Naming

Here are some common conversions:

- Prefer using prefix for language and domain specific class names (e.g., `Cwt...` `Paradox...` `ParadoxScript...`).
- prefer using verb form for actions and intentions (e.g, `CopyDefinitionNameIntention`).
- Prefer word-based or prefix-based abbreviations (e.g., for `scopeContext`: `context`, `sc` or just `c` is good, `ctx` is bad).

For more details, see: `agents/context/naming-conventions.md`

### Comments

- Write documentation comments in Chinese by default, unless explicitly requested, or need to be consistent with the context or relevant location.
- Write normal comments in Chinese or English, based on the context or relevant location.
- Prefer KDoc style for Kotlin.
- When referencing types like `PsiElement` in KDoc, prefer KDoc links like `[PsiElement]`.
- Avoid overly long parameter-by-parameter docs unless truly necessary, prefer describing the method as a whole.

### Caching

- Prefer `icu.windea.pls.core.util.CachesKt.CacheBuilder` for caching (if `ConcurrentHashMap` it not enough).
- Prefer `com.github.benmanes.caffeine.cache.Interner` for string interning.
- Global caches in `icu.windea.pls.core`: usually strong values with size + TTL.
- Very large caches (e.g. config objects): usually soft values.
- IDE-lifecycle-tied caches: usually soft values.

### Indexing

- File-level analysis data (e.g., locale, file path): use `FileBasedIndex`.
- Data depending on analysis data and/or PSI-structure, not depending on dynamic data (e.g., scripted variables, localisations): prefer `StubIndex`.
- Data depending on PSI reference resolve results and/or config data (e.g., definitions, complex enums values): prefer `FileBasedIndex`.

### Code organization

Package organization:

- `icu.windea.pls.core`: Common extensions, utilities and components for stdlib, platform and third-party libraries.
- `icu.windea.pls.base`: Plugin specific base code. Including internal state management, external data processing, environment detection and other logic.
- `icu.windea.pls.ide`: Global codes to handle IDE platform integration. Usually language-free and domain-free.
- `icu.windea.pls.config`: Codes related to config, config expression and config group. Usually not depend on game or mod files. 
- `icu.windea.pls.lang`: Codes which are domain specific, or related to semantic match and resolution.
  - `icu.windea.pls.lang.match`: Semantic-level matching (mainly based on indices, reference resolution and configs).
  - `icu.windea.pls.lang.resolve`: Semantic-level resolution (mainly based on indices, reference resolution and configs).
  - `icu.windea.pls.lang.util`: High-level managers and special components.
- `icu.windea.pls.tools`: Codes related to bundled utilities and integrations. Including game launcher, config generator and others.
- `icu.windea.pls.integrations`: Provides integrations with third-party tools.
- `icu.windea.pls.extensions`: Provides integrations and extensions to third-party plugins.

Service vs Manager vs Util:

- `Service`: Lower-level, may involve EP-driven analysis/match/resolve logic.
- `Manager`: Higher-level, convenient domain methods, typically hosts caching.
- `Util`: Narrow-purpose helpers.

### Code guidance

Here are some common code patterns:

- Get the coroutine scope: Use `ChronicleFacade.getCoroutineScope(project)` (or `ChronicleFacade.getCoroutineScope()` for application level).
- Get the config group: Use `ChronicleFacade.getConfigGroup(project, gameType)` (or `ChronicleFacade.getConfigGroup(gameType)` for application level).
- Get the config context: Use `ParadoxConfigManager.getConfigContext(element)`.
- Get the matched configs: Use `ParadoxConfigManager.getConfigs(element, options)`.
- How to search definitions (e.g., an event with specific event id): Search usages of `ParadoxDefinitionSearch` (so do other `Paradox...Search`s).
- How to check out domain or topic specific codes (e.g., definition, scope, recursion): Search declarations of `...Info`, `...Data`, `...Util`, `...Service`, `...Manager` and so on.
- How to check out provided features and domain entries: View relevant docs, check `plugin.xml` (and the including XML configuration files), or search relevant keywords.
- Assume and search existing extensions, components, utils, services, managers, etc., **before** reinventing the wheel.
- Follow the best practice for Kotlin programming and IntelliJ platform development, more importantly, the **conceptual consistency**.

## Domain terminology and guidance

### Translation terms

Prefer translate *config* to *规则*, and vice versa, if it specifically means *CWT* config.

Here are some common terms:

- scope → 作用域
- modifier → 修正
- trigger → 触发器
- effect → 效果
- scripted variable → 封装变量
- scripted trigger → 封装触发器
- scripted effect → 封装效果
- on action → 动作触发
- define → 定值
- definition → 定义
- localisation → 本地化

For more details, see: `agents/context/translation-terms.md`

### Language guidance

For detailed language syntax and recommended examples, see:

- `docs/en/ref-syntax.md`
- `src/test/testData/cwt/example.test.cwt`
- `src/test/testData/script/example.test.txt`
- `src/test/testData/localisation/example.test.yml`
- `src/test/testData/csv/example.test.csv`

### Config system guidance

For the documents and examples, see:

- `docs/en/config.md` (the config system document)
- `docs/en/ref-config-format.md` (the config format manual)
- `src/test/testData/chronicle` (the easter-egg config directory)
- `cwt/cwtools-stellaris-config` (the real-game config directory for Stellaris, also available for other game types)

## Agent instructions

### Communication

- **IMPORTANT**: Communicate with the maintainer in **Chinese**.
- **TIP**: Meanwhile, write documents, doc comments and normal comments in Chinese or/and English, depending on the specific scenario.

### Markdown specifics

- Prefer `-` for unordered lists.
- Prefer `**bold**` for emphasis.
- Prefer starting from `###` (H3) in responses (not in documents), unless there is a strong reason to use `#` and `##`.

### IntelliJ plugin specifics

- Many registrations live under `src/main/resources/META-INF/chronicle-*.xml` included by `plugin.xml`.
- Optional dependencies (enabled only when present): Markdown, Diagrams (Ultimate), Translation plugin.
- There is an internal code injection subsystem, avoid changing it casually unless you understand the impact.

### Making changes safely

- Keep changes minimal and localized.
- Prefer extending via existing EPs and config-driven mechanisms instead of hard-coding game-specific behavior.
- If adding new EP implementations, follow the naming conventions above.
- Add/update tests when feasible; distinguish unit vs integration tests.
- Run `./gradlew test` (or a targeted test task) before finishing.

## Tooling preferences

Prefer **tool-assisted** workflows over ad-hoc shell usage.

### General operations

- Prefer built-in tools for common operations (e.g., read, write, edit, patch, grep search, glob search).
- Prefer using built-in tools to execute commands for build tool operations (e.g., building, running tests), and operations that are more suitable to be done by commands.
- Prefer using suitable mcp when structured search or semantic search is available.
- Prefer running IDE inspections provided by intellij mcp or intellij-index mcp before compilation, building, or running tests, if necessary.

### JetBrains MCP server

When you need to **drive IDE actions** (not just code intelligence), prefer the built-in JetBrains MCP server tools when available:

- Run configurations: list via `get_run_configurations`, run via `execute_run_configuration`
- IDE inspections / file problems: `get_file_problems`
- Reformatting: `reformat_file`

### IDE Index MCP server

When doing **code navigation/refactoring** on symbols, prefer the IDE Index MCP server tools (semantic/index-based) instead of text-based grep when available:

- **Finding references**: use `ide_find_references`
- **Finding implementations**: use `ide_find_implementations`
- **Go to definition**: use `ide_find_definition`
- **Renaming symbols**: use `ide_refactor_rename`
- **Type hierarchy**: use `ide_type_hierarchy`
- **Diagnostics**: use `ide_diagnostics`
- Etc.

Notes:

- Prefer IDE tools because they understand PSI/AST and indices, which is far more accurate than plain text search.
- If the IDE is still indexing, check readiness first via `ide_index_status` (wait/retry when in dumb mode).
- If multiple projects are open in one IDE window, always pass the `project_path` parameter to avoid ambiguity.
