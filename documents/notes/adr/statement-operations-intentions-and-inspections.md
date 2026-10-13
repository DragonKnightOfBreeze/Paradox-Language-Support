# 语句操作服务 & 相关的意向动作和代码检查

> 所属模块：`后端 > 语言功能支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v2.1.10
> - 日期：2026-06-16
> - 状态：已实施

说明：
- 提供数种语句操作服务（statementManipulationService），以及相关的意向动作和代码检查，以便快速乃至批量地切换语句形式
- conditional - propertyForm (`PARAM = $PARAM|no$`) VS blockForm (`[[PARAM] PARAM = $PARAM$ ]`)
- scopeCall - normalForm (`owner = {...}`) VS safeForm (use `?=` or `? =`)
- scopeCall - chainedForm (`root.owner = { ... }`) VS nestedForm (`root = { owner = ... }`)

相关代码位置：
- conditional
  - [x] `icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService`
  - [x] `icu.windea.pls.lang.intentions.script.ConditionalStatementToPropertyFormIntention`
  - [x] `icu.windea.pls.lang.intentions.script.ConditionalStatementToBlockFormIntention`
  - [x] `icu.windea.pls.lang.inspections.script.statement.ConditionalStatementToPropertyFormInspection` - 默认级别：`INFORMATION`
  - [x] `icu.windea.pls.lang.inspections.script.statement.ConditionalStatementToBlockFormInspection` - 默认级别：`INFORMATION`
- scopeCall
  - [x] `icu.windea.pls.lang.manipulation.ParadoxScopeCallStatementManipulationService`
  - [x] `icu.windea.pls.lang.intentions.script.ScopeCallStatementToNormalFormIntention`
  - [x] `icu.windea.pls.lang.intentions.script.ScopeCallStatementToSafeFormIntention`
  - [x] `icu.windea.pls.lang.inspections.script.statement.ScopeCallStatementToNormalFormInspection` - 默认级别：`INFORMATION`
  - [x] `icu.windea.pls.lang.inspections.script.statement.ScopeCallStatementToSafeFormInspection` - 默认级别：`INFORMATION`
  - [x] `icu.windea.pls.lang.intentions.script.ScopeCallStatementToChainedFormIntention`
  - [x] `icu.windea.pls.lang.intentions.script.ScopeCallStatementToNestedFormIntention`
  - [x] `icu.windea.pls.lang.inspections.script.statement.ScopeCallStatementToChainedFormInspection` - 默认级别：`INFORMATION`
  - [x] `icu.windea.pls.lang.inspections.script.statement.ScopeCallStatementToNestedFormInspection` - 默认级别：`INFORMATION`
