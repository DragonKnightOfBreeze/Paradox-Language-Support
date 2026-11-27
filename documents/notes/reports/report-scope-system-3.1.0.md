# 调研报告：作用域系统与上下文推导（v3.1.0）

> - 版本：v3.1.0（仓库 `gradle.properties` 为 `3.1.0-dev`，调研快照对应提交 `17be200da3`）
> - 日期：2026-11-27
> - 状态：调研快照
> - 调研范围：Paradox Chronicle 插件的作用域（scope）模型、并集作用域（union scopes）、CWT 规则侧配置体系、`ParadoxScopeContext` 的数据结构与传播/合并/覆盖机制、相关扩展点（EP）、缓存与索引依赖、已知局限，以及与官方 Wiki、CWTools、Tiger 的术语与模型差异。
> - 写作约束：本报告是并发只读调研的产物，不修改任何代码、`documents/notes/reports.md`、`documents/design/` 下的文件，也不执行 `git add` 或 `git commit`；报告中的外部术语只用于后续设计讨论，不改变插件现有命名。

## 摘要

作用域是 Paradox 脚本语义的“主语”，插件将其建模为“当前作用域类型 + 系统作用域命名槽 + 任意/未知哨兵 + 作用域关系图”的组合，而不是集合或并集类型。并集作用域目前不是一等公民，Stellaris 的 `carrier` 通过 `scopes.cwt` 中的 `is_subscope_of` 继承关系在匹配和合并阶段被模拟。CWT 规则侧由 `scopes.cwt`、`scope_groups.cwt`、`system_scopes.cwt`、`links.cwt`、`localisation_links`/`localisation_commands`/`localisation_promotions` 以及选项 `## push_scope`、`## replace_scopes`、`## scope(s)` 共同驱动。`ParadoxScopeContext` 维护 `this`、`root`、`from`～`fromfromfromfrom`、`prev` 栈和链式表达式的节点结果，支持由定义/动态值 EP 提供、基于用法推断、合并、覆盖、缓存和失效。推断依赖 4 类合并索引（`ScopeInferrableDefinition`、`EventInOnAction`、`EventInEvent`、`OnActionInEvent`），默认关闭，按设置项分别开启。主要局限包括：并集不是显式类型、`is_subscope_of` 只支持单父作用域、`Any`/`Unknown`/`null` 的匹配语义不对称、事件/动作触发的推断作用域有限、动态值推断仍是 TODO、索引收集的数据较少，以及 `prev` 不参与 `replace_scopes` 与上下文合并。

## 调研方法与来源

本报告以代码事实为主，辅以维护者文档、规则文件、GitHub Issue、官方 Wiki 与竞品实现对照。代码导航优先使用 `intellij`/`intellij-index` MCP，未通过 shell 解压或反编译任何库/平台 JAR。外部资料只记录术语与模型差异，不作为修改插件命名的依据。

- 代码：`src/main/kotlin/icu/windea/pls/lang/scope/**`、`config/scopes/**`、`config/model/**`、`model/scope/**`、`ep/scope/**`、`lang/resolve/complexExpression/**`、`lang/util/ParadoxScopeManager.kt`、`lang/index/**`、`ep/index/**`、`lang/inspections/**`、`lang/overrides/**`。
- 维护者文档：`documents/notes/analysis/mod-scopes.md`、`documents/notes/adr/union-scopes.md`、`documents/notes/adr/scope-and-scope-methods-refactor.md`、`documents/notes/adr/root-info-and-metadata-resolution.md`、`documents/notes/ideas.md`、`documents/notes/issues.md`、`documents/notes/issues/p14-event-script-scope-inference-false-positive.md`、`documents/notes/roadmap/v3.0.2.md`。
- 规则示例：`cwt/core/00_system_scopes.cwt`、`cwt/core/00_links.cwt`、`cwt/core/00_localisation_links.cwt`、`cwt/core/internal/schema.cwt`、`cwt/cwtools-stellaris-config/config/{scopes,links,localisation,localisation_links}.cwt`、`cwt/cwtools-vic3-config/config/{scopes,links}.cwt`、`cwt/cwtools-ck3-config/config/{scopes,links}.cwt`。
- 参考文档：`docs/zh/ref-config-format.md` 与 `docs/en/ref-config-format.md` 中作用域、系统作用域、链接、扩展规则与 FAQ 章节；`docs/zh/ref-syntax.md` 与 `docs/en/ref-syntax.md` 中安全赋值与命令表达式章节。
- 外部资料：Stellaris/CK3/Vic3 官方 Wiki 的 Scopes 页面；CWTools 的 `CWTools/Process/Scopes/Scopes.fs`、`CWTools/Rules/RulesParser.fs`、配置示例与测试样例；Tiger 的 `src/scopes.rs`、`src/context.rs` 与维护者 Wiki。
- Issue：GitHub 仓库 `DragonKnightOfBreeze/Paradox-Language-Support` 中与作用域推断、event target、localisation scope、并集作用域和误报相关的条目。

## 一、术语与作用域分类模型

### 1.1 插件中的术语

插件在概念上区分以下对象。

| 概念 | 插件术语 | 说明 |
| :--- | :--- | :--- |
| 当前对象的静态类别 | 作用域类型（scope type） | 如 `country`、`planet`，通常由上下文推断，不直接出现在脚本中。 |
| 获取或切换对象的链接 | 作用域链接（scope link） | 如 `owner`、`root`、`event_target:x`；在复杂表达式解析中表现为链接节点。 |
| 预定义的系统级链接 | 系统作用域（system scope） | `This`、`Root`、`Prev`、`From` 及其重复形式；由 `system_scopes` 规则提供元信息。 |
| 带点号或动态数据的链式表达式 | 作用域字段表达式（scope field expression） | 如 `root.owner`、`event_target:x`、`root.event_target:x`。 |
| 某处当前作用域及系统作用域的状态 | 作用域上下文（scope context） | `ParadoxScopeContext`，包含 `this`、`root`、`from`、`prev` 等。 |
| 多个作用域类型的命名集合 | 作用域分组（scope group） | 由 `scope_groups` 规则定义，用于校验“期望作用域集合”，不是上下文中的值。 |
| 通过继承关系形成的父/子类型集合 | 并集作用域（union scopes） | 插件非正式术语；代码中通过 `is_subscope_of`、匹配/合并服务实现。 |
| 由数据源携带的数据 | 动态值（dynamic value） | 如 `event_target`、`var`、`scope`、`scripted_loc`；插件用轻量元素和动态值 EP 建模。 |

### 1.2 `ParadoxScope` 的封闭层级

`ParadoxScope` 是 `sealed interface`，只有三种实现（`src/main/kotlin/icu/windea/pls/model/scope/ParadoxScope.kt:32`）。

| 实现 | ID | 索引 | `isUnsure()` | 语义 |
| :--- | :--- | :--- | :--- | :--- |
| `Default` | 规范化后的作用域 ID | 全局自增的非负索引 | `false` | 普通具体作用域类型。 |
| `Any` | `any` | `-1` | `true` | 任意作用域；表示规则未细分或上下文无法进一步推断。 |
| `Unknown` | `?` | `-2` | `true` | 未知作用域；表示规则未定义、切换后无法解析、或节点错误。 |

`all` 在 `ParadoxScopeResolver.getScopeId` 和 `resolveScope` 中会被规范化为 `any`（`src/main/kotlin/icu/windea/pls/model/scope/ParadoxScopeResolver.kt:23`）；但这一规范化只发生在公共入口和规则解析路径，直接调用匹配/合并服务并传入原始 ID 时不会再次归一化。

`ParadoxScopeResolver` 还负责：

- 对作用域 ID 做小写化和空格到下划线的转换，并对字符串做弱引用 interning，减少内存占用（同文件 `:23`）。
- 以 `ConcurrentHashMap` 缓存 `Default` 实例，以 `AtomicInteger` 分配索引，以同步的 `Int2ObjectOpenHashMap` 提供索引反查（同文件 `:16`）。
- 提供 `getScopeName`，优先使用 `configGroup.scopes` 的规则名，其次使用 `scopeAliasMap`，最后回退为首字母大写形式（同文件 `:31`）。
- 提供 `resolveScopeContext`、`resolveNextScopeContext`、`toScopeMap`、`toScopeIdMap`、`toPresentableString` 等上下文构造和展示工具（同文件 `:58`）。

### 1.3 表达式节点的分类

作用域字段表达式由 `ParadoxScopeFieldExpression` 表示（`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxScopeFieldExpression.kt:62`），其节点分类如下。

| 节点类型 | 判定来源 | 典型形式 | 上下文结果 |
| :--- | :--- | :--- | :--- |
| `ParadoxSystemScopeNode` | `system_scopes` 规则 | `This`、`Root`、`Prev`、`From`、`FromFrom` | 从输入上下文取对应系统作用域；缺失时为 `Unknown`。 |
| `ParadoxStaticScopeNode` | `links` 中静态且 `type` 支持 `scope` 的规则 | `owner`、`capital_scope` | 使用 `output_scope`；为空时透传输入。 |
| `ParadoxDynamicScopeNode` | `links` 中动态链接（带前缀、数据源或传参） | `event_target:x`、`var:x`、`scope:x`、`relations(root)` | 有 `output_scope` 时直接切换；否则按数据源类型推断或透传。 |
| `ParadoxParameterizedScopeNode` | 文本包含参数占位符 | `$PARAM$`、`<<...>>` | 默认 `any`；可从扩展参数规则推断。 |
| `ParadoxErrorScopeNode` | 以上均不匹配 | 无效标识符 | `Unknown`。 |
| `ParadoxScopePrefixNode` | 动态链接前缀节点 | `event_target:` | 取对应链接配置的 `output_scope`。 |

`ParadoxScopeNode.resolve` 的匹配顺序是系统作用域、静态作用域、动态作用域、参数化作用域、错误节点（`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxScopeNode.kt:13`）。`ParadoxScopeFieldExpressionResolver` 按点号切分，但跳过括号内部和 `|` 屏障之后的点号，并保留参数范围（`ParadoxScopeFieldExpression.kt:75`）。

本地化命令表达式使用另一套节点：`ParadoxCommandScopeNode` 与 `ParadoxCommandFieldNode`（`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxCommandExpression.kt`）。命令作用域节点同样分为系统、静态、动态、参数化和错误五类，但静态链接从 `localisationLinks` 读取；命令字段节点不切换作用域。

### 1.4 并集作用域的实现方式

