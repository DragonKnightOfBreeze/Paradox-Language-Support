# AGENTS.md

This repository contains **Paradox Language Support** (abbr: PLS), the IntelliJ IDEA plugin designed specifically for Paradox game mod developers.

It's written in Kotlin and PSI-based (not LSP-based).
Many language features are powered by the **config system** based on **CWT config files**.
CWT is a DSL similar to Paradox script, the relationship is roughly like **JSON vs JSON Schema**.

## Project quick orientation

### What the plugin supports

- **Paradox Script** (`PARADOX_SCRIPT`)
- **Paradox Localisation** (`PARADOX_LOCALISATION`)
- **Paradox CSV** (`PARADOX_CSV`)
- **CWT** configs (`CWT`, `*.cwt`) used to drive semantics (completion, inspections, navigation, docs, etc.)

In addition to language features, the plugin also includes:

- **Image support** (DDS/TGA) with optional tool-based rendering/conversion.
- **Tool integrations** (e.g. ImageMagick, Tiger lint, Translation plugin).
- **AI integration** (LangChain4j-based) focused on localisation translation/polishing workflows.
- A substantial internal **extension point (EP)** architecture and a **code injection** subsystem.

### Repository structure (high level)

- `src/main/kotlin`, `src/main/java`, `src/main/resources`: plugin source.
- `src/test/kotlin`, `src/test/java`, `src/test/resources`: test codes and test resources.
- `src/test/testData`: test data files (such as test-specific cwt config files and script files).
- `src/main/resources/META-INF/plugin.xml`: plugin entry, most registrations are split into `META-INF/pls-*.xml`.
- `cwt/`: CWT config repositories (core + per-game downstream repos).
- `docs/`: reference documentation (including language syntax guidance and config format guidance).
- `documents/notes/`: maintainer documentation (including ai-generated docs and maintainer-written notes).

## Setup and build commands (Windows)

This project uses **Gradle** and the **IntelliJ Platform Gradle Plugin**.

### Requirements

- **JDK 21** (the build uses `kotlin.jvmToolchain(21)`)
- Gradle wrapper (use `./gradlew` / `./gradlew` on Windows)

### Common commands

> On Windows PowerShell, prefer running Gradle via the wrapper:
>
> - `./gradlew <task>`

- Run IDE for debugging: `./gradlew runIde`
- Build the plugin ZIP: `./gradlew buildPlugin`
- Run tests: `./gradlew test`

### CWT config repositories (important)

The plugin bundles CWT configs into the plugin JAR under `config/<gameTypeId>`.

- Prefer local repos in `cwt/<repoDir>`.
- If missing (common in CI), Gradle can download ZIPs and unzip them into: `build/generated/cwt/<repoDir>`
- If necessary, you can check the real-game config files in these local repos.

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
- `markIntegrationTest()` / `clearIntegrationTest()` toggles integration-test-only behavior and cleans up injected state.
- `markRootDirectory(...)` and `markConfigDirectory(...)` allow integration tests to inject “root/config directory” information.
- `markFileInfo(...)` (and `VirtualFile.injectFileInfo(...)`) allow integration tests to inject per-file metadata (game type, logical path, etc.) without requiring a real game installation.

Minimal setup example (typical for index/resolve tests):

```kotlin
@Before
fun setup() {
    markIntegrationTest()
    markRootDirectory("features/index")
    markConfigDirectory("features/index/.config")
    initConfigGroups(project, ParadoxGameType.Stellaris)
}

@After
fun tearDown() = clearIntegrationTest()
```

Typical per-test file arrangement pattern:

```kotlin
markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_direct_stellaris.test.txt") // NOTE that marked file path DO NOT starts with `game/`
myFixture.configureByFile("features/index/usage_direct_stellaris.test.txt") // alignment to marked file path is not required 
```

> Note: the intent here is “inject enough context for the feature under test”, not to reproduce the full game/mod filesystem.

### Optional / on-demand tests (assume-based)

Some tests are intentionally **disabled by default** and only run when explicitly enabled via system properties.

