# 报告_#2_项目探索_task-3

> 会话日期：2025-08-27 15:15（本地）  
> 会话主题：按文档与配置探索 PLS 项目（task-3）

---

## 概述

- 目标：
  - 按顺序阅读并分析项目文档与插件配置：
    1) `README.md`
    2) `src/main/resources/META-INF/plugin.xml`
    3) `plugin.xml` 中 include 的其他配置文件
  - 每阅读一个文件，生成一份独立的“探索笔记”至 `notes/ai-vision/task-3/`，命名为 `探索_{no}_<desc>.md`。
  - 产出的探索笔记定位于项目作者与 AI（后续会话可直接阅读以快速建立上下文）。
- 约定遵循：见 `notes/ai-reports/ai-task-reporting.md`（会话开始创建新报告、阶段性更新）。

---

## 阶段更新：阅读 README.md（探索_1）

- 笔记文件：`notes/ai-vision/task-3/探索_1_README.md`
- 关键发现（摘要）：
  - PLS 是 IntelliJ 平台插件，面向 Paradox 系游戏模组开发（重点 Stellaris），以 CWT 规则驱动语言能力。
  - 支持脚本/本地化/CSV/CWT，多项 IDE 语言能力（高亮、导航、补全、检查、重构、快速文档、Inlay、层级、图表、diff 等）。
  - 资源侧支持 DDS/TGA 预览、PNG/DDS/TGA 转换；生态集成 ImageMagick、Translation Plugin、Tiger。
  - 工作流：打开模组根目录 → 描述符 → 模组配置 → 索引 → 开发；提供 Project 视图聚合与全局检查入口。
  - 已知边界：Stellaris 特性仍在完善；其他游戏支持不完全。
- 下一步建议：进入 `META-INF/plugin.xml` 解析插件结构（actions/services/extensions/language/fileType/inspections 等）与 include 链路。

---

## 阶段更新：阅读 plugin.xml（探索_2）

- 笔记文件：`notes/ai-vision/task-3/探索_2_plugin.xml.md`
- 关键发现（摘要）：
  - 必需依赖：`com.intellij.modules.lang`、`com.intellij.platform.images`；可选依赖配合 `config-file`：Markdown/Diagram/Translation。
  - 主配置聚合：通过 `xi:include` 包含 `pls-*.xml` 共 13 个主题配置（如 `pls-lang.xml`、`pls-ep.xml`、`pls-inspections.xml` 等）。
  - 资源束：`messages.PlsBundle`。未在此文件看到 `<id>/<name>/<version>`，可能来自构建或其他配置。

---

## 阶段更新：阅读 pls-cwt.xml（探索_3）

- 笔记文件：`notes/ai-vision/task-3/探索_3_pls-cwt.xml.md`
- 关键发现（摘要）：
  - 定义 CWT 语言与 `.cwt` 文件类型；提供 `CwtParserDefinition`。
  - 编辑/导航矩阵：高亮、拼写、括号/引号、查找用法、扩展选区、注释、面包屑、折叠、实现选区、移动、智能回车、导航栏、结构视图、环绕/解包。
  - 代码样式与格式化：FormattingModel、Code Style Settings、Language Code Style。
  - Live Templates 上下文：`CWT`、`CWT_MEMBERS`。
  - PSI 元素操控器：`CwtOptionKey/PropertyKey/Value/String` 的 Manipulator。

---

## 阶段更新：阅读 pls-script.xml（探索_4）

- 笔记文件：`notes/ai-vision/task-3/探索_4_pls-script.xml.md`
- 关键发现（摘要）：
  - 定义 `PARADOX_SCRIPT` 语言，`descriptor.mod` 文件名绑定，解析器 `ParadoxScriptParserDefinition`。
  - 编辑/导航：高亮、拼写、括号/引号、折叠、注释、用法/描述、选区、注解、实现选区、错误快速修复、智能回车、语句移动、导航栏、结构视图、文件结构分组、环绕/解包。
  - 代码样式与格式化：FormattingModel、Code Style、Language Code Style。
  - Live Templates：基础/成员/内联数学表达式上下文。
  - PSI 操作：PropertyKey/Value/String/Parameter/InlineMathParameter 的 Manipulator。
  - 配色方案：多主题 `additionalTextAttributes` 文件。

