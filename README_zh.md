# Paradox Language Support - The Paradox Chronicle

<!-- Here we inscribe the revelations from the old era, the truths and realities of this land. -->
<!-- Ascend to the tower’s peak and open the Book of Prophecy, for within it are recorded all our discoveries and foresights. -->
<!-- Pay no heed to the mists and riddles of the past, for we shall reveal you a radiant horizon. -->

[![中文文档][badge:doc-zh]](README_zh.md)
[![English Documentation][badge:doc-en]](README.md)
[![参考文档][badge:doc-ref]][url:doc-ref]
[![Discord][badge:discord]][url:discord]
[![群聊][badge:qq-group]][url:qq-group]
<br/>
[![GitHub][badge:github]][url:github]
[![Release][badge:release]][url:release]
[![License][badge:license]][url:license]
[![Plugin Homepage][badge:plugin-homepage]][url:plugin-homepage]
[![Plugin Version][badge:plugin-version]][url:plugin-versions]
[![Plugin Downloads][badge:plugin-downloads]][url:plugin-homepage]
[![Plugin Rating][badge:plugin-rating]][url:plugin-homepage]
<br/>
[![Developed by Windea][badge:windea]][url:windea]
[![Supported by JetBrains][badge:jetbrains]][url:jetbrains]

## 概述

Paradox Language Support 是为 Paradox 游戏模组开发者设计的 IntelliJ IDEA 插件，提供智能、高效且功能全面的开发体验，助你轻松实现创意。

插件会自动检测游戏目录和模组目录，分析其中的文件以构建缓存与索引。
完成必要的配置工作（如游戏类型、游戏目录、模组依赖）以及项目分析（扫描文件、构建索引）后，即可启用并体验完整语言功能。