插件没有 `ParadoxScope.Union` 这样的类型。并集作用域通过作用域继承图实现，主要是 Stellaris 的 `carrier` 场景（`documents/notes/adr/union-scopes.md:9`、`documents/notes/roadmap/v3.0.2.md:133`）。

- `scopes.cwt` 中用 `is_subscope_of = carrier` 把 `Planet` 和 `Ship` 声明为 `Carrier` 的子作用域；`cwt/cwtools-stellaris-config/config/scopes.cwt` 中可以看到这一配置。
- `CwtScopeModel` 在规则分组初始化时把别名、父作用域、子作用域、匹配作用域和提升作用域转换为索引集合（`src/main/kotlin/icu/windea/pls/config/model/CwtScopeModel.kt:11`、`src/main/kotlin/icu/windea/pls/ep/config/configGroup/CwtConfigGroupPostProcessors.Model.kt:103`）。
- `ParadoxScopeMatchService` 的继承匹配采用并集策略：父作用域匹配子作用域，子作用域也匹配父作用域（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMatchService.kt:46`、ADR `union-scopes.md:17`）。
- `ParadoxScopeMergeService` 在合并时把子作用域提升到父作用域：子 vs 父返回父，兄弟返回共同父作用域；没有共同父作用域时返回 `null`（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMergeService.kt:67`）。
- 该实现会丢失“当前到底是 `planet` 还是 `ship`”的分支信息，只保留共同父类型；这是有意的宽松匹配，但也意味着并集不是可继续细分的集合。

### 1.5 `Any`、`Unknown` 与 `null` 的语义

匹配与合并服务对三种“非具体”输入的处理并不对称，设计文档必须明确记录。

- `null` 作用域上下文/作用域在匹配时视为“总是匹配”，在合并时视为“取另一项”（`ParadoxScopeMatchService.kt:35`、`ParadoxScopeMergeService.kt:37`）。
- 作为 `scope` 输入时，`Any` 和 `Unknown` 都视为通配，匹配任意 `scopeToMatch`（`ParadoxScopeMatchService.kt:57`）。
- 作为 `scopeToMatch` 时，只有 `any` 被视为通配；`?` 不是通配，只有具体 ID 完全相等或通过关系模型匹配时才算匹配（`ParadoxScopeMatchService.kt:35`）；`all` 在直接调用服务时也不会被识别为 `any`。
- 合并时 `Any` 优先于 `Unknown`，`Unknown` 优先于具体作用域（`ParadoxScopeMergeService.kt:41`）；`any` 与 `?` 同时出现时返回 `any`。这一优先级在 `ParadoxScopeMergeServiceTest` 中有明确断言。
- 在集合形式中，只有恰好等于 `setOf("any")` 的集合才视为通配；包含 `any` 的混合集合不会整体通配，`?` 也不会作为集合通配符（`ParadoxScopeMatchService.kt:104`、`ParadoxScopeMatchServiceTest.kt` 中 `matchesScope_forStellaris_withScopesSet`）。

### 1.6 作用域分组与“无作用域/原始作用域”

`scope_groups` 规则定义一组作用域类型，用于 `scope_group[...]` 数据类型和触发器的期望作用域集合；其值在解析时按作用域 ID 归一化（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtScopeGroupConfig.kt:45`）。`ParadoxScopeMatchService.matchesScopeGroup` 先按分组集合精确匹配，再按关系模型和提升关系匹配（`ParadoxScopeMatchService.kt:170`）。

Stellaris 的 `No Scope`/`no_scope`、Vic3 的 `none`、`value`、`boolean`、`flag` 等都被当作普通作用域 ID 处理；插件没有像 Tiger 那样区分 primitive scope（`Value`/`Bool`/`Flag`）与数据库作用域。这是后续设计需要明确的边界，尤其是“scopeless/global”内容的语义。

### 1.7 易混淆的同名概念

维护者文档中还有两类“scope”容易与领域作用域混淆，阅读和设计时应显式区分：

- `documents/notes/adr/scope-and-scope-methods-refactor.md` 讨论的是 Kotlin API 设计中的“作用域方法”（如 `SequenceScope`/`CoroutineScope` 风格），即通过成员方法或上下文参数限定 API 可用范围，不是 Paradox 脚本中的作用域上下文。
- `documents/notes/adr/root-info-and-metadata-resolution.md` 讨论的是 `ParadoxRootInfo`/`ParadoxRootMetadata`（游戏或模组根目录的解析元数据），不是脚本中的 `root` 系统作用域。
- 代码中的 `CwtConfigResolutionScope`/`ParadoxSelectScope` 等接口是“规则解析/选择的作用域辅助器”，同样不是 `ParadoxScopeContext`；只有 `icu.windea.pls.model.scope`、`ParadoxScopeService`、`ParadoxScopeManager` 和 `ep.scope` 包属于领域作用域系统。

### 1.8 分类速览

| 分类 | 表示来源 | 上下文结果 | 备注 |
| :--- | :--- | :--- | :--- |
| 系统作用域 | `system_scopes` 规则 + 命名槽 | 从输入上下文的 `this/root/from*/prev*` 取对应上下文 | 表达式求值中按硬编码名称分派。 |
| 静态作用域 | 静态 `links` 规则 | 使用 `output_scope`；为空时透传输入 | 不依赖数据源。 |
| 动态作用域 | 带前缀/数据源/传参的 `links` 规则 | 有 `output_scope` 时切换；否则按数据源推断或透传 | `event_target:`、`var:`、`scope:` 等。 |
| 参数化作用域 | 表达式中的参数占位符 | 默认 `any`；可从扩展参数规则推断 | 只处理完整参数化。 |
| 并集作用域 | `is_subscope_of` 继承图 | 匹配时父/子双向；合并时提升到父或共同父 | 不是独立的 `ParadoxScope` 类型。 |
| 任意作用域 | `Any`/`any`/`all` | 作为输入总是匹配；作为目标时 `any` 是通配 | `all` 在规则路径归一化为 `any`。 |
| 未知作用域 | `Unknown`/`?` | 作为输入总是匹配；作为目标不是通配 | 用于无法解析的切换或错误节点。 |
| 作用域分组 | `scope_groups` 规则 | 按命名集合匹配期望作用域 | 不进入上下文值。 |

## 二、CWT 规则侧配置体系

### 2.1 作用域规则：`scopes.cwt` 与 `CwtScopeConfig`

`CwtScopeConfig` 由 `scopes` 容器下的属性规则解析（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtScopeConfig.kt:45`）。

| 字段 | 类型 | 语义 |
| :--- | :--- | :--- |
| `name` | `String` | 规则名称，来自属性键；不在这里做大小写或空白归一化。 |
| `aliases` | `Set<String>` | 别名集合，逐项通过 `ParadoxScope.getId` 归一化。 |
| `is_subscope_of` | `String?` | 父作用域 ID，经 `ParadoxScope.getId` 归一化；当前只支持单个值。 |

解析流程会跳过属性缺失的无效规则，记录警告后返回 `null`（`CwtScopeConfig.kt:66`）。`scopeAliasMap` 在规则分组文件处理阶段构建，键为别名、值为对应的 `CwtScopeConfig`，并用大小写不敏感映射保存（`src/main/kotlin/icu/windea/pls/ep/config/configGroup/CwtConfigGroupProcessors.FileBased.kt:305`、`src/main/kotlin/icu/windea/pls/config/model/CwtConfigGroupDataModel.kt:122`）。

与 CWTools 的差异：CWTools 的 `is_subscope_of` 解析为多个叶子值（`CWTools/Parser/UtilityParser.fs` 中收集 `LeafValues`），测试规则中可见 `is_subscope_of = { parent }`；插件目前只读取单个字符串，因此多父作用域规则会被忽略，且 `data_type_name` 字段没有对应模型。这是后续决定“是否继续兼容 CWTools 的并集/多父模型”的关键事实。

### 2.2 作用域分组规则：`CwtScopeGroupConfig`

`CwtScopeGroupConfig` 由 `scope_groups` 容器解析（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtScopeGroupConfig.kt:45`）。`values` 是分组内作用域 ID 的集合，`valueConfigMap` 保存每个 ID 到原始值规则的映射。空分组会返回空集合而不是无效规则；无法解析的值会被跳过。分组匹配的输入是作用域上下文或作用域 ID，语义详见 `ParadoxScopeMatchService.matchesScopeGroup`（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMatchService.kt:170`）。

### 2.3 系统作用域规则：`CwtSystemScopeConfig`

`CwtSystemScopeConfig` 由 `system_scopes` 容器解析（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtSystemScopeConfig.kt:49`），字段只有 `name` 和 `base`；`base` 默认等于名称，用于把 `Prev`、`PrevPrev`、`From`、`FromFrom` 等归入同族。核心规则文件 `cwt/core/00_system_scopes.cwt` 给出了 10 个系统作用域的完整定义，`cwt/core/internal/schema.cwt` 给出了规则 schema（`:250` 附近）。

系统作用域在表达式节点解析中的实际行为是硬编码的：`evaluateScopeContextFromNode` 按 `This`、`Root`、`Prev`、`PrevPrev`、`PrevPrevPrev`、`PrevPrevPrevPrev`、`From`、`FromFrom`、`FromFromFrom`、`FromFromFromFrom` 逐一比较名称（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:498`）。只有 `base == "From"` 的节点会触发 `isFrom` 语义；其他系统作用域（包括未来在 `system_scopes` 中新增的条目）不会被这个 switch 识别。核心规则使用 PascalCase 名称，若规则文件中使用其他大小写形式，节点解析可能失配。

### 2.4 链接规则：`links.cwt` 与 `CwtLinkConfig`

