# 报告：术语全维度审计（v3.1.0）

> - 版本：`v3.1.0`
> - 日期：`2026-11-27`
> - 状态：**调研快照**
> - 调研范围：插件代码中的高频领域术语与 KDoc、`src/main/resources/messages/*.properties` 中的中英文 UI 词条、`documents/design/terminology.md` 与相关维护者文档、`docs/zh` 与 `docs/en` 用户文档、`cwt/` 规则注释与概念命名、GitHub Issues 与历史重命名记录，以及 Paradox 官方 Wiki、CWTools、ck3-tiger / vic3-tiger 的公开文档。
> - 调研性质：只读调研，不修改代码、术语表本体或其他维护者文档，不执行 `git add` 或 `git commit`。
> - 结论边界：本报告只客观记录术语使用现状、跨源差异与潜在歧义，不自行裁决是否改名，也不提出最终译名结论；是否统一由维护者后续评估。

---

## 摘要

- 现行 `documents/design/terminology.md` 收录 21 条核心词条，覆盖了 `scope`、`modifier`、`trigger`、`effect`、`scripted variable`、`localisation`、`define`、`definition`、`game rule`、`on action`、`event`、`macro`、`inline script`、`definition injection`、`config`、`config group` 等最关键概念。
- `documents/notes/guidelines.md` 的翻译小节额外给出了 `property`、`value`、`directValue`、`parameter`、`conditional block`、`syntax macro`、`semantic macro` 等语言构造译法，但这些词条尚未进入 `terminology.md`。
- 代码、UI 消息和文档中仍存在大量高频术语没有术语表条目，主要集中在 CWT 规则分类、规则表达式、覆盖策略、复杂枚举、动态值、片段匹配、定义注入模式和 IntelliJ 平台工程术语。
- 本次识别出的高风险问题不是“译名不好看”，而是同一中文词在不同子系统中承载不同英文概念，或同一英文概念在不同来源中拥有不同名字，典型包括：`define` / `definition`、`config` / `game rule`、`override` / `overwrite`、`conditional block` 与中文“参数化块/条件化块”、`injection` 的多重含义、`scope` 的多重含义、`parse` / `resolve` 都译作“解析”、`intention` 的“意向/意图”混用。
- 跨来源对照显示，插件内部术语与官方 Wiki、CWTools、Tiger 并不存在一一对应关系：官方更常用 `game object` / `entry` / `database entry`，CWTools 更常用 `type` / `config` / `rules`，Tiger 更常用 `item` / `report` / `key` / `severity` / `confidence`，而插件统一使用 `definition` / `config` / `inspection` 等自有抽象。
- 社区中文翻译（以 CK3 Wiki 中文翻译仓库为样本）把 `defines` 译为“定义”、把 `scripted trigger` 译为“脚本化触发器”、把 `on_action` 译为“触发动作”，与插件当前的“定值”“封装触发器”“动作触发”形成可直接观察到的差异。
- 本报告建议维护者优先把“术语消歧”和“来源标注”作为术语表扩充方向，而不是在本轮直接做全局改名。

---

## 图例与统计口径

- **已收录**：`documents/design/terminology.md` 已有对应词条。
- **建议新增**：在代码、UI、文档或规则中高频出现，但 `terminology.md` 尚未收录。
- **建议新增（guidelines 已有译法）**：`guidelines.md` 已给出译法，但术语表本体尚未收录。
- **待定夺**：存在两个以上可辩护译法，或跨来源存在实质冲突；本报告只记录，不建议直接改名。
- **外部差异**：与官方 Wiki、CWTools、Tiger 或社区中文翻译存在命名或语义范围差异。
- 本文的“已收录”判断以当前工作区的 `terminology.md` 为准；`guidelines.md` 的翻译小节不计入“已收录”。
- 本文引用的中文 UI 词条以 `*_zh.properties` 为准，英文 UI 词条以同名无后缀文件为准。
- 本文引用的“官方 Wiki”指 Paradox 各游戏 Wiki 的英文页面；“社区中文翻译”指公开的社区翻译仓库，并非 Paradox 官方中文术语表。

---

## 一、领域通用核心概念

### 1.1 已入库词条复核

| 英文 | 术语表中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **scope** | **作用域** | 已收录 | `terminology.md` 原有条目；官方 Wiki 与 CWTools 均使用 `scope`；插件 UI 与文档高度一致。 |
| **modifier** | **修正** | 已收录 | 官方 Wiki、CWTools、插件消息均使用同一概念；中文 UI 一致使用“修正”。 |
| **trigger** | **触发器** | 已收录 | 插件统一使用“触发器”；Stellaris Wiki 对应页面标题为 `Conditions`，但正文同时使用 `conditions` 与 `triggers`，存在页面级术语差异。 |
| **effect** | **效果** | 已收录 | 官方 Wiki、CWTools、插件一致。 |
| **scripted variable** | **封装变量** | 已收录 | 插件把 `@var` 统一称为“封装变量”，并按路径区分局部/全局；官方 Wiki 使用 `scripted variables` 与 `@ variables`。 |
| **localisation** | **本地化** | 已收录 | 插件主要使用英式拼写 `localisation`，同时兼容 `localization` 目录；CK3 游戏实际要求 `localization` 拼写。 |
| **define** | **定值** | 已收录 | 插件用“定值”区分 `definition`；CK3 社区中文翻译把 `defines` 译为“定义”，存在直接冲突。 |
| **definition** | **定义** | 已收录 | 插件把数据对象声明统一称为“定义”；官方 Wiki 更常用“游戏对象/条目”，CWTools 更常用“类型”。 |
| **scripted trigger** | **封装触发器** | 已收录 | 官方 Wiki 为 `scripted trigger`；社区中文翻译使用“脚本化触发器”。 |
| **scripted effect** | **封装效果** | 已收录 | 官方 Wiki 为 `scripted effect`；插件译法属于语义化翻译，非字面对应。 |
| **script value** | **脚本值** | 已收录 | 官方 Wiki 为 `script values`；插件与 CWTools 文档均使用“脚本值”。 |
| **game rule** | **游戏规则** | 已收录 | 官方 CK3 使用 `game rules`；插件“规则”同时指 CWT 规则，中文层面存在“规则”重载。 |
| **on action** | **动作触发** | 已收录 | 官方 Wiki 为 `on_action` / `on actions`；社区 CK3 中文翻译使用“触发动作”。 |
| **event** | **事件** | 已收录 | 官方 Wiki、CWTools、插件一致。 |
| **event namespace** | **事件命名空间** | 已收录 | 与“定值命名空间”共享“命名空间”一词，但英文来源分别为 `event namespace` 与 `define namespace`。 |
| **sprite** | **精灵** | 已收录 | 官方 Wiki 与插件一致；本地化图标可引用 sprite。 |
| **macro** | **宏** | 已收录 | 插件内部把宏分为“语法宏/语义宏”；CWTools 更常用 `alias` 描述可复用规则片段，官方 Wiki 则把 scripted trigger/effect 也泛称为 macro。 |
| **inline script** | **内联脚本** | 已收录 | 官方 Stellaris Wiki 使用 `inline scripts`；插件一致。 |
| **definition injection** | **定义注入** | 已收录 | 插件为 VIC3/EU5 特性自创的统一抽象；官方 Wiki 使用 `INJECT:` / `REPLACE:` 与 `database entry` 相关表述。 |
| **config (CWT)** | **规则** | 已收录 | 插件把 `config` 译为“规则”；CWTools 英文文档同时使用 `config` 与 `rules`，并未固定为单一词。 |
| **config group** | **规则分组** | 已收录 | 插件特有概念；CWTools 没有同名的“config group”抽象。 |

### 1.2 建议新增的通用领域词条

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **game type** | 游戏类型 | 建议新增 | UI 设置、文档与 `ParadoxGameType` 高频出现。 |
| **game version** | 游戏版本 | 建议新增 | UI 设置与 `ParadoxRootInfo` 高频出现。 |
| **game directory** | 游戏目录 | 建议新增 | UI 设置与快速开始文档高频出现。 |
| **mod** | 模组 | 建议新增 | 全项目高频；社区中文翻译亦使用“模组”。 |
| **mod descriptor** | 模组描述符 | 建议新增 | `quick-start.md` 使用“模组描述符文件（`descriptor.mod`）”。 |
| **mod source** | 模组来源 | 建议新增 | `ParadoxModSource` 区分本地、Steam、Paradox Mods。 |
| **root info** | 游戏或模组信息 | 建议新增 | `ParadoxRootInfo` 的 KDoc 使用“游戏或模组信息”。 |
| **file info** | 文件信息 | 建议新增 | `ParadoxFileInfo` 的 KDoc 使用“文件信息”。 |
| **file group** | 文件分组 | 建议新增 | `ParadoxFileGroup` 与文件类型识别逻辑使用。 |
| **entry** | 入口（目录） | 建议新增 | 覆盖规则、文件信息与规则路径均以入口目录为基准。 |
| **member path** | 成员路径 | 建议新增 | `ParadoxMemberPath` 的 KDoc 使用“脚本成员在脚本文件中的路径”。 |
| **event target** | 事件目标 | 待定夺 | 动态值文档使用“事件目标”，链接规则 KDoc 使用“事件对象引用（event target reference）”，两种译法并存。 |
| **variable** | 变量 | 建议新增 | 指作用域变量；与“封装变量”形成对照，需在术语表中明确区分。 |
| **flag** | 标志 | 建议新增 | 插件把 flag 作为动态值的一种；官方 Wiki 通常直接使用 `flag`。 |
| **scope type** | 作用域类型 | 建议新增 | 官方 Wiki、CWTools、插件规则均使用 scope type。 |
| **scope group** | 作用域分组 | 建议新增 | 插件规则类型与 UI 均使用；CWTools 兼容。 |
| **system scope** | 系统作用域 | 建议新增 | 官方 Stellaris Wiki 使用 system scopes；插件与 CWTools 一致。 |
| **scope context** | 作用域上下文 | 建议新增 | UI 设置、快速文档、检查描述高频出现。 |
| **scope switch** | 作用域切换 | 建议新增 | 官方 Wiki 使用 scope switch；插件检查描述使用“作用域切换”。 |
| **scope link** | 作用域链接 | 建议新增 | 官方 Wiki 使用 scope links；插件链接规则 KDoc 使用“作用域链接”。 |
| **union scope** | 并集作用域 | 待定夺 | 插件内部用于描述带继承关系的作用域匹配/合并策略；与 IntelliJ 的 `GlobalSearchScope`（搜索作用域）无关。 |
| **modifier category** | 修正分类 | 建议新增 | CWTools 与插件规则均使用 modifier category。 |
| **define namespace** | 定值命名空间 | 建议新增 | 插件为 `defines` 分类块自创的术语；官方 Stellaris/CK3 更常用 category / section。 |
| **define variable** | 定值变量 | 建议新增 | 插件为单个 define 自创的术语；官方通常直接称 `define`。 |
| **local scripted variable** | 局部封装变量 | 建议新增 | `ParadoxScriptedVariableType.Local`；与全局封装变量相对。 |
| **global scripted variable** | 全局封装变量 | 建议新增 | `ParadoxScriptedVariableType.Global`；覆盖规则与检查中高频出现。 |
| **synced localisation** | 同步本地化 | 建议新增 | 插件数据类型与规则类型均使用；官方使用 `localisation_synced`。 |
| **inline localisation** | 内联本地化 | 建议新增 | CWTools 使用 `localisation_inline`；插件数据类型使用“内联本地化引用”。 |
| **load order** | 加载顺序 | 建议新增 | 官方 Wiki 与社区中文翻译均使用；插件更多使用“覆盖策略/排序”。 |
| **file overwrite** | 文件覆盖 | 建议新增 | 官方 Wiki 使用 file overwrite；插件使用 override/覆盖策略描述。 |
| **injection and replacement** | 注入与替换 | 建议新增 | VIC3/EU5 官方 Wiki 的章节标题；插件统一抽象为“定义注入”。 |
| **database entry** | 数据库条目 | 待定夺 | 官方 VIC3/EU5 Wiki 使用 database entry；插件“数据库对象（database object）”是另一个概念，不能直接等同。 |
| **database object** | 数据库对象 | 待定夺 | 插件扩展概念，用于描述本地化中的 `civic:some_civic` 一类对象表达式；与官方 database entry 存在同词根冲突。 |
| **definition source** | 定义来源 | 建议新增 | `ParadoxDefinitionSource` 区分文件、属性、内联、注入。 |
| **inline definition** | 内联定义 | 建议新增 | 提案与 roadmap 使用“内联定义”；尚未进入术语表。 |
| **declaration** | 声明 | 建议新增 | 插件区分“定义（definition）”与“声明（declaration）”；`declaration config` 译为“声明规则”。 |
| **reference** | 引用 | 建议新增 | 与 `definition reference`、`localisation reference`、`reference snippet` 等组合词配套。 |
| **usage** | 用法 | 建议新增 | 与 `inline script usage`、`definition injection usage` 等组合词配套。 |
| **type key** | 类型键 | 建议新增 | 定义匹配的核心字段；与“定义名”不同。 |
| **root key** | 顶级键 | 建议新增 | 定义匹配与事件命名空间检查使用。 |
| **name field** | 名称字段 | 建议新增 | 类型规则用于解析定义名的字段。 |
| **subtype expression** | 子类型表达式 | 建议新增 | 声明规则与类型展示规则使用。 |
| **generated modifier** | 生成的修正 | 建议新增 | 类型规则可生成修正；检查与快速文档使用。 |
| **related localisation** | 相关本地化 | 建议新增 | 快速文档、装订线图标、缺失本地化检查高频出现。 |
| **related image** | 相关图片 | 建议新增 | 快速文档、装订线图标、缺失图片检查高频出现。 |
| **primary localisation** | 主要本地化 | 建议新增 | 类型展示规则中的 `primary` 选项。 |
| **primary image** | 主要图片 | 建议新增 | 类型展示规则中的 `primary` 选项。 |