---

## 阶段更新：阅读 pls-localisation.xml（探索_5）

- 笔记文件：`notes/ai-vision/task-3/探索_5_pls-localisation.xml.md`
- 关键发现（摘要）：
  - 定义 `PARADOX_LOCALISATION` 语言与解析器，独立 PSI 支撑。
  - 编辑/导航：高亮、拼写、括号/引号、注释、面包屑、查找用法/元素描述、扩展选区、注解、智能回车、折叠、声明范围、实现选区、错误快速修复、语句移动、解包；导航栏、结构视图、文件结构分组。
  - 模板：基础与“本地化文本”上下文，且提供默认模板集 `/liveTemplates/ParadoxLocalisation.xml`。
  - 代码样式与格式化：FormattingModel、Code Style、Language Code Style。
  - UI 增强：浮动工具栏与文本编辑器定制；样式动作 CreateReference/Icon/Command（默认快捷键 Ctrl+Alt+R/I/C），并集成到 Generate 菜单。
  - 配色方案：多主题 `additionalTextAttributes` 文件。

---

## 阶段更新：阅读 pls-csv.xml（探索_6）

- 笔记文件：`notes/ai-vision/task-3/探索_6_pls-csv.xml.md`
- 关键发现（摘要）：
  - 定义 `PARADOX_CSV` 语言与文件类型，解析器 `ParadoxCsvParserDefinition`。
  - 编辑/导航：高亮、拼写、注释、引号、面包屑、查找用法/元素描述、扩展选区、注解、引用高亮、折叠、声明范围；导航栏与结构视图。
  - 代码样式：`CodeStyleSettingsProvider` 与 `LanguageCodeStyleSettingsProvider`（未见显式 `formatter`）。
  - PSI 操作：`ParadoxCsvColumn` 的 Manipulator（列级元素可编辑/重构）。

---

## 阶段更新：阅读 pls-config.xml（探索_7）

- 笔记文件：`notes/ai-vision/task-3/探索_7_pls-config.xml.md`
- 关键发现（摘要）：
  - 设置页：注册 `applicationConfigurable`（`id=pls.config`，父分组 `pls`），用于“配置/配置组”的首选项页面。
  - 编辑器/索引集成：`editorNotificationProvider`、`editorFloatingToolbarProvider`、`additionalLibraryRootsProvider`、`psi.treeChangePreprocessor`、`vfs.asyncListener` 组成“变更监听 → 刷新索引 → 编辑器提示”的闭环。
  - 动作与快捷键：
    - `Pls.SyncConfigGroupFromRemote`（Alt+T）用于远端同步配置组，加入 `EditorContextBarMenu`；
    - `Pls.ConfigGroupRefreshActionGroup` 内含刷新（Ctrl+Shift+G）与隐藏入口。
  - 推断：规则解析引擎入口位于服务层（未在 XML 声明），上述 Provider/Listener/Action 与该服务交互以驱动“拉取/解析/索引/刷新”。

---

## 阶段更新：阅读 pls-lang.xml（探索_8）

- 笔记文件：`notes/ai-vision/task-3/探索_8_pls-lang.xml.md`
- 关键发现（摘要）：
  - 集中注册跨语言能力：索引/搜索（StubIndex/FileBasedIndex、QueryExecutor）、Inlay、行标记、参数信息、后缀模板、文档与链接处理。
  - 重构支持完整：rename validators、自动重命名、inline/extract handlers、refactoring support provider。
  - 导航矩阵：Go To Files/Definitions/Localisations/Related*，以及层级（Definition/Call）。
  - 动作体系聚合到 `Pls.Tools` 与上下文菜单；提供 Steam/路径/URL/复制等集成。
  - 运行时闭环：多类 Listener + LibraryProvider + FileListener + PsiTreeChangePreprocessor 触发刷新与通知。
  - Registry Keys 提供字体/图标/文本长度/深度/数量等运行时调参。

---

## 阶段更新：阅读 pls-ep.xml（探索_9）

