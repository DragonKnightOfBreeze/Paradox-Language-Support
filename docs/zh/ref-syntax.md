# 附录：语法参考



## 定位与愿景

本章节旨在成为 Paradox 模组语法的权威指南与速查参考。我们有信心以清晰、准确、可操作为原则，保持与实际用法及工具链表现的一致，并将随着生态与版本演进持续打磨与完善。

## 总览

本节涵盖以下语法（文档示例的代码块已配置 Prism 高亮）：

* __CWT config file__（`*.cwt`）
  - 注释：`#`、`##`（选项注释）、`###`（文档注释）
  - 代码块语言 ID：`cwt`
* __Paradox Script__（脚本文件）
  - 注释：`#`
  - 代码块语言 ID：`paradox_script`
* __Paradox Localisation__（本地化 `.yml`）
  - 注释：`#`
  - 代码块语言 ID：`paradox_localisation`
* __Paradox CSV__（分号分隔 CSV）
  - 注释：`#`
  - 代码块语言 ID：`paradox_csv`

## CWT config file

<!-- AI: maps to icu.windea.pls.cwt.CwtLanguage; icu.windea.pls.cwt.CwtFileType -->
<!-- AI: impl-notes
Language id: CWT; default extension: .cwt. This section describes surface syntax and lexer tokens; details follow the plugin grammar.
-->

本文档说明 CWT config file 的基本语法，用于编写与组织 CWT 配置内容。其外观与 Paradox 脚本类似，但有额外的“选项注释”等扩展能力。

基础概念：

* __成员类型__：属性（property）、值（value）、块（block）。
* __分隔符__：`=`、`==` 表示等于；`!=`、`<>` 表示不等于。
* __空白/换行__：空白与换行用于分隔标记；空行允许存在。

属性（property）：

* 结构：`<key> <sep> <value>`，其中 `<sep>` 为 `=`/`==`/`!=`/`<>`。
* 键（key）：未加引号或使用双引号；未加引号的键不应包含 `# = { }` 与空白。
* 例：

```cwt
cost = int
acceleration = float
class = enum[shipsize_class]
```

值（value）：

* __布尔__：`yes`/`no`。
* __整数__：`10`、`-5`（允许前导负号与前导零）。
* __浮点__：`1.0`、`-0.5`。
* __字符串__：
  - 未加引号：不包含 `# = { }` 与空白。
  - 双引号字符串：支持转义序列，如 `\"`。
* __块__：见下节。

块（block）：

* 使用花括号包裹：`{ ... }`。
* 内容可混合：属性、值与选项注释（见下节）；允许行内与行间注释。
* 例：

```cwt
ship_size = {
  ## cardinality = 0..1
  ### The base cost of this ship_size
  cost = int

  modifier = {
    alias_name[modifier] = alias_match_left[modifier]
  }
}
```

注释与文档：

* __行注释__：以 `#` 开始，整行视为注释。
* __选项注释__：以 `##` 开始；用于为紧随其后的成员（属性/块/值）声明元信息。
  - 语法与属性一致：`<optionKey> <sep> <optionValue>`。
  - 常见示例：`## cardinality = 0..1`，`## severity = warning`，`## push_scope = country`。
* __文档注释__：以 `###` 开始；用于为成员提供说明文本（在补全/文档中展示）。

语法要点与示例：

```cwt
# 普通注释
## option_key = option_value    # 选项注释（作用于下一成员）
### Documentation text          # 文档注释

types = {
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
      Name = "$"
      ## required
      Desc = "$_desc"
    }
    images = {
      ## primary
      icon = "#icon"
    }
  }
}
```

注意事项：

* 选项注释只影响其后紧随的一个成员（或该成员体内的内容，视实现而定）。
* 未加引号的键与字符串尽量避免包含空白与保留符号；复杂内容建议使用双引号。
* 等号与不等号均可用于选项与属性；请根据语义选择。

## Paradox 脚本文件

本文档说明 Paradox 脚本文件（Script）的表面语法，力求与实际用法及工具链行为保持一致。
<!-- impl: src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf, src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex -->

基础概念：

* __成员类型__：属性（property）、值（value）、块（block）。
* __分隔符（比较/赋值）__：`=`、`!=`、`<`、`>`、`<=`、`>=`、`?=`。
* __注释__：以 `#` 开始的单行注释。

属性（property）：

