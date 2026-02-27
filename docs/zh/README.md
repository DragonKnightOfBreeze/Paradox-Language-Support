# 介绍

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[插件市场](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) |
[Discord](https://discord.gg/vBpbET2bXT) |
[群聊](https://qm.qq.com/q/oRPgLwrTZm)

## 概述

Paradox Language Support（PLS）是专为 Paradox 游戏模组开发者设计的 IntelliJ IDEA 插件，提供智能、高效且功能全面的开发体验，助你轻松实现创意。

**核心特性：**

- **多语言支持**：完整支持模组开发所需的脚本语言、本地化语言与 CSV 语言，以及用于编写规则的 CWT 语言。
- **丰富的语言功能**：提供代码高亮、代码导航、代码补全、代码检查、代码重构、快速文档、内嵌提示、动态模板、代码层级、差异比较和图表等多项功能。
- **图像处理**：支持预览与渲染 DDS 和 TGA 图片，并可在不同图片格式（PNG、DDS、TGA）之间互相转换。
- **信息增强提示**：通过快速文档和内嵌提示，直观展示本地化文本、图片、作用域和参数等关键信息。
- **高级语言特性支持**：兼容脚本与本地化语言中的多种高级特性，包括参数、作用域、复杂表达式、内联脚本和定义注入等。
- **可扩展的规则系统**：支持自定义和导入规则文件，强化代码导航、代码补全、文档提示等功能。
- **工具集成**：集成 [Image Magick](https://www.imagemagick.org)、[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)、[Tiger](https://github.com/amtep/tiger) 等实用工具，提升开发效率。
- **AI 辅助**：初步集成 AI 技术，可用于本地化文本的翻译与润色。
- **目录检测**：自动检测游戏目录与模组目录，减少手动配置。

PLS 基于自身的[规则系统](https://windea.icu/Paradox-Language-Support/zh/config.html)实现核心语言功能。
其所使用的 CWT 规则文件与 [CWTools](https://github.com/cwtools/cwtools) 遵循基本一致的语法与格式，并进行了一定的改进与扩展。
插件内置最新版本规则，开箱即用，同时也支持[自定义](https://windea.icu/Paradox-Language-Support/zh/config.html#write-config-files)与[导入](https://windea.icu/Paradox-Language-Support/zh/config.html#import-config-files)规则文件，满足个性化开发需求。

![](../images/preview_1_zh.png)

## 参考链接

**官方文档：**

- [Kotlin Docs | Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Getting started | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/getting-started.html)
- [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [LangChain4j | LangChain4j](https://docs.langchain4j.dev/)

**工具与插件：**

- [cwtools/cwtools: A library for parsing, editing, and validating Paradox Interactive script files.](https://github.com/cwtools/cwtools)
- [cwtools/cwtools-vscode: A VS Code extension providing language server support for paradox script files using cwtools](https://github.com/cwtools/cwtools-vscode)
- [bcssov/IronyModManager: Mod Manager for Paradox Games. Official Discord: https://discord.gg/t9JmY8KFrV](https://github.com/bcssov/IronyModManager)
- [amtep/tiger: Checks game mod files for common mistakes and warns about them. Supports Crusader Kings 3, Victoria 3, and Imperator: Rome.](https://github.com/amtep/tiger)
- [nickbabcock/jomini: Parses Paradox files into javascript objects](https://github.com/nickbabcock/jomini)
- [OldEnt/stellaris-triggers-modifiers-effects-list: List of Stellaris triggers, modifiers and effects for most game versions since launch.](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
- [YiiGuxing/TranslationPlugin: Translation plugin for IntelliJ-based IDEs/Android Studio.](https://github.com/YiiGuxing/TranslationPlugin)

**教程与百科：**

- [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
- [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)
- [Stellaris Mod 教程](https://main--pdxdoc-next.netlify.app)

## 贡献与支持

欢迎任何形式的贡献与支持，包括但不限于：

- ⭐ 在 GitHub 上收藏项目。
- 🐛 提交问题反馈（通过 [Discord](https://discord.gg/vBpbET2bXT)、[群聊](https://qm.qq.com/q/oRPgLwrTZm) 或 [GitHub Issues](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues)）。
- 🔧 提交 Pull Request（提交至[插件仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)（即此项目），或者[各个规则仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md)）。
- 📢 向朋友或社区推荐本插件。
- 💝 通过[爱发电](https://afdian.com/a/dk_breeze)赞助项目。

如果你对提交 PR 感兴趣，但就插件开发或规则编写有任何疑问，欢迎通过[邮件](mailto:dk_breeze@qq.com)或 [Discord](https://discord.gg/vBpbET2bXT) 进行联系。

## 致谢

本项目使用 JetBrains IDEs 进行开发，并获得了 JetBrains 开源项目许可证的支持。

### Powered by

[![JetBrains logo.](https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg)](https://jb.gg/OpenSource)