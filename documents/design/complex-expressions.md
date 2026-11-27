# 复杂表达式体系设计规范

> - 版本：`v3.1.0`
> - 更新日期：`2026-11-28`
> - 状态：**现行活规范（Living Specification）**
> - 文档定位：规范 Paradox Chronicle 中由 CWT 规则数据类型驱动、在运行期动态解析的**复杂表达式（Complex Expressions）**子系统的设计架构、家族清单、节点模型、消费职责边界与现行约束。
> - 互链说明：文法层与语义层的整体映射详见 [`language-abstractions.md`](language-abstractions.md)；作用域推导与上下文传播详见 [`scope-system.md`](scope-system.md)；底层 PSI 架构详见 [`psi-architecture.md`](psi-architecture.md)。

---

## 一、复杂表达式的本质与架构定位

### 1.1 核心定义与设计意图

在 Paradox 脚本与本地化文本中，大量属性值或键并非单一标量，而是包含深层语义的复合文本结构，例如作用域链调用（`root.owner.capital_scope`）、数学公式与变量调用（`root.var:my_var`）、命令注入（`[Root.GetName]`）或带参数的脚本值（`value:my_sv|PARAM|1|`）。

在架构设计上，复杂表达式具备以下核心特征：

1. **轻量语法树而非 PSI 子树**：复杂表达式**不是** JFlex/Grammar-Kit 静态文法阶段生成的平台 AST/PSI 节点，而是**在运行期由 CWT 规则数据类型（`CwtDataType`）驱动动态构造的轻量 AST（Lightweight AST）**。
2. **细粒度语义赋能**：通过将一段物理文本切分为多个 `ParadoxComplexExpressionNode`，插件能够在不破坏原有物理 PSI 树结构的前提下，为单段文本的不同切片提供独立的**引用解析**（Reference）、**精准语法高亮**（Highlighting）、**错误标记与快速修复**（Inspection & Quick Fix）以及**代码补全**（Completion）。
3. **覆盖语言边界**：复杂表达式子系统仅覆盖 **Paradox Script**（属性键、字符串值、参数传入值）与 **Paradox Localisation**（命令文本 `ParadoxLocalisationCommandText`、概念名 `ParadoxLocalisationConceptName`）。Paradox CSV 采用独立的表格匹配体系（`ParadoxCsvExpressionMatcher`），不进入复杂表达式模型。

### 1.2 四个概念层次的区分

在阅读与维护代码时，需严格区分以下四个同名或近义层级：

- **表达式（Expression）**：脚本、本地化或 CSV 物理文件中的一段原始文本节点，对应 PSI 接口 `ParadoxExpressionElement`。
- **规则表达式（Config Expression）**：CWT 规则文件中的类型模式声明（如 `scope_field`、`value_field`、`$define_reference`），对应内存对象 `CwtDataExpression` 及其枚举 `CwtDataType`。
- **复杂表达式（Complex Expression）**：在规则驱动下将原始文本实例化出的整棵轻量语法树，对应顶层接口 `ParadoxComplexExpression`。
- **表达式节点（Complex Expression Node）**：轻量语法树中的最小组成单元，对应接口 `ParadoxComplexExpressionNode`。

---

## 二、现行 12 个复杂表达式家族清单

插件目前实现了 12 个复杂表达式家族，由 `ParadoxComplexExpressionResolver` 与各表达式伴生工厂统一分派：

### 2.1 家族清单总表

