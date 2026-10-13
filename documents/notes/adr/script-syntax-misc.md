# 未整理

> 所属模块：`后端（语法支持） > 脚本文件`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

记录：
- [ ] 可以考虑提供配置项，以在 conditionalBlockBody 的开始和结尾抑制语法错误。
- [ ] 可以考虑提供配置项，以在 argument （如，内联脚本的传入参数）的开始和结尾抑制语法错误。
- [ ] （搁置，比较复杂）条件化块也能在内联数学中使用（如 `@\[ 2 + [[foo] 3 ][[!foo] 7 ] ]`）。需要更改相关的 lexer、parser、evaluator。
  - 需要额外引入 `ParadoxScriptInlineMathConditionalBlock`，并实现 `ParadoxScriptInlineMathExpression`。
- [ ] （搁置，不是很必要）插值语法应当出现在块（`{...}`）中。也就是说，插值语法不应适用于文件顶层的声明。
  - 可以考虑提供相关的代码检查。然而，目前已经存在 `UnsupportedParameterInspection`。
- [ ] （搁置，这仍然会破坏上下文）可以考虑为 conditionalBlockBody（`[[PARAM]text]` 中的 `text`）适用语言注入（识别为脚本文件，并忽略首尾的语法错误）。
