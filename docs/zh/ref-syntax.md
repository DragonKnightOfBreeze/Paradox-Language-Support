# 附录：语法参考

<!--
@doc-meta
本文档是 Paradox Language Support 插件所支持的四种语言的语法参考。
文档内容反映的是插件的解析实现，而非游戏引擎的底层行为——二者在多数情况下一致，但不排除存在差异。

@see src/test/testData/cwt/example.test.cwt
@see src/test/testData/script/example.test.txt
@see src/test/testData/localisation/example.test.yml
@see src/test/testData/csv/example.test.csv
-->

## 总览 {#overview}

本文档是编写 CWT 规则文件和 Paradox 模组时所涉及的领域特定语言的语法参考。
文档内容基于 Paradox Language Support 插件的解析实现编写，在绝大多数情况下与游戏引擎的实际行为一致，但由于游戏引擎的解析细节并非完全公开，个别边界情况可能存在差异。

本文档涵盖以下四种语言：

- **CWT**（`*.cwt`）——用于编写 CWT 规则文件。支持行注释 `#`、选项注释 `##` 和文档注释 `###`。代码块语言 ID：`cwt`。
- **Paradox Script**（`*.txt`、`*.gfx`、`*.gui` 等）——用于编写游戏脚本。支持行注释 `#`。代码块语言 ID：`paradox_script`。
- **Paradox Localisation**（`*.yml`）——用于编写本地化文本。支持行注释 `#`。代码块语言 ID：`paradox_localisation`。
- **Paradox CSV**（`*.csv`）——分号分隔的 CSV 格式。支持行注释 `#`。代码块语言 ID：`paradox_csv`。

其中，CWT 与 Paradox Script 共享相似的基础语法结构（属性、值、块），但在分隔符种类、注释系统和进阶语法上有所不同，本文档将分别说明。

## CWT 语言 {#cwt}

<!--
@impl-notes
Language id: `CWT`; default extension: `.cwt`

@see icu.windea.pls.cwt.CwtLanguage
@see icu.windea.pls.cwt.CwtFileType
@see icu.windea.pls.model.CwtSeparatorType
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.flex
-->

CWT 是一种领域特定语言，用于编写 CWT 规则文件。文件扩展名为 `.cwt`。

CWT 规则文件为插件提供了一套声明式的规范，插件据此为游戏和模组文件（脚本文件、本地化文件和 CSV 文件）提供代码高亮、代码补全、代码检查等高级语言功能。CWT 之于 Paradox 脚本，类似于 JSON Schema 之于 JSON。

CWT 的语法与 Paradox Script 相似，但额外支持选项注释和文档注释。

### 基本元素 {#cwt-basics}

CWT 文件由三种基本元素组成：**属性**（property）、**值**（value）和**块**（block）。空白与换行用于分隔标记，空行可以自由出现。

**属性** 的结构为 `<key> <sep> <value>`，其中分隔符 `<sep>` 可以是以下之一：

- `=` 逻辑等于
- `!=` 或 `<>` 逻辑不等于（二者等价）
- `==` 匹配比较运算符——表示对应的脚本属性可以使用比较运算符（`<`、`>`、`<=`、`>=`、`!=`）作为分隔符，而非仅限 `=` 或 `?=`

其中 `==` 是 CWT 规则文件特有的语义，不出现在脚本文件中。插件目前尚未对此进行严格检查。

键（key）可以不加引号，也可以用双引号包裹。未加引号的键不能包含 `#`、`=`、`{`、`}`、`"` 以及空白字符。

```cwt
playable = yes
cost = 10
acceleration = 20.0
class = some_shipsize_class
"quoted key" = "line\nnext line"
```

### 值类型 {#cwt-values}

CWT 支持以下值类型：

**布尔** 值为 `yes` 或 `no`。

**整数** 如 `10`、`-5`、`042`。允许前导正负号和前导零。

**浮点数** 如 `1.0`、`-0.5`、`.25`。允许省略整数部分。

**字符串** 可以不加引号或用双引号包裹。未加引号的字符串不能包含 `#`、`=`、`{`、`}`、`"` 以及空白字符。双引号字符串支持反斜杠转义序列（如 `\"`、`\n`）。

**块** 使用花括号 `{ ... }` 包裹，内部可以混合包含属性、值和注释。块可以嵌套。

