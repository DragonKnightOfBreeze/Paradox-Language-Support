# Guidance to the Paradox Chronicle

## Overview

This repository contains **Paradox Chronicle** (formerly **Paradox Language Support**), the IntelliJ IDEA plugin designed for mod developers of Paradox Interactive games.

On narrative level, this plugin symbolizes the book also titled **Paradox Chronicle**. While playing on the double meaning of **Chronicle** and **Prophecy Book**, it is, indeed, also the guide book, to the paradox universe.

Given the large codebase (over 120k lines of Kotlin production code) and high complexity (platform & domain & architecture), make this project a challenging undertaking.

As you embark on this journey, keep serious, cautious, and curious.

## Project quick orientation

### What the plugin supports

Following custom languages are supported by the plugin:
- **Paradox Script** (`PARADOX_SCRIPT`) - used for providing game data and writing game logic.
- **Paradox Localisation** (`PARADOX_LOCALISATION`) - used for providing i18n text.
- **Paradox CSV** (`PARADOX_CSV`) - used for describing table data.
- **CWT** (`CWT`, `*.cwt`) - used for writing CWT config files which drive semantics (completion, inspections, navigation, docs, etc.).

In addition to language features, the plugin also includes:
- **Image support** (DDS/TGA) with optional tool-based rendering/conversion.
- **Tool integrations** (e.g. ImageMagick, Tiger lint, Translation plugin).
- **AI integration** (LangChain4j-based) focused on localisation translation/polishing workflows.
- A substantial internal **extension point (EP)** architecture.
- A **code injection** subsystem.

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

The plugin bundles CWT configs into the plugin JAR under `config/<gameTypeId>`. If missing (common in CI), Gradle can download ZIPs and unzip them into `build/generated/cwt/<repoDir>`.

## Testing guidance

### Principles and preferences

- Unit tests: for pure components/tools/extensions; usually no IntelliJ Platform API.
- Integration tests: for PSI/index/query/semantic match and resolve/tool integrations; usually depend on the IntelliJ Platform API or another external integration.
- Platform tests: aka integration tests depend on IntelliJ Platform API.
- Prefer Kotlin for tests.
- Tooling: JUnit4 + IntelliJ Platform test framework.
- If a test class targets one or a few specific subjects (rather than a theme or a bag of extension methods), call it out in the class KDoc via `@see TestTarget`.
- Group test cases with `// region ... // endregion` comments when a class has enough cases to split into modules.
- Add short inline comments where useful to clarify edge cases, details, or expected results of a test case.
- When a test is expected to pass, prefer running it at the default/warn log level (`--warn`) first; only drop to a more verbose log level if it actually fails.

### Best practices

- Prefer **targeted** test runs during development:
  - `./gradlew test --tests "<fully.qualified.TestClass>"`
  - `./gradlew test --tests "*SomeKeyword*"`
- Prefer adding or updating tests when behavior changes:
  - Unit tests for pure logic.
  - Integration tests for syntax/semantic/PSI/index/config-driven logic.
- A full `./gradlew test` run can take tens of minutes; don't run it casually during iterative development.

### IntelliJ platform test patterns

- **Parsing tests** (syntax/PSI snapshots): use `ParsingTestCase`, comparing the parsed tree output against a stored snapshot (e.g. `icu.windea.pls.script.ParadoxScriptParsingTest`).
- **Fixture-based tests**: use `BasePlatformTestCase` + `myFixture.configureByText(fileName, text)` (preferred for simple cases) or `myFixture.configureByFile(filePath)` (for more complex, file-based cases).
- **Highlighting tests** (annotators/inspections): build the text to check highlighting programmatically with the help of needed scope methods, then call `myFixture.checkHighlighting(...)`.

### Test data conventions

- Most platform/integration tests use test data under `src/test/testData`.
- Recommended (not universally enforced) naming: `*.test.txt` / `*.test.yml` / `*.test.cwt` / `*.test.csv`, in `snake_case`, optionally with a `.{gameTypeId}` segment to pin a game type (e.g. `example.stellaris.txt`).
- For `ParsingTestCase`-based tests, the input file is `<caseName>.test.txt` while the expected PSI-tree snapshot file is `<caseName>.txt` (no `.test.` segment).
- Some feature tests provide a test-local `.config/` directory under test data to simulate config groups (e.g. `features/index/.config`, `features/inspections/.config`); config files must live in a subdirectory of `.config/` (e.g. `core/`, `stellaris/`), never directly inside it.
- `src/test/testData/issues/<issueNumber>[_shortDesc]/` holds regression test data for GitHub-issue-specific tests (see `icu.windea.pls.test.issues.IssueNNNTest` below), typically with a `README.md` describing the scenario.
- `src/test/testData/chronicle/` is a self-contained showcase "game/mod" content tree (see Snapshot tests below) and follows common file naming (e.g. `00_events.txt`) instead of the `.test.` convention, since it simulates real game/mod content rather than being a narrow test fixture.