### 1.3 通用领域术语的主要风险

- **`define` / `definition` 的中文冲突**：插件用“定值”和“定义”强行区分两个英文近形词，但社区中文翻译把 `defines` 译为“定义”，把游戏对象译为“游戏对象”；新用户极易把插件的“定义”理解为“定值”。
- **`definition` / `game object` / `entry` / `type` 的跨源冲突**：同一个“数据对象声明”在插件中叫“定义”，在官方 Wiki 中叫“游戏对象/条目”，在 CWTools 中叫“类型”，在 Tiger 中叫“item”。这不是单纯译名差异，而是抽象层级不同。
- **`variable` / `scripted variable` 的层级差异**：插件用“变量”指作用域变量，用“封装变量”指 `@` 变量；官方 Wiki 的 `Variables` 页面主要讲作用域变量，`scripted variables` 才对应 `@` 变量；术语表需要明确这两个层级。
- **`event namespace` / `define namespace` 的“命名空间”重载**：两者英文不同，但中文都含“命名空间”；如果省略限定词，容易混淆事件命名空间与定值命名空间。
- **`game rule` / `config` 的“规则”重载**：插件把 CWT config 译为“规则”，把 game rule 译为“游戏规则”；单独说“规则”时，读者无法判断是 CWT 规则还是游戏规则。
- **`override` 的多重含义**：覆盖规则、覆盖策略、覆盖优先级、重载/被重载、官方 overwrite 都落在“覆盖/重载”语义场中，中文 UI 已同时使用“覆盖”和“重载”。
- **`scope` 的多重含义**：Paradox 作用域、作用域上下文、并集作用域、IntelliJ 搜索作用域、协程作用域、解析作用域共用“作用域”一词，跨层阅读时需要限定语。
- **`injection` 的多重含义**：定义注入、语言注入、代码注入、规则注入、注入的规则分组、注入目标共用“注入”，其中至少三种属于完全不同的子系统。
- **`localisation` / `localization` 拼写差异**：插件功能名使用英式拼写，游戏目录与日志文件名使用美式拼写；代码已兼容两者，但文档与 UI 需要明确“功能名”和“实际路径”的区别。

---

## 二、脚本与本地化语言构造

### 2.1 脚本语法构造

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **property** | 属性 | 建议新增（guidelines 已有译法） | `guidelines.md` 明确要求使用 property，而非 attribute；脚本与 CWT 共用。 |
| **value** | 值 | 建议新增（guidelines 已有译法） | `guidelines.md` 使用“值/value”。 |
| **direct value** | 直接的值 | 建议新增（guidelines 已有译法） | `guidelines.md` 与 `language-abstractions.md` 使用；旧称“单独的值”已重命名。 |
| **block** | 块 | 建议新增 | 脚本与 CWT 的基础语法元素；文档高频出现。 |
| **member** | 成员 | 建议新增 | 属性与块值统称成员；`language-abstractions.md` 使用。 |
| **statement** | 语句 | 建议新增 | `ParadoxScriptStatement` KDoc 明确“包括成员、封装变量声明、条件化块”。 |
| **expression** | 表达式 | 建议新增 | 与“规则表达式/数据表达式/复杂表达式”形成层级。 |
| **complex expression** | 复杂表达式 | 建议新增 | 指作用域字段、值字段、命令、本地化命令等轻量语法树。 |
| **parameter** | 参数 | 建议新增（guidelines 已有译法） | `$PARAM$` 形式；guidelines 明确不要称为“宏”。 |
| **argument** | 传入参数 | 建议新增 | 与参数相对；UI 使用“传入参数”，参数文档也使用“传入参数信息”。 |
| **conditional block** | 条件化块 | 待定夺 | `guidelines.md`、`language-abstractions.md` 与部分 KDoc 使用“条件化块”；UI 与多数 KDoc 使用“参数化块”。 |
| **inline conditional block** | 内联条件化块 | 待定夺 | 同上；UI 使用“内联参数化块”。 |
| **conditional expression** | 条件化表达式 | 待定夺 | UI 使用“参数化块表达式”；KDoc 使用“条件化表达式”。 |
| **interpolation** | 插值 | 建议新增 | `ParadoxScriptInterpolation` KDoc 使用“插值”；指内联形式的语法宏。 |
| **inline math** | 内联数学 | 建议新增 | UI 与文档高频使用；另有“内联数学块/内联数学表达式”。 |
| **syntax macro** | 语法宏 | 建议新增（guidelines 已有译法） | `guidelines.md` 把封装变量引用、参数、条件化块归类为语法宏。 |
| **semantic macro** | 语义宏 | 建议新增（guidelines 已有译法） | `guidelines.md` 把内联脚本、定义注入归类为语义宏；`CwtMacroConfig` KDoc 再次强调。 |
| **scripted variable reference** | 封装变量引用 | 建议新增 | UI 与 KDoc 高频使用。 |
| **scripted variable declaration** | 封装变量声明 | 建议新增 | 检查描述与规则文档使用。 |
| **parameter reference** | 参数引用 | 建议新增 | 用法统计与检查描述使用。 |
| **parameter value** | 参数值 | 建议新增 | 语言注入设置与参数文档使用。 |
| **event id** | 事件 ID | 建议新增 | 事件检查与文档高频使用。 |
| **event type** | 事件类型 | 建议新增 | 动作触发的扩展规则与事件检查使用。 |
| **inline script usage** | 内联脚本用法 | 建议新增 | UI 与检查描述高频使用。 |
| **definition injection mode** | 定义注入模式 | 建议新增 | 意向动作、规则与文档高频使用。 |
| **injection target** | 注入目标 | 建议新增 | UI 与检查描述高频使用；部分检查文案又误写为“定义目标”。 |
| **script value reference** | 脚本值引用 | 建议新增 | UI 检查描述与 `CwtDataTypes.ScriptValueReference` 使用。 |
| **define reference** | 定值引用 | 建议新增 | 检查描述与数据类型使用。 |
| **array define reference** | 数组定值引用 | 建议新增 | 检查描述与数据类型使用。 |
| **tags expression** | 标签集表达式 | 建议新增 | UI 检查描述与 `CwtDataTypes.Tags` 使用；旧称 `dynamicValueSetExpression` 已重命名。 |
| **tag type** | 标签类型 | 建议新增 | `ParadoxTagType` 区分预定义标签与类型键前缀。 |
| **dynamic value** | 动态值 | 建议新增 | 插件扩展概念；官方 Wiki 通常按 event target、variable、flag 分别描述。 |
| **dynamic value type** | 动态值类型 | 建议新增 | 规则类型与 UI 使用。 |
| **complex enum value** | 复杂枚举值 | 建议新增 | UI、检查与快速文档高频使用。 |
| **enum value** | 枚举值 | 建议新增 | 规则类型与检查描述使用。 |

### 2.2 脚本语义构造

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **definition** | 定义 | 已收录 | 见 1.1；语义级别的数据对象声明。 |
| **definition member** | 定义的成员 | 建议新增 | `ParadoxDefinitionInfo` 与声明规则文档使用。 |
| **definition name** | 定义名 | 建议新增 | 快速文档、补全、重命名高频使用。 |
| **definition type** | 定义类型 | 建议新增 | 检查描述与层级视图使用。 |
| **definition subtype** | 定义子类型 | 建议新增 | 检查描述与类型规则使用。 |
| **definition source** | 定义来源 | 建议新增 | `ParadoxDefinitionSource` 的 File/Property/Inline/Injection。 |
| **inline definition** | 内联定义 | 建议新增 | 提案与 roadmap 使用；与“内联脚本”相关但不同。 |
| **definition injection** | 定义注入 | 已收录 | 见 1.1；VIC3/EU5 语义宏。 |
| **inline script** | 内联脚本 | 已收录 | 见 1.1；Stellaris 语义宏。 |
| **scripted variable** | 封装变量 | 已收录 | 见 1.1；`@var` 声明与引用。 |
| **local scripted variable** | 局部封装变量 | 建议新增 | `ParadoxScriptedVariableType.Local`。 |
| **global scripted variable** | 全局封装变量 | 建议新增 | `ParadoxScriptedVariableType.Global`。 |
| **scripted trigger** | 封装触发器 | 已收录 | 见 1.1；可复用的条件宏。 |
| **scripted effect** | 封装效果 | 已收录 | 见 1.1；可复用的效果宏。 |
| **script value** | 脚本值 | 已收录 | 见 1.1；可计算的数值公式定义。 |
| **event** | 事件 | 已收录 | 见 1.1。 |
| **event namespace** | 事件命名空间 | 已收录 | 见 1.1。 |
| **event target** | 事件目标 | 待定夺 | 见 1.2；链接 KDoc 亦使用“事件对象引用”。 |
| **variable** | 变量 | 建议新增 | 见 1.2；作用域变量。 |
| **flag** | 标志 | 建议新增 | 见 1.2；动态值的一种。 |
| **on action** | 动作触发 | 已收录 | 见 1.1；社区中文翻译使用“触发动作”。 |
| **game rule** | 游戏规则 | 已收录 | 见 1.1。 |
| **complex enum** | 复杂枚举 | 建议新增 | UI、规则文档与代码高频使用；CWTools 同名。 |
| **dynamic value** | 动态值 | 建议新增 | UI、规则文档与代码高频使用；CWTools 的 value set 不能简单等同。 |
| **snippet** | 片段 | 建议新增 | 3.0.4 引入；指引用文本的局部片段，而非 IDE 代码片段。 |
| **definition reference snippet** | 定义引用片段 | 建议新增 | `CwtDataTypes.DefinitionSnippet` 与 ADR `snippet-match.md` 使用。 |
| **localisation reference snippet** | 本地化引用片段 | 建议新增 | `CwtDataTypes.LocalisationSnippet` 与 ADR `snippet-match.md` 使用。 |

