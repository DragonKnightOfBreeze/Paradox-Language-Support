# Appendix: Config Format Reference

<!-- TODO Manual improvement and polish -->

## Positioning & Vision {#vision}

This reference targets authors and maintainers who want to "understand / write / extend" CWT configs (CWT config files), and aims to:

- **Unify terminology and boundaries**: align semantics between PLS and CWTools, and clarify PLS extension points and differences.
- **Build a mapping from documentation to implementation**: each config item is annotated with the corresponding interfaces/resolvers so you can trace the source code and verify behavior.
- **Guide to practice**: outline purpose, format, and notes, laying the groundwork for refined examples and validation rules.

## Overview {#overview}

PLS reads `.cwt` files, builds "config groups", and parses configs into structured "config objects" used by language features (highlighting, completion, navigation, inspections, documentation, etc.).

- **Config sources and overriding**: see "Config Groups/Override Strategy" in `docs/en/config.md`. Common sources include built-in, remote, local, and project-local. Later ones override earlier ones by "path + config ID".
- **Two pillars**:
  - Configs: define allowed shapes and contexts for keys/values/blocks (e.g., types, enums, aliases, links ...).
  - Config expressions: describe the syntax used in string fields of configs for value/matching (e.g., `<type>`, `enum[...]`, `value[...]`, cardinality/template/location expressions).
- **Parsing flow (simplified)**:
  1. Read config groups and build PSI for config files.
  2. Use resolvers (by interface category) to build delegated/internal config objects.
  3. Language features query and apply these configs by context (scope, type name, declaration context, etc.).

Terminology:
- "Config(s)" include base configs, normal configs, extended configs, and internal configs.
- "Base configs" (e.g., `CwtPropertyConfig`) are generic syntax-tree-level nodes and are not described one by one here.

<!-- @see icu.windea.pls.config.configGroup.CwtConfigGroup -->
<!-- @see icu.windea.pls.config.config.CwtPropertyConfig -->
<!-- @see icu.windea.pls.config.config.delegated.* -->
<!-- @see icu.windea.pls.config.config.delegated.impl.* -->

## Configs {#configs}

<!-- @see icu.windea.pls.config.config -->

> This chapter introduces the purpose, key format points, and parsing notes for various configs to help you understand and write these special structures correctly.

### Normal Configs {#configs-normal}

> These configs drive various language features, including but not limited to code completion, code inspection, quick documentation, inlay hints, etc.

#### Priority Configs {#config-priority}

<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideStrategy -->
<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideService -->
<!-- @see cwt/core/priorities.core.cwt -->

Priority configs are used to configure how targets are overridden.

- **Purpose**: provide a unified strategy for overriding/merging of "targets", affecting the order of effect and the sort order of non-stream queries.
- **Applicable targets**: files, global scripted variables, definitions, localisations, etc.
- **Default value**: when no directory mapping matches, `LIOS` (last-in wins) is used.

**Override strategies and behavior**:
- `FIOS` (First In, Only Served): Read only once. The first-loaded one takes effect, and subsequent ones will be just ignored.
- `LIOS` (Last In, Only Served): Later reads override. The last loaded overrides the first loaded.
- `DUPL` (Duplicates): Whole-file override. Must be overridden entirely by a file with the same path.
- `ORDERED` (Ordered): Sequential reading. Existing ones cannot be overwritten, and later loaded ones will be sequentially added or merged.

**Sorting and loading notes**:
- Sort order of non-stream query results is driven by priority; under the same path, load order (game/dependency chain) determines the precedence.
- Within the same file, later items override earlier ones.
- See `ParadoxPriorityProvider.getComparator()` for the implementation and defaults.

**Format notes**:

```cwt
priorities = {
    # LHS - file path of containing directory, relative to entry directory
    # RHS - used override strategy
    # entry directory - normally game or mod directory, or `game` subdirectory of game directory
    # override strategy - available values: `fios`, `lios`, `dupl`, `ordered`; default: `lios`; ignore case

    "events" = fios
    # ...
}
```

**Examples**:

```cwt
priorities = {
    "common/event_chains" = fios
    "common/on_actions" = ordered
    "common/scripted_variables" = fios
    "events" = fios
}
```

- Two mods both define an event with the same name under `events/`: because `events = fios`, the mod read earlier (loaded earlier) takes effect and the later one is ignored.
- Two mods both add entries under `common/on_actions/`: because `ordered`, they will be merged in order without overriding.

#### Declaration Configs {#config-declaration}

<!-- @see icu.windea.pls.config.config.delegated.CwtDeclarationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDeclarationConfigResolverImpl -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator -->
<!-- @see icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider -->
<!-- @see icu.windea.pls.ep.config.CwtInjectedConfigProvider -->

- **Purpose**: declare the structure of a "definition entry" for completion, inspections, quick documentation, etc.
- **Path location**: `{name}`, where `{name}` is the config name (i.e., the "definition type"). A top-level property whose key is a valid identifier and is not matched by other configs falls back to being parsed as a declaration config.
- **Dependent context**: `CwtDeclarationConfigContextProvider` constructs the declaration context (definition name, type, subtype). Game Rule/On Action can rewrite the context via extended configs.

- **Parsing flow (implementation summary)**:
  1. Parse the name: skip if the key is not a valid identifier (`CwtDeclarationConfigResolverImpl`).
  2. Root-level inlining: if RHS is `single_alias_right[...]`, expand it into normal property configs first (`CwtConfigManipulator.inlineSingleAlias`).
  3. Build the final config tree:
     - Deep-copy and trim/flatten by subtype (`deepCopyConfigsInDeclarationConfig`).
     - If a `subtype[...]` matches the context subtype: flatten its children; if not, skip; non-`subtype[...]` nodes recurse normally.
     - Inject derived configs (`CwtInjectedConfigProvider.injectConfigs`) and uniformly set `parentConfig` to keep the parent chain.
  4. Subtype cache key: scan `subtype[...]` to collect used subtype set (`subtypesUsedInDeclaration`), and combine it with the current context to build the cache key, avoiding cache invalidation by irrelevant subtypes.

- **Cooperation with other configs**:
  - Can reference aliases and single-aliases inside a declaration (`alias_name[...]`/`alias_match_left[...]`, `single_alias_right[...]`).
  - Swapped-type declarations can be nested directly under the base type declaration.

**Example**:

```cwt
event = {
    id = scalar

    # Refine the structure by subtype; only takes effect under the matching subtype
    subtype[triggered] = {
        ## cardinality = 0..1
        weight_multiplier = {
            factor = float
            alias_name[modifier_rule] = alias_match_left[modifier_rule]
        }
    }

    ## cardinality = 0..1
    # The root-level single alias will be inlined before parsing
    trigger = single_alias_right[trigger_clause]
}
```

**Notes**:
- `subtype[...]` only takes effect when it matches the context subtype; otherwise it is ignored (no errors).
- Root-level `single_alias_right[...]` is expanded first and then participates in subsequent parsing and inspections.
- To ensure upward tracing in later features, all newly added nodes will have `parentConfig` (parent pointer) injected.

#### System Scope Configs {#config-system-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl -->
<!-- @see cwt/core/system_scopes.core.cwt -->

- **Purpose**: provide metadata for built-in "system-level scopes" (This/Root/Prev/From, etc.) for quick documentation and scope stack derivation.
- **Path location**: `system_scopes/{id}`, where `{id}` is the system scope ID.
- **Fields**:
  - `id`: system scope ID.
  - `base_id`: base scope ID; defaults to `id` if unspecified. Used to categorize scope families (e.g., `Prev*`, `From*`) for display and documentation.
  - `: string`: human-readable name; defaults to `id` if unspecified.

- **Parsing flow (implementation summary)**:
  - Read `id = key`, `base_id = properties['base_id'] ?: id`, and `name = stringValue ?: id` (`CwtSystemScopeConfigResolverImpl`).
  - Equality is compared by `id` (same `id` means the same system scope).

