# 附录：语法参考

<!-- TODO 人工改进与润色 -->

## 定位与愿景 {#vision}

本文档旨在成为编写 CWT 规则文件以及 Paradox 模组时使用的领域特定语言的语法的权威指南与速查参考。
我们有信心以清晰、准确、可操作为原则，保持与实际用法及工具链表现的一致，并将随着生态与版本演进持续打磨与完善。

## 总览 {#overview}

本文档涵盖对以下语言的语法的说明：

- **CWT**（`*.cwt`）
  - 注释：`#`、`##`（选项注释）、`###`（文档注释）
  - 代码块语言 ID：`cwt`
- **Paradox Script**（脚本）
  - 注释：`#`
  - 代码块语言 ID：`paradox_script`
- **Paradox Localisation**（本地化 `.yml`）
  - 注释：`#`
  - 代码块语言 ID：`paradox_localisation`
- **Paradox CSV**（分号分隔 CSV，`*.csv`）
  - 注释：`#`
  - 代码块语言 ID：`paradox_csv`

## CWT 语言 {#cwt}

<!--
@impl-notes
Language id: `CWT`; default extension: `.cwt`
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.cwt.CwtLanguage
@see icu.windea.pls.cwt.CwtFileType
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.flex
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.OptionDocument.flex
-->

本章节说明 CWT 语言的语法。

CWT 语言是一种领域特定语言，用于编写 CWT 规则。
CWT 语言的文件扩展名是 `.cwt`，其语法与 Paradox 脚本语言类似，但额外支持 *选项注释* 与 *文档注释*。

CWT 规则用于为 CWT 语言自身以及各种 Paradox 语言提供高级语言功能，包括但不限于代码高亮、代码检查、代码补全等。

基础概念：

- **成员类型**：属性（property）、值（value）、块（block）。
- **分隔符**：`=`、`==` 表示等于；`!=`、`<>` 表示不等于。
- **空白/换行**：空白与换行用于分隔标记；空行允许存在。

属性（property）：

- 结构：`<key> <sep> <value>`，其中 `<sep>` 为 `=`/`==`/`!=`/`<>`。
- 键（key）：未加引号或使用双引号；未加引号的键不应包含 `# = { }` 与空白。
- 例：

```cwt
playable = yes
cost = 10
acceleration = 20.0
class = some_shipsize_class
"text" = "line\nnext line"
```

值（value）：

- **布尔**：`yes`/`no`。
- **整数**：`10`、`-5`（允许前导负号与前导零）。
- **浮点**：`1.0`、`-0.5`。
- **字符串**：
  - 未加引号：不包含 `# = { }` 与空白。
  - 双引号字符串：支持转义序列，如 `\"`。
- **块**：见下节。

块（block）：

- 使用花括号包裹：`{ ... }`。
- 内容可混合：属性、值与选项注释（见下节）；允许行内与行间注释。
- 例：

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

注释与文档：

- **行注释**：以 `#` 开始，整行视为注释。
- **选项注释**：以 `##` 开始；用于为紧随其后的成员（属性/块/值）声明元信息。
  - 语法与属性一致：`<optionKey> <sep> <optionValue>`。
  - 常见示例：`## cardinality = 0..1`，`## severity = warning`，`## push_scope = country`。
- **文档注释**：以 `###` 开始；用于为成员提供说明文本（在补全/文档中展示）。

语法要点与示例：

```cwt
# 普通注释
types = {
    ### Documentation text
    ## option_key = option_value
    type[army] = {
        path = "game/common/armies"
        subtype[buildable] = {
            potential = {
                ## cardinality = 0..0
                always = no
            }
        }
        localisation = {
            ## required
            name = "$"
            ## required
            desc = "$_desc"
        }
        images = {
            ## primary
            icon = "#icon"
        }
    }
}
```

注意事项：

- 选项注释只影响其后紧随的一个成员（或该成员体内的内容，视实现而定）。
- 未加引号的键与字符串尽量避免包含空白与保留符号；复杂内容建议使用双引号。
- 等号与不等号均可用于选项与属性；请根据语义选择。

## Paradox 脚本语言 {#paradox-script}

<!--
@impl-notes
Language id: `PARADOX_SCRIPT`; extensions: `.txt`, `.gfx`, `.gui`, etc.
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.script.ParadoxScriptLanguage
@see icu.windea.pls.script.ParadoxScriptFileType
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex
-->

本章节说明 Paradox 脚本（Script）语言的基本语法。

Paradox 脚本语言是一种领域特定语言，用于编写游戏脚本。
其文件扩展名通常是 `.txt`。某些特定位置的文件会使用其他扩展名，例如 `.gfx`、`.gui`。

基础概念：

- **成员类型**：属性（property）、值（value）、块（block）。
- **分隔符（比较/赋值）**：`=`、`!=`、`<`、`>`、`<=`、`>=`、`?=`。
- **注释**：以 `#` 开始的单行注释。

属性（property）：

- 结构：`<key> <sep> <value>`，`<sep>` 取上述分隔符之一。
- 键（key）：未加引号或双引号。键可参数化（见下节）。
- 例：`enabled = yes`、`level >= 2`、`size ?= @var`。

值（value）：

