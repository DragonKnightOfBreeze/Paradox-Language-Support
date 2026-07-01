# 附录：规则格式参考

<!--
@doc-meta
本文档是 CWT 规则格式的参考手册，描述插件所支持的各种规则的用途、格式、字段与注意事项。
文档内容基于 Paradox Language Support 插件的实现编写，与 CWTools 的规则格式在多数情况下兼容，但在细节和扩展点上存在差异。

@see docs/zh/config.md
@see icu.windea.pls.config.config.*
@see icu.windea.pls.config.configExpression.*
-->

## 总览 {#overview}

<!-- @see icu.windea.pls.config.config.CwtConfig -->
<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->
<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->
<!-- @see icu.windea.pls.config.CwtDataType -->
<!-- @see icu.windea.pls.model.expressions.ParadoxExpression -->
<!-- @see icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression -->

本文档是 CWT 规则格式的参考手册，面向希望理解、编写或扩展规则文件的所有读者——包括模组作者、规则文件的协助维护者、插件维护者以及 AI 编程助手。

本文档旨在：

- **统一术语与边界**：对齐插件与 CWTools 的语义，明确插件的扩展点与差异。
- **建立从文档到实现的映射**：在必要时标注对应的接口与解析器，便于回溯源码与验证行为。
- **指导实践**：概述各类规则的用途、格式与注意事项，为正确编写和维护规则文件打好基础。

插件会在打开项目或应用时解析规则文件，读取和计算规则分组数据，以构建规则分组。
规则分组数据中存储了结构化的规则对象，以及其他各类数据。
它们驱动了核心的语义匹配和语义解析逻辑，在代码高亮、代码补全、代码检查等语言功能中被广泛使用。

其中：
- **规则（config）**：是一类统一的领域模型，在语法层面对应规则文件或者其中的属性、值等节点。
- **规则表达式（config expression）**：是在规则字段中使用的结构化语法，在语法层面对应规则文件中拥有特殊语义的字符串字面量。
- **数据表达式（data expression）**：是最常见的一种规则表达式，决定了表达式的匹配逻辑与解析逻辑。
- **数据类型（data type）**：是数据表达式的类型，不同的数据类型往往意味着不同语义的表达式。
- **表达式（expression）**：区别于规则表达式/数据表达式，在语法层面对应脚本文件、本地化文件或 CSV 文件中的各种表达式节点。例如，脚本文件中的属性和值、本地化文件中的命令文本、CSV 文件中的列。
- **复杂表达式（complex expression）**：区别于简单的表达式，作为一类轻量的语法树，其中的各个节点都有各自的语义。例如，作用域字段表达式 `root.owner`、本地化命令表达式 `Root.GetName`。

关于规则系统的整体介绍（如规则分组、规则覆盖、自定义规则等），请参阅[规则系统](config.md)文档。

## 规则 {#configs}

<!-- @see icu.windea.pls.config.config.CwtConfig -->

> 本章节介绍各种规则的用途、格式要点与注意事项，帮助读者正确理解与编写这些规则。

### 概述 {#configs-overview}

规则（config）是在插件自身的规则系统中使用的统一的、核心的领域模型。

#### 规则字段的表示约定 {#configs-fields}

每条规则由若干**字段**（field）组成。字段在规则文件中有多种来源，本文档采用以下格式统一描述：

- **属性字段**：以 `key = value` 形式出现在规则体中的普通属性。文档中直接使用字段名，如 `path`、`name_field`。
- **选项字段**：以选项注释 `## key = value` 形式出现的字段。文档中以 `## ` 为前缀，如 `## cardinality`、`## push_scope`。
- **布尔选项**：以选项注释 `## key` 形式出现的无值标记。文档中同样以 `## ` 为前缀，如 `## primary`、`## inherit`。注意这与 `## key = yes` 不同——布尔选项仅需标记名即可生效。
- **文档注释**：以 `### text` 形式出现的文档注释，通常用于提供快速文档文本。
- **值字段**：直接以值形式出现在规则体中（而非作为属性的值侧），如枚举的值列表。

字段名在规则文件中使用 `snake_case` 形式。

#### 处理流程 {#configs-processing-flow}

规则的整体处理流程可以简化为三个阶段：

1. 读取规则分组中的各个规则文件，构建其语法树（PSI）。
2. 按规则类别，使用对应的解析器（Resolver）将语法树节点转化为结构化的规则对象。
3. 在各语言功能中，根据当前上下文（作用域、类型名、声明上下文等）查询并应用这些规则对象。

规则的来源与覆盖机制详见[规则系统](config.md)文档中"规则分组"与"覆盖方式"相关章节。

#### 分类 {#config-category}

规则按层级分为以下几类：