- **Cooperation with other configs**:
  - Works together with "Scopes and Scope Groups" to drive scope checks and hints.
  - Some extended configs may use the option `replace_scopes` to map system scopes to concrete scope types under the current context (e.g., map `this/root/from` to `country`).
  - Note: `replace_scopes` does not support replacing the `prev` series of system scopes (`prev/prevprev/...`); see "How to specify scope context in config files" in `docs/en/config.md`.

**Example (built-in)**:

```cwt
system_scopes = {
    This = {}
    Root = {}
    Prev = { base_id = Prev }
    From = { base_id = From }
    # Chain members like PrevPrev/FromFrom are omitted
}
```

#### Directive Configs {#config-directive}

<!-- @see icu.windea.pls.config.config.delegated.CwtDirectiveConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDirectiveConfigResolverImpl -->
<!-- @see cwt/cwtools-stellaris-config/config/common/inline_scripts.cwt -->
<!-- @see cwt/cwtools-vic3-config/config/common/definition_injections.cwt -->
<!-- @see cwt/cwtools-eu5-config/config/common/definition_injections.cwt -->

Used to describe special expressions and structures in script files that are different from general abstractions, and provide additional metadata for hints and validation.
These expressions and structures change the behavior of the script parser at game runtime, allowing you to modify, extend, or reuse existing script snippets.
Different directives can have different config structures.

Language features currently involved:
- **Inline_script**: (Sellaris) will be replaced by the content of the target file during the parsing phase, and arguments can be specified.
- **definition_injection**: (VIC3/EU5) will inject or replace The declaration of the target definition during the parsing phase, and the mode can be specified to determine the specific behavior.

**Path location**: `directive[{name}]`, where `{name}` is the config name.

**Example**：

```cwt
directive[inline_script] = {
    # ...
}
```

#### Type Configs and Subtype Configs {#config-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSubtypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtTypeConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtSubtypeConfigResolverImpl -->

- **Purpose**: locate and name "definitions" by file path/key, and optionally declare subtypes, presentation and images.
- **Path location**:
  - Type: `types/type[{type}]`, where `{type}` is the definition type name.
  - Subtype: `types/type[{type}]/subtype[{subtype}]`.

- **File matching and sources**:
  - `path`/`path_file`/`path_extension`/`path_pattern`/`path_strict` together determine which files are scanned.
  - Paths are normalized by removing the `game/` prefix and unifying separators; `path_extension` should not include the dot (e.g., `.txt` -> `txt`).
  - `type_per_file` means "one type instance per file".

- **Definition key constraints**:
  - `type_key_prefix` specifies the key prefix; and it provides a corresponding raw-value config (`typeKeyPrefixConfig`) for rendering and hints.
  - `type_key_filter`/`type_key_regex`/`starts_with` constrain the allowed "definition key" values; `skip_root_key` allows skipping some top-level keys to continue matching (case-insensitive, supports multiple groups).
  - `possibleTypeKeys` is computed from type/subtype filters to support completion and validation.

- **Naming and uniqueness**:
  - `name_field` specifies the source field for display name; `name_from_file` means derive the name from the filename; `unique` is used for conflict checks/navigation hints; `severity` marks the display severity level.

- **Subtypes**:
  - Options: `type_key_filter`, `type_key_regex`, `starts_with`, `only_if_not`, `group`.
  - Matching is trimmed in declaration order; usually used together with `subtype[...]` in declaration configs to refine structure and checks.

- **Presentation**:
  - `localisation`/`images` subsections configure display texts and images for a type.

- **Parsing flow (implementation summary)**:
  1. Parse `type[...]` name and required properties; if required fields are missing, skip this type (`CwtTypeConfigResolverImpl`).
  2. Collect file sources and key constraints, build subtype mapping, and parse presentation settings.
  3. Merge `modifiers`: if `modifiers` is declared inside a type config, derive modifier configs and write them into `configGroup.modifiers` and `type2ModifiersMap` (grouped by `type` or `type.subtype`).
  4. Compute `possibleTypeKeys`, and bind a tag type (`CwtTagType.TypeKeyPrefix`) to `type_key_prefix` when needed.

- **Cooperation with other configs**:
  - Works with `Declaration` to provide context and structural constraints for concrete definitions.
  - Works with `Modifier/Modifier Category`, deriving type-related modifier configs via `modifiers`.

**Example**:

```cwt
types = {
    type[civic_or_origin] = {
        # File sources
        path = "game/common/governments/civics"   # the prefix `game/` will be removed automatically
        path_extension = .txt

        # Key constraints and prefix
        type_key_prefix = civic_
        ## type_key_filter = { +civic_ -origin_ }  # include/exclude sets
        ## starts_with = civic_
        ## skip_root_key = { potential }

        # Subtype
        subtype[origin] = {
            ## type_key_filter = +origin_
            ## group = lifecycle
        }

        # Presentation
        localisation = { name_field = name }
        images = { main = icon }
    }
}
```

**Notes**:
- Missing any required property will cause the type to be skipped (a log entry will be emitted).
- `path` and `path_pattern` can be used together; `path_strict` enforces strict matching.
- `skip_root_key` is a multi-group setting: if any group matches the sequence of top-level keys in the file, the matcher can skip them and continue to match the definition key.
- Subtype matching is order-sensitive; place more specific configs earlier.

#### Alias Configs and Single Alias Configs {#config-alias}

<!-- @see icu.windea.pls.config.config.delegated.CwtAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSingleAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtAliasConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtSingleAliasConfigResolverImpl -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.inlineAlias -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.inlineSingleAlias -->

- **Purpose**: abstract reusable config snippets as named aliases that can be referenced and expanded in multiple places; a single-alias is for one-to-one reuse on the value side.
- **Path location**:
  - Alias: `alias[{name}:{subName}]` (`{subName}` is a constrained data expression).
  - Single alias: `single_alias[{name}]`.

- **Syntax and reference**:
  - Declare an alias: `alias[effect:some_effect] = { ... }`
  - Use an alias: write `alias_name[effect] = alias_match_left[effect]` at the use site
  - Declare a single alias: `single_alias[trigger_clause] = { alias_name[trigger] = alias_match_left[trigger] }`
  - Use a single alias: `potential = single_alias_right[trigger_clause]`

- **Option semantics (alias)**:
  - `scope/scopes`: allowed input scope set (`supportedScopes`).
  - `push_scope`: output scope (`outputScope`).
  - `subName` supports a constrained data expression and is parsed as `subNameExpression`; it also serves as `configExpression` for matching and hints.

- **Parsing and inlining (implementation summary)**:
  - Parse `name`/`subName` from the key (`CwtAliasConfigResolverImpl`).
  - Expand at use site: `CwtConfigManipulator.inlineAlias` copies the alias body as normal property configs:
    - After expansion the key equals subName (`key = subName`), and the value/children are deep-copied while keeping options.
    - If the expanded RHS is `single_alias_right[...]`, it will continue to inline the single-alias (cascading expansion).
    - After expansion, the result participates in injection (`CwtInjectedConfigProvider.injectConfigs`) and parent pointer backfill, then enters regular validation/completion flows.
  - Single alias expands on the value side: `CwtConfigManipulator.inlineSingleAlias` replaces the entire declaration into the value and child block at the use site.

- **Cooperation with other configs**:
  - Often used with "Declaration" to reuse trigger/effect snippets inside definition declarations.
  - Works with "Types and Subtypes" as part of modifier configs or context constraints.

**Example**:

```cwt
# Alias: define an effect snippet
alias[effect:apply_bonus] = {
    add_modifier = {
        modifier = enum[modifier_rule]
        days = int
    }
}

# Use alias in scripts
scripted_effect = {
    alias_name[effect] = alias_match_left[effect]
}

# Single alias: define a trigger-block snippet
single_alias[trigger_clause] = {
    alias_name[trigger] = alias_match_left[trigger]
}

# Use single-alias on value side in a declaration
some_definition = {
    ## cardinality = 0..1
    potential = single_alias_right[trigger_clause]
}
```

