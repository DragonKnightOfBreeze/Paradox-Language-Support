# 附录：规则格式参考

<!-- TODO 人工改进与润色 -->

## 定位与愿景 {#vision}

本参考面向希望“理解/编写/扩展” CWT 规则（CWT config file）的作者与维护者，旨在：

- **统一术语与边界**：对齐 PLS 与 CWTools 的语义，明确 PLS 的扩展点与差异。
- **建立从文档到实现的映射**：每个规则条目均标注对应接口/解析器，便于回溯源码与验证行为。
- **指导实践**：概述用途、格式与常见陷阱，为后续细化示例与校验规则打基础。

参考关系：

- 概念与示例以 CWTools 指南为基线：`references/cwt/guidance.md`。
- PLS 的整体规则工作流与分组见：`docs/zh/config.md`。
- 规则接口与解析逻辑主要位于：`icu.windea.pls.config.config`（含 `delegated/` 与 `delegated/impl/`）。

<!-- @see icu.windea.pls.config.config -->
<!-- @see icu.windea.pls.config.config.delegated -->
<!-- @see icu.windea.pls.config.config.delegated.impl -->

## 总览 {#overview}

PLS 通过读取 `.cwt` 文件，构建“规则分组”，并将规则解析为结构化的“规则对象”，供代码高亮/补全/导航/检查/文档等功能使用。

- **规则来源与覆盖**：见 `docs/zh/config.md` 的“规则分组/覆盖策略”。常见来源包括内置、远程、本地与项目本地，按“路径 + 规则ID”执行后序覆盖。
- **两大要素**：
  - 规则（config）：定义“键/值/块”的允许形态与上下文（如类型、枚举、别名、链接……）。
  - 规则表达式（config expression）：在规则的字符串字段中描述取值/匹配的语法（如 `<type>`、`enum[...]`、`value[...]`、基数/模板/位置表达式等）。
- **解析流程（简化）**：
  1. 读取规则分组，构建规则文件 PSI。
  2. 按接口类别用解析器（Resolver）构造“委托型/内部”规则对象。
  3. 在语言特性中按上下文（作用域、类型名、声明上下文等）查询并应用这些规则。

术语约定：

- “规则（config）”包含“普通规则（与 CWTools 对齐/兼容）”“扩展规则（PLS 扩展）”“内部规则（PLS 内部使用）”。
- “基础规则”（如 `CwtPropertyConfig`）是语法树级别的通用节点，不在本章节逐一描述。

<!-- @see icu.windea.pls.config.configGroup.CwtConfigGroup -->
<!-- @see icu.windea.pls.config.config.CwtPropertyConfig -->
<!-- @see icu.windea.pls.config.config.delegated.* -->
<!-- @see icu.windea.pls.config.config.delegated.impl.* -->

## 规则 {#configs}

> 本节聚焦“委托型规则（普通/扩展）”与“内部规则”，并补充少量“无需规则对象”的条目。每个小节提供用途、格式要点与解析要点，细节将在后续版本补充示例。

### 普通规则 {#configs-normal}

> 与 CWTools 语义一致或兼容，PLS 可能在选项与上下文方面有少量扩展。

#### 类型与子类型 {#config-type}

- **用途**：按“文件路径/键名”定位并命名“定义”，并可声明子类型、局部作用域与展示信息。
- **格式要点**：`types/type[name] = { ... }`；子类型 `subtype[key] = { 规则… }`；可定义 `name_field`、`type_per_file`、`skip_root_key`、`type_key_filter/regex/starts_with` 等。
- **解析要点**：文件扫描与键名匹配；子类型判定顺序敏感；可向下传递作用域上下文。
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSubtypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtTypeConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtSubtypeConfigResolverImpl -->

#### 别名与单别名 {#config-alias}

- **用途**：复用一组规则；`alias[...]` 作为具名片段复用于多处；`single_alias[...]` 可在 RHS 直接展开。
- **格式要点**：`alias[group:key] = { ... }`；引用 `alias_name[group] = alias_match_left[group]`；单别名右侧 `single_alias_right[key]`。
- **解析要点**：分组与键名合并为唯一ID；单别名按上下文内联展开；校验基数/选项在展开后生效。
<!-- @see icu.windea.pls.config.config.delegated.CwtAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtSingleAliasConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtAliasConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtSingleAliasConfigResolverImpl -->

#### 枚举与复杂枚举 {#config-enum}

