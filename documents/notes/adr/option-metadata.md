# 重命名：`optionData` -> `optionMetadata`

> - 模块：后端 > 规则系统
> - 版本：v3.0.1
> - 日期：2026-07-266
> - 状态：已实施

说明：
- 将 `CwtOptionMetadataHolder` 重命名为 `CwtOptionMetadata`，将 `CwtMemberConfig.optionData` 重命名为 `CwtMemberConfig.optionMetadata`。
- 同步更改相关的代码、文档和注释。
- 主要目的是为了与 `CwtDataExpression` 的元数据（`CwtDataExpressionMetadata`）对齐。两者都是通过属性来访问元数据的，自身接口并不会直接实现元数据的接口，而实现类则会按需实现。