- 笔记文件：`notes/ai-vision/task-3/探索_9_pls-ep.xml.md`
- 关键发现（摘要）：
  - 定义 PLS 扩展点：Color/QuickDoc/HintText/ReferenceLink、CWT 注入/覆盖/关联、Config Context/Declaration Context、Config Group Data/File、DataExpression Resolver/Merger/Priority、Expression Matchers/Supports（Script/Localisation/CSV/PathRef）、Icon/Index、检查与抑制、元数据与推断、修饰符/参数/作用域、呈现/继承/内联/优先级、Mod Import/Export；均 `dynamic="true"`。
  - 默认实现：覆盖各 EP 的基础/核心/模板/正则/Ant/常量等策略，并通过 `order`（first/last/after）控制链路顺序；含 Stellaris 专项实现。
  - 定位：作为“语义/规则层”的抽象扩展面，为 `pls-lang.xml` 中的平台扩展提供语义数据源与行为策略。

---

## 阶段更新：阅读 pls-intentions.xml（探索_10）

- 笔记文件：`notes/ai-vision/task-3/探索_10_pls-intentions.xml.md`
- 关键发现（摘要）：
  - 按语言注册 Intention：
    - CWT：标识符加/去引号（含 `descriptionDirectoryName`）。
    - Script：加/去引号、复制脚本变量/定义/本地化名称与文本（含 Plain/Html）、条件片段格式转换（Property/Block）。
    - Localisation：切换语言/颜色、复制/替换本地化（含 from locale、with translation 变体）。
    - CSV：标识符加/去引号（含 `descriptionDirectoryName`）。
  - 分类键：`intention.cwt.category`、`intention.script.category`、`intention.localisation.category`、`intention.csv.category`。
  - 定位：面向编辑器的便捷操作，依赖语义/索引层结果以完成复制/转换等动作。

---

## 阶段更新：阅读 pls-inspections.xml（探索_11）

- 笔记文件：`notes/ai-vision/task-3/探索_11_pls-inspections.xml.md`
- 关键发现（摘要）：
  - 注册 `lang.inspectionSuppressor`：Script/Localisation/CSV。
  - 分组：Script（Common/Bug/Scope/Expression/Event/Inference）、Localisation（Common/Bug/Scope/Expression）、CSV（Common）、Overridden、Lints。
  - 级别与默认：解析/表达式类多为 ERROR；语义/风格多为 WARNING/WEAK WARNING；部分默认关闭控噪。
  - CSV 区段疑似排版问题：`IncorrectColumnSizeInspection` 下一行出现游离的 `enabledByDefault="true" level="ERROR"`。
  - Lint 集成：`PlsTigerLintAnnotator` 与 `PlsTigerLintInspection`（ERROR）。

---

## 待办与下一步

- 请指定下一个 include 文件（建议：`src/main/resources/META-INF/pls-images.xml`、`src/main/resources/META-INF/pls-integrations.xml`、`src/main/resources/META-INF/pls-ai.xml`）。
- 也可按主题推进（script/localisation/csv/images/ai/integrations/config 等）；每阅读一个文件即产出探索笔记并更新本报告。

---

## 变更历史（本会话）

- 2025-08-27：创建报告并完成第一阶段（README）探索，输出 `探索_1_README.md`。
- 2025-08-27：完成第二阶段（plugin.xml）探索，输出 `探索_2_plugin.xml.md`。
- 2025-08-27：完成第三阶段（pls-cwt.xml）探索，输出 `探索_3_pls-cwt.xml.md`。
- 2025-08-27：完成第四阶段（pls-script.xml）探索，输出 `探索_4_pls-script.xml.md`。
- 2025-08-27：完成第五阶段（pls-localisation.xml）探索，输出 `探索_5_pls-localisation.xml.md`。
- 2025-08-27：完成第六阶段（pls-csv.xml）探索，输出 `探索_6_pls-csv.xml.md`。
- 2025-08-27：完成第七阶段（pls-config.xml）探索，输出 `探索_7_pls-config.xml.md`。
- 2025-08-27：完成第八阶段（pls-lang.xml）探索，输出 `探索_8_pls-lang.xml.md`。
- 2025-08-27：完成第九阶段（pls-ep.xml）探索，输出 `探索_9_pls-ep.xml.md`。
- 2025-08-27：完成第十阶段（pls-intentions.xml）探索，输出 `探索_10_pls-intentions.xml.md`。
- 2025-08-27：完成第十一阶段（pls-inspections.xml）探索，输出 `探索_11_pls-inspections.xml.md`。