`CwtLinkConfig` 是复杂表达式链接节点的规则模型（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtLinkConfig.kt:94`）。

| 字段 | 默认值/规则 | 语义 |
| :--- | :--- | :--- |
| `type` | `scope` | `scope`/`value`/`both`，决定链接能否用于作用域字段或值字段。 |
| `from_data` | `no` | 动态链接是否从 `prefix:data` 的后置数据读取数据源。 |
| `from_argument` | `no` | 动态链接是否从 `func(arg)` 的传参读取数据源。 |
| `argument_separator` | `comma` | 多参数分隔符，`comma` 或 `pipe`。 |
| `prefix` | `null` | 动态链接前缀；为 `null` 时整个节点文本作为数据。 |
| `data_source` | 空列表 | 数据源数据表达式；为空时按静态链接处理。 |
| `input_scopes` | `{ any }` | 输入作用域集合；同时兼容 `input_scope` 单数写法。 |
| `output_scope` | `null` | 输出作用域；为 `null` 时表示按数据源传递或推断。 |
| `for_definition_type` | `null` | 限定适用的定义类型。 |

静态链接与动态链接的判定是 `dataSources.isEmpty()`（`src/main/kotlin/icu/windea/pls/config/config/_accessors.kt:68`）。`input_scopes`/`input_scope` 缺失或为空时回退为 `anyScopes`；`output_scope` 在解析时归一化为标准 ID（`CwtLinkConfig.kt:177`）。`CwtLinkModel` 按静态、无前缀、从数据、从参数等维度预分组，供复杂表达式解析快速查找（`src/main/kotlin/icu/windea/pls/config/model/CwtLinkModel.kt:13`）。

典型规则示例：

- Stellaris `owner`/`controller`/`space_owner` 等静态作用域链接，输入集合明确，输出为 `country`（`cwt/cwtools-stellaris-config/config/links.cwt`）。
- Stellaris `event_target` 动态链接：`from_data = yes`、`prefix = event_target:`、`data_source = value[event_target]`（`cwt/core/00_links.cwt`）。
- Vic3/CK3 `var` 动态链接：`type = both`、`prefix = var:`、`data_source = value[variable]`；这使同一个 `var:x` 既可能作为值使用，也可能作为作用域使用，是 #142 中冲突误报的背景。
- CK3 的事件目标（如 `holder`、`mother`）通常是没有前缀的静态链接，直接声明 `input_scopes` 与 `output_scope`；CK3 的 `scope:` 是保存作用域引用，配置为动态值链接（`cwt/cwtools-ck3-config/config/links.cwt`）。
- Vic3 的 `scope` 链接同样以 `prefix = scope:` 和 `data_source = value[event_target]` 表示保存作用域（`cwt/cwtools-vic3-config/config/links.cwt`）。

### 2.5 本地化链接、命令与提升

本地化命令表达式使用 `localisation_links`、`localisation_commands` 和 `localisation_promotions`。

- `CwtLocalisationCommandConfig` 声明命令字段的 `supportedScopes`，默认 `anyScopes`（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtLocalisationCommandConfig.kt:57`）。
- `CwtLocalisationPromotionConfig` 声明通过本地化链接切换作用域后仍可使用的命令字段及其提升作用域（`src/main/kotlin/icu/windea/pls/config/config/delegated/CwtLocalisationPromotionConfig.kt:63`）。
- `CwtLinkConfig` 也用于本地化链接，`isLocalisationLink` 标记规则来源；静态常规链接在没有显式本地化链接声明时会作为本地化链接的复制来源（`CwtLinkConfig.kt:55` 的文档注释）。
- 本地化命令作用域节点在静态情况下会从 `localisationPromotions` 读取提升作用域，并写入作用域上下文的 `promotions` 扩展属性（`ParadoxScopeService.kt:440`）；匹配服务会使用该提升集合继续匹配（`ParadoxScopeMatchService.kt:138`）。

### 2.6 选项元数据：`push_scope`、`replace_scopes`、`scope(s)`

选项元数据由 `CwtOptionMetadataService` 解析（`src/main/kotlin/icu/windea/pls/config/option/CwtOptionMetadataService.kt:46`）。

| 选项 | 解析结果 | 语义 |
| :--- | :--- | :--- |
| `## push_scope = x` | 归一化后的单个作用域 ID | 把当前上下文切换到 `x`，常用于别名规则、类型/子类型规则和声明规则。 |
| `## replace_scope = {...}` / `## replace_scopes = {...}` | `Map<String, String>`，键小写、值归一化 | 替换 `this`/`root`/`from` 系列系统作用域的映射；不支持 `prev` 系列。 |
| `## scope = x` / `## scopes = {...}` | 归一化后的集合 | 声明触发器/效果等别名规则允许的输入作用域集合。 |

`processFinalForScopeContext` 会计算 `optionMetadata.scopeContext`（`CwtOptionMetadataService.kt:182`）：

- 若存在 `replaceScopes`，先 `ParadoxScopeContext.resolve(map)`，再对结果执行 `resolveNext(pushScope)`。
- 否则若存在 `pushScope`，使用 `ParadoxScopeContext.resolve(pushScope, pushScope)`，即 `this` 和 `root` 都设为该作用域。
- 两者都没有时保持 `null`。

`CwtOptionMetadata` 的 KDoc 将 `scopeContext` 描述为“初始的作用域上下文”，用于后续表达式匹配、补全和校验（`src/main/kotlin/icu/windea/pls/config/option/CwtOptionMetadata.kt:195`）。`replace_scopes` 的键和值在解析时分别小写化和归一化（`src/main/kotlin/icu/windea/pls/config/scopes/CwtOptionMetadataResolutionScope.kt:56`）。

规则文档对 `replace_scopes` 的限制有明确说明：仅支持 `this`、`root` 和基于 `from` 的系统作用域，不支持 `prev` 系列（`docs/zh/ref-config-format.md:2709`、`docs/en/ref-config-format.md:2709`）。

### 2.7 `CwtScopeModel`：关系索引

`CwtScopeModel` 是作用域匹配/合并的性能优化模型（`src/main/kotlin/icu/windea/pls/config/model/CwtScopeModel.kt:11`），包含五组 `Int -> Set<Int>` 映射。

| 映射 | 含义 |
| :--- | :--- |
| `base2Aliases` | 基础作用域到别名形式的映射。 |
| `base2ParentScopes` | 基础作用域到父作用域（递归收集，兼容别名）的映射。 |
| `base2ChildScopes` | 基础作用域到子作用域的映射。 |
| `base2MatchedScopes` | 基础作用域到可匹配作用域（别名、父、子）的映射。 |
| `base2PromotedScopes` | 基础作用域到提升后作用域（别名、父）的映射。 |

构建过程（`src/main/kotlin/icu/windea/pls/ep/config/configGroup/CwtConfigGroupPostProcessors.Model.kt:103`）：

- 先为每个 `CwtScopeConfig` 收集别名索引和父作用域索引；把父作用域递归向上展开，并用 `guardStack` 防止循环。
- 再为父作用域补充别名，并把父作用域的反向关系写入 `base2ChildScopes`。
- 最后把别名和父关系写入 `base2MatchedScopes`/`base2PromotedScopes`，把子关系仅写入 `base2MatchedScopes`。
- 循环父作用域（如测试数据中的 `Alpha <-> Beta`）不会导致无限循环，但会形成双向匹配，合并时表现为顺序相关的提升结果（测试 `ParadoxScopeMatchServiceTest`/`ParadoxScopeMergeServiceTest` 中有断言）。
- ADR 期望在 `is_subscope_of` 非法或意外递归时于规则解析阶段打印警告（`documents/notes/adr/union-scopes.md:13`），当前代码只做了循环保护，没有看到对应的警告日志；后续可补充校验。

### 2.8 类型/别名模型与预定义数据

`CwtTypeModel` 保存了与作用域上下文相关的定义类型集合（`src/main/kotlin/icu/windea/pls/config/model/CwtTypeModel.kt:17`）：

- `supportScope`：支持作用域上下文的定义类型。
- `supportIndirectScope`：间接支持作用域上下文的定义类型（如事件、动作触发）。
- `supportScopeInference`：支持基于用法推断作用域上下文的定义类型。
- `skipCheckSystemScope`：不需要检查系统作用域切换的定义类型。
- `supportParameters`：支持参数的定义类型。
- `aliasNamesSupportScope`：支持作用域约束的别名名称集合（由 `CwtAliasModel.supportScope` 持有）。

核心预定义数据位于 `src/main/resources/data/config_group_data_list.json5`：

- `supportScope`：`scripted_trigger`、`scripted_effect`、`game_rule`。
- `supportIndirectScope`：`on_action`、`event`。
- `supportScopeInference`：`scripted_trigger`、`scripted_effect`。
- `skipCheckSystemScope`：`event`、`scripted_trigger`、`scripted_effect`、`script_value`、`game_rule`。
- `aliasNamesSupportScope`：`modifier`、`trigger`、`effect`。