**Notes**:
- The unique key of an alias is `name:subName`; duplicates are processed by the overriding strategy/priority.
- Cardinality and option checks happen after expansion; consider the final semantics at the use site rather than the declaration.
- `subName` is a constrained data expression; you can use templates/enums to increase reuse, but avoid being too broad and causing mismatches.

#### Enum Configs and Complex Enum Configs {#config-enum}

<!-- @see icu.windea.pls.config.config.delegated.CwtEnumConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtComplexEnumConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtEnumConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtComplexEnumConfigResolverImpl -->

- **Purpose**: provide value sets for the data expression `enum[...]`.
  - Simple enum: a fixed set of values, all declared in config files.
  - Complex enum: dynamically collect enum values from script files by path/anchors.

- **Path location**:
  - Simple: `enums/enum[{name}]`
  - Complex: `enums/complex_enum[{name}]`

---

**Simple Enum**:

Fields and implementation:
- `name`: enum name.
- `values`: candidate set (case-insensitive).
- `valueConfigMap`:
- The current implementation supports constant values only; template expressions are not supported.

Declaration:

```cwt
enums = {
    enum[weight_or_base] = { weight base }
}
```

---

**Complex Enum**:

**Matching Logic**:
- The combination of `path`/`path_file`/`path_extension`/`path_pattern`/`path_strict` determines the set of files to be scanned.
- `path` and `path_extension` are normalized during resolving.
- `start_from_root`: Whether to start searching for anchors from the top of the file (rather than from top-level properties).
- `## case_insensitive`: (PLS extension) Whether to mark complex enum values as case-insensitive.
- `## per_definition`: (PLS extension) Whether to restrict the equivalence of complex enum values with the same name and type to the definition level, rather than the file level.
- `name` subsection: Describes how to locate value anchors within matching files; the implementation collects all properties or values named `enum_name` within it as anchors (`enumNameConfigs`).

**Parsing flow (brief)**:
1. Simple: parse `enum[...]` and its value list; build a case-insensitive value set and mapping (`CwtEnumConfigResolverImpl`).
2. Complex: parse file sources and the `name` subsection and anchors; during indexing, collect actual values (`enum_name`) from matched files.
3. Both serve completion and validation of `enum[...]`.

Declaration (example):

```cwt
enums = {
    complex_enum[component_tag] = {
        path = "game/common/component_tags"
        start_from_root = yes
        name = {
            enum_name
        }
    }
}
```

**Notes**:
- Simple enums currently support constant values only; if you write a template expression, it will not be parsed as a template.
- A complex enum without a `name` subsection or without any `enum_name` anchors found in matched files will result in an empty enum.
- Simple enum values are case-insensitive by default, and complex enum values are case-sensitive by default.

#### Dynamic Value Type Configs {#config-dynamic-value}

<!-- @see icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDynamicValueTypeConfigResolverImpl -->

- **Purpose**: provide predefined (hard-coded) dynamic value sets for the data expression `value[...]`, as an alternative to fixed literals, for completion and validation.
- **Path location**: `values/value[{name}]`, where `{name}` is the dynamic value type name.

- **Fields and limits**:
  - `name`: dynamic value type name.
  - `values`: value set (case-insensitive).
  - `valueConfigMap`: mapping from value to its value config.
  - The current implementation supports constant values only; template expressions are not supported.

- **Parsing flow (implementation summary)**:
  - Parse the `value[...]` name and the value list, build a case-insensitive set and mapping (`CwtDynamicValueTypeConfigResolverImpl`).
  - Used by `value[...]` during completion and validation.

- **Relation to extended configs**:
  - If you need to declare scope context (e.g., only accepts a push scope) or generate values dynamically by context, refer to the extended config "Dynamic Value (Extended)".

**Example**:

```cwt
values = {
    value[event_target] = { owner capital }  # case-insensitive
}
```

#### Link Configs {#config-link}

<!-- @see icu.windea.pls.config.config.delegated.CwtLinkConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl -->

- **Purpose**: provide semantics and type (scope/value) constraints for "field/function-like" nodes in complex expressions, supporting chained access, completion, and inspections.
- **Path location**:
  - Regular links: `links/{name}`
  - Localisation links: `localisation_links/{name}` (if not declared explicitly, static regular links are copied automatically)

- **Static vs Dynamic**:
  - Static link: no `data_source`, represents a fixed node name (e.g., `owner`).
  - Dynamic link: declares `data_source` and/or `prefix`/`from_*`, and can carry dynamic data (e.g., `modifier:x`, `relations(x)`, `var:x`).

- **Fields and semantics (implementation)**:
  - `type`：link type (`scope`/`value`/`both`, default to `scope`).
  - `from_data`：whether to read dynamic data from text data (format `prefix:data`).
  - `from_argument`：whether to read dynamic data from arguments (format `func(arg)`).
  - `argument_separator`：the argument separator to use when there are multiple arguments (`comma`/`pipe`, default to `comma`).
  - `prefix`: prefix for dynamic links; when `from_argument = yes`, the parser removes a trailing colon to avoid `prefix:` duplication.
  - `data_source` (multiple): each is a data expression that constrains legal values of dynamic data, supporting multi-argument scenarios.
  - `input_scopes`: input scope set; both `input_scope` and `input_scopes` are accepted by the resolver.
  - `output_scope`: output scope; empty means passthrough/derived from data source.
  - `for_definition_type`: only available under the specified definition type.

- **Parsing flow (implementation summary)**:
  - Read fields and normalize: scope IDs are normalized via `ParadoxScopeManager.getScopeId()`.
  - Validation: when `from_data` or `from_argument` is `yes`, at least one `data_source` must exist.
  - Build data expressions: parse `CwtDataExpression` for each `data_source`, supporting multiple parameters (use `delegatedWith(index)` to specify the current parameter when needed).
  - Localisation links: can be copied from regular links (static) or parsed separately.

**Example**:

```cwt
links = {
    # Static scope link
    owner = {
        input_scopes = { any }
        output_scope = any
    }

    # Dynamic value link (with prefix)
    modifier = {
        type = value
        from_data = yes
        prefix = modifier
        data_source = dynamic_value[test_flag]
        input_scopes = { any }
    }

    # Dynamic scope link (function-like)
    relations = {
        from_argument = yes
        data_source = <country>           # multiple data sources can be mixed
        data_source = dynamic_value[test_flag]
        input_scopes = { country }
        # empty output_scope -> derived based on data source and implementation
    }
}
```

**Notes**:
- `prefix` should not contain quotes or parentheses; `input_scopes` uses a brace-enclosed set syntax (e.g., `{ country }`).
- Multiple `data_source` entries are allowed; for multi-argument links, use `delegatedWith(index)` to switch the current parameter expression.
- If the dynamic link argument is a single-quoted literal, treat it as a literal; generally no completion is provided.
- Prefer the `<type>` shorthand in `data_source` (e.g., `<country>`) over `definition[country]`.

#### Scope Configs and Scope Group Configs {#config-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtScopeConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtScopeGroupConfigResolverImpl -->

- **Purpose**: define "scope types" and their aliases (`scopes`), and group scopes (`scope_groups`) for scope checks, chaining constraints, and hints.

- **Path location and fields**:
  - Scope: `scopes/{name}`
    - `name`: scope ID.
    - `aliases: string[]`: alias set (case-insensitive).
  - Scope group: `scope_groups/{name}`
    - `name`: group name.
    - `: string[]`: scope IDs in this group (case-insensitive).

**Example**:

```cwt
scopes = {
    Country = { aliases = { country } }
}

scope_groups = {
    target_species = {
        country pop_group leader planet ship fleet army species first_contact
    }
}
```