- **用途**：提供可枚举值；复杂枚举可从脚本文件动态生成。
- **格式要点**：`enums = { enum[key] = { v1 v2 ... } }`；`complex_enum[key] = { path = ... name = { ... } }`。
- **解析要点**：复杂枚举在索引阶段按路径/模式收集；与 `<type>`/`enum[...]` 结合用于补全与校验。
<!-- @see icu.windea.pls.config.config.delegated.CwtEnumConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtComplexEnumConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtEnumConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtComplexEnumConfigResolverImpl -->

#### 动态值类型 {#config-dynamic-value}

- **用途**：为 `value[...]` 与“值引用”系统提供集合与范围，用以替代固定字面量。
- **格式要点**：声明动态值的名称、来源与作用域上下文（如仅接收 push scope）。
- **解析要点**：结合模板/数据表达式执行宽匹配；在补全与校验中与当前作用域互相约束。
<!-- @see icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDynamicValueTypeConfigResolverImpl -->

#### 链接（Scope/Value 链）{#config-link}

- **用途**：配置 `x.y` 链式访问的合法跳转（输入/输出作用域、可选前缀、数据源等）。
- **格式要点**：`links = { foo = { input_scopes = { ... } output_scope = ... prefix = ... data_source = <...> } }`。
- **解析要点**：前缀不带引号与括号；输入/输出作用域用于链路检查与补全；`data_source` 可混合多个。
<!-- @see icu.windea.pls.config.config.delegated.CwtLinkConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl -->

#### 作用域与作用域组 {#config-scope}

- **用途**：声明脚本中的作用域与分组，用于作用域检查、跳转与提示。
- **解析要点**：与系统作用域/替换规则共同决定“当前作用域栈”。
<!-- @see icu.windea.pls.config.config.delegated.CwtScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtScopeGroupConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtScopeConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtScopeGroupConfigResolverImpl -->

#### 修正与修正类别 {#config-modifier}

- **用途**：提供修正与修正分组的声明，用于图标渲染、校验与补全。
<!-- @see icu.windea.pls.config.config.delegated.CwtModifierConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtModifierConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtModifierCategoryConfigResolverImpl -->

#### 本地化命令与推广 {#config-localisation}

- **用途**：约束可用的本地化命令、指定推广关系与展示细节。
<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocalisationCommandConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocalisationPromotionConfigResolverImpl -->

#### 类型展示（本地化/图片）{#config-type-presentation}

- **用途**：为某类型定义“展示名称/描述/必需本地化键”“主要图片/切分规则”等。
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeLocalisationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtTypeImagesConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtTypeLocalisationConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtTypeImagesConfigResolverImpl -->

#### 数据库对象类型 {#config-db-type}

- **用途**：面向具备“数据库对象”语义的实体，提供类型化建模能力。
<!-- @see icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDatabaseObjectTypeConfigResolverImpl -->

#### 位置与行匹配 {#config-location-row}

- **用途**：位置匹配（如图标/本地化）与行规则（表格式内容）的通用声明。
<!-- @see icu.windea.pls.config.config.delegated.CwtLocationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.CwtRowConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtRowConfigResolverImpl -->

#### 语言环境 {#config-locale}

- **用途**：声明可用语言环境及其属性，辅助本地化流程与格式校验。
<!-- @see icu.windea.pls.config.config.delegated.CwtLocaleConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtLocaleConfigResolverImpl -->

### 扩展规则 {#configs-extended}

> PLS 扩展的规则家族，用于增强 IDE 功能（快速文档、内嵌提示、补全等）。

#### 封装变量（Scripted Variables） {#config-extended-scripted-variable}

- **用途**：声明/提示脚本中的封装变量。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedScriptedVariableConfigResolverImpl -->

#### 定义（扩展） {#config-extended-definition}

- **用途**：为定义提供额外上下文（类型、作用域替换、模板等）。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedDefinitionConfigResolverImpl -->

#### 游戏规则（扩展） {#config-extended-game-rule}

- **用途**：为“游戏规则”条目提供声明与上下文增强。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedGameRuleConfigResolverImpl -->

#### On Actions（扩展） {#config-extended-on-action}

- **用途**：声明 on_action 相关上下文（事件类型、作用域替换等）。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedOnActionConfigResolverImpl -->

#### 内联脚本（扩展） {#config-extended-inline-script}

- **用途**：为内联脚本路径声明上下文配置（可多态），并支持在根级使用单别名。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedInlineScriptConfigResolverImpl -->

#### 参数（扩展） {#config-extended-parameter}

- **用途**：为参数名绑定上下文键/继承策略/作用域替换等，并可在根级使用单别名。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedParameterConfigResolverImpl -->

#### 复杂枚举值（扩展） {#config-extended-complex-enum-value}

- **用途**：为复杂枚举的条目提供扩展声明与提示能力。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedComplexEnumValueConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedComplexEnumValueConfigResolverImpl -->

