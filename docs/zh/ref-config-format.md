# 附录：规则格式参考

<!--
@doc-meta
本文档是 CWT 规则格式的参考手册，描述插件所支持的各种规则的用途、格式、字段与注意事项。
文档内容基于 Paradox Language Support 插件的实现编写，与 CWTools 的规则格式在多数情况下兼容，但在细节和扩展点上存在差异。

@see docs/zh/config.md
@see icu.windea.pls.config.config.*
@see icu.windea.pls.config.configExpression.*
-->

## 定位与愿景 {#vision}

本文档是 CWT 规则格式的参考手册，面向希望理解、编写或扩展 CWT 规则文件的所有读者——包括模组作者、规则文件的协助维护者、插件维护者以及 AI 编程助手。

本文档旨在：

- **统一术语与边界**：对齐插件与 CWTools 的语义，明确插件的扩展点与差异。
- **建立从文档到实现的映射**：在必要时标注对应的接口与解析器，便于回溯源码与验证行为。
- **指导实践**：概述各类规则的用途、格式与注意事项，为正确编写和维护规则文件打好基础。

关于规则系统的整体介绍（如规则分组、规则覆盖、自定义规则等），请参阅 `docs/zh/config.md`。

## 总览 {#overview}

插件通过读取 `.cwt` 规则文件，构建"规则分组（config group）"，并将其中的规则解析为结构化的"规则对象（config object）"。这些规则对象在代码高亮、补全、导航、检查、快速文档等语言功能中被广泛使用。

规则系统由两大要素构成：

- **规则（config）**：每条规则定义了键、值或块的允许形态与上下文约束，如类型、枚举、别名、链接等。
- **规则表达式（config expression）**：嵌入在规则的字符串字段中，用于描述取值形态或匹配模式的结构化语法，如 `<type>`、`enum[...]`、`value[...]`，以及基数表达式、模板表达式、位置表达式等。

规则的整体处理流程可以简化为三个阶段：

1. 读取规则分组中的各个规则文件，构建其语法树（PSI）。
2. 按规则类别，使用对应的解析器（Resolver）将语法树节点转化为结构化的规则对象。
3. 在各语言功能中，根据当前上下文（作用域、类型名、声明上下文等）查询并应用这些规则对象。

规则的来源与覆盖机制详见 `docs/zh/config.md` 中"规则分组"与"覆盖方式"相关章节。

**术语约定**：本文档中的"规则"按层级分为以下几类：

- **基础规则**：如 `CwtPropertyConfig`，是语法树级别的通用节点，用于承载规则文件中的属性和值。本文档不逐一介绍基础规则。
- **普通规则**：驱动各种语言功能的核心规则，包括类型、别名、枚举、链接、作用域等。
- **扩展规则**：用于增强插件功能的附加规则，如为特定定义或内联脚本提供额外的上下文与提示。
- **内部规则**：由插件内部使用的规则，目前不支持（或尚不支持）自定义。

## 规则 {#configs}

<!-- @see icu.windea.pls.config.config.CwtConfig -->

> 本章节介绍各种规则的用途、格式要点与注意事项，帮助读者正确理解与编写这些规则。

### 普通规则 {#configs-normal}

> 这些规则驱动了各种各样的语言功能，包括但不限于代码补全、代码检查、快速文档、内嵌提示等。

#### 优先级规则 {#config-priority}

<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideStrategy -->
<!-- @see icu.windea.pls.lang.overrides.ParadoxOverrideService -->
<!-- @see cwt/core/priorities.core.cwt -->

优先级规则用于配置"目标"（文件、全局封装变量、定义、本地化等）的覆盖方式。它影响目标的生效顺序与查询结果排序（流式查询除外）。未命中任何目录映射时，默认使用 `LIOS`（后读覆盖）。

**覆盖方式**：

- **`FIOS`**（First In, Only Served）：先加载者生效，后加载者被忽略。
- **`LIOS`**（Last In, Only Served）：后加载者覆盖先加载者。
- **`DUPL`**（Duplicates）：整文件覆盖，必须用同路径文件进行整体替换。
- **`ORDERED`**（Ordered）：顺序读取，后加载者按序新增或合并，不覆盖既有条目。

查询（非流式）结果的排序由优先级驱动；同一路径下按加载顺序（游戏 / 依赖链）决定先后。同一文件内，后出现的项覆盖前面出现的项。

**格式说明**：

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

**示例**：

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

#### 声明规则 {#config-declaration}

<!-- @see icu.windea.pls.config.config.delegated.CwtDeclarationConfig -->
<!-- @see icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider -->
<!-- @see icu.windea.pls.ep.config.CwtInjectedConfigProvider -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.deepCopyConfigsInDeclaration -->

声明规则描述了"定义条目"的结构，是补全、检查与快速文档等功能的基础。

**路径定位**：`{name}`，其中 `{name}` 为规则名称（即"定义类型"名）。规则文件中的顶级属性，如果键为合法标识符且未被其他规则匹配到，会回退尝试解析为声明规则。

声明规则的处理流程大致如下：首先，只有键为合法标识符的顶级属性才会被视为声明规则。如果声明的根级值为 `single_alias_right[...]`，会先进行内联展开。随后，插件会按子类型裁剪和扁平化规则树——匹配当前上下文子类型的 `subtype[...]` 块会被展开为平级子规则，不匹配的则跳过。最终生成的规则树用于驱动补全、检查等功能。

