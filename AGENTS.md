# Paradox Language Support

This repository contains **Paradox Language Support**, the IntelliJ IDEA plugin designed for Paradox game mod developers.

In narrative level, the plugin's name is **Paradox Chronicle**.

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

The plugin bundles CWT configs into the plugin JAR under `config/<gameTypeId>`.

- Prefer local repos in `cwt/<repoDir>`.
- If missing (common in CI), Gradle can download ZIPs and unzip them into: `build/generated/cwt/<repoDir>`

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

The plugin is config-driven. Many features (e.g. directives like `inline_script`, definition injection modes, type inference) depend on **CWT config groups** and a simulated “game/mod context”.

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
- The marked file path DO NOT start with `game/` (see `ParadoxEntryInfo` for details about root directory VS entry directory).
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
  - Integration tests for PSI/index/config-driven resolution.

## Coding conventions

### Naming

- Prefer using prefix for language and domain specific class names (e.g., `Cwt...` `Paradox...` `ParadoxScript...`).
- Consider using prefix `Pls` for plugin specific class names (e.g., `PlsStates`).
- Prefer word-based or prefix-based abbreviations (e.g., for `scopeContext`: `context`, `sc` or just `c` is good, `ctx` is bad).

For more details, see: `agents/context/naming-conventions.md`

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
- `icu.windea.pls.ide`: Global codes to handle IDE platform integration. Usually language-free and domain-free.
- `icu.windea.pls.config`: Codes related to config, config expression and config group. Including models, services, resolvers, manipulators, etc.
- `icu.windea.pls.tools`: Codes provided as tool APIs. Including launchers, config generators, log readers, etc. Not necessarily "language features".
- `icu.windea.pls.lang`: Codes which are domain specific, or related to semantic match and resolution.
  - `icu.windea.pls.lang.match`: Semantic-level matching (mainly based on indices, reference resolution and configs).
  - `icu.windea.pls.lang.resolve`: Semantic-level resolution (mainly based on indices, reference resolution and configs).
  - `icu.windea.pls.lang.util`: High-level managers and special components.

Service vs Manager vs Util:

- `Service`: Lower-level, may involve EP-driven analysis/match/resolve logic.
- `Manager`: Higher-level, convenient domain methods, typically hosts caching.
- `Util`: Narrow-purpose helpers.

### Code Guidance

Here are some common code patterns:

- How to get the coroutine scope: Use `PlsFacade.getCoroutineScope(project)` (or `PlsFacade.getCoroutineScope()` for application level).
- How to get the config group: Use `PlsFacade.getConfigGroup(project, gameType)` (or `PlsFacade.getConfigGroup(gameType)` for application level).
- How to get the config context: Use `ParadoxConfigManager.getConfigContext(element)`.
- How to get the matched configs: Use `ParadoxConfigManager.getConfigs(element, options)`.
- How to search definitions (e.g., an event with specific event id): Search usages of `ParadoxDefinitionSearch` (so do other `Paradox...Search`s).
- How to check out domain or topic specific codes (e.g., definition, scope, recursion): Search declarations of `...Service`, `...Manager`, `...Util` and so on.

## Domain terminology and guidance

### Translation terms

Here are some common terms:

- CWT Config → CWT 规则 (prefer translate "config" to "规则", and vice versa, if it specifically means CWT config)
- scope → 作用域
- modifier → 修正
- trigger → 触发器
- effect → 效果
- scripted variable → 封装变量
- define → 定值
- localisation → 本地化
- definition → 定义
  - scripted trigger → 封装触发器
  - scripted effect → 封装效果
  - script value → 脚本值
  - game rule → 游戏规则
  - on action → 动作触发
  - event → 事件
  - event namespace → 事件命名空间
  - sprite → 精灵
- directive → 指令
  - inline script → 内联脚本
  - definition injection → 定义注入

### Language Guidance

For detailed language syntax and recommended examples, see:

- `docs/en/ref-syntax.md`
- `src/test/testData/cwt/example.test.cwt`
- `src/test/testData/script/example.test.txt`
- `src/test/testData/localisation/example.test.yml`
- `src/test/testData/csv/example.test.csv`

### Config System Guidance

For the config system and the config format, see:

- `docs/en/config.md`
- `docs/en/ref-config-format.md`
- `src/test/testData/chronicle` (the easter-egg config directory)
- `cwt/cwtools-stellaris-config` (the real-game config directory for Stellaris, other game types are also available)

## Agent instructions

### Communication

- IMPORTANT: Use Chinese when talking to the main maintainer (unless otherwise specified). / 重要：使用中文与主要维护者交流（除非特别说明）。

### Markdown output conventions

- Prefer `-` for unordered lists.
- Prefer `**bold**` for emphasis.
- If you include headings in Markdown responses, prefer starting from `###` (H3) unless there is a strong reason to use `#`/`##`.

### Documentation and comments

- Write documentation comments in Chinese by default, unless explicitly requested, or need to be consistent with the context or relevant location.
- Write normal comments in Chinese or English, based on the context or relevant location.
- Prefer KDoc style for Kotlin.
- When referencing types like `PsiElement` in KDoc, prefer KDoc links like `[PsiElement]`.
- Avoid overly long parameter-by-parameter docs unless truly necessary; prefer describing the method as a whole.

### IntelliJ plugin specifics

- Many registrations live under `src/main/resources/META-INF/pls-*.xml` included by `plugin.xml`.
- Optional dependencies (enabled only when present): Markdown, Diagrams (Ultimate), Translation plugin.
- There is an internal **code injection** subsystem; avoid changing it casually unless you understand the impact.

### Making changes safely

- Keep changes minimal and localized.
- Prefer extending via existing EPs and config-driven mechanisms instead of hard-coding game-specific behavior.
- If adding new EP implementations, follow the naming conventions above.
- Add/update tests when feasible; distinguish unit vs integration tests.
- Run `./gradlew test` (or a targeted test task) before finishing.

## Tooling preferences

Prefer **tool-assisted** workflows over ad-hoc shell usage.

### General file/text operations

- Prefer built-in tools for common operations (find files by name/glob, search text/regex across the repo, read files before editing, apply well-scoped patches, etc.)
- Prefer running IDE inspections provided by intellij mcp or intellij-index mcp before compilation, building, or running tests, if necessary.
- Avoid blind edits and avoid scanning via shell commands when structured search is available.

### JetBrains official MCP server (IDE actions)

When you need to **drive IDE actions** (not just code intelligence), prefer the built-in JetBrains MCP server tools when available:

- Run configurations: list via `get_run_configurations`, run via `execute_run_configuration`
- IDE inspections / file problems: `get_file_problems`
- Reformatting: `reformat_file`

### IDE Index MCP server (semantic code intelligence)

When doing **code navigation/refactoring** on symbols, prefer the IDE Index MCP server tools (semantic/index-based) instead of text-based grep when available:

- **Finding references**: use `ide_find_references`
- **Finding implementations**: use `ide_find_implementations`
- **Go to definition**: use `ide_find_definition`
- **Renaming symbols**: use `ide_refactor_rename`
- **Type hierarchy**: use `ide_type_hierarchy`
- **Diagnostics**: use `ide_diagnostics`
- **Getting Opened Files**: use `ide_get_active_file`
- Etc.

Notes:

- Prefer IDE tools because they understand PSI/AST and indices, which is far more accurate than plain text search.
- If the IDE is still indexing, check readiness first via `ide_index_status` (wait/retry when in dumb mode).
- If multiple projects are open in one IDE window, always pass the `project_path` parameter to avoid ambiguity.