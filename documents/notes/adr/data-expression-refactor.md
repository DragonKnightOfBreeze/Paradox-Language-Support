# 重构 `CwtDataExpression` （元数据不应公开可变，以及其他）

> - 模块：后端 > 规则系统
> - 版本：v3.0.1
> - 日期：2026-07-25
> - 状态：已实施（部分）

说明：
- `CwtDataExpression` 自身的所有属性都应当是不可变的（包括元数据相关的属性），仅在创建和解析数据表达式时，允许以可变形式（但最好是不可变形式）构建数据表达式。
- `CwtDataExpression` 自身没有必要实现 `UserDataHolder`（尤其是考虑到可能不携带任何元数据）。作为替代，让 `CwtDataExpressionMetadata` 实现 `UserDataHolder`。
- 这里的“元数据/metadata”实际上类似“负载/payload”，可以没有，可以是多种数据类型，也可以是对象/映射形式（多个元数据的组合）。

附加：
- [x] 常量类型（`CwtDataTypes.Constant`）和模板类型（`CwtDataTypes.Template`）的数据表达式，实际上并不需要元数据（`value`），直接从 `expressionString` 获取原始的表达式字符串即可。
- [x] 将属性 `val isKey: Boolean` 改为 `val role: CwtDataExpressionRole`（为了对齐 `ParadoxExpression` 的属性 `val role: ParadoxExpressionRole`，以及更好地区分键/值/其他），重构相关API。

相关代码位置：
- [x] `icu.windea.pls.config.configExpression.CwtDataExpression`
- [x] `icu.windea.pls.config.configExpression.CwtDataExpressionMetadata`