### 2.3 本地化语言构造

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **localisation property** | 本地化属性 | 建议新增 | UI 语义描述使用 `localisation.description.type.property`。 |
| **localisation text** | 本地化文本 | 建议新增 | 全项目高频；与“展示文本”不同。 |
| **locale** | 语言环境 | 建议新增 | UI 设置、规则类型与检查描述均使用“语言环境”。 |
| **localisation command** | 本地化命令 | 建议新增 | 规则类型、检查描述与复杂表达式使用。 |
| **localisation command field** | 本地化命令字段 | 建议新增 | 链接规则 KDoc 使用；官方 Wiki 通常直接称 command。 |
| **localisation promotion** | 本地化提升 | 建议新增 | 规则类型与作用域匹配使用。 |
| **localisation link** | 本地化链接 | 建议新增 | 规则类型与链接规则使用。 |
| **rich text** | 富文本 | 建议新增 | 本地化 UI 设置与文档使用。 |
| **text format** | 文本格式 | 建议新增 | 本地化折叠、检查与文档使用。 |
| **text icon** | 文本图标 | 建议新增 | UI、折叠、检查与图片文档使用。 |
| **icon** | 图标 | 建议新增 | 本地化富文本与图片处理高频使用。 |
| **concept** | 概念 | 建议新增 | 本地化富文本与概念命令使用。 |
| **grammatical tag** | 语法标签 | 建议新增 | 本地化 UI 设置使用“语法标签”。 |
| **context tag** | 上下文标签 | 建议新增 | 本地化 UI 设置使用。 |
| **escape sequence** | 转义序列 | 建议新增 | 语法参考与 UI 高亮设置使用。 |
| **localisation group** | 本地化分组 | 建议新增 | 本地化生成设置使用；指文件内的分组，不是规则分组。 |
| **localisation parameter** | 本地化参数 | 建议新增 | 数据类型与折叠设置使用。 |
| **localisation reference** | 本地化引用 | 建议新增 | 语义高亮、链接与检查描述使用。 |
| **localisation reference snippet** | 本地化引用片段 | 建议新增 | 见 2.2。 |

### 2.4 语言构造术语的主要风险

- **“条件化块”与“参数化块”并存**：`guidelines.md` 与 `language-abstractions.md` 使用“条件化块”，UI、设置与多数 KDoc 使用“参数化块”，检查描述中还同时出现两者；这是当前最直接的内部术语冲突之一。
- **`parameter` / `argument` 的默认值描述**：插件使用“参数”与“传入参数”区分 `$PARAM$` 和实际传入值，但社区文档常把两者都叫“参数”；术语表应明确这一区分。
- **`macro` 的层级**：插件区分“语法宏”和“语义宏”，但 UI 与文档有时直接用“宏”；官方 Wiki 与 CWTools 对 macro 的使用范围也不同，容易把 `alias`、`scripted trigger`、`inline script` 都笼统称为宏。
- **`inline` 的多重修饰**：内联脚本、内联数学、内联本地化、内联条件化块、内联定义、内联重构共用“内联”，需要限定语。
- **`tags` / `tag` / `tag type` 的混用**：`Tags` 是标签集表达式，`tag` 是单个标签，`tag type` 是标签类型；UI 与数据类型均使用，但术语表未记录。
- **`dynamic value` 与 `value set` 的范围差异**：插件把 event target、variable、flag 等统一抽象为动态值；CWTools 的 value set 更接近按使用收集的取值集合，二者不能简单互译。
- **`event target` 的两种中文**：动态值文档使用“事件目标”，链接规则 KDoc 使用“事件对象引用”；建议至少在同一文档内统一，或标注两者为同一概念的不同视角。

---

## 三、CWT 规则与语义系统术语

### 3.1 规则体系分层

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **config** | 规则 | 已收录 | 见 1.1；CWT 规则文件中的统一领域模型。 |
| **config file** | 规则文件 | 建议新增 | UI、文档与 KDoc 高频使用；英文代码仍使用 `config file`。 |
| **config system** | 规则系统 | 建议新增 | UI 设置分组与文档标题使用；英文代码使用 config system。 |
| **config group** | 规则分组 | 已收录 | 见 1.1。 |
| **config group source** | 规则分组来源 | 建议新增 | 内置、远程、本地、项目本地、注入。 |
| **config repository** | 规则仓库 | 建议新增 | 远程规则分组的 Git 仓库；UI 使用“远程仓库地址”。 |
| **config context** | 规则上下文 | 建议新增 | UI、内联脚本推断、规则匹配与检查高频使用。 |
| **standard config** | 标准规则 | 建议新增 | `docs/zh/ref-config-format.md` 的规则分类。 |
| **extended config** | 扩展规则 | 建议新增 | 同上；用于增强提示、上下文和声明改写。 |
| **internal config** | 内部规则 | 建议新增 | 同上；目前主要供插件内部使用。 |
| **config type** | 规则类型 | 建议新增 | `CwtConfigType` 与 UI 描述使用。 |
| **config category** | 规则分类 | 建议新增 | `CwtConfigTypeCategories` 使用。 |
| **override config** | 覆盖规则 | 建议新增 | `CwtOverrideConfig` 的 KDoc 使用；3.0.4 前称“优先级规则”。 |
| **override strategy** | 覆盖策略 | 建议新增 | `ParadoxOverrideStrategy` 的 KDoc 使用；UI 与规则文件高频出现。 |
| **override priority** | 覆盖优先级 | 建议新增 | 差异比较分组标签使用“最高/最低覆盖优先级”；与覆盖策略不同。 |

### 3.2 规则类型

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **type config** | 类型规则 | 建议新增 | `config.description.type`；定义定位与命名的基础。 |
| **subtype config** | 子类型规则 | 建议新增 | `config.description.subtype`；按条件裁剪声明规则。 |
| **row config** | 行规则 | 建议新增 | `config.description.row`；CSV 行声明。 |
| **define namespace config** | 定值命名空间规则 | 建议新增 | `config.description.defineNamespace`。 |
| **define variable config** | 定值变量规则 | 建议新增 | `config.description.defineVariable`。 |
| **enum config** | 枚举规则 | 建议新增 | `config.description.enum`。 |
| **enum value config** | 枚举值规则 | 建议新增 | `config.description.enumValue`。 |
| **complex enum config** | 复杂枚举规则 | 建议新增 | `config.description.complexEnum`；按路径与锚点动态收集。 |
| **union config** | 并集规则 | 建议新增 | `config.description.union`；插件扩展。 |
| **union value config** | 并集值规则 | 建议新增 | `config.description.unionValue`。 |
| **dynamic value type config** | 动态值类型规则 | 建议新增 | `config.description.dynamicValueType`。 |
| **dynamic value config** | 动态值规则 | 建议新增 | `config.description.dynamicValue`。 |
| **alias config** | 别名规则 | 建议新增 | `config.description.alias`；触发器、效果等以别名规则提供。 |
| **single alias config** | 单例别名规则 | 待定夺 | UI 使用“单例别名规则”；代码与文档使用“单别名右侧/single alias”。 |
| **macro config** | 宏规则 | 建议新增 | `config.description.macro`；插件文档强调其特指语义宏。 |
| **link config** | 链接规则 | 建议新增 | `config.description.link`；复杂表达式中的字段/函数样节点。 |
| **localisation link config** | 本地化链接规则 | 建议新增 | `config.description.localisationLink`。 |
| **localisation promotion config** | 本地化提升规则 | 建议新增 | `config.description.localisationPromotion`。 |
| **localisation command config** | 本地化命令规则 | 建议新增 | `config.description.localisationCommand`。 |
| **modifier config** | 修正规则 | 建议新增 | `config.description.modifier`。 |
| **modifier category config** | 修正分类规则 | 建议新增 | `config.description.modifierCategory`。 |
| **trigger config** | 触发器规则 | 建议新增 | `config.description.trigger`；以别名规则形式提供。 |
| **effect config** | 效果规则 | 建议新增 | `config.description.effect`；以别名规则形式提供。 |
| **scope config** | 作用域规则 | 建议新增 | `config.description.scope`。 |
| **scope group config** | 作用域分组规则 | 建议新增 | `config.description.scopeGroup`。 |
| **database object type config** | 数据库对象类型规则 | 建议新增 | `config.description.databaseObjectType`；插件扩展。 |
| **system scope config** | 系统作用域规则 | 建议新增 | `config.description.systemScope`。 |
| **locale config** | 语言环境规则 | 建议新增 | `config.description.locale`。 |

### 3.3 规则表达式

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **config expression** | 规则表达式 | 建议新增 | `CwtConfigExpression` 的 KDoc 与规则格式文档使用。 |
| **data expression** | 数据表达式 | 建议新增 | 决定匹配逻辑与解析逻辑；规则中最常见的表达式。 |
| **template expression** | 模板表达式 | 建议新增 | 常量片段与动态片段拼接；`CwtTemplateExpression`。 |
| **cardinality expression** | 基数表达式 | 建议新增 | 约束成员出现次数；`CwtCardinalityExpression`。 |
| **location expression** | 位置表达式 | 建议新增 | 定位图片、本地化等资源；`CwtLocationExpression`。 |
| **schema expression** | 模式表达式 | 建议新增 | 描述规则文件本身取值形态；`CwtSchemaExpression`。 |
| **constant** | 常量 | 建议新增 | 模式表达式的一种形态；也用于模板片段。 |
| **template** | 模板 | 建议新增 | 模式表达式的一种形态；与“模板表达式”不同层级。 |
| **type** | 类型 | 建议新增 | 模式表达式的一种形态；与 `type config` 不同。 |
| **constraint** | 约束 | 建议新增 | 模式表达式的一种形态（`$$NAME`）；与模型路径/语法约束不同。 |
| **enum** | 枚举 | 建议新增 | 模式表达式的一种形态（`$enum:NAME$`）。 |
| **data type** | 数据类型 | 建议新增 | 决定数据表达式如何解析与匹配。 |
| **data expression role** | 数据表达式角色 | 建议新增 | 键/值/其他。 |
| **data expression metadata** | 数据表达式元数据 | 建议新增 | 范围、忽略大小写、模板参数等。 |
| **option metadata** | 选项元数据 | 建议新增 | 3.0.1 由 `optionData` 重命名；规则选项的元数据。 |
| **line comment** | 行注释 | 建议新增 | CWT 语言使用 `#`；UI 颜色设置与语法参考使用。 |
| **option comment** | 选项注释 | 建议新增 | CWT 语言使用 `##`；用于 cardinality、push_scope、hint 等选项。 |
| **documentation comment** | 文档注释 | 建议新增 | CWT 语言使用 `###`；用于快速文档文本。 |

