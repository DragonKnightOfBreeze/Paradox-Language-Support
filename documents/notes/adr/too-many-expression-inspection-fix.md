# 修复来自 `TooManyExpressionInspection` 的误报

> 所属模块：`后端 > 语言功能支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.3+
> - 日期：2026-09-21+
> - 状态：告一段落（待补充测试）

计划：
- [x] 对于注入的上下文，总是应当在顶层跳过此项检查

任务（2026-09-21+）：
- [ ] 补充测试 `icu.windea.pls.lang.inspections.script.expression.TooManyExpressionInspectionTest`
