# [CK3/VIC3/EU5] 完善对定值引用表达式（defineReferenceExpression）和数组定值表达式（arrayDefineReferenceExpression）的支持

> - 模块：后端 > 语言构造支持
> - 版本：v2.1.10
> - 日期：2026-06-19
> - 状态：已实施

计划：
- 示例：`Namespace|Name` - 定值引用表达式。引用字面量（通常是数字、颜色或日期）。
- 示例：`Namespace|Name|0` - 数组定值引用表达式。引用字面量（通常是数字）。索引从0开始。
- 对应的规则表达式（数据表达式）：`$define_reference` 和 `$array_define_reference`
- 需要补充规范化的链接规则 `define` 和 `array_define`（`type = value`）。
- 需要补充或重构复杂表达式实现、代码补全、代码检查。
- 需要考虑补充评估器、检查器（基于评估器）、内嵌提示（基于评估器）、意向动作（基于评估器）。

相关代码位置：
- [x] `icu.windea.pls.config.CwtDataTypes`
- [x] `icu.windea.pls.ep.config.configExpression.CwtCoreDataExpressionSupport`
- [x] `icu.windea.pls.ep.match.expression.ParadoxCoreScriptExpressionMatcher`
- [x] `icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager`
- [x] `icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression`
- [x] `icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxScriptDefineReferenceExpressionSupport`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxScriptArrayDefineReferenceExpressionSupport`
- [x] `icu.windea.pls.lang.inspections.script.complexExpression.IncorrectDefineReferenceExpressionInspection`
- [x] `icu.windea.pls.lang.inspections.script.complexExpression.IncorrectArrayDefineReferenceExpressionInspection`
- [x] `icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpressionTest`
- [x] `icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpressionTest`

相关代码位置（评估器相关）：
- [x] `icu.windea.pls.lang.util.evaluators.ParadoxDefineReferenceExpressionEvaluator`
- [x] `icu.windea.pls.lang.util.evaluators.ParadoxArrayDefineReferenceExpressionEvaluator`
- [x] `icu.windea.pls.lang.codeInsight.hints.script.ParadoxDefineReferenceResultHintsProvider`
- [x] `icu.windea.pls.lang.codeInsight.hints.script.ParadoxArrayDefineReferenceResultHintsProvider`
- [x] `icu.windea.pls.lang.intentions.script.ReplaceDefineReferenceWithEvaluationResultIntention`
- [x] `icu.windea.pls.lang.intentions.script.ReplaceArrayDefineReferenceWithEvaluationResultIntention`