### 3.4 核心数据类型

| 英文 | 插件中文 | 状态 | 备注 |
| :--- | :--- | :--- | :--- |
| **Any** | 任意类型 | 建议新增 | 最低优先级通配匹配。 |
| **Literal** | 字面量类型 | 建议新增 | 匹配任意字面量。 |
| **Bool** | 布尔类型 | 建议新增 | 匹配 `yes` / `no`。 |
| **Int** | 整数类型 | 建议新增 | 支持范围参数。 |
| **Float** | 浮点数类型 | 建议新增 | 支持范围参数。 |
| **Scalar** | 标量类型 | 建议新增 | 匹配任意字面量，作为键时总是匹配。 |
| **Block** | 块类型 | 建议新增 | 匹配脚本块并递归匹配内容。 |
| **ColorField** | 颜色字段类型 | 建议新增 | 匹配 `rgb { ... }` 等颜色字段。 |
| **DateField** | 日期字段类型 | 建议新增 | 匹配日期字段。 |
| **PercentageField** | 百分比字段类型 | 建议新增 | 匹配百分比字段。 |
| **Definition** | 定义引用 | 建议新增 | 引用定义；可附带类型/子类型元数据。 |
| **Localisation** | 本地化引用 | 建议新增 | 引用普通本地化。 |
| **SyncedLocalisation** | 同步本地化引用 | 建议新增 | 引用 `localisation_synced` / `localization_synced`。 |
| **InlineLocalisation** | 内联本地化引用 | 建议新增 | 引用本地化键或内联本地化文本。 |
| **Modifier** | 修正引用 | 建议新增 | 引用修正。 |
| **EnumValue** | 枚举值 | 建议新增 | 引用简单枚举或复杂枚举值。 |
| **Value** | 动态值读取 | 建议新增 | 匹配动态值读取；CWTools 兼容。 |
| **ValueSet** | 动态值写入 | 建议新增 | 匹配动态值写入；CWTools 兼容。 |
| **DynamicValue** | 动态值 | 建议新增 | 读写不区分；插件扩展。 |
| **ScopeField** | 作用域字段 | 建议新增 | 匹配 `root.owner` 一类作用域字段表达式。 |
| **Scope** | 作用域 | 建议新增 | 引用作用域类型。 |
| **ScopeGroup** | 作用域组 | 建议新增 | 引用作用域分组。 |
| **ValueField** | 值字段 | 建议新增 | 匹配数值或值引用。 |
| **VariableField** | 变量字段 | 建议新增 | 匹配变量引用。 |
| **Command** | 命令表达式 | 建议新增 | 匹配命令表达式。 |
| **ScriptValueReference** | 脚本值引用表达式 | 建议新增 | 匹配 `script_value[...]`。 |
| **DefineReference** | 定值引用表达式 | 建议新增 | 匹配 `Namespace\|Name`。 |
| **ArrayDefineReference** | 数组定值引用表达式 | 建议新增 | 匹配 `Namespace\|Name\|Index`。 |
| **Tags** | 标签集表达式 | 建议新增 | 匹配逗号分隔的动态值节点。 |
| **DatabaseObject** | 数据库对象表达式 | 建议新增 | 匹配 `civic:x` 一类对象表达式；插件扩展。 |
| **NameFormat** | 命名格式表达式 | 建议新增 | 匹配命名格式文本。 |
| **Parameter** | 参数名 | 建议新增 | 匹配参数名。 |
| **ParameterValue** | 参数值 | 建议新增 | 匹配参数值。 |
| **LocalisationParameter** | 本地化参数名 | 建议新增 | 匹配本地化参数名。 |
| **Template** | 模板表达式 | 建议新增 | 匹配模板字符串。 |
| **DefinitionSnippet** | 定义引用片段 | 建议新增 | 匹配定义引用的局部片段。 |
| **LocalisationSnippet** | 本地化引用片段 | 建议新增 | 匹配本地化引用的局部片段。 |
| **Icon** | 图标路径 | 建议新增 | 匹配图标路径。 |
| **FilePath** | 文件路径 | 建议新增 | 匹配文件路径。 |
| **FileName** | 文件名 | 建议新增 | 匹配文件名。 |
| **AbsoluteFilePath** | 绝对文件路径 | 建议新增 | 匹配绝对路径。 |
| **ShaderEffect** | 着色器效果 | 建议新增 | 匹配着色器效果引用。 |
| **MeshLocator** | 网格定位器 | 建议新增 | 匹配网格定位器引用。 |
| **UnionValue** | 并集值 | 建议新增 | 匹配并集规则中的候选项。 |
| **AliasKeysField** | 别名键字段 | 建议新增 | 匹配别名规则的键。 |
| **AliasName** | 别名名称 | 建议新增 | 匹配别名名称。 |
| **AliasMatchLeft** | 别名匹配左侧 | 建议新增 | 匹配 `alias_match_left[...]`。 |
| **SingleAliasRight** | 单别名右侧 | 建议新增 | 匹配 `single_alias_right[...]`。 |
| **Glob** | GLOB 模式 | 建议新增 | 模式感知数据类型。 |
| **Ant** | ANT 路径模式 | 建议新增 | 模式感知数据类型。 |
| **Regex** | 正则表达式模式 | 建议新增 | 模式感知数据类型。 |

### 3.5 匹配、解析与覆盖

| 英文 | 插件中文 | 状态 | 主要证据与备注 |
| :--- | :--- | :--- | :--- |
| **match** | 匹配 | 建议新增 | 与“解析”并列，但语义不同。 |
| **matcher** | 匹配器 | 建议新增 | 表达式匹配器、相似度匹配器等。 |
| **resolve** | 解析 | 建议新增 | 引用解析、规则解析、链接解析。 |
| **resolver** | 解析器 | 建议新增 | 与 parser 的中文“解析器”冲突。 |
| **expand** | 展开 | 建议新增 | 并集规则、别名规则、模板展开。 |
| **inline** | 内联 | 建议新增 | 规则内联、别名内联、重构内联。 |
| **template match** | 模板匹配 | 建议新增 | 常量片段与动态片段逐个匹配。 |
| **snippet match** | 片段匹配 | 建议新增 | 3.0.4 引入；宽松匹配局部引用片段。 |
| **union match** | 并集匹配 | 建议新增 | 递归展开并依次尝试候选项。 |
| **union scope matching** | 并集作用域匹配 | 待定夺 | 带继承关系的作用域匹配；与 union config 不同。 |
| **reference resolution** | 引用解析 | 建议新增 | PSI 引用与规则引用解析。 |
| **CWTools compatibility** | CWTools 兼容性 | 建议新增 | 规则 KDoc 与参考文档高频出现；分为兼容、部分兼容、不兼容。 |
| **extension-provided** | 作为扩展提供 | 建议新增 | 规则 KDoc 对不兼容项的常见说明，表示插件自行扩展。 |
| **config expansion** | 规则展开 | 建议新增 | `CwtConfigExpansionService`。 |
| **config inline** | 规则内联 | 建议新增 | `CwtConfigInlineService`。 |
| **rule injection** | 规则注入 | 建议新增 | `## inject` 选项；与定义注入、语言注入不同。 |
| **override** | 覆盖 | 建议新增 | 与 overwrite、重载/被重载、覆盖策略共同构成语义场。 |
| **overriding/overridden** | 重载/被重载 | 待定夺 | UI 高频使用；与“覆盖”并列但语义相关。 |
| **FIOS** | 只读一次（First In, Only Served） | 建议新增 | 官方 Stellaris Wiki 使用同名缩写；插件策略枚举与规则文件使用。 |
| **LIOS** | 后读覆盖（Last In, Only Served） | 建议新增 | 默认策略；官方 Wiki 与 IMM 均使用。 |
| **DUPL** | 整文件覆盖（Duplicates） | 建议新增 | 必须用同路径文件整体替换。 |
| **ORDERED** | 顺序读取（Ordered） | 建议新增 | 按序新增或合并，不覆盖既有条目。 |
| **priority** | 优先级 | 建议新增 | 仍用于数据类型、补全、内嵌提示等非覆盖语义。 |

### 3.6 CWT 术语的主要风险

- **`config` / `rule` / `game rule` 三层重载**：插件把 CWT config 译为“规则”，把 game rule 译为“游戏规则”，但 CWTools 英文文档本身就把 config 和 rules 混用；中文读者很难仅凭“规则”判断层级。
- **`constraint` 的三种含义**：模式表达式中的 `$$NAME` 约束、基数/范围约束、`ParadoxPathConstraint` 等模型约束都叫“约束”；术语表需要区分语法层、规则层和模型层。
- **`template` 的多层含义**：模板表达式、模式表达式中的 Template、补全的子句模板、IntelliJ 文件模板/实时模板、参数模板都可能简称为“模板”。
- **`schema` 与 `pattern` 都译作“模式”**：模式表达式（schema expression）与路径模式/模式感知（pattern）在中文里都含“模式”，需要限定语。
- **`priority` 的历史包袱**：3.0.4 把“优先级规则”重命名为“覆盖规则”，但 `priority` 仍广泛用于非覆盖语义；术语表应明确“覆盖优先级”只是 `priority` 的一个使用场景。
- **`override config` / `override strategy` / `overriding/overridden` 三者不同**：覆盖规则是规则文件中的配置，覆盖策略是策略枚举，重载/被重载是目标之间的关系；UI 当前用“覆盖”和“重载”两个词承载，需明确边界。
- **`single alias` 的两种中文**：UI 使用“单例别名规则”，代码与文档使用“单别名右侧”；“单例”在编程语境中通常指 singleton，容易误解。
- **`dynamic value` 与 `value set` 的范围差异**：插件的 `Value` / `ValueSet` 数据类型与 CWTools 兼容，但 `DynamicValue` 是插件扩展；`CwtDynamicValueTypeConfig` 又提供预定义动态值，三者不是同一层级。
- **`database object` 与 `database entry` 的冲突**：插件的数据库对象表达式用于本地化中的 `civic:x`，官方 database entry 用于 `INJECT:` / `REPLACE:` 的目标条目；两者共享“数据库”前缀但语义不同。
- **`injection` 的多种含义**：规则注入（`## inject`）、定义注入（VIC3/EU5）、语言注入（IntelliJ）、代码注入（内部 EP 架构）、注入的规则分组（配置来源）都使用“注入”，是当前最危险的跨子系统术语之一。

---