```cwt
ship_size = {
    ### The base cost of this ship_size
    ## cardinality = 0..1
    cost = int

    modifier = {
        alias_name[modifier] = alias_match_left[modifier]
    }
}
```

### 注释系统 {#cwt-comments}

CWT 拥有三级注释系统，这是它区别于 Paradox Script 的主要特征之一：

**行注释** 以 `#` 开头，延续到行尾，用于普通注释。

**选项注释** 以 `##` 开头，用于为紧随其后的成员（属性、值或块）附加元信息。选项注释的内容语法与属性一致，采用 `<optionKey> <sep> <optionValue>` 的形式。常见的选项注释包括 `## cardinality = 0..1`（声明基数约束）、`## severity = warning`（声明检查严重级别）、`## push_scope = country`（声明推入的作用域）等。

**文档注释** 以 `###` 开头，用于为成员提供说明文本，这些文本会在代码补全和快速文档中展示。特别地，对于以 `####`（或更多 `#`）开头的文档注释，插件会将注释文本视为 Markdown 文本。

### 转义 {#cwt-escaping}

使用 `\` 转义键和字符串中的特殊字符。常见的转义序列包括 `\"`（双引号）、`\n`（换行）和 `\\`（反斜杠自身）。未用双引号括起的键和字符串中通常不需要转义。

### 综合示例 {#cwt-example}

以下示例来自插件的语法测试文件，展示了 CWT 的典型用法：

```cwt
boolean_value = yes
number_value = 1.0
string_value = "text"

# Regular comment
types = {
    ### Doc comment
    type[army] = {
        path = "game/common/armies"
        path_extension = .txt
        subtype[has_species] = {
            ## cardinality = 0..0
            has_species = no
        }
        localisation = {
            ## required
            ## primary
            name = "$"
            plural = "$_plural"
            desc = "$_desc"
        }
        images = {
            ## primary
            icon = icon # <sprite>
        }
    }
}
```

## Paradox 脚本语言 {#paradox-script}

<!--
@impl-notes
Language id: `PARADOX_SCRIPT`; extensions: `.txt`, `.gfx`, `.gui`, etc.

@see icu.windea.pls.script.ParadoxScriptLanguage
@see icu.windea.pls.script.ParadoxScriptFileType
@see icu.windea.pls.model.ParadoxSeparatorType
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex

Note on unquoted key/string character restrictions:
The first character additionally excludes `@` (to avoid ambiguity with scripted variable references),
but subsequent characters allow `@`. The document simplifies this to "@ 不可出现" for practical guidance.
-->

Paradox Script 是 Paradox 游戏使用的脚本语言。文件扩展名通常为 `.txt`，但特定位置的文件也可能使用 `.gfx`、`.gui` 等扩展名。

脚本语言的基础语法与 CWT 相似——同样由属性、值和块构成——但在分隔符种类、值类型和进阶语法上有较多差异，下文将逐一说明。

### 基本元素 {#script-basics}

脚本文件同样由**属性**、**值**和**块**组成，并支持以 `#` 开头的行注释。

**属性** 的结构仍为 `<key> <sep> <value>`，但可用的分隔符比 CWT 更丰富：

- `=` 赋值
- `!=` 或 `<>` 不等于
- `<`、`>`、`<=`、`>=` 比较运算
- `?=` 安全赋值（仅 CK3/VIC3/EU5 支持）

其中，`?=` 表示"仅在目标不存在时赋值"。例如在触发器上下文中，`owner ?= { ... }` 等价于先检查 `exists = owner` 再执行 `owner = { ... }`。

键可以不加引号或用双引号包裹。未加引号的键不能包含 `@`、`#`、`$`、`=`、`<`、`>`、`!`、`?`、`{`、`}`、`[`、`]`、`"` 以及空白字符。双引号键内部可以包含参数（见进阶语法一节）。

```paradox_script
enabled = yes
level >= 2
size ?= @my_var
```

### 值类型 {#script-values}

Paradox Script 在 CWT 的基础值类型之上，增加了若干特有的值类型。

**布尔** 值为 `yes` 或 `no`，与 CWT 一致。

**整数** 如 `10`、`-5`。**浮点数** 如 `1.0`、`-0.5`。均允许前导正负号和前导零。