#### 动态值（扩展） {#config-extended-dynamic-value}

- **用途**：为动态值提供扩展声明与提示能力（含作用域上下文）。
<!-- @see icu.windea.pls.config.config.delegated.CwtExtendedDynamicValueConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtExtendedDynamicValueConfigResolverImpl -->

### 内部规则 {#configs-internal}

> 由 PLS 内部使用，控制解析上下文或维护全局语义。

#### 声明（Declaration） {#config-declaration}

- **用途**：承载“声明级”上下文（如定义/参数/内联脚本等）与注入逻辑；支持深拷贝与父指针注入。
<!-- @see icu.windea.pls.config.config.delegated.CwtDeclarationConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtDeclarationConfigResolverImpl -->
<!-- @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator -->

#### 系统作用域 {#config-system-scope}

- **用途**：声明“系统级”作用域（如 this/root/from 等）与替换规则，影响整体作用域栈。
<!-- @see icu.windea.pls.config.config.delegated.CwtSystemScopeConfig -->
<!-- @see icu.windea.pls.config.config.delegated.impl.CwtSystemScopeConfigResolverImpl -->

#### 优先级（Priorities）与语言环境（Locales） {#config-internal-priority-locale}

- **用途**：配置文件/目录的覆盖方式，及全局语言环境能力。
<!-- @see icu.windea.pls.config.config.delegated.CwtLocaleConfig -->

### 不基于规则对象的规则 {#configs-non-object}

> 这些条目通常以“规则表达式”或“注释选项”的形式出现，不需要独立的规则对象。

- **基础值类型**：`bool`、`int`（可区间/inf）、`float`、`scalar`、`percentage_field`、`localisation(_synced/_inline)`、`filepath[...]`、`icon[...]`、`date_field`、`variable_field`/`value_field` 等。
- **值集合（value_set/value）**：配对声明“集合的定义点/使用点”。
- **选项（Options）**：`## cardinality`、`## push_scope`、`## replace_scopes`、`## severity`、`## scope`。
- **注释语义**：`#` 普通注释、`##` 选项、`###` 文档注释。

进一步的格式与边界行为，见本页“规则表达式”章节与 CWTools 指南。

## 规则表达式 {#config-expressions}

> 本节解释在 CWT 规则（.cwt，CWT config file）与扩展能力中使用到的“规则表达式”的用途、格式与默认/边界行为，帮助模组作者正确书写规则。

<!-- @see icu.windea.pls.config.configExpression.CwtConfigExpression -->

### 基础概念与适用范围

- **定义**：规则表达式是在规则的“字符串字段”中使用的结构化语法，用于描述值的形态或匹配模式。
- **主要家族**：
  - 架构（schema）表达式：用于 RHS 的取值形态声明。
  - 数据表达式：解析数据类型或动态片段。
  - 模板表达式（数据驱动）：由常量与动态片段拼接的模式，用于更灵活的匹配。
  - 基数（cardinality）表达式：用于声明出现次数范围及严谨/宽松校验。
  - 位置表达式：用于定位图片、本地化等资源。

---

### 架构（schema）表达式

<!-- @see icu.windea.pls.config.configExpression.CwtSchemaExpression -->

用于描述规则右侧允许的取值形态（常见于 `.cwt` 的数据类型或占位模板中）。

支持的形态：

- **常量（Constant）**：不含 `$` 的原样字符串。
- **类型（Type）**：以单个 `$` 起始，如 `$any`、`$int`（空名称允许）。
- **约束（Constraint）**：以 `$$` 起始，如 `$$custom`（空名称允许）。
- **枚举（Enum）**：以 `$enum:` 起始并以 `$` 结尾，如 `$enum:ship_size$`。
- **模板（Template）**：包含成对的 `$...$` 片段，其他部分按常量处理，如 `a $x$ b $y$`。

默认与边界行为：

- **奇数个 `$`**：判作非法模板并按常量处理。
- **两端都有 `$`**：优先判为模板，而非类型（`$any$` -> 模板，pattern 为 `*`）。
- **枚举夹在更大字符串内**：整体转为模板（例如 `prefix $enum:ship_size$ suffix`）。
- **转义美元 `\$`**：不参与占位，不会被替换成 `*`。若仅包含转义美元，不构成模板，按常量处理。
- **模板的 pattern**：用 `*` 替换每个未转义的 `$...$` 片段。

示例：

```text
$int             # 类型
$$custom         # 约束
$enum:class$     # 枚举
a $x$ b $y$      # 模板（pattern: "a * b *"）
a \$x\$ b        # 含转义美元，不是模板，视为常量
```

常见陷阱：

