# AGENTS.md

This repository contains **Paradox Language Support (PLS)**, a large IntelliJ Platform plugin (written in Kotlin) that provides deep IDE support for Paradox mod development.

PLS is **PSI/index/inspection-driven** (not LSP-based). Many language features are powered by a **config system** based on **CWT config files** (CWT is a DSL similar to Paradox script; the relationship is roughly like **JSON vs JSON Schema**).

## Project quick orientation

### What the plugin supports

- **Paradox Script** (`PARADOX_SCRIPT`)
- **Paradox Localisation** (`PARADOX_LOCALISATION`)
- **Paradox CSV** (`PARADOX_CSV`)
- **CWT** configs (`CWT`, `*.cwt`) used to drive semantics (completion, inspections, navigation, docs, etc.)

In addition to language features, PLS also includes:

- **Image support** (DDS/TGA) with optional tool-based rendering/conversion.
- **Tool integrations** (e.g. ImageMagick, Tiger lint, Translation plugin).
- **AI integration** (LangChain4j-based) focused on localisation translation/polishing workflows.
- A substantial internal **extension point (EP)** architecture and a **code injection** subsystem.

### Repository structure (high level)

- `src/main/kotlin` / `src/main/java` / `src/main/resources`: plugin source
- `src/main/resources/META-INF/plugin.xml`: plugin entry; most registrations are split into `META-INF/pls-*.xml`
- `cwt/`: CWT config repositories (core + per-game downstream repos)
- `docs/`: user-facing documentation (zh/en)
- `documents/notes/`: maintainer notes (coding conventions, domain model notes)
- `src/test/...`: tests and test data

## Setup and build commands (Windows)

This project uses **Gradle** and the **IntelliJ Platform Gradle Plugin**.

### Requirements

- **JDK 21** (the build uses `kotlin.jvmToolchain(21)`)
- Gradle wrapper (use `./gradlew` / `./gradlew` on Windows)

### Common commands

> On Windows PowerShell, prefer running Gradle via the wrapper:
>
> - `./gradlew <task>`

- Run IDE for debugging:
  - `./gradlew runIde`
- Build the plugin ZIP:
  - `./gradlew buildPlugin`
- Run tests:
  - `./gradlew test`

### CWT config repositories (important)

PLS bundles CWT configs into the plugin JAR under `config/<gameTypeId>`.

- Prefer local repos in `cwt/<repoDir>`.
- If missing (common in CI), Gradle can download ZIPs and unzip them into:
  - `build/generated/cwt/<repoDir>`

Relevant tasks:

- Prepare configs (download/unzip as needed):
  - `./gradlew prepareCwtConfigs`

Gradle properties controlling this behavior:

- `-Ppls.cwt.downloadIfMissing=true|false`
- `-Ppls.cwt.acceptAnyCertificate=true|false` (temporary SSL handshake bypass)

## Testing guidance

### Test taxonomy

- Prefer Kotlin for tests.
- **Unit tests**: for pure components/tools/extensions; usually no IntelliJ API.
- **Platform / integration tests**: for PSI/index/query/semantic match/resolve/integrations; uses the IntelliJ test framework.
- Tooling: **JUnit4 + IntelliJ test framework**.

### Test data conventions

- Most platform/integration tests use test data under `src/test/testData`.
- Naming convention is typically `*.test.txt` / `*.test.yml` / `*.test.cwt` / `*.test.csv`.
- Some feature tests provide a test-local `.config/` directory under test data to simulate config groups.

### IntelliJ platform test patterns

- **Parsing tests** (syntax/PSI snapshots): use `ParsingTestCase` and compare the parsed tree output.
- **Fixture-based tests**: use `BasePlatformTestCase` + `myFixture.configureByFile(...)`.
- **Index tests**:
  - `StubIndex`-based indices (stub-driven PSI data).
  - `FileBasedIndex`-based indices (file-level computed data).

### Config-driven integration tests (config groups + context injection)

PLS is config-driven. Many features (e.g. directives like `inline_script`, definition injection modes, type inference) depend on **CWT config groups** and a simulated “game/mod context”.

Test helpers exist to make these tests deterministic:

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
markFileInfo(ParadoxGameType.Stellaris, "common/test/usage_direct_stellaris.test.txt")
myFixture.configureByFile("features/index/usage_direct_stellaris.test.txt")
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

Example (run only AI tests when you have local credentials configured):

```bash
./gradlew test -Dpls.test.include.ai=true --tests "*ChatModelProviderTest*"
```

### Best practices

- Prefer **targeted** test runs during development:
  - `./gradlew test --tests "<fully.qualified.TestClass>"`
  - `./gradlew test --tests "*SomeKeyword*"`
- Prefer adding or updating tests when behavior changes:
  - Unit tests for pure logic.
  - Integration tests for PSI/index/config-driven resolution.
- Be aware this plugin is **config-driven**:
  - If your change affects config parsing, config group loading, indexing, or resolve semantics, you may need broader integration coverage.
  - If your change impacts bundled CWT configs, run `./gradlew prepareCwtConfigs` (or a full `./gradlew test`) to catch packaging/index regressions.
- Keep test logs actionable:
  - Avoid introducing tests that rely on external network calls.
  - If a feature depends on environment variables/keys, tests should be guarded to avoid false failures.