声明规则可以与其他规则协作：在声明内可引用别名与单别名（`alias_name[...]` / `alias_match_left[...]`、`single_alias_right[...]`）。切换类型（swapped type）的声明可直接嵌套在对应基础类型的声明中。游戏规则（game rule）和动作触发（on action）还可以通过扩展规则改写声明上下文。

**示例**：

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

**注意事项**：

- `subtype[...]` 仅在与上下文子类型匹配时生效；不匹配将被忽略（不会报错）。
- 根级 `single_alias_right[...]` 会先被展开，再参与后续解析与检查。
- 为保证后续功能能够"向上溯源"，生成的规则节点均会保持父链（parent config）引用。

#### 系统作用域规则 {#config-system-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig -->
<!-- @see cwt/core/system_scopes.core.cwt -->

系统作用域规则为内置的"系统级作用域"（如 This、Root、Prev、From 等）提供元信息，用于快速文档与作用域栈推导。

**路径定位**：`system_scopes/{id}`，其中 `{id}` 为系统作用域 ID。

**字段含义**：

- `id`：系统作用域 ID。
- `base_id`：基底作用域 ID，未指定时默认为 `id`。用于将同族系统作用域（如 `Prev` / `PrevPrev`、`From` / `FromFrom`）归类。
- `: string`（值）：可读名称，未指定时默认为 `id`。

系统作用域规则与"作用域与作用域分组"规则一起决定作用域检查与提示。在部分扩展规则中，可使用选项 `replace_scopes` 指定系统作用域在当前上下文下对应的具体作用域类型（如将 `this` / `root` / `from` 映射为 `country`）。需要注意的是，`replace_scopes` 不支持替换 `prev` 系列系统作用域，详见 `docs/zh/config.md` 中的相关说明。

**示例（内置）**：

```cwt
system_scopes = {
    This = {}
    Root = {}
    Prev = { base_id = Prev }
    From = { base_id = From }
    # Chain members like PrevPrev/FromFrom are omitted
}
```

#### 指令规则 {#config-directive}

<!-- @see icu.windea.pls.config.config.delegated.CwtDirectiveConfig -->
<!-- @see cwt/cwtools-stellaris-config/config/common/inline_scripts.cwt -->
<!-- @see cwt/cwtools-vic3-config/config/common/definition_injections.cwt -->
<!-- @see cwt/cwtools-eu5-config/config/common/definition_injections.cwt -->

指令规则用于描述脚本文件中区别于一般结构的特殊表达式和结构，并提供额外的提示和验证元数据。这些表达式和结构会改变游戏运行时脚本解析器的行为，从而改变、扩展或复用已有的脚本片段。不同的指令可以拥有不同的规则结构。

目前涉及的指令包括：

- **内联脚本（inline_script）**：（Stellaris）在解析阶段被替换为目标文件的内容，且可以指定参数。
- **定义注入（definition_injection）**：（VIC3 / EU5）在解析阶段对目标定义的声明进行注入或替换，且可以指定模式以决定具体行为。

**路径定位**：`directive[{name}]`，`{name}` 为规则名称。

**示例**：

```cwt
directive[inline_script] = {
    # ...
}
```

#### 类型规则与子类型规则 {#config-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSubtypeConfig -->

类型规则按"文件路径 / 键名"等条件定位并命名"定义（definition）"，并可声明子类型、展示信息与图片。

**路径定位**：

- 类型：`types/type[{type}]`，`{type}` 为定义类型名。
- 子类型：`types/type[{type}]/subtype[{subtype}]`。

**文件匹配**：`path` / `path_file` / `path_extension` / `path_pattern` / `path_strict` 的组合决定了参与扫描的文件集合。其中 `path` 和 `path_extension` 会在解析时进行规范化处理（例如移除 `game/` 前缀）。`type_per_file` 表示"一文件一类型实例"。

**类型键约束**：`type_key_prefix` 指定键前缀；`type_key_filter` / `type_key_regex` / `starts_with` 用于约束类型键的取值；`skip_root_key` 允许跳过若干顶级键后继续匹配（忽略大小写，支持多组）。

**名称与唯一性**：`name_field` 指定展示名称来源字段；`name_from_file` 表示从文件名推导名称；`unique` 用于冲突检查与导航提示；`severity` 标注展示严重级别。

**子类型**：子类型通过 `type_key_filter`、`type_key_regex`、`starts_with`、`only_if_not`、`group` 等选项进行匹配，按声明顺序裁剪。通常与声明规则中的 `subtype[...]` 一起使用，以细化结构与校验。

**展示**：`localisation` 和 `images` 小节分别用于类型的本地化展示与图片展示设置。

类型规则与声明规则协作，为具体定义的声明提供上下文与结构约束。类型规则中还可以声明 `modifiers` 小节，派生出与类型绑定的修正规则。

**示例**：