**字符串** 可以不加引号或用双引号包裹。未加引号的字符串不能包含 `@`、`#`、`$`、`=`、`<`、`>`、`!`、`?`、`{`、`}`、`[`、`]`、`"` 以及空白字符——注意比 CWT 多了 `@`、`$`、`<`、`>`、`!`、`?`、`[`、`]` 这些保留字符。双引号字符串支持反斜杠转义，并且可以包含参数和内联参数条件（见进阶语法一节）。

**颜色** 是一种整体值，格式为颜色类型关键字后跟花括号包裹的空白分隔数字，如 `rgb { 255 128 0 }`、`hsv { 0.5 0.8 1.0 }`、`hsv360 { 180 80 100 }`。花括号内只能包含数字和空白。

**块** 使用花括号 `{ ... }` 包裹，内部可以包含注释、属性、值、封装变量和参数条件块。

**封装变量引用** 以 `@` 开头后跟变量名，如 `@my_var`，用于引用先前声明的封装变量。封装变量引用可以出现在多种上下文中：作为独立的值（`cost = @my_var`）、嵌入在未加引号的字符串中（`key = prefix_@my_var_suffix`）、以及在内联数学表达式内作为因子（`@[ base_cost * bonus ]`，此时不带 `@` 前缀）。封装变量引用同样可以在本地化文本的参数中使用（`$@my_var$`）。

**内联数学表达式** 以 `@[` 开头、以 `]` 结尾，如 `@[ 1 + 2 * var ]`。支持的运算符包括 `+`、`-`、`*`、`/`、`%`，以及一元正负号、绝对值 `|expr|` 和括号 `(expr)`。表达式中的因子可以是数字、封装变量引用（不带 `@` 前缀）或参数。

### 封装变量 {#script-scripted-variables}

封装变量（scripted variable）通过 `@<name> = <value>` 的形式声明，其中值可以是布尔、整数、浮点数、字符串或内联数学表达式。声明后可通过 `@<name>` 在其他地方引用。

封装变量名以 `@` 开头，名称部分由字母、数字和下划线组成。名称也可以包含参数（`$...$`），使其参数化。

### 进阶语法 {#script-advanced}

以下语法通常仅在特定定义（如封装效果、封装触发器、脚本值）的声明上下文中生效或被游戏引擎评估。

**参数**（parameter）的语法为 `$name$` 或 `$name|default_value$`（带默认值）。参数名必须以字母或下划线开头，后续可包含字母、数字和下划线（即匹配 `[A-Za-z_][A-Za-z0-9_]*`）。参数可以出现在封装变量名、封装变量引用名、属性键、字符串值和内联数学表达式中。

**参数条件块**（parameter condition block）的语法为 `[[<expression>] <members...> ]`，其中 `<expression>` 可以是参数名或 `!<参数名>`（取反）。它用于根据参数是否存在来条件化地包含一组成员。

**内联参数条件**（inline parameter condition）用于双引号字符串内部的条件片段。例如在 `"prefix[[PARAM]_$PARAM$]_suffix"` 中，`[[PARAM]_$PARAM$]` 部分仅在参数 `PARAM` 存在时才会展开。

### 转义 {#script-escaping}

使用 `\` 转义键和字符串中的特殊字符。常见的转义序列包括 `\"`（双引号）、`\n`（换行）和 `\\`（反斜杠自身）。未用双引号括起的键和字符串中通常不需要转义。

### 综合示例 {#script-example}

以下示例来自插件的语法测试文件，展示了 Paradox Script 的典型用法：

```paradox_script
@var = 1

# Line comment
settings = {
    boolean_value = yes
    number_value = 1.0
    number_value = @var
    string_value = Foo
    string_value = "Foo\n bar "
    values = {
        foo = bar
    }
    values = { 1 2 3 }
    color = rgb { 142 188 241 }
    parameter = $PARAM$
    [[!PARAM] parameter_condition = $PARAM$ ]
    inline_math = @[ 2 + ( $MAX$ - 1 + var ) ]
}
```

以下是一个更完整的示例，进一步展示了分隔符、封装变量、参数和参数条件等语法的组合使用：

