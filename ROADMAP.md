# Roadmap

<!-- Here we list the prophecies from daily notes, and you are asked to organize them. -->

## Overview

> This document is written in Chinese. For non-Chinese users, please have your translation engine or AI assistant ready.

本文档是基于原始的[开发路线笔记](documents/notes/笔记：开发路线.md)进一步整理后的路线图文档。
原始笔记按日期组织，本文件将其重新归纳为按版本号组织后的结构，并且进一步规范化，补充了额外的细节。

- 截止版本：v2.1.10
- 截止日期：2026.06.15

## v2.1.10 {#v2-1-10}

本版本聚焦于**快速的修复、简单的新功能、相对简单的重构和优化**。涵盖解析器/表达式支持重构、新表达式类型支持、开发体验改进等方面。

### 计划

- [x] P2 补充并完善 `CONTRUBUTING.md` 和 `CONTRIBUTORS.md`
- [x] P2 补充并完善 `ROADMAP.md`
- [x] P2 清理 `CHANGELOG.md`，进一步确定编写规范
- [x] P2 重构 path/expression/complexExpression/config/configExpression 的解析器与解析的相关代码（改为：模型接口上的静态解析方法 + 委托给解析器类的对应方法）
- [x] P2 重构 expressionSupport 以及相关代码（精简与规范化）
- [x] P3 重构代码补全系统的相关代码（改为：传递专门的、只读的上下文对象，存在全局上下文/动态上下文/特定上下文，需要一定的设计工作） - 需要审查
- [x] P3 如果当前光标位于一个复杂表达式中，按照复杂表达式的结构来展开光标（Extend Selection） - 相关EP：`extendWordSelectionHandler`
- [x] P3 提供动作，以进行定义/定义注入之间的差异比较
- [x] P4 借助 AI，提供更多 specialPathProvider 和 specialUrlProvider 的默认 EP 实现
- [x] P2 完善对作用域字段表达式（scopeFieldExpression）和值字段表达式（valueFieldExpression）的支持：兼容其中作为链接数据源传入的动态值表达式（dynamicValueExpression） ([#330](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/330))
  - 示例：`event_target:target@root.owner` - `event_target:target@root` 是一个单独的作用域链接节点，插件目前暂不支持其中的 `@root` 部分。
  - 如果链接数据源传入了动态值，则从数据源节点替换为动态值表达式（dynamicValueExpression）。
  - 插件目前认为适用于传入的任意类型的动态值。
  - 单独的动态值表达式中的 `@` 之后允许一组链接节点，而嵌套时仅允许一个链接节点，再之后的链接节点属于外层的链式表达式。
  - 在游戏引擎底层层面，`target@root.owner` 会被解析为 `target__{id}`，其中 `{id}` 是 `root.owner` 的作用域内部ID。
  - 在游戏引擎底层层面，`event_target:target@root.owner` 会被解析为 `event_target:target__{id}.owner`，其中 `{id}` 是 `root` 的作用域内部ID。
  - [x] 补充实现代码。
  - [x] 补充测试。
  - [x] 创建单独的 Issue。
- [x] P2 [CK3/VIC3/EU5] 完善对定值引用表达式（defineReferenceExpression）和数组定值表达式（arrayDefineReferenceExpression）的支持
  - 示例：`Namespace|Name` - 定值引用表达式。引用字面量或数组（通常是数字、颜色或日期）。
  - 示例：`Namespace|Name|0` - 数组定值引用表达式。引用字面量或数组（通常是数字、颜色或日期）。索引从0开始。
  - 对应的规则表达式（数据表达式）：`$define_reference` 和 `$array_define_reference`
  - 需要补充规范化的链接规则 `define` 和 `array_define`（`type = value`）。
  - 需要补充或重构复杂表达式实现、代码补全、代码检查。
  - 需要考虑补充评估器、~~检查器（基于评估器）~~、内嵌提示（基于评估器）、意向动作（基于评估器）。
  - [x] 补充实现代码。
  - [x] 补充测试。
  - [x] 更新相关规则文件。
  - [x] 补充评估器。
  - [x] 补充基于评估器的那些语言功能（~~代码检查~~、~~代码折叠~~、内嵌提示、意向动作）。
- [x] P2 [CK3/VIC3?/EU5?] 完善对标签集表达式（tagsExpression）的支持 ([#163](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/163))
  - 示例：`tag1,tag2` - 写访问。`tag1` 和 `tag2` 视为标识符（作为动态值节点）。
  - 示例：`tag1,not(tag2)` - 读访问，条件变体。`not` 视为关键字（作为关键字节点）。
  - 对应的规则表达式（数据表达式）：`$tags[{name}]` 和 `$tags_condition[{name}]`（条件变体）。
  - 匹配时兼容空字符串。
  - 忽略表达式中的多余空白。
  - 需要用双引号括起，否则给出警告。
  - 需要进一步确认格式和语义。
  - [x] 补充实现代码。
  - [x] 补充测试。
  - [x] 更新相关规则文件。
- [x] P2/FAST 在推断游戏类型时，同时提供描述信息，并在模组设置对话框中显示
  - 例如说明模组目录直接位于游戏创意工坊目录下，因此被推断为对应的游戏类型
  - 此时不允许在模组设置对话框中更改游戏类型

### 附加

- [x] 重构：重命名EP接口 `CwtDataExpressionResolver` 为 `CwtDataExpressionSupport`
- [x] 重构：将 `icu.windea.pls.ep.match` 中的已有代码全部移到 `icu.windea.pls.ep.match.expression`
- [x] 重构：重构 `ParadoxAnalysisInjector` 中的方法并重命名为 `ParadoxAnalysisInjectionManager`
- [x] 重构：变更 `ParadoxGameType` 中的静态方法，提供 `ParadoxGameTypeConstraint`
- [x] 重构：将 `icu.windea.pls.lang.quickfix` 中的已有代码全部移到 `icu.windea.pls.lang.fixes`
- [x] 重构：将 `参数条件/参数条件块/parameterCondition/parameterConditionBlock` 对应地重命名为 `参数化块/conditionalBlock`
- [x] [Stellaris] 兼容 Stellaris 4.4 开始出现的 `? =`（包含空白的安全赋值运算符） ([#331](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/331))
- [x] P1 [Stellaris] 在文法级别区分 Stellaris 4.4 中的 `? =` (SAFE_CALL_ASSIGN) 与 CK3/VIC3/EU5 中的 `?=` (SAFE_ASSIGN) & 优化格式化逻辑 ([#331](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/331))
- [x] P2 [Stellaris] 完善代码检查 `ParadoxScriptIncorrectSyntax`：验证 `? =` `?=` 是否受游戏类型支持，且左值和右值是否在文法级别合法 ([#331](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/331))
- [x] P3/QoL 提供代码检查 `UnmatchedFileInspection`（对于脚本文件或CSV文件），以报告当前文件无法匹配到任何 `CwtFilePathMatchableConfig`（默认级别：`WARNING`） - 这意味着当前文件是特殊的，或者缺失相应的规则
- [x] P3/QoL 将 `ParadoxInlineScriptEditorNotificationProvider` 改为 `DeclaredInlineScriptInspection`（默认级别：`TEXT ATTRIBUTES`） - 为了对齐已有的 `UnusedInlineScriptInspection`
- [x] P3/QoL 提供意向和代码检查，以在作用域调用语句的链式形式（`root.owner = ...`）与嵌套形式（`root = { owner = ... }`）之间进行转换 - 需要同时检测于文法级别和语义级别
- [x] P3/QoL 提供意向和代码检查，以在作用域调用语句的安全形式（`owner ?= ...` 或 `owner? = ...`）与普通形式（`owner = ...`）之间进行转换 - 需要同时检测于文法级别和语义级别
- [x] P3/QoL 完善作用域调用的普通形式/安全形式之间的转换逻辑 - 存在更多细节，需要修复和改进
- [x] P3 在工具菜单中新增规则系统相关的动作，并且另外提供动作，以强制刷新内置规则目录、强制刷新规则分组数据

## v2.2.0-csv {#v2-2-0}

本版本聚焦于**完善对 CSV 文件与 rowConfig 的支持**，处理边界情况，调整相关代码检查。

### 计划

- [ ] P2/MS 完善对 CSV 文件与 rowConfig 的支持 ([#314](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/314))
  - [ ] 允许在 CSV 文件中声明复杂枚举值（提案：列规则上的 `## declare_complex_enum = x`）。
  - [ ] ~~允许在 CSV 文件中声明定义（提案：列规则上的 `## declare_type = x`）。~~
    - 将一整行视为定义声明，将某个列视为指定了定义名的列。
    - 需要架构上的大幅重构，并且涉及一些领域设计上的问题，因此暂不考虑。
  - [ ] 为 rowConfig 添加字段 type，可选值包括 key/index，默认 key。表示是按键还是按索引来匹配列中的每一行。
    - 表头列的列名（columnKey/columnName）可以重复，因此在解析规则数据和语义解析时，可能需要基于列索引，而非列名。
  - [ ] 为 rowConfig 添加字段 skip_last_row 和 skip_last_column，值为布尔值。表示匹配与解析时是否忽略最后一列/最后一行。
    - 表头列的最后一列，以及文件中的最后一行，可能需要在语义解析时忽略，因此需要提供对应的规则字段。
  - [ ] 考虑将从（脚本/CSV）表达式解析为复杂枚举值的表达式引用（即PSI引用）的逻辑，提取为需要置顶的 expressionSupport 的EP实现。
    - 后续也可以考虑以类似方式处理各种特殊标签。
  - [ ] 调整相关的代码检查的代码。
  - [ ] 调整复杂枚举值索引和合并索引的逻辑，以兼容在CSV文件中的复杂枚举值声明和动态值声明。

## v3.0.0-the-great-renaming 

本版本聚焦于**大重命名**（项目名、类名、特殊字面量等）以及**文档的全面更新**（普通文档、参考文档、规则仓库的普通文档），同时完善社区文档。

### 计划 {#v3-0-0}

- [ ] P2/MS 改进英文 `README.md`，检查并修正用词问题和单复数问题。包括更准确的描述和说明、更符合个人风格和项目叙事风格的文字，适当引入叙事性
- [ ] P2/MS 改进与补充 `README.md`（叙事 / 问题解决 / FAQ / 已知限制 / ……）
- [ ] P2/MS 修订参考文档，尤其是各个参考手册（修正事实性错误，补充必要的细节）
- [ ] P2/MS 新增与补充 `CONTRIBUTING.md`（概述，说明如何贡献代码/文档/规则，包括步骤/建议/复杂性警告）
- [ ] P2/MS 改进与补充 `CONTRIBUTORS.md`（概述，列出主要贡献者：代码/文档/规则/特别感谢/其他）

## v3.1.0-complex-expression {#v3-1-0}

本版本聚焦于**复杂表达式 API 的相关重构**（更准确的解析逻辑、更详细的报错、处理边界情况、补充测试），关键词包括 Token、TokenStream、Scanner、Resolver、Visitor、Validator、Evaluator 等。

### 计划

- [ ] P2/MS 重构复杂表达式的 API，尤其是解析逻辑与校验逻辑。考虑将相关代码移到 `icu.windea.pls.expression`
- [ ] P2/MS 优化对复杂表达式的支持：优化对动态链接节点中传入的字面量的处理逻辑
- [ ] P3/MAYBE 实现 script_value 的评估器，提供对应的求值意向动作。可能需要依赖游戏运行时的上下文（此时显示为动态结果/条件结果）

## v3.2.0-advanced-inline {#v3-2-0}

本版本聚焦于**内联脚本进阶**，包括内联脚本的展开评估器与对应的意向动作，以及内联定义（索引/查询/引用解析/各种语言功能等）。

### 计划

- [ ] P2/MS 支持内联定义（即通过内联脚本声明的定义，其名字可以取决于传入的参数值），包括索引/查询/语义信息/引用解析/快速文档/内嵌提示等
- [ ] P3 实现内联脚本的展开评估器，提供对应的意向动作（单步展开/递归）：
  - 点击打开非阻塞的对话框，可以复制文本，可以打开为临时文件
  - 保留处理后仍然带参数的用法
  - 忽略存在递归的用法，并且给出警告（注释）
- [ ] P3 实现 scripted_trigger/scripted_effect 的展开评估器，提供对应的意向动作（单步展开/递归）。详细说明同上
- [ ] P3 实现定义注入的展开评估器，提供对应的意向动作（合并当前/合并所有）。点击打开非阻塞的对话框，可以复制文本，可以打开为临时文件
- [ ] P3/DELAY 提供展开 scripted trigger/scripted effect/inline script（乃至定义注入）的两种意向动作（递归+单步）：
  - 参考 Rust 插件的宏展开相关功能：弹出包含编辑器的 popup（无顶栏，非 dialog）
  - 暂不考虑提供装订线图标（Rust 插件在光标位于当行时提供）
  - 暂不考虑在预览中适用语义高亮（Rust 插件当前未适用）

## 长期 {#long-term}

此章节包含所有**作为长期任务或未来愿景**的待办事项。

### 文档与参考

- [ ] P2/MS 新增与补充关于复杂表达式的参考文档
- [ ] P2/MS 新增与补充关于常见术语（IDE/领域）的参考文档
- [ ] P4/MS/CONSIDER 新增参考文档：功能速查表（可从 XML 配置文件提取，可借助 AI）
- [ ] P3 清理、补充与完善 complexExpression 的文档注释

### 进阶与愿景

- [ ] P3/CONSIDER 重构或重新编写代码，以更好地生成与合并规则，便于维护
  - 如果要考虑严谨性、可扩展性和可维护性的话，事情会变得非常复杂。
  - 不能简单地只设计一个 generator 组件，考虑分成 dataExtractor/dataMerger/generator 等组件，并且拥有额外的工具类和上层组件。
  - 后续改进时，可以考虑引入AI。
- [ ] DELAY 让 AI 有能力自行维护规则文件，并且尽可能自动化
- [ ] DELAY 需要使用 WebView 渲染的高级可视化功能

## 未整理 {#unsorted}

此章节包含所有**暂未纳入到上述任何版本计划中**的待办事项。

### 语言功能支持

- [ ] P4/CONSIDER 适用于 CWT 语言、脚本语言的结构化搜索
- [ ] P4 通过新引入的 EP，提供更多标准/特殊/定制化的层级视图：
  - EP 接口：`icu.windea.pls.ep.hierarchy.ParadoxDefinitionCallHierarchyViewProvider`
  - 两种主要类别：过滤型（仅显示定义节点，过滤要显示的定义类型）与关系型（通常仅显示定义节点，可以更改子节点的获取方式）

### 语言构造支持

- [ ] P2/MS 支持匹配带表达式前缀的脚本表达式（"表达式前缀"指目标脚本表达式之前紧邻的作为前缀的字符串，例如 `k = list v` 中的 `list`，通过规则选项 `## key_prefix` / `## value_prefix` 指定要匹配的通配符模式）

### 图片支持

- [ ] P2 图片支持：考虑内置解析代码，移除对外部库的依赖，移除相关的代码注入器
- [ ] P4 图片支持：提供更加规范化的API，用于实现切分图片、按层级堆叠图片等功能

### 规则系统

- [ ] P3 开放内部规则：允许自定义除了 schema 之外的所有现有内部规则类型，考虑新增规则类型 liveTemplateSettings
  - 这些特殊规则的变化基本上不会导致刷新与重新解析，以后可以考虑优化相关代码。
  - 考虑借助 AI 补充更多默认提供的设置规则。

### 工具与集成

- [ ] P4 开发用功能（可以考虑提供内部动作）：从本地已安装的所有游戏目录，检测所有图片文件，包括详细列表、数量统计、以及用到的 DDS 格式的统计
- [ ] P4 开发用功能（可以考虑提供内部动作）：从本地已安装的所有游戏目录，检测所有脚本文件和本地化文件，包括详细列表、数量统计、行数统计（行数、非空行、非空行非注释行）
- [ ] P3 开发用功能（可以考虑提取内部动作）：可配置式地直接调用渲染器，检查渲染后的结果
- [ ] P4 脚本文件、本地化文件、CSV 文件的序列化器与反序列化器：从 PSI 转化为一种更加 JSON 友好的格式，并且支持多种配置项，以兼容不同的格式偏好
- [ ] P4/MAYBE 提供实用工具动作的 EP 接口，以及一组预定义的实用工具动作：
  - EP 接口：`icu.windea.pls.ep.tool.ExtraUtilityProvider`
  - 设置类：`icu.windea.pls.lang.tool.ExtraUtilitySettings`
  - 服务类：`icu.windea.pls.lang.tool.ExtraUtilityService`
  - 预设：打包模组（可配置）/ 审计游戏和模组文件（按查询作用域）/ 调用渲染器（不确定）
- [ ] P3 关于 tiger 集成：经过检查，tiger 输出的 JSON 中未提供快速修复信息，且 tiger 的耦合程度较高，难以通过外部手段补充
- [ ] P4 重构和扩展基于Diagrams插件/调用层级的图表功能
  - 优化相关的设置页面，提供更好的、更易扩展的配置方式。
  - 考虑在已有EP的基础上再次提取插件特定的EP。
  - 借助AI，收集想法，考虑提供更多种类的图表。
- [ ] P4/DELAY 除了科技树与事件树以外的更多图表类型
  - 主要面向 Stellaris 以外的游戏，可以考虑AI协助。

### 其他

- [ ] P4/MAYBE 提供特殊的工具动作，以对当前文件设置或清空注入的文件信息
- [ ] P3 重构/重新设计插件设置的数据类和页面
- [ ] P4 评估是否可以通过 flag-mask 模式进一步压缩缓存数据的内存占用
- [ ] P4 为了支持更准确的作用域推断，可能需要在构建合并索引时收集更多数据