```cwt
types = {
    type[civic_or_origin] = {
        # File sources
        path = "game/common/governments/civics"   # the prefix `game/` will be removed automatically
        path_extension = .txt

        # Key constraints and prefix
        type_key_prefix = civic_
        ## type_key_filter = { civic_ }  # include sets
        ## type_key_filter <> { origin_ }  # exclude sets
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

**注意事项**：

- 缺少必需属性会导致类型被跳过（日志中会有提示）。
- `path` 与 `path_pattern` 可并用；`path_strict` 会强制严格匹配。
- `skip_root_key` 为多组设置：若存在任意一组与文件顶级键序列匹配，则允许跳过后继续匹配类型键。
- 子类型匹配"顺序敏感"，请将更具体的规则放在更前面。

#### 别名规则与单别名规则 {#config-alias}

<!-- @see icu.windea.pls.config.config.delegated.CwtAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSingleAliasConfig -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.inlineAlias -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.inlineSingleAlias -->

别名规则将可复用的规则片段抽象成"具名别名"，在多处引用并展开。单别名用于"值侧"的一对一复用。

**路径定位**：

- 别名：`alias[{name}:{subName}]`（`{subName}` 为受限的数据表达式）。
- 单别名：`single_alias[{name}]`。

**声明与引用语法**：

- 声明别名：`alias[effect:some_effect] = { ... }`
- 使用别名：`alias_name[effect] = alias_match_left[effect]`
- 声明单别名：`single_alias[trigger_clause] = { alias_name[trigger] = alias_match_left[trigger] }`
- 使用单别名：`potential = single_alias_right[trigger_clause]`

别名支持通过选项指定作用域约束：`scope` / `scopes` 声明允许的输入作用域集合，`push_scope` 声明输出作用域。别名的 `subName` 支持受限的数据表达式，用于匹配与提示。

在使用处，别名体会被复制为普通属性规则（键名 = 子名，值和子规则深拷贝，保留选项）。如果展开结果的值侧仍为 `single_alias_right[...]`，会继续触发级联展开。别名常与声明规则结合使用，在定义声明中复用 trigger / effect 等片段。

**示例**：

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

**注意事项**：

- 别名唯一键由 `name:subName` 组成；重复定义将按覆盖方式 / 优先级处理。
- 展开后才会进行基数与选项校验；请在展开位置而非声明处考虑最终语义。
- `subName` 为数据表达式（受限），可使用模板 / 枚举等提高复用度，但请避免过宽导致误匹配。

#### 枚举规则与复杂枚举规则 {#config-enum}

<!-- @see icu.windea.pls.config.config.delegated.CwtEnumConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtComplexEnumConfig -->

枚举规则为数据表达式 `enum[...]` 提供取值集合。根据值的来源不同，分为简单枚举和复杂枚举。

**路径定位**：

- 简单枚举：`enums/enum[{name}]`
- 复杂枚举：`enums/complex_enum[{name}]`

---

**简单枚举（Enum）**

简单枚举的值集合全部在规则文件中声明，匹配时忽略大小写。当前实现仅支持常量值，不支持模板表达式。

```cwt
enums = {
    enum[weight_or_base] = { weight base }
}
```

---

**复杂枚举（Complex Enum）**

复杂枚举从脚本文件中按路径和锚点动态收集枚举值。

`path` / `path_file` / `path_extension` / `path_pattern` / `path_strict` 组合决定参与扫描的文件集合，`path` 和 `path_extension` 会在解析时规范化。`start_from_root` 指定是否从文件顶部（而非顶级属性）开始查询锚点。`name` 小节描述如何在匹配文件中定位值锚点——实现会收集其中所有名为 `enum_name` 的属性或值作为锚点。

插件扩展选项：`## case_insensitive` 将复杂枚举值标记为忽略大小写；`## per_definition` 将同名同类型复杂枚举值的等效性限制在定义级别（而非文件级别）。

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

**注意事项**：

- 简单枚举当前仅支持常量值；若填写模板表达式，不会被按模板解析。
- 复杂枚举若缺少 `name` 小节或未能在匹配文件中找到任何 `enum_name` 锚点，将导致该枚举为空。
- 简单枚举值默认忽略大小写，复杂枚举值默认不忽略大小写。

#### 动态值类型规则 {#config-dynamic-value}

<!-- @see icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig -->

动态值类型规则为数据表达式 `value[...]` 提供"预定义（硬编码）"的动态值集合，替代固定字面量，便于补全与校验。当前实现仅支持常量值，不支持模板表达式。

**路径定位**：`values/value[{name}]`，`{name}` 为动态值类型名。

若需为动态值声明"作用域上下文"或按上下文动态生成值，请参考扩展规则中的"动态值的扩展规则"。

**示例**：

```cwt
values = {
    value[event_target] = { owner capital }  # case-insensitive
}
```

#### 链接规则 {#config-link}

<!-- @see icu.windea.pls.config.config.delegated.CwtLinkConfig -->

链接规则为复杂表达式中的"字段 / 函数样"节点提供语义与类型约束（作用域 / 值），支撑链式访问与补全检查。

**路径定位**：

- 常规链接：`links/{name}`
- 本地化链接：`localisation_links/{name}`（若未显式声明，会自动复制静态的常规链接）

**静态与动态**：未声明 `data_source` 的链接为静态链接，仅代表一个固定的节点名（如 `owner`）。声明了 `data_source` 与 / 或 `prefix` / `from_*` 的链接为动态链接，可携带动态数据（如 `modifier:x`、`relations(x)`、`var:x`）。

**主要字段**：