## 四、跨游戏、跨工具与官方 Wiki 术语对照

### 4.1 官方 Wiki 术语基线

以下表格用于对照外部来源，不作为插件术语表收录状态的判断依据；插件侧状态见第一至第三节。

| 游戏 | 关键英文术语 | 主要页面 | 与插件的关系 |
| :--- | :--- | :--- | :--- |
| **Stellaris** | `defines`、`scopes`、`scope type`、`system scopes`、`scope switch`、`chaining scopes`、`conditions`、`triggers`、`effects`、`modifiers`、`variables`、`scripted variables`、`scripted effects`、`scripted triggers`、`script values`、`inline scripts`、`on actions`、`localisation`、`overwriting`、`FIOS`、`LIOS` | `Modding`、`Defines`、`Scopes`、`Conditions`、`Effects`、`Dynamic modding`、`Localisation modding`、`On actions` | 插件大量术语与 Stellaris 对齐；但 `Conditions` 页面标题与插件“触发器”不同，`defines` 的分类在官方称 category/section，而插件称 namespace。 |
| **CK3** | `game objects`、`single object override`、`full file override`、`defines`、`category`、`scripted triggers`、`trigger`、`effect`、`scopes`、`on_action`、`localization` | `Modding`、`Modifiers`、`Game rules` | 插件“定义”对应官方“游戏对象”；插件“定值命名空间”对应官方 define 的分类（category）；CK3 本地化目录必须使用美式 `localization`。 |
| **VIC3** | `game objects`、`database entry`、`database entry keywords`、`INJECT:`、`REPLACE:`、`file overwrites`、`file loading order`、`game object load order`、`localization`、`replace` 目录 | `Mod compatibility`（`Mod files load order` 重定向） | 插件“定义注入”覆盖官方 `INJECT:` / `REPLACE:` 语义；插件“数据库对象”与官方 `database entry` 不是同一概念。 |
| **EU5** | `game objects`、`database entry`、`database entry keywords`、`INJECT:`、`REPLACE:`、`file overwrites`、`file loading order`、`game object load order`、`on actions`、`defines`、`localization` | `Mod compatibility`（`Mod files load order` 重定向） | 与 VIC3 基本一致；EU5 的 `defines` 支持单值替换，插件的“定值变量”抽象可以表达该行为。 |

补充观察：

- 官方 Wiki 更倾向使用 **game object / entry / database entry** 描述具体数据对象，而不是把 `definition` 当作统一术语；Tiger 则更多使用 **item**。
- 官方 Wiki 在 Stellaris 页面使用 **overwriting**，在 CK3 页面使用 **override rules**，在 VIC3/EU5 页面使用 **file overwrites** 与 **inject and replace**；英文本身并不统一。
- 官方 Wiki 在 Stellaris 页面使用 **conditions/triggers** 两种说法，在 CK3 页面使用 **trigger**，在 VIC3/EU5 页面使用 **trigger** 与 **effect**。
- 官方 Wiki 的 `localisation` / `localization` 拼写随游戏与页面变化，但实际目录要求以游戏为准。

### 4.2 CWTools 术语对照

| CWTools 英文 | 插件中文 | 状态 | 备注 |
| :--- | :--- | :--- | :--- |
| `.cwt config file` / `rules` | 规则文件 / 规则 | 已收录（config） | CWTools 英文同时使用 `config` 与 `rules`；插件固定译为“规则”。 |
| `schema` | 规则 / 模式 | 建议新增 | CWTools 把 `.cwt` 描述为 schema；插件把 schema expression 译为“模式表达式”。 |
| `type` | 类型规则 / 定义 | 建议新增 | CWTools 的 `type[technology]` 对应插件的类型规则，其实例对应插件的“定义”。 |
| `subtype` | 子类型 | 建议新增 | 插件与 CWTools 一致。 |
| `enum` | 枚举 | 建议新增 | 插件与 CWTools 一致。 |
| `complex_enum` | 复杂枚举 | 建议新增 | 同名概念；插件部分兼容但处理逻辑不同。 |
| `value_set` / `value` | 动态值写入 / 动态值读取 | 待定夺 | CWTools 的 value set 与插件 `DynamicValue` 不是同一层级；插件 `Value` / `ValueSet` 数据类型与 CWTools 兼容。 |
| `alias` / `single-alias` | 别名 / 单例别名 | 待定夺 | CWTools 使用 `single-alias`；插件 UI 使用“单例别名规则”，文档又使用“单别名”。 |
| `cardinality` | 基数 | 建议新增 | 插件与 CWTools 一致。 |
| `options` | 选项 / 选项注释 | 建议新增 | CWTools 使用 `##` 选项；插件使用“选项注释/选项元数据”。 |
| `scopes` / `scope` | 作用域 / 作用域规则 | 建议新增 | 插件与 CWTools 一致。 |
| `links` | 链接规则 | 建议新增 | 插件另提供 localisation_links、localisation_promotions、localisation_commands 等扩展规则。 |
| `modifiers` / `modifier_categories` | 修正 / 修正分类 | 建议新增 | 插件与 CWTools 一致。 |
| `localisation_commands` | 本地化命令 | 建议新增 | CWTools 文档主要描述 pre-Jomini；插件扩展到当前游戏。 |
| `script docs` | 脚本文档 | 建议新增 | 游戏生成的 triggers/effects/localizations 日志；插件工具模块也使用这些日志生成规则。 |
| `validation` / `completion` / `GoToType` | 代码检查 / 补全 / 导航到类型 | 建议新增 | CWTools 经 cwtools-vscode 作为 LSP 使用 validation/completion/导航能力；插件映射为 inspection/completion/navigation。 |
| `overwritten` / `overwrote` | 被重载 / 重载 | 待定夺 | CWTools 使用 overwrite 状态描述加载顺序；插件使用 override strategy 与“重载/被重载”。 |

### 4.3 Tiger 术语对照

| Tiger 英文 | 插件中文 | 状态 | 备注 |
| :--- | :--- | :--- | :--- |
| `validator` | 检查工具 / 验证器 | 建议新增 | 插件集成设置使用“检查工具”；Tiger 自身称 validator。 |
| `report` | 检查结果 / 报告 | 建议新增 | Tiger 输出 report；插件映射为 IDE inspection 结果。 |
| `key` | 检查项键 / 报告键 | 建议新增 | 例如 `missing-localization`；插件 UI 不直接暴露。 |
| `severity` | 严重度 | 建议新增 | 插件 Tiger 集成 UI 使用“严重度”。 |
| `confidence` | 置信度 | 建议新增 | 插件 Tiger 集成 UI 使用“置信度”。 |
| `Tips` | 提示 | 建议新增 | Tiger 严重度之一。 |
| `Untidy` | 不整洁 | 建议新增 | Tiger 严重度之一；中文译法保留英文括注。 |
| `Warning` | 警告 | 建议新增 | 与 IDE 高亮级别对应。 |
| `Error` | 错误 | 建议新增 | 与 IDE 高亮级别对应。 |
| `Fatal` | 致命 | 建议新增 | 与 IDE 高亮级别对应。 |
| `Weak` / `Reasonable` / `Strong` | 低 / 中 / 高 | 建议新增 | 插件 UI 使用“低（Weak）”等。 |
| `syntax validation` | 语法验证 | 建议新增 | Tiger 功能描述；插件对应“语法检查”。 |
| `missing items` | 缺失项 | 建议新增 | Tiger 功能描述；插件对应“无法解析的表达式/缺少定义”等检查。 |
| `missing localizations` | 缺少本地化 | 建议新增 | 插件同名检查。 |
| `scope consistency checking` | 作用域一致性检查 | 建议新增 | 插件对应“作用域检查/作用域切换检查”。 |
| `suppression` / `ignore` | 禁用代码检查 / 抑制 | 建议新增 | Tiger 使用 ignore/suppress；插件使用“禁用代码检查”。 |
| `localization` | 本地化 | 建议新增 | Tiger 使用美式拼写；插件功能名使用英式拼写。 |

### 4.4 社区中文翻译证据（CK3 Wiki 中文翻译）

以下样本来自公开的 CK3 Wiki 中文翻译仓库，仅用于观察社区常用中文，不代表 Paradox 官方术语表；其中的“社区中文”列也不作为插件术语表收录状态的判断依据。

| 英文 | 社区中文 | 插件中文 | 差异 |
| :--- | :--- | :--- | :--- |
| `Modding` / `mod` | 模组制作 / 模组 | 模组 | 基本一致。 |
| `defines` | 定义 | 定值 | **高风险冲突**；社区把 defines 直接译为“定义”。 |
| `game objects` | 游戏对象 | 定义 | **高风险冲突**；插件“定义”对应社区“游戏对象”。 |
| `trigger` | 触发器 | 触发器 | 一致。 |
| `effect` | 效果 | 效果 | 一致。 |
| `scope` | 作用域 | 作用域 | 一致。 |
| `scripted trigger` | 脚本化触发器 | 封装触发器 | 语体不同；社区更贴近字面。 |
| `on_action` | 触发动作 | 动作触发 | 词序不同；两者均能达意。 |
| `file loading order` | 加载顺序 | 覆盖策略/排序 | 插件更抽象；社区更贴近引擎行为。 |
| `override` | 覆盖 | 覆盖/重载 | 插件同时使用“覆盖”和“重载”。 |
| `localization` | 本地化 | 本地化 | 概念一致；拼写与目录要求不同。 |
| `category`（define 的分类） | 分类名称 | 定值命名空间 | 插件“命名空间”是自创抽象。 |

补充观察：

- 社区中文翻译更倾向直译和贴近游戏文件字段，插件更倾向建立统一的领域抽象；两者服务对象不同，不能简单判断优劣。
- 社区翻译把 `defines` 译为“定义”，使插件“定义/定值”的区分在社区语境中反而增加理解成本。
- 社区翻译把 `scripted trigger` 译为“脚本化触发器”，与插件“封装触发器”形成语体差异；插件译法强调可复用封装，社区译法强调脚本化来源。

### 4.5 跨源分歧点清单

#### D1. `define` / `definition` 的中文冲突

- **插件现状**：`define` = 定值；`definition` = 定义。
- **外部来源**：CK3 社区中文翻译把 `defines` 译为“定义”；官方英文中 `define` 与 `definition` 本身近形。
- **影响**：高；新用户、规则作者和翻译贡献者都可能把“定义”理解为“定值”。
- **待定夺**：是否在术语表中增加“定义（definition，数据对象声明）”与“定义/定值（define，引擎变量）”的对照说明，或增加“避免使用‘定义’指代 define”的注释。

#### D2. `definition` / `game object` / `entry` / `type` 的实体命名

- **插件现状**：统一称“定义”。
- **外部来源**：官方 Wiki 多用 game object / entry；CWTools 用 type；Tiger 用 item。
- **影响**：高；跨工具阅读时需要额外映射。
- **待定夺**：是否在术语表中把“定义”标注为插件的领域抽象，并列出官方/CWTools/Tiger 的对应词。

#### D3. `definition injection` 与官方 `INJECT` / `REPLACE` / `database entry modes`

