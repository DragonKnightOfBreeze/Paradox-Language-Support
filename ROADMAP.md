# Roadmap

<!-- Here we list the prophecies from daily notes, and you are asked to organize them. -->

## Overview

> This document is written in Chinese. For non-Chinese readers, please prepare a translation engine and an LLM for assistance.

本文档是基于项目原始开发笔记（`documents/notes/笔记：开发路线.md`）进一步整理后的路线图文档。原始笔记按日期组织，本文件将其重新归纳为按版本号划分的结构。

相对于原始笔记，本文件：

- **仅保留未完成**的待办事项（已完成项不再出现）
- **保留优先级标记**（如 `P2`）和**状态标记**（如 `MS`、`FAST`、`DELAY`、`MAYBE`），但不保留版本标记
- 将带有明确版本号标记的待办事项归入对应版本章节，其余未标记版本号的待办事项统一置于末尾的 `Unsorted` 章节
- 对非正式的吐槽性文字做了删除或改写

## v2.1.10

本版本聚焦于**快速的修复、简单的新功能、相对简单的重构和优化**。涵盖解析器/表达式支持重构、新表达式类型支持、开发体验改进等方面。

### 计划

- [x] **P2** 重构 path/expression/complexExpression/config/configExpression 的解析器与解析的相关代码（改为：模型接口上的静态解析方法 + 委托给解析器类的对应方法）
- [x] **P2** 重构 expressionSupport 以及相关代码（精简与规范化）
- [ ] **P3** 重构代码补全系统的相关代码（改为：传递专门的、只读的上下文对象，存在全局上下文/动态上下文/特定上下文，需要一定的设计工作）
- [ ] **P3** 提供动作，以进行定义/定义注入之间的差异比较
- [x] **P4** 借助 AI，提供更多 specialPathProvider 和 specialUrlProvider 的默认 EP 实现
- [ ] **P2/FAST** 支持形如 `event_target:target@root` 的动态链接节点
  - 可在作用域字段表达式（scopeFieldExpression）和值字段表达式（valueFieldExpression）中使用，适用于传入的任意类型的动态值
- [ ] **P2/FAST** 支持数组定值引用表达式（`arrayDefineReferenceExpression`）
  - 格式：`array_define:Namespace|Name|Index`，索引从0开始
  - 区别于已支持的 defineReferenceExpression
  - 用于：CK3/VIC3/EU5
- [ ] **P2/FAST/MAYBE** 支持动态值集合表达式（`dynamicValueSetExpression`）
  - 读：`set_flags = "flag1,flag2"`
  - 写：`required_flags = "flag1,not(flag2)"`
  - `flag1` 和 `flag2` 视为标识符（动态值），`not` 视为关键字，允许多余的空白
  - 要求必须用双引号括起，否则给出警告
- [x] **P2/FAST** 在推断游戏类型时，同时提供描述信息，并在模组设置对话框中显示
  - 例如说明模组目录直接位于游戏创意工坊目录下，因此被推断为对应的游戏类型
  - 此时不允许在模组设置对话框中更改游戏类型
- [ ] **P2/FAST** ~~在打开项目时，递归扫描项目中的目录，检测未配置游戏目录的模组目录，并逐个发送警告级别的通知~~
  - 跳过以 `.` 开始的目录、游戏目录和模组目录
  - 忽略：存在其他类似的编辑器通知

## 附加

- [x] 重命名EP接口 `CwtDataExpressionResolver` 为 `CwtDataExpressionSupport`
- [x] 将 `icu.windea.pls.ep.match` 中的已有代码全部移到 `icu.windea.pls.ep.match.expression`

## v2.2.0-csv

