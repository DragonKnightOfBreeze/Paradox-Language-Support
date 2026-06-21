# Appendix: Config Format Reference

<!--
@doc-meta
This document is the reference manual for the CWT config format, describing the purpose, format, fields, and considerations of the various configs supported by the plugin.
The content is based on the implementation of the Paradox Language Support plugin and is compatible with the CWTools config format in most cases, but differs in details and extension points.

@see docs/zh/config.md
@see icu.windea.pls.config.config.*
@see icu.windea.pls.config.configExpression.*
-->

## Overview {#overview}

<!-- @see icu.windea.pls.config.config.CwtConfig -->
<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->
<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->
<!-- @see icu.windea.pls.config.CwtDataType -->
<!-- @see icu.windea.pls.model.expressions.ParadoxExpression -->
<!-- @see icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression -->

This document is the reference manual for the CWT config format, intended for all readers who wish to understand, write, or extend config files – including mod authors, config file maintainers, plugin maintainers, and AI coding assistants.

This document aims to:

- **Unify terminology and boundaries**: Align the semantics between the plugin and CWTools, clarifying the plugin's extensions and differences.
- **Establish mappings from documentation to implementation**: Annotate corresponding interfaces and resolvers where necessary, facilitating source code tracing and behavior verification.
- **Guide to practice**: Outline the purpose, format, and considerations for various config types, laying the foundation for correctly writing and maintaining config files.

The plugin parses config files when opening a project or on application startup, reading and computing config group data to build config groups.
The config group data stores structured config objects, and other types of data.
They drive the core semantic matching and resolution logic, and are widely used in language features such as code highlighting, code completion, and code inspection.

Among these:

- **Config**: A unified domain model that corresponds syntactically to a config file or nodes within it (such as properties or values).
- **Config expression**: A structured syntax used in config fields, corresponding syntactically to string literals with special semantics in config files.
- **Data expression**: The most common type of config expression, determining the matching and parsing logic of an expression.
- **Data type**: The type of data expression; different data types often imply expressions with different semantics.
- **Expression**: Distinct from config expressions / data expressions, this corresponds syntactically to various expression nodes in script files, localisation files, or CSV files – for example, properties and values in script files, command text in localisation files, or columns in CSV files.
- **Complex expression**: Distinguished from simple expressions, this is a lightweight syntax tree where each node has its own semantics – for example, the scope field expression `root.owner`, or the localisation command expression `Root.GetName`.

For an overall introduction to the config system (such as config groups, config overriding, custom configs, etc.), please refer to the [Configs](config.md) documentation.

## Configs {#configs}

<!-- @see icu.windea.pls.config.config.CwtConfig -->

> This chapter introduces the purpose, format essentials, and considerations of various configs, helping readers correctly understand and write them.

### Overview {#configs-overview}

Configs are the unified and core domain model used in the plugin's own config system.

#### Representation Conventions for Config Fields {#configs-fields}

Each config is composed of several **fields**. Fields come from various sources in config files; this document uses the following format for unified description:

- **Property fields**: Regular properties appearing in the config body in the form `key = value`. The document uses the field name directly, such as `path`, `name_field`.
- **Option fields**: Fields appearing as option comments in the form `## key = value`. The document prefixes them with `## `, such as `## cardinality`, `## push_scope`.
- **Boolean options**: Valueless markers appearing as option comments in the form `## key`. The document also prefixes them with `## `, such as `## primary`, `## inherit`. Note that this differs from `## key = yes` — boolean options take effect with just the marker name.
- **Documentation comments**: Documentation comments in the form `### text`, typically used to provide quick documentation text.
- **Value fields**: Values appearing directly in the config body (rather than as the value side of a property), such as enum value lists.

Field names use `snake_case` form in config files.

#### Processing Flow {#configs-processing-flow}

The overall processing flow of configs can be simplified into three stages:

1. Read config files from config groups and build their syntax trees (PSI).
2. Use the corresponding resolver for each config category to transform syntax tree nodes into structured config objects.
3. In each language feature, query and apply these config objects based on the current context (scope, type name, declaration context, etc.).

The source and overriding mechanisms for configs are detailed in the "Config Groups" and "Override Methods" sections of [Config](config.md).

#### Category {#config-category}

Configs are categorized by level as follows:

- **Base configs**: As the general model at the syntax tree level, corresponding to the config file or the properties, values or other nodes in it. This document does not cover base configs individually.
- **[Standard configs](#configs-standard)**: Core configs that drive various language features, including types, aliases, enums, links, scopes, etc.
- **[Extended configs](#configs-extended)**: Additional configs for enhancing plugin functionality, such as providing extra context and hints for specific definitions or inline scripts.
- **[Internal configs](#configs-internal)**: Configs used internally by the plugin, currently not supporting (or not yet supporting) customization.

### Standard Configs {#configs-standard}

> These configs drive a wide variety of language features, including but not limited to code completion, code inspection, quick documentation, inlay hints, etc.

#### Priority Config {#config-priority}

<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideStrategy -->
<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideService -->
<!-- @see cwt/core/priorities.core.cwt -->

Priority configs configure the override strategy for "targets" (files, global scripted variables, definitions, localisations, etc.). They affect the order in which targets take effect and the sorting of query results (except for streaming queries). When no directory mapping is matched, the default is `LIOS` (Last In, Only Served).

Override strategies:

- **`FIOS`** (First In, Only Served): The first loaded takes effect; later ones are ignored.
- **`LIOS`** (Last In, Only Served): Later loaded overrides earlier loaded.
- **`DUPL`** (Duplicates): Whole-file override; must be replaced entirely with a file at the same path.
- **`ORDERED`** (Ordered): Read in order; later loaded items are added or merged in sequence without overriding existing entries.

Query (non-streaming) result sorting is driven by priority; within the same path, the load order (game / dependency chain) determines precedence. Within the same file, later items override earlier ones.

Format description:

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

Example:

```cwt
priorities = {
    "common/event_chains" = fios
    "common/on_actions" = ordered
    "common/scripted_variables" = fios
    "events" = fios
}
```

- Two mods both define an event with the same name in `events/`: Due to `events = fios`, the mod loaded first takes effect, and the later one is ignored.
- Two mods both add entries in `common/on_actions/`: Due to `ordered`, entries are merged in order without overriding.

#### System Scope Config {#config-system-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig -->
<!-- @see cwt/core/system_scopes.core.cwt -->

System scope configs provide metadata for built-in "system-level scopes" (such as This, Root, Prev, From, etc.), used for quick documentation and scope stack derivation.

Path location:

- `system_scopes/{name}`, where `{name}` is the system scope ID.

Field meanings:

- `id`: System scope ID.
- `base_id`: Base scope ID; defaults to `id` when not specified. Used to classify scopes of the same family (e.g. `Prev` / `PrevPrev`, `From` / `FromFrom`).
- `: string` (value): Readable name; defaults to `id` when not specified.

System scope configs, together with [scope configs and scope group configs](#config-scope), determine scope checking and hints. In some [extended configs](#configs-extended), the option `## replace_scopes` can be used to specify the concrete scope types that system scopes map to in the current context (e.g. mapping `this` / `root` / `from` to `country`). Note that `## replace_scopes` does not support replacing `prev`-series system scopes.

Example:

```cwt
# from `system_scopes.core.cwt` of core config group

system_scopes = {
    This = {}
    Root = {}
    Prev = { base_id = Prev }
    PrevPrev = { base_id = Prev }
    PrevPrevPrev = { base_id = Prev }
    PrevPrevPrevPrev = { base_id = Prev }
    From = { base_id = From }
    FromFrom = { base_id = From }
    FromFromFrom = { base_id = From }
    FromFromFromFrom = { base_id = From }
}
```

#### Locale Config {#config-locale}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocaleConfig -->

Locale configs declare basic information about locales, facilitating identification of the project / user's preferred locale and improving UI display and localisation validation.

Path location:

- `locales/{id}`, where `{id}` is the locale ID.

Field meanings:

- `id`: Locale ID.
- `codes: string[]`: Language codes included in this locale (e.g. `en`, `zh-CN`).

Example:

```cwt
locales = {
    l_english = { codes = { "en" } }
    l_simp_chinese = { codes = { "zh-CN" } }
}
```

#### Type Config and Subtype Config {#config-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSubtypeConfig -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesType -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesTypeFast -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesSubtype -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesSubtypeFast -->

Type configs locate and name "definitions" based on conditions such as "file path / key name", and can declare subtypes, display information, and images.

Path location:

- Type: `types/type[{type}]`, where `{type}` is the type name (i.e. the config name).
- Subtype: `types/type[{type}]/subtype[{subtype}]`, where `{type}` is the type name and `{subtype}` is the subtype name (i.e. the config name).

Type fields:

- `path`: Directory path of files to scan (the `game/` prefix is automatically removed during parsing). Multiple values can be declared.
- `path_file`: Restricts the filename (without extension). If specified, `path_extension` no longer takes effect independently.
- `path_extension`: Restricts the file extension (automatically normalized during parsing, e.g. adding `.`). Only takes effect independently when `path_file` is not specified.
- `path_pattern`: Uses ANT path patterns to match file paths. Multiple values can be declared, independent of `path` — if any `path_pattern` matches, the path check passes.
- `path_strict`: When set to `yes`, forces exact directory matching without matching subdirectories.
- `type_per_file`: When set to `yes`, indicates "one type instance per file" (the definition corresponds to the entire script file rather than a property within it).
- `name_field`: Reads the display name from the property key specified within the definition body. When present, the type key only accepts values explicitly listed in `## type_key_filter`, or is unrestricted.
- `name_from_file`: When set to `yes`, derives the definition name from the filename.
- `unique`: When set to `yes`, enables duplicate name conflict checking.
- `severity`: Reporting level for duplicate name conflicts (e.g. `warning`, `error`).
- `skip_root_key`: Allows skipping several top-level keys before continuing to match the type key. The value is a curly-brace set, supporting multiple groups (case-insensitive, supports wildcards `any`/`*`/`?`). If `skip_root_key` is non-empty but the file has no root keys, matching fails; if empty but the file has root keys, matching also fails.
- `type_key_prefix`: Required prefix for the type key (case-insensitive).
- `## type_key_filter`: Filter condition for the type key (option comment, case-insensitive). Supports inclusion sets `{ a b }` and exclusion sets `<> { x y }`.
- `## type_key_regex`: Regex filter for the type key (option comment, case-insensitive).
- `## starts_with`: Prefix filter for the type key (option comment, case-insensitive).
- `## graph_related_types`: Declares graph-related types (option comment), used for inter-definition dependency graphs.
- `localisation`: Localisation display section, see [Type Presentation Config](#config-type-presentation) for details.
- `images`: Image display section, see [Type Presentation Config](#config-type-presentation) for details.
- `modifiers`: Modifier section, which derives [modifier configs](#config-modifier) bound to the type.

Type matching flow:

For a property (or entire file) in a script file, type matching proceeds through the following steps in order:

1. **Element type check**: When `type_per_file` is `yes`, the definition must correspond to the entire script file; otherwise it must correspond to a property.
2. **Path matching**: First checks `path_pattern` (ANT patterns) — if any matches, the check passes; otherwise checks `path_file` or `path_extension`, then checks `path` (including `path_strict`). When both `path` and `path_extension`/`path_file` are non-empty, both must be satisfied.
3. **Type key check** (in order): `## starts_with` → `## type_key_regex` → `## type_key_filter` → `name_field` constraint (if `name_field` exists, the type key may only be one of the values explicitly listed in `## type_key_filter`, or is unrestricted).
4. **Root key check**: Determines whether root keys need to be skipped based on `skip_root_key`.
5. **Type key prefix check**: Checks whether the prefix matches based on `type_key_prefix` (case-insensitive).
6. **Declaration structure check**: Checks whether the definition's property value is consistent with the expected structure of the [declaration config](#config-declaration) (e.g. if the declaration config expects a block, the property value must be a block).

Subtype fields:

Subtypes are determined through content matching. Subtypes are checked one by one in declaration order, typically used together with `subtype[...] = {...}` in [declaration configs](#config-declaration) to refine structure and validation.

- `## type_key_filter`: Filters by type key (option comment, case-insensitive).
- `## type_key_regex`: Filters by type key regex (option comment, case-insensitive).
- `## starts_with`: Filters by type key prefix (option comment, case-sensitive).
- `## push_scope`: Scope type pushed when matched (option comment).
- `## display_name`: Display name for the subtype (option comment).
- `only_if_not`: Mutually exclusive with specified subtypes — only continues checking if none of the specified subtypes have matched.
- `## group`: Subtype group name (option comment). Subtypes within the same group are mutually exclusive (at most one matches).

Subtype matching flow:

1. **Mutual exclusion check**: If any subtype specified in `only_if_not` has already matched, skip.
2. **Type key check**: Checks in order: `## starts_with` (case-sensitive) → `## type_key_regex` → `## type_key_filter` (case-insensitive).
3. **Content matching**: If the subtype declaration body (`subtype[...] = { ... }`) contains property or value configs, recursively checks whether the definition body contains matching properties and values. Matching methods include exact boolean matching, string/data expression matching, and recursive matching of nested blocks. If the declaration body is empty (`{}`), only the type key check needs to pass for a match.

Type configs cooperate with [declaration configs](#config-declaration) to provide context and structural constraints for specific definition declarations.

Example:

```cwt
# from `events/events.cwt` of stellaris config group

types = {
    ## graph_related_types = { special_project anomaly_category }
    type[event] = {
        name_field = id
        path = "game/events"
        path_extension = .txt

        subtype[inherited] = {
            base = <event>
        }

        ## group = event_attribute
        subtype[triggered] = {
            is_triggered_only = yes
        }

        ## group = event_type
        ## type_key_filter = country_event
        ## push_scope = country
        ## display_name = Country Event
        subtype[country] = {}

        ## group = event_type
        ## type_key_filter = planet_event
        ## push_scope = planet
        ## display_name = Planet Event
        subtype[planet] = {}

        # ... more event type subtypes (ship, fleet, system, etc.)
    }
}
```

```cwt
# from `common/buildings.cwt` of stellaris config group
# which represents `localisation` and `images` sections

types = {
    type[building] = {
        path = "game/common/buildings"
        path_extension = .txt
        subtype[capital] = {
            capital = yes
        }
        localisation = {
            name = "$"
            desc = "$_desc"
        }
        images = {
            icon = icon
            icon = "gfx/interface/icons/buildings/$.dds"
        }
    }
}
```

Notes:

- `path` is a required field; if missing, the type will be skipped.
- `skip_root_key` is a multi-group setting: if any group matches the file's top-level key sequence, skipping is allowed and type key matching continues.
- Subtype matching is "order-sensitive"; place more specific configs earlier.
- Subtypes within the same `## group` are mutually exclusive (e.g. `country`, `planet`, `ship`, etc. in the `event_type` group).

#### Type Presentation Config {#config-type-presentation}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypePresentationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeImagesConfig -->

Type presentation configs configure "name / description / required localisation keys" and "primary image / frame rules" display information for definition types, for use in UI, navigation, and hints.

Path location:

- Localisation: `types/type[{type}]/localisation`, where `{type}` is the definition type.
- Images: `types/type[{type}]/images`, where `{type}` is the definition type.

Both have the same structure and contain multiple location configs. You can use `subtype[{expression}] = {...}` to specify the subtypes that need to be matched for a set of location configs, where `{expression}` is a subtype expression (e.g., `type`, `!type`, `type1&!type2`). Nesting is supported.

Common options for location configs include `required` (whether the item is required) and `primary` (whether it is the primary item, used for the main display icon / primary name). For the detailed syntax of location expressions, see [Location Expression](#config-expression-location).

Example:

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
            icon = "icon|icon_frame"  # image location expression
        }
    }
}
```

#### Location Config {#config-location}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocationConfig -->

Location configs declare the locating key and location expression for resources such as images / localisation, used in the `localisation` and `images` sections of type presentation configs.

Path location:

- Localisation resources: `types/type[{type}]/localisation/{key}`, where `{type}` is the definition type and `{key}` is the key name.
- Image resources: `types/type[{type}]/images/{key}`, where `{type}` is the definition type and `{key}` is the key name.

#### Declaration Config {#config-declaration}

<!-- @see icu.windea.pls.config.config.delegated.CwtDeclarationConfig -->
<!-- @see icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider -->
<!-- @see icu.windea.pls.ep.config.config.CwtInjectedConfigProvider -->
<!-- @see icu.windea.pls.config.manipulation.CwtConfigManipulationService.deepCopyConfigsInDeclaration -->

Declaration configs describe the structure of "definition entries" and serve as the foundation for features such as completion, inspection, and quick documentation.

Path location:

- `{name}`, where `{name}` is the config name.
- Top-level properties in config files whose keys are valid identifiers and are not matched by other configs will fall back to being parsed as declaration configs.

The processing flow of declaration configs is roughly as follows: First, only top-level properties with valid identifier keys are treated as declaration configs. If the root-level value of the declaration is `single_alias_right[...]`, inline expansion is performed first. Then, the plugin trims and flattens the config tree by subtypes — `subtype[...]` blocks matching the current context subtypes are expanded to sibling-level sub-configs, while non-matching ones are skipped. The resulting config tree is used to drive completion, inspection, and other features.

Declaration configs can cooperate with other configs: [aliases and single aliases](#config-alias) (`alias_name[...]` / `alias_match_left[...]`, `single_alias_right[...]`) can be referenced within declarations. Swapped type declarations can be nested directly within the corresponding base type's declaration. Game rules and on actions can also have their declaration context overridden through [extended configs](#configs-extended).

Example:

```cwt
# from `common/buildings.cwt` of stellaris config group

## push_scope = planet
building = {
    ## cardinality = 0..inf
    ## replace_scopes = { this = planet root = planet }
    desc = single_alias_right[triggered_desc_clause]

    ## cardinality = 0..1
    owner_type = corporate

    ## cardinality = 0..1
    ruined_icon = icon[gfx/interface/icons/buildings]

    ## cardinality = 0..1
    ruined_icon = <sprite>

    ## cardinality = 0..1
    building_sets = {
        ## cardinality = 0..inf
        enum[building_set]
    }

    # ...
}
```

Notes:

- `subtype[...]` only takes effect when it matches the context subtypes; non-matching ones are ignored (no error is reported).
- Root-level `single_alias_right[...]` is expanded first, then participates in subsequent parsing and inspection.
- To ensure that downstream features can "trace back upward", generated config nodes maintain parent config references.

#### Alias Config and Single Alias Config {#config-alias}

<!-- @see icu.windea.pls.config.config.delegated.CwtAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSingleAliasConfig -->
<!-- @see icu.windea.pls.config.manipulation.CwtConfigManipulationService.inlineAlias -->
<!-- @see icu.windea.pls.config.manipulation.CwtConfigManipulationService.inlineSingleAlias -->

Alias configs abstract reusable config fragments into "named aliases" that can be referenced and expanded in multiple places. Single aliases are used for one-to-one reuse on the "value side".

Path location:

- Alias: `alias[{name}:{subName}]`, where `{name}` is the name and `{subName}` is the sub-name (a restricted data expression).
- Single alias: `single_alias[{name}]`, where `{name}` is the config name.

Declaration and reference syntax:

- Declare alias: `alias[effect:some_effect] = { ... }`
- Use alias: `alias_name[effect] = alias_match_left[effect]`
- Declare single alias: `single_alias[trigger_clause] = { alias_name[trigger] = alias_match_left[trigger] }`
- Use single alias: `potential = single_alias_right[trigger_clause]`

Aliases support specifying scope constraints via options: `## scope` / `## scopes` declares the allowed input scope set, and `## push_scope` declares the output scope. The alias `subName` supports restricted data expressions for matching and hints.

At the usage site, the alias body is copied as a regular property config (key = sub-name, value and sub-configs deep-copied, options preserved). If the value side of the expanded result is still `single_alias_right[...]`, cascading expansion continues. Aliases are commonly used in combination with [declaration configs](#config-declaration) to reuse trigger / effect fragments within definition declarations.

Example:

```cwt
# Alias: define an effect fragment
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

# Single alias: define a trigger-block fragment
single_alias[trigger_clause] = {
    alias_name[trigger] = alias_match_left[trigger]
}

# Use single-alias on value side in a declaration
some_definition = {
    ## cardinality = 0..1
    potential = single_alias_right[trigger_clause]
}
```

Notes:

- The alias unique key is composed of `name:subName`; duplicate definitions are handled by the override strategy / priority.
- Cardinality and option validation occur only after expansion; consider the final semantics at the expansion site rather than at the declaration site.
- `subName` is a data expression (restricted); templates / enums can be used to increase reusability, but avoid being too broad to prevent false matches.

#### Row Config {#config-row}

<!-- @see icu.windea.pls.config.config.delegated.CwtRowConfig -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesRow -->

Row configs declare column names and value forms for CSV rows, used for completion and inspection.

Path location:

- `rows/row[{name}]`, where `{name}` is the config name.

- `path`: Directory path of files to scan (the `game/` prefix is automatically removed during parsing). Multiple values can be declared.
- `path_file`: Restricts the filename (without extension). If specified, `path_extension` no longer takes effect independently.
- `path_extension`: Restricts the file extension (automatically normalized during parsing, e.g. adding `.`). Only takes effect independently when `path_file` is not specified.
- `path_pattern`: Uses ANT path patterns to match file paths. Multiple values can be declared, independent of `path` — if any `path_pattern` matches, the path check passes.
- `path_strict`: When set to `yes`, forces exact directory matching without matching subdirectories.
- `columns`: Declares the mapping from column names to column configs
- `end_column`: Declares the terminating column name (once matched, subsequent columns are treated as optional trailing columns).

The path matching logic of row configs is the same as in [type configs](#config-type).

Example:

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

#### Define Configs {#config-define}

<!-- @see icu.windea.pls.config.config.delegated.CwtDefineConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtDefineVariableConfig -->

Define configs are used to describe define namespaces and define variables in script files, providing quick documentation text and config context.
They are located in script files with the `.txt` extension within the `common/defines` directory.

Path location:

- Define namespace: `defines/{namespace}`, where `{namespace}` is the namespace (i.e. the config name).
- Define variable: `defines/{namespace}/{variable}`, where `{namespace}` is the namespace and `{variable}` is the variable name (i.e. the config name).

Example:

```cwt
defines = {
    # define namespace config `NAMESPACE`
    NAMESPACE = {
        # define variable config `STRING`
        STRING = scalar
        # define variable config `STRING_SET`
        STRING_SET = {
            ## cardinality = 0..inf
            scalar
        }
    }
}
```

Notes:

- The plugin forcibly ignores any type config or declaration config named `define` or `defines`.
- Currently, based on define configs, the plugin checks the validity of the declaration structure for define variables, but does not check the validity of the names of define namespaces or define variables.

#### Enum Config and Complex Enum Config {#config-enum}

<!-- @see icu.windea.pls.config.config.delegated.CwtEnumConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtComplexEnumConfig -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesComplexEnum -->

Enum configs provide the value set for data expressions `enum[...]`. Depending on the source of values, they are divided into simple enums and complex enums.

Path location:

- Simple enum: `enums/enum[{name}]`, where `{name}` is the config name.
- Complex enum: `enums/complex_enum[{name}]`, where `{name}` is the config name.

---

**Simple Enum**

The value set of a simple enum is entirely declared in config files; matching is case-insensitive. The current implementation only supports constant values and does not support template expressions.

```cwt
enums = {
    enum[weight_or_base] = { weight base }
}
```

---

**Complex Enum**

Complex enums dynamically collect enum values from script files based on path and anchor points.

- `path`: Directory path of files to scan (the `game/` prefix is automatically removed during parsing). Multiple values can be declared.
- `path_file`: Restricts the filename (without extension). If specified, `path_extension` no longer takes effect independently.
- `path_extension`: Restricts the file extension (automatically normalized during parsing, e.g. adding `.`). Only takes effect independently when `path_file` is not specified.
- `path_pattern`: Uses ANT path patterns to match file paths. Multiple values can be declared, independent of `path` — if any `path_pattern` matches, the path check passes.
- `path_strict`: When set to `yes`, forces exact directory matching without matching subdirectories.
- `start_from_root`: Specifies whether to start searching for anchor points from the file top (rather than the next level below top-level properties).
- `name`: Describes how to locate value anchors in matching files — the implementation collects all property keys, property values, or block member values named `enum_name` as anchors.

The path matching logic of complex enum configs is the same as in [type configs](#config-type).

Complex enum matching flow: For each string expression in a matching file, the plugin checks whether it can serve as an anchor for a complex enum value. The specific steps are: First, find config entries containing `enum_name` in the `name` section; then, based on the position where `enum_name` appears (as a property key, property value, or block member value), determine the current expression's role — if `enum_name` is on the property key side, the current property key is the enum value anchor; if `enum_name` is on the property value side, the current property's value is the enum value anchor; if `enum_name` is a block member value, that value itself is the enum value anchor. Finally, match parent structures upward layer by layer from the anchor until reaching the root of the `name` section (`start_from_root` being `yes` requires reaching the file root level; otherwise, reaching the next level below top-level properties is sufficient).

Plugin extension options: The boolean option `## case_insensitive` marks complex enum values as case-insensitive; the boolean option `## per_definition` limits the equivalence of complex enum values with the same name and type to the definition level (rather than the file level).

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

Notes:

- Simple enums currently only support constant values; template expressions will not be parsed as templates.
- If a complex enum lacks a `name` section or no `enum_name` anchors are found in matching files, the enum will be empty.
- Simple enum values are case-insensitive by default; complex enum values are case-sensitive by default.

#### Dynamic Value Type Config {#config-dynamic-value-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig -->

Dynamic value type configs provide a set of "predefined (hardcoded)" dynamic values for the data expression `value[...]`, replacing fixed literals to facilitate completion and validation. The current implementation only supports constant values and does not support template expressions.

Path location:

- `values/value[{name}]`, where `{name}` is the config name.

To declare "scope context" for dynamic values or dynamically generate values based on context, see [Extended Config for Dynamic Values](#config-extended-dynamic-value).

Example:

```cwt
values = {
    value[event_target] = { owner capital }  # case-insensitive
}
```

#### Link Config {#config-link}

<!-- @see icu.windea.pls.config.config.delegated.CwtLinkConfig -->

Link configs provide semantic and type constraints (scope / value) for "field / function-like" nodes in complex expressions, supporting chained access and completion checking.

Path location:

- Regular links: `links/{name}`, where `{name}` is the config name.
- Localisation links: `localisation_links/{name}`, where `{name}` is the config name. If not explicitly declared, static regular links are automatically copied as localisation links.

Static and dynamic: Links without a declared `data_source` are static links, representing only a fixed node name (e.g. `owner`). Links with a declared `data_source` and/or `prefix` / `from_*` are dynamic links that can carry dynamic data (e.g. `modifier:x`, `relations(x)`, `var:x`).

Main fields:

- `type`: Link type (`scope` / `value` / `both`, defaults to `scope`).
- `from_data`: Whether to read dynamic data from text data (format like `prefix:data`).
- `from_argument`: Whether to read dynamic data from arguments (format like `func(arg)`).
- `argument_separator`: Separator for multiple arguments (`comma` / `pipe`, defaults to `comma`).
- `prefix`: Prefix for the dynamic link.
- `data_source` (multi-valued): Each data source is a data expression constraining the valid values for dynamic data.
- `input_scopes`: Input scope set; can be a single value or a set, supporting both `input_scope` and `input_scopes` notations.
- `output_scope`: Output scope; when empty, indicates passthrough or derivation based on data source.
- `for_definition_type`: Only available within the specified definition type.

Example:

```cwt
# from `links.cwt` of stellaris config group

links = {
    # Static scope link
    planet = {
        input_scopes = { megastructure planet pop_group leader army starbase deposit archaeological_site }
        output_scope = planet
    }
    
    # Dynamic value link (without prefix)
    variable = {
        type = value
        from_data = yes
        data_source = value[variable]
    }
    
    # Dynamic value link (with prefix)
    script_value = {
        from_data = yes
        type = value
        prefix = value:
        data_source = <script_value>
    }
}
```

```cwt
# from `links.core.cwt` of core config group

links = {
    event_target = {
        from_data = yes
        type = scope
        prefix = event_target:
        data_source = value[event_target]
    }
}
```

Notes:

- `prefix` should not contain quotes or parentheses; `input_scopes` uses curly-brace set syntax (e.g. `{ country }`).
- Multiple `data_source` values can be mixed.
- If a dynamic link argument is a single-quoted literal, it is treated as a literal and typically does not provide completion.

#### Localisation Command Config and Localisation Promotion Config {#config-localisation}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig -->

Localisation command configs declare the availability and allowed scopes of "localisation command fields" (e.g. `GetCountryType`). Localisation promotion configs declare "localisation scope promotions", allowing corresponding command fields to be used after switching scopes via localisation links.

Path location:

- Localisation command: `localisation_commands/{name}`, where `{name}` is the config name.
- Localisation promotion: `localisation_promotions/{name}`, where `{name}` is the config name.

Both contain the `supported_scopes` field, which declares the set of allowed scope types.

Example:

```cwt
# from `localisation.cwt` of stellaris config group

localisation_commands = {
    GetCountryType = { country }
}

localisation_promotions = {
    Ruler = { country }
}

# In localisation text:
# [Ruler.GetCountryType] is valid under the promoted scope after Ruler link
```

Notes:

- Names are case-insensitive; maintain spelling consistent with actual usage for searchability.
- The promotion config name should match the localisation link name; otherwise, correct matching cannot occur.
- Static regular links are automatically copied as localisation links; if dynamic behavior is needed, declare localisation links separately.

#### Modifier Config and Modifier Category Config {#config-modifier}

<!-- @see icu.windea.pls.config.config.delegated.CwtModifierConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig -->

Modifier configs declare modifiers and their categories, used for icon rendering, completion, and scope validation.

Path location:

- Modifier:
  - `modifiers/{name}`, where `{name}` is the config name (can be a constant or template expression).
  - `types/type[{type}]/modifiers/{name}`, where `{type}` is the definition type and `{name}` is the config name (the `$` placeholder is replaced with `<{type}>`).
  - `types/type[{type}]/modifiers/subtype[{subtype}]/{name}`, where `{subtype}` is the definition subtype.
- Modifier category: `modifier_categories/{name}`, where `{name}` is the config name.

Modifier fields: `name` is a templated name (e.g. `job_<job>_add`), supporting matching of dynamically generated modifiers. `categories` is a set of category names that determine the allowed scope types. If category mapping has been resolved, scopes are aggregated based on categories; otherwise, it falls back to the modifier's own `supported_scopes` option.

Modifier category fields: `name` is the category name (e.g. `Pops`), and `supported_scopes` is the set of scopes allowed for that category.

Modifier configs work in conjunction with the `modifiers` section of [type configs](#config-type): Modifier names declared in type configs use `$` as a placeholder, which is replaced with `<{type}>` or `<{type}.{subtype}>` during parsing, thereby deriving type-bound modifier configs.

Example:

```cwt
# from `modifiers.cwt` and `modifiers.categories.cwt` of stellaris config group

# Standalone modifier declarations
modifiers = {
    pop_happiness = { Pops }
    job_<job>_add = { Planets }
}

# Modifier categories
modifier_categories = {
    Pops = {
        supported_scopes = { species pop_group planet sector galacticobject country }
    }
}
```

```cwt
# Modifiers declared in type configs (will derive templated names)
types = {
    type[job] = {
        modifiers = {
            job_$_add = { Planets }   # -> job_<job>_add
        }
    }
}
```

Notes:

- Modifier entries missing `categories` will be skipped (not effective).
- Modifier names in type configs use `$` as a placeholder; ensure they correspond to the type / subtype expression.
- `supported_scopes` within categories should use standard scope IDs; case is automatically normalized during parsing.

#### Scope Config and Scope Group Config {#config-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig -->

Scope configs define "scope types" and their aliases; scope group configs group scopes together. Both are used for scope checking, link constraints, and hints.

Path location:

- Scope: `scopes/{name}`, where `{name}` is the config name.
- Scope group: `scope_groups/{name}`, where `{name}` is the config name.

Scope configs and system scopes together determine the scope stack and semantics; together with link configs, they constrain the input / output scopes of chained access. In extended configs, `## replace_scopes` can be used to specify the concrete scope types that system scopes map to in a specific context.

Example:

```cwt
# from `scopes.cwt` of stellaris config group

scopes = {
    Country = { aliases = { country } }
    Leader = { aliases = { leader } }
    System = { aliases = { galacticobject system galactic_object } }
    Planet = { aliases = { planet } }
    "Pop Group" = { aliases = { pop_group } }
    "Pop Job" = { aliases = { job pop_job } }
}

scope_groups = {
    target_species = {
        country pop_group leader planet ship fleet army species first_contact
    }
}
```

#### Database Object Type Config {#config-db-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig -->

Database object type configs are used to describe the type and format of database object expressions. Such expressions can be used as concept names in localization files (e.g. `['civic:some_civic', ...]`). They are eventually parsed into a definition or localization and rendered into UI hints.

Path location:

- `database_object_types/{name}`, where `{name}` is the config name.

Field meanings:

- `type`: If present, treats the `object` in `prefix:object` as a definition reference of this type.
- `swap_type`: If present, treats the `swap` in `prefix:object:swap` as a swapped type definition reference.
- `localisation`: If present, treats the `object` in `prefix:object` as a localisation key.

Example:

```cwt
# from `database_object_types.cwt` of stellaris config group

database_object_types = {
    civic = {
        type = civic_or_origin
        swap_type = swapped_civic
    }
    technology = {
        type = technology
        swap_type = swapped_technology
    }
    job = {
        localisation = job_
    }
}
```

#### Macro Config {#config-macro}

<!-- @see icu.windea.pls.config.config.delegated.CwtMacroConfig -->
<!-- @see cwt/cwtools-stellaris-config/config/common/inline_scripts.cwt -->
<!-- @see cwt/cwtools-vic3-config/config/definition_injections.cwt -->
<!-- @see cwt/cwtools-eu5-config/config/definition_injections.cwt -->

Macro configs describe special expressions and structures in script files that differ from regular structures, and provide additional hints and validation metadata. These expressions and structures alter the behavior of the game's runtime script parser, thereby modifying, extending, or reusing existing script fragments. Different directives can have different config structures.

Currently involved directives include:

- **Inline script (inline_script)**: (Stellaris) Replaced with the content of the target file during parsing, with parameters support.
- **Definition injection (definition_injection)**: (VIC3 / EU5) Injects into or replaces the declaration of the target definition during parsing, with mode support to determine specific behavior.

Path location:

- `macro[{name}]`, where `{name}` is the config name.

Example:

```cwt
macro[inline_script] = filepath[common/inline_scripts/,.txt]

macro[definition_injection] = {
    modes = { INJECT REPLACE TRY_INJECT TRY_REPLACE INJECT_OR_CREATE REPLACE_OR_CREATE }
    lenient_modes = { TRY_INJECT TRY_REPLACE INJECT_OR_CREATE REPLACE_OR_CREATE }
    replace_modes = { REPLACE TRY_REPLACE REPLACE_OR_CREATE }
    create_modes = { REPLACE_OR_CREATE }
}
```

### Extended Configs {#configs-extended}

> These configs are used to enhance the plugin's functionality, such as specifying config contexts, providing additional quick documentation and inlay hint text, etc.
>
> Extended configs share several common characteristics:
> - Most extended configs support multiple **name matching** methods: constants, [template expressions](#config-expression-template), [ANT path patterns](#faq-ant), and [regular expressions](#faq-regex).
> - Most extended configs support providing quick documentation text via documentation comments.
> - Most extended configs support providing inlay hint text via option comments (`## hint`).
> - Some extended configs support specifying **scope context** via option comments (`## replace_scopes` / `## push_scope`).

#### Extended Config for Scripted Variables {#config-extended-scripted-variable}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig -->

Provides additional hints (quick documentation, inlay hints, etc.) for scripted variables in scripts.

Path location:

- `scripted_variables/{name}`, where `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

Format description:

```cwt
scripted_variables = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## hint = §RSome inlay hint text§!
    x
}
```

Notes:

- The name can use template / ANT / regex matching, but avoid being too broad to prevent false matches.
- This entry only provides "hint enhancement" and is not responsible for declaring or validating the value and type of scripted variables.

#### Extended Config for Definitions {#config-extended-definition}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig -->

Provides additional context and hint information for specific "definitions", including documentation / hints (`## hint`), bound definition type (`## type`, required), and optionally specified scope context (`## replace_scopes` / `## push_scope`).

Path location:

- `definitions/{name}`, where `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

Format description:

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

Notes:

- `type` is required; if missing, the entry will be skipped.
- This extension is for "hint and context enhancement" and does not directly change the structure of [declaration configs](#config-declaration).

#### Extended Config for Game Rules {#config-extended-game-rule}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig -->

Provides documentation / hint enhancement for game rules (i.e. definitions of type `game_rule`), and supports "overriding [declaration configs](#config-declaration)".

Path location:

- `game_rules/{name}`, where `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

When the entry is a property node (e.g. `x = { ... }` or `x = single_alias_right[...]`), its value or sub-block acts as a "declaration config override" at the usage site. Only property nodes produce an override effect; pure value nodes only provide hints.

Format description:

```cwt
game_rules = {
    ### Some documentation
    ## hint = §RSome hint text§!
    x # provide hint only

    ### Some documentation
    ## replace_scopes = { this = country root = country }
    y = single_alias_right[trigger_clause] # override declaration via single alias
}
```

Example:

```cwt
# from `game_rules.cwt` of stellaris config group

game_rules = {
    ### This rule is a condition for declaring war
    ### Root = country, attacker
    ### This = country, target
    ## replace_scopes = { this = country root = country }
    can_declare_war
}
```

Notes:

- If the value is `single_alias_right[...]`, it is first inlined and expanded, then takes effect as the override config.
- This extension only affects the "source / structure of the [declaration config](#config-declaration)" and "hint information"; it does not change the overall priority and override strategy.

#### Extended Config for On Actions {#config-extended-on-action}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig -->

Provides documentation / hint enhancement for on actions (i.e. definitions of type `on_action`), and specifies the "event type" to influence event-related references in the declaration context.

Path location:

- `on_actions/{name}`, where `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

`## event_type` (required) declares the event type, used to replace event-related data expressions in the declaration context with expressions corresponding to that event type.

Format description:

```cwt
on_actions = {
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    ## event_type = country
    x
}
```

Example:

```cwt
# from `on_actions.cwt` of stellaris config group

on_actions = {
    ### Triggers when the game starts
    ## replace_scopes = { this = no_scope root = no_scope }
    ## event_type = scopeless
    on_game_start

    ## replace_scopes = { this = country root = country }
    ## event_type = country
    on_game_start_country
}
```

Notes:

- `## event_type` is required; if missing, the entry will be skipped.
- If scope replacement is needed, use `## replace_scopes` in combination.

#### Extended Config for Parameters {#config-extended-parameter}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedParameterConfig -->

Provides documentation and context enhancement for parameters (`$PARAM$` or `$PARAM|DEFAULT$`) in triggers / effects / inline scripts: binding context keys, declaring context configs and scope context, and supporting context inheritance from usage sites.

Path location:

- `parameters/{name}`, where `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

Main fields:

- `## context_key` (required): Context key (e.g. `scripted_trigger@some_trigger`); before `@` is the containing definition type (or `inline_script`), after `@` is the definition name or inline script path. The context key itself also supports pattern matching.
- `## context_configs_type`: `single` (default) or `multiple`, with the same meaning as in inline script extended configs.
- `## inherit`: Boolean option; when marked, inherits context (configs and scope) from the parameter's "usage site" rather than using static declarations.

Format description:

```cwt
parameters = {
    # 'x' is a parameter name, e.g., for '$JOB$', 'x' should be 'JOB'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## context_key = scripted_trigger@some_trigger
    x

    ## context_key = scripted_trigger@some_trigger
    x = localisation

    ## context_key = scripted_trigger@some_trigger
    ## context_configs_type = multiple
    x = {
        localisation
        scalar
    }

    # scope context options are also available
    ## context_key = scripted_trigger@some_trigger
    ## replace_scopes = { this = country root = country }
    x

    # inherit context from usage site
    ## context_key = scripted_trigger@some_trigger
    ## inherit
    x
}
```

![](../assets/config/parameters_1.png)

Notes:

- `## context_key` is required; if missing, the entry will be skipped.
- When `## inherit` is marked, the context is taken from the "usage site" and may be empty or vary by location.
- Root-level `single_alias_right[...]` is inlined and expanded before being used as a context config.

#### Extended Config for Complex Enum Values {#config-extended-complex-enum-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig -->

Provides documentation / hint enhancement (quick documentation, inlay hints, etc.) for specific entries of complex enums.

Path location:

- `complex_enum_values/{type}/{name}`, where `{type}` is the complex enum name and `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

Format description:

```cwt
complex_enum_values = {
    component_tag = {
        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x
    }
}
```

Notes:

- This extension does not change the collection logic for complex enum "value sources"; it only provides hint information.
- The name can use template / ANT / regex matching, but avoid being too broad to prevent false matches.

#### Extended Config for Dynamic Values {#config-extended-dynamic-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig -->

Provides documentation / hint enhancement for specific "dynamic value" entries under a dynamic value type.

Path location:

- `dynamic_values/{type}/{name}`, where `{type}` is the dynamic value type and `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions.

Format description:

```cwt
dynamic_values = {
    event_target = {
        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x

        # scope context options: only receive push scope (this scope)
        ## push_scope = country
        x
    }
}
```

Notes:

- This extension does not change the dynamic value type or the base "value set" definition; it only provides hint information.
- The name can use template / ANT / regex matching, but avoid being too broad to prevent false matches.

#### Extended Config for Inline Scripts {#config-extended-inline-script}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig -->

Declares "context configs" and "scope context" for specific inline scripts, used to provide correct completion and inspection at call sites.

Path location:

- `inline_scripts/{name}`, where `{name}` is the config name. The name supports constants, template expressions, ANT path patterns, and regular expressions. When `name` is `x/y`, the corresponding file is `common/inline_scripts/x/y.txt`.

`## context_configs_type` controls the aggregation form of context configs: `single` (default) takes only the value side as the context config; `multiple` takes the sub-config list as context configs.

Format description:

```cwt
inline_scripts = {
    # 'x' is an inline script expression
    # e.g., for 'inline_script = jobs/researchers_add', 'x' should be 'jobs/researchers_add'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to declare context config(s)

    x

    ## context_configs_type = multiple
    x = {
        ## cardinality = 0..1
        potential = single_alias_right[trigger_clause]
        ## cardinality = 0..1
        possible = single_alias_right[trigger_clause]
    }

    # scope context options are also available
    ## replace_scopes = { this = country root = country }
    x

    # using single alias at root level is also available
    ## context_configs_type = multiple
    x = single_alias_right[trigger_clause]
}
```

![](../assets/config/inline_scripts_1.png)

Notes:

- If only a single context config is needed, keep the default `single`; use `multiple` when declaring multiple.
- Root-level `single_alias_right[...]` is inlined and expanded before being used as a context config.

### Internal Configs {#configs-internal}

> These configs are used internally by the plugin and do not support customization (or do not yet support it).

#### Schema Config {#config-internal-schema}

<!-- @see icu.windea.pls.config.config.internal.CwtSchemaConfig -->
<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->
<!-- @see icu.windea.pls.config.util.CwtConfigSchemaManager -->
<!-- @see cwt/core/internal/schema.cwt -->

Schema configs provide declarations for the right-side value forms of "config files themselves", used for basic-level completion and (limited) structural validation. Currently focused on "preliminary completion" and does not yet provide strict schema validation.

Schema configs originate only from the built-in file `internal/schema.cwt` and cannot be overridden by external files. Their structure contains three types of entries:

- **Properties** (`properties`): Keys parsed as constant, type, or template forms.
- **Enums** (`enums`): Keys parsed as enum expressions (e.g. `$enum:NAME$`).
- **Constraints** (`constraints`): Keys parsed as constraint expressions (e.g. `$$NAME`).

#### Folding Settings Config {#config-internal-folding}

<!-- @see icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig -->
<!-- @see icu.windea.pls.lang.folding.ParadoxExpressionFoldingBuilder -->
<!-- @see cwt/core/internal/folding_settings.cwt -->

Folding settings configs provide additional code folding configs for the editor (internal use, customization not yet supported).

Originating only from the built-in file `internal/folding_settings.cwt`. Each entry is read per group and contains:

- `id`: Folding item ID (corresponding to config file path or expression).
- `key`: Folding item key.
- `keys`: Alternative key set.
- `folding_key`: Display key used for folding.
- `placeholder` (option comment `## placeholder`): Placeholder text after folding.

#### Postfix Template Settings {#config-internal-postfix-template}

<!-- @see icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig -->
<!-- @see icu.windea.pls.lang.codeInsight.postfix.ParadoxPostfixTemplateProvider -->
<!-- @see cwt/core/internal/postfix_template_settings.cwt -->

Postfix template settings configs provide postfix completion templates for the editor (internal use, customization not yet supported).

Originating only from the built-in file `internal/postfix_template_settings.cwt`. Each entry contains:

- `id`: Template ID.
- `key`: Keyword that triggers the postfix.
- `example`: Example text for display.
- `expression`: Template expression (using `$EXPR$` as placeholder).
- `context_expression` (option comment `## context_expression`): Expression constraining the template's available context.

## Config Expressions {#config-expressions}

<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->

> This chapter introduces the purpose, format, and default / boundary behaviors of various config expressions, helping readers correctly understand and write these special expressions.

### Overview {#config-expressions-overview}

Configs expressions are structured syntax used in config fields.

#### Category {#config-expressions-category}

This chapter covers the following config expressions:

- **[Data Expression](#config-expression-data)**: Describes the value form and matching patterns of expressions, yielding a specific data type after parsing.
- **[Template Expression](#config-expression-template)**: A pattern composed of constants and dynamic fragments concatenated, used for more flexible matching.
- **[Cardinality Expression](#config-expression-cardinality)**: Constrains the number of occurrences of definition members.
- **[Location Expression](#config-expression-location)**: Locates the source of resources such as images and localisation.
- **[Schema Expression](#config-expression-schema)**: Provides declarations for the value forms of config files themselves.

### Data Expression {#config-expression-data}

<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->

Data expressions are used to describe the value forms and matching patterns of various expressions, which determine their matching logic and parsing logic.

The data expression will get the specific [data type] (#data-types) after parsing, and can be accompanied by metadata. The data type, along with this metadata, determines which expressions in script files, localization files, and CSV files can be matched by data expressions.

**Default and boundary behaviors**:

- Blocks (`{ ... }`) correspond to the data type `Block`.
- Empty strings (`""`) correspond to the data type `Constant`, using themselves as the constant value.
- When no known data type can be matched, falls back to `Constant`, using the original string as the constant value.
- Definition references should use the angle-bracket form (e.g. `<event>`), not the bracket form with prefix (e.g. `definiton[event]`, which is an incorrect notation).

Example:

```cwt
int                         # integer
float[0.0..1.0]             # float with range constraint
enum[shipsize_class]        # enum reference
scope[country]              # scope reference
<ship_size>                 # definition reference
value[event_target]         # dynamic value reference
pre_<opinion_modifier>_suf  # template expression (with definition reference fragment)
```

### Template Expression {#config-expression-template}

<!-- @see icu.windea.pls.config.configExpression.CwtTemplateExpression -->

Template expressions are composed of multiple fragments concatenated — constant fragments and dynamic fragments alternating — used to describe value forms more complex than a single data expression. Each dynamic fragment is itself a restricted data expression (such as definition reference, enum reference, dynamic value reference, etc.).

**Parsing constraints**:

- Text containing whitespace characters is treated as an invalid template.
- When only one fragment exists (purely constant or purely a single dynamic), it is not treated as a template but as a regular data expression.
- Multiple fragments use a "leftmost earliest match" splitting strategy.
- Each fragment ultimately delegates to data expression parsing; when no known type is matched, it degrades to a constant fragment.

Example:

The following examples demonstrate typical usage of template expressions, with `#` comments annotating the splitting of fragments:

```cwt
job_<job>_add             # "job_" + <job> + "_add"
xxx_value[anything]_xxx   # "xxx_" + value[anything] + "_xxx"
a_enum[weight_or_base]_b  # "a_" + enum[weight_or_base] + "_b"
```

For example, `job_<job>_add` can match `job_researcher_add`, `job_farmer_add`, etc. — where the `<job>` part matches any definition name of type `job`.

**Notes**:

- When constant fragments and dynamic config names are adjacent, the parser prioritizes correct identification of dynamic configs.
- Template expressions do not support whitespace characters; if whitespace matching is needed, use [ANT path patterns](#faq-ant) or [regular expressions](#faq-regex) instead.

### Cardinality Expression {#config-expression-cardinality}

<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

Cardinality expressions constrain the number of occurrences of definition members, affecting code inspection and code completion features. Declared via the option comment `## cardinality`.

The format is `{min}..{max}`, where `{min}` and `{max}` are non-negative integers or `inf` (case-insensitive, meaning unlimited).
Adding a `~` prefix before the integer indicates lenient validation (when not satisfied, a weaker severity level is used, e.g. from warning to weak warning).

**Default and boundary behaviors**:

- A minimum value that is negative is clamped to 0.
- Missing the `..` separator is treated as invalid, producing no constraint.
- When `min > max`, treated as invalid, producing no constraint.

Example:

```cwt
## cardinality = 0..1     # optional, at most 1 occurrence
## cardinality = 0..inf   # optional, unlimited occurrences
## cardinality = 1..5     # must occur 1 to 5 times
## cardinality = ~1..10   # lenient: expected 1 to 10 times, but produces only a warning by default if not met
```

**Tip:**

- You can use `## cardinality_min_define` to dynamically obtain the minimum cardinality from a define variable in specified expression (e.g., `## cardinality_min_define = NGameplay.ETHOS_MIN_POINTS`).
- You can use `## cardinality_max_define` to dynamically obtain the maximum cardinality from a define variable in specified expression (e.g., `## cardinality_max_define = NGameplay.ETHOS_MAX_POINTS`).

### Location Expression {#config-expression-location}

<!-- @see icu.windea.pls.config.configExpression.CwtLocationExpression -->

Location expressions are used to locate the source of target resources (images, localisation, etc.). The `$` in expressions is a placeholder that is replaced at runtime with dynamic content such as "definition name" or "property value".

Location expressions use `|` to separate arguments, with the format `<location>|<args...>`. Different types of location expressions interpret arguments differently; see below for details.

#### Image Location Expression {#config-expression-location-image}

<!-- @see icu.windea.pls.config.configExpression.CwtImageLocationExpression -->

Used to locate images related to definitions. The location part can be a file path (e.g. `gfx/.../mod_$.dds`), a sprite name (e.g. `GFX_$`), or a property key name (e.g. `icon`). If it is a property key name, the image pointed to by that property value is further resolved.

Argument conventions:

- Arguments starting with `$` represent "name text source paths" (supporting comma-separated multiple paths), used to replace the `$` placeholder in the location.
- Other arguments represent "frame source paths" (supporting comma-separated multiple paths), used for image frame slicing.
- When arguments of the same type appear repeatedly, the later one takes precedence.

Example:

```cwt
gfx/interface/icons/modifiers/mod_$.dds
gfx/interface/icons/modifiers/mod_$.dds|$name
GFX_$
icon
icon|p1,p2
```

#### Localisation Location Expression {#config-expression-location-localisation}

<!-- @see icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression -->

Used to locate localisation related to definitions. The location part can be a localisation key pattern containing a `$` placeholder (e.g. `$_desc`), or a property key name (e.g. `title`).

Argument conventions:

- Arguments starting with `$` represent "name text source paths" (supporting comma-separated multiple paths), used to replace the `$` placeholder in the location.
- The argument `u` forces the final name to uppercase (only effective when using placeholders).
- When `$` arguments appear repeatedly, the later one takes precedence.

Example:

```cwt
$_desc
$_desc|$name
$_desc|$name|u
$_desc|$name,$alt_name
title
```

### Schema Expression {#config-expression-schema}

<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->

Schema expressions describe the value forms of keys and values in config files, thereby providing features such as code completion for config files themselves. Currently used only for providing basic code completion, and only in the built-in file `internal/schema.cwt`. Works in conjunction with [Internal Configs → Schema Config](#config-internal-schema).

Schema expressions support the following four forms:

- **Constant**: A literal string not containing `$`, such as `types`, `enums`.
- **Template**: A pattern containing one or more `$...$` parameters, such as `$type$`, `type[$type$]`.
- **Type**: Starting with a single `$` (unclosed), such as `$any`, `$int`.
- **Constraint**: Starting with `$$`, such as `$$declaration`.

## Data Types {#data-types}

<!-- @see icu.windea.pls.config.CwtDataType -->
<!-- @see icu.windea.pls.config.CwtDataTypes -->
<!-- @see icu.windea.pls.config.CwtDataTypeSets -->
<!-- @see icu.windea.pls.ep.config.configExpression.CwtDataExpressionSupport -->
<!-- @see icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher -->
<!-- @see icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher -->
<!-- @see icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport -->
<!-- @see icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport -->
<!-- @see icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport -->

> This chapter introduces the concepts, classification, and usage of data types, helping readers understand how data expressions in config files match actual content in script files.

### Overview {#data-types-overview}

Data types describe the type of data expression (as the most common type of config expression).

Each data type represents a semantic category and participates in determining the matching logic between expressions and config expressions.

Examples:
- For data expression `<event.country>`, the data type is `Definition`, with metadata `event.country`, indicating that it matches definition references of type `event` whose subtype includes `country`.
- the data expression `enum[weight_or_base]`, the data type is `EnumValue`, with metadata `weight_or_base`, indicating that it matches any of the possible values declared in that enum.

Related extension points:
- The resolution logic for data types is driven by the extension point `icu.windea.pls.dataExpressionSupport`.
- The matching logic between expressions in script files and config expressions is driven by the extension point `icu.windea.pls.scriptExpressionMatcher`.
- The matching logic between expressions in CSV files and config expressions is driven by the extension point `icu.windea.pls.csvExpressionMatcher` (limited support).
- The implementation logic for expressions in script files is driven by the extension point `icu.windea.pls.scriptExpressionSupport`.
- The implementation logic for expressions in localisation files is driven by the extension point `icu.windea.pls.localisationExpressionSupport` (special support).
- The implementation logic for expressions in CSV files is driven by the extension point `icu.windea.pls.csvExpressionSupport` (limited support).

### Basic Data Types {#data-types-base}

The following data types represent basic value forms.

#### Any {#data-type-any}

Any type.

Matches any script expression, acting as the lowest-priority fallback.

Format of corresponding data expressions:
- `$any`

#### Bool {#data-type-bool}

Boolean type.

Matches boolean values (`yes` / `no`).

Format of corresponding data expressions:
- `bool`

#### Int {#data-type-int}

Integer type.

Matches integer values.
When a range parameter is present, it also constrains the numeric range (for inspection only; still considered a match).
Quoted numbers are also considered a match (compatible with vanilla game files).

The range parameter can be any combination of open and closed intervals; by convention, `inf` denotes infinity.

Format of corresponding data expressions:
- `int`
- `int{range}` – where `{range}` matches a range parameter (e.g., `[0..1]` `[-100..100)` `[0..inf)`).

#### Float {#data-type-float}

Float type.

Matches floating-point values.
When a range parameter is present, it also constrains the numeric range (for inspection only; still considered a match).
Quoted numbers are also considered a match (compatible with vanilla game files).

The range parameter can be any combination of open and closed intervals; by convention, `inf` denotes infinity.

Format of corresponding data expressions:
- `float`
- `float{range}` – where `{range}` matches a range parameter (e.g., `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`).

#### Scalar {#data-type-scalar}

Scalar type.

Matches most non-clause expressions (strings, numbers, booleans, etc.), acting as a low-priority broad match.
Always matches when used as a key. The `wildcard_scalar` variant sets a wildcard flag.

Format of corresponding data expressions:
- `scalar`
- `wildcard_scalar` – wildcard variant.

#### ColorField {#data-type-color-field}

Color field type.

Matches script color fields (e.g., `rgb { 255 255 255 }`).
When a parameter is present, it also validates the color type prefix.

Format of corresponding data expressions:
- `colour_field` `color_field`
- `colour[{type}]` `color[{type}]` – where `{type}` matches a color type (possible values: `rgb` `hsv` `hsv360`).

#### Block {#data-type-block}

Block type.

Matches script blocks (`{ ... }`). Applies only to script expressions used as values, and recursively matches the block contents.

Used only for internal representation and does not correspond to a config expression string.

### Extended Basic Data Types {#data-types-extended-base}

#### PercentageField {#data-type-percentage-field}

Percentage field type.

Matches percentage value strings where the numeric part is a float (e.g., `50.0%`).

Format of corresponding data expressions:
- `percentage_field`

#### IntPercentageField {#data-type-int-percentage-field}

Integer percentage field type.

Matches percentage value strings where the numeric part is an integer (e.g., `50%`).

Format of corresponding data expressions:
- `int_percentage_field`

#### DateField {#data-type-date-field}

Date field type.

Matches date value strings (e.g., `2200.1.1`). When a parameter is present, it also validates the date format.

Format of corresponding data expressions:
- `date_field`
- `date_field[{format}]` – where `{format}` matches a date format (e.g., `y.M.d`).

### Reference Data Types {#data-types-reference}

The following data types match by referencing content from other configs or indexes. Some of them match specific complex expressions.

#### Definition {#data-type-definition}

Definition reference type.

Matches a reference to a definition of a specified type. The expression must be a valid identifier (`.` and `-` are allowed).  
It can be an integer or a float (as in the case of `<technology_tier>`).  
Validates that the referenced definition exists when matching.

Format of corresponding data expressions:
- `<{type}>` – where `{type}` matches a type name.
- `<{type}.{subtypes}>` – where `{type}` matches a type name and `{subtypes}` matches a dot-separated list of subtype names.

Examples of corresponding data expressions:
- `<building>` – matches a building name reference.
- `<event>` – matches an event ID reference.
- `<event.country>` – matches an event ID reference.
- `<technology_tier>` – matches a technology tier reference. This is an integer rather than a string.

#### Localisation {#data-type-localisation}

Localisation reference type.

Matches a reference to a localisation key. The expression must be a valid identifier (`.`, `-`, `'` are allowed).  
Validates that the referenced localisation exists when matching.  
The localisation file containing the referenced key must be located in the `localisation` or `localization` directory (or its subdirectories).

Format of corresponding data expressions:
- `localisation`

#### SyncedLocalisation {#data-type-synced-localisation}

Synced localisation reference type.

Similar to [Localisation](#data-type-localisation), but points to a synced localisation key.  
The localisation file containing the referenced key must be located in the `localisation_synced` or `localization_synced` directory (or its subdirectories).

Format of corresponding data expressions:
- `localisation_synced`

#### InlineLocalisation {#data-type-inline-localisation}

Inline localisation reference type.

Matches either a localisation key reference or any quoted string (the latter is treated as inline text and returned as a fallback match).

Format of corresponding data expressions:
- `localisation_inline`

#### Modifier {#data-type-modifier}

Modifier reference type.

Matches a reference to a modifier. The expression must be a valid identifier.  
Validates that the referenced modifier exists within the config group when matching. Has higher priority than [Definition](#data-type-definition).

Format of corresponding data expressions:
- `<modifier>`

#### EnumValue {#data-type-enum-value}

Enum value type.

Matches a reference to an enum value.  
For simple enums, it matches exactly against the list of enum values; for complex enums, it queries through the index.

Format of corresponding data expressions:
- `enum[{name}]` – where `{name}` matches an enum name.

Examples of corresponding data expressions:
- `enum[weight_or_base]`
- `enum[ship_class]`

#### Value {#data-type-value}

Dynamic value read type.

Matches a dynamic value expression (e.g., `target`, `target@root`, `target@root.owner`), representing a read reference to a declared dynamic value.  
The dynamic value name must be a valid identifier (`.` is allowed).

Format of corresponding data expressions:
- `value[{name}]` – where `{name}` matches a dynamic value type name.

Examples of corresponding data expressions:
- `value[event_target]`

#### ValueSet {#data-type-value-set}

Dynamic value write type.

Matches a dynamic value expression (e.g., `target`, `target@root`, `target@root.owner`), representing a write (declaration) reference to a dynamic value.  
The dynamic value name must be a valid identifier (`.` is allowed).

Format of corresponding data expressions:
- `value_set[{name}]` – where `{name}` matches a dynamic value type name.

Examples of corresponding data expressions:
- `value_set[event_target]`

#### DynamicValue {#data-type-dynamic-value}

Dynamic value type.

Matches a dynamic value expression (e.g., `target`, `target@root`, `target@root.owner`), representing a reference to a dynamic value (without distinguishing between read and write).  
The dynamic value name must be a valid identifier (`.` is allowed).

Format of corresponding data expressions:
- `dynamic_value[{name}]` – where `{name}` matches a dynamic value type name.

Examples of corresponding data expressions:
- `dynamic_value[event_target]`

#### ScopeField {#data-type-scope-field}

Scope field type.

Matches a scope field expression (consisting of multiple scope nodes separated by dots forming a chain, e.g., `root`, `root.owner`, `root.event_target:target`).

Format of corresponding data expressions:
- `scope_field`

#### Scope {#data-type-scope}

Scope type.

Matches a scope field expression (consisting of multiple scope nodes separated by dots forming a chain, e.g., `root`, `root.owner`, `root.event_target:target`), while also constraining the output scope type.  
When the parameter is `any`, it is equivalent to [ScopeField](#data-type-scope-field).

Format of corresponding data expressions:
- `scope[{type}]` – where `{type}` matches a scope type name. Use `any` for any type.

Examples of corresponding data expressions:
- `scope[country]`
- `scope[any]`

#### ScopeGroup {#data-type-scope-group}

Scope group type.

Matches a scope field expression (consisting of multiple scope nodes separated by dots forming a chain, e.g., `root`, `root.owner`, `root.event_target:target`), while also constraining the output scope to belong to a specified scope group.

Format of corresponding data expressions:
- `scope_group[{name}]` – where `{name}` matches a scope group name.

Examples of corresponding data expressions:
- `scope_group[economic_categories]`

#### ValueField {#data-type-value-field}

Value field type.

Matches a float or a value field expression (consisting of zero or more scope nodes and a final value field node, separated by dots forming a chain, e.g., `var`, `root.var`, `root.value:sv`).  
When a range parameter is present, it also constrains the numeric range (for annotation only; still considered a match).

The range parameter can be any combination of open and closed intervals; by convention, `inf` denotes infinity.

Format of corresponding data expressions:
- `value_field`
- `value_field{range}` – where `{range}` matches a range parameter (e.g., `[0.0..1.0]`, `[-100.0..100.0)`, `[0.0..inf)`).

Examples of corresponding data expressions:
- `value_field`
- `value_field[0.0..1.0]`

#### IntValueField {#data-type-int-value-field}

Integer value field type.

Matches an integer or an integer value field expression (consisting of zero or more scope nodes and a final value field node, separated by dots forming a chain, e.g., `var`, `root.var`, `root.value:sv`).  
When a range parameter is present, it also constrains the numeric range (for annotation only; still considered a match).

The range parameter can be any combination of open and closed intervals; by convention, `inf` denotes infinity.

Format of corresponding data expressions:
- `int_value_field`
- `int_value_field{range}` – where `{range}` matches a range parameter (e.g., `[0..1]`, `[-100..100)`, `[0..inf)`).

Examples of corresponding data expressions:
- `int_value_field`
- `int_value_field[-100..100]`

#### VariableField {#data-type-variable-field}

Variable field type.

Matches a float or a variable field expression (consisting of zero or more scope nodes and a final variable node, separated by dots forming a chain, e.g., `var`, `root.var`).  
Can be considered a special subset of [ValueField](#data-type-value-field).  
When a range parameter is present, it also constrains the numeric range (for annotation only; still considered a match).

The range parameter can be any combination of open and closed intervals; by convention, `inf` denotes infinity.

Format of corresponding data expressions:
- `variable_field`
- `variable_field{range}` – where `{range}` matches a range parameter (e.g., `[0.0..1.0]`, `[-100.0..100.0)`, `[0.0..inf)`).
- `variable_field_32` – 32-bit variant.
- `variable_field_32{range}` – 32-bit variant, where `{range}` matches a range parameter (e.g., `[0.0..1.0]`, `[-100.0..100.0)`, `[0.0..inf)`).

Examples of corresponding data expressions:
- `variable_field`
- `variable_field[0.0..1.0]`
- `variable_field_32`

#### IntVariableField {#data-type-int-variable-field}

Integer variable field type.

Matches an integer or an integer variable field expression (consisting of zero or more scope nodes and a final variable node, separated by dots forming a chain, e.g., `var`, `root.var`).  
Can be considered a special subset of [IntValueField](#data-type-int-value-field).  
When a range parameter is present, it also constrains the numeric range (for annotation only; still considered a match).

The range parameter can be any combination of open and closed intervals; by convention, `inf` denotes infinity.

Format of corresponding data expressions:
- `int_variable_field`
- `int_variable_field{range}` – where `{range}` matches a range parameter (e.g., `[0..1]`, `[-100..100)`, `[0..inf)`).
- `int_variable_field_32` – 32-bit variant.
- `int_variable_field_32{range}` – 32-bit variant, where `{range}` matches a range parameter (e.g., `[0..1]`, `[-100..100)`, `[0..inf)`).

Examples of corresponding data expressions:
- `int_variable_field`
- `int_variable_field[-100..100]`
- `int_variable_field_32`

#### Command {#data-type-command}

Command expression type.

Matches a command expression (consisting of zero or more command scope nodes and a final command field node, separated by dots forming a chain, e.g., `GetName`, `Root.GetName`).  
Command expressions are widely used in localisation files (`[...]`); however, currently this is only a placeholder and does not support matching expressions in script files.

Format of corresponding data expressions:
- `$command`

#### DefineReference {#data-type-define-reference}

Define reference expression type.

Matches a define reference expression (e.g., `define:Namespace|Variable`).

Format of corresponding data expressions:
- `$define_reference`

#### ArrayDefineReference {#data-type-array-define-reference}

Array define reference expression type.

Matches an array define reference expression (e.g., `array_define:Namespace|Variable|0`).

Format of corresponding data expressions:
- `$array_define_reference`

(To be implemented in 2.1.10)

#### Tags {#data-type-tags}

Tag set expression type.

Matches a tag set expression (consisting of a group of dynamic value nodes separated by commas, e.g., `tag`, `tag1,tag2`), or an empty string.  
In the condition variant, individual dynamic value nodes can be negated (e.g., `tag1,not(tag2)`).

Format of corresponding data expressions:
- `$tags[{name}]` – where `{name}` matches a dynamic value type name.
- `$tags_condition[{name}]` – condition variant, where `{name}` matches a dynamic value type name.

(To be implemented in 2.1.10)

#### DatabaseObject {#data-type-database-object}

Database object expression type.

Matches a database object expression (consisting of multiple reference nodes separated by colons, e.g., `building:x`, `civic:x:y`).

Format of corresponding data expressions:
- `$database_object`

#### NameFormat {#data-type-name-format}

Name format expression type.

Matches a name format expression (e.g., `{alpha}`, `{<adj> {<noun>}}`).

Format of corresponding data expressions:
- `name_format[{type}]` - where `{name}` matches the format name (the corresponding definition type is `{name}_name_format`).

#### ShaderEffect {#data-type-shader-effect}

Shader effect type.

Matches a reference to a shader effect.  
The plugin currently treats these references as dynamic references, even though their declarations actually reside in `.shader` files.

"Dynamic reference" means that there is no actual declaration site, and only read/write access is distinguished — just like dynamic values. Here, only read access is always assumed.

Format of corresponding data expressions:
- `$shader_effect`

#### MeshLocator {#data-type-mesh-locator}

Mesh locator type.

Matches a reference to a mesh locator.  
The plugin currently treats these references as dynamic references, even though their declarations actually reside in `.mesh` files.

"Dynamic reference" means that there is no actual declaration site, and only read/write access is distinguished — just like dynamic values. Here, only read access is always assumed.

Format of corresponding data expressions:
- `$mesh_locator`

#### TechnologyWithLevel {#data-type-technology-with-level}

Technology with level type.

Matches a technology reference with a level (e.g., `some_repeatable_tech@1`), where the technology name and level are separated by `@`.  
Only applies to the Stellaris game type, and has lower priority than [Definition](#data-type-definition).

Format of corresponding data expressions:
- `$technology_with_level`

#### Parameter {#data-type-parameter}

Parameter name type.

Matches a parameter name. The expression must be a valid identifier. It is considered a match even if the parameter name does not exist in the corresponding definition declaration.

Format of corresponding data expressions:
- `$parameter`

#### ParameterValue {#data-type-parameter-value}

Parameter value type.

Matches a parameter value. Matches anything as long as it is not a block.

Format of corresponding data expressions:
- `$parameter_value`

#### LocalisationParameter {#data-type-localisation-parameter}

Localisation parameter name type.

Matches a localisation parameter name. The expression must be a valid identifier (`.`, `-`, `'` are allowed).

Format of corresponding data expressions:
- `$localisation_parameter`

### Alias Data Types {#data-types-alias}

The following data types are related to the alias resolution mechanism. They generally do not directly participate in script matching, but are handled internally by the alias system.

#### SingleAliasRight {#data-type-single-alias-right}

Single alias right type.

Does not directly participate in script matching; handled by the alias resolution mechanism. Can only be used to match property values.

Format of corresponding data expressions:
- `single_alias_right[{name}]` – where `{name}` matches a single alias name.

#### AliasKeysField {#data-type-alias-keys-field}

Alias keys field type.

When matching, resolves alias sub-keys and matches recursively.

Format of corresponding data expressions:
- `alias_keys_field[{name}]` – where `{name}` matches an alias name.

#### AliasName {#data-type-alias-name}

Alias name type.

When matching, resolves alias sub-keys and matches recursively. Can only be used to match property keys, and must be combined with [AliasMatchLeft](#data-type-alias-match-left).

Format of corresponding data expressions:
- `alias_name[{name}]` – where `{name}` matches an alias name.

#### AliasMatchLeft {#data-type-alias-match-left}

Alias match left type.

Does not directly participate in script matching; handled by the alias resolution mechanism. Can only be used to match property values, and must be combined with [AliasName](#data-type-alias-name).

Format of corresponding data expressions:
- `alias_match_left[{name}]` – where `{name}` matches an alias name.

### Path Reference Data Types {#data-types-path-reference}

The following data types are used to match file path references, and validate whether the referenced file exists when matching.

#### Icon {#data-type-icon}

Icon path type.

Matches references to icon file paths.
When matching, it verifies whether the referenced image file exists. A path pattern may be specified to restrict the parent path.

Notes:
- For path references, "/", "\", repeated separators, or separators appearing as suffixes are all supported and will be normalized.
- For path references, separators as prefixes are not supported; i.e., absolute path form is **not supported**.
- File extensions are not distinguished.

Corresponding data expression format:
- `icon[{path}]` – where `{path}` matches a path pattern (e.g., `gfx/interface/icons`).

Corresponding data expression example:
- `icon[gfx/interface/icons]`

#### FilePath {#data-type-file-path}

File path type.

Matches references to file paths.
When matching, it verifies whether the referenced file exists. A path pattern may be specified to restrict the parent path and file extension, or a relative path may be used for location.

Notes:
- For path references, "/", "\", repeated separators, or separators appearing as suffixes are all supported and will be normalized.
- For path references, separators as prefixes are supported and will be ignored; i.e., absolute path form is **supported**.

Corresponding data expression format:
- `filepath` – locates relative to the entry path.
- `filepath[./]` – locates relative to the current script file.
- `filepath[{path}]` – where `{path}` matches a path pattern.

Corresponding data expression examples:
- `filepath`
- `filepath[./]`
- `filepath[flags/]`
- `filepath[common/inline_scripts/,.txt]`

#### FileName {#data-type-file-name}

File name type.

Matches references to file names.
When matching, it verifies whether the referenced file exists. A path pattern may be specified to restrict the parent path.

Notes:
- For path references, "/", "\", repeated separators, or separators appearing as suffixes are all supported and will be normalized.
- For path references, separators as prefixes are not supported; i.e., absolute path form is **not supported**.
- Only the file name is distinguished.

Corresponding data expression format:
- `filename`
- `filename[{path}]` – where `{path}` matches a path pattern.

Corresponding data expression examples:
- `filename`
- `filename[gfx/models]`

#### AbsoluteFilePath {#data-type-absolute-file-path}

Absolute file path type.

Matches an absolute file path string.  
When matching, only validates as a string type (wildcard match).

Format of corresponding data expressions:
- `abs_filepath`

### Pattern-Aware Data Types {#data-types-pattern-aware}

The following data types adopt special pattern-matching strategies. The constant data type [Constant](#data-type-constant) also belongs to this category.

#### Constant {#data-type-constant}

Constant type.

Matches an expression that is exactly identical to the constant value.
When used as a value, boolean constants (`yes` / `no`) will not match quoted string literals.
Additionally, string literals that do not contain special characters (`:.@[]<>`) are resolved as this type as a fallback.

Format of corresponding data expressions:
- Use the constant value directly as the data expression string itself, e.g., `yes`, `10`, `trigger`, etc.

#### TemplateExpression {#data-type-template-expression}

Template expression type.

A pattern consisting of alternating constant text fragments and reference fragments.
When matching, the script expression is split according to the template structure, and each reference fragment is validated individually.

Examples of corresponding data expressions:
- `a_<b>_enum[c]_value[d]`
- `job_<job>_add`

This is a pattern-aware type, and its data expression format is the template expression itself (see [Template Expression](#config-expression-template)).

#### Ant {#data-type-ant}

ANT path pattern type (pattern-aware).

Matches expressions that conform to ANT path patterns. Supports wildcards `?` (single character), `*` (single segment), and `**` (multiple segments, less commonly used).

Format of corresponding data expressions:
- `ant:{pattern}` – where `{pattern}` matches a pattern.
- `ant.i:{pattern}` – case-insensitive variant.

Examples of corresponding data expressions:
- `ant:**/*.txt`
- `ant.i:common/**/*`

#### Regex {#data-type-regex}

Regular expression pattern type (pattern-aware).

Matches expressions that conform to a regular expression.

Format of corresponding data expressions:
- `re:{pattern}` – where `{pattern}` matches a pattern.
- `re.i:{pattern}` – case-insensitive variant.

Examples of corresponding data expressions:
- `re:^country_.*`
- `re.i:event_.*`

### Suffix-Aware Data Types {#data-types-suffix-aware}

#### SuffixAwareDefinition {#data-type-suffix-aware-definition}

Suffix-aware definition reference type.

Consists of a base definition reference and a comma-separated list of suffixes. When matching, both the definition reference and the suffixes are validated.
If the suffix list is empty, it degrades to a plain [Definition](#data-type-definition).

Format of corresponding data expressions:
- `<{type}>|{suffixes}` – where `{type}` matches a type name, and `{suffixes}` matches a comma-separated set of suffixes.
- `<{type}.{subtypes}>|{suffixes}` – where `{type}` matches a type name, `{subtypes}` matches a dot-separated set of subtype names, and `{suffixes}` matches a comma-separated set of suffixes.

Example of a corresponding data expression:
- `<event>|country,crisis`

#### SuffixAwareLocalisation {#data-type-suffix-aware-localisation}

Suffix-aware localisation reference type.

Consists of a base localisation reference and a comma-separated list of suffixes. When matching, both the localisation reference and the suffixes are validated.
If the suffix list is empty, it degrades to a plain [Localisation](#data-type-localisation).

Format of corresponding data expressions:
- `localisation|{suffixes}` – where `{suffixes}` matches a comma-separated set of suffixes.

Example of a corresponding data expression:
- `localisation|name,desc`

#### SuffixAwareSyncedLocalisation {#data-type-suffix-aware-synced-localisation}

Suffix-aware synced localisation reference type.

Consists of a base synced localisation reference and a comma-separated list of suffixes. When matching, both the synced localisation reference and the suffixes are validated.
If the suffix list is empty, it degrades to a plain [SyncedLocalisation](#data-type-synced-localisation).

Format of corresponding data expressions:
- `localisation_synced|{suffixes}` – where `{suffixes}` matches a comma-separated set of suffixes.

## FAQ {#faq}

#### About Template Expressions {#faq-template}

<!-- @see icu.windea.pls.config.CwtDataTypes.TemplateExpression -->

Template expressions are composed of multiple [data expression](#config-expression-data) fragments (such as definition references, enum references, dynamic value references, etc.) combined with constant fragments, used for more flexible matching. See the [Template Expression](#config-expression-template) chapter for details.

The following examples demonstrate the progression from simple literals to complex templates:

- `x`: String literal, exactly matches `x`.
- `a_<job>_b`: Template containing the definition reference `<job>`, can match `a_researcher_b`, `a_farmer_b`, etc.
- `a_enum[weight_or_base]_b`: Template containing the enum reference `enum[weight_or_base]`, can match `a_weight_b` and `a_base_b`.
- `a_value[anything]_b`: Template containing the dynamic value reference `value[anything]`. Since `value[anything]` typically has no value restrictions, the effect is similar to the regex `a_.*_b`.

Example:

```cwt
x
a_<job>_b
a_enum[weight_or_base]_b
a_value[anything]_b
```

#### How to Use ANT Path Patterns in Config Files {#faq-ant}

<!-- @see icu.windea.pls.config.CwtDataTypes.Ant -->

Starting from plugin version 1.3.6, ANT path patterns can be used in data expressions for more flexible matching. ANT expressions are identified by prefix: `ant:` for case-sensitive, `ant.i:` for case-insensitive.

ANT path patterns support the following wildcards:

- `?`: Matches any single character.
- `*`: Matches any characters (excluding `/`).
- `**`: Matches any characters (including `/`).

Example:

```cwt
ant:/foo/bar?/*
ant.i:/foo/bar?/*
```

#### How to Use Regular Expressions in Config Files {#faq-regex}

<!-- @see icu.windea.pls.config.CwtDataTypes.Regex -->

Starting from plugin version 1.3.6, regular expressions can be used in data expressions for more flexible matching. Regular expressions are identified by prefix: `re:` for case-sensitive, `re.i:` for case-insensitive. The part after the prefix is a standard regular expression.

Example:

```cwt
re:foo.*
re.i:foo.*
```

#### How to Specify the Occurrence Count of a Definition Member in Config Files {#faq-cardinality}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.cardinality -->
<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

In config files, the occurrence range of a defined member is specified through the `## cardinality` option.  
The value of this option is a [cardinality expression](#config-expression-cardinality), which constrains the number of times a defined member can appear, affecting features such as code inspection and code completion.

If not explicitly specified, and the data type of this member config is constant or enum value, it is inferred as `1..~1`; otherwise, it defaults to `0..inf`.

Example:

```cwt
# optional, redeclaration is not allowed
## cardinality = 0..1
status = bool

# required, redeclaration is lenient allowed
value = float

# required, redeclaration is not allowed
## cardinality = 1..1
type = float

# optional, redeclaration is allowed
effect = single_alias_right[effect_clause]
```

#### How to Specify Scope Context in Config Files {#faq-scope-context}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.pushScope -->
<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.replaceScopes -->

In config files, scope context is specified via the options `## push_scope` and `## replace_scopes` (or `## replace_scope`).

`## push_scope = x` pushes the specified scope type onto the current scope stack.

`## replace_scopes = { this = x root = y}` replaces the specified system scope to scope type mappings into the current scope context. Only `this`, `root`, and `from`-based system scopes are supported; `prev`-based system scopes are not supported.

Example:

```cwt
# for this example, the next this scope will be `country`
## push_scope = country
some_config = single_alias_right[trigger_clause]

# for this example, the next this scope will be `country`
# so do the next root scope, the next from scope, and the next fromfrom scope
## replace_scopes = { this = country root = country from = country fromfrom = country }
some_config = single_alias_right[trigger_clause]
```

#### How to Specify Supported Scopes in Config Files {#faq-supported-scopes}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.supportedScopes -->

In config files, the supported scopes for triggers and effects are specified via the option `## scopes` (or `## scope`).

Example:

```cwt
# for this example, the supported scope type of trigger `has_country_flag` is `country`
## scopes = { country }
alias[trigger:has_country_flag] = value[country_flag]
```

#### How to Specify Color Types in Config Files {#faq-color-type}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.colorType -->
<!-- @see icu.windea.pls.config.CwtDataTypes.ColorField -->
<!-- @see icu.windea.pls.ep.codeInsight.hints.ParadoxColorProvider -->

In config files, color types for properties and values are specified via the option `## color_type`, which accepts `hex`, `rgb`, `hsv`, or `hsv360`. This applies to strings and numeric arrays.

For script color fields (e.g. `rgb { 255 255 255 }`), the color type is inferred from the `{type}` in the matching data expression `color[{type}]`; see [ColorField](#data-type-color-field).

By specifying a color type, color gutter icons can be provided for various targets in script files, allowing convenient viewing and editing of colors.

Example (config fragments)：

```cwt
# specify the color type as hexadecimal

## color_type = hex
color = scalar

# specify the color type as rgb

## color_type = rgb
color_rgb = {
    ## cardinality = 3..4
    int[0..255]
}
## color_type = hsv
color_hsv = {
    ## cardinality = 3..4
    float
}

# inferred from data expression

color_field_rgb = color[rgb]
color_field_hsv = color[hsv]
```

Example (matched script fragments)：

```paradox_script
color = 0x2288E1

color_rgb = { 34 136 225 }
color_hsv = { 208 0.849 0.882 }

color_field_rgb = rgb { 34 136 225 }
color_field_hsv = hsv { 208 0.849 0.882 }
```

#### How to Specify File Extensions for Path References in Config Files {#faq-file-extensions}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.fileExtensions -->

In config files, the allowed file extensions for path references are specified via the option `## file_extensions`.

By specifying a color type, it can constrain which file extensions path references can match, providing code inspection and filtered code completion.

Note that path references in some formats do not carry extension information.

By specifying allowed extensions, the plugin can limit the file extensions that path references can match, providing code inspection and filtered code completion.

Note that path references of some data types (such as [Icon](#data-type-icon)) and formats (such as extension information has been specified) will not carry extension information, so this option should not be used.

Example:

```cwt
## file_extensions = { png dds tga }
icon = filepath

## file_extensions = { png dds tga }
texture = filename[gfx/models]

## file_extensions = { ogg }
file = filepath[./]
```

#### How to Perform Config Injection in Config Files {#faq-config-injection}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.inject -->
<!-- @see icu.windea.pls.ep.config.config.CwtInjectConfigPostProcessor -->

Starting from plugin version 2.1.0, config injection can be performed during the config parsing phase by using the option `## inject`.

If there is an existing config fragment

```cwt
# some/file.cwt
some = {
    property = v1
    property = v2
}
```

then the config fragment

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

after processing is equivalent to

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
- The part before `@` is the config file path relative to the config group directory (e.g. the `config/stellaris` directory inside the plugin's JAR), and must match exactly (no wildcards, case-sensitive).
- The part after `@` is the config path; the sub-path `-` matches all standalone values, while other cases serve as wildcards (case-insensitive, using `any` or `*` to match any character, `?` to match a single character) matching all properties with the corresponding key.
- Only applicable to configs with clause values (i.e. `k = {...}` or `{...}`); matched configs are injected at the end of the clause as sub-configs of the target config.
- Config injection is processed only once during the config file parsing phase, so injection can be performed at any location in any config file.
- If the injection fails (the matching config does not exist, there is recursion, etc.), it will be ignored directly and a warning log will be printed.