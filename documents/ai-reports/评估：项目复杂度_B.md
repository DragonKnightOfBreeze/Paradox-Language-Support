# 评估：项目复杂度

> 评估日期：2026-02-14
> 数据来源：README、code_stats.py、config_stats.py、plugin.xml 及其引用的全部 18 个 XML 配置文件
> 模型：GPT-5.2 High Thinking

## 结论（摘要）

- **总体复杂度：高（High）**
- **复杂度主要来源**：
  - **规模很大**：IDE 插件代码（Kotlin/Java）接近 17 万行；内置/附带的 CWT config 约 23.5 万行。
  - **平台耦合深**：基于 IntelliJ Platform SDK + PSI + Stub/FileBasedIndex + 各类 Code Insight（补全/引用/导航/检查/重构/文档/内嵌提示/Hierarchy）。
  - **领域复杂**：同时覆盖 Paradox 模组开发的多语言（Script/Localisation/CSV/CWT）与多游戏（CK2/CK3/EU4/EU5/HOI4/IR/Stellaris/VIC2/VIC3）。
  - **扩展点密集**：自定义扩展点（EP）数量多，并在多个子系统中注册实现，且支持 optional 依赖功能。
  - **存在“非常规机制”**：包含 `inject` 子系统，对 IDE 内部实现进行注入/修复/优化，提升能力上限的同时也显著提高维护与兼容成本。

如果将其视为一个“语言支持插件 + 可扩展规则系统 + 工具集成平台”的组合体，则其复杂度已接近中大型 IDE 插件项目的上限区间。

## 规模指标（客观数据）

### 代码规模（来自 `code_stats.py`）

统计口径：按扩展名 `.kt` / `.java`，空行不计入“代码行数”。

#### src（包含 main + test）

- **Kotlin**
  - 文件数量：2016
  - 总行数：153524
  - 代码行数（非空行）：134671
  - 最大文件行数：1508
- **Java**
  - 文件数量：157
  - 总行数：16323
  - 代码行数（非空行）：13559

#### src/main

- **Kotlin**
  - 文件数量：1849
  - 总行数：131669
  - 代码行数（非空行）：115920
- **Java**
  - 文件数量：157
  - 总行数：16323
  - 代码行数（非空行）：13559

#### src/test

- **Kotlin**
  - 文件数量：167
  - 总行数：21855
  - 代码行数（非空行）：18751

#### 规模小结

- **主代码 + 测试代码**整体达到：
  - 代码文件总数（Kotlin + Java）：2173
  - 总行数（Kotlin + Java）：169847
  - 代码行数（非空行）：148230

这已经明显超出“小型插件”范畴，属于典型的**长期演进型大型插件代码库**。

### 规则规模（来自 `config_stats.py`）

统计口径：`cwt/**` 下各目录内 `.cwt` 文件。

- **CWT 文件总数**：1013
- **CWT 总行数**：234875
- **CWT 代码行数（非空行）**：206456

其中规模较大的目录：

- `cwt/cwtools-stellaris-config`
  - 文件数：163
  - 总行数：41537
- `cwt/cwtools-eu5-config`
  - 文件数：158
  - 总行数：35447
- `cwt/cwtools-eu4-config`
  - 文件数：75
  - 总行数：33366
- `cwt/cwtools-hoi4-config`
  - 文件数：110
  - 总行数：33120
- `cwt/cwtools-vic3-config`
  - 文件数：189
  - 总行数：32164

#### 规则规模小结

- 规则数据本身即构成一个“**中大型知识库**”，而且它并非静态：
  - README 明确强调“可扩展规则系统”“支持自定义与导入”。
  - 这意味着项目复杂度不只来自代码，还来自 **规则语义、版本差异、与工具链/解析器的一致性维护**。

## 结构复杂度（来自 plugin.xml 与各模块 XML）

### 插件装配结构

`plugin.xml` 通过 `xi:include` 引入 14 个模块 XML，并声明了 3 个 optional 依赖模块：

- `pls-inject.xml`
- `pls-cwt.xml`
- `pls-script.xml`
- `pls-localisation.xml`
- `pls-csv.xml`
- `pls-config.xml`
- `pls-images.xml`
- `pls-lang.xml`
- `pls-integrations.xml`
- `pls-ai.xml`
- `pls-intentions.xml`
- `pls-inspections.xml`
- `pls-tools.xml`
- `pls-extensions.xml`
- optional：`pls-extension-markdown.xml` / `pls-extension-diagram.xml` / `pls-extension-translation.xml`

这说明项目采用了**模块化的插件声明层**：每个 XML 大体对应一个“子系统边界”。从复杂度角度，这是好事（边界可见），但也意味着整体功能面非常广。

### 自定义扩展点（EP）数量与密度

从 XML 可直接确认的自定义 EP（不含 IntelliJ 平台自带 EP）：

- `pls-lang.xml`：搜索相关 EP 14 个（`icu.windea.pls.search.*`）
- `pls-ep.xml`：核心 EP 47 个（覆盖解析、匹配、覆盖策略、上下文、作用域推断、工具导入导出等）
- `pls-inject.xml`：注入相关 EP 3 个（`codeInjectorSupport` / `codeInjector` / `injectedFileProcessor`）
- `pls-images.xml`：图片支持 EP 1 个（`icu.windea.pls.images.support`）
- `pls-integrations.xml`：集成工具 EP 3 个（image/translation/lint tool provider）

合计：**至少 68 个自定义 EP**。

EP 的存在本身不等于“坏复杂度”，但 EP 数量大通常意味着：

- 需求面广且演进快（需要用扩展点解耦）。
- 行为组合多，回归测试面更大。
- 项目内部存在“可插拔策略”，理解成本高于单体逻辑。