本版本聚焦于**完善对 CSV 文件与 rowConfig 的支持**，处理边界情况，调整相关代码检查。核心目标参见 [#314](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/314)。

### 计划

- [ ] **P2/MS** 完善对 CSV 文件与 rowConfig 的支持：
  - [ ] 允许在 CSV 文件中声明复杂枚举值（提案：列规则上的 `## declare_complex_enum = x`）
  - [ ] 为 rowConfig 添加字段 `type`，可选值包括 `key`/`index`，默认 `key`。表示是按键还是按索引来匹配列中的每一行。表头列的列名可以重复，因此在解析规则数据和语义解析时，可能需要基于列索引而非列名
  - [ ] 为 rowConfig 添加字段 `skip_last_row` 和 `skip_last_column`，值为布尔值。匹配与解析时是否忽略最后一列/最后一行
  - [ ] 调整相关的代码检查的代码
  - [ ] 调整复杂枚举值索引和合并索引的逻辑，以兼容在 CSV 文件中的复杂枚举值声明和动态值声明
  - [ ] 考虑将从（脚本/CSV）表达式解析为复杂枚举值的表达式引用逻辑，提取为需要置顶的 expressionSupport 的 EP 实现（后续也可以考虑以类似方式处理各种特殊标签）

## v3.0.0-the-great-renaming

本版本聚焦于**大重命名**（项目名、类名、特殊字面量等）以及**文档的全面更新**（普通文档、参考文档、规则仓库的普通文档），同时完善社区文档。

### 计划

- [ ] **P2/MS** 改进英文 `README.md`，检查并修正用词问题和单复数问题。包括更准确的描述和说明、更符合个人风格和项目叙事风格的文字，适当引入叙事性
- [ ] **P2/MS** 改进与补充 `README.md`（叙事 / 问题解决 / FAQ / 已知限制 / ……）
- [ ] **P2/MS** 修订参考文档，尤其是各个参考手册（修正事实性错误，补充必要的细节）
- [ ] **P2/MS** 新增与补充 `CONTRIBUTING.md`（概述，说明如何贡献代码/文档/规则，包括步骤/建议/复杂性警告）
- [ ] **P2/MS** 改进与补充 `CONTRIBUTORS.md`（概述，列出主要贡献者：代码/文档/规则/特别感谢/其他）

## v3.1.0-complex-expression

本版本聚焦于**复杂表达式 API 的相关重构**（更准确的解析逻辑、更详细的报错、处理边界情况、补充测试），关键词包括 Token、TokenStream、Scanner、Resolver、Visitor、Validator、Evaluator 等。

### 计划

- [ ] **P2/MS** 重构复杂表达式的 API，尤其是解析逻辑与校验逻辑。考虑将相关代码移到 `icu.windea.pls.expression`
- [ ] **P2/MS** 优化对复杂表达式的支持：优化对动态链接节点中传入的字面量的处理逻辑
- [ ] **P3/MAYBE** 实现 script_value 的评估器，提供对应的求值意向动作。可能需要依赖游戏运行时的上下文（此时显示为动态结果/条件结果）

## v3.2.0-advanced-inline

本版本聚焦于**内联脚本进阶**，包括内联脚本的展开评估器与对应的意向动作，以及内联定义（索引/查询/引用解析/各种语言功能等）。

### 计划

- [ ] **P2/MS** 支持内联定义（即通过内联脚本声明的定义，其名字可以取决于传入的参数值），包括索引/查询/语义信息/引用解析/快速文档/内嵌提示等
- [ ] **P3** 实现内联脚本的展开评估器，提供对应的意向动作（单步展开/递归）：
  - 点击打开非阻塞的对话框，可以复制文本，可以打开为临时文件
  - 保留处理后仍然带参数的用法
  - 忽略存在递归的用法，并且给出警告（注释）
- [ ] **P3** 实现 scripted_trigger/scripted_effect 的展开评估器，提供对应的意向动作（单步展开/递归）。详细说明同上
- [ ] **P3** 实现定义注入的展开评估器，提供对应的意向动作（合并当前/合并所有）。点击打开非阻塞的对话框，可以复制文本，可以打开为临时文件
- [ ] **P3/DELAY** 提供展开 scripted trigger/scripted effect/inline script（乃至定义注入）的两种意向动作（递归+单步）：
  - 参考 Rust 插件的宏展开相关功能：弹出包含编辑器的 popup（无顶栏，非 dialog）
  - 暂不考虑提供装订线图标（Rust 插件在光标位于当行时提供）
  - 暂不考虑在预览中适用语义高亮（Rust 插件当前未适用）

## Unsorted

此章节包含所有**未标记具体版本号**的待办事项，暂未归类到上述任何版本中。

### 图片支持

- [ ] **P2** 图片支持：考虑内置解析代码，移除对外部库的依赖
- [ ] **P4** 图片支持：提供更加规范化的 API，用于切分图片、按层级堆叠图片

### 开发用功能

- [ ] **P4** 开发用功能（可以考虑提供内部动作）：从本地已安装的所有游戏目录，检测所有图片文件，包括详细列表、数量统计、以及用到的 DDS 格式的统计
- [ ] **P4** 开发用功能（可以考虑提供内部动作）：从本地已安装的所有游戏目录，检测所有脚本文件和本地化文件，包括详细列表、数量统计、行数统计（行数、非空行、非空行非注释行）
- [ ] **P3** 开发用功能（可以考虑提取内部动作）：可配置式地直接调用渲染器，检查渲染后的结果

### 规则系统

- [ ] **P3** 开放内部规则：除了 schema 之外都允许自定义，加载规则数据时不允许覆盖 schema。另外还可以考虑新增 liveTemplateSettings。这些特殊规则的变化基本上不会导致刷新与重新解析，以后可以考虑优化相关代码。还可以借助 AI 补充更多默认提供的设置规则
- [ ] **P3/CONSIDER** 重构或重新编写代码，以更好地生成与合并规则，便于维护。如果要考虑严谨性、可扩展性和可维护性的话，事情会变得非常复杂。不能简单地只设计一个 generator 组件，考虑分成 dataExtractor/dataMerger/generator 等组件，并且拥有额外的工具类和上层组件。后续改进时，可以考虑引入 AI

### 性能与内存优化

- [ ] **P4** 评估是否可以通过 flag-mask 模式进一步压缩规则对象的内存占用
- [ ] **P4** 为了支持更准确的作用域推断，可能需要在构建合并索引时收集更多数据
- [ ] **P3** 再次验证索引游戏目录时的性能，检查是否存在性能回退

### 表达式相关

- [ ] **P2/MS** 支持匹配带表达式前缀的脚本表达式（"表达式前缀"指目标脚本表达式之前紧邻的作为前缀的字符串，例如 `k = list v` 中的 `list`，通过规则选项 `## key_prefix` / `## value_prefix` 指定要匹配的通配符模式）

### 代码补全与搜索

- [ ] **P4** 对代码补全系统的重构：改为传递专门的、只读的上下文对象，而非直接传递 processingContext；按需复制上下文对象，以切换上下文
- [ ] **P4** 适用于 CWT 语言、脚本语言的结构化搜索（看起来 JetBrains 官方都没有提供 JSON 和 YAML 的结构化搜索，真的需要提供这个吗？）

### 工具与扩展

- [ ] **P4/MAYBE** 提供实用工具动作的 EP 接口，以及一组预定义的实用工具动作：
  - EP 接口：`icu.windea.pls.ep.tool.ExtraUtilityProvider`
  - 设置类：`icu.windea.pls.lang.tool.ExtraUtilitySettings`
  - 服务类：`icu.windea.pls.lang.tool.ExtraUtilityService`
  - 预设：打包模组（可配置）/ 审计游戏和模组文件（按查询作用域）/ 调用渲染器（不确定）
- [ ] **P4** 通过新引入的 EP，提供更多标准/特殊/定制化的层级视图：
  - EP 接口：`icu.windea.pls.ep.hierarchy.ParadoxDefinitionCallHierarchyViewProvider`
  - 两种主要类别：过滤型（仅显示定义节点，过滤要显示的定义类型）与关系型（通常仅显示定义节点，可以更改子节点的获取方式）
- [ ] **P4/MAYBE** 提供特殊的工具动作，以对当前文件设置或清空注入的文件信息

### Tiger 集成

- [ ] **P3** 关于 tiger 集成：经过检查，tiger 输出的 JSON 中未提供快速修复信息，且 tiger 的耦合程度较高，难以通过外部手段补充

### 序列化

- [ ] **P4** 脚本文件、本地化文件、CSV 文件的序列化器与反序列化器：从 PSI 转化为一种更加 JSON 友好的格式，并且支持多种配置项，以兼容不同的格式偏好

### 图表功能

- [ ] **P4** 重构和扩展基于 Diagrams 插件/调用层级的图表功能：优化相关的设置页面，提供更好的、更易扩展的配置方式；考虑在已有 EP 的基础上再次提取插件特定的 EP；借助 AI，收集想法，考虑提供更多种类的图表

### 文档注释

- [ ] **P3** 清理、补充与完善 complexExpression 的文档注释

### 插件设置

- [ ] **P3** 重构/重新设计插件设置的数据类和页面

### 文档与参考

- [ ] **P2/MS** 新增与补充关于复杂表达式的参考文档
- [ ] **P2/MS** 新增与补充关于常见术语（IDE/领域）的参考文档
- [ ] **P4/MS/CONSIDER** 新增参考文档：功能速查表（可从 XML 配置文件提取，可借助 AI）

### DELAY（延期事项）

以下事项被明确标记为 DELAY，暂不安排在当前路线图中，留待后续评估。

- [ ] **DELAY** 让 AI 有能力自行维护规则文件，并且尽可能自动化
- [ ] **DELAY** 需要使用 WebView 渲染的高级可视化功能
- [ ] **DELAY** 除了科技树与事件树以外的图表（Diagram）——需要 AI 协助，主要面向 Stellaris 以外的游戏
- [ ] **DELAY** 评估方案 C（在脚本成员级别检查继承属性）是否有必要应用以及效果