### About test scope

The plugin is config-driven. Many features (e.g. type inference, scope inference, macros) depend on **CWT config groups** and a simulated “game/mod context”.

A set of scope methods, defined in interface `icu.windea.pls.test.ChronicleTestScope`, makes these tests deterministic. It's mixed into test classes via interface composition (not a shared base class): `class XxxTest : BasePlatformTestCase(), ChronicleTestScope`. There is no common `ChronicleBasePlatformTestCase`; only a few narrower abstract bases exist for specific test families (e.g. `ParadoxComplexExpressionTest`).

Key `ChronicleTestScope` methods:
- `findElementAtCaret()` / `findReferenceAtCaret()` are `CodeInsightTestFixture` extensions for caret-based lookups.
- `addAdditionalAllowedRoots(...)` whitelists extra filesystem roots for VFS access checks (used e.g. to reach a real local Steam/game path in `ParadoxModImporterTest`).
- `markIntegrationTest()` / `clearIntegrationTest()` toggle integration-test-only behavior (inferring file type/game type from file name) and clean up injected state.
- `markRootDirectory(relPath)` / `markConfigDirectory(relPath)` inject the root/config directory path, relative to the test data directory (`src/test/testData`).
- `createRootInfo(gameType, gameVersion = null)` builds an injected root info, optionally pinned to a specific game version - useful for testing version-gated behavior.
- `markFileInfo(gameType or rootInfo, path, entry = "", group = null)` (for a file to be configured afterward, e.g. via `myFixture.configureByFile`) and `VirtualFile.injectFileInfo(...)` (for an already-existing `VirtualFile`) inject per-file metadata; both have an overload taking a pre-built `ParadoxRootInfo` (from `createRootInfo`) instead of a bare `gameType`.
- `initConfigGroups(project, ...gameTypes)` initializes the required config groups for the specified game types. Use built-in and injected config files, and the general config group (`core`) is always initialized.

Notes:
- The marked config directory SHOULD NOT directly contain config files, place them in the `core` (or some game type id like `stellaris`, see `ParadoxGameType` for details about game types) subdirectory.
- The marked file path DO NOT start with `game/` (see `ParadoxGameTypeMetadata` for details about root directories VS entry directories).
- Alignment between real file path and marked file path is not required.
- If a config-driven test fails unexpectedly, first check whether `markRootDirectory(...)` was omitted (some caches are keyed at the root-directory level), the config files were not correctly injected, or the relevant config groups were not correctly loaded.

For the showcase test demonstrating `ChronicleTestScope` usage, see `icu.windea.pls.test.chronicle.ChronicleHelloWorldTest`.

### About test DSL

Additionally, the plugin provides some DSLs for unit tests and platform tasks, which can be used to write tests that are simpler and clearer.

They are defined in package `icu.windea.pls.test.dsl`, with corresponding DSL marker annotations, scope objects and scope methods. These scope methods should be used on certain limited scope.

Introduce to these DSLs:
- `ExpectDsl.kt` can be used to write assertions in extension-function style, which will apply type convertion and Kotlin Contract when necessary. It's not forced at this moment.
- `HighlightingDsl.kt` can be used to build text for checking highlighting in string-interpolation style, which are more readable and maintainable comparing to raw text or raw files. It's preferred at this moment.

Examples:
- `expectScope { result.expectNotNull().someMethodForNonNullType() }`
- `expectScope { result.expectIs<SomeType>().someMethodForThisType() }`
- `myFixture.configureByText("test.txt") { "${error(message)}key${errorEnd} = value" }`

For the showcase test demonstrating `ChronicleTestScope` usage in DSL-style, see `icu.windea.pls.test.chronicle.ChronicleHelloWorldDslTest`.

### Snapshot tests

Package `icu.windea.pls.test.chronicle` hosts a family of "snapshot" tests driven by the showcase content tree `src/test/testData/chronicle/`:

- `ChronicleSnapshotTest` - abstract base providing `computeDataFilePaths()` over the `chronicle/` directory.
- `ChronicleInspectionBasedSnapshotTest` - runs every bundled `LocalInspectionTool` (filtered to this plugin) over all files under `chronicle/` and asserts no warnings/errors are produced.
- `ChronicleAnnotatedSnapshotTest` - renders script/CSV files via `ParadoxScriptTextAnnotatedRenderer` / `ParadoxCsvTextAnnotatedRenderer` and diffs the result against expected files under `chronicle/.annotated/**/*.annotated.*`.