* 结构：`<key> <sep> <value>`，`<sep>` 取上述分隔符之一。
* 键（key）：未加引号或双引号。键可参数化（见下节）。
* 例：`enabled = yes`、`level >= 2`、`size ?= @var`。

值（value）：

* __布尔__：`yes`/`no`。
* __整数/浮点__：如 `10`、`-5`、`1.25`。
* __字符串__：未加引号或双引号；双引号字符串中可嵌入参数、内联参数条件。
* __颜色__：`rgb{...}`、`hsv{...}`、`hsv360{...}`。
* __块__：`{ ... }`，内部可包含注释、属性、值与脚本变量等。
* __脚本变量引用__：`@name`。
* __内联数学表达式__：`@[ <expr> ]`，支持 `+ - * / %`、一元正负号、绝对值 `|x|` 与括号。

脚本变量（scripted variable）：

* 定义：`@<name> = <value>`，其中 `<value>` 可为布尔/整数/浮点/字符串/内联数学表达式。
* 引用：`@<name>`。

参数（parameter）：

* 语法：`$name$` 或 `$name|argument$`。
* 位置：可用于脚本变量名、脚本变量引用名、键、字符串、内联数学表达式。

参数条件（parameter condition）：

* 语法（外层）：`[ [!]<parameter> <members...> ]`。
* 说明：用于定义声明上下文中的条件化成员（仅在特定定义中有效）。

内联参数条件（inline parameter condition）：

* 用于字符串内部的条件片段，形如：`"a[[cond]b]c"`。

示例：

```paradox_script
# comment
@my_var = 42

effect = {
  enabled = yes
  level >= 2
  size ?= @my_var

  name = "Hello $who|leader$!"

  modifier = {
    add = 1
  }

  result = @[1 + 2 * 3]
}
```

注意事项：

> [!warning]
> 参数、参数条件与内联数学等属于进阶语法，通常仅在特定定义（如脚本化效果/触发）中生效或被引擎评估。

* 未加引号的键与字符串请避免保留字符；复杂文本使用双引号。

## Paradox 本地化文件

本文档说明 Paradox 本地化（Localisation）文件的表面语法，力求与实际用法及工具链行为保持一致。

文件结构：

* 可选的 __语言标识__ 行：如 `l_english:`、`l_simp_chinese:`（可以有多个，以兼容 `localisation/languages.yml`）。
* 多个 __键值对__：`<key>:<number?> "<text>"`，其中 `<number>` 为可选的内部追踪号。
* 注释：以 `#` 开始的单行注释。

文本（`"<text>"`）内可用的标记：

* __颜色__：`§X ... §!`（`X` 为单字符 ID）。
* __参数__：`$name$` 或 `$name|argument$`。`name` 可为本地化键、命令，或脚本变量引用（如 `$@var$` 形式在解析层面等价）。
* __方括号命令__：`[text|argument]`，其中 `text` 可参数化；常用于 `Get...`/上下文调用。
* __图标__：`£icon|frame£`（`|frame` 可省略），在渲染时嵌入 GFX 图标。
* __概念命令（Stellaris）__：`['concept' <rich text>]`，用于链接概念与显示说明文本。
* __文本格式（CK3/Vic3）__：`#format ... #!`，用于样式化文本块；以及 __文本图标__：`@icon!`（以 `@` 开始、以 `!` 结尾）。

示例：

```paradox_localisation
l_english:
  my_key:0 "Hello §Y$target$§! [GetPlayerName] £alloys£"
  concept_key:0 "['pop_growth', §G+10%§!]"
```

注意事项：

* 冒号后的数字（追踪号）可以省略。
* 文本中的双引号在多数情况下不需要转义，但建议避免不成对的引号。

> [!warning]
> `#format`、`@icon!` 等为特定游戏支持的进阶标记；仅在对应游戏中有效。`['concept' ...]` 仅 Stellaris 支持。

## Paradox CSV 文件

Paradox CSV 文件在常规 CSV 的基础上，约定：

* __列分隔符__：分号 `;`（常见于早期/工具导出的 CSV）。
* __注释__：以 `#` 开始的整行注释。
* __字符串__：使用双引号包裹；内部双引号使用 `""` 表示（标准 CSV 规则）。

示例：

```paradox_csv
# comment
key;col1;col2
id1;"text with ; semicolon";42
id2;plain;"quoted"
```