**Cooperation with other configs**:
- Works with "System Scopes" to determine scope stacks and meanings; works with "Links" to constrain input/output scopes for chaining.
- In extended configs, you can specify `replace_scopes` to map system scopes to concrete scope types under specific contexts.

#### Modifier Configs and Modifier Category Configs {#config-modifier}

<!-- @see icu.windea.pls.config.config.delegated.CwtModifierConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtModifierConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtModifierCategoryConfigResolverImpl -->

- **Purpose**: declare modifiers and their categories, used for icon rendering, completion, and scope validation.

- **Path location**:
  - Modifier:
    - `modifiers/{name}` (`{name}` can be a constant or a template expression)
    - `types/type[{type}]/modifiers/{name}` (where `$` is replaced with `<{type}>`)
    - `types/type[{type}]/modifiers/subtype[{subtype}]/{name}` (use `{type}.{subtype}` as the type expression for replacement)
  - Modifier category: `modifier_categories/{name}`

- **Fields and semantics (modifier)**:
  - `name`: templated name (e.g., `job_<job>_add`), supporting dynamically generated modifiers.
  - `categories: string | string[]`: category names; determine the allowed scope types.
  - `supportedScopes`: allowed scope set.
    - If `categoryConfigMap` is resolved, derive scopes from categories (`ParadoxScopeManager.getSupportedScopes(...)`).
    - Otherwise, fall back to the modifier's local option `supported_scopes` (if present).

- **Fields and semantics (modifier category)**:
  - `name`: category name (e.g., `Pops`).
  - `supported_scopes: string | string[]`: allowed scope set of this category.

- **Parsing flow (implementation summary)**:
  - Modifier (`CwtModifierConfigResolverImpl`):
    1. Parse `categories` from value or value-list; skip if missing.
    2. If from a type config's `modifiers`, replace `$` in `name` with `<{typeExpression}>`, where `typeExpression` is `type` or `type.subtype`.
    3. Parse template expressions and compute `supportedScopes` (from categories or local options).
  - Modifier category (`CwtModifierCategoryConfigResolverImpl`):
    1. Parse `name` and `supported_scopes`.

- **Cooperation with other configs**:
  - Works with `Types and Subtypes` via `modifiers` to derive type-bound modifier configs.
  - Cooperates with `Scopes/Links/System Scopes` for scope checks and hints.

**Example**:

```cwt
# Independent modifiers
modifiers = {
    pop_happiness = { Pops }
    job_<job>_add = { Planets }
}

# Modifiers declared in type configs (will derive templated names)
types = {
    type[job] = {
        modifiers = {
            job_$_add = { Planets }   # -> job_<job>_add
        }
    }
}

# Modifier categories
modifier_categories = {
    Pops = { supported_scopes = { species pop_group planet } }
}
```

**Notes**:
- A modifier entry without `categories` will be skipped (ineffective).
- For modifier names under type configs, use `$` placeholders consistent with type/subtype expressions.
- `supported_scopes` in categories should use standard scope IDs; parsing will normalize case automatically.

#### Localisation Command Configs and Localisation Promotion Configs {#config-localisation}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocalisationCommandConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocalisationPromotionConfigResolverImpl -->

- **Purpose**: declare the availability and allowed scopes of localisation command fields (Get...), and declare localisation scope promotions to keep command fields available after switching scope via localisation links.

- **Path location**:
  - Localisation command: `localisation_commands/{name}` (`{name}` is case-insensitive)
  - Localisation promotion: `localisation_promotions/{name}` (`{name}` is case-insensitive; corresponds to a localisation link name)

- **Fields and semantics**:
  - Command: `supported_scopes: string | string[]` (allowed scope types)
  - Promotion: `supported_scopes: string | string[]` (allowed scopes after promotion)

- **Parsing flow (implementation summary)**:
  - Command (`CwtLocalisationCommandConfigResolverImpl`): parse name (case-insensitive) and `supported_scopes`.
  - Promotion (`CwtLocalisationPromotionConfigResolverImpl`): parse name (case-insensitive, match localisation link name) and `supported_scopes`.
  - In localisation texts, after switching scope via a localisation link, use the promotion configs to determine which command fields are valid.

**Example**:

```cwt
localisation_commands = {
    GetCountryType = { country }
}

localisation_promotions = {
    Ruler = { country }
}

localisation_links = {
    ruler = { ... }
}

# In localisation text:
# [Ruler.GetCountryType] is valid under the promoted scope after Ruler link
```

**Notes**:
- Names are case-insensitive; keep the spelling style consistent with actual command fields to improve searchability.
- Promotion names should match localisation link names; otherwise they won't match correctly.
- When cooperating with "Links (Localisation Links)", static regular links are copied automatically as localisation links; for dynamic behavior, declare localisation links separately.

#### Type Presentation Configs {#config-type-presentation}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeImagesConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtTypeLocalisationConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtTypeImagesConfigResolverImpl -->

- **Purpose**: configure name/description/required localisation keys and main images/splitting configs for a definition type, for UI, navigation, and hints.
- **Path location**:
  - Localisation: `types/type[{type}]/localisation`
  - Images: `types/type[{type}]/images`

- **Fields and semantics**:
  - Both share the same structure: pairs of "subtype expression + location configs" (`locationConfigs`).
  - At runtime, filter and merge by the actual set of definition subtypes to obtain the final config list (`getConfigs(subtypes)`).
  - For location configs, see "Location and Row Matching" → `CwtLocationConfig`. Common options:
    - `required`: whether this item is mandatory (report hint/error if missing).
    - `primary`: whether this item is the primary one (e.g., for main icon/name).
  - For location expressions, see "Config Expressions → Location Expression".

**Example**:

```cwt
types = {
    type[ship_design] = {
        localisation = {
            ## primary
            name = some_loc_key
            subtype[corvette] = { name = some_corvette_loc_key }
        }
        images = {
            ## primary ## required
            icon = "icon|icon_frame"  # image location expression; supports frame and name path parameters
        }
    }
}
```

#### Database Object Type Configs {#config-db-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDatabaseObjectTypeConfigResolverImpl -->

- **Purpose**: define types and formats for "database object expressions" in localisation (e.g., `['civic:some_civic', ...]`), enabling resolution to definitions or localisation in UI and hints.
- **Path location**: `database_object_types/{name}`, where `{name}` is the prefix (e.g., `civic`).

- **Fields and semantics**:
  - `type`: if present, for `prefix:object`, treat `object` as a definition reference of that type.
  - `swap_type`: if present, for `prefix:object:swap`, treat `swap` as a definition reference of the "swapped type".
  - `localisation`: if present, for `prefix:object`, treat `object` as a localisation key.

**Example**：

```cwt
database_object_types = {
    civic = {
        type = civic_or_origin
        swap_type = swapped_civic
    }
}
```

#### Location Configs {#config-location}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl -->

- **Purpose**: Declares the location key and location expression for resources such as images/localization.
- **Path Location**: `types/type[{type}]/localisation/{key}` and `types/type[{type}]/images/{key}`.

#### Row Configs {#config-row}

<!-- @see icu.windea.pls.config.config.delegated.CwtRowConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtRowConfigResolverImpl -->

- **Purpose**: Declares column names and value types for CSV rows, used for completion/validation.
- **Path Location**: `rows/row[{name}]`.

**Matching Logic**:
- The combination of `path`/`path_file`/`path_extension`/`path_pattern`/`path_strict` determines the set of files to be scanned.
- `path` and `path_extension` are normalized during parsing.
- `columns` (column name → column config), `end_column` (termination column name, columns after a match are considered optional trailing columns).

**Example**:

```cwt
rows = {
    row[component_template] = {
        path = "game/common/component_templates"
        path_extension = .csv
        columns = {
            key = <component_template>
            # ... other columns
        }
    }
}
```

#### Locale Configs {#config-locale}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocaleConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocaleConfigResolverImpl -->

- **Purpose**: declare basic information about locales to recognize project/user-preferred locales and improve UI display and localisation validation.
- **Path location**: `locales/{id}`, where `{id}` is like `l_english`.

- **Fields and semantics**:
  - `id`: locale ID.
  - `codes: string[]`: language codes included in the locale (e.g., `en`, `en-US`).
  - Derived fields: `shortId` (prefix `l_` removed), `idWithText` (with display text).
  - Resolver extra capability: can auto-detect by IDE/OS or provide fallback (internal use).

**Example**:

```cwt
locales = {
    l_english = { codes = { "en" } }
    l_simp_chinese = { codes = { "zh-CN" } }
}
```

### Extended Configs {#configs-extended}

> PLS-extended family of configs to enhance IDE features (quick documentation, inlay hints, completion, etc.).

#### Extended Scripted Variable Configs {#config-extended-scripted-variable}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedScriptedVariableConfigResolverImpl -->

- **Purpose**: provide extra hints (quick doc, inlay hints, etc.) for scripted variables.
- **Path location**: `scripted_variables/{name}`.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `name`: variable name or its matching pattern.
  - `hint: string?`: optional extra hint text (for inlay hints or documentation).

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedScriptedVariableConfigResolverImpl`).
  - Option extraction: read hint text from `hint` option if present.
  - Application: match against scripted variable references in scripts by name/pattern, injecting docs and hints into the UI.

**Format**:

```cwt
scripted_variables = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## hint = §RSome inlay hint text§!
    x
}
```

**Example**:

```cwt
scripted_variables = {
    ### Some documentation
    ## hint = §RSome hint text§!
    x # or write `x = 1`
}
```

**Notes**:
- Name can use template/ANT/regex patterns; avoid overly broad patterns that cause mismatches.
- This entry only provides hint enhancements; it does not declare or validate the value/type of scripted variables.

#### Extended Definition Configs {#config-extended-definition}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedDefinitionConfigResolverImpl -->

- **Purpose**: provide extra context and hint information for concrete definitions.
  - Use cases: docs/hints (`hint`), bind definition type (`type` required), and optionally specify scope context via `replace_scopes`/`push_scope`.
- **Path location**: `definitions/{name}`.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `name`: definition name or its matching pattern.
  - `type: string` (required): the definition type this extended item targets. Missing value causes the item to be skipped.
  - `hint: string?`: optional hint text (for quick docs/inlay hints).
  - Scope context (options):
    - `replace_scopes`: rewrite system scope mapping, e.g., `{ this = country root = country }`.
    - `push_scope`: declare output scope, used by chaining/inspections.

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedDefinitionConfigResolverImpl`).
  - Required check: missing `type` will skip this item and log a warning.
  - Option extraction: read `hint`; `replace_scopes`/`push_scope` are parsed by common option parsers and used by later context building.

**Format**:

```cwt
definitions = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    
    ### Some documentation
    ## type = civic_or_origin.civic
    x
    
    # since 1.3.5, scope context related options are also available here
    ## type = scripted_trigger
    ## replace_scopes = { this = country root = country }
    x
}
```

**Example**:

```cwt
definitions = {
    ### Some documentation
    ## hint = §RSome hint text§!
    ## replace_scopes = { this = country root = country }
    ## type = scripted_trigger
    x # or `x = ...`
}
```

**Notes**:
- `type` is required; missing it will skip the item (ineffective).
- The name can use template/ANT/regex patterns; avoid overly broad matches that cause false positives.
- This extension provides documentation/context enhancements only; it does not directly change the declaration structure. Cooperation with the declaration happens at use sites during context building and inspections/documentation.

#### Extended Game Rule Configs {#config-extended-game-rule}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedGameRuleConfigResolverImpl -->

- **Purpose**: provide docs/hints for game rules (definitions of type `game_rule`) and optionally override declaration configs.
- **Path location**: `game_rules/{name}`.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `name`: game rule name or its matching pattern.
  - `hint: string?`: optional hint text (for quick docs/inlay hints).
  - `configForDeclaration: CwtPropertyConfig?`: if the current item is a property node, its value/children will be used as an "override declaration" at the use site; if the value is `single_alias_right[...]`, it will be inlined first.

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedGameRuleConfigResolverImpl`).
  - `configForDeclaration`: only valid when it is a property node; call `inlineSingleAlias(...)` on the value if applicable, otherwise use the original value.
  - Use-site cooperation: `GameRuleCwtDeclarationConfigContextProvider` overrides the declaration with `configForDeclaration` when this extended config matches, and it has child configs.

**Format**:

```cwt
game_rules = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to override declaration config
    
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    x
}
```

**Example**:

```cwt
game_rules = {
    ### Some documentation
    ## hint = §RSome hint text§!
    x # provide hint only

    y = single_alias_right[trigger_clause] # override declaration via single alias
}
```

**Notes**:
- Only property nodes produce `configForDeclaration` and participate in overriding; pure value nodes do not.
- If the value is `single_alias_right[...]`, it will be inlined before being used as an override.
- This extension only affects the source/structure of declaration and the hints; it does not change global priority or override strategies.

#### Extended On Action Configs {#config-extended-on-action}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedOnActionConfigResolverImpl -->

- **Purpose**: provide docs/hints for specific `on_action` definitions and specify `event_type` to influence event-related references in the declaration context.
- **Path location**: `on_actions/{name}`.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `name`: on_action name or its matching pattern.
  - `event_type: string` (required): event type. Used to substitute event-related data expressions (e.g., `<event>`) in the declaration context with the expression for that event type.
  - `hint: string?`: optional hint text (for quick docs/inlay hints).

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedOnActionConfigResolverImpl`).
  - Required check: missing `event_type` will skip this item and log a warning.
  - Use-site cooperation: after matching this extended config, `OnActionCwtDeclarationConfigContextProvider` substitutes event placeholders in the declaration context with the specified `event_type` to drive completion/inspections and quick docs.

**Format**:

```cwt
on_actions = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## replace_scopes = { this = country root = country }
    ## event_type = country
    x
}
```

**Example**:

```cwt
on_actions = {
  ### Some documentation
  ## hint = §RSome hint text§!
  ## event_type = country
  x
}
```

**Notes**:
- `event_type` is required; missing it makes the item ineffective.
- Name can use template/ANT/regex patterns; avoid overly broad patterns.
- If scope replacement is needed, combine with general options (e.g., `replace_scopes`), but whether it participates in specific checks depends on the use-site context and feature implementation.

#### Extended Inline Script Configs {#config-extended-inline-script}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedInlineScriptConfigResolverImpl -->

- **Purpose**: declare context config(s) and scope context for specific inline scripts so that completion/validation at call sites is correct.
- **Path location**: `inline_scripts/{name}`.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).
- **File mapping**: when `name` is `x/y`, the file is `common/inline_scripts/x/y.txt`.

