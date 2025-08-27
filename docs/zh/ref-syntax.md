# 附录：语法参考

待编写。

<!-- @ai-generated DS
本节提供关于语法的权威参考信息，采用简洁的技术文档风格。
目标是成为开发过程中的速查手册，方便快速定位关键信息。
-->

## CWT 文件

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

待编写。

<!-- @ai-generated DS
描述：脚本语法权威参考
编写思路：
- 语法结构：块/键值对/列表的规范格式
- 特殊语法：内联脚本/表达式的高级用法
- 注释规范：支持的单行和多行注释
- 示例：展示符合规范的事件脚本结构
-->

## Paradox 本地化文件

待编写。

<!-- @ai-generated DS
描述：.yml本地化文件完整规范
编写思路：
- 文件结构：键值对组织标准
- 富文本标记：§符号命令全集
- 多语言支持：locale代码标准
- 示例：展示包含条件表达式的复杂本地化条目
-->

## Paradox CSV 文件

Paradox CSV 文件在常规 CSV 文件的基础上，使用 `;` 作为列分隔符，且允许以 `#` 开始的单行注释。