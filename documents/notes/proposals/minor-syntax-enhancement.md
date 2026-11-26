# 次要语法与语言基础设施完善

> - 模块：后端（语法支持）
> - 版本：v3.1.x+
> - 状态：待规划（拆分自原 `roadmap.md`）
> - 标签：`语法完善` / `代码检查` / `颜色词元` / `条件化块`

## 概述

次要的语法完善与语言基础设施增强，涉及脚本颜色词元切分、条件化块、通配参数兼容、语法错误抑制以及相关代码检查调整。

## 候选任务

- [ ] P2 完善脚本文件的 lexer：将 *颜色/颜色字段/color/color field*（如 `rgb { 255 255 255 }`）切分为更具体的一组词元（参见 [adr/script-lexer-refactor.md](../adr/script-lexer-refactor.md)）
- [ ] P3 `[hoi4]` 初步兼容通配参数（如 `[?PARAM]`），包括语法、代码高亮、索引、参数化判断等
  - 引入 `ParadoxScriptWildcardParameter`，并实现 `ParadoxScriptConditionParameter`。
  - 考虑到其似乎没有对应的传入参数，视为 `conditionParameter` 而非常规的 `parameter`。
  - 参见 [proposals/script-syntax-advanced-interpolations.md](script-syntax-advanced-interpolations.md)（需要创建对应的 Issue）。
- [ ] P3 分离出代码高亮的配置项分组（语义高亮、抑制插值内容/注入内容的首尾语法错误等）
  - 可以考虑提供配置项，以在 `conditionalBlockBody` 的开始和结尾抑制语法错误。
  - 可以考虑提供配置项，以在 `argument`（如内联脚本的传入参数）的开始和结尾抑制语法错误。
- [ ] P4 对于脚本文件，考虑从 `IncorrectSyntaxInspection` 分离出 `UnsupportedOperatorInspection`，以强调这是“不支持的语法”，且通常不应禁用这项检查
- [ ] P4 通过 `IncorrectExpressionInspection` 报告字符串/块形式的颜色字段的参数不正确（越界/缺失/过多）
- [ ] P4 通过 `IncorrectSyntaxInspection` 报告颜色块的参数不正确（越界/缺失/过多）
- [ ] P4 通过 `IncorrectSyntaxInspection` 报告内容为空的条件化块（如 `[[PARAM]]`）
- [ ] P4 完善颜色块的语法支持（使用特殊的词元类型）
- [ ] P4 对于脚本文件，考虑统一 `colorfulText` / `textIcon` / `textFormat` 的风格（PSI 命名 / 词元类型命名 / AST 结构等），可能的方案是将 `colorfulText` 改为 `textColor` + `textColorText`

## 关联提案

- [脚本高级插值语法](script-syntax-advanced-interpolations.md)
- [本地化高级富文本语法](localisation-syntax-advanced-rich-text.md)