| 家族名 | 触发 CWT 数据类型 / 规则表达式 | 匹配优先级 | 典型文本示例 | 结构模式 |
| :--- | :--- | :--- | :--- | :--- |
| **`ParadoxScopeFieldExpression`** | `scope_field`、`scope[...]`、`scope_group[...]` | `40.0` | `root.owner`、`event_target:target` | 点号分隔的作用域链接链 |
| **`ParadoxValueFieldExpression`** | `value_field`、`int_value_field` | `35.0` | `root.owner.trigger:calc`、`10` | 零或多个作用域节点 + 最终值字段节点 |
| **`ParadoxVariableFieldExpression`** | `variable_field`、`int_variable_field` | `35.0` | `root.my_var` | 零或多个作用域节点 + 最终变量数据源节点 |
| **`ParadoxDynamicValueExpression`** | `value[...]`、`value_set[...]`、`dynamic_value[...]` | `30.0` | `target@root.owner` | 动态值标识符 + 可选的 `@` 与作用域链 |
| **`ParadoxCommandExpression`** | `$command`、本地化 `[...]` 命令 | `35.0` | `Root.Owner.GetName` | 点号分隔的命令作用域链 + 命令方法字段 |
| **`ParadoxScriptValueReferenceExpression`**| `$script_value_reference` | `60.0` | `my_sv\|PARAM\|1\|` | 脚本值名 + 管道符分隔的参数名与值对 |
| **`ParadoxDefineReferenceExpression`** | `$define_reference` | `60.0` | `NCharacter\|BASE_AGE` | 命名空间 + `\|` + 定值变量名（固定 3 节点） |
| **`ParadoxArrayDefineReferenceExpression`** | `$array_define_reference` | `60.0` | `NCharacter\|NAMES\|0` | 命名空间 + `\|` + 变量名 + `\|` + 索引（固定 5 节点） |
| **`ParadoxTagsExpression`** | `$tags[...]`、`$tags_condition[...]` | `60.0` | `tag_a, not(tag_b)` | 逗号分隔的动态值节点，支持 `not()` 条件取反 |
| **`ParadoxDatabaseObjectExpression`** | `$database_object`、概念名 | `60.0` | `civic:my_civic`、`type:val:swap` | 冒号分隔的类型节点 + 基础值节点 + 可选交换值 |
| **`ParadoxNameFormatExpression`** | `name_format[...]`、`$name_format[...]` | `60.0` | `{<first_name> [Root.GetName]}` | 花括号包裹，内部支持命令、定义占位与本地化 |
| **`ParadoxTemplateExpression`** | `CwtDataTypes.Template` | `72.0` | `job_<job>_add` | 模板字面量与引用片段交替出现的序列 |

### 2.2 链式表达式族（`ParadoxLinkedExpression`）

`ScopeField`、`ValueField`、`VariableField` 与 `Command` 共同实现 `ParadoxLinkedExpression` 接口，表示由点号（`.`）串联而成的链式求值结构：

1. **作用域字段表达式 (`ParadoxScopeFieldExpression`)**：
   - 由若干 `ParadoxScopeNode` 组成。
   - 节点解析遵循严格的降级匹配顺序：系统作用域（`SystemScopeNode`）→ 静态作用域（`StaticScopeNode`）→ 动态作用域（`DynamicScopeNode`）→ 参数化作用域（`ParameterizedScopeNode`）→ 错误作用域（`ErrorScopeNode`）。
   - 动态节点支持前缀传参（`event_target:x`）与函数式传参（`relations(root)`），内部参数可递归嵌套解析为子复杂表达式。
2. **值字段表达式 (`ParadoxValueFieldExpression`)**：
   - 前驱为零或多个 `ParadoxScopeNode`，末尾必须是一个 `ParadoxValueFieldNode`。
   - 若原始文本为纯浮点数字面量或带符号的参数，解析器直接短路返回 `null`，交由通用字面量规则处理。
   - 末尾值节点可进一步下钻解析为动态值、作用域字段、脚本值引用或定值引用。
3. **变量字段表达式 (`ParadoxVariableFieldExpression`)**：
   - 结构上为值字段的特化子集，前驱为作用域链，但末尾固定为单个 `ParadoxDataSourceNode`，专用且仅用于访问变量（`variable`）。
4. **命令表达式 (`ParadoxCommandExpression`)**：
   - 前驱为零或多个 `ParadoxCommandScopeNode`，末尾为一个 `ParadoxCommandFieldNode`。
   - 驱动配置来自本地化专用规则：作用域节点查 `localisation_links`，字段节点查 `localisation_commands`。
   - 注意：`ParadoxCommandExpression.scopeNodes` 返回强类型 `ParadoxCommandScopeNode` 列表。

### 2.3 复合与特定格式表达式族

1. **动态值表达式 (`ParadoxDynamicValueExpression`)**：
   - 语法格式为 `dynamic_value ("@" scope_field_expression)?`。
   - 标识符部分解析为 `ParadoxDynamicValueLightElement`；`@` 之后递归调用作用域字段表达式解析器，用于支持动态值作用域链。
2. **脚本值引用表达式 (`ParadoxScriptValueReferenceExpression`)**：
   - 语法格式为 `script_value ("|" argument_name "|" argument_value "|")*`。
   - 首节点为脚本值定义名，后续必须以奇数（`>=3`）个管道符连接参数名与参数值。
3. **定值与数组定值引用表达式 (`ParadoxDefineReferenceExpression` / `ParadoxArrayDefineReferenceExpression`)**：
   - 严格要求 3 个或 5 个节点。
   - 数组索引节点 `indexNode` 在代码中暴露为 `ParadoxLiteralNode?`，允许容忍非整数形式的畸形输入并在校验阶段报错。
