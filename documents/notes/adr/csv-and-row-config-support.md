# 完善对 CSV 文件与 rowConfig 的支持

> - 模块：后端 > 语言构造支持
> - 版本：v2.2.0
> - 日期：2026-07-05
> - 状态：已实施
> - 相关问题：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/314

任务：
- [x] 支持在 CSV 文件中声明复杂枚举值（通过在列规则上使用 `## declare_complex_enum = x`）。
  - 需要同步调整复杂枚举值索引的逻辑。
- [ ] ~~支持在 CSV 文件中声明定义（通过在列规则上使用 `## declare_type = x`）。~~
  - 将一整行视为定义声明，将某个列视为指定了定义名的列。
  - 需要架构上的大幅重构，并且涉及一些领域设计上的问题，因此暂不考虑。
- [x] 为行规则添加字段 type，可选值包括 key/index，默认 key。表示是按键还是按索引来匹配行中的每一列。
  - 表头列的列名（columnKey/columnName）可以重复，因此在解析规则数据和语义解析时，可能需要基于列索引，而非列名。
- [x] 为行规则添加字段 skip_last_row 和 skip_last_column，值为布尔值。表示匹配与解析时是否忽略最后一列/最后一行。
  - 表头列的最后一列，以及文件中的最后一行，可能需要在语义解析时忽略，因此需要提供对应的规则字段。
- [ ] ~~考虑将从（脚本/CSV）表达式解析为复杂枚举值的表达式引用（即PSI引用）的逻辑，提取为需要置顶的 expressionSupport 的EP实现。~~
  - 后续也可以考虑以类似方式处理各种特殊标签。
    - 目前不采用，对于特殊标签和复杂枚举值声明，使用需要提前处理、可能不依赖匹配的规则的特殊硬编码逻辑。
- [x] 调整复杂枚举值索引的逻辑，以兼容在 CSV 文件中的复杂枚举值声明。
- [x] 调整合并索引的逻辑，以兼容在 CSV 文件中的动态值引用。
- [x] 调整 CSV 文件的相关代码检查的代码。
- [x] 补充测试（CSV 文件的相关代码检查）。
- [x] 检查各个游戏的 CSV 文件，完善和补充对应的 rowConfig。
- [x] 让 `CwtComplexEnumConfig` 不仅可以来自显式的规则（`complex_enum[x] = {...}`），也可以来自注有 `## declare_complex_enum = x` 的列规则

相关代码位置：
- [x] `icu.windea.pls.config.config.delegated.CwtRowConfig`
- [x] ~~`icu.windea.pls.lang.resolve.ParadoxCsvService`~~ -> `icu.windea.pls.lang.resolve.ParadoxConfigService`
- [x] ~~`icu.windea.pls.lang.util.ParadoxCsvManager`~~ -> `icu.windea.pls.lang.util.ParadoxConfigManager`

调整后的代码检查：
- [x] `UnresolvedExpressionInspection` - 某个列存在对应的列规则，但是无法匹配（因此视为无法解析的表达式）。
- [x] `IncorrectExpressionInspection` - 某个列存在对应的列规则，但是检查发现不正确（因此视为不正确的表达式）。
- [x] `IncorrectColumnNameInspection` - 表头行的列名不正确（要求存在对应的行规则，按列索引和列名进行匹配）。
- [x] `IncorrectColumnSizeInspection` - 某行的列的数量与表头行的列的数量不一致（要求存在对应的行规则）。
