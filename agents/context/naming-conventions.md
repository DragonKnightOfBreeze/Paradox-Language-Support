---
created: 2026-04-28
updated: 2026-04-28
---

## General

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

## Tips

- Prefer using prefix for language and domain specific class names (e.g., `Cwt...` `Paradox...` `ParadoxScript...`).
- Consider using prefix `Pls` for plugin specific class names (e.g., `PlsStates`).
- Prefer word-based or prefix-based abbreviations (e.g., for `scopeContext`: `context`, `sc` or just `c` is good, `ctx` is bad).