- 只出现一次 `$`（或数量为奇数）时，不会形成参数占位，整体按常量处理。
- 模板内的 `$...$` 必须成对且未转义；否则不会被视作参数位置。

---

### 数据表达式

<!-- @see icu.windea.pls.config.configExpression.CwtDataExpression -->

用于描述规则中“键/值”的取值形态，既可表示常量，也可表示由规则驱动的动态片段（如 `value[...]`、`enum[...]`、`scope[...]`、`icon[...]`、`<definition>` 等）。

要点：

- **键/值上下文**：解析会区分键（isKey=true）与值（isKey=false）。
- **类型**：解析后会得到具体的数据类型（如 `int`、`float`、`scalar`、`enum[...]`、`scope[...]`、`<type_key>` 等）。
- **扩展元数据**：按数据类型可附带数值范围、大小写策略等（例如 `int[-5..100]`、`float[-inf..inf]`、`ignoreCase`）。

示例（节选）：

```cwt
count = int
acceleration = float
class = enum[shipsize_class]
who = scope[country]
for_ship = <ship_size>
pre_<opinion_modifier>_suf
```

提示：此处不逐一列举所有基础类型与形式；以本文档的规则约定为准。

---

### 模板表达式（数据驱动）

<!-- @see icu.windea.pls.config.configExpression.CwtTemplateExpression -->

由若干“片段”顺序拼接而成：常量片段 + 动态片段（由数据表达式的动态规则定义，如 `value[...]`/`enum[...]`/`scope[...]`/`icon[...]`/`<...>`）。

默认与约束：

- **不允许空白**：包含空白字符视为无效模板。
- **片段判定**：仅存在一个片段（纯常量或纯动态）时不构成模板。
- **匹配策略**：基于所有动态规则（具有前后缀的规则）进行“最左最早匹配”拆分。

示例：

```text
job_<job>_add
xxx_value[anything]_xxx   # 一般等价于正则 a_.*_b 之类的宽匹配
a_enum[weight_or_base]_b
```

常见陷阱：

- 常量片段与“看起来像规则名”的组合紧邻时，优先保证动态规则的正确识别，避免将“符号 + 规则名”整体当作常量。
- 若需要空白，请改用更合适的匹配方式（如 ANT/正则）。

---

### 基数（cardinality）表达式

<!-- @see icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

用于声明某规则可出现的次数范围，支持“宽松”校验与无限上界。

格式：

```text
min..max           # 例如 0..1, 1..inf
~min..max          # 小于最小值仅警告（宽松）
min..~max          # 大于最大值仅警告（宽松）
```

默认与边界行为：

- **最小值负数**：会被钳制为 0。
- **max 为 `inf`（不区分大小写）**：表示无限。
- **无 `..` 分隔**：视为无效，不产生约束。
- **min > max**：视为无效，不产生约束。

示例（来自注释惯例）：

```cwt
## cardinality = 0..1
## cardinality = 0..inf
## cardinality = ~1..10
```

---

### 位置表达式（资源定位）

<!-- @see icu.windea.pls.config.configExpression.CwtLocationExpression -->

用于定位目标资源（图片 / 本地化）。若 `location` 中包含 `$`，表示存在占位符，通常在后续步骤以“定义名或属性值”等替换。

默认与边界行为：

- **占位符数量**：建议最多一个；若出现多个，所有占位符都会被替换。

#### 图片位置表达式

<!-- @see icu.windea.pls.config.configExpression.CwtImageLocationExpression -->

语法与约定：

- 使用 `|` 分隔参数：`<location>|<args...>`。
- 以 `$` 开头的参数表示“名称文本来源路径”（支持逗号分隔多路径）：替换占位符，写入 namePaths。
- 其他参数表示“帧数来源路径”（支持逗号分隔多路径）：用于图片切分，写入 framePaths。
- 同类参数重复出现时（均以 `$` 开头，或均为非 `$`），以后者为准。

示例：

```text
gfx/interface/icons/modifiers/mod_$.dds
gfx/interface/icons/modifiers/mod_$.dds|$name
GFX_$
icon
icon|p1,p2
```

说明：`icon` 可被解析为文件路径、sprite 名或定义名；若为定义名则继续解析其最相关图片。

#### 本地化位置表达式

<!-- @see icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression -->

语法与约定：

- 使用 `|` 分隔参数：`<location>|<args...>`。
- 以 `$` 开头的参数表示“名称文本来源路径”（支持逗号分隔多路径），写入 namePaths。
- 参数 `u` 表示将最终名称强制转为大写。仅限使用占位符时有效。
- `$` 参数重复出现时，以后者为准。

示例：

```text
$_desc
$_desc|$name
$_desc|$name|u
title
```

