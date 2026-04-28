# Technical Domain

**Purpose**: Tech stack, architecture, code patterns, and development conventions for Paradox Language Support (PLS).
**Last Updated**: 2026-03-24

## Quick Reference

- **Update When**: Tech stack changes, new architectural patterns, new code conventions
- **Audience**: Developers, AI agents working on PLS

## Primary Stack

| Layer | Technology | Notes |
|-------|-----------|-------|
| Language | **Kotlin** (primary) | Java only for Grammar-Kit generated code and rare legacy cases |
| Build | Gradle + IntelliJ Platform Gradle Plugin | `./gradlew` on Windows |
| SDK | IntelliJ Platform SDK | PSI-based, NOT LSP |
| Parser | Grammar-Kit | Generates Java lexer/parser; wrap in Kotlin |
| AI | LangChain4j | Localisation translation/polishing workflows |
| Testing | JUnit4 + IntelliJ Platform Test Framework | |
| JDK | 21 | `kotlin.jvmToolchain(21)` |

## Architecture Pattern

PSI-based IntelliJ plugin. Language features are driven by a **CWT config system** (analogous to JSON Schema for game script files). Core layers:

- `icu.windea.pls.core` — stdlib/platform extensions, shared utilities
- `icu.windea.pls.config` — config/config group/expression models, resolvers
- `icu.windea.pls.lang` — plugin-specific domain logic (match, resolve, util)
- `icu.windea.pls.ep` — Extension Point implementations (organized by category)
- `icu.windea.pls.tools` — tool-like APIs (launchers, generators)

## Project Structure

```
src/main/kotlin/icu/windea/pls/
├── core/            # Extensions, utilities, shared infrastructure
├── config/          # CWT config models and services
├── lang/            # Domain logic: match, resolve, util, actions, codeInsight
│   ├── util/        # High-level Managers (e.g., ParadoxDefinitionManager)
│   ├── resolve/     # Services (e.g., ParadoxDefinitionService)
│   ├── match/       # Semantic matching
│   └── actions/     # IDE actions (verb/adjective prefix, no domain prefix)
├── ep/              # EP implementations by category
│   ├── codeInsight/ # Hints, documentation, navigation EPs
│   ├── resolve/     # Resolution EPs
│   └── ...          # analysis, config, index, match, overrides, tools, util
src/main/resources/META-INF/
├── plugin.xml       # Entry point, includes pls-*.xml
└── pls-*.xml        # Feature registrations (split by domain)
cwt/                 # CWT config repositories (core + per-game)
```

## Code Patterns

### Manager (high-level, cached domain methods)

```kotlin
// ParadoxDefinitionManager.getInfo — cache-first pattern
fun getInfo(element: ParadoxDefinitionElement): ParadoxDefinitionInfo? {
    return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
        ProgressManager.checkCanceled()
        runReadActionSmartly {
            val file = element.containingFile
            val value = ParadoxDefinitionService.resolveInfo(element, file)
            val dependencies = ParadoxDefinitionService.getDependencies(element, file)
            value.withDependencyItems(dependencies)
        }
    }
}
```

### Service (lower-level, EP-driven resolve logic)

```kotlin
// ParadoxDefinitionService.resolveInfo — resolve with config group
fun resolveInfo(element: ParadoxDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
    val fileInfo = file.fileInfo ?: return null
    val gameType = fileInfo.rootInfo.gameType
    val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
    // ... match against config group
}
```

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Files (single class/object) | PascalCase | `ParadoxDefinitionManager.kt` |
| Files (package-level helpers) | `_extensions.kt`, `_accessors.kt`, `_models.kt` | `_extensions.kt` |
| Files (class-level helpers) | `XxxExtensions.kt`, `XxxAccessors.kt` | `ParadoxDefinitionExtensions.kt` |
| Files (EP implementations, grouped) | `SomeProviders.kt`, `SomeProviders.Category.kt` | `ParadoxHintTextProviders.kt` |
| Classes | PascalCase, domain prefix where needed | `ParadoxDefinitionInfo`, `CwtTypeConfig` |
| Prefixes (domain) | `Cwt`, `CwtConfig`, `Paradox` | — |
| Abstract class suffix | `Base` | `ParadoxScriptExpressionSupportBase` |
| Actions/Intentions/Inspections | Verb/adjective first, NO domain prefix | `CopyPathFromRootProvider`, `GoToPathAction` |
| Functions/variables | camelCase (standard) | `getInfo`, `resolveInfo` |
| Constants | `UPPER_SNAKE_CASE` | `EMPTY_OBJECT` |
| Factory methods | `PascalCase()` | `SomeFactoryMethod()` |
| Enum values | PascalCase or UPPER_SNAKE_CASE (no strict rule) | `Stellaris`, `GAME_TYPE` |