## Coding conventions (repo-specific)

This section summarizes `documents/notes/笔记：代码规范.md`.

### Naming

For EP implementation classes, prefer:

- `Domain + Layer + ImplementationType + Role`
  - Domain examples: `Cwt`, `Paradox`, `Stellaris`
  - Layer examples: `Base`, `Core`, `Default`
  - Role: the key part of the EP interface name (e.g. `ExpressionSupport`)

Semantics of common layer words:

- `Default`: the default/most general implementation
- `Base`: foundational implementation when there are also core/extended implementations
- `Core`: more advanced built-in implementation

For abstract classes, `Base` may appear as a suffix (e.g. `ParadoxScriptExpressionSupportBase`).

Avoid non-word abbreviations. For example, prefer `context` over `ctx`.

### Imports

- Prefer explicit imports; allow star imports mainly for DSL-style packages.
- Star import threshold: **50**.
- Star-import is acceptable for known DSL packages, such as:
  - `com.intellij.ui.dsl.*`
  - `icu.windea.pls.config.select.*`
  - `icu.windea.pls.lang.psi.select.*`

### Caching

- Prefer `icu.windea.pls.core.util.CachesKt.CacheBuilder`.
- Prefer `com.github.benmanes.caffeine.cache.Interner` for string interning.
- Global caches in `icu.windea.pls.core`: usually strong values with size + TTL.
- Very large caches (e.g. config objects): usually soft values.
- IDE-lifecycle-tied caches: usually soft values.

### Indexing

- File-level analysis data (language area, file path): use `FileBasedIndex`.
- PSI-structure data not depending on dynamic config (scripted variables, localisation): use `StubIndex`.
- Data depending on resolved references or config data: consider `FileBasedIndex`.
- If you need simulated PSI or member-rule-dependent data: prefer `ParadoxMergedIndex` to reduce repeated parsing.

### Code organization (packages)

- `icu.windea.pls.core`: stdlib/platform/third-party extensions + shared utilities
- `icu.windea.pls.config`: config/config group/config expression models + services/resolvers/manipulators
- `icu.windea.pls.tools`: tool-like APIs (launchers, generators, log readers) that are not necessarily “language features”
- `icu.windea.pls.lang.match` / `icu.windea.pls.lang.resolve`: semantic-level matching/resolution (often config-driven)

Service vs manager vs util:

- `service`: lower-level, may involve EP-driven analysis/match/resolve logic
- `manager`: higher-level, convenient domain methods; typically hosts caching
- `util`: narrow-purpose helpers

### Inspection message formatting

When referring to identifiers (names, types, etc.) in English messages, prefer backticks (`` `id` ``) rather than single quotes, except in some “after a colon” contexts.

## Domain notes and terminology

This section summarizes `documents/notes/笔记：领域相关.md`.

### Recommended English terms (important for docs/messages)

- “规则 / CWT 规则” → **config / CWT config** (avoid “rule” if it specifically means CWT configs)
- “封装变量” → **scripted variable**
- “作用域” → **scope**
- “触发器 / 效果 / 修正” → **trigger / effect / modifier**
- “本地化” → **localisation**
- “指令” → **directive**
  - “内联脚本” → **inline script**
  - “定义注入” → **definition injection**

### Script PSI vs semantic concepts

PLS distinguishes syntax-level PSI elements (properties/values/blocks, inline math, parameters, parameter conditions) from semantic-level entities like:

- `scriptedVariable`
- `definition` (root key, type key, definition member)
- `directive` (inline script usage, definition injection)
- `complexExpression` (scope field expression, value field expression, etc.)

When implementing new features, be explicit about which layer you are operating on.

## Agent instructions

### Communication

- Prefer Chinese when talking to the main maintainer.

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

### Working style

- If a task is large/complex and needs a detailed plan, consider writing it into `documents/ai-plans/`.
- If the task is analysis/evaluation/exploration, consider writing a report into `documents/ai-reports/`.
- If you notice existing conversation notes in opened files, you may use them to guide the work and suggest next steps, but do not execute unrelated tasks unless explicitly requested.

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

### Tooling preferences (important)

Prefers **tool-assisted** workflows over ad-hoc shell usage.

#### General file/text operations

- Prefer built-in repo tools for common operations:
  - Find files by name/glob
  - Search text/regex across the repo
  - Read files before editing
  - Apply minimal, well-scoped patches
- Avoid blind edits and avoid scanning via shell commands when structured search is available.

#### IDE Index MCP server (semantic code intelligence)

When doing **code navigation/refactoring** on symbols, prefer the IDE Index MCP server tools (semantic/index-based) instead of text-based grep:

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

#### JetBrains official MCP server (IDE actions)

When you need to **drive IDE actions** (not just code intelligence), prefer the built-in JetBrains MCP server tools when available:

- Run configurations: list via `get_run_configurations`, run via `execute_run_configuration`
- IDE inspections / file problems: `get_file_problems`
- Reformatting: `reformat_file`

#### Shell usage

- Use the shell mainly for running Gradle tasks/tests.
- Prefer the Gradle wrapper: use `./gradlew` for all Gradle tasks.
- Prefer non-destructive commands and minimal output.
