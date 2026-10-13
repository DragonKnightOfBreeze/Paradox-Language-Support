# 支持并集规则

> 所属模块：`后端 > 规则系统`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.2.0
> - 日期：2026-07-01
> - 状态：已实施

说明：
- 支持 *并集规则（unionConfig）*，类似枚举规则（enumConfig），但是其中预定义的是任意数据表达式。
- 匹配时可能涉及多个不同类型的匹配结果（`ParadoxMatchResult`），目前的解决方案是检查准确的匹配结果（调用 `get()` 方法）。

相关代码位置：
- [x] `icu.windea.pls.config.config.delegated.CwtUnionConfig`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxScriptUnionValueExpressionSupport`
- [x] `icu.windea.pls.ep.resolve.expression.ParadoxCsvUnionValueExpressionSupport`