### Optional / on-demand tests (assume-based)

Some tests are intentionally **disabled by default** and only run when explicitly enabled via system properties, gated through `icu.windea.pls.test.ChronicleAssume` (each method wraps `org.junit.Assume.assumeTrue(...)`, so a gated-out test is reported as *skipped*, not *failed*). Flags are read by `icu.windea.pls.test.ChronicleTestCapacities` from system properties:

| `ChronicleAssume` method   | System property                           | Category                                                                          |
|----------------------------|-------------------------------------------|-----------------------------------------------------------------------------------|
| `includeBenchmark()`       | `chronicle.test.include.benchmark`        | Benchmarks                                                                        |
| `includeAi()`              | `chronicle.test.include.ai`               | AI-backed tests                                                                   |
| `includeRemote()`          | `chronicle.test.include.remote`           | Tests requiring access remote network (e.g., `SpecialUrlServiceTest`)                                    |
| `includeLocalEnv()`        | `chronicle.test.include.local.env`        | Tests requiring a real local game/mod environment (e.g. `ParadoxModImporterTest`) |
| `includeConfigGenerator()` | `chronicle.test.include.config.generator` | Config generator tests                                                            |

`chronicle.test.include.all` (checked via `includeAll()`) unconditionally enables every category above. Example: `./gradlew test -Dchronicle.test.include.local.env=true`.

## Code architecture

### Modules

- `icu.windea.pls.core` - General infrastructure. Including common extensions, utilities and components for stdlib, platform and third-party libraries.
- `icu.windea.pls.base` - Infrastructure for the plugin itself and platform integration. Including internal state management, external data processing, environment detection, high-level platform integration, etc.
- `icu.windea.pls.cwt` - Infrastructure to support the CWT language. Usually semantic-free.
- `icu.windea.pls.script` - Infrastructure to support the script language. Usually semantic-free.
- `icu.windea.pls.localisation` - Infrastructure to support the localization language. Usually semantic-free.
- `icu.windea.pls.csv` - Infrastructure to support the CSV language. Usually semantic-free.
- `icu.windea.pls.config` - Infrastructure for the config system. Including config, config expression, config group, etc.
- `icu.windea.pls.lang` - The semantic module. Including semantic matching, semantic resolution, language construct support, language feature support, domain-specific logic, etc.
  - `icu.windea.pls.lang.match` - Semantic-level matching. Mainly based on indices, reference resolution and configs.
  - `icu.windea.pls.lang.resolve` - Semantic-level resolution. Mainly based on indices, reference resolution and configs.
  - `icu.windea.pls.lang.util` - High-level managers and special components.
- `icu.windea.pls.integrations` - The integration module. Provides integrations to third-party tools (e.g, image processing tools and linting tools).
- `icu.windea.pls.extensions` - The extension module. Provides integrations and extensions to third-party plugins (e.g., JSON and Markdown).
- `icu.windea.pls.tools` - The tool module. Provides several bundled utilities and integrations (e.g., game launchers and config generators).
- `icu.windea.pls.ep` - Various EP interfaces and implementations to provide language construct support, language feature support and several QoL features.

### Notes

Notes on `icu.windea.pls.config`:
- This is where the infrastructure of the config system is stored, but not all code closely related to the config system. This means they usually not depends on game or mod files.
- For logic such as config contexts, config matching, template resolution, and template matching, since they occur at semantic-level, is usually located in `lang.match` and `lang.resolve`.

Notes on `icu.windea.pls.lang.match` and `icu.windea.pls.lang.resolve`:
- `lang.match` and `lang.resolve` hold domain-specific, semantic-level matching and resolution logic, and are lower-level than `lang.util.*Manager`.
- The latter is only extracted/refactored into the former when necessary, most of it can stay as-is.
- Prefer calling EPs that involve matching/resolution logic only from here (typically in a `*Service`), not from an EP interface's companion object or a `*Manager`.

Notes on `icu.windea.pls.config.manipulation` and `icu.windea.pls.lang.manipulation`:
- These packages hold "manipulation"-focused services plus related model classes, enums, and extension methods.
- The code here typically transforms or directly mutates the state of a target (a config object, PSI, etc.)
- It can happen at several different stages/levels (building the resolve context, performing semantic resolution, invoking a language feature).
- It also serves as infrastructure for parts of semantic resolution as well as some refactorings, intentions, and quick fixes.

Notes on `Service` vs `Manager` vs `Util`:
- `Service` - lower-level, may include domain analysis/matching/resolution logic that's delegated to concrete EP implementations.
- `Manager` - higher-level, exposes ready-to-use domain methods, which may depend on the corresponding `Service` methods.
- `Util` - narrowly-scoped helper methods usable only for a specific component/feature, or only in a handful of specific scenarios.
- `Service` and `Manager` don't need to be declared as IntelliJ services, they can be declared as plain Kotlin singleton objects if stateless.
- Caching logic usually lives at the `Manager` level, `Service`-level code is usually a direct fetch/resolve without caching.

