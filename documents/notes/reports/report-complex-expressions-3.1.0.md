# 复杂表达式体系调研报告（v3.1.0）

> - 版本：`v3.1.0`（当前工作树为 `3.1.0-dev`）
> - 日期：`2026-11-27`
> - 状态：`调研快照`
> - 调研范围：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/**`、`src/main/kotlin/icu/windea/pls/expression/**`、`src/main/kotlin/icu/windea/pls/model/expressions/**`、`src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt`、`src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt`，以及 `lang/references/**`、`lang/codeInsight/highlighting/**`、`lang/inspections/**`、`lang/codeInsight/completion/**`、`lang/scope/**`、`ep/**` 中与复杂表达式消费相关的入口。
> - 对照材料：`documents/notes/proposals/`、`documents/notes/adr/`、`documents/notes/ideas.md`、`docs/zh/ref-syntax.md`、`docs/zh/ref-config-format.md`、`documents/notes/issues/issue-451-vic3-quote-escape-parser-break.md`、GitHub Issues、Paradox 官方 Wiki、CWTools 官方 Wiki。
> - 性质：只读调研快照；本报告不修改代码，不替代 `documents/design/` 中的正式设计文档。
> - 说明：官方 Paradox Wiki 在当前网络环境下会触发 Cloudflare 挑战，本次通过 `r.jina.ai` 只读代理取得正文；CWTools 官方 Wiki 直接可读；Tiger 的独立公开文档未能在本次检索中取得，因此外部工具对照以 CWTools 官方 Wiki 为准，不把未核实内容写成事实。

## 1. 结论摘要

- 当前插件中的“复杂表达式”是一套**由 CWT 数据表达式类型驱动、运行期构造的轻量语法树**，不是语言文法层（BNF/JFlex）的产物，也不是 IntelliJ 平台意义上的 PSI 子树。
- 这套体系的中心是 `ParadoxComplexExpression` 接口族、`ParadoxComplexExpressionNode` 节点族，以及 `ParadoxComplexExpressionResolver` 中按 `CwtDataType` 分派的私有解析入口，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:40` 与同文件 `:76`。
- 目前共有 **12 个复杂表达式家族**：作用域字段、值字段、变量字段、动态值、命令、脚本值引用、定值引用、数组定值引用、标签集、数据库对象、命名格式、模板。
- 这 12 个家族并不完全同构：前 5 个以“链接/分段/运算符”结构为主，后 7 个各自拥有独立语法；其中 `ScriptValueReference`、`Command`、`NameFormat`、`Template` 还存在入口范围或语义覆盖上的特殊边界。
- 复杂表达式只覆盖**脚本文件**与**本地化文件**两类语言上下文；CSV 文件走独立的 `ParadoxCsvExpressionMatcher` 体系，不进入 `ParadoxComplexExpression` 子系统。
- 复杂表达式在子系统内部承担“结构解析 + 节点级引用 + 节点级高亮属性 + 节点级错误 + 节点级相关规则”的职责；语义匹配、作用域评估、补全、代码检查、评估器分别由独立消费者按节点类型分派，参见第 5 节。
- 当前设计与 `documents/notes/proposals/complex-expression-refactors.md` 中的重构蓝图存在明确边界：现行契约是 v3.1.0-dev 的 12 个家族与既有节点模型；Token/TokenStream/Scanner 重构、Jomini 数据函数、单引号字面量语义、动态链接字面量优化均属于 v3.1.x+ 的提案范围，不应写成现行事实。
- 主要结构性风险集中在：解析逻辑按家族重复手写、`CwtDataType` 分派硬编码、坐标基准不统一、完整性模式依赖 `ThreadLocal`、`ScriptValueReference` 未进入通用入口、`Command` 仅占位、命名格式文档与代码不一致，详见第 6 节。

## 2. 术语与层次

### 2.1 四个不同层次

- **表达式（expression）**：脚本文件、本地化文件或 CSV 文件中的一段文本，对应 `ParadoxExpression` 或 `ParadoxExpressionElement`，参见 `src/main/kotlin/icu/windea/pls/model/expressions/ParadoxExpression.kt:47`。
- **规则表达式（config expression）**：CWT 规则字段中的结构化语法，例如 `scope_field`、`value_field`、`$define_reference`，对应 `CwtDataExpression` 及其类型 `CwtDataType`。
- **复杂表达式（complex expression）**：一段被解析为多节点轻量语法树的表达式，对应 `ParadoxComplexExpression`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:40`。
- **节点（node）**：复杂表达式内部的组成单元，可以是标识符、字面量、标记、运算符、链接、动态数据或嵌套复杂表达式，对应 `ParadoxComplexExpressionNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxComplexExpressionNode.kt:30`。

### 2.2 与 CWTools 的关系

- CWTools 用“简单规则”“值集合”“链接规则”等描述脚本取值；插件在此基础上增加了“复杂表达式”这一层运行期结构，用于把一段文本拆成可分别解析引用、高亮与报错的节点。
- 插件内部的数据类型名称大量沿用 CWTools 的命名（如 `scope_field`、`value_field`、`variable_field`、`localisation`），但插件的 `Value`、`ValueSet`、`DynamicValue`、`DatabaseObject`、`NameFormat`、`Template` 等类型存在扩展或语义重解释，详见第 8 节。
- CWT 规则文档自身将复杂表达式定义为“一类轻量的语法树”，参见 `docs/zh/ref-config-format.md:40`；链接规则文档明确其“驱动了这些复杂表达式的解析逻辑以及各种相关的语言功能”，参见 `docs/zh/ref-config-format.md:732` 与 `:738`。

## 3. 全家族清单与触发条件

### 3.1 总表

| 家族 | 触发数据表达式 / 数据类型 | 优先级 | 主要语言上下文 | 通用解析入口 |
| --- | --- | --- | --- | --- |
| `ParadoxScopeFieldExpression` | `scope_field`、`scope[{type}]`、`scope_group[{name}]`；对应 `CwtDataTypeSets.ScopeField` | `40.0` | 脚本；也可作为其他复杂表达式的嵌套段 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxValueFieldExpression` | `value_field{range}`、`int_value_field{range}`；对应 `CwtDataTypeSets.ValueField` | `35.0` | 脚本；也可作为嵌套段 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxVariableFieldExpression` | `variable_field{range}`、`int_variable_field{range}`、`variable_field_32{range}`、`int_variable_field_32{range}`；对应 `CwtDataTypeSets.VariableField` | `35.0` | 脚本；也可作为嵌套段 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxDynamicValueExpression` | `value[{name}]`、`value_set[{name}]`、`dynamic_value[{name}]`；对应 `CwtDataTypeSets.DynamicValue` | `30.0` | 脚本；作为链接数据源或独立配置值 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxCommandExpression` | `$command`；对应 `CwtDataTypes.Command` | `35.0` | 本地化命令文本；命名格式表达式中的 `[...]`；不用于脚本匹配 | `ParadoxComplexExpression.resolveByConfig`（脚本侧）与 `ParadoxComplexExpression.resolve`（本地化侧） |
| `ParadoxScriptValueReferenceExpression` | `$script_value_reference`；对应 `CwtDataTypes.ScriptValueReference` | `60.0` | 脚本；主要作为值字段的数据源，或由 matcher 直接检查 | **不在**通用 `resolveByConfig` 分派中；由值字段值节点与 matcher 直接构造 |
| `ParadoxDefineReferenceExpression` | `$define_reference`；对应 `CwtDataTypes.DefineReference` | `60.0` | 脚本 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxArrayDefineReferenceExpression` | `$array_define_reference`；对应 `CwtDataTypes.ArrayDefineReference` | `60.0` | 脚本 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxTagsExpression` | `$tags[{name}]`、`$tags_condition[{name}]`；对应 `CwtDataTypes.Tags` | `60.0` | 脚本 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxDatabaseObjectExpression` | `$database_object`；对应 `CwtDataTypes.DatabaseObject` | `60.0` | 脚本；本地化概念名称（严格模式要求包含 `:`） | `ParadoxComplexExpression.resolveByConfig` 与 `ParadoxComplexExpression.resolve` |
| `ParadoxNameFormatExpression` | `name_format[{type}]`、`$name_format[{type}]`；对应 `CwtDataTypes.NameFormat` | `60.0` | 脚本；内部 `[...]` 命令使用本地化链接/命令规则 | `ParadoxComplexExpression.resolveByConfig` |
| `ParadoxTemplateExpression` | 模板自身作为数据表达式（含常量与动态片段）；对应 `CwtDataTypes.Template` | `72.0` | 脚本；也用于修正模板配置 | `ParadoxComplexExpression.resolveByConfig` |

- 表中的优先级来自 `CwtDataTypes` 的 `withPriority`，不是复杂表达式解析器自身的排序；它影响规则匹配时多个数据表达式的候选选择。
- `CwtDataTypes.Command` 的 KDoc 明确写明“目前仅作占位，不支持匹配脚本文件中的表达式”，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:547`；脚本侧 matcher 也直接返回 `NotMatch`，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:209`。
- `CwtDataTypes.Template` 的优先级在 3.0.5 被提升到 `72.0`，以高于裸定义引用（`70.0`），参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:729`。

### 3.2 作用域字段表达式

- **触发条件**：CWT 数据类型属于 `CwtDataTypeSets.ScopeField`，即 `ScopeField`、`Scope`、`ScopeGroup`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:44`。
- **结构**：由若干作用域链接节点组成，节点之间用点号分隔；每个节点可以是系统链接、静态链接、动态链接或带参数链接，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScopeFieldExpression.kt:18`。
- **解析入口**：`ParadoxComplexExpression.resolveByConfig` 先判断 `ScopeField` 集合并调用 `ParadoxScopeFieldExpression.resolve(text, range, configGroup)`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:105`。
- **节点类型优先级**：工厂依次尝试 `ParadoxSystemScopeNode`、`ParadoxStaticScopeNode`、`ParadoxDynamicScopeNode`、`ParadoxParameterizedScopeNode`，全部失败则回退为 `ParadoxErrorScopeNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxScopeNode.kt:15`。
- **静态/系统节点**：系统作用域直接查 `configGroup.systemScopes`；静态作用域查 `configGroup.links[text]`，要求 `type.forScope()` 且 `isStatic`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxSystemScopeNode.kt:47` 与 `ParadoxStaticScopeNode.kt:48`。
- **动态节点**：支持两种形式，前缀形式如 `event_target:some_target`，传参形式如 `relations(root.owner)`；前者从 `linkModel.forScopeFromDataSorted` 匹配前缀，后者从 `linkModel.forScopeFromArgumentSortedByPrefix` 匹配函数名，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDynamicScopeNode.kt:34` 与 `:70`。
- **参数值**：动态节点的值节点可以继续嵌套解析为 `ParadoxDynamicValueExpression`、`ParadoxScopeFieldExpression` 或 `ParadoxValueFieldExpression`，否则回退为 `ParadoxDataSourceNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxScopeValueNode.kt:134`。
- **参数列表**：值节点会检测顶层分隔符（逗号或管道，取决于链接规则的 `argument_separator`），并保留空白、分隔符与参数值；顶层分隔符检测会考虑括号深度、单引号与参数区间，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxScopeValueNode.kt:34` 与 `:43`。
- **校验**：`validateAllNodes` 检查每个数据源节点是否为参数感知标识符，失败则报“malformed scope field expression”；脚本文件中的表达式若包含 `(`、`)`、`,` 且未加双引号，额外报“表达式未加引号”，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScopeFieldExpression.kt:130` 与 `util/ParadoxComplexExpressionValidatorScope.kt:30`。

### 3.3 值字段表达式

- **触发条件**：CWT 数据类型属于 `CwtDataTypeSets.ValueField`，即 `IntValueField`、`ValueField`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:51`。
- **结构**：零个或多个作用域链接节点，加上最后一个值字段节点，节点之间用点号分隔，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxValueFieldExpression.kt:22`。
- **解析前置条件**：文本本身是浮点数，或文本是带一元前导符的完整参数时，解析器直接返回 `null`，交由基本数值匹配处理，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxValueFieldExpression.kt:110`。
- **值字段节点工厂**：依次尝试 `ParadoxStaticValueFieldNode`、`ParadoxDynamicValueFieldNode`、`ParadoxParameterizedValueFieldNode`，失败则回退为 `ParadoxErrorValueFieldNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxValueFieldNode.kt:12`。
- **静态值字段**：查 `configGroup.links[text]`，要求 `type.forValue()` 且 `isStatic`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxStaticValueFieldNode.kt:40`。
- **动态值字段**：与动态作用域节点同源，使用 `linkModel.forValueFromArgumentSortedByPrefix`、`forValueFromDataSorted`、`forValueNoPrefixSorted`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDynamicValueFieldNode.kt:34`、`:70`、`:92`。
- **值节点嵌套优先级**：`ParadoxValueFieldValueNode.resolveDsNode` 依次尝试动态值（仅当文本包含 `@`）、作用域字段、值字段、脚本值引用、定值引用、数组定值引用，最后回退为数据源节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxValueFieldValueNode.kt:139`。
- **参数索引**：`ParadoxLinkValueNode.getArgumentIndex` 通过统计当前偏移之前的逗号标记数计算参数索引，供补全使用，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxLinkValueNode.kt:29`。
- **校验**：同作用域字段的数据源标识符检查与引号检查，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxValueFieldExpression.kt:164`。
- **匹配**：脚本 matcher 对整数/浮点数直接按数值匹配（含范围检查），只有非数值字符串才进入复杂表达式解析；若解析结果包含错误则返回 `PartialMatch`，否则按综合属性返回精确/通配匹配，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:148` 与 `ParadoxMatchResultFactory.kt:313`。

### 3.4 变量字段表达式

- **触发条件**：CWT 数据类型属于 `CwtDataTypeSets.VariableField`，即 `IntVariableField`、`VariableField`；KDoc 将其描述为 `ValueField` 的特殊子集，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:56` 与 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:496`。
- **结构**：零个或多个作用域链接节点，加上最后一个数据源节点；最后一个节点固定使用 `configGroup.linkModel.variable` 对应的链接配置，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxVariableFieldExpression.kt:27` 与 `:125`。
- **和值字段的差异**：变量字段的末尾不构造 `ParadoxValueFieldNode`，而是直接构造 `ParadoxDataSourceNode`；其 KDoc 明确“仅支持调用变量”，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxVariableFieldExpression.kt:24`。
- **当前宽松点**：末尾数据源节点即使没有匹配到任何链接配置，也会被构造；校验只检查其文本是否为参数感知标识符，因此普通标识符可以通过格式校验，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxVariableFieldExpression.kt:129` 与 `:143`。
- **匹配**：与值字段类似，先处理数值字面量，再解析为复杂表达式，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:173`。

### 3.5 动态值表达式

- **触发条件**：CWT 数据类型属于 `CwtDataTypeSets.DynamicValue`，即 `Value`（读）、`ValueSet`（写）、`DynamicValue`（读写不区分），参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:39`。
- **结构**：一个动态值节点，加上可选的 `@` 标记与之后的作用域字段表达式；语法为 `dynamic_value ("@" scope_field_expression)?`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDynamicValueExpression.kt:36`。
- **独立与嵌套的差别**：独立使用时，`@` 之后允许完整的链式作用域；嵌套在链式表达式中时，外层解析器会在点号处切分，`@` 之后实际只保留属于当前链接节点的部分，之后的外层节点归外层表达式，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDynamicValueExpression.kt:26`。
- **解析约束**：传入的配置列表必须全部属于 `DynamicValue`，否则解析器返回 `null`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDynamicValueExpression.kt:79`。
- **匹配宽松性**：脚本 matcher 只检查表达式是“宽松字面量”、非参数化、`@` 之前的名称是合法标识符（允许 `.`），并且配置存在动态值类型元数据，然后直接返回 `FallbackMatch`，不会验证 `@` 之后的作用域链，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:121`。
- **校验**：动态值节点必须匹配参数感知标识符（允许 `.`）；失败则报“malformed dynamic value expression”，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDynamicValueExpression.kt:131`。
- **引用**：`ParadoxDynamicValueNode.getReference` 在文本非参数化时返回引用，解析目标为 `ParadoxDynamicValueLightElement`，并由 `ParadoxDynamicValueManager` 按动态值类型构造，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDynamicValueNode.kt:36` 与 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxDynamicValueManager.kt:24`。

### 3.6 命令表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.Command`（数据表达式 `$command`）；在本地化侧，元素必须是 `ParadoxLocalisationCommandText`，参见 `src/main/kotlin/icu/windea/pls/lang/psi/ParadoxPsiExtensions.kt:236`。
- **结构**：零个或多个命令作用域节点，加上最后一个命令字段节点，节点之间用点号分隔；命令作用域节点可以是系统、静态、动态或带参数链接，命令字段节点同样可以是静态、动态或带参数链接，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxCommandExpression.kt:26`。
- **节点工厂**：`ParadoxCommandScopeNode` 与 `ParadoxCommandFieldNode` 各自按系统、静态、动态、带参数、错误的顺序回退，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxCommandScopeNode.kt:13` 与 `ParadoxCommandFieldNode.kt:12`。
- **配置来源**：命令作用域使用 `configGroup.localisationLinks`/`localisationLinkModel`；命令字段的静态节点使用 `configGroup.localisationCommands`；动态字段使用 `localisationLinkModel.forValue*`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxStaticCommandScopeNode.kt:48`、`ParadoxStaticCommandFieldNode.kt:47`、`ParadoxDynamicCommandFieldNode.kt:34`。
- **匹配边界**：脚本 matcher 的 `ForCommand` 永远返回 `NotMatch`；因此命令表达式目前只在本地化上下文与命名格式内部生效，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:209`。
- **本地化参数表示**：命令文本之外的 `|argument` 后缀与 `&tags` 不计入 `ParadoxCommandExpression`；这些由本地化 PSI、参数引用与富文本相关逻辑处理，参见 `docs/zh/ref-syntax.md:369` 与 `:373`。
- **校验**：命令表达式的数据源标识符检查与引号检查，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxCommandExpression.kt:156`。

### 3.7 脚本值引用表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.ScriptValueReference`（数据表达式 `$script_value_reference`），优先级 `60.0`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:573`。
- **结构**：`script_value ("|" argument_name "|" argument_value "|")*`；脚本值名、参数名与参数值分别对应 `ParadoxScriptValueNode`、`ParadoxScriptValueArgumentNameNode`、`ParadoxScriptValueArgumentValueNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScriptValueReferenceExpression.kt:39`。
- **约束**：管道数量应为 `0` 或奇数（`>=3`）；仅 1 个或非零偶数个管道视为格式错误，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScriptValueReferenceExpression.kt:58` 与 `:151`。
- **入口特殊性**：`ParadoxComplexExpression.resolveByConfig` 没有 `ScriptValueReference` 分支；该表达式目前主要作为值字段的数据源，由 `ParadoxValueFieldValueNode.resolveDsNode` 在文本匹配 `$script_value_reference` 时直接构造，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxValueFieldValueNode.kt:152`；命令字段的值节点当前不处理脚本值引用，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxCommandFieldValueNode.kt:137`。
- **脚本支持注册与实际入口的差异**：EP 中注册了 `ParadoxComplexScriptExpressionSupport.ForScriptValueReference`，但其基类 `annotate`/`getReferences` 走通用 `resolveByConfig`；由于通用入口缺少该分支，顶层配置类型不会返回复杂表达式，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/expression/ParadoxScriptExpressionSupports.ComplexExpression.kt:34` 与 `src/main/kotlin/icu/windea/pls/ep/resolve/expression/ParadoxScriptExpressionSupports.ComplexExpression.kt:127`。
- **引用**：脚本值节点解析为脚本值定义引用；参数名节点解析为参数光元素；参数值节点当前不提供引用/高亮，注释说明改由语言注入实现高级功能，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxScriptValueNode.kt:46`、`ParadoxScriptValueArgumentNameNode.kt:29`、`ParadoxScriptValueArgumentValueNode.kt:13`。
- **评估边界**：KDoc 称评估结果应是一个数字字面量，但当前没有 `ScriptValueReference` 评估器；`CwtDataTypeSets.ScriptValueReferenceEvaluatable` 也未被任何代码使用，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:145`。

### 3.8 定值引用表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.DefineReference`（数据表达式 `$define_reference`），优先级 `60.0`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:589`。
- **结构**：`Namespace|Name`，对应命名空间节点、`|` 标记、变量节点，共 3 个节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDefineReferenceExpression.kt:25`。
- **解析约束**：当前解析器没有处理参数区间；文件中有显式 TODO“3.1.0+ compatible with parameter ranges”，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDefineReferenceExpression.kt:65`。
- **引用**：命名空间节点使用 `ParadoxDefineNamespaceSearch`；变量节点使用 `ParadoxDefineVariableSearch`；两者均不支持重命名，重命名时抛 `IncorrectOperationException`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDefineNamespaceNode.kt:64` 与 `ParadoxDefineVariableNode.kt:70`。
- **校验**：所有 `ParadoxIdentifierNode` 的文本必须是参数感知标识符；节点数必须为 3，否则报格式错误，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDefineReferenceExpression.kt:100`。
- **评估**：`ParadoxDefineReferenceExpressionEvaluator` 解析变量节点，读取属性值，并在 `resolve=true` 时调用 `resolved()`，参见 `src/main/kotlin/icu/windea/pls/lang/util/evaluators/ParadoxDefineReferenceExpressionEvaluator.kt:55`。

### 3.9 数组定值引用表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.ArrayDefineReference`（数据表达式 `$array_define_reference`），优先级 `60.0`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:605`。
- **结构**：`Namespace|Name|index`，共 5 个节点；索引从 0 开始，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxArrayDefineReferenceExpression.kt:29`。
- **参数区间**：与定值引用相同，当前有“3.1.0+ compatible with parameter ranges”的 TODO，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxArrayDefineReferenceExpression.kt:71`。
- **索引节点类型不一致**：解析器兼容字符串字面量，非数值文本会构造 `ParadoxStringLiteralNode`；但接口属性 `indexNode` 的类型是 `ParadoxNumberLiteralNode?`，因此字符串索引时属性为 `null`，而校验器仍通过 `ParadoxLiteralNode` 查找索引节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxArrayDefineReferenceExpression.kt:106`、`:168` 与 `:145`。
- **校验**：检查变量引用是否指向数组定义，并检查索引是否为整数、是否越界，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxArrayDefineReferenceExpression.kt:135`。
- **评估**：`ParadoxArrayDefineReferenceExpressionEvaluator` 检查数组定义、读取索引值并调用 `ParadoxDefineManager.getArrayValue`，参见 `src/main/kotlin/icu/windea/pls/lang/util/evaluators/ParadoxArrayDefineReferenceExpressionEvaluator.kt:56`。

### 3.10 标签集表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.Tags`（数据表达式 `$tags[{name}]` 或 `$tags_condition[{name}]`），优先级 `60.0`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:623`。
- **结构**：逗号分隔的动态值节点序列，允许空白、允许空段；条件变体允许 `not(tag)`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTagsExpression.kt:21`。
- **条件变体来源**：解析器读取 `config.configExpression.metadata.value` 作为动态值类型，并读取 `metadata.condition`；条件变体构造 `value[$type]`（读），非条件变体构造 `value_set[$type]`（写），参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTagsExpression.kt:69`。
- **校验**：标识符必须是参数感知标识符；非条件变体中出现 `not(...)` 时报“notCondition”；脚本表达式中未加双引号时报未加引号错误，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTagsExpression.kt:144` 与 `:156`。
- **空字符串**：通用解析器在非完整性模式下对空文本返回 `null`；匹配层的 `ForTags` 对空字符串直接返回 `FallbackMatch`，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:254`。
- **节点**：动态值节点、取反动态值节点、标记节点、空白节点；取反节点是复合节点而不是复杂表达式，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxNegatedDynamicValueNode.kt:11`。

### 3.11 数据库对象表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.DatabaseObject`（数据表达式 `$database_object`），优先级 `60.0`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:639`。
- **语言上下文**：脚本表达式的值可以匹配；本地化文件中的 `ParadoxLocalisationConceptName` 在严格模式下要求包含 `:` 才作为数据库对象表达式，参见 `src/main/kotlin/icu/windea/pls/lang/psi/ParadoxPsiExtensions.kt:236` 与 `:244`。
- **结构**：`type ":" value (":" swapped_value)?`，节点依次为类型节点、`:` 标记、值节点、可选的 `:` 标记与交换值节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDatabaseObjectExpression.kt:21`。
- **类型配置**：`ParadoxDatabaseObjectTypeNode` 从 `configGroup.databaseObjectTypes` 获取配置；配置决定搜索定义类型还是本地化类型，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDatabaseObjectTypeNode.kt:58`。
- **基础/交换值语义**：`ParadoxDatabaseObjectNode` 区分基础值与交换值；`isForcedBase` 表示重复引用同一基础值以强制显示非交换形态；`getTypeToSearch` 根据 `isBase`/`isForcedBase` 在 `type` 与 `swapType` 之间选择，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDatabaseObjectNode.kt:70` 与 `:115`。
- **本地化解析**：数据库对象类型配置指定 `localisation` 时，只在基础值位置解析本地化，并按上下文语言/首选语言搜索，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDatabaseObjectNode.kt:162`。
- **校验**：节点数必须为 3 或 5；本地化类型允许 `.`、`-`、`'`，定义类型按普通参数感知标识符检查，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxDatabaseObjectExpression.kt:112`。

### 3.12 命名格式表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.NameFormat`（`name_format[{type}]` 或 `$name_format[{type}]`），优先级 `60.0`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:655`。
- **结构**：顶层只识别花括号闭包 `{...}`；闭包内容按优先级识别命令 `[...]`、定义占位 `<name>`、本地化标识符与普通文本，空白保留为空白节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:21` 与 `:49`。
- **定义占位类型**：代码取 `configExpression.metadata.value` 作为格式名，并构造定义类型 `${formatName}_name_parts_list`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:89` 与 `:137`。
- **命令节点**：`[...]` 被包装为 `ParadoxCommandNode`，内部为 `ParadoxCommandExpression`，并保留 `[`、`]` 标记节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:242`。
- **命名部件节点**：`<name>` 被包装为 `ParadoxNamePartNode`，内部包含 `<`、`>` 标记节点与 `ParadoxNameFormatDefinitionNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:201`。
- **本地化节点**：连续标识符字符若整体是参数感知标识符（允许 `.-'`），解析为 `ParadoxNameFormatLocalisationNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:324`。
- **不完整输入**：缺失 `]`、`>`、`}` 时，在包装节点内部追加空错误标记；必要时在当前层末尾追加尾随错误节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:60`。
- **文档与代码的不一致**：`CwtDataTypes.NameFormat` 与 `docs/zh/ref-config-format.md` 写的是“对应的定义类型为 `{name}_name_format`”，而代码实际使用 `${formatName}_name_parts_list`，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:648`、`docs/zh/ref-config-format.md:2174` 与 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:90`。

### 3.13 模板表达式

- **触发条件**：CWT 数据类型为 `CwtDataTypes.Template`；模板字符串来自 `CwtDataExpression.expressionString`，或修正配置的 `CwtModifierConfig.template`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTemplateExpression.kt:53`。
- **结构**：与模板按组匹配，顺序交替产生常量片段与引用片段；常量片段对应 `ParadoxTemplateSnippetConstantNode`，引用片段对应 `ParadoxTemplateSnippetNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTemplateExpression.kt:25`。
- **部分匹配**：完整性模式下允许部分匹配；非完整性模式下，若匹配组数量不足或非常量片段为空则返回 `null`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTemplateExpression.kt:67`。
- **引用解析**：引用片段使用模拟的值配置（`CwtValueConfig.mock`）；若片段类型属于动态值，则走 `ParadoxDynamicValueManager`，否则走脚本表达式解析，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxTemplateSnippetNode.kt:37` 与 `:95`。
- **匹配**：脚本 matcher 先做快速模板匹配；快速匹配返回 `FallbackMatch` 时再做完整的惰性模板匹配，参见 `src/main/kotlin/icu/windea/pls/lang/match/util/ParadoxMatchResultFactory.kt:261`。
- **与片段数据类型的关系**：`DefinitionSnippet`、`LocalisationSnippet` 是独立的数据类型，不属于复杂表达式家族；它们可以出现在模板/引用片段语义中，但本身不构造 `ParadoxComplexExpression`，参见 `documents/notes/adr/snippet-match.md:11` 与 `:15`。

### 3.14 不在复杂表达式范围内的相关构造

- 本地化参数 `$key$`、`$key|arg$`、`$@var$` 是 `ParadoxLocalisationParameter`，由本地化 PSI 与引用提供者处理，不是复杂表达式，参见 `docs/zh/ref-syntax.md:369`。
- 本地化颜色标记、图标、概念命令、文本格式、文本图标分别由本地化富文本 PSI/引用/检查处理；`ParadoxComplexExpression.resolve` 只识别命令文本与概念名称，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:84` 与 `docs/zh/ref-syntax.md:363`。
- CSV 文件使用 `ParadoxCsvExpressionMatcher`/`ParadoxCsvExpressionSupport`，不进入 `ParadoxComplexExpression` 的脚本/本地化分支，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxCsvExpressionMatcher.kt` 与 `src/main/kotlin/icu/windea/pls/ep/resolve/expression/ParadoxCsvExpressionSupport.kt`。

## 4. 节点树与 DSL 模型

### 4.1 根接口与基本契约

- `ParadoxComplexExpressionNode` 暴露 `text`、`rangeInExpression`、`configGroup`、`nodes`、`parent`，以及 `getRelatedConfigs()`、`getAttributesKey()`、`getAttributesKeyConfig()`、`getReference()`、`getUnresolvedError()` 与访问者方法，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxComplexExpressionNode.kt:30`。
- `ParadoxComplexExpression` 继承节点接口，额外暴露 `getErrors()`、`getAllErrors()`、`getAllReferences()`，并提供静态 `resolve`/`resolveByConfig` 入口，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:40`。
- `ParadoxComplexExpressionBase` 负责在 `finishResolution()` 时递归绑定父节点；`getAllErrors()` 先取自身错误，再递归收集每个节点的未解析错误；`getAllReferences()` 递归收集每个节点的引用，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpressionBase.kt:10`。
- `ParadoxComplexExpressionNodeBase` 提供默认空子节点列表、可变父节点与基于文本的相等性/哈希；各表达式实现通常再按自身接口重写相等性，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxComplexExpressionNodeBase.kt:3`。

### 4.2 节点树（按抽象层次）

```text
ParadoxComplexExpressionNode
├─ ParadoxComplexExpression（本身也是节点）
│  ├─ ParadoxScopeFieldExpression
│  ├─ ParadoxValueFieldExpression
│  ├─ ParadoxVariableFieldExpression
│  ├─ ParadoxCommandExpression
│  ├─ ParadoxDynamicValueExpression
│  ├─ ParadoxScriptValueReferenceExpression
│  ├─ ParadoxDefineReferenceExpression
│  ├─ ParadoxArrayDefineReferenceExpression
│  ├─ ParadoxTagsExpression
│  ├─ ParadoxDatabaseObjectExpression
│  ├─ ParadoxNameFormatExpression
│  └─ ParadoxTemplateExpression
├─ ParadoxLinkedExpression（链式表达式接口）
│  ├─ ParadoxScopeFieldExpression
│  ├─ ParadoxValueFieldExpression
│  ├─ ParadoxVariableFieldExpression
│  └─ ParadoxCommandExpression
├─ ParadoxLinkNode
│  ├─ ParadoxScopeNode
│  │  ├─ ParadoxSystemScopeNode
│  │  ├─ ParadoxStaticScopeNode
│  │  ├─ ParadoxDynamicScopeNode
│  │  ├─ ParadoxParameterizedScopeNode
│  │  └─ ParadoxErrorScopeNode
│  ├─ ParadoxValueFieldNode
│  │  ├─ ParadoxStaticValueFieldNode
│  │  ├─ ParadoxDynamicValueFieldNode
│  │  ├─ ParadoxParameterizedValueFieldNode
│  │  └─ ParadoxErrorValueFieldNode
│  ├─ ParadoxCommandScopeNode
│  │  ├─ ParadoxSystemCommandScopeNode
│  │  ├─ ParadoxStaticCommandScopeNode
│  │  ├─ ParadoxDynamicCommandScopeNode
│  │  ├─ ParadoxParameterizedCommandScopeNode
│  │  └─ ParadoxErrorCommandScopeNode
│  └─ ParadoxCommandFieldNode
│     ├─ ParadoxStaticCommandFieldNode
│     ├─ ParadoxDynamicCommandFieldNode
│     ├─ ParadoxParameterizedCommandFieldNode
│     └─ ParadoxErrorCommandFieldNode
├─ ParadoxLinkPrefixNode
│  ├─ ParadoxScopePrefixNode
│  ├─ ParadoxValueFieldPrefixNode
│  ├─ ParadoxCommandScopePrefixNode
│  └─ ParadoxCommandFieldPrefixNode
├─ ParadoxLinkValueNode
│  ├─ ParadoxScopeValueNode
│  ├─ ParadoxValueFieldValueNode
│  ├─ ParadoxCommandScopeValueNode
│  └─ ParadoxCommandFieldValueNode
├─ ParadoxIdentifierNode
├─ ParadoxLiteralNode
│  ├─ ParadoxNumberLiteralNode
│  └─ ParadoxStringLiteralNode
├─ ParadoxTokenNode
│  ├─ ParadoxMarkerNode
│  ├─ ParadoxOperatorNode
│  └─ ParadoxKeywordNode
├─ ParadoxErrorNode
│  ├─ ParadoxErrorTokenNode
│  ├─ ParadoxErrorScopeNode
│  ├─ ParadoxErrorValueFieldNode
│  ├─ ParadoxErrorCommandScopeNode
│  └─ ParadoxErrorCommandFieldNode
├─ ParadoxDynamicDataNode（混入接口）
│  ├─ ParadoxDynamicValueNode
│  ├─ ParadoxDataSourceNode
│  ├─ ParadoxScriptValueNode
│  ├─ ParadoxDefineNamespaceNode
│  ├─ ParadoxDefineVariableNode
│  ├─ ParadoxDatabaseObjectNode
│  ├─ ParadoxNameFormatDefinitionNode
│  ├─ ParadoxNameFormatLocalisationNode
│  └─ ParadoxTemplateSnippetNode
├─ ParadoxBlankNode
├─ ParadoxDynamicValueNode
├─ ParadoxNegatedDynamicValueNode
├─ ParadoxDataSourceNode
├─ ParadoxScriptValueNode
├─ ParadoxScriptValueArgumentNameNode
├─ ParadoxScriptValueArgumentValueNode
├─ ParadoxDefineNamespaceNode
├─ ParadoxDefineVariableNode
├─ ParadoxDatabaseObjectTypeNode
├─ ParadoxDatabaseObjectValueNode
├─ ParadoxDatabaseObjectNode
├─ ParadoxNameFormatClosureNode
├─ ParadoxNamePartNode
├─ ParadoxNameFormatDefinitionNode
├─ ParadoxNameFormatLocalisationNode
├─ ParadoxNameFormatTextNode
├─ ParadoxCommandNode
├─ ParadoxTemplateSnippetConstantNode
└─ ParadoxTemplateSnippetNode
```

- 上述树是抽象层次视图，不是严格的继承树；例如 `ParadoxDynamicValueNode` 同时实现 `ParadoxIdentifierNode` 与 `ParadoxDynamicDataNode`，某些节点会同时出现在多个分组中。
- `ParadoxLinkPrefixNode`、`ParadoxLinkValueNode` 与 `ParadoxParameterizedNode` 是标记接口；`ParadoxSystemScopeAwareLinkNode` 额外暴露 `CwtSystemScopeConfig`，用于系统作用域与系统命令作用域的统一处理。
- 叶子节点通常没有子节点；复合节点（动态链接、带参数链接、取反动态值、命名格式闭包、命名部件、命令节点、数据库对象值节点）会持有子节点。
- `ParadoxErrorNode` 是错误节点的统一标记；错误节点默认会生成未解析错误，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxErrorScopeNode.kt:14` 与 `ParadoxErrorValueFieldNode.kt:14`。

### 4.3 范围、父节点与坐标约定

- 正常解析时，节点 `rangeInExpression` 以表达式文本起点为基准（通常为 0），节点错误与引用在消费侧通过 `ParadoxExpressionService.getExpressionOffset(element)` 与元素起始偏移换算，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxSystemScopeNode.kt:33` 与 `util/ParadoxComplexExpressionError.kt:27`。
- `ParadoxComplexExpression.resolveByConfig(text, range, ...)` 允许调用方传入 `range`；解析器把 `range.startOffset` 加到节点范围上，因此同一节点在不同调用中可能处于“表达式内相对坐标”或“元素内绝对坐标”两种基准；补全调用即传入 `TextRange.from(context.keywordOffset, context.keyword.length)`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScopeFieldExpression.kt:81` 与 `src/main/kotlin/icu/windea/pls/lang/codeInsight/completion/ParadoxComplexExpressionCompletionManager.kt:49`。
- 父节点不是构造时传入，而是在 `finishResolution()` 中递归绑定；`ParadoxComplexExpressionAttributesEvaluator` 的宽松动态数据判定依赖 `parent` 链，因此直接构造节点而不调用 `finishResolution()` 会改变该判定，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpressionBase.kt:11` 与 `attributes/ParadoxComplexExpressionAttributesEvaluator.kt:52`。

### 4.4 DSL 模型

- `ParadoxComplexExpressionDsl` 是测试用 DSL 的密封接口，包含 `ParadoxComplexExpressionDslStub` 与 `ParadoxComplexExpressionDslNode`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/dsl/ParadoxComplexExpressionDsl.kt:9`。
- `ParadoxComplexExpressionDslNode` 保存节点类型、文本、起止偏移与子节点列表；`buildComplexExpression` 构造根节点，`node` 构造子节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/dsl/ParadoxComplexExpressionDsl.kt:22` 与 `:36`。
- `ParadoxComplexExpressionDslStub` 与 `buildComplexExpressionStub` 目前是占位符，测试代码中未发现实际使用，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/dsl/ParadoxComplexExpressionDsl.kt:14` 与 `:30`。
- `ParadoxComplexExpressionDslChecker` 校验类型（`isSuperclassOf`）、文本、起止偏移与子节点数量；不匹配时抛出 `IllegalStateException`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/dsl/ParadoxComplexExpressionDslChecker.kt:7`。
- `ParadoxComplexExpressionDslRenderer` 把已解析节点渲染为 `buildComplexExpression<T>(...)`/`node<T>(...)` 代码，可选择裁剪空叶子节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/dsl/ParadoxComplexExpressionDslRenderer.kt:18`。
- 测试基类 `ParadoxComplexExpressionTest` 提供 `markIncomplete`、`check`、`render` 等辅助方法，参见 `src/test/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpressionTest.kt:13`。
- 12 个家族均有独立测试类（例如 `ParadoxScopeFieldExpressionTest`、`ParadoxValueFieldExpressionTest`、`ParadoxNameFormatExpressionTest`），测试数据配置位于 `src/test/testData/features/complexExpression/.config/`，脚本/本地化测试数据位于同目录的 `common/` 与 `localisation/`。
- DSL 只用于验证“解析结果是否与预期节点树一致”，不是生产解析器；生产解析仍然由各家族的 `resolve` 函数完成。

## 5. 解析、引用解析、高亮、检查的职责边界

### 5.1 责任分工总表

| 职责 | 主要入口 | 依赖 | 明确不负责 |
| --- | --- | --- | --- |
| 结构解析 | `ParadoxComplexExpression.resolveByConfig`、各家族 `resolve` | 规则分组、数据表达式类型、参数区间、`incompleteComplexExpression` 线程状态 | 语义引用解析、代码检查 |
| 引用解析 | 节点 `getReference()`、EP `getReferences` | 节点自身配置、搜索服务、light element | 错误展示、代码高亮 |
| 高亮/注解 | `ParadoxExpressionSupportFactory.annotateComplexExpression` | 节点 `getAttributesKey()`/`getAttributesKeyConfig()`、IDE 配色方案 | 结构解析、引用解析 |
| 代码检查 | `IncorrectComplexExpressionInspectionBase`、各家族检查、通用表达式检查 | 解析结果、`getAllErrors`、节点错误码 | 补全、导航 |
| 语义匹配 | `ParadoxMatchResultFactory`、`ParadoxCoreScriptExpressionMatcher` | 解析结果、`ParadoxComplexExpressionAttributesEvaluator` | 代码高亮、代码补全 |
| 作用域评估 | `ParadoxScopeService`、`ParadoxScopeManager` | 节点类型、链接配置、动态值 light element | 字段引用解析 |
| 代码补全 | `ParadoxComplexExpressionCompletionManager` | 解析结果、节点树、作用域上下文、模拟配置 | 错误报告、持久缓存 |
| 表达式评估 | `ParadoxComplexExpressionEvaluator`、定值/数组定值评估器 | 定值引用解析、脚本值解析 | 通用数值计算、脚本值引用评估 |
| 相关规则/参数推断 | `CwtRelatedConfigProviders`、`ParadoxComplexExpressionNodeParameterInferredConfigProvider` | 节点 `getRelatedConfigs()`、节点类型 | 引用解析、匹配 |
| 编辑器交互 | `ParadoxMarkerManager`、`ParadoxSemanticTypedHandler`、`ParadoxHeavyBraceHighlighter`、单词选择处理器 | 节点树、标记节点 | 结构解析、代码检查 |

### 5.2 结构解析

- 通用入口 `ParadoxComplexExpression.resolve` 对脚本表达式取首个匹配规则，再委托 `resolveByConfig`；对本地化表达式只处理命令文本与概念名称，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:77`。
- `resolveByConfig` 的分派顺序是：作用域字段 → 值字段 → 变量字段 → 命令 → 动态值 → 定值引用 → 数组定值引用 → 标签集 → 数据库对象 → 命名格式 → 模板，参见同文件 `:102`。
- 解析器不注册为 EP；新增家族需要直接修改这个私有 `when`。这是当前 API 的核心约束，也是 `proposals/complex-expression-refactors.md:18` 提出迁移到 `icu.windea.pls.expression` 的原因之一。
- 解析器在非完整性模式下对空文本返回 `null`；完整性模式由 `ParadoxThreadContext.incompleteComplexExpression` 控制，补全入口通过 `markIncomplete` 设置，参见 `src/main/kotlin/icu/windea/pls/lang/ParadoxThreadContext.kt:24` 与 `src/main/kotlin/icu/windea/pls/lang/codeInsight/completion/ParadoxComplexExpressionCompletionManager.kt:149`。
- 参数区间由 `ParadoxExpressionManager.getParameterRanges(text)` 计算，作用域/值/变量/命令的扫描会跳过参数文本中的分隔符，参见 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxExpressionManager.kt:132`。
- 作用域/值/变量/命令解析器共享一套手写扫描策略：按 `.` 切分、括号深度保护、`|` 作为屏障、`value:` 作为屏障检查位置；四个解析器中重复出现，例如 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScopeFieldExpression.kt:89`、`ParadoxValueFieldExpression.kt:123`、`ParadoxVariableFieldExpression.kt:98`、`ParadoxCommandExpression.kt:115`。
- `ParadoxComplexExpression.resolveByConfig` 没有 `ScriptValueReference` 分支；`ScriptValueReference` 的顶层 EP/检查注册与实际通用入口不一致，详见 3.7 与 6.2。
- `ParadoxComplexExpression.resolveByConfig` 对 `Command` 没有语言限制；脚本侧 matcher 明确返回 `NotMatch`，因此“解析层可构造”与“匹配层可使用”并不完全一致，详见 3.6 与 6.2。

### 5.3 引用解析

- 引用由节点自身构造：系统/静态作用域与值字段使用 `CwtConfigBasedPsiReference`；动态值使用 `PsiReferenceBase`；定值节点使用 `PsiPolyVariantReferenceBase`；数据库对象使用多态引用；模板片段与脚本值引用也各自实现引用，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxSystemScopeNode.kt:33`、`ParadoxDynamicValueNode.kt:36`、`ParadoxDefineNamespaceNode.kt:49`、`ParadoxDatabaseObjectNode.kt:62`、`ParadoxTemplateSnippetNode.kt:58`、`ParadoxScriptValueNode.kt:46`。
- 所有引用实现都实现 `ParadoxIdentifierNode.Reference`，该接口继承 `ParadoxConstrainedPsiReference`，默认 `canResolveFor` 返回 `false`；具体节点按约束类型重写，例如动态值支持 `DynamicValue`/`DynamicValueReference`，数据库对象支持 `Definition`/`Localisation`，模板片段支持 `Definition`/`Localisation`/`ComplexEnumValue`/`DynamicValue`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxIdentifierNode.kt:13`。
- 脚本/本地化表达式引用提供者通过 `ParadoxExpressionManager.getExpressionReferences` 进入 EP `ParadoxScriptExpressionSupport`/`ParadoxLocalisationExpressionSupport`；复杂表达式的 `getReferences` 最终调用 `ParadoxComplexExpression.getAllReferences(element)`，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/expression/ParadoxScriptExpressionSupports.ComplexExpression.kt:42` 与 `ParadoxLocalisationExpressionSupports.ComplexExpression.kt:34`。
- 脚本表达式要求“可解析字面量表达式”且是“数据表达式”；本地化表达式要求是复杂表达式；CSV 走独立分支，参见 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxExpressionManager.kt:355` 与 `src/main/kotlin/icu/windea/pls/lang/psi/ParadoxPsiExtensions.kt:196`。
- 引用结果通常被 `ResolveCache` 缓存；数据库对象、模板片段、脚本值节点等都有各自的 `Resolver`/`MultiResolver`，例如 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxDatabaseObjectNode.kt:139`。
- 参数名节点解析为参数光元素，参数值节点当前不提供引用；KDoc 说明高级功能由语言注入实现，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxScriptValueArgumentNameNode.kt:39` 与 `ParadoxScriptValueArgumentValueNode.kt:13`。

### 5.4 高亮与注解

- 复杂表达式高亮由 `ParadoxExpressionSupportFactory.annotateComplexExpression` 递归完成；节点优先使用 `getAttributesKey`，当属性键是 `PROPERTY_KEY` 或 `STRING` 时优先使用 `getAttributesKeyConfig` 进行脚本表达式注解，否则使用属性键注解，然后递归子节点，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/util/ParadoxExpressionSupportFactory.kt:79`。
- `ParadoxTokenNode` 在非单元测试模式下会把 `HighlighterColors.TEXT` 与节点属性键合并，避免只覆盖前景色导致文本属性不完整，参见同文件 `:108`。
- 语义颜色由 `ParadoxSemanticHighlighterColors` 提供，覆盖系统作用域、作用域、作用域前缀、值字段、值字段前缀、命令作用域、命令字段、命令前缀、动态值、变量、定值命名空间、定值变量、数据库对象类型、数据库对象、定义引用、本地化引用、数字、字符串、关键字、标记、运算符与参数等。
- `ParadoxMarkerManager` 定义成对标记 `()[]{}<>`，并按表达式家族限制可用标记：作用域/值/变量/命令只使用 `()`，命名格式使用 `[]{}<>`，参见 `src/main/kotlin/icu/windea/pls/lang/util/ParadoxMarkerManager.kt:13` 与 `:17`。
- `ParadoxHeavyBraceHighlighter` 在脚本/本地化文件中按光标位置查找复杂表达式标记节点并高亮成对标记；如果表达式不是复杂表达式，则退化为字符串内的水平括号匹配，参见 `src/main/kotlin/icu/windea/pls/lang/codeInsight/highlighting/ParadoxHeavyBraceHighlighter.kt:44` 与 `:83`。
- `ParadoxSemanticTypedHandler` 根据复杂表达式类型自动补全闭标记；它先解析当前表达式，再用 `ParadoxMarkerManager.isLeftMaker` 判断当前字符是否可能是开标记，参见 `src/main/kotlin/icu/windea/pls/lang/editor/ParadoxSemanticTypedHandler.kt:41`。
- 复杂表达式的单词选择处理器使用 `ParadoxComplexExpressionWordSelectionRecursiveVisitor` 按节点树扩展选择范围，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/util/ParadoxComplexExpressionVisitors.kt:34`。

### 5.5 代码检查

- 脚本侧专用基类 `IncorrectComplexExpressionInspectionBase` 在文件可用的前提下，取首个匹配规则，通过 `resolveByConfig` 解析，然后调用 `getAllErrors(element)` 注册全部错误；对 `EXPRESSION_NOT_QUOTED` 错误提供 `QuoteLiteralFix`，参见 `src/main/kotlin/icu/windea/pls/lang/inspections/script/complexExpression/IncorrectComplexExpressionInspectionBase.kt:47`。
- 脚本侧每个家族都有一个薄检查类，仅按数据类型过滤是否可用，例如 `IncorrectScopeFieldExpressionInspection` 只对 `ScopeField` 集启用，`IncorrectDynamicValueExpressionInspection` 只对 `DynamicValue` 集启用，参见 `src/main/kotlin/icu/windea/pls/lang/inspections/script/complexExpression/IncorrectScopeFieldExpressionInspection.kt:10` 与 `IncorrectDynamicValueExpressionInspection.kt:10`。这些类的 KDoc 明确“不适用于嵌套的此类复杂表达式”。
- 本地化侧专用基类 `IncorrectComplexExpressionInspectionBase` 通过 `ParadoxComplexExpression.resolve` 解析，支持命令与数据库对象；命令/数据库对象检查在不含未解析错误时提供 `EscapeCommandFix`，参见 `src/main/kotlin/icu/windea/pls/lang/inspections/localisation/complexExpression/IncorrectComplexExpressionInspectionBase.kt:40` 与 `IncorrectCommandExpressionInspection.kt:20`。
- 作用域链长度检查有脚本与本地化两个独立实现，分别统计 `ParadoxScopeNode` 与 `ParadoxCommandScopeNode` 的数量，超过 `ParadoxScopeManager.maxScopeLinkSize` 时报告问题，参见 `src/main/kotlin/icu/windea/pls/lang/inspections/script/scope/IncorrectScopeLinkChainInspection.kt:63` 与 `src/main/kotlin/icu/windea/pls/lang/inspections/localisation/scope/IncorrectScopeLinkChainInspection.kt:50`。
- 通用“不正确表达式”检查器另有基于复杂表达式的检查：作用域字段输出作用域/作用域分组校验、整数/浮点值字段的范围校验使用 `ParadoxComplexExpressionEvaluator`，参见 `src/main/kotlin/icu/windea/pls/ep/inspections/ParadoxIncorrectExpressionCheckers.Basic.kt:110`、`:136`、`:163` 与 `:197`。
- 错误对象 `ParadoxComplexExpressionError` 用数字区间区分未解析错误（`1..100`）与格式错误（`101..200`）；`register` 按区间选择高亮类型，并把表达式内范围平移到元素上，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/util/ParadoxComplexExpressionError.kt:10` 与 `:19`。
- 错误码表中还包含数组定值相关的 `190/191`、取反标签的 `192` 与未加引号的 `201`，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/util/ParadoxComplexExpressionErrors.kt:6`。

### 5.6 语义匹配

- 脚本 matcher 按数据类型分派：动态值、作用域字段、值字段、变量字段、命令、脚本值引用、定值引用、数组定值引用、标签集、数据库对象、命名格式、模板各有 `ForXxx` 实现，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:121` 起。
- 值字段/变量字段先处理数值并做范围检查，只有非数值文本才进入复杂表达式解析；作用域字段、定值、数组定值、标签集、数据库对象、命名格式、模板都直接调用 `ParadoxMatchResultFactory` 的对应方法。
- `forScopeFieldExpression` 等方法先解析复杂表达式，若 `getAllErrors()` 非空则返回 `PartialMatch`，否则按家族语义继续匹配；`getAllErrors()` 不带元素，因此依赖元素的语义未解析错误不会参与该判断，参见 `src/main/kotlin/icu/windea/pls/lang/match/util/ParadoxMatchResultFactory.kt:307`。
- `forComplexExpressionFromAttributes` 对链式表达式只检查最后一个链接节点的综合属性；`lenientDynamicDataInvolved` 返回 `LenientWildcardMatch`，`dynamicDataInvolved` 返回 `WildcardMatch`，否则返回 `ExactMatch`，参见 `src/main/kotlin/icu/windea/pls/lang/match/util/ParadoxMatchResultFactory.kt:367`。
- `ParadoxComplexExpressionAttributesEvaluator` 只定义两个标志：`DYNAMIC_DATA_INVOLVED`（出现任意 `ParadoxDynamicDataNode`）与 `LENIENT_DYNAMIC_DATA_INVOLVED`（动态数据节点位于最后一个链接的唯一子值中），参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/attributes/ParadoxComplexExpressionAttributes.kt:17` 与 `ParadoxComplexExpressionAttributesEvaluator.kt:19`。
- `DynamicValue` matcher 返回 `FallbackMatch`；`Command` matcher 返回 `NotMatch`；`Tags` 空字符串返回 `FallbackMatch`；`Template` 先快速匹配，必要时再做惰性模板匹配，参见 `src/main/kotlin/icu/windea/pls/ep/match/expression/ParadoxScriptExpressionMatchers.Core.kt:121`、`:209`、`:254` 与 `ParadoxMatchResultFactory.kt:261`。
- `CwtDataTypeSets.DynamicValueInvolved` 还用于声明类规则的属性（`involveDynamicValue`）与索引优化，见 `src/main/kotlin/icu/windea/pls/config/match/CwtConfigExpressionMatchService.kt:12` 与 `src/main/kotlin/icu/windea/pls/config/attributes/CwtDeclarationLikeConfigAttributes.kt:7`。

### 5.7 作用域上下文评估

- `ParadoxScopeService.evaluateScopeContextForExpression` 遍历作用域字段表达式的作用域节点，逐步更新作用域上下文，并记录链接节点与对应上下文的列表；遇到错误作用域节点时停止，参见 `src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:385`。
- `evaluateScopeContextForNode` 按节点类型分派：参数化/系统/静态/动态作用域节点分别处理；命令作用域节点处理本地化提升；命令字段节点不改变作用域；错误节点返回未知上下文，参见同文件 `:400`。
- 命令字段的支持作用域由 `evaluateSupportedScopesForNode` 提供：静态命令字段返回 `config.supportedScopes`，其他命令节点/字段返回任意作用域，参见同文件 `:572`。
- 动态值的作用域上下文由 `ParadoxDynamicValueLightElement` 和 `ParadoxScopeManager.getScopeContext` 评估；如果存在外部 EP 提供的精确上下文则优先使用，否则合并推断上下文，参见 `src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:354`。
- 其他 EP 也会用作用域字段表达式推断上下文，例如 `ParadoxTriggerWithParametersAwareOverriddenScopeContextProvider` 处理 `complex_trigger_modifier`/`export_trigger_value_to_variable` 的 `trigger_scope`，参见 `src/main/kotlin/icu/windea/pls/ep/scope/ParadoxOverriddenScopeContextProviders.kt:92`。

### 5.8 代码补全

- `ParadoxComplexExpressionCompletionManager` 为每个家族提供入口方法；入口方法统一用 `markIncomplete` 设置完整性线程状态，并传入关键字范围解析表达式，参见 `src/main/kotlin/icu/windea/pls/lang/codeInsight/completion/ParadoxComplexExpressionCompletionManager.kt:46`。
- 补全按节点树游标推进：遍历表达式节点，如果光标位于节点之前则中止，位于节点之后则根据节点类型切换作用域上下文，位于节点范围内则按节点类型分派具体补全，参见同文件 `:157` 与 `:179`。
- 作用域字段/值字段/变量字段的补全会逐个经过作用域节点并更新 `ParadoxScopeContext`；值字段/变量字段会在末尾节点解析为作用域节点或值字段节点后分别补全，参见同文件 `:179` 与 `:208`。
- 动态值补全处理嵌套作用域字段；脚本值引用补全处理脚本值节点与参数节点；定值/数组定值补全处理命名空间、变量与索引；标签集补全处理取反；数据库对象补全处理类型、值、强制基础值与交换值；命名格式补全处理定义、本地化、命令与文本；模板补全处理片段与常量，参见同文件 `:399`、`:425`、`:469`、`:507`、`:540`、`:566`、`:610`。
- 补全层依赖节点结构而非重新实现语法扫描，因此节点形状或范围变化会直接影响补全；这是未来重构必须保持或显式迁移的契约。

### 5.9 表达式评估

- `ParadoxComplexExpressionEvaluator` 先从规则取数据表达式类型，要求属于 `CwtDataTypeSets.Evaluatable`，再通过 `findExpression` 找到最终表达式；对链式表达式取最后一个链接节点的最后一个值节点的唯一子节点，对定值/数组定值直接返回自身，参见 `src/main/kotlin/icu/windea/pls/lang/util/evaluators/ParadoxComplexExpressionEvaluator.kt:29`。
- `findExpression` 只把 `ParadoxDefineReferenceExpression` 与 `ParadoxArrayDefineReferenceExpression` 视为可评估结果；`evaluateExpression` 也只分派这两个评估器，参见同文件 `:42` 与 `:57`。
- `ParadoxDefineReferenceExpressionEvaluator` 解析变量节点，读取 `propertyValue`，并按需调用 `resolved()`；`ParadoxArrayDefineReferenceExpressionEvaluator` 检查数组定义、索引并读取数组值，参见 `src/main/kotlin/icu/windea/pls/lang/util/evaluators/ParadoxDefineReferenceExpressionEvaluator.kt:55` 与 `ParadoxArrayDefineReferenceExpressionEvaluator.kt:56`。
- `CwtDataTypeSets.Evaluatable` 包含 `ScriptValueReference`、`Command` 与 `ValueField`，但当前没有脚本值引用评估器；`ScriptValueReferenceEvaluatable` 未被使用，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:144` 与 `:162`。

### 5.10 其他消费者

- `CwtInComplexExpressionRelatedConfigProvider` 根据光标偏移找到复杂表达式节点，并把节点 `getRelatedConfigs()` 累加到相关规则中，供快速文档等使用，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/config/CwtRelatedConfigProviders.kt:153`。
- `ParadoxComplexExpressionNodeParameterInferredConfigProvider` 根据参数节点在复杂表达式中的位置推断上下文规则：数据源节点取链接配置，动态值节点取动态值配置，脚本值节点取脚本值配置，作用域节点取 `scope_field`，值字段节点取 `value_field`，参数值节点取传入参数的推断上下文，参见 `src/main/kotlin/icu/windea/pls/ep/resolve/parameter/ParadoxParameterInferredConfigProviders.kt:173`。
- `ParadoxSyntaxService.isSafeAssignOperatorValid` 用复杂表达式判断属性键是否为链式表达式，从而决定安全赋值运算符是否有效，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/ParadoxSyntaxService.kt:155`。
- `ParadoxScopeCallStatementManipulationService` 用 `ParadoxLinkedExpression` 判断普通形式、安全形式、嵌套形式与链式形式，并进行相应的语句操作，参见 `src/main/kotlin/icu/windea/pls/lang/manipulation/ParadoxScopeCallStatementManipulationService.kt:86` 与 `:159`。
- `ParadoxMarkerManager`、`ParadoxSemanticTypedHandler`、`ParadoxHeavyBraceHighlighter` 与单词选择处理器共同提供编辑器交互；它们只消费节点类型与标记节点，不重新解析表达式，参见 5.4。

## 6. 现行设计约束与已知痛点

### 6.1 结构性约束

- **分派硬编码**：复杂表达式家族由私有 `ParadoxComplexExpressionResolver` 的 `when` 决定；没有 EP、注册表或工厂扩展点，新增家族必须同时改动解析器、数据表达式、matcher、support、completion、inspection、测试与文档。
- **解析逻辑重复**：作用域字段、值字段、变量字段、命令各自实现一套字符扫描；点号切分、括号深度、参数区间、`|` 屏障、`value:` 屏障检查位置在多处重复，参见 5.2。
- **没有共享 Token 模型**：当前没有 Token/TokenStream/Scanner 抽象；解析结果直接是节点树，解析器自行处理字符与偏移；`proposals/complex-expression-refactors.md:12` 把 `Token`、`TokenStream`、`Scanner`、`Resolver`、`Visitor`、`Validator`、`Evaluator` 列为重构关键词。
- **完整性模式是线程状态**：`ParadoxThreadContext.incompleteComplexExpression` 是 `ThreadLocal<Boolean>`，补全临时设置；所有解析器都必须手动检查它；它不是解析参数，也没有作用域隔离或嵌套恢复语义，参见 `src/main/kotlin/icu/windea/pls/lang/ParadoxThreadContext.kt:24`。
- **坐标基准混用**：正常解析以表达式文本为基准，补全调用传入 `range` 后以 `range.startOffset` 为基准；消费侧统一通过表达式偏移与元素偏移换算；这要求每个节点、错误与引用实现都遵守同一约定，参见 4.3。
- **父节点延迟绑定**：节点父链在 `finishResolution()` 时才建立；综合属性评估器依赖父链判断“最后一个链接的唯一动态值”；直接构造节点而不完成解析会得到不同属性，参见 4.3 与 5.6。

### 6.2 语义与实现边界

- **`ScriptValueReference` 不在通用分派中**：`resolveByConfig` 没有该类型分支；它主要作为值字段的数据源由值节点构造，或由 matcher 直接调用。EP 注册了 `ForScriptValueReference`，但其 `annotate`/`getReferences` 走通用入口，顶层配置类型不会得到复杂表达式；`IncorrectScriptValueReferenceExpressionInspection` 同样走通用入口，参见 3.7、5.2 与 `src/main/kotlin/icu/windea/pls/lang/inspections/script/complexExpression/IncorrectScriptValueReferenceExpressionInspection.kt:10`。
- **`Command` 仅占位**：数据表达式 `$command` 的 KDoc 明确“目前仅作占位，不支持匹配脚本文件中的表达式”；脚本 matcher 返回 `NotMatch`；通用解析器却不限制语言，形成“解析层可构造、匹配层不可用”的不对称，参见 3.6 与 `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:547`。
- **Jomini 数据函数未建模**：Vic3/CK3 的 `Concept(...)`、`Concatenate(...)`、`SelectLocalization(...)`、`AddTextIf(...)` 等嵌套函数调用与单引号实参不在当前命令表达式模型中；这属于 `documents/notes/proposals/localisation-command-advanced-semantic-support.md` 的计划范围，参见该文件 `:11` 与 `:26`。
- **定值/数组定值未处理参数区间**：两个解析器都有“3.1.0+ compatible with parameter ranges”的 TODO；当前使用 `text.indexOf('|')`，参数文本中的 `|` 可能被误认为分隔符，参见 3.8 与 3.9。
- **命名格式文档与代码不一致**：代码使用 `${formatName}_name_parts_list`，而 `CwtDataTypes.NameFormat` KDoc 与 `docs/zh/ref-config-format.md` 写的是 `{name}_name_format`；设计文档必须以代码实际行为为准，并显式标注该差异，参见 3.12。
- **数组定值索引类型不一致**：接口属性 `indexNode` 是 `ParadoxNumberLiteralNode?`，解析器却可能构造 `ParadoxStringLiteralNode`；字符串索引在校验器中可被检查，但评估器会因 `indexNode` 为 `null` 直接返回 `null`，参见 3.9。
- **动态值匹配过度宽松**：matcher 只看 `@` 之前的名称与配置元数据，不验证 `@` 之后的链；真正的结构校验发生在解析器与检查器；匹配与检查的严格度不同，参见 3.5 与 5.6。
- **综合属性只有两个标志**：`ParadoxComplexExpressionAttributes` 目前只表达“涉及动态数据”和“宽松动态数据”；`ideas.md` 记录了“提取复杂表达式的综合属性评估器”已完成，但如果未来需要更精细的匹配、索引或补全策略，当前属性模型可能不足，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/attributes/ParadoxComplexExpressionAttributes.kt:17` 与 `documents/notes/ideas.md:132`。
- **错误收集与嵌套**：`getAllErrors` 先取根错误，再递归收集节点未解析错误；`validateAllNodes` 在遇到嵌套复杂表达式时调用其 `getErrors`，同时继续递归其子节点；理论上存在嵌套错误被多层收集的风险，设计文档应明确错误归属与去重策略，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpressionBase.kt:22` 与 `util/ParadoxComplexExpressionValidatorScope.kt:12`。
- **错误码区间约定**：未解析错误 `1..100`、格式错误 `101..200`，但数组定值的 `190/191` 与取反标签的 `192` 落在格式错误区间，`EXPRESSION_NOT_QUOTED=201` 落在区间之外；高亮类型依赖区间，新增错误码需要遵守该约定，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/util/ParadoxComplexExpressionErrors.kt:7`。
- **引号检查范围有限**：脚本侧只对 `(`、`)`、`,` 检查是否需要双引号；`|`、`$`、`@` 等特殊字符不在该检查范围内，参见 `util/ParadoxComplexExpressionValidatorScope.kt:30`。
- **模板表达式缺少家族级校验器**：`ParadoxTemplateExpressionImpl` 没有覆盖 `getErrors`，模板错误主要来自未解析的引用片段；模板结构本身是否完整由匹配服务决定，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxTemplateExpression.kt:99`。
- **命名格式解析器体量较大**：`ParadoxNameFormatExpression.kt` 约 452 行，内部包含多个局部函数（括号匹配、花括号匹配、常量/本地化/定义片段构建、内容解析、顶层解析），是当前家族中最难独立维护的解析器，参见 `src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxNameFormatExpression.kt:81`。
- **屏障启发式**：四个链式解析器都用 `text.lastIndexOf("value:")` 决定 `|` 屏障检查位置；`root.owner|x.y` 的测试把 `owner|x.y` 整体视为错误作用域节点，说明该行为是被测试固定的现行契约，参见 `src/test/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScopeFieldExpressionTest.kt:80`。
- **`ScriptValueReferenceEvaluatable` 未使用**：该集合定义后没有消费者；与 `Evaluatable` 的关系不清晰，设计文档应确认它是遗留物还是未来评估器的预留契约，参见 `src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:145`。
- **空字符串语义分散**：`Tags` 在解析器中对空文本返回 `null`，在 matcher 中对空文本返回 `FallbackMatch`；`DynamicValue`、`ValueField` 等对空文本也有各自的完整性模式处理；空值语义不是一个统一入口，参见 3.10 与 5.6。

### 6.3 文档与外部认知差异

- `CwtDataTypes.NameFormat` 的定义类型描述与代码不一致，见 3.12。
- `CwtDataTypes.Command` 的 KDoc 已经承认“仅占位”，但 EP、检查和补全仍然注册了命令支持，容易让读者误以为脚本命令匹配已可用，参见 3.6。
- `ScriptValueReference` 的 KDoc 声称“评估结果应是一个数字字面量”，但当前没有对应评估器；这也是文档承诺与实现能力不一致的例子，参见 3.7。
- CWTools 官方 Wiki 将 `value[key]`/`value_set[key]` 描述为“值集合”（由使用处收集候选值），而插件将 `Value`/`ValueSet` 用于动态值读/写；同名但语义不同，见第 8 节。
- Vic3/CK3 官方 Wiki 的“data functions/data types”包含嵌套函数调用与参数，而插件命令表达式只支持点号链式命令字段；这是能力差异，不是简单命名差异，见第 8 节。

## 7. 未来重构蓝图边界

### 7.1 与 `proposals/complex-expression-refactors.md` 的界线

- `documents/notes/proposals/complex-expression-refactors.md` 的版本是 `v3.1.x+`，状态是“待规划”；其候选任务包括重构复杂表达式 API、把相关代码迁移到 `icu.windea.pls.expression`、优化动态链接节点中的字面量处理、支持本地化命令的高级复杂表达式与单引号字面量，参见该文件 `:3`、`:18`、`:19`、`:20`。
- 当前 `src/main/kotlin/icu/windea/pls/expression/` 只包含 `text/ParadoxLiteralNodeQuotePattern.kt`，提案中的迁移目标尚未落地，不能把 `icu.windea.pls.expression` 写成复杂表达式现有主目录。
- 这些任务**不是** v3.1.0 的现行契约；本报告描述的 12 个家族、现有节点树、错误码、补全行为与匹配行为才是 v3.1.0 快照的事实。
- 设计文档应把“现行契约”与“重构目标”分成两个独立章节，避免把提案中的 `Token`/`TokenStream`/`Scanner` 写成已存在；`ideas.md` 也把“重构复杂表达式的 API，尤其是解析逻辑与校验逻辑”标记为 `P2/MS 3.1.0+` 的待办，参见 `documents/notes/ideas.md:217`。
- 重构提案中的“动态链接字面量处理”对应 `ideas.md` 的 `P2/MS 3.1.0+ 优化对复杂表达式的支持：优化对动态链接节点中传入的字面量的处理逻辑`，参见 `documents/notes/ideas.md:236`。
- 本地化命令高级语义提案对应 Issue #137、#451，计划建立 `ParadoxLocalisationFunctionCallExpression` 等新节点，参见 `documents/notes/proposals/localisation-command-advanced-semantic-support.md:56`；该模型目前不存在。
- `localisation-syntax-advanced-rich-text.md` 与 `script-syntax-advanced-interpolations.md` 属于语法支持层提案，不是复杂表达式重构提案；当前复杂表达式不处理颜色、图标、文本格式、文本图标、内联条件化块与 Hoi4 通配参数，参见 `documents/notes/proposals/localisation-syntax-advanced-rich-text.md:15` 与 `documents/notes/proposals/script-syntax-advanced-interpolations.md:10`。

### 7.2 设计文档必须保留的现行契约

- 12 个家族的触发数据类型与语言上下文，见 3.1。
- `ParadoxComplexExpression.resolveByConfig` 的分派顺序与 `ScriptValueReference` 不在通用分派中的事实，见 5.2 与 3.7。
- 节点树抽象、链接节点工厂回退顺序、父节点延迟绑定与范围基准，见 4.1 至 4.3。
- 节点级 `getAttributesKey`/`getAttributesKeyConfig`/`getReference`/`getUnresolvedError` 的职责与消费方式，见 5.3 至 5.5。
- 错误码区间、错误注册时的范围平移、脚本/本地化引号与格式校验差异，见 5.5 与 6.2。
- matcher 对数值、空字符串、动态值、命令、模板的不同处理，见 5.6。
- 补全依赖节点树与作用域切换的事实，见 5.8。
- 评估器只覆盖定值引用与数组定值引用的事实，见 5.9。
- `ScriptValueReference` 缺少通用入口与评估器、`Command` 仅占位、`NameFormat` 文档与代码不一致等已知差异，见 6.2。

### 7.3 建议设计文档明确回答的问题

- 新家族如何注册：继续使用 `when`，还是引入显式工厂/EP；如果引入，现行 12 个家族如何迁移。
- 坐标基准是否统一为“表达式内相对坐标”，还是保留“调用方可传入基准范围”的能力；如果保留，如何在接口文档中固定约定。
- 完整性模式是否改为显式解析选项；如果是，`ThreadLocal` 的兼容层如何退出。
- 综合属性是否需要扩展；如果需要，哪些消费者依赖现有两个标志，哪些可以改为按节点查询。
- `ScriptValueReference` 的最终定位：是仅作为数据源表达式，还是也应支持顶层配置、引用、高亮、检查与评估。
- `Command` 与 Jomini 数据函数的关系：现行 `CommandExpression` 是否作为简单链式命令保留，还是被新的函数调用模型替换或包裹。
- 命名格式的定义类型、错误节点与不完整输入契约，尤其是 `_name_parts_list` 与文档 `_name_format` 的冲突。
- 模板表达式与片段数据类型的边界：模板片段是否继续使用 `CwtValueConfig.mock`，片段匹配是否继续独立于复杂表达式。
- 错误收集与嵌套表达式的唯一归属；是否需要错误去重或分层报告。

## 8. 官方 Wiki 与外部工具对照差异

### 8.1 对照表

| 主题 | 插件现行命名/模型 | 官方 Wiki | CWTools 官方 Wiki | 差异说明 |
| --- | --- | --- | --- | --- |
| 复杂表达式本身 | `ParadoxComplexExpression`、`ParadoxComplexExpressionNode` | 无统一术语；按“scope/command/data function”等分散描述 | 无统一术语；用“简单规则/链接规则/值集合”描述 | “复杂表达式”是插件架构术语，不是官方或 CWTools 的正式术语。 |
| 链式作用域 `root.owner` | `ParadoxScopeFieldExpression`，节点为 `ParadoxScopeNode` | Stellaris Wiki 称“scope switch”“dot-scoping”；Vic3 Wiki 称“event targets（also called Scope Links）”“chaining scopes” | 称 `scope_field`，`scope[scope_key]` 等价于 `event_target[scope_key]` | 插件把整条链称为“作用域字段表达式”，官方更强调“从当前作用域切换到另一个作用域”的动作；CWTools 的 `scope_field` 是通用匹配类型。 |
| 系统作用域 `THIS`/`PREV`/`ROOT`/`FROM` | `ParadoxSystemScopeNode`、`ParadoxSystemCommandScopeNode`，配置来自 `systemScopes` | Stellaris Wiki 明确称为“system scopes” | CWTools 指引未单列系统作用域，而是在 `scopes.cwt`/链接规则中描述 | 插件把系统作用域作为独立节点子类并绑定配置；官方把它作为特殊系统作用域；CWTools 不以节点子类表达。 |
| 动态链接 `event_target:x` | `ParadoxDynamicScopeNode`（前缀形式 `prefix:data`） | Vic3 Wiki 称“contextual/specified event targets”；Stellaris Wiki 称“event targets” | `links.cwt` 使用 `from_data`、`data_source`、`prefix`、`input_scopes`、`output_scope` | 插件遵循 CWTools 的链接配置模型，并映射为动态作用域节点；官方 Wiki 不使用 CWT 配置字段描述。 |
| 传参链接 `relations(x)` | `ParadoxDynamicScopeNode`/`ParadoxDynamicValueFieldNode`/`ParadoxDynamicCommand*`，支持 `from_argument` 与 `argument_separator` | Vic3/CK3 的 event target/data function 可以使用括号参数 | CWTools 指引提到 `data_source` 可多个以表示多传参，但未把 `func(arg)` 作为独立链接语法 | 插件的 `from_argument`、`argument_separator` 是扩展；CWTools 主要通过多个 `data_source` 描述多参数。 |
| 作用域组 `scope_group` | `ParadoxScopeFieldExpression`（数据表达式 `scope_group[name]`） | 官方 Wiki 只使用 scope/scope types 等术语，不使用 `scope_group` 数据类型 | CWTools 指引未单列 `scope_group` 数据类型 | 插件提供输出作用域分组的约束；这是插件/CWTools 规则体系的扩展，不能直接映射到官方术语。 |
| 值字段 `root.value:sv` | `ParadoxValueFieldExpression`，数据表达式 `value_field`/`int_value_field` | Vic3 Wiki 有“value triggers”与值比较；Stellaris Wiki 分散在变量/触发器值中 | `value_field`、`int_value_field` | 插件名称与 CWTools 对齐；官方没有“值字段表达式”这一统一术语。 |
| 变量字段 `root.var` | `ParadoxVariableFieldExpression`，数据表达式 `variable_field`/`int_variable_field` | Stellaris Wiki 称“variables”，支持点号作用域与 `var`/`local_var` | `variable_field`、`int_variable_field` | 插件把变量字段视为值字段的子集；官方把变量视为可读写的数据；CWTools 用 `variable_field` 做匹配。 |
| 动态值 `target`/`target@root` | `ParadoxDynamicValueExpression`，数据表达式 `value`（读）、`value_set`（写）、`dynamic_value`（读写不区分） | Stellaris Wiki 称“event targets”“saved scopes”“variables”；Vic3 Wiki 称“saved scopes” | `value[key]`/`value_set[key]` 被描述为“值集合”（由使用处收集候选值） | 这是最显著的同名异义：CWTools 的 `value[key]`/`value_set[key]` 是值集合，不是动态值读/写；插件重解释为动态值读/写并增加 `dynamic_value`。 |
| 命令 `[Root.GetName]` | `ParadoxCommandExpression`，数据表达式 `$command` | Stellaris Wiki 称“Bracket Commands（scoped localization statements）”“Primary/Secondary Scopes”“Text Retrieval”；Vic3 Wiki 称“Data functions/localization functions”；CK3 Wiki 称“Data Types” | CWTools 的 `localisation.cwt` 只列 `localisation_commands`，并注明仅用于 pre-Jomini | 插件命令表达式只支持点号链式的作用域/字段；官方命令/数据函数支持函数参数、单引号实参、返回类型；CWTools 对 Jomini 数据函数也没有同等语义模型。 |
| 脚本值引用 `sv\|PARAM\|VALUE\|` | `ParadoxScriptValueReferenceExpression`，数据表达式 `$script_value_reference` | 官方 Wiki 的 script value/本地化参数语法与插件语法不同；Vic3 使用 `ScriptValue('name')` | CWTools `links.cwt` 通过 `data_source` 引用该类型，但简单规则指引未把它列为通用表达式 | 插件语法与官方 Vic3 `ScriptValue('...')` 不是同一形式；插件当前也缺少评估器。 |
| 定值/数组定值 `Namespace\|Name` | `ParadoxDefineReferenceExpression`、`ParadoxArrayDefineReferenceExpression` | 官方 Wiki 的 defines/脚本常量语境不以复杂表达式描述 | CWTools 简单规则指引未列对应数据类型 | 插件扩展；CWTools 不兼容（插件 KDoc 明确“不兼容，插件作为扩展提供”）。 |
| 标签集 `tag1,not(tag2)` | `ParadoxTagsExpression`，数据表达式 `$tags`/`$tags_condition` | 官方 Wiki 用 flag 相关 trigger/effect 描述，不使用“tags expression”术语 | CWTools 简单规则指引未列该表达式 | 插件扩展；语义对应官方 flag 检查，但不是官方统一表达式。 |
| 数据库对象 `civic:x:y` | `ParadoxDatabaseObjectExpression`，数据表达式 `$database_object` | Stellaris/CK3 称“game concepts”“data types”；Vic3 称“Concepts” | CWTools 有 `database_object_types`，但简单规则指引未把它作为复杂表达式 | 插件把“类型:值[:交换值]”作为表达式，并同时支持定义与本地化解析；官方概念语法更窄。 |
| 命名格式 `{<adj> {<noun>}}` | `ParadoxNameFormatExpression`，数据表达式 `name_format`/`$name_format` | Stellaris Wiki 使用 name lists/name formats 的领域概念；Vic3/CK3 无同名复杂表达式 | CWTools 用 `type[...]` 与本地化规则描述名称格式，不使用 `name_format` 表达式 | 插件扩展；其定义占位类型为 `${format}_name_parts_list`，而插件文档写成 `_name_format`。 |
| 模板 `a_<b>_c` | `ParadoxTemplateExpression`，数据表达式 `Template` | 官方 Wiki 无对应统一术语 | CWTools 使用 `prefix_<type_key>_suffix` 形式 | 插件模板支持常量/动态片段交替，比 CWTools 的简单前后缀更一般化；`DefinitionSnippet`/`LocalisationSnippet` 是插件扩展。 |
| 本地化引用 | `Localisation`、`SyncedLocalisation`、`InlineLocalisation` | 官方 Wiki 用 `$key$`、本地化键、`localisation` 目录描述 | CWTools 有 `localisation`、`localisation_synced`、`localisation_inline` | 插件与 CWTools 更接近；官方 Wiki 不区分这三种数据表达式。 |
| 参数/本地化参数 | `Parameter`、`ParameterValue`、`LocalisationParameter` | 官方 Wiki 用 `$PARAM$`、`$name\|arg$`、`$@var$` 描述 | CWTools 简单规则未列这些插件扩展类型 | 插件把参数名/参数值/本地化参数名拆成不同数据类型；官方只按语法位置描述。 |

### 8.2 对照结论

- **术语层面**：插件的 `ScopeField`、`ValueField`、`VariableField`、`Localisation` 等名称主要对齐 CWTools；官方 Wiki 更偏向游戏领域词（scope、event target、data function、data type），“复杂表达式”本身没有官方对应词。
- **语义层面**：`Value`/`ValueSet` 与 CWTools 值集合存在同名异义；`DynamicValue`、`DatabaseObject`、`NameFormat`、`Template`、`Tags`、`DefineReference`、`ArrayDefineReference` 是插件扩展或重解释；`Command` 与官方 data function/data type 能力差距最大。
- **覆盖层面**：官方 Wiki 的 data function 支持嵌套调用、参数、返回类型与单引号字面量；插件当前只支持点号链式命令字段，并把命令限制在本地化上下文与命名格式内部。
- **配置层面**：CWTools 的 `links.cwt` 用 `from_data`/`data_source`/`prefix`/`input_scopes`/`output_scope` 描述动态链接；插件复用这些字段并增加 `from_argument`、`argument_separator`；官方 Wiki 不使用 CWT 配置术语，因此对照时应以“语义角色”而不是字段名比较。
- **文档层面**：命名格式定义类型与 `ScriptValueReference` 评估承诺是两个已确认的文档-代码差异；设计文档应把“插件实际行为”与“官方/CWTools 术语”分别列出，不应为了对齐官方 Wiki 而直接重命名现行代码。

## 9. 证据索引

### 9.1 代码入口

- 通用接口与分派：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpression.kt:40`、`:76`、`:102`。
- 基类与父节点绑定：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpressionBase.kt:10`。
- 节点接口与基类：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxComplexExpressionNode.kt:30`、`ParadoxComplexExpressionNodeBase.kt:3`。
- 综合属性：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/attributes/ParadoxComplexExpressionAttributes.kt:17`、`ParadoxComplexExpressionAttributesEvaluator.kt:19`。
- 错误定义：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/util/ParadoxComplexExpressionError.kt:10`、`ParadoxComplexExpressionErrors.kt:6`。
- 校验作用域与访问者：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/util/ParadoxComplexExpressionValidatorScope.kt:11`、`ParadoxComplexExpressionVisitors.kt:13`。
- DSL：`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/dsl/ParadoxComplexExpressionDsl.kt:9`、`ParadoxComplexExpressionDslChecker.kt:6`、`ParadoxComplexExpressionDslRenderer.kt:5`。
- 数据类型：`src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:369`（Value）、`:385`（ValueSet）、`:401`（DynamicValue）、`:418`（ScopeField）、`:435`（Scope）、`:451`（ScopeGroup）、`:472`（ValueField）、`:492`（IntValueField）、`:516`（VariableField）、`:539`（IntVariableField）、`:557`（Command）、`:573`（ScriptValueReference）、`:589`（DefineReference）、`:605`（ArrayDefineReference）、`:623`（Tags）、`:639`（DatabaseObject）、`:655`（NameFormat）、`:728`（Template）。
- 数据类型分组：`src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:39`（DynamicValue）、`:44`（ScopeField）、`:51`（ValueField）、`:56`（VariableField）、`:144`（ScriptValueReferenceEvaluatable）、`:162`（Evaluatable）、`:171`（DynamicValueInvolved）。
- 消费入口：`src/main/kotlin/icu/windea/pls/ep/resolve/expression/ParadoxScriptExpressionSupports.ComplexExpression.kt:33`、`ParadoxLocalisationExpressionSupports.ComplexExpression.kt:23`、`src/main/kotlin/icu/windea/pls/lang/match/util/ParadoxMatchResultFactory.kt:284`、`src/main/kotlin/icu/windea/pls/lang/resolve/util/ParadoxExpressionSupportFactory.kt:79`、`src/main/kotlin/icu/windea/pls/lang/codeInsight/completion/ParadoxComplexExpressionCompletionManager.kt:43`、`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:385`、`src/main/kotlin/icu/windea/pls/ep/inspections/ParadoxIncorrectExpressionCheckers.Basic.kt:110`。
- 编辑器交互：`src/main/kotlin/icu/windea/pls/lang/util/ParadoxMarkerManager.kt:13`、`src/main/kotlin/icu/windea/pls/lang/editor/ParadoxSemanticTypedHandler.kt:41`、`src/main/kotlin/icu/windea/pls/lang/codeInsight/highlighting/ParadoxHeavyBraceHighlighter.kt:44`。

### 9.2 测试

- 测试基类：`src/test/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxComplexExpressionTest.kt:13`。
- 家族测试：同目录下 12 个 `ParadoxXxxExpressionTest.kt`。
- 测试配置：`src/test/testData/features/complexExpression/.config/core/`。
- 测试数据：`src/test/testData/features/complexExpression/common/scripted_triggers/complex_vic3.test.txt` 与 `src/test/testData/features/complexExpression/localisation/complex_localisation_links.yml`。

### 9.3 维护者文档与参考文档

- `documents/notes/proposals/complex-expression-refactors.md:3`、`:18`、`:19`、`:20`。
- `documents/notes/proposals/localisation-command-advanced-semantic-support.md:11`、`:26`、`:56`。
- `documents/notes/proposals/script-syntax-advanced-interpolations.md:10`。
- `documents/notes/proposals/localisation-syntax-advanced-rich-text.md:15`。
- `documents/notes/adr/localisation-command-lexical-defense.md:11`、`:27`、`:51`。
- `documents/notes/adr/template-expression-and-pattern-match.md:8`、`:10`。
- `documents/notes/adr/define-reference-expression-support.md:9`、`:26`。
- `documents/notes/adr/tags-expression-support.md:10`、`:18`。
- `documents/notes/adr/wildcard-data-types.md:24`、`:30`、`:42`。
- `documents/notes/adr/snippet-match.md:9`、`:11`、`:15`。
- `documents/notes/adr/data-expression-refactor.md:9`。
- `documents/notes/adr/expression-parser-and-resolve-style-refactor.md:9`。
- `documents/notes/adr/config-match-processing.md:15`、`:24`。
- `documents/notes/ideas.md:132`、`:217`、`:236`。
- `documents/notes/issues/issue-451-vic3-quote-escape-parser-break.md:9`、`:26`、`:92`。
- `docs/zh/ref-syntax.md:363`、`:369`、`:373`。
- `docs/zh/ref-config-format.md:40`、`:732`、`:738`、`:1838`、`:1842`、`:2174`、`:2224`、`:2240`、`:2266`。

### 9.4 GitHub Issues

- [#451](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/451)：Vic3 `|q` 引号转义导致本地化命令解析器崩溃；已修复，词法防御属于 v3.0.6。
- [#330](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/330)：作用域字段/值字段兼容嵌套动态值；已修复，是动态值嵌套行为的直接来源。
- [#387](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/387)：`localisation_links` 中 `data_source = value[something]` 的冲突；与动态值类型匹配优先级相关，已修复。
- [#340](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/340)：参数化本地化键引用；催生 `DefinitionSnippet`/`LocalisationSnippet` 与片段匹配，已修复。
- [#137](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137)：Vic3/CK3 特殊本地化、命令、格式与文本图标；仍是开放增强项，属于高级本地化命令提案。
- [#109](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/109)：Vic3/CK3 GUI 文件中的复杂函数表达式；仍是开放增强项，说明“复杂表达式”与“GUI 表达式”不是同一子系统。
- [#101](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/101)：Vic3 `relations(root)` 等链接比较；催生动态链接传参与比较作用域的支持，已修复。
- [#102](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/102)：Vic3 中 `scope_field` 与 `value_field` 的冲突讨论；说明两种表达式在语义匹配上并非总能严格区分。
- [#351](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/351)：SGUI 字符串中的水平括号匹配；与 `ParadoxHeavyBraceHighlighter` 的字符串回退路径相关。
- [#458](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/458)：舰船区段本地化链接丢失；属于本地化链接/定义派生属性问题，不是复杂表达式解析问题，但影响本地化命令上下文。

### 9.5 官方 Wiki 与 CWTools

- Stellaris Wiki：[Scopes](https://stellaris.paradoxwikis.com/Scopes)、[Localisation modding](https://stellaris.paradoxwikis.com/Localisation_modding)、[Variables](https://stellaris.paradoxwikis.com/Variables)。
- Victoria 3 Wiki：[Scope](https://vic3.paradoxwikis.com/Scope)、[Localization](https://vic3.paradoxwikis.com/Localization)。
- Crusader Kings III Wiki：[Localization](https://ck3.paradoxwikis.com/Localization)、[Data types](https://ck3.paradoxwikis.com/Data_types)。
- CWTools Wiki：[.cwt config file guidance](https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance)。
- CWTools 官网：[CW Tools](https://cwtools.github.io/)。
- 说明：官方 Wiki 正文通过 `r.jina.ai` 只读代理获取；CWTools Wiki 直接抓取；Tiger 独立文档未获取，因此第 8 节以 CWTools 为外部工具代表。

## 10. 待确认问题与后续验证

- `ScriptValueReference` 的最终契约：顶层配置是否需要支持，EP/检查注册是否应继续保留，是否需要评估器。
- `Command` 的定位：是保持“本地化/命名格式内部的简单链式命令”，还是成为 Jomini 数据函数模型的一部分。
- 命名格式定义类型的正确名称：`_name_parts_list` 与 `_name_format` 哪个是设计意图，是否需要统一文档或代码。
- 数组定值字符串索引的语义：字符串索引是否应被评估器接受，还是只作为兼容性格式存在。
- `Tags` 空字符串与空段的统一语义：解析器、matcher、补全与检查是否需要同一套空值策略。
- 嵌套复杂表达式的错误收集是否需要去重；当前 `getAllErrors` 与 `validateAllNodes` 的递归关系需要专门用例验证。
- 坐标基准是否应统一；如果保留两种基准，接口文档需要给出明确的调用约定与示例。
- `DynamicValue` matcher 的宽松策略是否应继续；如果收紧，是否会影响索引期间的性能与候选回退。
- `ParadoxComplexExpressionAttributes` 是否需要扩展为更完整的综合属性模型；如果需要，现有两个标志的消费者如何迁移。
- 官方 Wiki/CWTools 术语表是否应作为设计文档附录；如果使用，需要区分“官方领域术语”“CWTools 规则术语”“插件内部术语”，避免同名异义。