- `type`：链接类型（`scope` / `value` / `both`，默认为 `scope`）。
- `from_data`：是否从文本数据中读取动态数据（格式如 `prefix:data`）。
- `from_argument`：是否从传参中读取动态数据（格式如 `func(arg)`）。
- `argument_separator`：多传参时使用的分隔符（`comma` / `pipe`，默认为 `comma`）。
- `prefix`：动态链接的前缀。
- `data_source`（可多值）：每个数据源是一个数据表达式，用于约束动态数据的合法取值。
- `input_scopes`：输入作用域集合，可写单个或集合，同时支持 `input_scope` 与 `input_scopes` 两种写法。
- `output_scope`：输出作用域；为空时表示透传或基于数据源推导。
- `for_definition_type`：仅在指定定义类型中可用。

**示例**：

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

**注意事项**：

- `prefix` 不应带引号或括号；`input_scopes` 使用花括号集合语法（如 `{ country }`）。
- 可混合多个 `data_source`。
- 若动态链接参数为单引号字面量，则按字面量处理，通常不提供补全。
- 建议在 `data_source` 中使用 `<type>` 简写（如 `<country>`），而非 `definition[country]`。

#### 作用域规则与作用域分组规则 {#config-scope}

<!-- @see icu.windea.pls.config.config.delegated.CwtScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig -->

作用域规则定义"作用域类型"及其别名，作用域分组规则对作用域进行分组，二者用于作用域检查、链路约束与提示。

**路径定位与字段**：

- 作用域：`scopes/{name}`
  - `name`：作用域 ID。
  - `aliases: string[]`：别名集合（忽略大小写）。
- 作用域分组：`scope_groups/{name}`
  - `name`：分组名。
  - `: string[]`（值列表）：分组内作用域 ID 集合（忽略大小写）。

作用域规则与系统作用域共同决定作用域栈与含义；与链接规则共同约束链式访问的输入 / 输出作用域。在扩展规则中可通过 `replace_scopes` 指定在特定上下文下系统作用域映射到的具体作用域类型。

**示例**：

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

#### 修正规则与修正分类规则 {#config-modifier}

<!-- @see icu.windea.pls.config.config.delegated.CwtModifierConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig -->

修正规则声明修正（modifier）与其分类，用于图标渲染、补全与作用域校验。

**路径定位**：

- 修正：
  - `modifiers/{name}`（`{name}` 可为常量或模板表达式）
  - `types/type[{type}]/modifiers/{name}`（其中 `$` 会被替换为 `<{type}>`）
  - `types/type[{type}]/modifiers/subtype[{subtype}]/{name}`（`{type}.{subtype}` 作为类型表达式参与替换）
- 修正分类：`modifier_categories/{name}`

**修正字段**：`name` 为模板化名称（如 `job_<job>_add`），支持匹配动态生成的修正。`categories` 为分类名集合，决定允许的作用域类型。如果已解析出分类映射，则基于类别汇总作用域；否则回退到修正自身的选项 `supported_scopes`。

**修正分类字段**：`name` 为分类名（如 `Pops`），`supported_scopes` 为该分类允许的作用域集合。

修正规则与类型规则的 `modifiers` 小节联动：在类型规则中声明的修正名称使用 `$` 占位，解析时会被替换为 `<{type}>` 或 `<{type}.{subtype}>`，从而派生出与类型绑定的修正规则。

**示例**：

```cwt
# Standalone modifier declarations
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

**注意事项**：

- 修正条目缺少 `categories` 会被跳过（不生效）。
- 类型规则中的修正名称使用 `$` 占位，请确保与类型 / 子类型表达式对应。
- 类别中的 `supported_scopes` 应使用标准作用域 ID，解析时会自动归一化大小写。

#### 本地化命令规则与本地化提升规则 {#config-localisation}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig -->

本地化命令规则声明"本地化命令字段"（如 `GetCountryType`）的可用性与允许作用域。本地化提升规则声明"本地化作用域提升"，使得通过本地化链接切换作用域后仍能使用对应的命令字段。

**路径定位**：

- 本地化命令：`localisation_commands/{name}`（名称忽略大小写）
- 本地化提升：`localisation_promotions/{name}`（名称忽略大小写，对应本地化链接名）

二者均包含 `supported_scopes` 字段，声明允许的作用域类型集合。

**示例**：

```cwt
localisation_commands = {
    GetCountryType = { country }
}

localisation_promotions = {
    Ruler = { country }
}

