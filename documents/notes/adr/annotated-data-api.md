# 引入 `ParadoxAnnotatedData`

> 所属模块：`后端 > 其他语义相关`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.2
> - 日期：2026-08-25
> - 状态：已实施

提供“注解数据”的API，以便获取关键的语法信息和语义信息。

说明：
- 可用于提取、嵌入、渲染、验证一些关键语法信息和语义信息。
- 可用于实现 annotatedRenderer，而这又可用于快照测试。

说明（代码位置）：
- 迁移自 `icu.windea.pls.lang.codeInsight.annotated`。
- 模型代码位于 `icu.windea.pls.model.data.annotated` 中。
- 其他相关代码位于 `icu.windea.pls.lang.data.annotated` 中。

说明（RWA 相关）：
- 仅在通过 dataSource 查询数据时需要 readAction，但是，目不前考虑在必要时自动加上（通过 `runSmartReadAction`）。

相关代码位置：
- [x] `icu.windea.pls.model.data.annotated.ParadoxAnnotatedData`
- [x] `icu.windea.pls.model.data.annotated.ParadoxAnnotatedLevel`
- [x] `icu.windea.pls.model.lang.annotated.ParadoxAnnotatedDataFactory`
- [x] `icu.windea.pls.model.data.annotated.ParadoxAnnotatedDataImpls.kt` - 一组预定义的模型实现
