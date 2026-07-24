---
created: 2026-04-28
updated: 2026-04-28
---

## General

| Type                                | Convention                                      | Example                                      |
|-------------------------------------|-------------------------------------------------|----------------------------------------------|
| Files (single class/object)         | PascalCase                                      | `ParadoxDefinitionManager.kt`                |
| Files (package-level helpers)       | `_extensions.kt`, `_accessors.kt`, `_models.kt` | `_extensions.kt`                             |
| Files (class-level helpers)         | `SomeExtensions.kt`, `SomeAccessors.kt`         | `ParadoxDefinitionExtensions.kt`             |
| Files (EP implementations, grouped) | `SomeProviders.kt`, `SomeProviders.Category.kt` | `ParadoxHintTextProviders.kt`                |
| Classes                             | PascalCase, domain prefix where needed          | `ParadoxDefinitionInfo`, `CwtTypeConfig`     |
| Prefixes (domain)                   | `Cwt`, `CwtConfig`, `Paradox`                   | -                                            |
| Abstract class suffix               | `Base`                                          | `ParadoxScriptExpressionSupportBase`         |
| Actions/Intentions/Inspections      | Verb/adjective first, NO domain prefix          | `CopyPathFromRootProvider`, `GoToPathAction` |
| Functions/variables                 | camelCase (standard)                            | `getInfo`, `resolveInfo`                     |
| Constants                           | `UPPER_SNAKE_CASE`                              | `EMPTY_OBJECT`                               |
| Factory methods                     | `PascalCase()`                                  | `SomeFactoryMethod()`                        |
| Enum values                         | PascalCase or UPPER_SNAKE_CASE (no strict rule) | `Stellaris`, `GAME_TYPE`                     |

## Tips

- Prefer using prefix for language and domain specific class names (e.g., `Cwt...` `Paradox...` `ParadoxScript...`).
- prefer using verb form for actions and intentions (e.g, `CopyDefinitionNameIntention`).
- Prefer word-based or prefix-based abbreviations (e.g., for `scopeContext`: `context`, `sc` or just `c` is good, `ctx` is bad).
- Interfaces with a specific purpose: `*Aware` (aware of some domain model), `*Resolver` (semantic-level resolution of a domain model/config/expression), `*Scope` (utility/extension methods scoped to a specific component).
- EP implementation classes: prefer `[Domain][Layer][ImplementationType][Role]`, domain-first for natural grouping (e.g. `Domain` like `Cwt`/`Paradox`/`Stellaris`; `Layer` like `Base`/`Core`/`Default`; `Role` is the core part of the EP interface name, e.g. `ExpressionSupport`). For abstract classes, `Base` may instead go last (e.g. `ParadoxScriptExpressionSupportBase`).
- Interfaces with a specific purpose: `*Aware` (aware of some domain model), `*Resolver`/`*.Resolver` (semantic-level resolution of a domain model/config/expression), `*Scope` (utility/extension methods scoped to a specific component; prefer this over `*ImplExtensions`).
- `calculate` vs `evaluate`: prefer `evaluate` when parameter resolution/context (or dynamic game data) is involved, e.g. evaluating an inline math expression or a trigger; `calculate` otherwise.
- `call` vs `invoke`: both mean "execute a piece of code", but `call` leans static/direct (compile-time-known target, e.g. `scripted_trigger`/`scripted_effect`/`script_value`/`inline_script`), while `invoke` leans indirect/dynamic (reflection, delegates/callbacks, cross-thread dispatch, e.g. `event` invocation - also seen as `trigger`/`fire`).