插件基于自身的[规则系统](https://windea.icu/Paradox-Language-Support/zh/config.html)实现核心语言功能。
其使用的 CWT 规则文件与 [CWTools](https://github.com/cwtools/cwtools) 遵循基本一致的语法与格式，并进行了一定的改进与扩展。
插件内置最新版本的规则，开箱即用，同时也支持[自定义](https://windea.icu/Paradox-Language-Support/zh/config.html#write-config-files)与[导入](https://windea.icu/Paradox-Language-Support/zh/config.html#import-config-files)规则文件，满足个性化开发需求。

**核心特性**：

- **多语言支持**：支持用于开发模组的脚本语言、本地化语言和 CSV 语言，以及用于编写规则的 CWT 语言。
- **高级语言构造支持**：支持参数、作用域、复杂表达式、内联脚本和定义注入等多种高级语言构造。
- **丰富的语言功能**：支持代码高亮、代码导航、代码补全、代码重构、意向动作、代码检查、快速文档、内嵌提示、代码层级、差异比较和图表等多种语言功能。
- **灵活的代码导航**：通过导航动作、快速文档、内嵌提示、代码层级、随处搜索等方式，快速导航到各种目标位置，包括文件、定义、本地化、相关本地化、相关图片、相关规则等。
- **增强的信息提示**：通过快速文档、内嵌提示等方式，直观显示各类关键信息，包括本地化文本、图片、作用域和参数等。
- **可扩展的规则系统**：支持自定义和导入规则文件，强化代码导航、代码补全、文档提示等功能。
- **图像处理**：支持预览和渲染 DDS 和 TGA 图片，并可在不同图片格式（PNG、DDS、TGA）之间互相转换。
- **工具集成**：集成 [Image Magick](https://www.imagemagick.org)、[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)、[Tiger](https://github.com/amtep/tiger) 等实用工具，提升开发效率。
- **AI 辅助**：初步集成 AI 技术，适用于本地化文本的翻译和润色。

![](docs/assets/preview_1_en.png)

![](docs/assets/preview_1_zh.png)

## 入门

<!-- TODO: updating -->

### 安装

- 使用 IDE 内置的插件系统：`Settings/Preferences` > `Plugins` > `Marketplace` > 搜索 "Paradox Language Support" > `Install`
- 使用 JetBrains Marketplace：前往 [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)，点击 `Install to ...` 按钮进行安装。
- 手动安装：下载[最新版本](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases/latest)并手动安装（无需解压）：`Settings/Preferences` > `Plugins` > `⚙️` > `Install plugin from disk...`

### 使用步骤

- 在 IDE 中打开你的模组根目录。
- 打开模组描述符文件（`descriptor.mod`，VIC3 和 EU5 中为 `.metadata/metadata.json`）。
- 点击编辑器右上角的悬浮工具栏中的模组设置按钮。
- 配置模组的游戏类型、游戏目录及所需的模组依赖。
- 确认配置，等待 IDE 完成项目分析。
- 开启你的模组开发之旅。

### 实用技巧

- **全局搜索**：
  - 使用 `Ctrl + Shift + R` 或 `Ctrl + Shift + F` 在当前项目、目录或指定查询作用域中搜索。
  - 使用 `Shift + Shift` 快速查找文件、定义、封装变量及其他符号。
- **代码导航**：
  - 使用 `Ctrl + 点击` 跳转到目标的声明或使用位置。
  - 使用 `Ctrl + Shift + 点击` 跳转到目标的类型声明。
  - 使用 `Alt + 点击` 跳转到目标的相关规则的声明。
  - 使用 `Shift + Alt + 点击` 跳转到目标相关本地化的声明。
  - 使用 `Ctrl + Shift + Alt + 点击` 跳转到目标的相关图片的声明。
  - 通过 `Navigate` 菜单（或者编辑器右键菜单中的 `Go To` 选项）快速定位。
  - 使用 `Navigate > Definition Hierarchy` 打开定义的类型层级窗口，从而查看特定类型的定义。
  - 使用 `Navigate > Call Hierarchy` 打开定义的调用层级窗口，从而查看定义、本地化、封装变量等的调用关系。
  - 在项目面板中选择 `Paradox Files` 视图，浏览汇总后的游戏与模组文件。
  - 在项目面板中选择 `CWT Config Files` 视图，浏览汇总后的规则文件。
- **代码检查**：
  - 在问题面板中查看当前文件的问题。
  - 使用 `Code > Inspect Code…` 执行全局代码检查，并在问题面板中查看详细报告。
- **设置修改**：
  - 可通过以下方式打开插件的全局设置页面：
    - 点击设置页面中的 `Languages & Frameworks > Paradox Language Support`。
  - 可通过以下方式打开模组设置对话框：
    - 点击编辑器右上角的悬浮工具栏中的蓝色齿轮图标。
    - 点击编辑器右键菜单中的 `Paradox Language Support > Open Mod Settings...`。
    - 点击主菜单中的 `Tools > Paradox Language Support > Open Mod Settings...`。
  - 可在全局设置中修改偏好语言环境、默认游戏类型、默认游戏目录等配置，以及其他功能细节。
  - 可在模组设置中调整游戏目录、模组依赖等配置。

### 问题排查

- 确保 IDE 和插件均为最新版本。
- 如果问题可能与索引有关，可尝试[清除缓存并重启 IDE](https://www.jetbrains.com/help/idea/invalidate-caches.html)。
- 如果问题可能与规则有关，可尝试[编写自定义的规则文件](https://windea.icu/Paradox-Language-Support/zh/config.html#write-config-files)。
- 如果问题可能与插件配置有关，可尝试删除插件的配置文件（`paradox-language-support.xml`，推荐使用 [Everything](https://www.voidtools.com) 搜索定位）。
- 欢迎通过 GitHub、Discord 等渠道反馈问题。

## 技术细节

- 主要采用 [Kotlin](https://kotlinlang.org/) 编程语言开发。
- 基于 [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html) 构建，基于 [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html)（而非 [LSP](https://microsoft.github.io/language-server-protocol)）实现深度的语义分析与丰富的语言功能。
- 使用 JFlex 进行词法分析，使用 BNF 进行语法解析。
- 内置丰富的自定义扩展点，提供功能的灵活扩展，便于定制与增强插件行为。
- 内置代码注入系统，用于实现常规手段无法达成的功能、修复与优化。
- 内置图像处理模块（DDS、TGA），用于预览、渲染和处理额外的图片格式。
- 内置工具集成模块（图片处理、翻译、检查工具），用于优化和扩展插件能力。
- 内置 AI 集成模块（初步，MVP 状态），用于翻译和润色本地化文本。

## 已知限制

<!-- TODO: updating -->

- 插件对脚本文件与本地化文件的部分复杂语言构造的支持尚不完整，并仍在完善中，欢迎反馈。
- 规则驱动的功能（如代码补全、代码检查和快速文档）的质量取决于各游戏 CWT 规则文件的完整度与时效性。
  目前，**Stellaris**、**Victoria 3** 和 **Europa Universalis V** 的规则正在积极维护中。
  其他游戏（如 Crusader Kings III、Hearts of Iron IV）的规则可能已过时，这会导致误报警告或缺少补全。
  欢迎向[各个规则仓库](cwt/README.md)贡献。
- 部分高级或不常见的脚本写法（如在内联脚本中声明定义、复杂的本地化命令等）尚未完全支持，正在逐步改进中。

## 贡献与支持

我们欢迎各种形式的贡献与支持，包括但不限于：

- 📢 向朋友或社区推荐此插件。
- ⭐ 在 GitHub 上收藏此项目。
- 💝 通过[爱发电][url:afdian] 赞助此项目。
- 🐛 提交问题反馈（通过 [GitHub Issues][url:issues]、[Discord][url:discord]、[群聊][url:qq-group] 或 [邮件][mailto]）。
- 🔧 提交 Pull Request（提交至[插件仓库][url:github]（即此项目），或者[各个规则仓库](cwt/README.md)）。

关于具体的贡献指南，请参见 [`CONTRIBUTING.md`](CONTRIBUTING.md)。

关于具体的贡献者名单，请参见 [`CONTRIBUTORS.md`](CONTRIBUTORS.md)。

### 贡献代码

鉴于插件同时拥有庞大的代码规模（约11万行 Kotlin 生产代码）与极高的复杂度（平台 & 领域 & 架构），贡献核心代码是极具挑战性的。
尽管如此，通过参考已有的文档、代码、配置文件和规则文件，AI 协助以及自行探索，考虑与实践部分代码上的贡献仍然是可能的。
例如，通过检查参考文档和配置文件（`plugin.xml` 以及其中引入的 XML 配置文件），结合关键词搜索，你可以尝试新增与完善各种语言功能，或是增强插件的能力和兼容性。

### 贡献文档

项目的文档主要分为普通文档（如 `README.md`）、维护者文档（位于 [documents](documents) 目录中）和参考文档（位于 [docs](docs) 目录中）。
这些文档中难免存在错误和不足之处、遗漏的细节以及待补充的内容，欢迎贡献以改善文档质量，尤其是项目的参考文档。

### 贡献规则文件

鉴于插件的规则驱动架构，贡献规则文件是改善特定游戏支持的最有效方式之一。
规则文件定义了驱动代码补全、代码检查和文档提示的语义信息（定义、修正、作用域、效果、触发器等），保持其更新将直接改善该游戏所有用户的使用体验。

作为参考，请阅读[规则系统的文档](https://windea.icu/Paradox-Language-Support/zh/config.html)、[语法的参考手册](https://windea.icu/Paradox-Language-Support/zh/ref-syntax.html)和[规则格式的参考手册](https://windea.icu/Paradox-Language-Support/zh/ref-config-format.html)。

## 参考链接

### 官方文档

- [Kotlin Docs | Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Getting started | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/getting-started.html)
- [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [LangChain4j | LangChain4j](https://docs.langchain4j.dev/)

### 工具与插件

- [cwtools/cwtools: A library for parsing, editing, and validating Paradox Interactive script files.](https://github.com/cwtools/cwtools)
- [cwtools/cwtools-vscode: A VS Code extension providing language server support for paradox script files using cwtools](https://github.com/cwtools/cwtools-vscode)
- [bcssov/IronyModManager: Mod Manager for Paradox Games. Official Discord: https://discord.gg/t9JmY8KFrV](https://github.com/bcssov/IronyModManager)
- [amtep/tiger: Checks game mod files for common mistakes and warns about them. Supports Crusader Kings 3, Victoria 3, and Imperator: Rome.](https://github.com/amtep/tiger)
- [nickbabcock/jomini: Parses Paradox files into javascript objects](https://github.com/nickbabcock/jomini)
- [OldEnt/stellaris-triggers-modifiers-effects-list: List of Stellaris triggers, modifiers and effects for most game versions since launch.](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
- [Victoria-3-Modding-Co-op/Modding-Digests: This repository contains modding digests for Victoria 3 updates.](https://github.com/Victoria-3-Modding-Co-op/Modding-Digests)

### 教程与百科

- [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
- [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)
- [Stellaris Mod 教程](https://main--pdxdoc-next.netlify.app)

## 关于

### Developed by

**Windea** - the dragon knight who with the title of breeze.

### Powered by

[![JetBrains logo.](https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg)][url:jetbrains]

[badge:doc-zh]: https://img.shields.io/badge/中文文档-2f89d7.svg
[badge:doc-en]: https://img.shields.io/badge/English%20Documentation-2f89d7.svg
[badge:doc-ref]: https://img.shields.io/badge/参考文档-2f89d7.svg
[badge:github]: https://img.shields.io/badge/GitHub-blue.svg?logo=github
[badge:release]: https://img.shields.io/github/release/DragonKnightOfBreeze/Paradox-Language-Support.svg?sort=semver
[badge:license]: https://img.shields.io/github/license/DragonKnightOfBreeze/Paradox-Language-Support.svg
[badge:plugin-homepage]: https://img.shields.io/badge/Plugin%20Homepage-orange.svg?logo=jetbrains
[badge:plugin-version]: https://img.shields.io/jetbrains/plugin/v/16825.svg?label=version
[badge:plugin-downloads]: https://img.shields.io/jetbrains/plugin/d/16825.svg
[badge:plugin-rating]: https://img.shields.io/jetbrains/plugin/r/rating/16825.svg
[badge:discord]: https://img.shields.io/badge/Discord-社区-blue.svg?logo=discord
[badge:qq-group]: https://img.shields.io/badge/群聊-653824651-blue.svg?logo=qq
[badge:windea]: https://img.shields.io/badge/Developed%20by-Windea-2f89d7.svg?style=flat
[badge:jetbrains]: https://img.shields.io/badge/Supported%20by-JetBrains-000000.svg?style=flat&logo=jetbrains

[url:doc-ref]: https://windea.icu/Paradox-Language-Support
[url:github]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support
[url:issues]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues
[url:release]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases
[url:license]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/LICENSE
[url:plugin-homepage]: https://plugins.jetbrains.com/plugin/16825-paradox-language-support
[url:plugin-versions]: https://plugins.jetbrains.com/plugin/16825-paradox-language-support/versions
[url:discord]: https://discord.gg/vBpbET2bXT
[url:qq-group]: https://qm.qq.com/q/oRPgLwrTZm
[url:afdian]: https://afdian.com/a/dk_breeze
[url:windea]: https://github.com/DragonKnightOfBreeze
[url:jetbrains]: https://jb.gg/OpenSource

[mailto]: mailto:dk_breeze@qq.com