# 关于作为通配策略的数据类型的相关重构

> 所属模块：`后端 > 规则系统`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：3.0.4
> - 日期：2026-10-03
> - 状态；已实施（告一段落）

基本数据类型：
- Any - 匹配任意表达式。作为最低优先级的通配匹配策略。
- Literal - 匹配任意字面量（包括布尔值、数值和字符串）。作为低优先级的通配匹配策略。
- Bool - 匹配任意布尔字面量。兼容需要预先解析或评估的语法宏，例如参数、封装变量引用。对于参数，需要表达式整个作为参数。
- Int - 匹配任意整数字面量。兼容需要预先解析或评估的语法宏，例如参数、封装变量引用、内联数学块。对于参数，需要表达式整个作为参数（兼容起始的一元运算符）。
- Float - 匹配任意浮点数字面量（也包括整数字面量）。兼容需要预先解析或评估的语法宏，例如参数、封装变量引用、内联数学块。对于参数，需要表达式整个作为参数（兼容起始的一元运算符）。
- Scalar - 匹配任意字面量（包括布尔值、数值和字符串）。兼容需要预先解析或评估的语法宏，例如参数、封装变量引用。
- Block - 匹配任意块。由插件内部使用，不能直接在规则文件中使用。

特殊的基本数据类型：
- WildcardAny - Any 的一种特殊变体，当匹配候选项过多，或者无法进一步确定时使用。由插件内部使用，不应直接在规则文件中使用。
- WildcardLiteral - Literal 的一种特殊变体，当匹配候选项过多，或者无法进一步确定时使用。由插件内部使用，不应直接在规则文件中使用。
- ~~或者，只是在创建模拟规则时加上特殊的元数据，例如 `wildcard=true`。~~ - 但是目前的代码架构并不允许在后续注入元数据。
- 对应的数据表达式分别是 `$$any` 和 `$$literal`。通过两个美元符号暗示其特殊性。

核心数据类型：
- ValueField
- IntValueField
- VariableField
- IntVariableField

备注：
- 由于 valueField 和 variableField 不一定会被评估为数值（在少数情况下，也可能是字符串、数组等），需要考虑新增数据类型 FloatValueField 和 FloatVariableField。
- 某些数据类型主要由插件内部使用（在运行时模拟构造对应的规则对象），而不是直接在规则文件中使用（作为更低优先级的匹配策略）。

任务（主要）：
- [x] 新增必要的数据类型
- [x] 实现相关的 dataExpressionSupport
- [x] 实现相关的 scriptExpressionMatcher/csvExpressionMatcher
- [x] 更新数据表达式的合并逻辑
- [x] 更新其他与这些数据类型相关的代码位置
- [x] 更新相关的参考文档

任务（尚未确定）：
- [ ] 新增数据类型 FloatValueField 和 FloatVariableField，更新相关的代码、文档等

相关代码位置：
- `icu.windea.pls.config.CwtDataType`
- `icu.windea.pls.config.CwtDataTypes`
- `icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectCandidate` - 关键使用处
- `icu.windea.pls.lang.match.ParadoxMatchCandidateService.collectProcessedCandidate` - 关键使用处
- `icu.windea.pls.lang.resolve.ParadoxConfigService.collectConfigsForConfigContext` - 关键使用处
- `icu.windea.pls.ep.resolve.parameter.ParadoxDefaultExpressionParameterInferredConfigProvider.getConfig` - 关键使用处
