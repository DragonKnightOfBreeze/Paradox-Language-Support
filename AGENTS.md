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
- `documents/ai-rules/`: agent-facing behavioral rules used by this repo
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

From `documents/ai-rules/test.md`:

- Prefer Kotlin for tests.
- **Unit tests**: for pure components/tools/extensions; usually no IntelliJ API.
- **Integration tests**: for PSI/index/query/semantic match/resolve/integrations; uses IntelliJ test framework.
- Frameworks/tooling: **JUnit4 + IntelliJ test framework**.

When running tests during iterative work, start with a “warning-level” run (keep logs readable). If needed, re-run with more logging.

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

## Documentation and comments (agent instructions)

From `documents/ai-rules/docstring.md` and `documents/ai-rules/be-more-lovely.md`:

- **Write KDoc/docstrings in Chinese by default**, unless the user explicitly requests English or the local context is English-only.
- Prefer KDoc style for Kotlin.
- When referencing types like `PsiElement` in KDoc, prefer KDoc links: `[PsiElement]`.
- Avoid overly long parameter-by-parameter docs unless truly necessary; prefer describing the method as a whole.

## IntelliJ plugin specifics

- Many registrations live under `src/main/resources/META-INF/pls-*.xml` included by `plugin.xml`.
- Optional dependencies (enabled only when present): Markdown, Diagrams (Ultimate), Translation plugin.
- There is an internal **code injection** subsystem; avoid changing it casually unless you understand the impact.

## When changing code

- Keep changes minimal and localized.
- Prefer extending via existing EPs and config-driven mechanisms instead of hard-coding game-specific behavior.
- If adding new EP implementations, follow the naming conventions above.
- Add/update tests when feasible; distinguish unit vs integration tests.
- Run `./gradlew test` (or a targeted test task) before finishing.

## Safety and secrets

- Do not commit API keys or credentials.
- AI-related tests or features may depend on environment variables (example in build script: `DEEPSEEK_KEY` in a commented-out task).
- Plugin signing/publishing uses environment variables (`CERTIFICATE_CHAIN_PLS`, `PRIVATE_KEY_PLS`, `IDEA_TOKEN`, etc.). Never log them.