### Co-located impl pattern

If an implementation class does not need to be public, place it in the same file as the interface, wrapped in `// region ... // endregion`. See `icu.windea.pls.model.paths.ParadoxMemberPath` as a canonical example.

## Code Standards

- **Caching**: Prefer `CacheBuilder` (from `icu.windea.pls.core.util.CachesKt`); use `ConcurrentHashMap` only when insufficient
- **String interning**: Use `com.github.benmanes.caffeine.cache.Interner`
- **Cache strength**: Global caches → strong refs + size+TTL; large/config caches → soft refs
- **Indexing**: File-level analysis → `FileBasedIndex`; PSI-structure-dependent → `StubIndex`; ref-resolve-dependent → `FileBasedIndex`
- **Extension**: Extend via EP and config-driven mechanisms; avoid hardcoding game-specific behavior
- **Change scope**: Keep changes minimal and localized; prefer existing EPs over new ones
- **Config access**: `PlsFacade.getConfigGroup(project, gameType)` / `PlsFacade.getConfigGroup(gameType)`
- **Coroutine scope**: `PlsFacade.getCoroutineScope(project)` / `PlsFacade.getCoroutineScope()`
- **Config context**: `ParadoxConfigManager.getConfigContext(element)`
- **Matched configs**: `ParadoxConfigManager.getConfigs(element, options)`

## Documentation Standards

- **KDoc**: Default to Chinese; KDoc style
- **Inline comments**: Chinese or English based on context
- **KDoc type refs**: Use `[PsiElement]` link style
- **Method docs**: Prefer describing the method as a whole; avoid per-parameter docs unless truly necessary

## Testing Conventions

- **Unit tests**: Pure components/tools/extensions; no IntelliJ API
- **Integration tests**: PSI/index/semantic match/resolve; uses IntelliJ test framework
- **Parsing tests**: `ParsingTestCase` — PSI snapshot comparison
- **Fixture tests**: `BasePlatformTestCase` + `myFixture.configureByFile(...)`
- **Config-driven test setup**:
  ```kotlin
  @Before fun doSetUp() {
      markIntegrationTest()
      markRootDirectory("features/index")
      markConfigDirectory("features/index/.config")
      initConfigGroups(project, ParadoxGameType.Stellaris)
  }
  @After fun doTearDown() = clearIntegrationTest()
  ```
- **Test file naming**: `snake_case.test.txt` / `.test.yml` / `.test.cwt`
- **Config directory**: Place configs under `core/` or `<gameTypeId>/` subdir (e.g., `stellaris/`)
- **Targeted runs**: `./gradlew test --tests "*SomeKeyword*"`

## 📂 Codebase References

- **Manager pattern**: `src/main/kotlin/icu/windea/pls/lang/util/ParadoxDefinitionManager.kt:58`
- **Service pattern**: `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxDefinitionService.kt:82`
- **EP implementations**: `src/main/kotlin/icu/windea/pls/ep/`
- **Plugin registrations**: `src/main/resources/META-INF/pls-*.xml`
- **CWT configs**: `cwt/` (core + per-game repos)
- **Test data**: `src/test/testData/`
- **Build**: `build.gradle.kts`, `settings.gradle.kts`

## Related Files

- `business-domain.md` — Project purpose and domain context
- `business-tech-bridge.md` — Business needs → technical solutions mapping
- `decisions-log.md` — Major decisions with rationale
- `living-notes.md` — Active issues, open questions