### IntelliJ 平台能力覆盖面（从注册项可见）

以 `pls-lang.xml` 为例，其注册项覆盖：

- **文件与语言基础设施**
  - `fileTypeOverrider`、自定义 `fileType`（其他模块）、`parserDefinition`
  - 额外库根：`additionalLibraryRootsProvider`
  - VFS 监听：`vfs.asyncListener`
- **Code Insight 全家桶**
  - `completion.contributor`（CWT/Script/Localisation/CSV）
  - `annotator`（Script/Localisation/CSV + CWT 模块另有）
  - `inlayProvider` / `declarativeInlayProvider`
  - `lineMarkerProvider`
  - `parameterInfo`
  - `documentation`（psiTargetProvider、inlineDocumentationProvider、linkHandler）
- **导航/搜索/索引**
  - `gotoSymbolContributor`、`searchEverywhereContributor`
  - `languageStubDefinition`、`stubIndex`、`fileBasedIndex`
  - `referencesSearch`、`definitionsScopedSearch`、`searchScopesProvider`
- **重构能力**
  - `lang.refactoringSupport`
  - 多个 `renameInputValidator`、`automaticRenamerFactory`
  - `inlineActionHandler`（含 inline script 优先级 order=first）
- **层级视图/差异比较/工具动作**
  - `typeHierarchyProvider`、`callHierarchyProvider`
  - 多组 `actions`（导航、生成、重构、diff、编辑器菜单）

换句话说：PLS 并非“仅提供语法高亮”，而是**深度进入 IDE 的语义层与工程能力层**。这种覆盖面天然决定了高复杂度与高维护成本。

### “注入系统”（pls-inject.xml）带来的额外复杂度

`pls-inject.xml` 显式声明了：

- 多类 `codeInjectorSupport`
- 大量 `codeInjector`（包含：核心注入、提供额外特性、图片读写优化、修复 IDE bug、性能/内存优化等）

这属于非常强的工程手段：

- **优点**：可突破平台限制、修复或绕开上游缺陷、做性能优化。
- **代价**：
  - 与 IDE 内部实现细节耦合，IDE 升级时风险更高。
  - 调试与定位更困难（行为可能被注入层改变）。
  - 对团队知识要求高（需要了解平台内部机制）。

在复杂度评估里，这是一个非常强的“加权项”。

## 复杂度分解与评分（建议口径）

为了让复杂度结论可复用，给出一个可解释的维度评分（1-5，越高越复杂）：

- **规模复杂度（代码 + 规则体量）**：5/5
  - 证据：169847 行代码 + 234875 行 CWT config。
- **平台复杂度（IntelliJ SDK 覆盖面）**：5/5
  - 证据：索引、引用、导航、重构、检查、文档、inlay、hierarchy、actions 全面覆盖。
- **领域复杂度（Paradox 多语言 + 多游戏）**：5/5
  - 证据：Script/Localisation/CSV/CWT 四语言；多个游戏 config；且存在 game-specific 特性与差异。
- **架构复杂度（扩展点与可插拔策略）**：4/5
  - 证据：至少 68 个自定义 EP；大量策略类注册。
- **变更风险（兼容性/回归面）**：5/5
  - 证据：注入系统 + 索引/解析/检查/重构等“高敏感功能”。
- **可测试性与可验证性（从结构推断）**：3/5
  - 已存在 `src/test`（Kotlin 2.1 万行规模），但由于平台耦合深，很多能力的端到端验证成本仍然高。

综合判断：**高复杂度（High）**，并且属于“会随 IDE 版本、游戏版本、规则版本持续变动”的高维护项目。

## 主要复杂度驱动因素（解释性描述）

- **驱动因素 1：规则系统是核心，不是附属**
  - README 明确说明语言能力建立在“规则系统”之上。
  - `pls-config.xml` 提供 config group 相关的 library/provider/listener 等支撑。
  - `pls-ep.xml` 里大量与 config context / overridden / related / expression resolver 相关的 EP。

- **驱动因素 2：索引与搜索体系复杂且多层**
  - 同时存在 stub index 与 file-based index。
  - 同时提供 choose-by-name、Search Everywhere、Find Usages、自定义 QueryExecutor 搜索等。

- **驱动因素 3：语言能力是“跨文件、跨语言、跨规则”的**
  - 例如 script 里的定义/引用要能链接 localisation、图片、scope、参数、dynamic value。
  - 这类能力天然要求复杂的解析、上下文推断与缓存策略。

- **驱动因素 4：生态工具集成增加外部变量**
  - `pls-integrations.xml`：ImageMagick / texconv / Tiger 等工具，带来路径、版本、平台差异。
  - `pls-extension-translation.xml`：可选的翻译插件接入。

- **驱动因素 5：注入系统扩大能力边界，也扩大维护边界**
  - 这是“为了达成功能或性能目标而引入的高阶工程手段”。

## 维护性风险点（从证据推断）

- **兼容性风险**
  - 注入系统对 IDE 内部实现敏感。
  - 可选依赖（Markdown/Diagram/Translation）需要处理不同安装组合。

- **性能与正确性风险**
  - 大量索引/搜索/解析/检查会叠加到 IDE 的后台任务与 UI 响应。
  - CWT config 体量巨大，任何“全量扫描/解析”的策略都需要精细缓存与增量更新。

- **回归面风险**
  - inspections、intentions、actions、inlay、documentation、refactoring 等能力互相联动。

## 附：本次阅读到的模块清单

- `src/main/resources/META-INF/plugin.xml`
- `src/main/resources/META-INF/pls-*.xml`（注入、语言、规则、图片、集成、AI、意图、检查、工具、扩展点等）

（以上清单用于后续做“子系统地图/维护指南”时复用。）