- **插件现状**：统一称“定义注入”，并抽象出注入模式、注入目标、宽松模式、替换模式、创建模式。
- **外部来源**：VIC3/EU5 官方 Wiki 使用 `INJECT:` / `REPLACE:` / `database entry keywords`；Issue #252 提到官方开发日志中的“database entry modes（数据库入口模式）”。
- **影响**：高；用户搜索官方术语时可能找不到“定义注入”。
- **待定夺**：是否在术语表中增加“定义注入（插件抽象）”与“数据库入口模式/INJECT、REPLACE（官方术语）”的对照，而不改动内部命名。

#### D4. `database object` 与 `database entry`

- **插件现状**：`database object` = 本地化中的 `civic:some_civic` 一类对象表达式，由 `database_object_types` 规则描述。
- **外部来源**：VIC3/EU5 的 `database entry` 指可被 `INJECT:` / `REPLACE:` 操作的数据条目。
- **影响**：高；两者共享“数据库”前缀但语义不同，容易被误认为同一概念。
- **待定夺**：是否在术语表中明确“数据库对象（本地化对象表达式）”与“数据库条目（官方注入目标）”的边界。

#### D5. `trigger` 与 `condition`

- **插件现状**：统一使用“触发器”，规则类型、检查描述、文档均如此。
- **外部来源**：Stellaris Wiki 对应页面标题为 `Conditions`，正文同时使用 conditions 与 triggers；CK3 与 CWTools 更常用 trigger。
- **影响**：中；Stellaris 用户可能更熟悉“条件”。
- **待定夺**：是否在 `trigger` 词条中注明 Stellaris Wiki 的 `Conditions` 页面与 `conditions/triggers` 混用现象。

#### D6. `scripted trigger` / `scripted effect` 的“封装”与“脚本化”

- **插件现状**：封装触发器 / 封装效果。
- **外部来源**：官方英文为 scripted trigger / scripted effect；社区 CK3 中文翻译使用“脚本化触发器”。
- **影响**：中；插件译法强调复用封装，社区译法强调脚本来源。
- **待定夺**：是否在术语表中把“封装”标注为语义化译法，并注明社区常见“脚本化”说法。

#### D7. `on action` 的“动作触发”与“触发动作”

- **插件现状**：动作触发。
- **外部来源**：官方英文为 on_action / on actions；社区 CK3 中文翻译使用“触发动作”。
- **影响**：中；词序不同但含义相近。
- **待定夺**：是否在术语表中注明两种词序，或保留“动作触发（on action）”并附社区别名。

#### D8. `override` / `overwrite` / “覆盖” / “重载”

- **插件现状**：覆盖规则、覆盖策略、覆盖优先级使用“覆盖”；overriding/overridden 目标使用“重载/被重载”。
- **外部来源**：Stellaris 使用 overwriting；CK3 使用 override rules；VIC3/EU5 使用 file overwrites；CWTools 使用 overwritten/overwrote；社区中文样本使用“覆盖”。
- **影响**：高；同一英文语义场在插件内部裂成“覆盖”和“重载”两个中文词。
- **待定夺**：是否把“覆盖”限定为策略/规则/动作，把“重载/被重载”限定为目标关系，并在术语表中明确说明；或评估是否统一为“覆盖”。

#### D9. `localisation` / `localization`

- **插件现状**：功能名、语言名、规则名主要使用英式 `localisation`；代码兼容 `localization` 目录；少量英文 UI 使用 `localization`。
- **外部来源**：CK3 实际目录要求 `localization`；Tiger 使用 `localization`；Stellaris Wiki 使用 `localisation`。
- **影响**：中；用户可能误以为插件只支持英式目录。
- **待定夺**：是否在术语表与用户文档中明确“功能名使用 localisation，实际目录同时兼容 localisation/localization”。

#### D10. `FIOS` / `LIOS` / `DUPL` / `ORDERED`

- **插件现状**：保留英文缩写，并给出中文解释“只读一次/后读覆盖/整文件覆盖/顺序读取”。
- **外部来源**：Stellaris Wiki 使用 FIOS / LIOS；CK3 使用 LIOS / FIOS；IMM 使用 FIOS/LIOS；ORDERED 是插件抽象。
- **影响**：低到中；缩写本身可跨源复用，但 ORDERED 的官方对应并不明确。
- **待定夺**：是否在术语表中注明 ORDERED 为插件抽象，并非所有官方 Wiki 都使用该词。

#### D11. `dynamic value` 与 `value set`

- **插件现状**：动态值、动态值类型、动态值读取（Value）、动态值写入（ValueSet）并存。
- **外部来源**：CWTools 使用 value set / value；官方 Wiki 按 event target、variable、flag 分别描述。
- **影响**：中；用户可能把插件的“动态值”与 CWTools 的“value set”直接等同。
- **待定夺**：是否在术语表中分别定义“动态值（插件统一抽象）”“值集（CWTools 来源）”“动态值读取/写入”。

#### D12. `single alias` 的“单例别名”与“单别名”

- **插件现状**：UI 使用“单例别名规则”，代码与文档使用“单别名右侧/single alias”。
- **外部来源**：CWTools 使用 `single-alias`。
- **影响**：低到中；“单例”容易被理解为 singleton。
- **待定夺**：是否统一为“单别名”，或至少在术语表中注明“单例别名”指 single alias，不是 singleton。

#### D13. `config` / `rules` / `schema`

- **插件现状**：统一译为“规则”。
- **外部来源**：CWTools 英文同时使用 config、rules、schema。
- **影响**：中；英文资料中的 config 与 rules 可能指同一事物，也可能指规则文件与规则条目。
- **待定夺**：是否在术语表中给出“config（规则文件/规则体系）”“rule（规则条目）”“schema（规则模式）”的层级说明。

#### D14. `macro` / `alias` / semantic macro

- **插件现状**：宏分为语法宏和语义宏；`macro config` 特指语义宏规则。
- **外部来源**：CWTools 更常用 alias 描述可复用规则片段；官方 Wiki 把 scripted trigger/effect 泛称为 macro。
- **影响**：中；同名概念的范围不同。
- **待定夺**：是否在术语表中明确“宏（插件）”“别名（CWTools 规则复用）”“语义宏（内联脚本/定义注入）”的边界。

#### D15. `conditional block` 与“条件化块/参数化块”

- **插件现状**：UI 与多数 KDoc 使用“参数化块”；guidelines 与 design 文档使用“条件化块”；检查描述混用。
- **外部来源**：CWTools 指南未系统描述 `[[PARAM]text]`；官方 Wiki 也未提供对应的中文术语。
- **影响**：高；这是插件内部可直接观察到的术语分裂。
- **待定夺**：是否统一为“条件化块”或“参数化块”，或在术语表中把另一个标为别名。

#### D16. `variable` / `scripted variable` / 作用域变量

- **插件现状**：变量 = 作用域变量；封装变量 = `@` 变量。
- **外部来源**：官方 Stellaris Wiki 的 `Variables` 页面主要讲作用域变量，`Dynamic modding` 页面讲 scripted variables。
- **影响**：中；用户可能把“变量”与“封装变量”混用。
- **待定夺**：是否在术语表中明确“变量（作用域变量）”与“封装变量（scripted variable）”的层级关系，并列出局部/全局封装变量。

#### D17. `injection` 的多重含义

- **插件现状**：至少六种用法并存。
  - 定义注入（`ParadoxDefinitionInjectionInfo`，VIC3/EU5 语义宏）。
  - 语言注入（IntelliJ language injection，参数值/本地化文本/路径）。
  - 代码注入（`icu.windea.pls.inject`，内部 EP 架构）。
  - 规则注入（`## inject`，CWT 规则解析阶段）。
  - 注入的规则分组（配置来源 `Injected`）。
  - 注入目标（定义注入的 target）。
- **外部来源**：官方使用 INJECT / database entry；IntelliJ 使用 language injection；CWTools 未统一使用 inject。
- **影响**：高；跨子系统搜索与文档阅读时极易混淆。
- **待定夺**：是否在术语表中为每种“注入”增加限定语，并明确其所属子系统。

#### D18. `game rule` 与 CWT `rule`

- **插件现状**：`game rule` = 游戏规则；CWT `config` = 规则；单独说“规则”时含义不确定。
- **外部来源**：官方 CK3 使用 game rules；CWTools 使用 config/rules。
- **影响**：中；中文“规则”本身没有层级信息。
- **待定夺**：是否在术语表中把“规则”限定为 CWT 规则，把“游戏规则”固定为 game rule，并建议在文档中避免单独使用“规则”。

#### D19. Tiger `report` / `key` / `severity` / `confidence` 与 IDE `inspection`

- **插件现状**：Tiger 集成把 report 映射为 inspection 结果，UI 使用“严重度”“置信度”“高亮级别”。
- **外部来源**：Tiger 使用 report/key/severity/confidence；IDE 使用 inspection/problem/highlight severity。
- **影响**：中；跨工具用户需要理解两套术语。
- **待定夺**：是否在集成文档中增加 Tiger 术语与 IDE 术语的对照表。

#### D20. 社区中文与插件中文的系统性差异

- **插件现状**：偏好语义化、统一抽象（定值、封装变量、封装触发器、动作触发、定义注入）。
- **外部来源**：社区翻译偏字面、贴近游戏字段（定义、脚本化触发器、触发动作、覆盖、加载顺序）。
- **影响**：中到高；插件文档与社区教程之间存在学习迁移成本。
- **待定夺**：是否在术语表中增加“插件译法 / 社区常见说法 / 官方英文”三列，而不是只保留两列。

---

## 五、常用 IntelliJ IDE 平台工程术语（中英文对照）

### 5.1 用户可见语言功能