- **Fields and semantics**:
  - `name`: inline script name or its matching pattern.
  - `context_configs_type: string = single | multiple` (default `single`): aggregation shape of context configs.
    - `single`: take value (`value`) as the context config.
    - `multiple`: take child configs list (`configs`) as the context configs.
  - Scope context (options):
    - `replace_scopes`: rewrite system scope mappings.
    - `push_scope`: declare output scope.

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedInlineScriptConfigResolverImpl`).
  - Container config: if it is a property node, first apply `inlineSingleAlias(...)` to its value (supports root-level single alias) to get the container config (`getContainerConfig()`).
  - Context configs:
    - If `context_configs_type = multiple`, take the container config's child configs; otherwise take the container config's value config.
    - Wrap into a consumable "context configs container" via `inlineWithConfigs(...)` (`getContextConfigs()` returns a single-element list).

**Format**:

```cwt
inline_scripts = {
    # 'x' or 'x = xxx'
    # 'x' is an inline script expression, e.g., for 'inline_script = jobs/researchers_add', 'x' should be 'jobs/researchers_add'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to declare context config(s) (add '## context_configs_type = multiple' if there are various context configs)
    # note extended documentation is unavailable for inline scripts

    x

    # more detailed examples for declaring context config(s)

    ## context_configs_type = multiple
    x = {
        ## cardinality = 0..1
        potential = single_alias_right[trigger_clause]
        ## cardinality = 0..1
        possible = single_alias_right[trigger_clause]
    }

    # since 1.3.5, scope context related options are also available here

    ## replace_scopes = { this = country root = country }
    x

    # since 1.3.6, using single alias at root level is also available here

    ## context_configs_type = multiple
    x = single_alias_right[trigger_clause]
}
```

**Example**:

```cwt
inline_scripts = {
    ## replace_scopes = { this = country root = country }
    triggers/some_trigger_snippet

    ## context_configs_type = multiple
    triggers/some_trigger_snippet = { ... }

    ## context_configs_type = multiple
    triggers/some_trigger_snippet = single_alias_right[trigger_clause]
}
```

![](../images/config/inline_scripts_1.png)

**Notes**:
- Keep default `single` when only one context config is needed; use `multiple` to declare multiple.
- Root-level `single_alias_right[...]` will be inlined before being used as context configs.
- This extension only provides context/scope information; it does not directly constrain where/How many times the inline script can be called.

#### Extended Parameter Configs {#config-extended-parameter}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedParameterConfigResolverImpl -->

- **Purpose**: provide docs/context enhancements for parameters (`$PARAM$` or `$PARAM|DEFAULT$`) in triggers/effects/inline scripts:
  - Bind a context key (pointing to a concrete trigger/effect/inline script context).
  - Declare context configs and scope context.
  - Support inheriting context from use sites.
- **Path location**: `parameters/{name}`.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `name`: parameter name or its matching pattern.
  - `context_key: string` (required): context key (e.g., `scripted_trigger@X`) used to locate the source of the parameter's context configs.
  - `context_configs_type: string = single | multiple` (default `single`): aggregation shape of context configs.
    - `single`: take value (`value`) as the context config.
    - `multiple`: take child configs list (`configs`) as the context configs.
  - `inherit: boolean = no`: whether to inherit context (configs and scopes) from the use site.
  - Scope context (options):
    - `replace_scopes`: rewrite system scope mapping.
    - `push_scope`: declare output scope.

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedParameterConfigResolverImpl`).
  - Required check: missing `context_key` will skip this item and log a warning.
  - Container config: if it is a property node, first apply `inlineSingleAlias(...)` to its value to get the container config (`getContainerConfig(...)`).
  - Context configs:
    - If `inherit = yes`: climb from the parameter's use site to the containing script member and take its resolved context config list (dynamic context).
    - Otherwise: according to `context_configs_type`, extract `value` or `configs` from the container config and wrap it into a consumable container via `inlineWithConfigs(...)` (`getContextConfigs(...)` returns a single-element list).

**Format**:

```cwt
parameters = {
    # 'x' or 'x = xxx'
    # 'x' is a parameter name, e.g., for '$JOB$', 'x' should be 'JOB'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to declare context config(s) (add '## context_configs_type = multiple' if there are various context configs)

    # for value of option 'context_key',
    # before '@' is the containing definition type (e.g., 'scripted_trigger'), or 'inline_script' for inline script parameters
    # after '@' is the containing definition name, or the containing inline script path
    # since 1.3.6, value of option 'context_key' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## context_key = scripted_trigger@some_trigger
    x

    # more detailed examples for declaring context config(s)

    ## context_key = scripted_trigger@some_trigger
    x = localistion

    ## context_key = scripted_trigger@some_trigger
    ## context_configs_type = multiple
    x = {
        localisation
        scalar
    }

    # since 1.3.5, scope context related options are also available here

    ## context_key = scripted_trigger@some_trigger
    ## replace_scopes = { this = country root = country }
    x

    # since 1.3.6, using single alias at root level is also available here

    ## context_key = scripted_trigger@some_trigger
    ## context_configs_type = multiple
    x = single_alias_right[trigger_clause]

    # since 1.3.12, a parameter's config context and scope context can be specified to inherit from its context
    # e.g. for parameter 'x' with context key 'scripted_trigger@some_trigger', its context is scripted trigger 'some_trigger'

    ## context_key = scripted_trigger@some_trigger
    ## inherit
    x
}
```

**Example**:

```cwt
parameters = {
    ## replace_scopes = { this = country root = country }
    ## context_key = some_trigger
    PARAM

    ## context_configs_type = multiple
    ## context_key = some_trigger
    PARAM = { ... }

    ## context_configs_type = multiple
    ## context_key = some_trigger
    PARAM = single_alias_right[trigger_clause]
}
```

![](../images/config/parameters_1.png)

**Notes**:
- `context_key` is required; missing it makes the item ineffective.
- When `inherit = yes`, the context is taken from the use site; note it can be empty or vary by position. PLS enables "dynamic context" mode on this path.
- Root-level `single_alias_right[...]` will be inlined before being used as context configs.

#### Extended Complex Enum Value Configs {#config-extended-complex-enum-value}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedComplexEnumValueConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedComplexEnumValueConfigResolverImpl -->