- **布尔**：`yes`/`no`。
- **整数/浮点**：如 `10`、`-5`、`1.25`。
- **字符串**：未加引号或双引号；双引号字符串中可嵌入参数、内联参数条件。
- **颜色**：`rgb {...}`、`hsv {...}`、`hsv360 {...}`。
- **块**：`{ ... }`，内部可包含注释、属性、值与脚本变量等。
- **脚本变量引用**：`@name`。
- **内联数学表达式**：`@[ <expr> ]`，支持 `+ - * / %`、一元正负号、绝对值 `|x|` 与括号。

脚本变量（scripted variable）：

- 声明：`@<name> = <value>`，其中 `<value>` 可为布尔/整数/浮点/字符串/内联数学表达式。
- 引用：`@<name>`。

参数（parameter）：

- 语法：`$name$` 或 `$name|argument$`。
- 位置：可用于脚本变量名、脚本变量引用名、键、字符串、内联数学表达式。

参数条件（parameter condition）：

- 语法（外层）：`[[<expression>] <members...> ]`。
- 说明：用于定义声明上下文中的条件化成员（仅在特定定义中有效）。

内联参数条件（inline parameter condition）：

- 用于字符串内部的条件片段，形如：`"a[[cond]b]c"`。

示例：

```paradox_script
# comment
@my_var = 42

effect = {
    enabled = yes
    level >= 2
    size ?= @my_var
    color = rgb { 34, 136, 255 }

    name = "Hello $who|leader$!"
    "tooltip" = "line\nnext line"

    modifier = {
        add = 1
        [[!PARAM]
            factor = 10
        ]
    }

    result = @[ 1 + 2 * $PARAM$ / var ]
}
```

> [!warning]
> 参数、参数条件与内联数学等属于进阶语法，通常仅在特定定义（如脚本化效果/触发）中生效或被引擎评估。

## Paradox 本地化语言 {#paradox-localisation}

<!--
@impl-notes
Language id: `PARADOX_LOCALISATION`; default extension: `.yml`
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.localisation.ParadoxLocalisationLanguage
@see icu.windea.pls.localisation.ParadoxLocalisationFileType
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex
-->

本章节说明 Paradox 本地化（Localisation）语言的基本语法。

Paradox 本地化语言是一种领域特定语言，用于为游戏提供可国际化的、可包含动态内容的富文本。
其文件扩展名是 `.yml`，但实际上并非合法的 YAML 文件，且需要使用 **UTF-8 WITH BOM** 作为文件编码。

> [!tip]
> PLS 可以检测本地化文件的文件编码问题，并且支持自动修正文件编码。

文件结构：

- 可选的 **语言标识** 行：如 `l_english:`、`l_simp_chinese:`（可以有多个，以兼容 `localisation/languages.yml`）。
- 多个 **键值对**：`<key>:<number?> "<text>"`，其中 `<number>` 为可选的内部追踪号。
- 注释：以 `#` 开始的单行注释。

文本（`"<text>"`）内可用的标记：

- **颜色**：`§X ... §!`（`X` 为单字符 ID）。
- **参数**：`$name$` 或 `$name|argument$`。`name` 可为本地化键、命令，或脚本变量引用（如 `$@var$` 形式在解析层面等价）。
- **命令**：`[text|argument]`，其中 `text` 可参数化；常用于 `Get...`/上下文调用。
- **图标**：`£icon|frame£`（`|frame` 可省略），在渲染时嵌入 GFX 图标。
- **概念命令（Stellaris）**：`['concept' <rich text>]`，用于链接概念与显示说明文本。
- **文本格式（CK3/Vic3）**：`#format ... #!`，用于样式化文本块；以及 **文本图标**：`@icon!`（以 `@` 开始、以 `!` 结尾）。

示例：

```paradox_localisation
l_english:
 # comment
 key:0 "line\nnext line"
 another_key:0 "§Y$target$§! produces £unity£"
 command_key:0 "Name: [Root.GetName]"
 concept_command_key:0 "['pop_growth', §G+10%§!]"
```

注意事项：

- 冒号后的数字（追踪号）可以省略。
- 文本中的双引号在多数情况下不需要转义，但建议避免不成对的引号。

> [!warning]
> `#format`、`@icon!` 等为特定游戏支持的进阶标记；仅在对应游戏中有效。`['concept' ...]` 仅 Stellaris 支持。

## Paradox CSV 语言 {#paradox-csv}

<!--
@impl-notes
Language id: `PARADOX_CSV`; default extension: `.csv`
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.csv.ParadoxCsvLanguage
@see icu.windea.pls.csv.ParadoxCsvFileType
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.flex
-->

本章节说明 Paradox CSV 语言的语法。
Paradox CSV 语言的文件扩展名是 `.csv`，其在常规 CSV 的基础上，约定：

- **列分隔符**：分号 `;`（常见于早期/工具导出的 CSV）。
- **注释**：以 `#` 开始的整行注释。
- **字符串**：使用双引号包裹；内部双引号使用 `""` 表示（标准 CSV 规则）。

示例：

```paradox_csv
# comment
key;col1;col2
id1;"text with ; semicolon";42
id2;plain;"line\nnext line"
```