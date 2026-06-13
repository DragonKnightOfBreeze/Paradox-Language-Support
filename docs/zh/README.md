# 介绍

<!-- With the guidebook currently being written, we are fully prepared — welcome to the real world. -->

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Plugin Home](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) |
[Discord](https://discord.gg/vBpbET2bXT) |
[群聊](https://qm.qq.com/q/oRPgLwrTZm) |
[By me a coffee ☕](https://afdian.com/a/dk_breeze)

## 概述

Paradox Language Support 是为 Paradox 游戏模组开发者设计的 IntelliJ IDEA 插件，提供智能、高效且功能全面的开发体验，助你轻松实现创意。

插件会自动检测游戏目录和模组目录，分析其中的文件以构建缓存与索引。
完成必要的配置工作（如游戏类型、游戏目录、模组依赖）以及项目分析（扫描文件、构建索引）后，即可启用并体验完整语言功能。

插件基于自身的[规则系统](config.md)实现核心语言功能。
其使用的 CWT 规则文件与 [CWTools](https://github.com/cwtools/cwtools) 遵循基本一致的语法与格式，并进行了一定的改进与扩展。
插件内置最新版本的规则，开箱即用，同时也支持[自定义](config.md#write-config-files)与[导入](config.md#import-config-files)规则文件，满足个性化开发需求。

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

![](../assets/preview_1_en.png)

![](../assets/preview_1_zh.png)

## 已知限制

- 插件对脚本文件与本地化文件的部分复杂语言构造的支持尚不完整，并仍在完善中，欢迎反馈。
- 规则驱动的功能（如代码补全、代码检查和快速文档）的质量取决于各游戏 CWT 规则文件的完整度与时效性。
  目前，**Stellaris**、**Victoria 3** 和 **Europa Universalis V** 的规则正在积极维护中。
  其他游戏（如 Crusader Kings III、Hearts of Iron IV）的规则可能已过时，这会导致误报警告或缺少补全。
  欢迎向[各个规则仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md)贡献。
- 部分高级或不常见的脚本写法（如在内联脚本中声明定义、复杂的本地化命令等）尚未完全支持，正在逐步改进中。

## 技术细节

- 基于 IntelliJ Platform SDK 构建，采用 Kotlin 开发，基于 [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html)（而非 [LSP](https://microsoft.github.io/language-server-protocol)）实现深度的语言解析与操作。
- 使用 BNF 进行语法解析，JFlex 进行词法分析。
- 通过扩展点机制实现功能的动态扩展，便于插件自身及模组开发者定制与增强插件行为。
- 内置自定义的代码注入器，用于实现常规手段无法达成的语言功能。
- 内置与图片处理、翻译和检查工具的集成，以优化和扩展插件能力。
- 初步集成 AI 技术，为本地化文本提供翻译和润色支持。

## 贡献与支持

欢迎任何形式的贡献与支持，包括但不限于：

- ⭐ 在 GitHub 上收藏项目。
- 🐛 提交问题反馈（通过 [Discord][url:discord]、[群聊][url:qq-group] 或 [GitHub Issues][url:issues]）。
- 🔧 提交 Pull Request（提交至[插件仓库][url:github]（即此项目），或者[各个规则仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md)）。
- 📢 向朋友或社区推荐本插件。
- 💝 通过[爱发电][url:afdian]赞助项目。

如果你对提交 PR 感兴趣，但就插件开发或规则编写有任何疑问，欢迎通过[邮件][mailto]或 [Discord][url:discord] 进行联系。

**贡献规则文件**：

鉴于插件的规则驱动架构，贡献 CWT 规则文件是改善特定游戏支持的最有效方式之一。
规则文件定义了驱动代码补全、代码检查和文档提示的语义信息（定义、修正、作用域、效果、触发器等）——保持其更新将直接改善该游戏所有用户的使用体验。

各规则仓库的链接请参见 [cwt/README.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md)，规则格式的编写指南请参见[规则文档](config.md)。

当前维护者与贡献者名单请参见 [CONTRIBUTORS.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/CONTRIBUTORS.md)。

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

[url:github]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support
[url:issues]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues
[url:release-latest]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases/latest
[url:plugin-homepage]: https://plugins.jetbrains.com/plugin/16825-paradox-language-support
[url:discord]: https://discord.gg/vBpbET2bXT
[url:qq-group]: https://qm.qq.com/q/oRPgLwrTZm
[url:afdian]: https://afdian.com/a/dk_breeze
[url:profile]: https://github.com/DragonKnightOfBreeze
[url:jetbrains]: https://jb.gg/OpenSource

[mailto]: mailto:dk_breeze@qq.com