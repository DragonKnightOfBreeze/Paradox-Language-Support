# [CK3/VIC3/EU5] 完善对标签集表达式（tagsExpression）的支持

> - 模块：后端 > 语言构造支持
> - 版本：v2.1.10
> - 日期：2026-06-19
> - 状态：已实施
> - 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/163

计划：
- 示例：`tag1,tag2` - 写访问。`tag1` 和 `tag2` 视为标识符（作为动态值节点）。
- 示例：`tag1,not(tag2)` - 读访问，条件变体。`not` 视为关键字（作为关键字节点）。
- 对应的规则表达式（数据表达式）：`$tags[{name}]` 和 `$tags_condition[{name}]`（条件变体）。
- 匹配时兼容空字符串。
- 忽略表达式中的多余空白。
- 需要用双引号括起，否则给出警告。

相关代码位置：
- [x] `icu.windea.pls.config.CwtDataTypes`
- [x] `icu.windea.pls.ep.config.configExpression.CwtCoreDataExpressionSupport`
- [x] `icu.windea.pls.ep.match.expression.ParadoxCoreScriptExpressionMatcher`
- [x] `icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager`
- [x] `icu.windea.pls.lang.resolve.complexExpression.ParadoxTagsExpression`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxScriptTagsExpressionSupport`
- [x] `icu.windea.pls.lang.inspections.script.complexExpression.IncorrectTagsExpressionInspection`
- [x] `icu.windea.pls.lang.resolve.complexExpression.ParadoxTagsExpressionTest`