# In localisation text:
# [Ruler.GetCountryType] is valid under the promoted scope after Ruler link
```

**注意事项**：

- 名称大小写不敏感；请保持与实际使用一致的拼写风格以便检索。
- 提升规则的名称应与本地化链接名一致；否则无法正确匹配。
- 静态常规链接会自动复制为本地化链接；如需动态行为，请单独声明本地化链接。

#### 类型展示规则 {#config-type-presentation}

<!-- @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeImagesConfig -->

类型展示规则为定义类型配置"名称 / 描述 / 必需本地化键"和"主要图片 / 切分规则"等展示信息，以便在 UI、导航与提示中展示。

**路径定位**：

- 本地化：`types/type[{type}]/localisation`
- 图片：`types/type[{type}]/images`

二者结构一致：由若干"子类型表达式 + 位置规则"的配对组成。在运行时根据实际"定义的子类型集合"过滤并合并得到最终的规则列表。位置规则的常用选项包括 `required`（是否必需项）和 `primary`（是否主要项，用于主展示图标 / 主名称）。位置表达式的详细语法参见"规则表达式 → 位置表达式"。

**示例**：

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

#### 数据库对象类型规则 {#config-db-type}

<!-- @see icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig -->

数据库对象类型规则为本地化中的"数据库对象表达式"（如 `['civic:some_civic', ...]`）定义类型与格式，支持在 UI 与提示中将其解析为定义或本地化。

**路径定位**：`database_object_types/{name}`，`{name}` 为前缀（如 `civic`）。

**字段含义**：

- `type`：若存在，将 `prefix:object` 的 `object` 作为该类型的定义引用。
- `swap_type`：若存在，将 `prefix:object:swap` 的 `swap` 作为切换类型的定义引用。
- `localisation`：若存在，将 `prefix:object` 的 `object` 作为本地化键解析。

**示例**：

```cwt
database_object_types = {
    civic = {
        type = civic_or_origin
        swap_type = swapped_civic
    }
}
```

#### 位置规则 {#config-location}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocationConfig -->

位置规则声明图片 / 本地化等资源的定位键与位置表达式，用于类型展示规则的 `localisation` 和 `images` 小节中。

**路径定位**：`types/type[{type}]/localisation/{key}` 和 `types/type[{type}]/images/{key}`。

#### 行规则 {#config-row}

<!-- @see icu.windea.pls.config.config.delegated.CwtRowConfig -->

行规则为 CSV 行声明列名与取值形态，用于补全与检查。

**路径定位**：`rows/row[{name}]`。

`path` / `path_file` / `path_extension` / `path_pattern` / `path_strict` 组合决定参与扫描的文件集合。`columns` 小节声明列名到列规则的映射，`end_column` 声明终止列名（匹配到后视为可省略的尾列）。

**示例**：

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

#### 语言环境规则 {#config-locale}

<!-- @see icu.windea.pls.config.config.delegated.CwtLocaleConfig -->

语言环境规则声明语言环境（locale）的基本信息，便于识别项目 / 用户偏好的语言环境，改进 UI 展示与本地化校验。

**路径定位**：`locales/{id}`，`{id}` 如 `l_english`。

**字段含义**：

- `id`：语言环境 ID。
- `codes: string[]`：该语言环境包含的语言代码（如 `en`、`zh-CN`）。
- 派生字段：`shortId`（去除前缀 `l_`）、`idWithText`（带展示文本）。

**示例**：

```cwt
locales = {
    l_english = { codes = { "en" } }
    l_simp_chinese = { codes = { "zh-CN" } }
}
```

### 扩展规则 {#configs-extended}

> 这些规则用于增强插件的功能，例如指定规则上下文、提供额外的快速文档与内嵌提示文本等。
>
> 扩展规则中有一些常见的共通特征：
> - 大部分扩展规则支持多种**名称匹配**方式：常量、模板表达式、ANT 路径模式和正则表达式（详见 FAQ）。
> - 大部分扩展规则支持通过选项注释提供 `hint`（提示文本，用于快速文档和内嵌提示）。
> - 部分扩展规则支持通过选项注释指定**作用域上下文**（`replace_scopes` / `push_scope`）。

#### 封装变量的扩展规则 {#config-extended-scripted-variable}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig -->

为脚本中的封装变量（scripted variable）提供额外提示（快速文档、内嵌提示等）。

**路径定位**：`scripted_variables/{name}`。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**格式说明**：

```cwt
scripted_variables = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)

    ### Some documentation
    ## hint = §RSome inlay hint text§!
    x
}
```

**注意事项**：

- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。
- 本条目仅提供"提示增强"，不负责声明或校验封装变量的取值与类型。

#### 定义的扩展规则 {#config-extended-definition}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig -->

为具体"定义（definition）"提供额外上下文与提示信息，包括文档 / 提示（`hint`）、绑定定义类型（`type`，必填）、以及按需指定的作用域上下文（`replace_scopes` / `push_scope`）。

**路径定位**：`definitions/{name}`。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**格式说明**：

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

**注意事项**：

- `type` 为必填；缺失将导致该条目被跳过。
- 此扩展用于"提示与上下文增强"，并不直接改变声明规则的结构。

#### 游戏规则的扩展规则 {#config-extended-game-rule}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig -->

为游戏规则（即类型为 `game_rule` 的定义）提供文档 / 提示增强，并支持"重载声明规则"。

**路径定位**：`game_rules/{name}`。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

当条目为属性节点时（如 `x = { ... }` 或 `x = single_alias_right[...]`），其值或子块会作为"声明规则重载"在使用处生效。仅当为属性节点时才会产生重载效果；纯值节点仅提供提示。

**格式说明**：

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

**注意事项**：

- 若值为 `single_alias_right[...]`，会先被内联展开，再作为重载规则生效。
- 该扩展仅影响"声明规则的来源 / 结构"与"提示信息"，不改变整体优先级与覆盖方式。

#### 动作触发的扩展规则 {#config-extended-on-action}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig -->

为动作触发（即类型为 `on_action` 的定义）提供文档 / 提示增强，并指定"事件类型"以影响声明上下文中与事件有关的引用。

**路径定位**：`on_actions/{name}`。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

`event_type`（必填）声明事件类型，用于在声明上下文中将与事件相关的数据表达式替换为该事件类型对应的表达式。

**格式说明**：

```cwt
on_actions = {
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    ## event_type = country
    x
}
```

**注意事项**：

- `event_type` 为必填；缺失将导致该条目被跳过。
- 如需作用域替换，可结合 `replace_scopes` 使用。

#### 内联脚本的扩展规则 {#config-extended-inline-script}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig -->

为具体的内联脚本（inline script）声明"上下文规则"和"作用域上下文"，用于在被调用处提供正确的补全与检查。

**路径定位**：`inline_scripts/{name}`。名称支持常量、模板表达式、ANT 路径模式与正则表达式。其中 `name` 为 `x/y` 时，对应文件为 `common/inline_scripts/x/y.txt`。

`context_configs_type` 控制上下文规则的聚合形态：`single`（默认）仅取值侧作为上下文规则；`multiple` 取子规则列表作为上下文规则。

**格式说明**：

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

**注意事项**：

- 若仅需单条上下文规则，保持默认 `single` 即可；需要声明多条时使用 `multiple`。
- 根级 `single_alias_right[...]` 会被内联展开后再作为上下文规则使用。

#### 复杂枚举值的扩展规则 {#config-extended-complex-enum-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig -->

为复杂枚举的具体条目提供文档 / 提示增强（快速文档、内嵌提示等）。

**路径定位**：`complex_enum_values/{type}/{name}`，其中 `{type}` 为复杂枚举名，`{name}` 为条目名或匹配模式。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**格式说明**：

```cwt
complex_enum_values = {
    component_tag = {
        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x
    }
}
```

**注意事项**：

- 本扩展不改变复杂枚举"值来源"的收集逻辑，仅提供提示信息。
- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。

#### 动态值的扩展规则 {#config-extended-dynamic-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig -->

为某种动态值类型下的具体"动态值"条目提供文档 / 提示增强。

**路径定位**：`dynamic_values/{type}/{name}`，其中 `{type}` 为动态值类型，`{name}` 为条目名或匹配模式。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**格式说明**：

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

**注意事项**：

- 本扩展不改变动态值类型与基础"值集合"的定义，仅提供提示信息。
- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。

#### 参数的扩展规则 {#config-extended-parameter}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedParameterConfig -->

为触发器 / 效果 / 内联脚本中的参数（`$PARAM$` 或 `$PARAM|DEFAULT$`）提供文档与上下文增强：绑定上下文键、声明上下文规则与作用域上下文，以及支持从使用处继承上下文。

**路径定位**：`parameters/{name}`。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**主要字段**：

- `context_key`（必填）：上下文键（如 `scripted_trigger@some_trigger`），`@` 之前为包含的定义类型（或 `inline_script`），`@` 之后为定义名或内联脚本路径。上下文键自身也支持模式匹配。
- `context_configs_type`：`single`（默认）或 `multiple`，含义同内联脚本扩展规则。
- `inherit`：设为 `yes` 时，从参数的"使用处"继承上下文（规则与作用域），而非使用静态声明。

**格式说明**：

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

**注意事项**：

- `context_key` 为必填；缺失将导致该条目被跳过。
- `inherit = yes` 时，上下文取自"使用处"，可能为空或因位置不同而变化。
- 根级 `single_alias_right[...]` 会被内联展开后再作为上下文规则使用。

#### 复杂枚举值的扩展规则 {#config-extended-complex-enum-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedComplexEnumValueConfig -->

为复杂枚举的具体条目提供文档 / 提示增强（快速文档、内嵌提示等）。

**路径定位**：`complex_enum_values/{type}/{name}`，其中 `{type}` 为复杂枚举名，`{name}` 为条目名或匹配模式。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**格式说明**：

```cwt
complex_enum_values = {
    component_tag = {
        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x
    }
}
```

**注意事项**：

- 本扩展不改变复杂枚举"值来源"的收集逻辑，仅提供提示信息。
- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。

#### 动态值的扩展规则 {#config-extended-dynamic-value}

<!-- @see icu.windea.pls.config.config.extended.CwtExtendedDynamicValueConfig -->

为某种动态值类型下的具体"动态值"条目提供文档 / 提示增强。

**路径定位**：`dynamic_values/{type}/{name}`，其中 `{type}` 为动态值类型，`{name}` 为条目名或匹配模式。名称支持常量、模板表达式、ANT 路径模式与正则表达式。

**格式说明**：

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

**注意事项**：

- 本扩展不改变动态值类型与基础"值集合"的定义，仅提供提示信息。
- 名称可使用模板 / ANT / 正则匹配，但请避免过宽导致误匹配。

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

## 数据类型 {#data-types}

<!-- @see icu.windea.pls.config.CwtDataType -->
<!-- @see icu.windea.pls.config.CwtDataTypes -->
<!-- @see icu.windea.pls.config.CwtDataTypeSets -->
<!-- @see icu.windea.pls.ep.config.configExpression.CwtDataExpressionResolver -->
<!-- @see icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher -->

> 本章节介绍数据类型的概念、分类与用途，帮助读者理解规则文件中的数据表达式如何与脚本文件中的实际内容进行匹配。

### 概述

数据类型（Data Type）是连接"规则表达式"与"脚本内容"的桥梁。每条数据表达式在解析后都会得到一个具体的数据类型，该数据类型决定了这条表达式能够匹配脚本文件中的哪些键或值。

例如，数据表达式 `<event.country>` 的数据类型为 `Definition`，附带元数据 `event.country`，表示匹配类型为 `event`、子类型包含 `country` 的定义。又如，`enum[weight_or_base]` 的数据类型为 `Enum`，附带元数据 `weight_or_base`，表示匹配该枚举中声明的所有可选值。

数据类型的解析由 `CwtDataExpressionResolver` 扩展点驱动，匹配逻辑由 `ParadoxScriptExpressionMatcher` 扩展点驱动。二者协作，使规则系统能够灵活地支持各种复杂的取值形态。

### 基本数据类型

以下数据类型表示脚本中的基本取值形态：

- **`Block`**：匹配块（`{ ... }`）。当数据表达式为块时使用。
- **`Bool`**：匹配布尔值（`yes` / `no`）。
- **`Int`**：匹配整数，可附带范围约束（如 `int[-5..100]`）。
- **`Float`**：匹配浮点数，可附带范围约束（如 `float[0.0..1.0]`）。
- **`Scalar`**：匹配任意标量值（字符串、数字、布尔等均可）。
- **`String`**：匹配任意字符串。通常以加引号的形式出现在脚本中。
- **`ColorField`**：匹配颜色字段（如 `color[rgb]`、`color[hsv]` 等）。
- **`PercentageField`**：匹配百分比字段（如 `percentage_field`）。
- **`DateField`**：匹配日期字段（如 `date_field`）。

### 引用数据类型

以下数据类型通过引用其他规则或索引中的内容来进行匹配：

- **`Constant`**：匹配固定的常量字符串。当数据表达式为字面量时使用。
- **`Definition`**：匹配特定类型的定义。语法为 `<type>` 或 `<type.subtype>`。
- **`Enum`**：匹配枚举值（简单枚举或复杂枚举）。语法为 `enum[name]`。
- **`DynamicValue`**：匹配动态值。语法为 `value[name]`。
- **`Modifier`**：匹配修正名。语法为 `modifier`。
- **`Parameter`**：匹配参数引用。语法为 `parameter`。
- **`ShorthandParameter`**：匹配简写参数引用。语法为 `shorthand_parameter`。
- **`LocalisationCommand`**：匹配本地化命令字段。语法为 `localisation_command`。
- **`DatabaseObject`**：匹配数据库对象。语法为 `database_object[type]`。

### 复杂数据类型

以下数据类型对应更复杂的表达式结构，匹配的脚本表达式通常会被进一步解析为"复杂表达式"：

- **`ScopeField`**：匹配作用域字段表达式（如 `root.owner`）。语法为 `scope_field`。
- **`Scope`**：匹配特定的作用域。语法为 `scope[name]` 或 `scope[group_name]`。
- **`ScopeGroup`**：匹配作用域分组。语法为 `scope_group[name]`。
- **`ValueField`**：匹配值字段表达式。语法为 `value_field`。
- **`VariableField`**：匹配变量字段表达式。语法为 `variable_field`。
- **`IntVariableField`**：匹配整数变量字段表达式。语法为 `int_variable_field`。
- **`InlineScript`**：匹配内联脚本表达式。语法为 `single_alias_right[inline_script_usage]`（特殊处理）。

### 特殊数据类型

- **`AnyType`**：匹配任意类型（包括块），用于宽松校验场景。语法为 `any`。
- **`Other`**：兜底类型，当无法解析为上述任何已知类型时使用。

### 数据类型分组

插件内部将数据类型按行为特征分组（`CwtDataTypeSets`），用于在特定上下文中快速判断表达式的可用行为。例如：

- 哪些数据类型可以出现在复杂表达式的键侧或值侧。
- 哪些数据类型支持作为"动态值"或"作用域"参与链式访问。
- 哪些数据类型需要参与补全或校验等。

这些分组主要服务于插件内部逻辑，规则文件的编写者通常不需要直接关注。

## 规则表达式 {#config-expressions}

<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->

> 本章节介绍各种规则表达式的用途、格式与默认 / 边界行为，帮助读者正确理解与编写这类特殊的表达式。

### 基础概念与适用范围

规则表达式是在规则的"字符串字段"中使用的结构化语法，用于描述值的形态或匹配模式。主要包括：

- **数据表达式**（Data Expression）：解析数据类型或动态片段。
- **模板表达式**（Template Expression）：由常量与动态片段拼接的模式，用于更灵活的匹配。
- **基数表达式**（Cardinality Expression）：用于声明出现次数范围及宽松校验。
- **位置表达式**（Location Expression）：用于定位图片、本地化等资源。
- **模式表达式**（Schema Expression）：用于规则文件本身的 RHS 取值形态声明。

### 数据表达式 {#config-expression-data}

<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->

数据表达式用于描述脚本文件中键或值的取值形态，可为常量、基本数据类型、引用或动态内容等。

要点：

- **键 / 值上下文**：解析会区分键（isKey=true）与值（isKey=false）。
- **类型**：解析后会得到具体的数据类型（如 `int`、`float`、`scalar`、`enum[...]`、`scope[...]`、`<type_key>` 等），详见"数据类型"章节。
- **扩展元数据**：按数据类型可附带数值范围、大小写策略等（例如 `int[-5..100]`、`float[-inf..inf]`）。

默认与边界行为：

- 无法匹配任何规则时，类型回退为 `Constant`，并把原始字符串写入扩展属性 `value`。
- 空串按 `Constant("")` 处理；解析"块"时返回专用类型 `Block`。
- 定义类型优先使用 `<country>` 这类尖括号简写，而非 `definition[country]`。
- 可混用不同来源的动态片段，例如 `<country>/<planet>`、`dynamic_value[test_flag]`。
- 对于 `relations('...')` 等带参动态链接，若参数为单引号字面量，视为字面量，不提供代码补全。

示例（节选）：

```cwt
int
float
enum[shipsize_class]
scope[country]
<ship_size>
pre_<opinion_modifier>_suf
```

### 模板表达式 {#config-expression-template}

<!-- @see icu.windea.pls.config.configExpression.CwtTemplateExpression -->

模板表达式由多个片段拼接而成（常量片段 + 动态片段），用于描述更复杂的取值形态。可视为多个数据表达式的组合。

默认与约束：

- 包含空白字符视为无效模板（直接返回空表达式）。
- 仅存在一个片段（纯常量或纯一个动态）时不视为模板。
- 匹配采用"最左最早匹配"拆分策略。
- 每段最终委托数据表达式解析；未匹配到规则时降级为 Constant。

示例：

```cwt
job_<job>_add           # "job_" + <job> + "_add"
xxx_value[anything]_xxx # "xxx_" + value[anything] + "_xxx"
a_enum[weight_or_base]_b # "a_" + enum[weight_or_base] + "_b"
```

**注意事项**：

- 常量片段与"看起来像规则名"的组合紧邻时，优先保证动态规则的正确识别。
- 若需要空白，请改用更合适的匹配方式（如 ANT / 正则）。

### 基数表达式 {#config-expression-cardinality}

<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

基数表达式用于约束定义成员的出现次数，驱动代码检查与代码补全等功能。

用 `min..max` 表示允许的出现次数范围，`~` 为宽松标记，`inf` 表示无限。

默认与边界行为：

- 最小值为负数时会被钳制为 0。
- `max` 为 `inf`（不区分大小写）时表示无限。
- 无 `..` 分隔时视为无效，不产生约束。
- `min > max` 时视为无效，不产生约束。

示例：

```cwt
## cardinality = 0..1
## cardinality = 0..inf
## cardinality = ~1..10
```

### 位置表达式 {#config-expression-location}

<!-- @see icu.windea.pls.config.configExpression.CwtLocationExpression -->

位置表达式用于定位目标资源（图片、本地化等）的来源。如果表达式中包含 `$`，视为占位符，需要在后续步骤以"定义名或属性值"等动态内容替换。

#### 图片位置表达式 {#config-expression-location-image}

<!-- @see icu.windea.pls.config.configExpression.CwtImageLocationExpression -->

用于定位定义的相关图片。

语法与约定：

- 使用 `|` 分隔参数：`<location>|<args...>`。
- 以 `$` 开头的参数表示"名称文本来源路径"（支持逗号分隔多路径）。
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

说明：`icon` 可被解析为文件路径、sprite 名或定义名；若为定义名则继续解析其最相关图片。

#### 本地化位置表达式 {#config-expression-localisation}

<!-- @see icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression -->

用于定位定义的相关本地化。

语法与约定：

- 使用 `|` 分隔参数：`<location>|<args...>`。
- 以 `$` 开头的参数表示"名称文本来源路径"（支持逗号分隔多路径）。
- 参数 `u` 表示将最终名称强制转为大写（仅限使用占位符时有效）。
- `$` 参数重复出现时，以后者为准。

示例：

```cwt
$_desc
$_desc|$name
$_desc|$name|u
$_desc|$name,$alt_name  # multiple name paths, comma-separated
title
```

### 模式表达式 {#config-expression-schema}

<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->

模式表达式用于描述规则文件中键与值的取值形态，从而为规则文件本身提供代码补全等功能。目前仅用于提供基础的代码补全，且仅在 `cwt/core/schema.cwt` 中有用到。

支持的形态：

- **常量（Constant）**：不包含 `$` 的原样字符串。
- **模板（Template）**：包含一个或多个参数（`$...$`），如 `$type$`、`type[$type$]`。
- **类型（Type）**：以单个 `$` 起始，如 `$any`、`$int`。
- **约束（Constraint）**：以 `$$` 起始，如 `$$declaration`。

## FAQ {#faq}

#### 关于模板表达式 {#faq-template}

模板表达式由多个数据表达式（如定义、本地化、字符串字面量对应的数据表达式）组合而成，用来进行更加灵活的匹配。

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

#### 如何在规则文件中使用 ANT 路径模式 {#faq-ant}

插件对规则表达式进行了扩展，从插件版本 1.3.6 开始，可以通过 ANT 路径模式进行更加灵活的匹配。

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

#### 如何在规则文件中使用正则表达式 {#faq-regex}

插件对规则表达式进行了扩展，从插件版本 1.3.6 开始，可以通过正则表达式进行更加灵活的匹配。

```cwt
# a regex use prefix 're:'
re:foo.*
# a regex use prefix 're.i:' (ignore case)
re.i:foo.*
```

#### 如何在规则文件中指定作用域上下文 {#faq-scope}

在规则文件中，作用域上下文是通过选项 `push_scope` 与 `replace_scopes` 来指定的。

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

从插件版本 2.1.0 开始，可以通过使用选项 `## inject` 在规则的解析阶段进行规则注入。

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