`CwtAliasModel.supportScope` 保存支持作用域约束的别名名称；`ParadoxScopeService.isScopeContextSupportedForMember` 会用它判断某个配置是否值得计算作用域上下文（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:219`）。

### 2.9 数据类型与链接模型

与作用域直接相关的数据表达式类型（`src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt:405`、`src/main/kotlin/icu/windea/pls/config/CwtDataTypeSets.kt:44`）：

- `scope_field`：匹配作用域字段表达式，等价于任意输出作用域。
- `scope[type]`：匹配作用域字段表达式并约束输出作用域；参数为 `any` 时等同 `scope_field`。
- `scope_group[name]`：匹配作用域字段表达式并约束输出作用域属于指定分组。

`CwtLinkModel` 把链接规则分为静态、无前缀动态、从数据动态、从参数动态等集合，并额外维护值链接集合（`src/main/kotlin/icu/windea/pls/config/model/CwtLinkModel.kt:13`）。这套模型同时服务常规脚本表达式和本地化命令表达式。

## 三、`ParadoxScopeContext` 的数据结构与机制

### 3.1 结构

`ParadoxScopeContext` 是 `sealed interface`，并有三种实现（`src/main/kotlin/icu/windea/pls/model/scope/ParadoxScopeContext.kt:39`）。

| 实现 | 结构 | 用途 |
| :--- | :--- | :--- |
| `Simple` | `scope` + 可选 `root` | 新建上下文、无 `from`/`prev` 的简单上下文。 |
| `Complex` | `scope`、`root`、`from`～`from4`、`prevStack` | 普通切换后的完整上下文。 |
| `Linked` | 链接节点到上下文的列表 + `prevStack` | 链式表达式的逐步结果；`scope`/`root`/`from` 委托给最后一个节点上下文。 |

公共属性包括 `scope`（当前 `this`）、`root`、`from`、`from2`、`from3`、`from4`、`prev`～`prev4` 以及 `prevStack`、`links`。`prev` 系列是从 `prevStack` 的前四个元素派生的只读属性（`ParadoxScopeContext.kt:47`）。`rootScope`、`fromScope`、`prevScope` 等则是从上下文取 `scope` 的便捷属性（同文件 `:55`）。

`prevStack` 的更新方式是“把当前输入上下文压到栈顶”：每次 `resolveNext` 都会在栈顶插入输入上下文（`src/main/kotlin/icu/windea/pls/model/scope/ParadoxScopeResolver.kt:107`、`:121`）。因此 `prev` 表示“切换前的上下文”，而不是脚本中显式的 `prev` 节点结果。

### 3.2 构造与 map 转换

`ParadoxScopeResolver` 提供以下入口：

- `resolveScopeContext(thisScope)`：构造只有 `this` 的 `Simple`。
- `resolveScopeContext(thisScope, rootScope)`：构造带 `root` 的 `Simple`。
- `resolveScopeContext(map)`：从 `this/root/from/fromfrom/.../prev/prevprev/...` 字符串映射构造 `Complex`；`this` 缺失时返回 `null`，`prevStack` 要求从 `prev` 开始连续存在。
- `resolveAnyScopeContext()`：构造 `this = any`、`root = any` 的上下文。
- `resolveUnknownScopeContext(input, isFrom)`：构造 `this = ?` 的上下文，或以 `input` 为前驱切换到 `?`。

`toScopeMap(showFrom, showPrev)` 总是包含 `this`，在存在时包含 `root`，并按参数决定是否包含 `from` 系列和 `prev` 系列（`ParadoxScopeResolver.kt:136`）。`toScopeIdMap` 与之一一对应，只是值改为 ID 字符串（同文件 `:157`）。`toPresentableString` 把 map 渲染成 `key = value` 的拼接文本，供内嵌提示和类型信息使用（同文件 `:178`）。

### 3.3 传播规则

| 入口 | `scope` | `root` | `from` 系列 | `prevStack` | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `resolveNext(input, pushScope, isFrom=false)` | `pushScope` 解析结果 | 保持 `input.root` | 保持 `input.from*` | 插入 `input` | `pushScope == null` 时直接返回 `input`。 |
| `resolveNext(input, pushScope, isFrom=true)` | `pushScope` 解析结果 | 置空 | 置空 | 插入 `input` | 用于显式 `from` 型切换的未知回退。 |
| `resolveNext(input, next, isFrom=false)` | `next.scope` | 保持 `input.root` | `next.from ?: input.from`，其余同理 | 插入 `input` | 优先采用 `next` 自带的 `from` 链，否则继承输入。 |
| `resolveNext(input, next, isFrom=true)` | `next.scope` | 保持 `input.root` | 直接使用 `next.from` 等（包括空值） | 插入 `input` | 用于进入 `From` 系统作用域。 |
| `resolveNext(input, links)` | `links.last.scope` | `links.last.root` | `links.last.from*` | 插入 `input` | 生成 `Linked`；`links` 保存每个节点及其结果。 |

上述规则见 `ParadoxScopeResolver.kt:99`、`:114`、`:128`。需要特别注意的是，`resolveNext(input, next)` 不会采用 `next.root`，而是始终保留 `input.root`；这符合“切换作用域不改变根作用域”的语义，但也意味着外部传入的 `next.root` 会被忽略。

### 3.4 合并机制

`ParadoxScopeMergeService` 提供四个层次的合并（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMergeService.kt:37`）。

各层次的规则如下：

- 两者都为 `null` 时返回 `null`；只有一方存在时返回另一方。
- 任一方为 `any` 时返回 `any`；任一方为 `?` 时返回 `?`；`any` 优先于 `?`。
- ID 完全相等时返回第一个；随后尝试别名和父子关系。
- 父子关系通过 `base2PromotedScopes` 判定：子作用域提升到父作用域，兄弟作用域提升到共同父作用域；没有共同提升目标时返回 `null`。
- `mergeScopeContext` 使用 `toScopeIdMap(showPrev = false)`，因此上下文级合并会丢弃 `prev` 系列（测试中明确断言 `prevStack` 为空）。
- `mergeScopeContextMap` 会逐键合并 `this/root/from1..4/prev1..4`，并丢弃合并失败（返回 `null`）的键；结果为空时返回 `null`。
- `orUnknown = true` 时，`this` 或 `root` 缺失（或为 `?`）会被强制写成 `?`，从而保证上下文级合并能构造出一个 `this` 存在的上下文；这常用于事件/动作触发的推断。
- 上下文级合并即使 map 非空，只要合并结果缺少 `this`，`ParadoxScopeContext.resolve(map)` 仍会返回 `null`；`mergeScopeContextMap` 和 `mergeScopeContext` 的行为需要区分。

这些规则在 `ParadoxScopeMergeServiceTest` 中有大量断言，包括别名、父子、多级父、兄弟、循环父、`orUnknown` 和 `prev` 丢弃等场景。

### 3.5 匹配机制