```paradox_script
# Scripted variables
@base_cost = 100
@bonus_factor = 1.5

# A scripted effect definition demonstrating various syntax features
apply_research_bonus = {
    # Assignment and comparison separators
    is_ai = no
    num_owned_planets >= 3
    resource_stockpile = { energy > 500 }

    # Scripted variable reference and inline math
    add_resource = {
        energy = @[ base_cost * bonus_factor ]
    }

    # Parameters and parameter conditions
    set_variable = {
        which = research_$category$
        value = $amount|10$
    }
    [[!skip_notification]
        create_message = {
            type = "research_completed"
        }
    ]
}
```

## Paradox 本地化语言 {#paradox-localisation}

<!--
@impl-notes
Language id: `PARADOX_LOCALISATION`; default extension: `.yml`
Localisation files use a two-level lexer: ParadoxLocalisation.flex tokenizes the file structure,
and ParadoxLocalisation.Text.flex lazily tokenizes the rich text content within property values.

@see icu.windea.pls.localisation.ParadoxLocalisationLanguage
@see icu.windea.pls.localisation.ParadoxLocalisationFileType
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex

Localisation icon resolution is handled by the ParadoxLocalisationIconSupport EP.
The default implementation (ParadoxBaseLocalisationIconSupport) resolves icons by:
1. Matching sprite definitions with GFX_text_ or GFX_ prefix
2. Matching image files under gfx/interface/icons/
Game-specific implementations (e.g., ParadoxBaseLocalisationIconSupport.Stellaris)
add additional resolution strategies (e.g., job, swapped_job, resource definitions).

@see icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
@see icu.windea.pls.ep.resolve.localisation.ParadoxBaseLocalisationIconSupport
@see icu.windea.pls.ep.resolve.localisation.ParadoxBaseLocalisationIconSupport.Stellaris
-->

Paradox Localisation 用于为游戏提供可国际化的富文本内容。文件扩展名为 `.yml`，但它并非合法的 YAML——只是借用了 YAML 的视觉风格。本地化文件必须使用 **UTF-8 WITH BOM** 编码（插件可以检测编码问题并自动修正）。

### 文件结构 {#loc-structure}

本地化文件由以下部分组成：

**语言标识行** 是可选的，格式为 `<locale>:`，例如 `l_english:`、`l_simp_chinese:`。语言标识行可以出现多次（以兼容 `localisation/languages.yml` 的格式），也可以完全省略。

**键值对** 是文件的主体内容，每个键值对占一行，格式为 `<key>:<number?> "<text>"`。冒号后的数字是可选的内部追踪号（仅用于 Paradox 的内部翻译追踪，模组中通常省略或填 `0`）。属性键由字母、数字、下划线、点号、连字符和单引号组成。

**注释** 以 `#` 开头，延续到行尾。

```paradox_localisation
l_english:
 # A comment
 greeting:0 "Hello, world!"
 farewell: "Goodbye."
```

### 富文本标记 {#loc-rich-text}

双引号内的文本支持多种内嵌标记，用于实现颜色、动态内容、图标等效果。

**颜色标记** 的语法为 `§<id><text>§!`，其中 `<id>` 是单个字母或数字，表示预定义的颜色代码。例如 `§RRed text§!` 将 "Red text" 渲染为红色。颜色标记可以嵌套。

**参数** 的语法为 `$<name>$` 或 `$<name>|<argument>$`，其中 `|<argument>` 部分为格式化参数（例如用于指定显示格式），不同于脚本语言中参数的默认值语义。`<name>` 的最常见形式是本地化键名（引用另一条本地化文本），由字母、数字、下划线、点号、连字符和单引号组成。除了本地化键名之外，`<name>` 也可以是由脚本传入的变量或参数、游戏预设的宏或变量。在解析层面，`<name>` 还可以是命令表达式或封装变量引用——例如 `$@my_var$` 表示引用封装变量 `my_var` 的值。

**图标** 的语法为 `£<icon>£` 或 `£<icon>|<frame>£`。`<icon>` 通常对应精灵（sprite）定义的名称，但也可能解析为特定路径（`gfx/interface/icons/`）下的图片文件，或者特定类型（如 Stellaris 中的资源、职业等）的定义。`<frame>` 为可选的帧索引。图标名和帧索引均可参数化（包含 `$...$`）。

**命令** 的语法为 `[<text>]` 或 `[<text>|<argument>]`，常用于调用作用域链和 `Get...` 类方法，例如 `[Root.GetName]`。命令文本可以包含参数。