- Gatekeeping is done via `Assume` predicates (e.g. AI tests, local-environment-only tests, benchmarks).
- Enable categories using system properties:
  - `-Dpls.test.include.all=true`
  - `-Dpls.test.include.ai=true`
  - `-Dpls.test.include.local.env=true`
  - `-Dpls.test.include.benchmark=true`
  - `-Dpls.test.include.config.generator=true`

### Best practices

- Prefer **targeted** test runs during development:
  - `./gradlew test --tests "<fully.qualified.TestClass>"`
  - `./gradlew test --tests "*SomeKeyword*"`
- Prefer adding or updating tests when behavior changes:
  - Unit tests for pure logic.
  - Integration tests for PSI/index/config-driven resolution.

## Coding conventions

### Naming

- Prefix class names with the domain name if necessary (e.g., `Cwt` `CwtConfig` `Paradox`).
- For abstract classes, `Base` may appear as a suffix (e.g., `ParadoxScriptExpressionSupportBase`).
- Avoid non-word, non-prefix abbreviations (e.g., for `scopeContext`, prefer `context`, `sc` or just `c`, but never `ctx`).

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

- `icu.windea.pls.core`: stdlib/platform/third-party extensions + shared utilities
- `icu.windea.pls.config`: config/config group/config expression models + services/resolvers/manipulators
- `icu.windea.pls.tools`: tool-like APIs (launchers, generators, log readers) that are not necessarily “language features”
- `icu.windea.pls.lang.match` & `icu.windea.pls.lang.resolve`: semantic-level matching & resolution (often config-driven)

Service vs Manager vs Util:

- `Service`: lower-level, may involve EP-driven analysis/match/resolve logic
- `Manager`: higher-level, convenient domain methods; typically hosts caching
- `Util`: narrow-purpose helpers

## Domain notes and terminology

### Translation terms

- CWT Config → CWT 规则 (prefer translate "config" to "规则", and vice versa, if it specifically means CWT configs)
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
  - sprite -> 精灵
- directive → 指令
  - inline script → 内联脚本
  - definition injection → 定义注入

### Language Guidance

For detailed language syntax and recommended examples, see:

- `docs/en/ref-syntax.md`
- `src/test/testData/cwt/example.test.cwt`
- `src/test/testData/script/example.test.txt`
- `src/test/testData/localisation/example.test.yml`

### Config System Guidance

For the config system and the config format, see:

- `docs/en/config.md`
- `docs/en/ref-config-format.md`
- `cwt/cwtools-stellaris-config/config` (the real-game config directory)
- `src/test/testData/chronicle` (the easter-egg config directory)

## Agent instructions

### Communication

- Prefer Chinese when talking to the main maintainer.
- Conversation notes may exist in opened files, you can use them to guide the work and suggest next steps, but do not execute unrequested tasks.

### Markdown output conventions (when you generate or edit Markdown)

- Prefer `-` for unordered lists.
- Prefer `**bold**` for emphasis.
- If you include headings in Markdown responses, prefer starting from `###` (H3) unless there is a strong reason to use `#`/`##`.

### Documentation and comments

- Write documentation comments in Chinese by default, unless explicitly requested, or need to be consistent with the context or relevant location.
- Write normal comments in Chinese or English, based on the context or relevant location.
- Prefer KDoc style for Kotlin.
- When referencing types like `PsiElement` in KDoc, prefer KDoc links: `[PsiElement]`.
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
- **Go to definition**: use `ide_find_definition`
- **Renaming symbols**: use `ide_refactor_rename` (safe, project-wide)
- **Type hierarchy**: use `ide_type_hierarchy`
- **Finding implementations**: use `ide_find_implementations`
- **Diagnostics**: use `ide_diagnostics`

Notes:

- Prefer IDE tools because they understand PSI/AST and indices, which is far more accurate than plain text search.
- If the IDE is still indexing, check readiness first via `ide_index_status` (wait/retry when in dumb mode).
- If multiple projects are open in one IDE window, always pass the `project_path` parameter to avoid ambiguity.