## Coding conventions

### Naming

Here are some common conversions:
- Prefer using prefix for language and domain specific class names (e.g., `Cwt...` `Paradox...` `ParadoxScript...`).
- prefer using verb form for actions and intentions (e.g, `CopyDefinitionNameIntention`).
- Prefer word-based or prefix-based abbreviations (e.g., for `scopeContext`: `context`, `sc` or just `c` is good, `ctx` is bad).

For more details, see: `agents/context/naming-conventions.md`

### Importing

- Prefer explicit imports; avoid wildcard (`*`) imports except for a short list of DSL-style packages (see below), or when the explicit imports from one package would exceed the star-import threshold (50).
- Packages preferred for wildcard imports (including subpackages): `com.intellij.ui.dsl` (IntelliJ UI DSL), `icu.windea.pls.lang.resolve.complexExpression.dsl`, `icu.windea.pls.lang.resolve.complexExpression.nodes`.
- It's fine for agent-authored code to leave behind unused or slightly unordered imports - these are trivial for a human (or an "Optimize Imports" pass) to clean up afterward, so don't block on perfecting imports over correct logic.

For more details, see: `agents/context/importing-conventions.md`

### Comments

- Write documentation comments in Chinese by default, unless explicitly requested, or need to be consistent with the context or relevant location.
- Write normal comments in Chinese or English, based on the context or relevant location.
- Prefer KDoc style for Kotlin.
- When referencing types like `PsiElement` in KDoc, prefer KDoc links like `[PsiElement]`.
- Prefer natural-language descriptions over embedding literal code snippets; reference key code locations instead (e.g. via `@see`) when necessary.
- Avoid documenting member properties individually - fold them into the containing class/interface KDoc (using `@property`) unless a property truly needs detailed documentation.
- Avoid overly long parameter-by-parameter docs (`@param`) unless truly necessary; prefer describing the method as a whole and referencing parameters inline (e.g. `[param]`) when needed.

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
- For file indices that depend on resolved/matched member configs, prefer unifying via a merged index (e.g. `ParadoxMergedIndex`) for performance.
- Compress serialized index data when necessary (e.g. via `readOrReadFrom` or `readIndexedStringList`).

### Code guidance

Here are some common code patterns:
- Get the coroutine scope: Use `ChronicleFacade.getCoroutineScope(project)` (or `ChronicleFacade.getCoroutineScope()` for application level).
- Get the config group: Use `ChronicleFacade.getConfigGroup(project, gameType)` (or `ChronicleFacade.getConfigGroup(gameType)` for application level).
- Get the config context: Use `ParadoxConfigManager.getConfigContext(element)`.
- Get the matched configs: Use `ParadoxConfigManager.getConfigs(element, options)`.
- How to filter and query configs in config trees (e.g., query down, by path): Use the config select API (see `icu.windea.pls.config.select`).
- How to filter and query members in script files (e.g., query down, query up, by path): Use the select API (see `icu.windea.pls.lang.select`).
- How to search definitions (e.g., an event with specific event id): Use `ParadoxDefinitionSearch` (so do other `Paradox...Search`s).
- How to check out domain or topic specific codes (e.g., definition, scope, recursion): Search declarations of`...Data`,  `...Info`, `...Util`, `...Service`, `...Manager` and so on.
- How to check out provided features and domain entries: View relevant docs, check `plugin.xml` (and the including XML configuration files), or search relevant keywords.
- Assume and search existing extensions, components, utils, services, managers, EPs, etc., **before** reinventing the wheel.
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
- `src/test/testData/chronicle/` (the easter-egg config directory)
- `cwt/cwtools-stellaris-config/` (the real-game config directory for Stellaris, also available for other game types)

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

### General operations

- Prefer using built-in tools for common operations (e.g., read, write, edit, patch, grep search, glob search).
- Prefer using built-in tools to execute commands for build tool operations (e.g., building, running tests), and operations that are more suitable to be done by commands.
- Prefer using suitable mcp when structured search or semantic search is available.
- Prefer running IDE inspections provided by intellij mcp or intellij-index mcp before compilation, building, or running tests, if necessary.

### JetBrains MCP server

When you need to **drive IDE actions** (not just code intelligence), prefer the built-in JetBrains MCP server tools when available.

### IDE Index MCP server

When doing **code navigation/refactoring** on symbols, prefer the IDE Index MCP server tools (semantic/index-based) instead of text-based grep when available.