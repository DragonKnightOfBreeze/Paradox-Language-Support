# 探索_1_README

> 时间：2025-08-27 15:15（本地）
> 来源文件：`README.md`
> 目的：从总览文档推断项目定位、功能边界、集成生态与后续探索线索。

---

## 一、项目概览（从 README 提炼）

- **项目名称**：Paradox Language Support（简称 PLS）。
- **项目类型**：JetBrains/IntelliJ 平台插件，用于 P 社（Paradox）游戏的模组开发，重点支持 Stellaris（群星），并兼容其他同系游戏（CK3/IR/VIC3 等）。
- **价值主张**：为脚本/本地化/CSV/CWT 语言提供“智能、便捷且更具潜力”的 IDE 级语言能力与工具整合，降低模组开发门槛、提升效率与质量。
- **目标用户**：Paradox 系游戏模组作者、维护者，以及需要阅读与分析脚本/规则文件的技术用户。

## 二、支持对象与语言范围

- **游戏范围**：明确支持 Stellaris；对其它 P 社游戏处于“进行中/不完全”的支持状态。
- **语言范围**：
  - 模组脚本语言（event、trigger、effect 等）
  - 本地化语言（yaml/loc 等）
  - CSV 语言
  - CWT 规则语言（配置规则，用于驱动/约束语言能力）

## 三、核心功能矩阵（语言智能 + 资源处理）

- **IDE 语言能力**：代码高亮、导航、补全、检查（inspections）、重构、快速文档、内嵌提示（inlay hints）、动态模板（live templates）、层级（definition/call）、图表、差异比较等。
- **高级脚本能力**：参数、作用域、内联脚本与复杂表达式解析与提示。
- **信息渲染**：在快速文档/内嵌提示中渲染本地化文本、图片、作用域与参数信息。
- **图片预览与转换**：支持 DDS/TGA 预览，并提供 PNG/DDS/TGA 相互转换能力（推断需要外部工具 ImageMagick 支持）。
- **规则驱动的语言特性**：以“规则分组（Config Group）+ 规则文件（CWT）”为核心，内置最新版本规则，支持用户自定义与导入，决定/增强诸多语言能力（如补全/导航/快速文档等）。
- **自动目录识别**：自动识别游戏安装目录与模组目录。
- **初步 AI 集成**：当前聚焦于本地化文本的翻译与润色。

## 四、生态与集成

- **工具与插件**：
  - 图片工具：ImageMagick（图片转换与处理）
  - 翻译工具：Translation Plugin（YiiGuxing/TranslationPlugin）
  - 静态分析：Tiger（ck3-tiger），用于脚本/本地化检查
- **相关生态项目**（参考/灵感/数据来源）：
  - cwtools/cwtools、cwtools-vscode（CWT 相关）
  - IronyModManager（模组管理）
  - stellaris-triggers-modifiers-effects-list（资料清单）

## 五、用户工作流（从“快速开始”总结）

- 打开模组根目录 → 打开描述符（`descriptor.mod`，VIC3 为 `.metadata/metadata.json`）→ 通过编辑器右上角悬浮工具栏进入“模组配置” → 设置游戏类型/目录/依赖 → 等待索引 → 开始开发。
- 常用操作：
  - 全局搜索（Ctrl Shift R/F）、通用搜索（Shift Shift）
  - 多维导航（定义/类型/CWT 规则/本地化/图片、Definition Hierarchy、Call Hierarchy）
  - 专属项目视图：`Project > Paradox Files` 与 `Project > CWT Config Files`
  - 全局检查：`Problems` 窗口与 `Code > Inspect Code...`
  - 设置入口：`Settings > Languages & Frameworks > Paradox Language Support` 或编辑器悬浮工具、右键菜单与 `Tools` 菜单
  - 故障排除：更新 IDE/插件、重建索引、编写/导入 CWT 规则、重置插件配置、GitHub/Discord 反馈

## 六、已知问题与边界

- Stellaris 的“黑魔法”语法特性仍在完善。
- Stellaris 之外的其他游戏支持仍不完全（优先级可能取决于规则/生态与用户诉求）。

## 七、文档与社区

- **主页与仓库**：GitHub、Plugin Marketplace、Reference 文档（`docs/` 同站点镜像）、Discord、QQ群。
- **参考资料**：Kotlin 文档、IntelliJ IDEA/Platform SDK 文档、JFlex 手册等。

## 八、工程与技术栈（从 README 的线索“合理推断”）

- **语言/平台**：Kotlin + IntelliJ Platform（插件）。
- **构建**：Gradle（通常配合 Gradle IntelliJ Plugin）。
- **语言实现**：
  - 词法/语法：参考了 JFlex（lexer 生成）文档，推测部分语言的词法实现使用 JFlex。
  - 规则引擎：以 CWT 规则文件驱动 IDE 功能（补全/导航/校验/文档/提示等）。
- **外部依赖**：ImageMagick、Translation Plugin、Tiger（ck3-tiger）。

> 注：以上为基于 README 的“配置与生态线索”推断，后续将以 `META-INF/plugin.xml` 及其包含的配置文件进行佐证或修正。

## 九、配置与规则体系（初步）

- 插件围绕“规则分组（Config Group）”组织功能，`cwt/` 目录下有多套规则仓库（README 链接指向该目录说明）。
- 规则可内置（开箱即用）也可用户自定义/导入；这意味着插件启动时会加载一套默认规则，并允许覆盖/扩展。

## 十、风险与改进建议（从 README 出发）

- **跨游戏一致性**：不同游戏规则差异大，建议强化“配置分层”与“版本/兼容策略”。
- **AI 能力扩展**：从本地化文本扩展到脚本意图理解、规则生成、批量修复建议等。
- **规则可观测性**：提供“规则生效视图/调试工具”，帮助用户定位为何某补全或检查生效/失效。
- **图像链路稳定性**：针对 DDS/TGA 处理链增加依赖检测、失败回退与更友好的错误提示。
- **检查工具整合**：与 Tiger 的结果联动（问题面板聚合、跳转定位、quick fix 协议）。

## 十一、后续探索计划

- 按照用户指定的步骤，下一步准备阅读：`src/main/resources/META-INF/plugin.xml`（插件主配置）。
- 重点关注：
  - `plugin.xml` 中声明的 actions、services、extensions、fileType、language、inspections、settings UI 等
  - `include` 的其他 XML 配置（下一阶段逐个阅读并产出探索笔记）。

## 附录：关键链接（来自 README）

- GitHub：<https://github.com/DragonKnightOfBreeze/Paradox-Language-Support>
- 文档站（参考）：<https://windea.icu/Paradox-Language-Support>
- Marketplace：<https://plugins.jetbrains.com/plugin/16825-paradox-language-support>
- Discord：<https://discord.gg/vBpbET2bXT>
- 规则说明：`cwt/README.md`（仓库内）
- 参考文档：Kotlin、IntelliJ Platform SDK、JFlex 等