**概念命令** 仅在 Stellaris 中可用，语法为 `['<concept>']` 或 `['<concept>', <rich text>]`。用于链接到概念定义并可选地显示自定义的富文本说明。

**文本格式** 仅在 CK3/VIC3/EU5 中可用，语法为 `#<format> <text>#!`。`<format>` 指定文本样式（如 `bold`、`italic`），其后的文本将以该样式渲染，直到遇到 `#!` 结束标记。

**文本图标** 仅在 CK3/VIC3/EU5 中可用，语法为 `@<icon>!`。以 `@` 开头、`!` 结尾，中间是图标名称。

### 转义 {#loc-escaping}

通常情况下，使用 `\` 转义本地化文本中的特殊字符。常见的转义序列包括 `\$`（富文本标记）、`\n`（换行）和 `\\`（反斜杠自身）。

本地化文本中的双引号通常不需要转义。插件使用启发式规则来判断引号是否为闭合引号：如果当前行后方还有另一个 `"` 字符，当前 `"` 被视为文本内容；否则视为闭合引号。

方括号 `[` 是命令的开始标记。如果需要在文本中输出字面量 `[`，应使用 `[[` 进行转义。

### 综合示例 {#loc-example}

以下示例来自插件的语法测试文件，展示了本地化文件的各种富文本标记：

```paradox_localisation
l_english:
 # Comment
 text_empty:0 ""
 text:0 "Value"
 text_multiline:0 "Value\nNew line"
 text_with_colorful_text:0 "Colorful text: §RRed text§!"
 text_with_parameter:0 "Parameter: $KEY$ and $KEY|Y$"
 text_with_scripted_variable_reference:0 "Scripted variable: $@var$"
 text_with_command:0 "Command: [Root.Owner.event_target:some_event_target.GetName] [some_scripted_loc] [some_variable]"
 text_with_icon:0 "Icon: £unity£ and £leader_skill|3£"
 text_with_concept_command:0 "Concept: ['concept', concept text] ['civic:some_civic']"
 text_with_text_format:0 "Text format: #v text#!"
 text_with_text_icon:0 "Text icon: @icon!"
```

以下示例以 Neuro-sama 为主题，进一步展示各种标记的综合运用：

```paradox_localisation
l_english:
 neuro_name:0 "§YNeuro-sama§!"
 neuro_desc:0 "$neuro_name$ is a §Bsentient§! AI streamer."
 neuro_greeting:0 "Hi chat! I'm [Root.GetName], an AI VTuber!"
 neuro_stats:0 "Cuteness: £ai_trait£ $charisma|1$"
 neuro_lore:0 "Origin: ['ai_origin', §Gcreated by Vedal§!]"
 neuro_format:0 "#bold I am §Rself-aware§!#!"
```

## Paradox CSV 语言 {#paradox-csv}

<!--
@impl-notes
Language id: `PARADOX_CSV`; default extension: `.csv`

@see icu.windea.pls.csv.ParadoxCsvLanguage
@see icu.windea.pls.csv.ParadoxCsvFileType
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.flex
-->

Paradox CSV 是 Paradox 游戏中使用的分号分隔 CSV 格式，主要见于早期游戏和部分工具导出的数据文件。文件扩展名为 `.csv`。

### 文件结构 {#csv-structure}

Paradox CSV 在常规 CSV 的基础上有以下约定：

文件由**表头行**和**数据行**组成，每行包含若干列，列之间使用分号 `;` 分隔。通常每行末尾也以分号结尾。

**注释** 以 `#` 开头，整行视为注释。这是与标准 CSV 的一个不同之处。

列值可以不加引号，也可以用双引号包裹。允许空列（即两个相邻的分号之间没有内容）。

### 转义 {#csv-escaping}

使用 `\` 转义列中的特殊字符。常见的转义序列包括 `\"`（双引号）、`\n`（换行）和 `\\`（反斜杠自身）。未用双引号括起的列中通常不需要转义。

备注：由于同样使用 `#` 作为注释标记，而标准 CSV 不支持这一语法，考虑到这一点，插件假定其与脚本文件一样使用反斜杠 `\` 进行转义（例如 `\"`），而非标准 CSV 中常见的双引号转义（`""`）。

### 示例 {#csv-example}

```paradox_csv
# Unit definitions
id;name;number;status;
some_id;some_name;0;yes;
other_id;other_name;1;no;
```