- **Purpose**: provide docs/hints (quick docs, inlay hints, etc.) for concrete entries of complex enums.
- **Path location**: `complex_enum_values/{type}/{name}`, where `{type}` is the complex enum name and `{name}` is the entry name or a matching pattern.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `type: string`: complex enum name (from the path segment `{type}`).
  - `name`: entry name or its matching pattern (from key or value).
  - `hint: string?`: optional hint text.

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedComplexEnumValueConfigResolverImpl`).
  - Type source: provided by upper traversal (`resolve(config, type)`), corresponding to the `{type}` path segment.
  - Option extraction: read hint text from `hint`.

**Format**:

```cwt
complex_enum_values = {
    component_tag = {
        # 'x' or 'x = xxx'
        # 'x' can also be a pattern expression (template expression, ant expression or regex)

        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x
    }
}
```

**Example**:

```cwt
complex_enum_values = {
    component_tag = {
        ### Some documentation
        ## hint = §GUseful note§!
        x # or write `x = ...`
    }
}
```

**Notes**:
- This extension does not change how values are collected for complex enums; it only provides hints.
- Name can use template/ANT/regex patterns; avoid overly broad patterns.

#### Extended Dynamic Value Configs {#config-extended-dynamic-value}

<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedDynamicValueConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedDynamicValueConfigResolverImpl -->

- **Purpose**: provide docs/hints for specific dynamic values under a dynamic value type.
- **Path location**: `dynamic_values/{type}/{name}`, where `{type}` is the dynamic value type and `{name}` is the entry name or a matching pattern.
- **Name matching**: supports constant, template expression, ANT expression, and regex (pattern-aware; see `CwtDataTypeGroups.PatternAware`).

- **Fields and semantics**:
  - `type: string`: dynamic value type (from the path segment `{type}`).
  - `name`: entry name or its matching pattern (from key or value).
  - `hint: string?`: optional hint text.

- **Parsing flow (implementation summary)**:
  - Name source: use key if it is a property; otherwise use the value (`CwtExtendedDynamicValueConfigResolverImpl`).
  - Type source: provided by upper traversal (`resolve(config, type)`), corresponding to the `{type}` path segment.
  - Option extraction: read hint text from `hint`.

**Format**:

```cwt
dynamic_values = {
    event_target = {
        # 'x' or 'x = xxx'
        # 'x' can also be a pattern expression (template expression, ant expression or regex)

        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x

        # since 1.3.9, scope context related options are also available here
        # only receive push scope (this scope), ignore others (like root scope, etc.)

        ## push_scope = country
        x
    }
}
```

**Example**:

```cwt
dynamic_values = {
    event_target = {
        ### Some documentation
        ## hint = §RSome hint text§!
        owner # or write `owner = ...`
    }
}
```

**Notes**:
- This extension does not change the definition of dynamic value types or their base value sets; it only provides hints.
- Name can use template/ANT/regex patterns; avoid overly broad patterns.

### Internal Configs {#configs-internal}

> These configs are used internally by the plugin and do not support customization (or are not currently supported).

#### Schema Config {#config-internal-schema}

<!-- @see icu.windea.pls.config.config.internal.CwtSchemaConfig -->
<!-- @see icu.windea.pls.config.config.internal.impl.CwtSchemaConfigResolverImpl -->
<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->
<!-- @see icu.windea.pls.config.util.CwtConfigSchemaManager -->
<!-- @see icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager -->
<!-- @see cwt/core/internal/schema.cwt -->

- **Purpose**: declare the RHS value shapes for ".cwt" config files themselves for basic completion and (limited) structural checks.
  - Currently focuses on initial completion; strict schema validation is not provided yet.

- **Source and loading**:
  - Only from built-in file: `internal/schema.cwt` (cannot be overridden by external files).
  - Collected and injected into `configGroup.schemas` via `FileBasedCwtConfigGroupDataProvider.processInternalFile()` calling `CwtSchemaConfig.resolveInFile(...)`.

- **Structure (`CwtSchemaConfig`)**:
  - `file: CwtFileConfig`: the corresponding config file.
  - `properties: CwtPropertyConfig[]`: normal keys (keys parsed as Constant/Type/Template forms).
  - `enums: Map<String, CwtPropertyConfig>`: keys parsed as enum expressions (`$enum:NAME$`).
  - `constraints: Map<String, CwtPropertyConfig>`: keys parsed as constraint expressions (`$$NAME`).

**Example (in `internal/schema.cwt`)**:

```cwt
$enum:my_enum$ = { ... }     # goes into enums
$$is_valid_key = { ... }     # goes into constraints
some_key = $any              # goes into properties
```

**Notes**:
- Works together with "Config Expressions → Schema Expression"; mainly for editor-side hints and lightweight checking.

#### Folding Settings Configs {#config-internal-folding}

<!-- @see icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig -->
<!-- @see icu.windea.pls.config.config.internal.impl.CwtFoldingSettingsConfigResolverImpl -->
<!-- @see icu.windea.pls.lang.folding.ParadoxExpressionFoldingBuilder -->
<!-- @see cwt/core/internal/folding_settings.cwt -->

- **Purpose**: provide additional code folding rules for the editor (internal; not customizable for now).

- **Source and loading**:
  - Only from built-in file: `internal/folding_settings.cwt`.
  - Collected and injected into `configGroup.foldingSettings[group]` by `FileBasedCwtConfigGroupDataProvider.processInternalFile()` calling `CwtFoldingSettingsConfig.resolveInFile(...)`.

- **Structure (`CwtFoldingSettingsConfig`)**:
  - `id: string`: folding item ID (unique within group).
  - `key: string?`: target key (optional).
  - `keys: string[]?`: target key set (optional).
  - `placeholder: string`: placeholder text after folding (required).

- **Parsing flow (summary)**:
  - Read per group: `group -> id -> { key/keys/placeholder }`.
  - Skip and warn when missing `placeholder` or no child properties.
  - Build a case-insensitive map for each group: `configGroup.foldingSettings[group][id]`.

**Example (in `internal/folding_settings.cwt`)**:

```cwt
folds = {
    expression = {
        fold_modifier = {
            key = "modifier"
            placeholder = "<modifier> ..."
        }
        fold_triggers = {
            keys = { "AND" "OR" }
            placeholder = "<triggers> ..."
        }
    }
}
```

**Notes**:
- `key` and `keys` are alternatives; `keys` is for multiple keys. When both exist, the consumer decides (current implementation reads both).
- Final behavior is controlled by the folding builder, see `ParadoxExpressionFoldingBuilder`.

#### Postfix Template Settings Configs {#config-internal-postfix}

<!-- @see icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig -->
<!-- @see icu.windea.pls.config.config.internal.impl.CwtPostfixTemplateSettingsConfigResolverImpl -->
<!-- @see icu.windea.pls.lang.codeInsight.template.postfix.ParadoxExpressionEditablePostfixTemplate -->
<!-- @see icu.windea.pls.lang.codeInsight.template.postfix.ParadoxVariableOperationExpressionPostfixTemplate -->
<!-- @see cwt/core/internal/postfix_template_settings.cwt -->

- **Purpose**: provide additional postfix template abilities for the editor (internal; not customizable for now).

- **Source and loading**:
  - Only from built-in file: `internal/postfix_template_settings.cwt`.
  - Collected and injected into `configGroup.postfixTemplateSettings[group]` by `FileBasedCwtConfigGroupDataProvider.processInternalFile()` calling `CwtPostfixTemplateSettingsConfig.resolveInFile(...)`.

- **Structure (`CwtPostfixTemplateSettingsConfig`)**:
  - `id: string`: template ID (unique within group).
  - `key: string`: trigger key (required), the keyword where postfix can be applied.
  - `example: string?`: example text (optional) to help understand the template.
  - `variables: Map<string, string>`: variable name → default value (for editable template variables).
  - `expression: string`: template expression (required), parsed and applied by postfix template implementations.

- **Parsing flow (summary)**:
  - Read per group: `group -> id -> { key/example/variables/expression }`.
  - Skip and warn when missing `key` or `expression`.
  - Read `variables` as a child property map: `name = defaultValue`.
  - Build a case-insensitive map: `configGroup.postfixTemplateSettings[group][id]`.

**Example (in `internal/postfix_template_settings.cwt`)**:

```cwt
postfix = {
    variable_ops = {
        decr = {
            key = "variable"
            example = "$x.decr"
            variables = { amount = 1 }
            expression = "${x} = ${x} - ${amount}"
        }
    }
}
```

**Notes**:
- The semantics of `expression` are controlled by the postfix template implementation, e.g., `ParadoxExpressionEditablePostfixTemplate`.
- `variables` only provide defaults; actual editing behavior is decided by the template implementation.

## Config Expressions {#config-expressions}

> This chapter introduces the purposes, formats, defaults, and edge behaviors of various config expressions.

<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->

### Basic Concepts and Scope

Config expressions are structured syntax used inside string fields of configs to describe value shapes or matching patterns.

Include:
- Data Expression: parse data types or dynamic snippets.
- Template Expression: patterns concatenating constants and dynamic snippets for flexible matching.
- Cardinality Expression: declare occurrence ranges and strict/lenient checks.
- Location Expression: locate resources like images/localisations.
- Schema Expression: declare RHS value shapes in config files.

### Data Expression {#config-expression-data}

<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->

Describe value shapes for keys/values in script files: constants, basic data types, references, and expressions that resolve to dynamic content.

Key points:

- **Key/Value context**: parse differently for keys (`isKey=true`) vs values (`isKey=false`).
- **Type**: the resolved concrete data type (e.g., `int`, `float`, `scalar`, `enum[...]`, `scope[...]`, `<type_key>`, etc.).
- **Extended metadata**: number ranges, case strategy, etc., depending on type (e.g., `int[-5..100]`, `float[-inf..inf]`, `ignoreCase`).

Defaults and edge behavior:

- **Fallback**: when nothing matches, fall back to `Constant` and store the original string in extension property `value`.
- **Empty string/block**: empty string as `Constant("")`; parsing a block returns `Block` type as a placeholder.
- **Definition `<...>` shorthand**: prefer `<country>` over `definition[country]`.
- **Mixing multiple data sources**: allowed in templates/combinations, e.g., `<country>/<planet>`, `dynamic_value[test_flag]`.
- **Single-quoted literals in dynamic links**: for `relations('...')`, a single-quoted argument is treated as a literal: no completion, and return early in completion entry.

Examples:

```cwt
int
float
enum[shipsize_class]
scope[country]
<ship_size>
pre_<opinion_modifier>_suf
```

### Template Expression {#config-expression-template}

<!-- @see icu.windea.pls.config.configExpression.CwtTemplateExpression -->

Describe more complex value shapes (as a combination of multiple data expressions).
Built from segments: constant fields + dynamic snippets (restricted data expressions).

Defaults and constraints:

- **No whitespace**: any whitespace makes it invalid (empty expression returned).
- **Segment decision**: a single segment (pure constant or single dynamic) is not considered a template (empty expression returned).
- **Matching strategy**: only scan dynamic configs with both prefix and suffix; use "leftmost earliest" splitting.
- **Segment types**: each segment delegates to data expression parsing; unmatched segments degrade to Constant.
- **Reference counting**: only non-Constant segments are counted as "reference segments" for subsequent reference/navigation.

Examples:

```cwt
job_<job>_add # "job" + <job> + "_add"
xxx_value[anything]_xxx # "xxx_" + value[anything] + "_xxx"
a_enum[weight_or_base]_b # "a_" + enum[weight_or_base] + "_b"
value[gui_element_name]:<sprite> # value[gui_element_name] + ":" + sprite
value[gui_element_name]:localisation # value[gui_element_name] + ":" + localisation
```

**Notes**:
- When constants are adjacent to segments that look like config names, prefer correct recognition of dynamic configs to avoid treating "symbol + config name" as a single constant.
- If whitespace is needed, use a more appropriate matching method (e.g., ANT/regex).

### Cardinality Expression {#config-expression-cardinality}

<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

Constrain occurrences of definition members, driving inspections/completions.
Supports lenient checks and infinite upper bounds.

Use `min..max` for the range; `~` marks lenient; `inf` means infinity.

Defaults and edge behavior:

- **Min < 0**: clamped to 0.
- **max = `inf` (case-insensitive)**: infinite.
- **No `..`**: invalid, no constraint.
- **min > max**: invalid, no constraint.

Examples:

```cwt
## cardinality = 0..1
## cardinality = 0..inf
## cardinality = ~1..10
```

### Location Expression {#config-expression-location}

<!-- @see icu.windea.pls.config.configExpression.CwtLocationExpression -->

Locate resource sources (images/localisations, etc.).
If the expression contains `$`, it is treated as a placeholder to be substituted later with dynamic content such as definition names or property values.

#### Image Location Expression {#config-expression-location-image}

<!-- @see icu.windea.pls.config.configExpression.CwtImageLocationExpression -->

Locate images related to a definition.

Syntax and conventions:

- Use `|` to separate args: `<location>|<args...>`.
- Args starting with `$` are name text source paths (comma-separated allowed): substitute placeholders, write into namePaths.
- Other args are frame source paths (comma-separated allowed): used for image splitting, write into framePaths.
- When repeated args of the same kind appear (all starting with `$`, or all non-`$`), the latter wins.

Examples:

```cwt
gfx/interface/icons/modifiers/mod_$.dds
gfx/interface/icons/modifiers/mod_$.dds|$name
gfx/interface/icons/modifiers/mod_$_by_$.dds|$name
GFX_$
icon
icon|p1,p2
```

Note: `icon` can be parsed as a file path, a sprite name, or a definition name; for a definition name, parse its most relevant image next.

#### Localisation Location Expression {#config-expression-localisation}

<!-- @see icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression -->

Locate localisations related to a definition.

Syntax and conventions:

- Use `|` to separate args: `<location>|<args...>`.
- Args starting with `$` are name text source paths (comma-separated allowed), written to namePaths.
- Arg `u` forces the final name to UPPER case. Only effective when placeholders are used.
- When `$` args repeat, the latter wins.

Examples:

```cwt
$_desc
$_desc|$name
$_desc|$name|u
$_desc|$name,$alt_name # multiple name paths, comma-separated
$_desc|$name|$alt_name # when `$` repeats, the latter wins
title
```

### Schema Expression {#config-expression-schema}

<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->

Describe value shapes for keys and values in config files themselves, enabling completion and inspections for config files.
Currently used for basic completion, and only in `cwt/core/schema.cwt`.

Supported forms:

- **Constant**: a plain string without `$`.
- **Template**: contains one or more params (`$...$`), e.g., `$type$`, `type[$type$]`.
- **Type**: starts with a single `$`, e.g., `$any`, `$int`.
- **Constraint**: starts with `$$`, e.g., `$$declaration`.

## FAQ {#faq}

#### About Template Expressions {#faq-template}

Template expressions are composed of multiple data expressions (e.g., definition/localisation/string literal related data expressions) for more flexible matching.

```cwt
# a string literal, exactly matches 'x'
x
# a template expression which contains a reference to jobs, matches 'a_researcher_b', 'a_farmer_b', etc.
a_<job>_b
# a template expression which contains a references to enum values of 'weight_or_base', matches 'a_weight_b' and 'a_base_b'
a_enum[weight_or_base]_b
# a template expression which contains a references to dynamic values of 'anything'
# generally, there is no limit for 'value[anything]', so this expression is equivalent to regex 'a_.*_b'
a_value[anything]_b
```

#### How to use ANT path patterns in config files {#faq-ant}

PLS extends config expressions. Since plugin version 1.3.6, you can use ANT path patterns for more flexible matching.

```cwt
# a ant expression use prefix 'ant:'
ant:/foo/bar?/*
# a ant expression use prefix 'ant.i:' (ignore case)
ant.i:/foo/bar?/*

# wildcards in ant expression:
# '?' - used to match any single character
# '*' - used to match any characters (exclude '/')
# '**' - used to match any characters
```

#### How to use regex in config files {#faq-regex}

PLS extends config expressions. Since plugin veresion 1.3.6, you can use regex for more flexible matching.

```cwt
# a regex use prefix 're:'
re:foo.*
# a regex use prefix 're.i:' (ignore case)
re.i:foo.*
```

#### How to specify scope context in config files {#faq-scope}

Scope context is specified via options `push_scope` and `replace_scopes`.

```cwt
# push 'country' scope to scope stack
# for this example, the next this scope will be 'country'
## push_scope = country
some_config

# replace scopes of specific system scopes into scope context
# not supported for 'prev' system scope (and 'prevprev', etc.)
# for this example, the next this scope will be 'country', so do the next root scope and the next from scope
## replace_scopes = { this = country root = country from = country }
some_config
```

#### 如何在规则文件中进行规则注入 {#faq-config-injection}

Since plugin version 2.1.0, config injection can be performed during the resolving phase of config by using the `inject` option.

If there is an existing config snippet

```cwt
# some/file.cwt
some = {
    property = v1
    property = v2
}
```

Then the config snippet

```cwt
# some/other/file.cwt
## inject = some/file.cwt@some/property
k1 = v
## inject = some/file.cwt@some/property
k2 = {}
## inject = some/file.cwt@some/property
k3 = {
    p = v
}
```

After processing, is equivalent to

```cwt
# some/other/file.cwt
k1 = v
k2 = {
    property = v1
    property = v2
}
k3 = {
    p = v
    property = v1
    property = v2
}
```

Notes:
- The part before `@` is the path of the config file relative to the config group directory (e.g., the `config/stellaris` directory in the plugin's jar package) and must match exactly (wildcards are not supported, and case sensitivity is not ignored).
- The part after `@` is the config path. The subpath `-` matches all individual values, while in other cases, it acts as a wildcard (case-insensitive, using `any` or `*` to match any character, and `?` to match a single character) to match all attributes of the corresponding key.
- This only applies to configs where the value is a clause (i.e., `k = {...}` or `{...}`). The matched configs are injected at the end of the clause as sub-configs of the target config.
- Config injection is processed only once during the parsing phase of the config file, so injection can be performed anywhere in any config file.
- If injection fails (e.g., the matched config does not exist, recursion occurs, etc.), it is simply ignored, and a warning log is printed.