| 英文 | 插件中文 | 状态 | 备注 |
| :--- | :--- | :--- | :--- |
| **inspection** | 代码检查 | 建议新增 | 插件 UI、README、快速开始均使用“代码检查”。 |
| **inspection profile** | 检查配置文件 | 建议新增 | 快速开始使用“配置文件（profile）”；与 CWT“规则文件”不同。 |
| **intention** | 意向 / 意图 | 待定夺 | README 使用“意向动作”，`docs/zh/ai.md` 与 `integrations.md` 使用“意图”；UI intention bundle 本身不显示类别词。 |
| **intention action** | 意向动作 | 待定夺 | README 使用；与“意图动作”并存。 |
| **quick fix** | 快速修复 | 建议新增 | UI `fix.*` 词条与检查描述使用。 |
| **quick documentation** | 快速文档 | 建议新增 | UI 设置、文档与 README 高频使用。 |
| **inlay hint** | 内嵌提示 | 建议新增 | 插件统一使用“内嵌提示”；英文为 inlay hints。 |
| **hint text** | 提示文本 | 建议新增 | 扩展规则 `## hint` 与设置使用。 |
| **gutter icon** | 装订线图标 | 建议新增 | UI、README 与检查描述使用。 |
| **code completion** | 代码补全 | 建议新增 | UI 设置、README 与文档高频使用。 |
| **clause template** | 子句模板 | 建议新增 | 补全设置与补全项使用。 |
| **code folding** | 代码折叠 | 建议新增 | UI 设置与文档使用。 |
| **structure view** | 结构视图 | 建议新增 | UI 设置与脚本结构视图使用。 |
| **call hierarchy** | 调用层级 | 建议新增 | README、快速开始与设置使用；平台中文资料常见“调用层次结构”。 |
| **code hierarchy** | 代码层级 | 建议新增 | README 与设置分组使用；是 call hierarchy、type hierarchy 等的上位词。 |
| **type hierarchy** | 类型层级 | 建议新增 | 定义层级视图与平台概念使用。 |
| **search everywhere** | 随处搜索 | 建议新增 | UI 设置与快速开始使用。 |
| **find usages** | 查找用法 | 建议新增 | 装订线图标、导航与重命名使用。 |
| **diff** | 差异比较 | 建议新增 | UI 设置、动作与数据模型使用；英文为 diff。 |
| **refactoring** | 重构 | 建议新增 | UI、README 与文档高频使用。 |
| **rename** | 重命名 | 建议新增 | UI、检查与重构高频使用。 |
| **inline (refactoring)** | 内联 | 建议新增 | 内联脚本、内联定义、内联重构共用“内联”。 |
| **code style** | 代码样式 | 建议新增 | UI 设置与格式化使用。 |
| **syntax highlighting** | 语法高亮 | 建议新增 | UI 颜色设置与文档使用。 |
| **semantic highlighting** | 语义高亮 | 建议新增 | UI 颜色设置使用。 |
| **annotator** | 注解器（待确认） | 待定夺 | 插件内部用于简单文本级错误提示；中文本地化未见固定译法，且与 inspection 不同。 |
| **problems tool window** | 问题面板 / 问题视图 | 建议新增 | 快速开始与集成文档使用。 |
| **editor notification** | 编辑器通知 | 建议新增 | 内联脚本声明、规则刷新等场景使用。 |
| **notification** | 通知 | 建议新增 | UI 与文档使用。 |
| **run configuration** | 运行配置 | 建议新增 | IntelliJ 通用术语；插件文档较少直接使用。 |

### 5.2 内部工程与架构术语

| 英文 | 插件中文 | 状态 | 备注 |
| :--- | :--- | :--- | :--- |
| **PSI** | PSI / 程序结构接口 | 建议新增 | 通常保留缩写；文档不强行翻译。 |
| **AST** | 抽象语法树 / 语法树 | 建议新增 | 文档使用“语法树”；LighterAST 使用“轻量语法树”。 |
| **lexer** | 词法分析器 | 建议新增 | 语法生成与语言基础设施使用。 |
| **parser** | 语法分析器 / 解析器 | 待定夺 | 与 resolver 都常被译为“解析器”，需要限定语。 |
| **token** | 词元 | 建议新增 | guidelines 明确使用“词元”。 |
| **light element** | 轻量元素 | 建议新增 | 插件 PSI light element；与普通 PSI 元素不同。 |
| **light AST (LighterAST)** | 轻量语法树 | 建议新增 | guidelines 使用 LighterAST；用于索引性能优化。 |
| **index** | 索引 | 建议新增 | IDE 索引与 CSV/数组索引共用“索引”。 |
| **stub index** | 存根索引 | 建议新增 | guidelines 使用 `StubIndex`。 |
| **file-based index** | 文件索引 | 建议新增 | guidelines 使用 `FileBasedIndex`。 |
| **merged index** | 合并索引 | 建议新增 | 插件特有 `ParadoxMergedIndex`。 |
| **reference** | 引用 | 建议新增 | PSI 引用、规则引用、引用解析共用。 |
| **resolve** | 解析 | 建议新增 | 与 parse 都常被译为“解析”。 |
| **resolver** | 解析器 | 建议新增 | 与 parser 都常被译为“解析器”。 |
| **match** | 匹配 | 建议新增 | 与 resolve 区分。 |
| **matcher** | 匹配器 | 建议新增 | AntMatcher、GlobMatcher、RegexMatcher 等。 |
| **extension point (EP)** | 扩展点 | 建议新增 | guidelines 与架构文档高频使用；保留 EP 缩写。 |
| **service** | 服务 | 建议新增 | guidelines 区分的架构层。 |
| **manager** | 管理器 | 建议新增 | guidelines 区分的架构层。 |
| **util** | 工具类 | 建议新增 | guidelines 使用 `Util`。 |
| **factory** | 工厂 | 建议新增 | guidelines 使用 `Factory`。 |
| **builder** | 构建器 | 建议新增 | guidelines 使用 `Builder`。 |
| **provider** | 提供者 | 建议新增 | EP 实现类命名使用 `Provider`。 |
| **processor** | 处理器 | 建议新增 | EP 实现类命名使用 `Processor`。 |
| **listener** | 监听器 | 建议新增 | EP 实现类命名使用 `Listener`。 |
| **resolver (EP)** | 解析器 | 建议新增 | EP 实现类命名使用 `Resolver`。 |
| **support (EP)** | 支持 | 建议新增 | 如 `ExpressionSupport`、`OverrideStrategyProvider`。 |
| **facade** | 门面 | 建议新增 | `ChronicleFacade`；guidelines 使用“门面”。 |
| **context** | 上下文 | 建议新增 | 规则上下文、作用域上下文、参数上下文、模板上下文共用。 |
| **settings** | 设置 | 建议新增 | IntelliJ 平台服务；与 `options` 区分。 |
| **options** | 选项 | 建议新增 | 不可变数据类；与 `settings` 区分。 |
| **capabilities** | 能力项 | 建议新增 | `ChronicleCapabilities`；用于功能开关。 |
| **coroutine scope** | 协程作用域 | 建议新增 | `ChronicleFacade.getCoroutineScope`；与 Paradox 作用域不同。 |
| **search scope** | 搜索作用域 | 建议新增 | `GlobalSearchScope`；测试代码中的 `unionScope` 是搜索作用域变量，不是并集作用域。 |
| **presentable name** | 展示名字 | 建议新增 | 由 `localizedName` 重命名；通常来自本地化文本。 |
| **presentable text** | 展示文本 | 建议新增 | 由 `localizedText` 重命名；用于快速文档、提示、层级视图等。 |
| **code injection** | 代码注入 | 建议新增 | `icu.windea.pls.inject` 内部 EP 架构；与定义注入、语言注入不同。 |
| **language injection** | 语言注入 | 建议新增 | IntelliJ 平台能力；用于参数值、本地化文本、路径等。 |
| **injected config group** | 注入的规则分组 | 建议新增 | 规则分组来源之一；与语言注入、定义注入不同。 |

### 5.3 IDE 术语使用差异与风险

- **`intention` 的“意向/意图”**：README 使用“意向动作”，`docs/zh/ai.md` 与 `integrations.md` 使用“意图”；这不是严重的功能问题，但会让文档搜索与用户理解出现分叉。
- **`call hierarchy` 的“调用层级/调用层次结构”**：插件统一使用“调用层级”，平台中文资料中也可能见到“调用层次结构”；是否对齐属于风格选择。
- **`inlay hint` 的“内嵌提示”**：插件统一使用“内嵌提示”，英文为 inlay hints；与“提示文本”“内联提示”需要区分。
- **`inspection profile` 的“检查配置文件”**：与 CWT“规则文件”共享“配置文件/规则文件”语义场，但两者完全不同；文档应避免简称“配置文件”时指代不清。
- **`parse` / `resolve` 都译作“解析”**：代码中的 parser、resolver、resolve、parse 在中文里都可能写成“解析器/解析”，这是维护者阅读代码与文档时的高频歧义；建议在术语表中明确英文对应，必要时用“语法分析”指 parse。
- **`scope` 的跨层重载**：Paradox scope、scope context、search scope、coroutine scope、resolution scope 都含“作用域”；建议在术语表中列出限定形式，而不是只保留一条 `scope`。
- **`template` 的跨层重载**：模板表达式、模式表达式中的 Template、子句模板、文件模板/实时模板都含“模板”；建议在术语表中按层级拆分。
- **`index` 的跨层重载**：IDE 索引、CSV 列索引、数组定值索引都含“索引”；通常可由上下文区分，但文档首次出现时应加限定语。

---

## 六、建议新增词条汇总与优先级

### 6.1 优先级 P1（建议优先进入 `terminology.md`）

- `definition injection` 的官方对照：`database entry modes`、`INJECT:`、`REPLACE:`、`injection target`。
- `override config` / `override strategy` / `override priority` / `overriding/overridden` 的边界说明。
- `config context`、`config expression`、`data expression`、`data type`、`template expression`、`cardinality expression`、`location expression`、`schema expression`。
- `complex enum`、`dynamic value`、`dynamic value type`、`Value` / `ValueSet` 的层级关系。
- `conditional block` 的“条件化块/参数化块”待定说明。
- `scripted variable` 的局部/全局区分，以及 `variable`（作用域变量）的对照。
- `define namespace` / `define variable` 与官方 `define category` / `define` 的对照。
- `definition` 与官方 `game object` / CWTools `type` / Tiger `item` 的对照。
- `database object` 与官方 `database entry` 的边界说明。
- `injection` 的多重含义（定义注入、语言注入、代码注入、规则注入、注入的规则分组）。
- `scope` 的多重含义（Paradox 作用域、作用域上下文、并集作用域、搜索作用域、协程作用域、解析作用域）。
- `snippet`、`definition reference snippet`、`localisation reference snippet`、`snippet match`。

### 6.2 优先级 P2（建议随文档迭代补充）

- `config group source`、`config repository`、`standard config`、`extended config`、`internal config`。
- `alias config`、`single alias config`、`macro config`、`link config`、`localisation link config`、`localisation promotion config`、`localisation command config`。
- `row config`、`type config`、`subtype config`、`declaration config`、`modifier category config`、`database object type config`。
- `FIOS`、`LIOS`、`DUPL`、`ORDERED` 与 `overwrite` / `override` 的关系。
- `localisation command`、`localisation promotion`、`localisation link`、`localisation group`、`localisation parameter`。
- `text format`、`text icon`、`rich text`、`grammatical tag`、`context tag`、`concept`。
- `presentable name`、`presentable text`、`light element`、`merged index`、`stub index`、`file-based index`。

### 6.3 优先级 P3（维护者内部参考即可）

- `data expression role`、`data expression metadata`、`option metadata`、`config category`、`config type`。
- `root key`、`type key`、`name field`、`subtype expression`、`member path`、`definition source`。
- `glob`、`ant`、`regex`、`constant`、`wildcard` 等模式相关数据类型。
- `parameter context`、`parameter context reference`、`localisation parameter` 等参数相关细分。
- `annotator`、`intention action`、`quick fix`、`inspection profile` 等平台细项。

---

## 七、来源与证据

### 7.1 本地代码与资源