- **基础规则**：作为语法树层面的通用模型，对应规则文件或者其中的属性、值等节点。本文档不逐一介绍基础规则。
- **[标准规则](#configs-standard)**：驱动各种语言功能的核心规则，包括类型、别名、枚举、链接、作用域等。
- **[扩展规则](#configs-extended)**：用于增强插件功能的附加规则，如为特定定义或内联脚本提供额外的上下文与提示。
- **[内部规则](#configs-internal)**：由插件内部使用的规则，目前不支持（或尚不支持）自定义。

### 标准规则 {#configs-standard}

> 这些规则驱动了各种各样的语言功能，包括但不限于代码补全、代码检查、快速文档、内嵌提示等。

#### 优先级规则 {#config-priority}

<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideStrategy -->
<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideService -->
<!-- @see cwt/core/priorities.core.cwt -->

优先级规则用于配置"目标"（文件、全局封装变量、定义、本地化等）的覆盖方式。它影响目标的生效顺序与查询结果排序（流式查询除外）。未命中任何目录映射时，默认使用 `LIOS`（后读覆盖）。

覆盖方式：

- **`FIOS`**（First In, Only Served）：先加载者生效，后加载者被忽略。
- **`LIOS`**（Last In, Only Served）：后加载者覆盖先加载者。
- **`DUPL`**（Duplicates）：整文件覆盖，必须用同路径文件进行整体替换。
- **`ORDERED`**（Ordered）：顺序读取，后加载者按序新增或合并，不覆盖既有条目。

查询（非流式）结果的排序由优先级驱动；同一路径下按加载顺序（游戏 / 依赖链）决定先后。同一文件内，后出现的项覆盖前面出现的项。

格式说明：

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

示例：

```cwt
priorities = {
    "common/event_chains" = fios
    "common/on_actions" = ordered
    "common/scripted_variables" = fios
    "events" = fios
}
```

- 两个 MOD 都在 `events/` 中定义同名事件：由于 `events = fios`，先被读取（加载更早）的 MOD 生效，后者被忽略。
- 两个 MOD 都在 `common/on_actions/` 添加条目：由于 `ordered`，会顺序合并执行，不发生覆盖。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 系统作用域规则 {#config-system-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig -->
<!-- @see cwt/core/system_scopes.core.cwt -->

系统作用域规则为内置的"系统级作用域"（如 This、Root、Prev、From 等）提供元信息，用于快速文档与作用域栈推导。

路径定位：
- `system_scopes/{name}`。其中 `{name}` 匹配系统作用域 ID。

字段说明：

- `id`：系统作用域 ID。
- `base_id`：基底作用域 ID，未指定时默认为 `id`。用于将同族系统作用域（如 `Prev` / `PrevPrev`、`From` / `FromFrom`）归类。
- `: string`（值）：可读名称，未指定时默认为 `id`。

系统作用域规则与[作用域规则与作用域分组规则](#config-scope)一起决定作用域检查与提示。在部分[扩展规则](#configs-extended)中，可使用选项 `## replace_scopes` 指定系统作用域在当前上下文下对应的具体作用域类型（如将 `this` / `root` / `from` 映射为 `country`）。需要注意的是，`## replace_scopes` 不支持替换 `prev` 系列系统作用域。

示例：

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

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 语言环境规则 {#config-locale}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocaleConfig -->

语言环境规则用于提供语言环境（locale）的相关信息（快速文档、ID、语言代码等）。

插件基于这些规则，识别和推断可用的语言环境、偏好的语言环境以及上下文（如本地化文件）中的语言环境，从而改进 UI 展示、提示信息以及本地化校验逻辑。
通用的规则分组中应声明所有全局的语言环境，其中部分可能不受当前游戏类型支持。

路径定位：
- `locales/{id}`。其中 `{id}` 匹配语言环境 ID。

字段说明：

- `id`：语言环境 ID（如 `l_english`）。
- `codes: string[]`：此语言环境包含的语言代码列表（如 `en`、`zh-CN`）。默认为空。
- `supports: boolean`：此语言环境是否受当前游戏类型支持。默认为 `yes`。

示例：

```cwt
locales = {
    l_english = { codes = { "en" } }
    l_simp_chinese = { codes = { "zh-CN" } }
}
```

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 类型规则与子类型规则 {#config-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSubtypeConfig -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesType -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesTypeFast -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesSubtype -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesSubtypeFast -->

类型规则按"文件路径 / 键名"等条件定位并命名"定义（definition）"，并可声明子类型、展示信息与图片。

路径定位：
- 类型：`types/type[{type}]`。其中 `{type}` 匹配类型名（即规则名称）。
- 子类型：`types/type[{type}]/subtype[{subtype}]`。其中 `{type}` 匹配类型名，`{subtype}` 匹配子类型名（即规则名称）。

类型规则的字段说明：

- `path`：参与扫描的文件目录路径（解析时会自动移除 `game/` 前缀）。可声明多个。
- `path_file`：限定文件名（不含扩展名）。若指定，则 `path_extension` 不再单独生效。
- `path_extension`：限定文件扩展名（解析时会自动规范化，如补齐 `.`）。仅在未指定 `path_file` 时单独生效。
- `path_pattern`：使用 ANT 路径模式匹配文件路径。可声明多个，与 `path` 独立——任一 `path_pattern` 匹配即可通过路径检查。
- `path_strict`：设为 `yes` 时强制精确匹配目录，不匹配子目录。
- `type_per_file`：设为 `yes` 时表示"一文件一类型实例"（此时定义对应整个脚本文件而非其中的某个属性）。
- `name_field`：从定义体中指定的属性键读取展示名称。存在时，类型键仅接受 `## type_key_filter` 中显式列出的值或无限制。
- `name_from_file`：设为 `yes` 时从文件名推导定义名。
- `unique`：设为 `yes` 时启用重名冲突检查。
- `severity`：重名冲突的报告级别（如 `warning`、`error`）。
- `skip_root_key`：允许跳过若干顶级键后继续匹配类型键。值为花括号集合，支持多组（忽略大小写，支持通配符 `any`/`*`/`?`）。若 `skip_root_key` 非空但文件中无根键则不匹配；若为空但文件中有根键同样不匹配。
- `type_key_prefix`：类型键的必需前缀（忽略大小写）。
- `## type_key_filter`：类型键的过滤条件（选项注释，忽略大小写）。支持包含集合 `{ a b }` 和排除集合 `<> { x y }`。
- `## type_key_regex`：类型键的正则过滤（选项注释，忽略大小写）。
- `## starts_with`：类型键的前缀过滤（选项注释，忽略大小写）。
- `## graph_related_types`：声明图相关类型（选项注释），用于定义间依赖关系图。
- `localisation`：本地化展示小节，详见[类型展示规则](#config-type-presentation)。
- `images`：图片展示小节，详见[类型展示规则](#config-type-presentation)。
- `modifiers`：修正小节，派生出与类型绑定的[修正规则](#config-modifier)。

类型规则的匹配流程：

对于一个脚本文件中的属性（或整个文件），类型匹配按以下步骤依次进行：

1. **元素类型检查**：`type_per_file` 为 `yes` 时，定义必须对应整个脚本文件；否则必须对应一个属性。
2. **路径匹配**：先检查 `path_pattern`（ANT 模式），若有任一匹配即通过；否则检查 `path_file` 或 `path_extension`，再检查 `path`（含 `path_strict`）。`path` 和 `path_extension`/`path_file` 均不为空时必须同时满足。
3. **类型键检查**（顺序进行）：`## starts_with` → `## type_key_regex` → `## type_key_filter` → `name_field` 约束（若 `name_field` 存在，则类型键仅可为 `## type_key_filter` 显式列出的值之一，或无限制）。
4. **根键检查**：根据 `skip_root_key` 判断是否需要跳过根键。
5. **类型键前缀检查**：根据 `type_key_prefix` 判断是否匹配（忽略大小写）。
6. **声明结构检查**：检查定义的属性值是否与[声明规则](#config-declaration)的预期结构一致（如声明规则期望块则属性值必须为块）。

子类型规则的字段说明：

子类型通过内容匹配确定。子类型按声明顺序逐个检查，通常与[声明规则](#config-declaration)中的 `subtype[...] = {...}` 一起使用，以细化结构与校验。

- `## type_key_filter`：按类型键过滤（选项注释，忽略大小写）。
- `## type_key_regex`：按类型键正则过滤（选项注释，忽略大小写）。
- `## starts_with`：按类型键前缀过滤（选项注释，不忽略大小写）。
- `## push_scope`：匹配时推入的作用域类型（选项注释）。
- `## display_name`：子类型的展示名称（选项注释）。
- `only_if_not`：与指定子类型互斥——仅在指定的子类型均未匹配时才继续检查。
- `## group`：子类型分组名（选项注释）。同一分组内的子类型互斥（最多匹配一个）。

子类型规则的匹配流程：

1. **互斥检查**：若 `only_if_not` 中指定的任一子类型已匹配，则跳过。
2. **类型键检查**：依次检查 `## starts_with`（不忽略大小写）→ `## type_key_regex` → `## type_key_filter`（忽略大小写）。
3. **内容匹配**：若子类型声明体（`subtype[...] = { ... }`）中包含属性或值规则，则递归检查定义体中是否存在匹配的属性和值。匹配方式包括布尔值精确匹配、字符串/数据表达式匹配、以及嵌套块的递归匹配。若声明体为空（`{}`），则仅需类型键检查通过即可匹配。

类型规则与[声明规则](#config-declaration)协作，为具体定义的声明提供上下文与结构约束。

示例：

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

注意事项：

- `path` 为必需字段；缺失将导致类型被跳过。
- `skip_root_key` 为多组设置：若存在任意一组与文件顶级键序列匹配，则允许跳过后继续匹配类型键。
- 子类型匹配"顺序敏感"，请将更具体的规则放在更前面。
- 同一 `## group` 内的子类型互斥（如 `event_type` 分组中的 `country`、`planet`、`ship` 等）。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### 类型展示规则 {#config-type-presentation}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypePresentationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeImagesConfig -->

类型展示规则为定义类型配置"名称 / 描述 / 必需本地化键"和"主要图片 / 切分规则"等展示信息，以便在 UI、导航与提示中展示。

两者结构一致，包含多个位置规则。可以通过 `subtype[{expression}] = {...}` 为一组位置规则指定需要匹配的子类型，其中 `{expression}` 为子类型表达式（示例：`type`、`!type`、`type1&!type2`）。支持嵌套使用。

位置规则的常用选项包括 `required`（是否必需项）和 `primary`（是否主要项，用于主展示图标 / 主名称）。位置表达式的详细语法参见[位置表达式](#config-expression-location)。

路径定位：
- 本地化：`types/type[{type}]/localisation`。其中 `{type}` 匹配定义类型。
- 图片：`types/type[{type}]/images`。其中 `{type}` 匹配定义类型。

示例：

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

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### 位置规则 {#config-location}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocationConfig -->

位置规则声明图片 / 本地化等资源的定位键与位置表达式，用于类型展示规则的 `localisation` 和 `images` 小节中。

路径定位：
- 本地化资源：`types/type[{type}]/localisation/{key}`。其中 `{type}` 匹配定义类型，`{key}` 匹配键名。
- 图片资源：`types/type[{type}]/images/{key}`。其中 `{type}` 匹配定义类型，`{key}` 匹配键名。

> CWTools 兼容性：兼容。

#### 声明规则 {#config-declaration}

<!-- @see icu.windea.pls.config.config.delegated.CwtDeclarationConfig -->
<!-- @see icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider -->

声明规则描述了"定义条目"的结构，是补全、检查与快速文档等功能的基础。

声明规则的处理流程大致如下：首先，只有键为合法标识符的顶级属性才会被视为声明规则。如果声明的根级值为 `single_alias_right[...]`，会先进行内联展开。随后，插件会按子类型裁剪和扁平化规则树——匹配当前上下文子类型的 `subtype[...]` 块会被展开为平级子规则，不匹配的则跳过。最终生成的规则树用于驱动补全、检查等功能。

声明规则可以与其他规则协作：在声明内可引用[别名与单别名](#config-alias)（`alias_name[...]` / `alias_match_left[...]`、`single_alias_right[...]`）。切换类型（swapped type）的声明可直接嵌套在对应基础类型的声明中。游戏规则（game rule）和动作触发（on action）还可以通过[扩展规则](#configs-extended)改写声明上下文。

路径定位：
- `{name}`。其中 `{name}` 匹配规则名称。
- 对于规则文件中的顶级属性，如果未在解析其他规则的过程中被匹配到，且键是一个合法的标识符，最终都会在回退时尝试解析为声明规则。

示例：

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

注意事项：

- `subtype[...]` 仅在与上下文子类型匹配时生效；不匹配将被忽略（不会报错）。
- 根级 `single_alias_right[...]` 会先被展开，再参与后续解析与检查。
- 为保证后续功能能够"向上溯源"，生成的规则节点均会保持父链（parent config）引用。

> CWTools 兼容性：兼容。

#### 别名规则与单别名规则 {#config-alias}

<!-- @see icu.windea.pls.config.config.delegated.CwtAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSingleAliasConfig -->
<!-- @see icu.windea.pls.config.manipulation.CwtConfigManipulationService.inlineAlias -->
<!-- @see icu.windea.pls.config.manipulation.CwtConfigManipulationService.inlineSingleAlias -->

别名规则将可复用的规则片段抽象成"具名别名"，在多处引用并展开。单别名用于"值侧"的一对一复用。

路径定位：
- 别名：`alias[{name}:{subName}]`。其中 `{name}` 匹配名称，`{subName}`匹配子名（受限支持的数据表达式）。
- 单别名：`single_alias[{name}]`。其中 `{name}` 匹配规则名称。

声明与引用语法：

- 声明别名：`alias[effect:some_effect] = { ... }`
- 使用别名：`alias_name[effect] = alias_match_left[effect]`
- 声明单别名：`single_alias[trigger_clause] = { alias_name[trigger] = alias_match_left[trigger] }`
- 使用单别名：`potential = single_alias_right[trigger_clause]`

别名支持通过选项指定作用域约束：`## scope` / `## scopes` 声明允许的输入作用域集合，`## push_scope` 声明输出作用域。别名的 `subName` 支持受限的数据表达式，用于匹配与提示。

在使用处，别名体会被复制为普通属性规则（键名 = 子名，值和子规则深拷贝，保留选项）。如果展开结果的值侧仍为 `single_alias_right[...]`，会继续触发级联展开。别名常与[声明规则](#config-declaration)结合使用，在定义声明中复用 trigger / effect 等片段。

示例：

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

注意事项：

- 别名唯一键由 `name:subName` 组成；重复定义将按覆盖方式 / 优先级处理。
- 展开后才会进行基数与选项校验；请在展开位置而非声明处考虑最终语义。
- `subName` 为数据表达式（受限），可使用模板 / 枚举等提高复用度，但请避免过宽导致误匹配。

> CWTools 兼容性：兼容。

#### 行规则 {#config-row}

<!-- @see icu.windea.pls.config.config.delegated.CwtRowConfig -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesRow -->

行规则为 CSV 行声明列名与取值形态，用于补全与检查。

路径定位：
- `rows/row[{name}]`。其中 `{name}` 匹配规则名称。

字段说明：

- `path`：参与扫描的文件目录路径（解析时会自动移除 `game/` 前缀）。可声明多个。
- `path_file`：限定文件名（不含扩展名）。若指定，则 `path_extension` 不再单独生效。
- `path_extension`：限定文件扩展名（解析时会自动规范化，如补齐 `.`）。仅在未指定 `path_file` 时单独生效。
- `path_pattern`：使用 ANT 路径模式匹配文件路径。可声明多个，与 `path` 独立——任一 `path_pattern` 匹配即可通过路径检查。
- `path_strict`：设为 `yes` 时强制精确匹配目录，不匹配子目录。
- `type`：行类型（`key`/`index`，默认为 `key`）。决定如何匹配其中的每一列。按列名匹配（列名不可重复），还是按列在所在行中的索引匹配（列名可重复）。
- `skip_last_row`：解析与匹配时，是否忽略最后一行。默认为 `no`。
- `skip_last_column`：解析与匹配时，如果列索引越界，是否忽略最后一列。默认为 `no`。
- `columns`：列规则的列表（一组属性规则，键为列名，值为需要匹配的数据表达式）。

列规则的字段说明：

- `## declare_complex_enum`：表示这一列声明了一个指定类型的复杂枚举值（而非引用）。

行规则的路径匹配逻辑与[类型规则](#config-type)相同。

示例：

```cwt
rows = {
    row[weapon_template] = {
        path = "game/common/weapon_templates"
        path_extension = .csv
        skip_last_column = yes
        columns = {
            key = <weapon_template>
            damage = float
            ## declare_complex_enum = weapon_tag
            tag = scalar
        }
    }
}
```

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 定值规则 {#config-define}

<!-- @see icu.windea.pls.config.config.delegated.CwtDefineConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtDefineVariableConfig -->

定值规则用于描述脚本文件中的定值命名空间和定值变量，提供快速文档文本和规则上下文。
它们位于 `common/defines` 目录中的扩展名为 `.txt` 的脚本文件中。

路径定位：
- 定值命名空间：`defines/{namespace}`。其中 `{namespace}` 匹配命名空间（即规则名称）。
- 定值变量：`defines/{namespace}/{variable}`。其中 `{namespace}` 匹配命名空间，`variable` 匹配变量名（即规则名称）。

示例：

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

注意事项：

- 插件会强制忽略名为 `define` 或 `defines` 的类型规则和声明规则。
- 目前，基于定值规则，插件会检查定值变量的声明结构的合法性，但不会检查定值命名空间或定值变量的名字的合法性。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 枚举规则 {#config-enum}

<!-- @see icu.windea.pls.config.config.delegated.CwtEnumConfig -->

枚举规则用于描述简单枚举，并提供一组可选项，作为固定的枚举值。
简单枚举的枚举值必须是常量，且会忽略大小写。

路径定位：
- `enums/enum[{name}]`。其中 `{name}` 匹配规则名称。

示例：

```cwt
enums = {
    enum[weight_or_base] = { weight base }
}
```

CWTools 兼容性：部分兼容。拥有不同的解析和处理逻辑。

#### 复杂枚举规则 {#config-complex-enum}

<!-- @see icu.windea.pls.config.config.delegated.CwtComplexEnumConfig -->
<!-- @see icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesComplexEnum -->

复杂枚举规则用于描述复杂枚举，并基于锚点动态定位可选项，作为动态的枚举值。
按照路径模式匹配脚本文件，并在其中进一步匹配锚点。
复杂枚举的枚举值默认不忽略大小写。

路径定位：
- `enums/complex_enum[{name}]`。其中 `{name}` 匹配规则名称。

字段说明：

- `path`：参与扫描的文件目录路径（解析时会自动移除 `game/` 前缀）。可声明多个。
- `path_file`：限定文件名（不含扩展名）。若指定，则 `path_extension` 不再单独生效。
- `path_extension`：限定文件扩展名（解析时会自动规范化，如补齐 `.`）。仅在未指定 `path_file` 时单独生效。
- `path_pattern`：使用 ANT 路径模式匹配文件路径。可声明多个，与 `path` 独立——任一 `path_pattern` 匹配即可通过路径检查。
- `path_strict`：设为 `yes` 时强制精确匹配目录，不匹配子目录。
- `start_from_root`：指定是否从文件顶部（而非顶级属性的下一级）开始查询锚点。
- `name`：描述如何在匹配文件中定位值锚点——实现会收集其中所有名为 `enum_name` 的属性键或属性值或块成员值作为锚点。
- `## case_insensitive`：（扩展）布尔选项，将复杂枚举值标记为忽略大小写。
- `## per_definition`：（扩展）布尔选项，将同名同类型复杂枚举值的等效性限制在定义级别（而非文件级别）。

匹配流程：

复杂枚举规则的路径匹配逻辑与[类型规则](#config-type)相同。

对于匹配文件中的每个字符串表达式，插件会检查它是否可以作为某个复杂枚举值的锚点。
具体步骤为：首先在 `name` 小节中查找包含 `enum_name` 的规则条目；然后根据 `enum_name` 出现的位置（作为属性键、属性值或块成员值），确定当前表达式的角色。
若为属性键侧的 `enum_name`，则当前属性键即为枚举值锚点；若为属性值侧的 `enum_name`，则当前属性的值即为枚举值锚点；若为块成员值的 `enum_name`，则该值本身即为枚举值锚点。
最后，从锚点向上逐层匹配父级结构，直至到达 `name` 小节的根（`start_from_root` 为 `yes` 时必须到达文件根级，否则到达顶级属性的下一级即可）。

示例：

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

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### 动态值类型规则 {#config-dynamic-value-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig -->

动态值类型规则用于为对应的动态值类型提供一组可选项，作为预定义的动态值。
这里预定义的动态值必须是常量，且不会忽略大小写。

动态值是一组不固定的可选项，通常是合法的标识符，使用同名本地化的文本作为 UI 显示。
事件目标（event target）、变量（variable）、标志（flag）等通常都会被视为动态值。

路径定位：
- `values/value[{name}]`。其中 `{name}` 匹配规则名称。

示例：

```cwt
values = {
    value[event_target] = { owner capital }  # case-insensitive
}
```

> CWTools 兼容性：部分兼容。拥有不同的解析和处理逻辑。

#### 链接规则 {#config-link}

<!-- @see icu.windea.pls.config.config.delegated.CwtLinkConfig -->

链接规则为复杂表达式中的"字段 / 函数样"节点提供语义与类型约束（作用域 / 值），支撑链式访问与补全检查。

路径定位：
- 常规链接：`links/{name}`。其中 `{name}` 匹配规则名称。
- 本地化链接：`localisation_links/{name}`。其中 `{name}` 匹配规则名称。
- 如果静态的本地化链接未被声明，静态的常规链接会被全部复制作为本地化链接。

字段说明：

- `type`：链接类型（`scope` / `value` / `both`，默认为 `scope`）。
- `from_data`：是否从文本数据中读取动态数据（格式如 `prefix:data`）。
- `from_argument`：是否从传参中读取动态数据（格式如 `func(arg)`）。
- `argument_separator`：多传参时使用的分隔符（`comma` / `pipe`，默认为 `comma`）。
- `prefix`：动态链接的前缀。
- `data_source`（可多值）：每个数据源是一个数据表达式，用于约束动态数据的合法取值。
- `input_scopes`：输入作用域集合，可写单个或集合，同时支持 `input_scope` 与 `input_scopes` 两种写法。
- `output_scope`：输出作用域；为空时表示透传或基于数据源推导。
- `for_definition_type`：仅在指定定义类型中可用。

未声明 `data_source` 的链接为静态链接，仅代表一个固定的节点名（如 `owner`）。声明了 `data_source` 与 / 或 `prefix` / `from_*` 的链接为动态链接，可携带动态数据（如 `modifier:x`、`relations(x)`、`var:x`）。

示例：

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
        data_source = $script_value_reference
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

注意事项：

- `prefix` 不应带引号或括号；`input_scopes` 使用花括号集合语法（如 `{ country }`）。
- 可混合多个 `data_source`。
- 若动态链接参数为单引号字面量，则按字面量处理，通常不提供补全。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### 本地化命令规则与本地化提升规则 {#config-localisation}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig -->

本地化命令规则声明"本地化命令字段"（如 `GetCountryType`）的可用性与允许作用域。本地化提升规则声明"本地化作用域提升"，使得通过本地化链接切换作用域后仍能使用对应的命令字段。

路径定位：
- 本地化命令：`localisation_commands/{name}`。其中 `{name}` 匹配规则名称。
- 本地化提升：`localisation_promotions/{name}`。其中 `{name}` 匹配规则名称。

二者均包含 `supported_scopes` 字段，声明允许的作用域类型集合。

示例：

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

注意事项：

- 名称大小写不敏感；请保持与实际使用一致的拼写风格以便检索。
- 提升规则的名称应与本地化链接名一致；否则无法正确匹配。
- 静态常规链接会自动复制为本地化链接；如需动态行为，请单独声明本地化链接。

> CWTools 兼容性：兼容。

#### 修正规则与修正分类规则 {#config-modifier}

<!-- @see icu.windea.pls.config.config.delegated.CwtModifierConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig -->

修正规则声明修正（modifier）与其分类，用于图标渲染、补全与作用域校验。

路径定位：
- 修正：
  - `modifiers/{name}`。其中 `{name}` 匹配规则名称。
  - `types/type[{type}]/modifiers/{name}`。其中 `{type}` 匹配定义类型，`{name}` 匹配规则名称（其中的 `$` 会被替换为 `<{type}>`）。
  - `types/type[{type}]/modifiers/subtype[{subtype}]/{name}`。其中 `{subtype}` 匹配定义的子类型。
- 修正分类：
  - `modifier_categories/{name}`。其中 `{name}` 匹配规则名称。

**修正字段**：`name` 为模板化名称（如 `job_<job>_add`），支持匹配动态生成的修正。`categories` 为分类名集合，决定允许的作用域类型。如果已解析出分类映射，则基于类别汇总作用域；否则回退到修正自身的选项 `supported_scopes`。

**修正分类字段**：`name` 为分类名（如 `Pops`），`supported_scopes` 为该分类允许的作用域集合。

修正规则与[类型规则](#config-type)的 `modifiers` 小节联动：在类型规则中声明的修正名称使用 `$` 占位，解析时会被替换为 `<{type}>` 或 `<{type}.{subtype}>`，从而派生出与类型绑定的修正规则。

示例：

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

注意事项：

- 修正条目缺少 `categories` 会被跳过（不生效）。
- 类型规则中的修正名称使用 `$` 占位，请确保与类型 / 子类型表达式对应。
- 类别中的 `supported_scopes` 应使用标准作用域 ID，解析时会自动归一化大小写。

> CWTools 兼容性：兼容。

#### 作用域规则与作用域分组规则 {#config-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig -->

作用域规则定义"作用域类型"及其别名，作用域分组规则对作用域进行分组，二者用于作用域检查、链路约束与提示。

作用域规则与系统作用域共同决定作用域栈与含义；与链接规则共同约束链式访问的输入 / 输出作用域。在扩展规则中可通过 `## replace_scopes` 指定在特定上下文下系统作用域映射到的具体作用域类型。

路径定位：
- 作用域：`scopes/{name}`。其中 `{name}` 匹配规则名称。
- 作用域分组：`scope_groups/{name}`。其中 `{name}` 匹配规则名称。

示例：

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

> CWTools 兼容性：兼容。

#### 数据库对象类型规则 {#config-db-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig -->

数据库对象类型规则用于描述数据库对象表达式的类型与格式。
这种表达式可以在本地化文件中作为概念名称使用（如 `['civic:some_civic', ...]`）。
它们最终会被解析为一个定义或本地化，并渲染到 UI 提示中。

路径定位：
- `database_object_types/{name}`。其中 `{name}` 匹配规则名称。

字段说明：

- `type`：若存在，将 `prefix:object` 的 `object` 作为该类型的定义引用。
- `swap_type`：若存在，将 `prefix:object:swap` 的 `swap` 作为切换类型的定义引用。
- `localisation`：若存在，将 `prefix:object` 的 `object` 作为本地化键解析。

示例：

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

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 并集规则 {#config-union}

<!-- @see icu.windea.pls.config.config.delegated.CwtUnionConfig -->

并集规则用于提供一组数据表达式的候选项，以进行并集匹配，匹配时会递归展开并依次尝试其中的候选项。
不同于枚举规则，这里的可选项可以是各种数据类型的数据表达式。

路径定位：
- `union[{name}]`。其中 `{name}` 匹配规则名称。

示例：

```cwt
union[loc_or_text] = { localisation scalar }
```

> CWTools 兼容性：不兼容，插件作为扩展提供。

#### 宏规则 {#config-macro}

<!-- @see icu.windea.pls.config.config.delegated.CwtMacroConfig -->
<!-- @see cwt/cwtools-stellaris-config/config/common/inline_scripts.cwt -->
<!-- @see cwt/cwtools-vic3-config/config/definition_injections.cwt -->
<!-- @see cwt/cwtools-eu5-config/config/definition_injections.cwt -->

宏规则用于描述脚本文件中区别于一般抽象的特殊的语言构造（表达式、语句等），并提供额外的用于提示和验证的元数据。
这些语言构造会改变游戏运行时的脚本解析器的行为，从而改变、扩展或复用已有的脚本片段。
不同的宏可以拥有不同的规则结构。

目前涉及的宏包括：
- **内联脚本（inline_script）**：（Stellaris）在解析阶段被替换为目标文件的内容，且可以指定参数。
- **定义注入（definition_injection）**：（VIC3 / EU5）在解析阶段对目标定义的声明进行注入或替换，且可以指定模式以决定具体行为。

路径定位：
- `macro[{name}]`。其中 `{name}` 匹配规则名称。

示例：

```cwt
macro[inline_script] = filepath[common/inline_scripts/,.txt]

macro[definition_injection] = {
    modes = { INJECT REPLACE TRY_INJECT TRY_REPLACE INJECT_OR_CREATE REPLACE_OR_CREATE }
    lenient_modes = { TRY_INJECT TRY_REPLACE INJECT_OR_CREATE REPLACE_OR_CREATE }
    replace_modes = { REPLACE TRY_REPLACE REPLACE_OR_CREATE }
    create_modes = { REPLACE_OR_CREATE }
}
```

> CWTools 兼容性：不兼容。插件作为扩展提供。

### 扩展规则 {#configs-extended}

> 这些规则用于增强插件的功能，例如指定规则上下文、提供额外的快速文档与内嵌提示文本等。
>
> 扩展规则中有一些常见的共通特征：
> - 大部分扩展规则支持多种**名称匹配**方式：常量、[模板表达式](#config-expression-template)、[ANT 路径模式](#faq-ant)和[正则表达式](#faq-regex)。
> - 大部分扩展规则支持通过文档注释提供快速文档文本。
> - 大部分扩展规则支持通过选项注释提供内嵌提示文本（`## hint`）。
> - 部分扩展规则支持通过选项注释指定**作用域上下文**（`## replace_scopes` / `## push_scope`）。

#### 封装变量的扩展规则 {#config-extended-scripted-variable}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig -->

为脚本中的封装变量（scripted variable）提供额外提示（快速文档、内嵌提示等）。

路径定位：
- `scripted_variables/{name}`。其中 `{name}` 匹配规则名称。

格式说明：

```cwt
scripted_variables = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## hint = §RSome inlay hint text§!
    x
}
```

注意事项：

- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。
- 本条目仅提供"提示增强"，不负责声明或校验封装变量的取值与类型。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 定义的扩展规则 {#config-extended-definition}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig -->

为具体"定义（definition）"提供额外上下文与提示信息，包括文档 / 提示（`## hint`）、绑定定义类型（`## type`，必填）、以及按需指定的作用域上下文（`## replace_scopes` / `## push_scope`）。

路径定位：
- `definitions/{name}`。其中 `{name}` 匹配规则名称。

格式说明：

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

注意事项：

- `type` 为必填；缺失将导致该条目被跳过。
- 此扩展用于"提示与上下文增强"，并不直接改变[声明规则](#config-declaration)的结构。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 游戏规则的扩展规则 {#config-extended-game-rule}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig -->

为游戏规则（即类型为 `game_rule` 的定义）提供文档 / 提示增强，并支持"重载[声明规则](#config-declaration)"。

规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见[模式感知的数据类型](#data-types-pattern-aware)）。

路径定位：
- `game_rules/{name}`。其中 `{name}` 匹配规则名称。

当条目为属性节点时（如 `x = { ... }` 或 `x = single_alias_right[...]`），其值或子块会作为"声明规则重载"在使用处生效。仅当为属性节点时才会产生重载效果；纯值节点仅提供提示。

格式说明：

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

示例：

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

注意事项：

- 若值为 `single_alias_right[...]`，会先被内联展开，再作为重载规则生效。
- 该扩展仅影响"[声明规则](#config-declaration)的来源 / 结构"与"提示信息"，不改变整体优先级与覆盖方式。

> CWTools 兼容性：不兼容。拥有不同的格式和行为。

#### 动作触发的扩展规则 {#config-extended-on-action}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig -->

为动作触发（即类型为 `on_action` 的定义）提供文档 / 提示增强，并指定"事件类型"以影响声明上下文中与事件有关的引用。

规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见[模式感知的数据类型](#data-types-pattern-aware)）。

路径定位：
- `on_actions/{name}`。其中 `{name}` 匹配规则名称。

字段说明：

- `## event_type`（必填）：声明事件类型，用于在声明上下文中将与事件相关的数据表达式替换为该事件类型对应的表达式。

格式说明：

```cwt
on_actions = {
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    ## event_type = country
    x
}
```

示例：

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

注意事项：

- `## event_type` 为必填；缺失将导致该条目被跳过。
- 如需作用域替换，可结合 `## replace_scopes` 使用。

> CWTools 兼容性：不兼容。拥有不同的格式和行为。

#### 参数的扩展规则 {#config-extended-parameter}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedParameterConfig -->

为触发器 / 效果 / 内联脚本中的参数（`$PARAM$` 或 `$PARAM|DEFAULT$`）提供文档与上下文增强：绑定上下文键、声明上下文规则与作用域上下文，以及支持从使用处继承上下文。

规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见[模式感知的数据类型](#data-types-pattern-aware)）。

路径定位：
- `parameters/{name}`。其中 `{name}` 匹配规则名称。

字段说明：

- `## context_key`（必填）：上下文键（如 `scripted_trigger@some_trigger`），`@` 之前为包含的定义类型（或 `inline_script`），`@` 之后为定义名或内联脚本路径。上下文键自身也支持模式匹配。
- `## context_configs_type`：`single`（默认）或 `multiple`，含义同内联脚本扩展规则。
- `## inherit`：布尔选项，标记后从参数的"使用处"继承上下文（规则与作用域），而非使用静态声明。

格式说明：

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

注意事项：

- `## context_key` 为必填；缺失将导致该条目被跳过。
- 标记 `## inherit` 时，上下文取自"使用处"，可能为空或因位置不同而变化。
- 根级 `single_alias_right[...]` 会被内联展开后再作为上下文规则使用。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 复杂枚举值的扩展规则 {#config-extended-complex-enum-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig -->

为复杂枚举的具体条目提供文档 / 提示增强（快速文档、内嵌提示等）。

规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见[模式感知的数据类型](#data-types-pattern-aware)）。

路径定位：
- `complex_enum_values/{type}/{name}`。其中 `{type}` 匹配枚举名，`{name}` 匹配规则名称。

格式说明：

```cwt
complex_enum_values = {
    component_tag = {
        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x
    }
}
```

注意事项：

- 本扩展不改变复杂枚举"值来源"的收集逻辑，仅提供提示信息。
- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 动态值的扩展规则 {#config-extended-dynamic-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig -->

为某种动态值类型下的具体动态值条目提供文档 / 提示增强。

规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见[模式感知的数据类型](#data-types-pattern-aware)）。

路径定位：
- `dynamic_values/{type}/{name}`。其中 `{type}` 匹配动态值类型，`{name}` 匹配规则名称。

格式说明：

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

注意事项：

- 本扩展不改变动态值类型与基础"值集合"的定义，仅提供提示信息。
- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### 内联脚本的扩展规则 {#config-extended-inline-script}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig -->

为具体的内联脚本（inline script）声明"上下文规则"和"作用域上下文"，用于在被调用处提供正确的补全与检查。

规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见[模式感知的数据类型](#data-types-pattern-aware)）。

路径定位：
- `inline_scripts/{name}`。其中 `{name}` 匹配规则名称。

字段说明：

- `## context_configs_type`：控制上下文规则的聚合形态：`single`（默认）仅取值侧作为上下文规则；`multiple` 取子规则列表作为上下文规则。

格式说明：

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

注意事项：

- 若仅需单条上下文规则，保持默认 `single` 即可；需要声明多条时使用 `multiple`。
- 根级 `single_alias_right[...]` 会被内联展开后再作为上下文规则使用。

> CWTools 兼容性：不兼容。插件作为扩展提供。

### 内部规则 {#configs-internal}

> 这些规则由插件内部使用，不支持自定义（或是目前尚不支持）。

#### 模式规则 {#config-internal-schema}

<!-- @see icu.windea.pls.config.config.internal.CwtSchemaConfig -->
<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->
<!-- @see icu.windea.pls.config.util.CwtConfigSchemaManager -->
<!-- @see cwt/core/internal/schema.cwt -->

模式规则为"规则文件本身"的右侧取值形态提供声明，用于基础级别的补全与（有限的）结构校验。目前以"初步补全"为主，暂不提供严格的 Schema 校验。

模式规则仅来源于内置文件 `internal/schema.cwt`，不可被外部文件覆盖。其结构包含三类条目：

- **普通属性**（`properties`）：键解析为常量、类型或模板形态。
- **枚举**（`enums`）：键解析为枚举表达式（如 `$enum:NAME$`）。
- **约束**（`constraints`）：键解析为约束表达式（如 `$$NAME`）。

#### 折叠设置规则 {#config-internal-folding}

<!-- @see icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig -->
<!-- @see icu.windea.pls.lang.folding.ParadoxExpressionFoldingBuilder -->
<!-- @see cwt/core/internal/folding_settings.cwt -->

折叠设置规则为编辑器提供额外的代码折叠规则（内部使用，目前尚不支持自定义）。

仅来源于内置文件 `internal/folding_settings.cwt`。以组为单位读取每个条目，每个条目包含：

- `id`：折叠项 ID（对应规则文件路径或表达式）。
- `key`：折叠项键。
- `keys`：备选键集合。
- `folding_key`：用于折叠的显示键。
- `placeholder`（选项注释 `## placeholder`）：折叠后的占位文本。

#### 后缀模板设置 {#config-internal-postfix-template}

<!-- @see icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig -->
<!-- @see icu.windea.pls.lang.codeInsight.postfix.ParadoxPostfixTemplateProvider -->
<!-- @see cwt/core/internal/postfix_template_settings.cwt -->

后缀模板设置规则为编辑器提供后缀补全模板（内部使用，目前尚不支持自定义）。

仅来源于内置文件 `internal/postfix_template_settings.cwt`。每个条目包含：

- `id`：模板 ID。
- `key`：触发后缀的关键字。
- `example`：展示用示例文本。
- `expression`：模板表达式（使用 `$EXPR$` 作为占位符）。
- `context_expression`（选项注释 `## context_expression`）：约束模板可用上下文的表达式。

## 规则表达式 {#config-expressions}

<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->

> 本章节介绍各种规则表达式的用途、格式与默认 / 边界行为，帮助读者正确理解与编写这类特殊的表达式。

### 概述 {#config-expressions-overview}

规则表达式（config expression）是在规则字段中使用的结构化语法。

#### 分类 {#config-expressions-category}

本章节涵盖以下几种规则表达式：

- **[数据表达式](#config-expression-data)**（Data Expression）：描述表达式的取值形态和匹配模式，解析后得到具体的数据类型。
- **[模板表达式](#config-expression-template)**（Template Expression）：由常量与动态片段拼接的模式，用于更灵活的匹配。
- **[基数表达式](#config-expression-cardinality)**（Cardinality Expression）：约束定义成员的出现次数。
- **[位置表达式](#config-expression-location)**（Location Expression）：定位图片、本地化等资源的来源。
- **[模式表达式](#config-expression-schema)**（Schema Expression）：为规则文件本身的取值形态提供声明。

### 数据表达式 {#config-expression-data}

<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->

数据表达式用于描述各种表达式的取值形态和匹配模式，以决定它们的匹配逻辑与解析逻辑。

数据表达式在解析后会得到具体的[数据类型](#data-types)，并且可以附带元数据。数据类型以及这些元数据决定了数据表达式能够匹配脚本文件、本地化文件和 CSV 文件中的哪些表达式。

默认与边界行为：

- 块（`{ ... }`）对应的数据类型是 `Block`。
- 空字符串（`""`）对应的数据类型是 `Constant`，并把自身作为常量值。
- 无法匹配任何已知数据类型时，回退为 `Constant`，并把原始字符串作为常量值。
- 定义引用应使用尖括号形式（如 `<event>`），而非带前缀的方括号形式（如 `definiton[event]`，这是错误的写法）。

示例：

```cwt
int                         # 整数
float[0.0..1.0]             # 带范围约束的浮点数
enum[shipsize_class]        # 枚举引用
scope[country]              # 作用域引用
<ship_size>                 # 定义引用
value[event_target]         # 动态值引用
pre_<opinion_modifier>_suf  # 模板表达式（含定义引用片段）
```

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

### 模板表达式 {#config-expression-template}

<!-- @see icu.windea.pls.config.configExpression.CwtTemplateExpression -->

模板表达式由多个片段拼接而成——常量片段与动态片段交替组合——用于描述比单一数据表达式更复杂的取值形态。每个动态片段本身是一个受限的数据表达式（如定义引用、枚举引用、动态值引用等）。

解析约束：

- 包含空白字符的文本视为无效模板。
- 仅存在一个片段（纯常量或纯一个动态）时不视为模板，而是作为普通的数据表达式处理。
- 多个片段之间采用"最左最早匹配"的拆分策略。
- 每个片段最终委托数据表达式解析；未匹配到已知类型时降级为常量片段。

示例：

以下示例展示了模板表达式的典型用法，`#` 后的注释标注了各片段的拆分方式：

```cwt
job_<job>_add             # "job_" + <job> + "_add"
xxx_value[anything]_xxx   # "xxx_" + value[anything] + "_xxx"
a_enum[weight_or_base]_b  # "a_" + enum[weight_or_base] + "_b"
```

例如，`job_<job>_add` 能匹配 `job_researcher_add`、`job_farmer_add` 等——其中 `<job>` 部分匹配类型为 `job` 的任意定义名。

注意事项：

- 常量片段与动态规则名紧邻时，解析器会优先保证动态规则的正确识别。
- 模板表达式不支持空白字符；如需要空白匹配，请改用 [ANT 路径模式](#faq-ant)或[正则表达式](#faq-regex)。

> CWTools 兼容性：部分兼容。拥有不同的解析和处理逻辑。

### 基数表达式 {#config-expression-cardinality}

<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

基数表达式用于约束定义成员的出现次数，影响代码检查与代码补全等功能。通过选项注释 `## cardinality` 声明。

格式为 `{min}..{max}`，其中 `min` 和 `max` 为非负整数或 `inf`（不区分大小写，表示无限）。
在整数前添加 `~` 前缀表示宽松校验（未满足时，使用更弱的严重度级别，例如从警告改为弱警告）。

默认与边界行为：

- 最小值为负数时会被限制为 0。
- 缺少 `..` 分隔符时视为无效，不产生约束。
- `min > max` 时视为无效，不产生约束。

示例：

```cwt
## cardinality = 0..1     # 可选，最多出现 1 次
## cardinality = 0..inf   # 可选，出现次数不限
## cardinality = 1..5     # 必须出现 1 到 5 次
## cardinality = ~1..10   # 宽松校验：期望出现 1 到 10 次，但未出现时默认仅产生弱警告
```

提示：

- 可使用 `## cardinality_min_define` 从对应表达式的定值变量动态获取最小基数（如 `## cardinality_min_define = NGameplay.ETHOS_MIN_POINTS`）。
- 可使用 `## cardinality_max_define` 从对应表达式的定值变量动态获取最大基数（如 `## cardinality_max_define = NGameplay.ETHOS_MAX_POINTS`）。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

### 位置表达式 {#config-expression-location}

<!-- @see icu.windea.pls.config.configExpression.CwtLocationExpression -->

位置表达式用于定位目标资源（图片、本地化等）的来源。表达式中的 `$` 为占位符，运行时会被"定义名"或"属性值"等动态内容替换。

位置表达式使用 `|` 分隔参数，格式为 `<location>|<args...>`。不同类型的位置表达式对参数的解读方式有所不同，详见下文。

#### 本地化位置表达式 {#config-expression-location-localisation}

<!-- @see icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression -->

本地化位置表达式用于定位定义的相关本地化。位置部分可以是包含 `$` 占位符的本地化键模式（如 `$_desc`），也可以是属性键名（如 `title`）。

参数约定：

- 以 `$` 开头的参数表示"名称文本来源路径"（支持逗号分隔多路径），用于替换位置中的 `$` 占位符。
- 参数 `u` 表示将最终名称强制转为大写（仅限使用占位符时有效）。
- `$` 参数重复出现时，以后者为准。

示例：

```cwt
$_desc
$_desc|$name
$_desc|$name|u
$_desc|$name,$alt_name
title
```

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### 图片位置表达式 {#config-expression-location-image}

<!-- @see icu.windea.pls.config.configExpression.CwtImageLocationExpression -->

图片位置表达式用于定位定义的相关图片。位置部分可以是文件路径（如 `gfx/.../mod_$.dds`）、sprite 名（如 `GFX_$`）或属性键名（如 `icon`）。若为属性键名，则会继续解析该属性值所指向的图片。

参数约定：

- 以 `$` 开头的参数表示"名称文本来源路径"（支持逗号分隔多路径），用于替换位置中的 `$` 占位符。
- 其他参数表示"帧数来源路径"（支持逗号分隔多路径），用于图片切分。
- 同类参数重复出现时，以后者为准。

示例：

```cwt
gfx/interface/icons/modifiers/mod_$.dds
gfx/interface/icons/modifiers/mod_$.dds|$name
GFX_$
icon
icon|p1,p2
```

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

### 模式表达式 {#config-expression-schema}

<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->

模式表达式用于描述规则文件中键与值的取值形态，从而为规则文件本身提供代码补全等功能。
目前仅用于提供基础的代码补全，且仅在内置规则文件 `cwt/core/internal/schema.cwt` 中使用。
与[内部规则 → 模式规则](#config-internal-schema)协同工作。

模式表达式支持以下四种形态：

- **常量（Constant）**：不包含 `$` 的原样字符串，如 `types`、`enums`。
- **模板（Template）**：包含一个或多个 `$...$` 参数的模式，如 `$type$`、`type[$type$]`。
- **类型（Type）**：以单个 `$` 起始（不闭合），如 `$any`、`$int`。
- **约束（Constraint）**：以 `$$` 起始，如 `$$declaration`。

> CWTools 兼容性：不兼容。插件作为扩展提供。

## 数据类型 {#data-types}

<!-- @see icu.windea.pls.config.CwtDataType -->
<!-- @see icu.windea.pls.config.CwtDataTypes -->
<!-- @see icu.windea.pls.config.CwtDataTypeSets -->
<!-- @see icu.windea.pls.ep.config.configExpression.CwtDataExpressionSupport -->
<!-- @see icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher -->
<!-- @see icu.windea.pls.ep.match.ParadoxCsvExpressionMatcher -->
<!-- @see icu.windea.pls.ep.resolve.expression.ParadoxScriptExpressionSupport -->
<!-- @see icu.windea.pls.ep.resolve.expression.ParadoxLocalisationExpressionSupport -->
<!-- @see icu.windea.pls.ep.resolve.expression.ParadoxCsvExpressionSupport -->

> 本章节介绍数据类型的概念、分类与用途，帮助读者理解规则文件中的数据表达式如何与脚本文件中的实际内容进行匹配。

### 概述 {#data-types-overview}

数据类型（data type）描述数据表达式（作为最常见的一种规则表达式）的类型。

每种数据类型表示一种语义范畴，参与决定表达式与规则表达式之间的匹配逻辑。

示例：
- 数据表达式 `<event.country>` 的数据类型为 `Definition`，附带元数据 `event.country`，表示匹配类型为 `event`、子类型包含 `country` 的定义引用。
- 数据表达式 `enum[weight_or_base]` 的数据类型为 `EnumValue`，附带元数据 `weight_or_base`，表示匹配该枚举中声明的所有可选值。

相关的扩展点：
- 数据类型的解析逻辑由扩展点 `icu.windea.pls.dataExpressionSupport` 驱动。
- 脚本文件中的表达式与规则表达式的匹配逻辑由扩展点 `icu.windea.pls.scriptExpressionMatcher` 驱动。
- CSV 文件中的表达式与规则表达式的匹配逻辑由扩展点 `icu.windea.pls.csvExpressionMatcher` 驱动（有限支持）。
- 脚本文件中的表达式的各种语言功能的实现逻辑由扩展点 `icu.windea.pls.scriptExpressionSupport` 驱动。
- 本地化文件中的表达式的各种语言功能的实现逻辑由扩展点 `icu.windea.pls.localisationExpressionSupport` 驱动（特殊支持）。
- CSV 文件中的表达式的各种语言功能的实现逻辑由扩展点 `icu.windea.pls.csvExpressionSupport` 驱动（有限支持）。

### 基本数据类型 {#data-types-basic}

以下数据类型表示基本的取值形态。

#### Any {#data-type-any}

任意类型。

匹配任意脚本表达式，作为最低优先级的后备匹配。

对应的数据表达式的格式：
- `$any`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### Bool {#data-type-bool}

布尔类型。

匹配布尔值（`yes` / `no`）。

对应的数据表达式的格式：
- `bool`

> CWTools 兼容性：兼容。

#### Int {#data-type-int}

整数类型。

匹配整数值。
带范围参数时，还会限制数值范围（仅作检查，仍然视为匹配）。
用引号括起的数字也视为匹配（兼容原版游戏文件）。

范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。

对应的数据表达式的格式：
- `int`
- `int{range}` - 其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### Float {#data-type-float}

浮点数类型。

匹配浮点数值。
带范围参数时，还会限制数值范围（仅作检查，仍然视为匹配）。
用引号括起的数字也视为匹配（兼容原版游戏文件）。

范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。

对应的数据表达式的格式：
- `float`
- `float{range}` - 其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### Scalar {#data-type-scalar}

标量类型。

匹配大多数非子句表达式（字符串、数字、布尔值等），作为低优先级的宽泛匹配。
作为键时总是匹配。`wildcard_scalar` 变体会设置通配符标记。

对应的数据表达式的格式：
- `scalar`
- `wildcard_scalar` - 通配符变体。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### ColorField {#data-type-color-field}

颜色字段类型。

匹配脚本颜色字段（如 `rgb { 255 255 255 }`）。
带参数时，还会验证颜色类型前缀。

对应的数据表达式的格式：
- `colour_field` `color_field`
- `colour[{type}]` `color[{type}]` - 其中 `{type}` 匹配颜色类型（可选值：`rgb` `hsv` `hsv360`）。

> CWTools 兼容性：兼容。

#### Block {#data-type-block}

块类型。

匹配脚本块（`{ ... }`）。仅适用于作为值的脚本表达式，并递归匹配块内容。

仅用于内部表示，不对应规则表达式字符串。

> CWTools 兼容性：兼容。

### 额外基本数据类型 {#data-types-extra-basic}

#### PercentageField {#data-type-percentage-field}

百分比字段类型。

匹配数字部分为浮点数的百分比值字符串（如 `50.0%`）。

对应的数据表达式的格式：
- `percentage_field`

> CWTools 兼容性：兼容。

#### IntPercentageField {#data-type-int-percentage-field}

整数百分比字段类型。

匹配数字部分为整数的百分比值字符串（如 `50%`）。

对应的数据表达式的格式：
- `int_percentage_field`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### DateField {#data-type-date-field}

日期字段类型。

匹配日期值字符串（如 `2200.1.1`）。带参数时还会验证日期格式。

对应的数据表达式的格式：
- `date_field`
- `date_field[{format}]` - 其中 `{format}` 匹配日期格式（如 `y.M.d`）。

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

### 引用数据类型 {#data-types-reference}

以下数据类型通过引用其他规则或索引中的内容来进行匹配，其中一些数据类型会匹配某种复杂表达式。

#### Definition {#data-type-definition}

定义引用类型。

匹配对指定类型定义的引用。表达式须为合法标识符（允许 `.` 和 `-`），
可以是整数或浮点数（如 `<technology_tier>` 的情况）。
匹配时验证引用的定义是否存在。

对应的数据表达式的格式：
- `<{type}>` - 其中 `{type}` 匹配类型名。
- `<{type}.{subtypes}>` - 其中 `{type}` 匹配类型名， `{subtypes}` 匹配点号分隔的一组子类型名。

对应的数据表达式的示例：
- `<building>` - 匹配建筑名称引用。
- `<event>` - 匹配事件ID引用。
- `<event.country>` - 匹配事件ID引用。
- `<technology_tier>` - 匹配科技级别引用。这是整数而非字符串。

> CWTools 兼容性：兼容。

#### Localisation {#data-type-localisation}

本地化引用类型。

匹配对本地化键的引用。表达式须为合法标识符（允许 `.`、`-`、`'`）。
匹配时验证引用的本地化是否存在。
引用的本地化所在的本地化文件需要位于 `localisation` 或 `localization` 目录（或其子目录）中。

对应的数据表达式的格式：
- `localisation`

> CWTools 兼容性：兼容。

#### SyncedLocalisation {#data-type-synced-localisation}

同步本地化引用类型。

与 [Localisation][#data-type-localisation] 类似，但指向同步本地化键。
引用的本地化所在的本地化文件需要位于 `localisation_synced` 或 `localization_synced` 目录（或其子目录）中。

对应的数据表达式的格式：
- `localisation_synced`

> CWTools 兼容性：兼容。

#### InlineLocalisation {#data-type-inline-localisation}

内联本地化引用类型。

匹配本地化键引用或用引号括起的任意字符串（后者作为内联文本，以后备匹配返回）。

对应的数据表达式的格式：
- `localisation_inline`

> CWTools 兼容性：兼容。

#### Modifier {#data-type-modifier}

修正引用类型。

匹配对修正（modifier）的引用。表达式须为合法标识符。
匹配时验证引用的修正是否在规则组中存在。优先级高于 [Definition][CwtDataTypes.Definition]。

对应的数据表达式的格式：
- `<modifier>`

> CWTools 兼容性：兼容。

#### EnumValue {#data-type-enum-value}

枚举值类型。

匹配对枚举值的引用。
匹配简单枚举时精确匹配枚举值列表，匹配复杂枚举时则通过索引查询。

对应的数据表达式的格式：
- `enum[{name}]` - 其中 `{name}` 匹配枚举的名字。

对应的数据表达式的示例：
- `enum[weight_or_base]`
- `enum[ship_class]`

> CWTools 兼容性：部分兼容。拥有不同的解析和处理逻辑。

#### Value {#data-type-value}

动态值读取类型。

匹配动态值表达式（如 `target` `target@root` `target@root.owner`），表示对已声明动态值的读取引用。
动态值的名字须为合法标识符（允许 `.`）。

对应的数据表达式的格式：
- `value[{name}]` - 其中 `{name}` 匹配动态值类型的名字。

对应的数据表达式的示例：
- `value[event_target]`

> CWTools 兼容性：兼容。

#### ValueSet {#data-type-value-set}

动态值写入类型。

匹配动态值表达式（如 `target` `target@root` `target@root.owner`），表示对动态值的写入（声明）引用。
动态值的名字须为合法标识符（允许 `.`）。

对应的数据表达式的格式：
- `value_set[{name}]` - 其中 `{name}` 匹配动态值类型的名字。

对应的数据表达式的示例：
- `value_set[event_target]`

> CWTools 兼容性：兼容。

#### DynamicValue {#data-type-dynamic-value}

动态值类型。

匹配动态值表达式（如 `target` `target@root` `target@root.owner`），表示对动态值的引用（不区分读写）。
动态值的名字须为合法标识符（允许 `.`）。

对应的数据表达式的格式：
- `dynamic_value[{name}]` - 其中 `{name}` 匹配动态值类型的名字。

对应的数据表达式的示例：
- `dynamic_value[event_target]`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### ScopeField {#data-type-scope-field}

作用域字段类型。

匹配作用域字段表达式（由多个作用域节点组成，通过点号分隔并形成链接，如 `root` `root.owner` `root.event_target:target`）。

对应的数据表达式的格式：
- `scope_field`

> CWTools 兼容性：兼容。

#### Scope {#data-type-scope}

作用域类型。

匹配作用域字段表达式（由多个作用域节点组成，通过点号分隔并形成链接，如 `root` `root.owner` `root.event_target:target`），同时约束输出作用域类型。
参数为 `any` 时，等同于 [ScopeField](#data-type-scope-field)。

对应的数据表达式的格式：
- `scope[{type}]` - 其中 `{type}` 匹配作用域类型名。使用 `any` 表示任意类型。

对应的数据表达式的示例：
- `scope[country]`
- `scope[any]`

> CWTools 兼容性：兼容。

#### ScopeGroup {#data-type-scope-group}

作用域组类型。

匹配作用域字段表达式（由多个作用域节点组成，通过点号分隔并形成链接，如 `root` `root.owner` `root.event_target:target`），同时约束输出作用域属于指定的作用域组。

对应的数据表达式的格式：
- `scope_group[{name}]` - 其中 `{name}` 匹配作用域分组的名字。

对应的数据表达式的示例：
- `scope_group[economic_categories]`

> CWTools 兼容性：兼容。

#### ValueField {#data-type-value-field}

值字段类型。

匹配浮点数或值字段表达式（由零个或多个作用域节点，以及最后一个值字段节点组成，通过点号分隔并形成链接，如 `var` `root.var` `root.value:sv`）。
带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。

范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。

对应的数据表达式的格式：
- `value_field`
- `value_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。

对应的数据表达式的示例：
- `value_field`
- `value_field[0.0..1.0]`

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### IntValueField {#data-type-int-value-field}

整数值字段类型。

匹配整数或整数值字段表达式（由零个或多个作用域节点，以及最后一个值字段节点组成，通过点号分隔并形成链接，如 `var` `root.var` `root.value:sv`）。
带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。

范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。

对应的数据表达式的格式：
- `int_value_field`
- `int_value_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。

对应的数据表达式的示例：
- `int_value_field`
- `int_value_field[-100..100]`

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### VariableField {#data-type-variable-field}

变量字段类型。

匹配浮点数或变量字段表达式（由零个或多个作用域节点，以及最后一个变量节点组成，通过点号分隔并形成链接，如 `var` `root.var`）。
可以视为 [ValueField](#data-type-value-field) 的一种特殊的子集。
带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。

范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。

对应的数据表达式的格式：
- `variable_field`
- `variable_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。
- `variable_field_32` - 32 位变体。
- `variable_field_32{range}` - 32 位变体。其中 `{range}` 匹配范围参数（如 `[0.0..1.0]` `[-100.0..100.0)` `[0.0..inf)`）。

对应的数据表达式的示例：
- `variable_field`
- `variable_field[0.0..1.0]`
- `variable_field_32`

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### IntVariableField {#data-type-int-variable-field}

整数变量字段类型。

匹配整数或整数变量字段表达式（由零个或多个作用域节点，以及最后一个变量节点组成，通过点号分隔并形成链接，如 `var` `root.var`）。
可以视为 [IntValueField](#data-type-int-value-field) 的一种特殊的子集。
带范围参数时，还会限制数值范围（仅做标注，仍然视为匹配）。

范围参数可以是开区间与闭区间的任意组合，习惯上使用 `inf` 表示无限大。

对应的数据表达式的格式：
- `int_variable_field`
- `int_variable_field{range}` - 其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。
- `int_variable_field_32` - 32 位变体。
- `int_variable_field_32{range}` - 32 位变体。其中 `{range}` 匹配范围参数（如 `[0..1]` `[-100..100)` `[0..inf)`）。

对应的数据表达式的示例：
- `int_variable_field`
- `int_variable_field[-100..100]`
- `int_variable_field_32`

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### Command {#data-type-command}

命令表达式类型。

匹配命令表达式（由零个或多个命令作用域节点，以及最后一个命令字段节点组成，通过点号分隔并形成链接，如 `GetName` `Root.GetName`）。
命令表达式在本地化文件中被广泛使用（`[...]`），然而，目前仅作占位，不支持匹配脚本文件中的表达式。

对应的数据表达式的格式：
- `$command`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### ScriptValueReference {#data-type-script-value-reference}

脚本值引用表达式类型。

匹配脚本值引用表达式（如 `some_sv|PARAM|VALUE|`）。

对应的数据表达式的格式：
- `$script_value_reference`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### DefineReference {#data-type-define-reference}

定值引用表达式类型。

匹配定值引用表达式（如 `Namespace|Name`）。

对应的数据表达式的格式：
- `$define_reference`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### ArrayDefineReference {#data-type-array-define-reference}

数组定值引用表达式类型。

匹配数组定值引用表达式（如 `Namespace|Name|0`，索引从0开始）。

对应的数据表达式的格式：
- `$array_define_reference`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### Tags {#data-type-tags}

标签集表达式类型。

匹配标签集表达式（由逗号分隔的一组动态值节点组成，如 `tag` `tag1,tag2`），或者空字符串。
在条件变体下，可对其中的动态值节点进行取反（如 `tag1,not(tag2)`）。

对应的数据表达式的格式：
- `$tags[{name}]` - 其中 `{name}` 匹配动态值类型的名字。
- `$tags_condition[{name}]` - 条件变体。其中 `{name}` 匹配动态值类型的名字。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### DatabaseObject {#data-type-database-object}

数据库对象表达式类型。

匹配数据库对象表达式（由冒号分隔的多段引用节点组成，如 `building:x` `civic:x:y`）。

对应的数据表达式的格式：
- `$database_object`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### NameFormat {#data-type-name-format}

命名格式表达式类型。

匹配命名格式表达式（如 `{alpha}` `{<adj> {<noun>}}`）。

对应的数据表达式的格式：
- `name_format[{type}]` - 其中 `{name}` 匹配格式类型，对应的定义类型为 `{name}_name_format`。

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### ShaderEffect {#data-type-shader-effect}

着色器效果类型。

匹配对着色器效果（shader effect）的引用。
插件目前将这些引用视为动态引用，尽管其声明实际上位于 `.shader` 文件中。

“动态引用”意味着不存在实际上的声明处，仅区分读写访问，如同动态值一样。而这里总是视为读访问。

对应的数据表达式的格式：
- `$shader_effect`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### MeshLocator {#data-type-mesh-locator}

网格定位器类型。

匹配对网格定位器（mesh locator）的引用。
插件目前将这些引用视为动态引用，尽管其声明实际上位于 `.mesh` 文件中。

“动态引用”意味着不存在实际上的声明处，仅区分读写访问，如同动态值一样。而这里总是视为读访问。

对应的数据表达式的格式：
- `$mesh_locator`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### TechnologyWithLevel {#data-type-technology-with-level}

带等级的科技类型。

匹配带等级的科技引用（如 `some_repeatable_tech@1`），通过 `@` 分隔科技名和等级。
仅限 Stellaris 游戏类型，且优先级低于 [Definition](#data-type-definition)。

对应的数据表达式的格式：
- `$technology_with_level`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### Parameter {#data-type-parameter}

参数名类型。

匹配参数名。表达式须为合法标识符。即使对应的定义声明中不存在该参数名，也视为匹配。

对应的数据表达式的格式：
- `$parameter`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### ParameterValue {#data-type-parameter-value}

参数值类型。

匹配参数值。只要不是块即可匹配。

对应的数据表达式的格式：
- `$parameter_value`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### LocalisationParameter {#data-type-localisation-parameter}

本地化参数名类型。

匹配本地化参数名。表达式须为合法标识符（允许 `.`、`-`、`'`）。

对应的数据表达式的格式：
- `$localisation_parameter`

> CWTools 兼容性：不兼容。插件作为扩展提供。

### 别名数据类型 {#data-types-alias}

以下数据类型与别名解析机制相关，通常不直接参与脚本匹配，而是由别名系统内部处理。

#### SingleAliasRight {#data-type-single-alias-right}

单别名右侧类型。

不直接参与脚本匹配，由别名解析机制处理。只能用来匹配属性值。

对应的数据表达式的格式：
- `single_alias_right[{name}]` - 其中 `{name}` 匹配单别名的名字。

> CWTools 兼容性：兼容。

#### AliasKeysField {#data-type-alias-keys-field}

别名键字段类型。

匹配时解析别名子键并递归匹配。

对应的数据表达式的格式：
- `alias_keys_field[{name}]` - 其中 `{name}` 匹配别名的名字。

> CWTools 兼容性：兼容。

#### AliasName {#data-type-alias-name}

别名名称类型。

匹配时解析别名子键并递归匹配。只能用来匹配属性键，且需要与 [AliasMatchLeft](#data-type-alias-match-left) 组合使用。

对应的数据表达式的格式：
- `alias_name[{name}]` - 其中 `{name}` 匹配别名的名字。

> CWTools 兼容性：兼容。

#### AliasMatchLeft {#data-type-alias-match-left}

别名匹配左侧类型。

不直接参与脚本匹配，由别名解析机制处理。只能用来匹配属性值，且需要与 [AliasName](#data-type-alias-name) 组合使用。

对应的数据表达式的格式：
- `alias_match_left[{name}]` - 其中 `{name}` 匹配别名的名字。

> CWTools 兼容性：兼容。

### 路径引用数据类型 {#data-types-path-reference}

以下数据类型用于匹配文件路径引用，匹配时验证路径引用的文件是否存在。

#### Icon {#data-type-icon}

图标路径类型。

匹配对图标文件的路径引用。
匹配时验证路径引用的图片文件是否存在，需要指定路径模式，从而限定父路径。

说明：
- 对于路径引用，兼容 "/" "\" 以及重复或作为后缀的分隔符，即会做规范化处理。
- 对于路径引用，不兼容作为前缀的分隔符，即**不兼容**绝对路径形式。
- 不区分文件扩展名。

对应的数据表达式的格式：
- `icon[{path}]` - 其中 `{path}` 匹配路径模式（如 `gfx/interface/icons`）。

对应的数据表达式的示例：
- `icon[gfx/interface/icons]`

> CWTools 兼容性：兼容。

#### FilePath {#data-type-file-path}

文件路径类型。

匹配对文件的路径引用。
匹配时验证路径引用的文件是否存在，可以指定路径模式，从而限定父路径和文件扩展名，或是使用相对路径定位。

说明：
- 对于路径引用，兼容 "/" "\" 以及重复或作为后缀的分隔符，即会做规范化处理。
- 对于路径引用，兼容并忽略作为前缀的分隔符，即**兼容**绝对路径形式。

对应的数据表达式的格式：
- `filepath` - 使用相对于入口路径的路径定位。
- `filepath[./]` - 使用相当于当前脚本文件的路径定位。
- `filepath[{path}]` - 其中 `{path}` 匹配路径模式。

对应的数据表达式的示例：
- `filepath`
- `filepath[./]`
- `filepath[flags/]`
- `filepath[common/inline_scripts/,.txt]`

> CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。

#### FileName {#data-type-file-name}

文件名类型。

匹配对文件名的引用。
匹配时验证路径引用的文件是否存在。可以指定路径模式，从而限定父路径。

说明：
- 对于路径引用，兼容 "/" "\" 以及重复或作为后缀的分隔符，即会做规范化处理。
- 对于路径引用，不兼容作为前缀的分隔符，即**不兼容**绝对路径形式。
- 仅区分文件名。

对应的数据表达式的格式：
- `filename`
- `filename[{path}]` - 其中 `{path}` 匹配路径模式。

对应的数据表达式的示例：
- `filename`
- `filename[gfx/models]`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### AbsoluteFilePath {#data-type-absolute-file-path}

绝对文件路径类型。

匹配绝对文件路径字符串。
匹配时仅验证为字符串类型（通配匹配）。

对应的数据表达式的格式：
- `abs_filepath`

> CWTools 兼容性：不兼容。插件作为扩展提供。

### 模式感知的数据类型 {#data-types-pattern-aware}

以下数据类型采用特殊的模式匹配策略。常量数据类型 [Constant](#data-type-constant) 也属于这类数据类型。

#### Constant {#data-type-constant}

常量类型。

匹配与常量值完全相同的表达式。
作为值时，布尔常量（`yes` / `no`）不会匹配用引号括起的字符串字面量。
另外，不含特殊字符（`:.@[]<>`）的字符串字面量会被回退解析为此类型。

对应的数据表达式的格式：
- 直接使用常量值作为数据表达式字符串本身，如 `yes`、`10`、`trigger` 等。

> CWTools 兼容性：兼容。

#### TemplateExpression {#data-type-template-expression}

模板表达式类型。

由常量文本片段和引用片段交替组成的模式。
匹配时将脚本表达式按模板结构拆分，逐个验证各引用片段。

对应的数据表达式的示例：
- `a_<b>_enum[c]_value[d]`
- `job_<job>_add`

此类型为模式感知类型，其数据表达式格式即为模板表达式本身（参见[模板表达式](#config-expression-template)）。

> CWTools 兼容性：部分兼容。拥有不同的解析和处理逻辑。

#### Glob {#data-type-glob}

<!-- @see icu.windea.pls.core.match.GlobMatcher -->

GLOB 模式类型。模式感知的数据类型之一。

匹配符合 GLOB 模式的表达式。支持通配符 `?`（单个字符） 和 `*`（任意个字符）。

对应的数据表达式的格式：
- `glob:{pattern}` - 其中 `{pattern}` 匹配模式。
- `glob.i:{pattern}` - 忽略大小写的变体。

对应的数据表达式的示例：
- `glob:name?`
- `glob.i:*desc`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### Ant {#data-type-ant}

<!-- @see icu.windea.pls.core.match.AntMatcher -->

ANT 路径模式类型。模式感知的数据类型之一。

匹配符合 ANT 路径模式的表达式。支持通配符 `?`（单个字符）、`*`（子路径中的任意个字符）和 `**`（任意个子路径）。

对应的数据表达式的格式：
- `ant:{pattern}` - 其中 `{pattern}` 匹配模式。
- `ant.i:{pattern}` - 忽略大小写的变体。

对应的数据表达式的示例：
- `ant:**/*.txt`
- `ant.i:common/**/*`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### Regex {#data-type-regex}

<!-- @see icu.windea.pls.core.match.RegexMatcher -->

正则表达式模式类型。模式感知的数据类型之一。

匹配符合正则表达式的表达式。

对应的数据表达式的格式：
- `re:{pattern}` - 其中 `{pattern}` 匹配模式。
- `re.i:{pattern}` - 忽略大小写的变体。

对应的数据表达式的示例：
- `re:^country_.*`
- `re.i:event_.*`

> CWTools 兼容性：不兼容。插件作为扩展提供。

### 后缀感知的数据类型 {#data-types-suffix-aware}

#### SuffixAwareDefinition {#data-type-suffix-aware-definition}

后缀感知的定义引用类型。

由基础定义引用和逗号分隔的后缀列表组成，匹配时同时验证定义引用和后缀。
如果后缀列表为空，则退化为普通的 [Definition](#data-type-definition)。

对应的数据表达式的格式：
- `<{type}>|{suffixes}` - 其中 `{type}` 匹配类型名，`{suffixes}` 匹配逗号分隔的一组后缀。
- `<{type}.{subtypes}>|{suffixes}` - 其中 `{type}` 匹配类型名， `{subtypes}` 匹配点号分隔的一组子类型名，`{suffixes}` 匹配逗号分隔的一组后缀。

对应的数据表达式的示例：
- `<event>|country,crisis`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### SuffixAwareLocalisation {#data-type-suffix-aware-localisation}

后缀感知的本地化引用类型。

由基础本地化引用和逗号分隔的后缀列表组成，匹配时同时验证本地化引用和后缀。
如果后缀列表为空，则退化为普通的 [Localisation](#data-type-localisation)。

对应的数据表达式的格式：
- `localisation|{suffixes}` - 其中 `{suffixes}` 匹配逗号分隔的一组后缀。

对应的数据表达式的示例：
- `localisation|name,desc`

> CWTools 兼容性：不兼容。插件作为扩展提供。

#### SuffixAwareSyncedLocalisation {#data-type-suffix-aware-synced-localisation}

后缀感知的同步本地化引用类型。

由基础同步本地化引用和逗号分隔的后缀列表组成，匹配时同时验证同步本地化引用和后缀。
如果后缀列表为空，则退化为普通的 [SyncedLocalisation](#data-type-synced-localisation)。

对应的数据表达式的格式：
- `localisation_synced|{suffixes}` - 其中 `{suffixes}` 匹配逗号分隔的一组后缀。

> CWTools 兼容性：不兼容。插件作为扩展提供。

## FAQ {#faq}

#### 关于模板表达式 {#faq-template}

<!-- @see icu.windea.pls.config.CwtDataTypes.TemplateExpression -->
<!-- @see icu.windea.pls.config.configExpression.CwtTemplateExpression -->

模板表达式由多个[数据表达式](#config-expression-data)片段（如定义引用、枚举引用、动态值引用等）与常量片段组合而成，用来进行更加灵活的匹配。详见[模板表达式](#config-expression-template)章节。

以下示例展示了从简单字面量到复杂模板的演进：

- `x`：字符串字面量，精确匹配 `x`。
- `a_<job>_b`：包含定义引用 `<job>` 的模板，可匹配 `a_researcher_b`、`a_farmer_b` 等。
- `a_enum[weight_or_base]_b`：包含枚举引用 `enum[weight_or_base]` 的模板，可匹配 `a_weight_b` 和 `a_base_b`。
- `a_value[anything]_b`：包含动态值引用 `value[anything]` 的模板。由于 `value[anything]` 通常没有取值限制，效果近似于正则表达式 `a_.*_b`。

示例：

```cwt
x
a_<job>_b
a_enum[weight_or_base]_b
a_value[anything]_b
```

#### 如何在规则文件中使用 ANT 路径模式 {#faq-ant}

<!-- @see icu.windea.pls.config.CwtDataTypes.Ant -->

从插件版本 1.3.6 开始，可以在数据表达式中使用 ANT 路径模式进行更灵活的匹配。
ANT 表达式通过前缀标识：`ant:` 表示区分大小写，`ant.i:` 表示忽略大小写。

这里使用的 ANT 路径模式支持以下通配符：
- `?`：匹配任意单个字符。
- `*`：匹配任意字符（不含 `/`）。
- `**`：匹配任意字符（含 `/`）。

示例：

```cwt
ant:/foo/bar?/*
ant.i:/foo/bar?/*
```

#### 如何在规则文件中使用正则表达式 {#faq-regex}

<!-- @see icu.windea.pls.config.CwtDataTypes.Regex -->

从插件版本 1.3.6 开始，可以在数据表达式中使用正则表达式进行更灵活的匹配。
正则表达式通过前缀标识：`re:` 表示区分大小写，`re.i:` 表示忽略大小写。
前缀之后的部分即为标准的正则表达式。

示例：

```cwt
re:foo.*
re.i:foo.*
```

#### 如何在规则文件中指定定义成员的出现次数 {#faq-cardinality}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.cardinality -->
<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

在规则文件中，定义成员的出现次数范围是通过选项 `## cardinality` 指定的。
此选项的值是一个[基数表达式](#config-expression-cardinality)，用于约束定义成员的出现次数，影响代码检查与代码补全等功能。

如果未显式指定，且此成员规则的数据类型是常量或枚举值，则推断为 `1..~1`，否则默认使用 `0..inf`。

示例：

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

#### 如何在规则文件中指定作用域上下文 {#faq-scope-context}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.pushScope -->
<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.replaceScopes -->

在规则文件中，作用域上下文是通过选项 `## push_scope` 与 `## replace_scopes`（或 `## replace_scope`）指定的。

`## push_scope = x` 用于将指定的作用域类型压入当前的作用域堆栈。

`## replace_scopes = { this = x root = y}` 用于将指定的系统作用域到作用域类型的映射替换到当前的作用域上下文。仅支持 `this`、`root` 和基于 `from` 的系统作用域，不支持基于 `prev` 的系统作用域。

示例：

```cwt
# for this example, the next this scope will be `country`
## push_scope = country
some_config = single_alias_right[trigger_clause]

# for this example, the next this scope will be `country`
# so do the next root scope, the next from scope, and the next fromfrom scope
## replace_scopes = { this = country root = country from = country fromfrom = country }
some_config = single_alias_right[trigger_clause]
```

#### 如何在规则文件中指定支持的作用域 {#faq-supported-scopes}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.supportedScopes -->

在规则文件中，触发器（trigger）与效果（effect）的支持的作用域是通过选项 `## scopes`（或 `## scope`）指定的。

示例：

```cwt
# for this example, the supported scope type of trigger `has_country_flag` is `country`
## scopes = { country }
alias[trigger:has_country_flag] = value[country_flag]
```

#### 如何在规则文件中指定颜色类型 {#faq-color-type}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.colorType -->
<!-- @see icu.windea.pls.config.CwtDataTypes.ColorField -->
<!-- @see icu.windea.pls.ep.codeInsight.hints.ParadoxColorProvider -->

在规则文件中，属性和值的颜色类型是通过选项 `## color_type` 指定的，这适用于字符串和数字数组。

对于脚本颜色字段（如 `rgb { 255 255 255 }`），颜色类型由其匹配的数据表达式 `color[{type}]` 中的 `{type}` 指定，可参见 [ColorField](#data-type-color-field)。

通过指定颜色类型，可以为脚本文件中的各种目标提供颜色的装订线图标，以便查看与修改颜色。

示例（规则片段）：

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

示例（匹配的脚本片段）：

```paradox_script
color = 0x2288E1

color_rgb = { 34 136 225 }
color_hsv = { 208 0.849 0.882 }

color_field_rgb = rgb { 34 136 225 }
color_field_hsv = hsv { 208 0.849 0.882 }
```

#### 如何在规则文件中指定路径引用的扩展名 {#faq-file-extensions}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.fileExtensions -->

在规则文件中，路径引用的允许的扩展名是通过选项 `## file_extensions` 指定的。

通过指定允许的扩展名，可以限制路径引用可匹配的文件扩展名，从而提供代码检查与过滤代码补全。

需要注意的是，某些数据类型（如 [Icon](#data-type-icon)）与格式（如已指定了扩展名信息的情况）的路径引用不会携带扩展名信息，因此也不应使用此选项。

示例：

```cwt
## file_extensions = { png dds tga }
icon = filepath

## file_extensions = { png dds tga }
texture = filename[gfx/models]

## file_extensions = { ogg }
file = filepath[./]
```

#### 如何在规则文件中注入规则 {#faq-config-inject}

<!-- @see icu.windea.pls.config.option.CwtOptionDataHolder.inject -->
<!-- @see icu.windea.pls.ep.config.config.CwtInjectConfigPostProcessor -->

从插件版本 2.1.0 开始，可以通过使用选项 `## inject` 在规则的解析阶段注入规则。

如果已存在规则片段

```cwt
# some/file.cwt
some = {
    property = v1
    property = v2
}
```

那么规则片段

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

在处理后等价于

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

备注：
- `@` 之前的是规则文件相对于规则分组目录（例如，插件的 jar 包中的 `config/stellaris` 目录）的路径，并且必须完全匹配（不支持通配符，不忽略大小写）。
- `@` 之后的是规则路径，子路径 `-` 匹配所有单独的值，其余情况会作为通配符（忽略大小写，使用 `any` 或 `*` 匹配任意字符，使用 `?` 匹配单个字符）匹配对应键的所有属性。
- 仅适用于值为子句的规则（即 `k = {...}` 或 `{...}`），匹配的规则会被注入到子句中的最后，作为目标规则的子规则。
- 规则注入仅在规则文件的解析阶段处理一次，因此可以在任意规则文件中的任意位置进行注入。
- 如果注入失败（匹配的规则不存在、存在递归等情况），则直接忽略，并打印警告日志。