4. **标签集表达式 (`ParadoxTagsExpression`)**：
   - 逗号分隔的动态值序列，支持空白与空段。在条件变体（`$tags_condition`）下允许出现 `not(tag)` 结构，并解析为 `ParadoxNegatedDynamicValueNode`。
5. **数据库对象表达式 (`ParadoxDatabaseObjectExpression`)**：
   - 语法为 `type ":" value (":" swapped_value)?`。
   - 类型节点查 `configGroup.databaseObjectTypes`，支持指向定义或本地化文本，支持基础值与交换值（Swapped value）语义。
6. **命名格式表达式 (`ParadoxNameFormatExpression`)**：
   - 顶层必须由 `{}` 包裹。内部支持 `[...]` 命令节点、`<name>` 命名部件节点（`ParadoxNamePartNode`）以及本地化标识符。
   - **代码事实修正**：命名部件 `<name>` 对应的实际定义类型固定为 `${formatName}_name_parts_list`（而非早期文档中提及的 `${formatName}_name_format`）。
7. **模板表达式 (`ParadoxTemplateExpression`)**：
   - 按照模板规则中的常量串与类型槽将文本交替切分为 `ParadoxTemplateSnippetConstantNode` 与 `ParadoxTemplateSnippetNode`，片段通过模拟规则（`CwtValueConfig.mock`）接入语义解析。

---

## 三、节点对象模型与消费职责边界

### 3.1 通用节点对象模型

所有复杂表达式及其内部子节点均继承自 `ParadoxComplexExpressionNode`：

```text
ParadoxComplexExpressionNode
├─ text: String                                    // 节点原始切片文本
├─ rangeInExpression: TextRange                    // 相对整个复杂表达式的偏移区间
├─ configGroup: CwtConfigGroup                     // 所属规则分组
├─ nodes: List<ParadoxComplexExpressionNode>       // 子节点集合
├─ parent: ParadoxComplexExpressionNode?           // 父节点弱引用
├─ getRelatedConfigs(): List<CwtMemberConfig<*>>   // 节点关联的 CWT 规则
├─ getAttributesKey(): TextAttributesKey?          // 节点高亮属性键
├─ getReference(element): PsiReference?            // 节点承载的代码引用
├─ getUnresolvedError(element): ParadoxComplexExpressionError? // 节点未解析错误
└─ accept(visitor): Unit                           // 节点访问者遍历
```

顶层接口 `ParadoxComplexExpression` 同时继承 `ParadoxComplexExpressionNode`，并对外暴露整树聚合能力：
- `getAllErrors(): List<ParadoxComplexExpressionError>`：递归收集树内所有解析错误。
- `getAllReferences(element): List<PsiReference>`：递归收集树内所有节点的引用对象。

### 3.2 生产与消费的职责分离

复杂表达式遵循严格的“轻量解析引擎 + 外部功能分派消费”的架构解耦原则：

```text
               +--------------------------------------+
               | ParadoxComplexExpression.resolve(...) |  <-- 规则驱动轻量解析器
               +--------------------------------------+
                                  │
                                  ▼
                     [ParadoxComplexExpression AST]
                                  │
       ┌──────────────┬───────────┴───────────┬──────────────┬──────────────┐
       ▼              ▼                       ▼              ▼              ▼
[引用解析系统]   [语义高亮系统]            [代码检查系统]   [代码补全系统]   [作用域推导系统]
PsiReference   ParadoxExpressionAnnotator  Inspections    Completion     ParadoxScopeService
```

1. **解析期职责（Parser Phase）**：
   - 仅负责词法切分、构建轻量节点树、绑定父子关系、识别语法格式错误，并确定每个节点的关联规则（`relatedConfigs`）与高亮键（`attributesKey`）。
2. **消费期职责（Consumer Phase）**：
   - **引用解析**：由各节点的 `getReference` 返回具体实现（如 `ParadoxDynamicValueNode` 返回指向动态值轻量元素的引用，`ParadoxSystemScopeNode` 返回系统作用域符号引用）。
   - **语义高亮**：`ParadoxExpressionAnnotator` 遍历整树，直接读取各节点的 `getAttributesKey()` 进行范围标注。
   - **代码检查**：`ParadoxComplexExpressionInspection` 调用 `getAllErrors()` 统一上报格式畸形或未解析错误。
   - **代码补全**：`ParadoxComplexExpressionCompletionProvider` 定位光标所在的 `ParadoxComplexExpressionNode`，依据其规则上下文评估候选集。
   - **作用域推导**：`ParadoxScopeService` 遍历链式节点（`ParadoxLinkedExpression`），自左向右逐步演算作用域切换与上下文转移。