- `src/main/resources/messages/ChronicleBundle.properties` 与 `ChronicleBundle_zh.properties`：脚本、本地化、CWT、覆盖策略、定义注入、内联脚本、复杂枚举、动态值、代码层级、颜色设置等核心 UI 词条。
- `src/main/resources/messages/ChronicleBaseBundle.properties` 与 `ChronicleBaseBundle_zh.properties`：设置页面、快速文档、代码补全、代码折叠、代码层级、推断、语言注入等词条。
- `src/main/resources/messages/ChronicleInspectionBundle.properties` 与 `ChronicleInspectionBundle_zh.properties`：检查名称、检查描述、修复名称、覆盖关系、定义注入、内联脚本、复杂枚举、动态值等词条。
- `src/main/resources/messages/ChronicleIntentionBundle.properties` 与 `ChronicleIntentionBundle_zh.properties`：意向动作名称。
- `src/main/resources/messages/ChronicleIntegrationsBundle.properties` 与 `ChronicleIntegrationsBundle_zh.properties`：Tiger 严重度、置信度与高亮级别映射。
- `src/main/kotlin/icu/windea/pls/config/CwtDataTypes.kt`：核心数据类型及其 KDoc；与术语直接相关的类型包括 `Any`、`Literal`、`Bool`、`Int`、`Float`、`Scalar`、`Definition`、`Localisation`、`SyncedLocalisation`、`InlineLocalisation`、`Modifier`、`EnumValue`、`Value`、`ValueSet`、`DynamicValue`、`ScopeField`、`Scope`、`ScopeGroup`、`ValueField`、`VariableField`、`Command`、`ScriptValueReference`、`DefineReference`、`ArrayDefineReference`、`Tags`、`DatabaseObject`、`NameFormat`、`Parameter`、`ParameterValue`、`LocalisationParameter`、`Template`、`DefinitionSnippet`、`LocalisationSnippet`、`Icon`、`FilePath`、`FileName`、`AbsoluteFilePath`、`ShaderEffect`、`MeshLocator`、`UnionValue`、`AliasKeysField`、`AliasName`、`AliasMatchLeft`、`SingleAliasRight`、`Constant`、`Glob`、`Ant`、`Regex`。
- `src/main/kotlin/icu/windea/pls/config/config/CwtConfig.kt`：规则统一抽象。
- `src/main/kotlin/icu/windea/pls/config/configGroup/CwtConfigGroup.kt`：规则分组。
- `src/main/kotlin/icu/windea/pls/config/config/delegated/`：类型、子类型、行、定值、枚举、复杂枚举、并集、动态值类型、别名、单例别名、宏、链接、本地化链接、本地化提升、本地化命令、修正、修正分类、作用域、作用域分组、数据库对象类型、系统作用域、语言环境等规则 KDoc。
- `src/main/kotlin/icu/windea/pls/config/config/detached/CwtOverrideConfig.kt`：覆盖规则。
- `src/main/kotlin/icu/windea/pls/model/overrides/ParadoxOverrideStrategy.kt`：覆盖策略。
- `src/main/kotlin/icu/windea/pls/model/ParadoxDefinitionInfo.kt`、`ParadoxDefinitionInjectionInfo.kt`、`ParadoxComplexEnumValueInfo.kt`：定义、定义注入、复杂枚举值的解析信息。
- `src/main/kotlin/icu/windea/pls/model/ParadoxDefinitionSource.kt`、`ParadoxDefinitionCandidateInfo.kt`：定义来源与定义候选。
- `src/main/kotlin/icu/windea/pls/model/ParadoxRootInfo.kt`、`ParadoxFileInfo.kt`、`ParadoxModSource.kt`、`ParadoxFileGroup.kt`：游戏/模组、文件、模组来源、文件分组。
- `src/main/kotlin/icu/windea/pls/model/paths/ParadoxMemberPath.kt`：成员路径。
- `src/main/kotlin/icu/windea/pls/model/scope/`：作用域、作用域上下文、作用域解析。
- `src/main/kotlin/icu/windea/pls/model/injection/`：语言注入相关模型；与 `ParadoxDefinitionInjectionInfo` 的定义注入不同。
- `src/main/kotlin/icu/windea/pls/script/psi/ParadoxScriptMacro.kt`、`ParadoxScriptConditionalBlock.kt`、`ParadoxScriptInterpolation.kt`、`ParadoxScriptParameter.kt`、`ParadoxScriptStatement.kt`：语法宏、条件化块、插值、参数、语句。
- `src/main/kotlin/icu/windea/pls/config/configExpression/CwtConfigExpression.kt`、`CwtDataExpression.kt`、`CwtTemplateExpression.kt`、`CwtCardinalityExpression.kt`、`CwtLocationExpression.kt`、`CwtSchemaExpression.kt`：规则表达式体系。
- `src/main/kotlin/icu/windea/pls/config/CwtConfigTypeCategories.kt`、`CwtConfigTypes.kt`：规则类型与分类。
- `src/main/kotlin/icu/windea/pls/model/constants/ChronicleConstants.kt`：本地化目录同时兼容 `localisation` 与 `localization`。
- `src/main/kotlin/icu/windea/pls/inject/`：内部代码注入子系统。

### 7.2 维护者文档与用户文档

- `documents/design/terminology.md`：现行 21 条术语表。
- `documents/notes/guidelines.md`：语言构造、翻译、call/invoke、evaluate/calculate、索引、命名等约定。
- `documents/notes/ideas.md`：定义命名歧义、术语参考文档、片段匹配、覆盖规则重命名、展示名字/展示文本重命名等条目。
- `documents/notes/analysis/cwt-override-rules.md` 与 `cwt-override-rules-updates.md`：覆盖规则来源、策略、重命名历史。
- `documents/notes/adr/union-scopes.md`：并集作用域。
- `documents/notes/adr/union-configs.md`：并集规则。
- `documents/notes/adr/snippet-match.md`：片段匹配。
- `documents/design/language-abstractions.md`：脚本语言抽象层级与“条件化块”术语。
- `docs/zh/ref-syntax.md` 与 `docs/en/ref-syntax.md`：CWT、Paradox Script、Paradox Localisation、Paradox CSV 的基本元素与值类型。
- `docs/zh/ref-config-format.md` 与 `docs/en/ref-config-format.md`：规则分类、规则表达式、数据类型、覆盖规则、定义注入、内联脚本、复杂枚举、动态值、片段匹配。
- `docs/zh/config.md` 与 `docs/en/config.md`：规则分组、规则来源、覆盖策略。
- `docs/zh/quick-start.md`、`docs/zh/README.md`、`docs/zh/ai.md`、`docs/zh/integrations.md`：用户可见语言功能、检查、意图、快速文档、内嵌提示、Tiger 集成。
- `cwt/core/internal/schema.cwt`：规则类型的英文描述与概念命名。
- `cwt/core/00_overrides.cwt`：覆盖规则注释与策略说明。
- `cwt/cwtools-*/config/overrides.cwt`：各游戏覆盖策略。

### 7.3 GitHub Issues 与提交记录

- Issue #252：定义注入特性说明；正文明确提到官方开发日志/Wiki 中的“database entry modes（数据库入口模式）”。链接：`https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/252`。
- Issue #137：VIC3/CK3 本地化命令、本地化格式、文本图标、概念等术语与需求。链接：`https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137`。
- Issue #142：VIC3 变量既可能是数值又可能是作用域，体现 `variable` / `value` / `scope` 的语义重载。链接：`https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/142`。
- Issue #109：VIC3/CK3 GUI 的 `types`、`templates`、`blockoverride` 等术语；本次未作为核心术语来源展开。链接：`https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/109`。
- Issue #456：实际内容为 Stellaris 4.6 起 `common/name_lists` 的 UTF-8 BOM 编码变更，与术语命名无直接关系；本次仅记录为“已检索但与术语审计无关”。链接：`https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/456`。
- 提交 `44acac3a57`、`15faeb0d44`：将“优先级规则/priorityConfig”重命名为“覆盖规则/overrideConfig”。
- 提交 `5623593720`、`58314cfa95`、`95c14b88d4`：将“参数条件/parameterCondition”重命名为“参数化块/conditionalBlock”。
- 提交 `2251195308`、`f9d065103d`：将“显示名称/localizedName”重命名为“展示名字/presentableName”。
- 提交 `8c28e0da62`：将“显示文本/localizedText”重命名为“展示文本/presentableText”。
- 提交 `e9c4ee7ab5`：将 `dynamicValueSetExpression` 重命名为 `tagsExpression`。
- 提交 `1fe93ac7b4`、`19fea2652b`：将 `scriptValueExpression` 重命名为 `scriptValueReferenceExpression`。
- 提交 `9182b76b8c`：将 `optionData` 重命名为 `optionMetadata`。
- 提交 `a58f898db3`：将 `CwtDataTypes.TemplateExpression` 重命名为 `CwtDataTypes.Template`。
- 提交 `3bc248069a`：统一“单独的值”为“直接的值”。

### 7.4 外部 Wiki 与工具文档

- Stellaris Wiki：`https://stellaris.paradoxwikis.com/Modding`、`https://stellaris.paradoxwikis.com/Scopes`、`https://stellaris.paradoxwikis.com/Defines`、`https://stellaris.paradoxwikis.com/Conditions`、`https://stellaris.paradoxwikis.com/Effects`、`https://stellaris.paradoxwikis.com/Dynamic_modding`、`https://stellaris.paradoxwikis.com/Localisation_modding`、`https://stellaris.paradoxwikis.com/On_actions`。
- CK3 Wiki：`https://ck3.paradoxwikis.com/Modding`。
- VIC3 Wiki：`https://vic3.paradoxwikis.com/Mod_compatibility`。
- EU5 Wiki：`https://eu5.paradoxwikis.com/Mod_compatibility`。
- CWTools Wiki：`https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance`、`https://github.com/cwtools/cwtools/wiki/Overview`。
- CWTools 仓库：`https://github.com/cwtools/cwtools`。
- Tiger README：`https://github.com/amtep/tiger`。
- Tiger 过滤指南：`https://github.com/amtep/tiger/blob/main/filter.md`。
- Tiger 标注指南：`https://github.com/amtep/tiger/blob/main/annotations.md`。
- Tiger Wiki：`https://github.com/amtep/tiger/wiki`。
- CK3 Wiki 社区中文翻译样本：`https://github.com/williamlorder/Translation_CK3/blob/493ed3e1dc4cf42721cd8dc8b59dd9c656f538d9/translated/zh/Modding.md`。
- CK3 Wiki 社区中文 Jargon 样本：`https://github.com/williamlorder/Translation_CK3/blob/493ed3e1dc4cf42721cd8dc8b59dd9c656f538d9/translated/zh/Jargon.md`。

### 7.5 调研限制

- Paradox Wiki 站点带有反爬挑战，普通 HTTP 抓取会返回挑战页；本次通过真实浏览器访问并调用 MediaWiki API 获取正文，抓取结果为调研时点快照，后续页面可能变化。
- CWTools 与 Tiger 文档以英文为主，未发现官方中文术语表；本报告中的中文对应主要来自插件现有译法与社区中文翻译样本。
- 社区中文样本仅覆盖 CK3 Wiki 的一小部分页面，不能代表全部 Paradox 中文社区；本报告仅把它作为“存在不同中文说法”的证据，不把它当作权威标准。
- 本次检索以高频核心类、核心 KDoc、核心 UI 消息和重点文档为主，未对 120k 行生产代码做逐行术语普查；建议后续以本报告为索引做一次自动化词条扫描。
- GitHub Issue 检索以关键词和分词结果为主，可能漏掉未使用“术语/命名/翻译”等词的讨论；本次确认 #252、#137、#142 与术语或游戏差异直接相关，#109 提供 GUI 术语背景，#456 与术语无关。
- 本报告不对任何改名行为作出裁决；所有“待定夺”条目均需要维护者结合用户影响、文档成本、代码迁移成本和跨工具兼容性再评估。
