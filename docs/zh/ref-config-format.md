# 附录：规则格式参考

## 概述

待编写。

## 规则

待编写。

## 规则表达式

> 本节解释在 CWT 规则（.cwt，CWT config file）与扩展能力中使用到的“规则表达式”的用途、格式与默认/边界行为，帮助模组作者正确书写规则。

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtConfigExpression -->
<!-- AI: impl-notes
Resolvers (Schema/Cardinality/Template/Data(key|value|template)/ImageLocation/LocalisationLocation) share Guava caches: maximumSize=4096, expireAfterAccess=10 minutes.
Schema: allow empty names for Type/Constraint; prefer Template when both ends are '$'; enum inside larger string -> Template; escaped dollars not replaced; odd dollars -> Constant; only escaped dollars -> Constant; Template.pattern replaces each unescaped '$...$' with '*', also records TextRanges.
Template (data-driven): forbid blanks; a single snippet (pure const or pure dynamic) is not a template; choose leftmost earliest dynamic rule with prefix/suffix; special split to avoid combining symbol + rule-name as a single constant.
Cardinality: '~' relax flags; negative min clamped to 0; 'inf' (case-insensitive) is unlimited; if invalid or min>max -> treat as empty constraint.
Location: '$' indicates placeholder in 'location'; ImageLocation: '|' args, '$' args -> namePaths, others -> framePaths; LocalisationLocation: '$' args -> namePaths, 'u' -> force upper case; if multiple placeholders exist, all placeholders are replaced.
Schema tests cover edge cases mentioned above.
-->

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

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtSchemaExpression (subtypes: Constant, Template, Type, Enum, Constraint) -->

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

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtDataExpression -->

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

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtTemplateExpression -->

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

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtCardinalityExpression -->

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

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtLocationExpression -->

用于定位目标资源（图片 / 本地化）。若 `location` 中包含 `$`，表示存在占位符，通常在后续步骤以“定义名或属性值”等替换。

默认与边界行为：

- **占位符数量**：建议最多一个；若出现多个，所有占位符都会被替换。

#### 图片位置表达式

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtImageLocationExpression -->

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

说明：`icon` 可解析为文件路径、sprite 名或定义名；若为定义名则继续解析其最相关图片。

#### 本地化位置表达式

<!-- AI: maps to icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression -->

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