---

## 四、现行约束与已知实现边界

### 4.1 核心边界与特殊约束

1. **`ScriptValueReference` 未入通用分派**：
   - `ParadoxComplexExpression.resolveByConfig` 的分派分支中**未包含** `CwtDataTypes.ScriptValueReference`。
   - 该表达式目前主要作为值字段（`ValueField`）的数据源节点，在 `ParadoxValueFieldValueNode.resolveDsNode` 内部被定向构造；且该表达式目前**尚未实现运行期评估器（Evaluator）**。
2. **`Command` 表达式的脚本占位**：
   - `CwtDataTypes.Command` 在脚本侧仅作占位符，其脚本匹配器 `ParadoxScriptExpressionMatchers.ForCommand` 始终返回 `NotMatch`。
   - 命令表达式仅在本地化富文本（`ParadoxLocalisationCommandText`）及命名格式表达式内部生效。
3. **坐标系统基准（Coordinate Baseline）**：
   - 顶层复杂表达式的 `rangeInExpression` 始终以传入的整体文本相对偏移（通常为 `TextRange(0, text.length)`）为基准。
   - 嵌套复杂表达式（如动态值内部的 `@scope`、命名格式内部的 `[...]` 命令）的子节点偏移严格相对于**最外层复杂表达式的起点**评估。外部消费者在向物理 PSI 映射时，须将节点的 `rangeInExpression` 加上表达式在宿主 PSI 元素内部的文本起始偏移。
4. **完整性模式（Complete Mode）依赖**：
   - 在代码补全或容错解析场景下，解析器需要宽容不完整输入的节点。这一行为当前通过 `ParadoxThreadContext` / `ThreadLocal` 传递 `complete = true` 标志，属于有状态调用。
5. **部分匹配与回退语义**：
   - 多个复杂表达式（如 `TagsExpression`、`TemplateExpression`）在格式不匹配时直接返回 `null`，交由规则引擎回退为常规字面量或标量匹配处理。

---

## 五、与未来重构蓝图的边界

维护者笔记 `documents/notes/proposals/complex-expression-refactors.md` 等提出了面向后续版本的表达式架构重构构想。在此明确界定现行事实与未来蓝图的界限：

1. **Token / Scanner 体系重构（v3.1.x+ 规划）**：
   - 现行实现采用手写的逐字符循环或正则表达式扫描；提案中规划的统一 `TokenStream`、通用字符扫描器（`Scanner`）与词元消费抽象**尚未实施**。
2. **Jomini 数据函数与单引号字面量（v3.1.x+ 规划）**：
   - 当前动态作用域节点对传参语法采用启发式切分；针对 VIC3 等新一代游戏引入的 Jomini 数据函数模型及单引号字面量逃逸语义属于后续版本规划。
3. **动态链接优化（v3.1.x+ 规划）**：
   - 现行实现按前缀对规则列表进行遍历匹配；提案中的预编译前缀树（Trie）优化属于未来性能改进项。

---

## 六、外部工具与术语对照消歧

| 插件概念 / 类型 | CWTools 对应概念 | 官方 Wiki 概念 | 语义与模型差异消歧 |
| :--- | :--- | :--- | :--- |
| **`scope_field`** | `scope_field` / `scope[...]` | Scope chaining | 基本同构。插件在此基础上增加了细粒度 AST 节点与光标补全。 |
| **`value_field`** | `value_field` / `int_value_field` | Value expressions | 插件支持将其解析为包含作用域前驱的链式评估树。 |
| **`DynamicValue`** | `value_set[...]` / `value[...]` | Dynamic variables / Event targets | **同名异义**：CWTools 的 `value_set` 仅是静态规则集合声明；插件将其提升为具备读写语义、轻量元素建模与作用域引用的统一“动态值（Dynamic Value）”体系。 |
| **`DatabaseObject`** | 无直接对应（通常使用定义引用） | Database entries (`civic:...`) | 插件特有的复杂表达式，用于精准表达本地化及特定脚本中带命名空间的数据库对象映射。 |
| **`NameFormat`** | 无直接对应 | Name format blocks | 插件特有，用于解析带命名部件占位符与命令表达式的字符串构造格式。 |