`ParadoxScopeMatchService` 提供 ID、`ParadoxScope`、`ParadoxScopeContext`、作用域集合和作用域分组的匹配入口（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMatchService.kt:35`）。

- ID 入口先处理 `null`、`any` 目标、`any`/`?` 输入和完全相等，再通过 `base2MatchedScopes` 做关系匹配。
- `ParadoxScope`/上下文入口额外处理 `Any`/`Unknown` 对象和 `promotions` 提升集合。
- 集合入口只把 `null`、空集合和恰好等于 `{ any }` 的集合视为通配；混合集合逐个目标匹配。
- 分组入口先查分组配置，再按分组值、关系模型和提升关系匹配；分组不存在时返回 `false`。
- 关系匹配使用索引集合，避免重复字符串比较；提升匹配先检查提升 ID，再检查提升作用域的关系模型。

### 3.6 用户数据扩展

`ParadoxScopeContext` 继承 `UserDataHolder`，并在 `_accessors.kt` 中注册了三个扩展属性（`src/main/kotlin/icu/windea/pls/model/scope/_accessors.kt:9`）：

- `promotions: Set<String>`：本地化提升作用域，默认空集。
- `isExact: Boolean`：当前上下文是否已经精确，默认 `true`；`ParadoxDefaultDefinitionScopeContextProvider` 仅在类型上下文和声明上下文同时存在时把它设为 `false`（`src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDefinitionScopeContextProviders.kt:23`）。
- `overriddenProvider: ParadoxOverriddenScopeContextProvider?`：产生该上下文的覆盖提供者，默认 `null`；`ParadoxScopeService.getOverriddenScopeContext` 在返回时会记录它（`ParadoxScopeService.kt:208`）。

这些扩展属性使上下文可以携带“是否还需推断”“是否来自覆盖”“可用于哪些本地化命令”等元信息，而不必扩展接口本身。

### 3.7 `Linked` 上下文的注意事项

`Linked` 适合表示链式表达式的逐步结果，但其字段委托给最后一个节点上下文，且 `promotions`、`isExact` 等用户数据不会自动从节点结果继承。若后续需要把本地化提升或精确标记带入最终链式上下文，可能需要显式传播这些扩展属性。

## 四、作用域上下文推导管线

### 4.1 总览

以脚本成员为例，推导管线可以概括为以下步骤。

1. 规则分组加载时解析 `scopes`、`scope_groups`、`system_scopes`、`links`、`localisation_*`，初始化 `CwtConfigGroupDataModel`，并构建 `CwtScopeModel`/`CwtTypeModel`/`CwtAliasModel`。
2. 定义匹配得到 `ParadoxDefinitionInfo`（类型、名称、类型规则、子类型规则、声明规则等）。
3. 对定义调用 `ParadoxScopeService.getScopeContext`，按 EP 顺序取第一个显式上下文。
4. 若显式上下文不精确，调用推断 EP 获取 `ParadoxScopeContextInferenceInfo`，合并结果；任一推断冲突则放弃推断。
5. 合并显式上下文与推断结果；合并失败则回退 `Any`。
6. 对脚本成员，先检查覆盖提供者；若成员本身是作用域字段属性，则用父成员上下文逐节点求值；否则按 `replace_scopes`/`push_scope`/父上下文推导。
7. 对动态值，使用动态值 EP 或推断 EP，得到其上下文。
8. 用 `ParadoxScopeManager` 的 `CachedValue` 缓存结果，依赖文件修改和全局推断修改跟踪器。
9. 语言功能通过 `ParadoxScopeMatchService` 判断当前上下文是否满足期望作用域/分组，或通过 `ParadoxScopeMergeService` 合并多个上下文。

### 4.2 定义级上下文

`ParadoxScopeService.getScopeContext(definition, definitionInfo)` 遍历 `ParadoxDefinitionScopeContextProvider` 扩展点（`src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:85`）。每个 EP 先检查游戏类型，再检查 `supports`，并调用 `ProgressManager.checkCanceled`；第一个返回非 `null` 的 EP 生效。

内置提供者：

- `ParadoxDefaultDefinitionScopeContextProvider`：组合类型/子类型规则和声明规则的 `scopeContext`；若两者都存在，用 `scopeContextOnType.resolveNext(scopeContextOnDeclaration)` 合并并标记 `isExact = false`。
- `ParadoxBaseDefinitionScopeContextProvider`：从 `extendedDefinitions` 扩展规则读取 `scopeContext`。
- `ParadoxGameRuleScopeContextProvider`：从 `extendedGameRules` 读取 `scopeContext`，仅支持 `game_rule` 定义。
- `ParadoxOnActionScopeContextProvider`：从 `extendedOnActions` 读取 `scopeContext`，仅支持 `on_action` 定义。

`ParadoxScopeService.getInferredScopeContext(definition, definitionInfo)` 遍历推断 EP（`ParadoxScopeService.kt:100`）。每个 EP 返回 `ParadoxScopeContextInferenceInfo(scopeContextMap, hasConflict)`；一旦任一 EP 报告冲突，整个推断返回 `null`；否则把各 EP 的映射逐键合并，最后 `ParadoxScopeContext.resolve(resultMap)`。推断消息和错误消息分别由 `getInferenceMessage`/`getInferenceErrorMessage` 提供给检查器。

`evaluateScopeContextFromDefinition` 的优先级如下（`ParadoxScopeService.kt:294`）：

- 显式上下文存在且 `isExact` 为 `true` 时，直接使用显式上下文。
- 否则尝试推断；推断结果存在时，把显式上下文和推断上下文合并；合并失败则返回 `Any`。
- 推断结果不存在时，返回显式上下文或 `Any`。

需要特别记录的是：只有“类型上下文 + 声明上下文”组合会把 `isExact` 设为 `false`；仅由类型/子类型 `push_scope` 产生的上下文默认是精确的。因此如果某个定义类型（例如当前配置中多数 `event` 子类型）仅通过 `push_scope` 提供 `this/root`，推断 EP 可能不会参与该定义的求值。该行为是否符合预期需要后续用专项测试确认，并应在设计文档中明确“精确”的定义。

### 4.3 成员级上下文

`ParadoxScopeService.evaluateScopeContextForMember` 先尝试定义级上下文，再尝试成员级上下文（`ParadoxScopeService.kt:288`）。

成员级流程（`ParadoxScopeService.kt:314`）：

- 从属性键或值中取得表达式元素；表达式文本带参数时直接跳过（3.0.3 行为）。
- 用 `ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))` 获取规则，再用 `CwtConfigManager.selectFirstPrioritizedConfig` 选择优先级最高的规则（3.0.3 行为）。
- 找到最近的父成员（不包含自身），取其上下文。
- 先调用 `getOverriddenScopeContext`；如果有覆盖上下文，直接返回。
- 如果规则成员类型是属性且数据类型是 `scope_field`，解析作用域字段表达式，并在父上下文上逐节点求值；父上下文缺失时返回 `null`。
- 否则按 `replaceScopes`（优先内联前规则，其次内联后规则）或父上下文 + `pushScope` 推导；两者都没有时返回 `null`。

`ParadoxScopeManager.getScopeContext(element)` 提供成员级缓存入口（`src/main/kotlin/icu/windea/pls/lang/util/ParadoxScopeManager.kt:64`）。缓存键为 `Keys.cachedScopeContext`，依赖 `element.containingFile` 和 `ChronicleModificationTrackers.ScopeResolution`；`ScopeResolution` 目前就是 `DefinitionScopeContextInference` 的别名（`src/main/kotlin/icu/windea/pls/base/ChronicleModificationTrackers.kt:47`）。

### 4.4 表达式级节点求值

`evaluateScopeContextForExpression(element, expression, inputScopeContext)` 逐一遍历 `expression.scopeNodes`，把每个节点结果收集到 `links`，最后返回 `input.resolveNext(links)`；遇到 `ParadoxErrorScopeNode` 会停止后续节点（`ParadoxScopeService.kt:385`）。

节点求值结果如下（`ParadoxScopeService.kt:400`）。

| 节点 | 行为 |
| :--- | :--- |
| 参数化作用域 | 仅完整参数化时尝试从扩展参数规则推断；可从 `scope[type]` 值或上下文规则容器得到输出；否则返回 `Any`。 |
| 系统作用域 | 按硬编码名称取 `this/root/prev*/from*`；缺失时返回 `Unknown`；`base == From` 时使用 `isFrom` 语义。 |
| 静态作用域 | 使用 `config.outputScope` 切换；`outputScope` 为空时 `resolveNext(null)` 返回输入。 |
| 动态作用域 | 有 `outputScope` 时直接切换；否则按数据源类型处理 `scope_field` 嵌套、动态值或透传。 |
| 错误作用域 | 返回 `Unknown(input)`。 |
| 作用域前缀 | 取第一个链接配置的 `outputScope`。 |
| 命令作用域-系统 | 同系统作用域。 |
| 命令作用域-静态 | 切换 `outputScope`，并从 `localisationPromotions` 设置 `promotions`。 |
| 命令作用域-动态 | 返回 `Any`。 |
| 命令作用域-错误 | 返回 `Unknown(input)`。 |
| 命令字段 | 不改变上下文。 |

### 4.5 动态值上下文

`ParadoxScopeManager.getScopeContext(dynamicValue)` 返回动态值的上下文；若没有显式/推断结果则回退 `Any`（`ParadoxScopeManager.kt:75`）。`getScopeContext(dynamicValue, inputScopeContext)` 只接收动态值的 `this` 作用域，忽略其 `root`、`from` 等其他信息，再在输入上下文上执行 `resolveNext(scope.id)`（`ParadoxScopeManager.kt:86`）。这意味着 `event_target` 或 `var` 自身的复杂上下文在切换时可能被压缩为单个 `this`。

动态值的显式提供者 `ParadoxBaseDynamicValueScopeContextProvider` 从 `extendedDynamicValues` 的 `scopeContext` 选项读取（`src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDynamicValueScopeContextProviders.kt:8`）。动态值推断提供者目前只是返回 `null` 的占位实现（`src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDynamicValueInferredScopeContextProviders.kt:6`），对应 Issue #46 的“从用法推断动态值作用域”仍未实现。

### 4.6 覆盖提供者

`ParadoxOverriddenScopeContextProvider` 用于对特定脚本结构的上下文进行覆盖（`src/main/kotlin/icu/windea/pls/ep/scope/ParadoxOverriddenScopeContextProvider.kt:9`）。当前有两个实现：

- `ParadoxSwitchOverriddenScopeContextProvider`：处理 `switch`/`inverted_switch` 中的 `scalar`/`default` 属性，根据 `trigger` 的值推断目标属性的上下文（`ParadoxOverriddenScopeContextProviders.kt:27`）。
- `ParadoxTriggerWithParametersAwareOverriddenScopeContextProvider`：处理 `complex_trigger_modifier`/`export_trigger_value_to_variable` 的 `trigger`/`parameters` 属性，兼容 `trigger_scope` 与当前上下文不匹配的情况（同文件 `:62`）。

覆盖提供者的结果会写入 `overriddenProvider`，并且 `isScopeContextSupportedForMember` 会因此把成员视为支持作用域上下文（`ParadoxScopeService.kt:219`）。

### 4.7 支持性判定与诊断

`ParadoxScopeService.isScopeContextSupportedForMember` 的判定条件（`ParadoxScopeService.kt:219`）：

- 定义类型在 `typeModel.supportScope` 中，或在 `indirect=true` 时位于 `typeModel.supportIndirectScope` 中。
- 匹配的规则是 `AliasKeysField`，或规则的根/子链包含 `aliasModel.supportScope` 中的别名名称。
- 存在覆盖提供者。

诊断相关检查器：

- `IncorrectScopeInspection`：检查父上下文是否满足期望作用域；支持脚本和本地化命令表达式（`src/main/kotlin/icu/windea/pls/lang/inspections/script/scope/IncorrectScopeInspection.kt:25`）。
- `IncorrectScopeSwitchInspection`：检查静态作用域切换的输入作用域、系统作用域的未知切换；脚本版本有一个 `checkForSystemScopes` 选项，默认关闭（`src/main/kotlin/icu/windea/pls/lang/inspections/script/scope/IncorrectScopeSwitchInspection.kt:22`）。
- `IncorrectScopeLinkChainInspection`：检查作用域链长度是否超过 `ParadoxScopeManager.maxScopeLinkSize = 5`（`ParadoxScopeManager.kt:38`、`IncorrectScopeLinkChainInspection.kt:63`）。
- `ConflictingScopeContextInferenceInspection`：报告推断冲突，默认不启用（`src/main/kotlin/icu/windea/pls/lang/inspections/script/scope/ConflictingScopeContextInferenceInspection.kt:12`）。
- 本地化命令版本使用 `ParadoxCommandScopeNode`/`ParadoxCommandFieldNode` 逐节点检查，并且每个复杂表达式只报告第一个问题（`src/main/kotlin/icu/windea/pls/lang/inspections/localisation/scope/IncorrectScopeSwitchInspection.kt`、`IncorrectScopeInspection.kt`）。

## 五、扩展点、索引与缓存

### 5.1 作用域相关 EP

| EP | 接口 | 当前实现/用途 |
| :--- | :--- | :--- |
| `icu.windea.pls.definitionScopeContextProvider` | `ParadoxDefinitionScopeContextProvider` | 默认、扩展定义、游戏规则、动作触发。 |
| `icu.windea.pls.definitionInferredScopeContextProvider` | `ParadoxDefinitionInferredScopeContextProvider` | 定义引用用法、事件在动作触发中、事件在事件中、动作触发在事件中。 |
| `icu.windea.pls.definitionSupportedScopesProvider` | `ParadoxDefinitionSupportedScopesProvider` | Stellaris `scripted_modifier` 的修正分类作用域。 |
| `icu.windea.pls.dynamicValueScopeContextProvider` | `ParadoxDynamicValueScopeContextProvider` | 扩展动态值规则的 `scopeContext`。 |
| `icu.windea.pls.dynamicValueInferredScopeContextProvider` | `ParadoxDynamicValueInferredScopeContextProvider` | 当前为 TODO，返回 `null`。 |
| `icu.windea.pls.overriddenScopeContextProvider` | `ParadoxOverriddenScopeContextProvider` | `switch`/`inverted_switch`、带参数的触发器/值导出。 |

EP 注册见 `src/main/resources/META-INF/chronicle-ep.xml:396` 附近。`ParadoxScopeService` 对每个 EP 先检查游戏类型，再检查 `supports`，并在应用逻辑前执行 `ProgressManager.checkCanceled`；这一顺序在 3.0.1 的优化中被明确加入（`ParadoxScopeService.kt:70`、`:85`、`:100`、`:168`、`:183`、`:208`）。

需要特别核对术语：本快照中不存在名为 `ParadoxScriptExpressionScopeContextProvider` 的类型；与脚本表达式上下文相关的能力由 `ParadoxScopeManager`/`ParadoxScopeService` 的表达式求值方法、`ParadoxOverriddenScopeContextProvider` 以及动态值相关 EP 共同提供。任务描述中的该名称可能是旧版本、计划中的命名或其他调研材料的口误，后续设计文档不应把它当作现有扩展点入口。

`ParadoxDefinitionSupportedScopesProvider` 当前只有一个实现 `StellarisScriptedModifierSupportedScopesProvider`，仅支持 Stellaris 的 `scripted_modifier`，通过修正分类汇总支持作用域（`src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDefinitionSupportedScopesProviders.kt:10`、`ParadoxScopeManager.getSupportedScopes`）。`ParadoxScopeManager.getSupportedScopes(modifierCategories)` 在分类为空或任一分类支持 `any` 时返回 `anyScopes`，否则取分类支持作用域的并集（`ParadoxScopeManager.kt:117`）。

### 5.2 推断提供者细节

所有推断提供者都先通过 `ParadoxSearchScope.fromElement(definition)` 从定义获取搜索范围，事件/动作触发再分别用 `withFilePath("common/on_actions", "txt")` 或 `events/**/*.txt` 收窄范围，以减少索引查询量（`ParadoxDefinitionInferredScopeContextProviders.kt:78`、`:182`、`:289`、`:439`）。

`ParadoxBaseDefinitionInferredScopeContextProvider`（`src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDefinitionInferredScopeContextProviders.kt:46`）：

- 仅支持 `typeModel.supportScopeInference` 中的定义类型（当前为 `scripted_trigger`、`scripted_effect`）。
- 受 `ChronicleSettings.state.inference.scopeContext` 开关控制，默认 `false`。
- 搜索 `ParadoxMergedIndexTypes.ScopeInferrableDefinition`，匹配定义名称和类型表达式。
- 对每个使用处取定义的作用域上下文，只收集 `this` 和 `root`；多个用法通过 `mergeScopeContextMap(..., orUnknown = true)` 合并；冲突时标记 `hasConflict`。
- 缓存依赖 `DefinitionScopeContextInference` 和 `CwtConfigGroupModificationTrackers.definitionScopeContext`（由 `supportScopeInference` 定义类型的文件路径模式生成）。

`ParadoxEventInOnActionInferredScopeContextProvider`（同文件 `:148`）：

- 仅支持 `event` 定义，受 `scopeContextForEvents` 开关控制，默认 `false`。
- 搜索 `EventInOnAction` 索引；对同一事件的每个包含动作触发，匹配扩展动作触发规则的 `eventType`。
- 使用扩展规则的 `replaceScopes` 映射合并；没有映射时跳过；冲突时标记。
- 缓存依赖固定路径 `common/on_actions/**/*.txt` 和全局推断跟踪器。

`ParadoxEventInEventInferredScopeContextProvider`（同文件 `:256`）：

- 仅支持 `event`，受 `scopeContextForEvents` 控制。
- 搜索 `EventInEvent` 索引；初始 `this`/`root` 为事件自身的作用域（来自 `ParadoxEventManager.getScope`）。
- 对每个调用事件，若存在 `scopes = {...}`，只处理 base 为 `from` 的系统作用域，逐项解析对应表达式；否则使用包含事件的 `containingEventScope` 填充 `from`、`fromfrom` 等。
- 递归深度最多 4；使用 `withRecursionGuard`/`withRecursionCheck` 防止循环。
- 缓存依赖 `events/**/*.txt` 和全局推断跟踪器。

`ParadoxOnActionInEventInferredScopeContextProvider`（同文件 `:403`）：

- 仅支持 `on_action`，受 `scopeContextForOnActions` 控制，默认 `false`。
- 若动作触发已在 `extendedOnActions` 中预定义，则跳过推断。
- 搜索 `OnActionInEvent` 索引；初始 `this`/`root` 为 `any`；其余逻辑与事件在事件中的 `from` 推断类似。
- 缓存依赖 `events/**/*.txt` 和全局推断跟踪器。

### 5.3 合并索引

作用域推断使用的合并索引类型定义在 `src/main/kotlin/icu/windea/pls/lang/index/ParadoxMergedIndexTypes.kt:18`。

| 索引类型 | 信息字段 | 生成条件 | 用途 |
| :--- | :--- | :--- | :--- |
| `ScopeInferrableDefinition` | 定义名、类型表达式、定义元素偏移 | 声明规则综合属性包含 `involveScopeInferrableDefinitionReference` | 收集可推断定义的使用处。 |
| `EventInOnAction` | 事件名、类型表达式、包含的动作触发名 | `on_action` 类型强制可用 | 事件在动作触发中的调用。 |
| `EventInEvent` | 事件名、包含事件名、包含事件作用域、`scopes` 元素偏移 | `event` 类型强制可用 | 事件在事件中的调用及 `from` 推断。 |
| `OnActionInEvent` | 动作触发名、包含事件名、包含事件作用域、`scopes` 元素偏移 | `event` 类型强制可用 | 动作触发在事件中的调用及 `from` 推断。 |

索引构建逻辑见 `src/main/kotlin/icu/windea/pls/ep/index/ParadoxMergedIndexSupports.Inference.kt:38`。索引数据只保存少量字符串和偏移；`definitionElement`/`scopesElement` 通过 `ParadoxScriptPsiService.findPropertyFromStartOffset` 按需从文件定位（`src/main/kotlin/icu/windea/pls/model/index/ParadoxScopeInferrableDefinitionIndexInfo.kt:13`、`ParadoxEventInEventIndexInfo.kt:13`、`ParadoxOnActionInEventIndexInfo.kt:13`）。

索引优化器只为可能包含目标数据的类型/声明构建对应索引（`src/main/kotlin/icu/windea/pls/ep/index/ParadoxMergedIndexOptimizers.kt:78`）：`on_action` 强制 `EventInOnAction`，`event` 强制 `EventInEvent` 与 `OnActionInEvent`，其他类型按声明规则综合属性决定是否加入 `ScopeInferrableDefinition`。`scope` 元素偏移的提取对事件/动作触发的结构有硬编码假设，并留有 `TODO 3.1.0+ [major-refactor]`（`ParadoxMergedIndexSupports.Inference.kt:153`、`:220`）。

### 5.4 缓存与失效

| 缓存 | 位置 | 依赖 | 说明 |
| :--- | :--- | :--- | :--- |
| 脚本成员上下文 | `ParadoxScopeManager.Keys.cachedScopeContext` | `containingFile`、`ScopeResolution` | 每个脚本成员独立缓存。 |
| 动态值上下文 | 同上 | 动态值元素 | `CachedValueProvider.Result.create(value, element)`。 |
| 定义推断信息 | 各推断 EP 的 `cachedScopeContextInferenceInfo` | `DefinitionScopeContextInference`、按定义类型/路径生成的脚本跟踪器 | 每个 EP 独立缓存。 |
| Scope 实例 | `ParadoxScopeResolver.cache` | 全局 | `ConcurrentHashMap`，弱引用 interning；索引反查同步。 |

`ChronicleCapabilities.Cache.scopeContext` 可以关闭上下文缓存，默认 `true`（`src/main/kotlin/icu/windea/pls/ChronicleCapabilities.kt:74`）。`ChronicleSettingsConfigurable` 在刷新作用域上下文推断时递增 `DefinitionScopeContextInference`，刷新文件与内嵌提示（`src/main/kotlin/icu/windea/pls/base/settings/ChronicleSettingsConfigurable.kt:832`）。

历史问题 P14：在事件脚本中修改文件内容后，部分作用域推断出现误报，最终定位到 `ParadoxMatchResult.LazyMatch` 的并发处理，并已修复（`documents/notes/issues/p14-event-script-scope-inference-false-positive.md:1`）。这说明作用域推断对并发缓存和惰性匹配的依赖较深，后续修改需要保持并发安全。

## 六、语言功能消费面

作用域上下文不只是检查器的输入，也是补全、文档、提示和类型信息的基础。

| 功能 | 主要代码 | 作用域使用方式 |
| :--- | :--- | :--- |
| 表达式匹配 | `src/main/kotlin/icu/windea/pls/lang/match/util/ParadoxMatchResultFactory.kt:291` | `scope_field`/`scope_group` 使用上下文匹配期望作用域。 |
| 代码补全 | `ParadoxCompletionManager.kt`、`ParadoxExpressionCompletionManager.kt`、`ParadoxComplexExpressionCompletionManager.kt` | 按当前上下文过滤链接、命令、触发器和效果。 |
| 代码检查 | `lang/inspections/script/scope/**`、`lang/inspections/localisation/scope/**` | 报告作用域切换错误、期望作用域不匹配、链过长和推断冲突。 |
| 内嵌提示 | `ParadoxScopeContextInfoHintsProvider.kt:59` | 在块左花括号后显示 `this/root/from...` 映射；可只在上下文变化时显示。 |
| 快速文档 | `ParadoxDocumentationManager.kt:835`、`CwtDocumentationManager.kt:445` | 在文档中展示当前作用域上下文。 |
| 类型信息 | `ParadoxTypeService.kt:239`、`ParadoxTypeProvider.kt:75` | 把 `toScopeMap()` 渲染为类型信息。 |
| 修正匹配 | `ParadoxModifierSupports.kt:69`、`:154`、`:244` | 使用修正分类或修正规则的支持作用域与当前上下文匹配。 |
| 注解数据/快照 | `ParadoxAnnotatedDataFactory.kt:141` | 把作用域上下文附加到注解数据。 |
| 本地化命令检查 | `lang/inspections/localisation/scope/**` | 逐节点检查命令作用域与命令字段支持作用域。 |

测试覆盖主要集中在匹配/合并服务：`src/test/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMatchServiceTest.kt` 与 `ParadoxScopeMergeServiceTest.kt` 覆盖了别名、父子、多级父、循环父、通配、`any`/`?`、`orUnknown`、`prev` 丢弃等行为；测试配置位于 `src/test/testData/features/scope/.config/stellaris/scopes.cwt`，其中包含 `carrier`、`world`、`moon`、`star`、`army`、`satellite`、`alpha`/`beta` 等专门构造。`ParadoxScopeFieldExpressionTest` 覆盖表达式解析；`Issue383Test` 覆盖复杂触发器修饰符的作用域上下文。推断 EP、事件/动作触发跨文件推断和缓存失效目前缺少同等密度的专项测试。

## 七、已知局限与待改进项

### 7.1 模型与语义

- 并集作用域不是一等类型，只有继承关系；不能表达“精确的 planet 或 ship 集合”，也不能在并集成员之间继续保留分支信息。
- `is_subscope_of` 只支持单个父作用域，CWTools 支持多个父作用域；多父规则会被静默忽略，且 `data_type_name` 字段没有对应模型。
- 循环父作用域只做了保护，没有按 ADR 期望在规则解析阶段输出警告；循环关系的匹配/合并在语义上依赖输入顺序，测试中也以顺序相关断言记录。
- `Any`、`Unknown` 与 `null` 的匹配/合并语义不对称；直接调用服务时不会自动归一化 `all`、大小写和别名，调用方必须保证输入已是规范 ID。
- 作用域 ID 比较是大小写敏感的；规则解析路径会归一化，但服务层测试明确检查了未归一化输入不会匹配。
- `prev` 不参与 `replace_scopes`，上下文级合并会丢弃 `prev`；`replace_scopes` 文档明确说明不支持 `prev` 系列。
- 系统作用域节点解析使用硬编码名称，只有 `base == From` 的节点会触发 `isFrom`；新增或改名的 `system_scopes` 条目可能只在规则层可见，而不会参与表达式求值。
- 原始/无作用域（Stellaris `No Scope`、Vic3 `none`）和 primitive scope（`value`/`bool`/`flag`）没有独立分类，全部按普通作用域 ID 处理。

### 7.2 推断准确性

- 基础推断只收集 `this` 和 `root`；事件/动作触发推断才涉及 `from` 系列，且最多 4 层。
- 任意推断方式存在冲突时整个推断返回 `null`，最终由定义求值回退到 `Any`；这可能丢失仍有价值的部分信息。
- 仅由类型/子类型 `push_scope` 提供上下文的定义可能被视为精确，从而跳过推断 EP；事件/动作触发推断的实际生效范围需要专项验证。
- 参数化作用域默认 `Any`，只有完整参数化且能匹配扩展参数规则时才会推断；带参数、内联和复杂嵌套场景仍可能产生宽松结果。
- 动态值的推断提供者仍是 TODO；Issue #46 仍未解决；`event_target`/`var`/`scope` 仍大量回退到 `Any`。
- `getScopeContext(dynamicValue, input)` 只接收动态值的 `this`，忽略其 `root`/`from` 链，可能丢失动态值自身携带的上下文。
- `ParadoxCommandExpression.scopeNodes` 当前过滤的是 `ParadoxScopeNode`（`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/ParadoxCommandExpression.kt` 的实现），而命令节点实现的是 `ParadoxCommandScopeNode`（`src/main/kotlin/icu/windea/pls/lang/resolve/complexExpression/nodes/ParadoxCommandScopeNode.kt`），两者是 `ParadoxLinkNode` 下的兄弟接口；该属性看起来不会被填充，后续应确认是否应改为命令节点类型或移除。
- `Linked` 上下文不会自动传播 `promotions`/`isExact` 等用户数据，链式表达式和提升逻辑组合时可能出现信息丢失。

### 7.3 规则与兼容性

- CWTools 的 `replace_scope`（单数）在插件中同时支持 `replace_scope`/`replace_scopes`；`system_scopes`、`localisation_promotions` 等是插件扩展，迁移规则时不能假定 CWTools 完全等价。
- `input_scopes`/`input_scope` 缺失或空集合时回退为 `any`；`output_scope` 为 `null` 时按数据源传递或推断；动态链接的数据源类型只显式处理 `scope_field` 和 `dynamic_value`，其他类型会透传输入，可能不符合预期。
- 静态常规链接会作为本地化链接的复制来源；如果规则覆盖顺序或链接类型变化，本地化命令的可用性可能与脚本侧不一致。
- `scope_groups` 的值必须使用可归一化的作用域 ID；空分组和缺失分组的行为不同，规则作者需要明确。
- `CwtScopeConfig` 名称不做归一化，`scopeAliasMap` 在别名冲突时后写入者覆盖先写入者，没有冲突诊断。

### 7.4 索引、缓存与性能

- 合并索引只保存少量字段，ScopeInferrableDefinition 只保存定义名/类型表达式/偏移，事件索引只保存事件名/包含关系/偏移；`documents/notes/ideas.md:65` 与 `:143` 提出的“为更准确的作用域推断在合并索引中收集更多数据”仍是 P4 想法。
- 事件/动作触发的 `scopes` 元素偏移提取依赖硬编码结构，留有 3.1.0+ 重构 TODO；事件链跨越 `scripted_effect` 仍不支持（`ideas.md:442`）。
- 推断缓存依赖全局推断跟踪器和脚本文件跟踪器；如果合并索引数据变化但未触发相应文件修改跟踪器，存在缓存陈旧的理论风险；设置页刷新会显式递增跟踪器。
- 推断默认关闭，开启后会在大型游戏目录中查询多个合并索引并递归合并；推断冲突直接放弃，可能掩盖性能问题或准确性问题。
- `CwtScopeModel` 使用 `Int2ObjectOpenHashMap`/`IntArraySet`，构建阶段不是线程安全的；构建完成后由规则分组持有，应避免在错误线程修改。

### 7.5 低优先级想法与待验证项

- `ideas.md:407` 提出“快速推断”策略，避免默认合并所有用法；可用于降低推断成本。
- `ideas.md:404` 提出数据流分析，检测作用域在当前上下文是否存在；目前只有静态上下文校验，没有完整数据流。
- `ideas.md:618` 提出进一步完善内联脚本和参数的上下文规则推断。
- `ideas.md:700` 提出基于用法/数据流的 `event_target`/`variable` 检查。
- `ideas.md:72` 提出在上下文规则和匹配规则中展开并集/别名配置；当前仅有限支持。
- `ideas.md:442` 提出事件链解析应能跨越 `scripted_effect`；这会影响 `from` 推断的完整性。
- `issues.md:24` 记录 `scope[species]` 应始终匹配 `this` 的待验证问题；如果无法精确匹配，需要回退到同类型第一个规则。
- `CHANGELOG.md` 3.0.6 修复了作用域链接被误报为“匹配多个冲突规则”的问题（#284），说明作用域链接匹配与规则优先级仍然敏感。
- `CHANGELOG.md` 3.0.2 已实现并集作用域匹配/合并；3.0.3 跳过参数化表达式的作用域上下文评估；3.0.4 支持嵌套动态值表达式。

## 八、与官方 Wiki、CWTools、Tiger 的术语与模型对照

### 8.1 官方 Wiki

Stellaris Wiki（`https://stellaris.paradoxwikis.com/Scopes`，页面标注版本 3.2/checked 3.4）：

- 作用域是对象的类型化上下文；系统作用域为 `THIS`、`PREV`、`ROOT`、`FROM`。
- `PREV` 和 `FROM` 最多重复 4 次；点号链式作用域用于简化嵌套，但链式切换不产生新的 `PREV`。
- 作用域存在性通过 `exists = [scope]` 检查；触发器和效果只在特定作用域中有效。
- `event_target` 可通过 `save_event_target_as`/`save_global_event_target_as` 保存，通过 `event_target:<name>` 引用；`on_action` 会覆盖 `THIS`/`FROM` 等系统作用域。
- 页面列出了 scope type 与 scope link 的表格（country、planet、pop、fleet、ship 等）。

CK3 Wiki（`https://ck3.paradoxwikis.com/Scopes`）：

- 区分 database scope、primitive scope（number/boolean/flag）、top scope。
- 通过嵌套块进行 context switch；`root` 是脚本实例的默认上下文，不一定等于玩家或国家。
- 事件目标（event target）从 CK3 直接以名字访问（如 `holder`、`mother`），由游戏提供 input/output scope 信息，通常无前缀；目标可以链式。
- `this`/`prev` 是特殊上下文引用；`prev` 不能像 CK2 那样多次链式回溯。
- 保存作用域使用 `scope:<name>`，通过 `save_scope_as` 保存，随未中断的效果链传递，可由 `clear_saved_scope` 清除；还有 `save_temporary_scope_as`。
- 列表构建器包括 `every_X`、`random_X`、`ordered_X`、`any_X`；保存作用域值使用 `scope:<name>` 指向 primitive scope。
- CK3 的专门 `Event_target` 页面当前没有内容，以上事件目标信息取自其 `Scopes` 页面。

Vic3 Wiki（`https://vic3.paradoxwikis.com/Scopes`，页面标注版本 1.10）：

- 作用域是 effects/triggers 的上下文；数据库作用域有类型，primitive scope 包括 number/boolean/flag；`none`/global 表示不要求特定作用域。
- event target 也被称为 scope link，分为 contextual 与 specified；某些 event target 是 global；input/output scope 记录在 `event_targets.log`。
- 支持点号链式作用域；`?=` 是作用域断言运算符；`root`、`prev`、`this` 是特殊目标；`prev` 可以链式但不建议超过两次。
- 保存作用域使用 `scope:` 与 `save_scope_as`；迭代器包括 `any_`、`every_`、`ordered_`、`random_`，另有 scripted lists 和 geographic regions。
- Vic3 的专门 `Event_target` 页面进一步把 event target 分为 data event target、value event target 和 wild card event target，并按所需作用域分类，包含 `none scope/scopeless`；数据形式为 `event_target:data` 或 `event_target(data)`，后者可以包含点链并需要用引号括起（`https://vic3.paradoxwikis.com/Event_target`，页面标注版本 1.13）。

这些页面描述的是游戏运行时语义；它们没有定义 `is_subscope_of`、`input_scopes`、`output_scope` 等 CWT 规则字段，这些是规则系统/工具层的抽象。

### 8.2 CWTools

CWTools 的相关事实（以仓库代码与配置为准）：

- 规则侧使用 `scopes.cwt` 的 `aliases` 与 `is_subscope_of`，`scope_groups` 分组；`links.cwt` 使用 `input_scopes`、`output_scope`、`type`、`from_data`、`prefix`、`data_source`。`is_subscope_of` 在 CWTools 解析器中可以读取多个叶子值，测试规则使用 `is_subscope_of = { parent }`。
- 选项层面，CWTools 的 `RulesParser` 支持 `push_scope`，并通过 `replace_scope` 读取 `this`/`root`/`from`/`fromfrom`/`fromfromfrom`/`fromfromfromfrom`；插件同时支持复数 `replace_scopes`，并扩展了系统作用域、本地化提升等规则。
- CWTools 的内部 `ScopeContext` 是一个记录：`Root: Scope`、`From: Scope list`、`Scopes: Scope list`；`CurrentScope` 是 `Scopes` 的头部，`GetFrom` 从 `From` 链取值（`CWTools/Process/Scopes/Scopes.fs`）。
- CWTools 用 `ScopeResult` 表示求值结果：`NewScope`、`WrongScope`、`NotFound`、`VarFound`、`VarNotFound`、`ValueFound`，并通过 `Scope.IsOfScope` 检查当前作用域是否符合期望列表；默认上下文使用 `AnyScope`，无上下文使用 `InvalidScope`。
- CWTools 的处理模型是按作用域栈和期望作用域列表逐段匹配，和插件的 `this/root/from/prev` 命名映射不同；插件在保留 CWT 语法兼容的同时，加入了更丰富的上下文对象和推断管线。

### 8.3 Tiger

Tiger 的实现事实（`src/scopes.rs`、`src/context.rs`）：

- 用 `bitflags` 定义 `Scopes`，每个游戏类型对应一个位；`Scopes` 可以表示“可能的作用域类型集合”，因此并集是原生能力；`Scopes::all()`、`Scopes::primitive()`、`Scopes::non_primitive()` 等辅助集合用于表示任意/原始/非原始作用域。
- `scope_to_scope` 返回输入和输出两个 `Scopes`；`scope_prefix` 还返回参数约束；`scope_iterator` 处理迭代器；`from_snake_case_multi` 支持用 `|` 组合多个作用域 ID。
- `ScopeContext` 维护 `scope_stack`（以 `this` 结尾）、`root`、`from`（HOI4 专用）、具名作用域/列表/局部变量、输入作用域标记、builder 状态、unrooted 状态、严格模式和来源 token；`ScopeEntry` 支持 `Backref`、`Rootref`、`Scope`、`Named`、变量引用等。
- Tiger 支持 `open_scope`、`open_builder`/`finalize_builder`、`root_for_event`/`root_for_action`、`save_current_scope`、`exists_scope` 等操作；脚本值、脚本化效果和触发器使用 `new_unrooted` 表示“不知道调用者 root”。
- Tiger 的作用域类型和事件目标数据来自游戏导出的日志（如 `event_scopes.log`、`event_targets.log`），不是 CWT 规则；此外有 `scope_override` 配置用于修正已知错误的上下文。
- Tiger 的本地化数据类型区分 promotes 与 functions，并在作用域上下文中校验数据链；插件则以 `localisation_links`/`localisation_commands`/`localisation_promotions` 建模，概念相近但规则来源不同。

### 8.4 对照表

| 维度 | 插件 | 官方 Wiki | CWTools | Tiger |
| :--- | :--- | :--- | :--- | :--- |
| 作用域值 | 单个 `ParadoxScope` + `Any`/`Unknown` | 一个具体对象/类型 | `Scope` 值 + `AnyScope`/`InvalidScope` | `Scopes` 位集，可表示多个可能类型 |
| 并集 | 继承图 + 匹配/合并提升 | 无显式并集概念 | `is_subscope_of` 可用于类型关系 | 原生位集并集 |
| 系统作用域 | `system_scopes` + 命名槽 | `THIS`/`ROOT`/`FROM`/`PREV` | `Root`/`From`/`Scopes` 结构 | `Rootref`/`Fromref`/`Backref` |
| 动态链接 | `links.cwt` 的 `data_source`/`prefix` | event target/scope link | `links.cwt` 数据源与 `ScopeResult` | `scope_to_scope`/`scope_prefix` |
| 保存作用域 | `scope:`/`var:`/`event_target:` 动态值 | `event_target:`（Stellaris）/`scope:`（CK3/Vic3） | 由游戏数据与链接配置处理 | 由游戏数据表处理 |
| 本地化范围 | `localisation_links`/命令/提升 | localization 命令/数据函数 | `localisation_commands`/`localisation_links` | datatype promotes/functions |
| 规则来源 | CWT 规则文件 | 游戏运行时/日志 | CWT 规则文件 | 游戏日志 + `scope_override` |

## 九、结论与后续设计建议

作用域系统的现状可以概括为“以 CWT 规则为骨架、以单个作用域类型为值、以系统作用域命名槽为上下文、以继承关系模拟并集、以 EP 和合并索引补充推断”。这一模型已经覆盖了 Stellaris 的 `carrier`、事件/动作触发的 `from` 推断、本地化命令提升和复杂表达式的链式切换，但也留下了若干需要在设计文档中明确的问题。

- 需要明确并集作用域的目标模型：继续使用继承图并接受信息损失，还是引入显式的联合/集合表示；如果引入集合表示，需要同时设计匹配、合并、`isExact` 和缓存失效语义。
- 需要明确 `Any`、`Unknown`、`null` 与 `all` 的规范语义和归一化边界；建议区分“调用方输入的规范 ID”和“服务层的宽松输入”，避免直接调用服务时出现与规则路径不一致的行为。
- 需要决定是否支持 CWTools 的多父 `is_subscope_of` 和 `data_type_name`；无论采用与否，都应该在规则解析阶段给出诊断，而不是静默忽略。
- 需要明确 `isExact` 的判定标准和推断 EP 的生效范围，并用事件/动作触发的专项测试验证“仅由 `push_scope` 提供上下文时推断是否跳过”这一行为。
- 需要扩展动态值推断和跨文件用法推断，至少覆盖 `event_target`、`scope:`、`var:` 的保存/使用关系；这与 `ideas.md` 的 P4 索引数据收集、P3 数据流分析和 #46 直接相关。
- 需要为 `ParadoxScopeContext` 的用户数据传播（`promotions`、`isExact`、`overriddenProvider`）建立统一规则；当前 `Linked` 上下文不会自动继承这些属性。
- 需要把缓存/索引的失效契约写清楚：哪些文件修改会触发推断缓存失效、哪些设置会重置跟踪器、索引数据变化是否总能触发足够的修改事件。
- 需要决定 `ParadoxCommandExpression.scopeNodes` 的 API 语义；如果它确实没有被使用，应在设计文档中标注为待清理项，而不是作为稳定的公开能力。

以上事实足以作为后续编写 `documents/design/scope-system.md` 的基线；本文只记录现状与差异，不对插件命名或外部术语做任何修改。

## 附录 A：关键代码位置

| 主题 | 位置 |
| :--- | :--- |
| 作用域模型 | `src/main/kotlin/icu/windea/pls/model/scope/ParadoxScope.kt:32` |
| 上下文模型 | `src/main/kotlin/icu/windea/pls/model/scope/ParadoxScopeContext.kt:39` |
| 上下文解析/合并工具 | `src/main/kotlin/icu/windea/pls/model/scope/ParadoxScopeResolver.kt:58` |
| 上下文扩展属性 | `src/main/kotlin/icu/windea/pls/model/scope/_accessors.kt:9` |
| 作用域规则 | `src/main/kotlin/icu/windea/pls/config/config/delegated/CwtScopeConfig.kt:45` |
| 作用域分组规则 | `src/main/kotlin/icu/windea/pls/config/config/delegated/CwtScopeGroupConfig.kt:45` |
| 系统作用域规则 | `src/main/kotlin/icu/windea/pls/config/config/delegated/CwtSystemScopeConfig.kt:49` |
| 链接规则 | `src/main/kotlin/icu/windea/pls/config/config/delegated/CwtLinkConfig.kt:94` |
| 选项元数据 | `src/main/kotlin/icu/windea/pls/config/option/CwtOptionMetadata.kt:150` |
| 选项解析 | `src/main/kotlin/icu/windea/pls/config/option/CwtOptionMetadataService.kt:46` |
| 作用域关系模型 | `src/main/kotlin/icu/windea/pls/config/model/CwtScopeModel.kt:11` |
| 关系模型构建 | `src/main/kotlin/icu/windea/pls/ep/config/configGroup/CwtConfigGroupPostProcessors.Model.kt:103` |
| 匹配服务 | `src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMatchService.kt:35` |
| 合并服务 | `src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeMergeService.kt:37` |
| 上下文编排 | `src/main/kotlin/icu/windea/pls/lang/scope/ParadoxScopeService.kt:85` |
| 成员缓存管理 | `src/main/kotlin/icu/windea/pls/lang/util/ParadoxScopeManager.kt:64` |
| 定义提供者 | `src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDefinitionScopeContextProviders.kt:18` |
| 推断提供者 | `src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDefinitionInferredScopeContextProviders.kt:46` |
| 覆盖提供者 | `src/main/kotlin/icu/windea/pls/ep/scope/ParadoxOverriddenScopeContextProviders.kt:27` |
| 动态值提供者 | `src/main/kotlin/icu/windea/pls/ep/scope/ParadoxDynamicValueScopeContextProviders.kt:8` |
| 推断索引 | `src/main/kotlin/icu/windea/pls/ep/index/ParadoxMergedIndexSupports.Inference.kt:38` |
| 索引类型 | `src/main/kotlin/icu/windea/pls/lang/index/ParadoxMergedIndexTypes.kt:18` |
| 修改跟踪器 | `src/main/kotlin/icu/windea/pls/base/ChronicleModificationTrackers.kt:21` |
| 推断设置 | `src/main/kotlin/icu/windea/pls/base/settings/ChronicleSettings.kt:320` |

## 附录 B：相关 Issue 与历史记录

| Issue | 状态 | 与本报告的关系 |
| :--- | :--- | :--- |
| #46 Scope context inference for dynamic values (e.g. event targets) from usages | open | 动态值推断仍是 TODO。 |
| #45 Parameter of type scope in scripted effects breaks highlighting/does not get inferred by what is passed | closed | 参数化作用域推断的早期反馈。 |
| #69 Allow specifying a trigger/effect blocks scope somehow through cwt configs if it cannot be inferred | closed | `push_scope`/`replace_scopes` 配置能力的背景。 |
| #71 Some effects change root scopes | closed | 系统作用域传播与 `root`/`prev` 语义问题。 |
| #78 push/replace_scope does not work on event targets | closed | 动态值/事件目标上下文能力。 |
| #79 Scope parameter that has defined type in cwt configs does not correctly switch scope if used to change scope | closed | 参数化作用域推断。 |
| #262 `replace_scopes` not applied on sub blocks | closed | 成员级上下文和声明根匹配。 |
| #330 Improve support for scope field expressions and value field expressions: compatible with nested dynamic value expressions | closed | 动态作用域节点和嵌套表达式。 |
| #342 [Feature Request] On action scope inheritance | closed | 动作触发在事件中的 `from` 推断。 |
| #344 [Jomini] On action event type validation wrong | closed | 动作触发事件类型匹配和误报。 |
| #387 localisation_links elements with data_source = value[something] lead to conflicts | closed | 本地化链接与动态值优先级。 |
| #430 [Vic3] Geographic region short keys and their iterators are broken | closed | Vic3 地理区域键/迭代器的匹配回归；与作用域推断共享匹配链路，作为相邻风险参考。 |
| P14 在事件脚本文件中，变更文件内容后，其中特定部分的作用域推断会发生误报 | 已解决 | 并发惰性匹配导致的误报。 |

## 附录 C：外部资料与访问日期

以下资料均于 2026-11-27 调研时访问；官方 Wiki 页面可能随游戏版本变化，后续引用时应重新核对。

- Stellaris Wiki：`https://stellaris.paradoxwikis.com/Scopes`；`https://stellaris.paradoxwikis.com/Event_target` 重定向到该页面的 Event target 章节。
- CK3 Wiki：`https://ck3.paradoxwikis.com/Scopes`；`https://ck3.paradoxwikis.com/Event_target` 页面当前为空。
- Vic3 Wiki：`https://vic3.paradoxwikis.com/Scopes`；另有 `https://vic3.paradoxwikis.com/Event_target`（页面标注版本 1.13）。
- CWTools 作用域实现：`https://github.com/cwtools/cwtools/blob/master/CWTools/Process/Scopes/Scopes.fs`。
- CWTools 规则解析：`https://github.com/cwtools/cwtools/blob/master/CWTools/Rules/RulesParser.fs`。
- CWTools 规则解析工具：`https://github.com/cwtools/cwtools/blob/master/CWTools/Parser/UtilityParser.fs`。
- Tiger 作用域模型：`https://github.com/amtep/tiger/blob/main/src/scopes.rs`。
- Tiger 上下文模型：`https://github.com/amtep/tiger/blob/main/src/context.rs`。
- Tiger 维护者 Wiki：`https://raw.githubusercontent.com/wiki/amtep/tiger/Overview-for-coders.md`。
- 官方 Wiki 的版本提示：Stellaris 页面标注 version 3.2/checked 3.4；Vic3 页面标注 version 1.10；CK3 页面未在正文中给出统一版本号。
