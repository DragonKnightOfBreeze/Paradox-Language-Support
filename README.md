# Paradox Language Support

[![中文文档](https://img.shields.io/badge/文档-中文-blue)](README.md)
[![English Documentation](https://img.shields.io/badge/Docs-English-green)](README_en.md)

[![GitHub](https://img.shields.io/badge/GitHub-Repo-blue?logo=github)](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)
[![参考文档](https://img.shields.io/badge/文档-参考-orange)](https://windea.icu/Paradox-Language-Support)
[![插件市场](https://img.shields.io/badge/JetBrains-插件市场-orange)](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)
[![Discord](https://img.shields.io/badge/Discord-社区-blue?logo=discord)](https://discord.gg/vBpbET2bXT)
[![群聊](https://img.shields.io/badge/群聊-653824651-blue?logo=qq)](https://qm.qq.com/q/oRPgLwrTZm)

## 概述

Paradox Language Support（PLS）是一款专为 Paradox 游戏模组开发者设计的 IntelliJ IDEA 插件，提供智能、高效且功能全面的开发体验，助您轻松实现创意。

**核心特性：**

- **多语言支持**：完整支持模组开发所需的脚本语言、本地化语言与 CSV 语言，以及用编写规则的 CWT 语言。
- **丰富的语言功能**：提供代码高亮、导航、补全、检查、重构、快速文档、内嵌提示、动态模板、代码层级、图表和差异比较等多项功能。
- **图像处理**：支持预览与渲染 DDS 和 TGA 图片，并可在 PNG、DDS、TGA 等格式之间互相转换。
- **信息增强提示**：通过快速文档和内嵌提示，直观展示本地化文本、图片、作用域和参数等关键信息。
- **高级语言特性支持**：兼容脚本与本地化语言中的多种高级特性，包括参数、作用域、内联脚本及复杂表达式。
- **可扩展的规则系统**：支持自定义和导入规则文件，强化代码导航、补全、文档提示等功能。
- **工具集成**：集成 [Image Magick](https://www.imagemagick.org)、[Translation Plugin](https://github.com/yiiguxing/TranslationPlugin)、[Tiger](https://github.com/amtep/tiger) 等实用工具，提升开发效率。
- **AI 辅助**：初步集成 AI 技术，可用于本地化文本的翻译与润色。
- **目录识别**：自动识别游戏目录与模组目录，减少手动配置。

PLS 基于自身的[规则系统](https://windea.icu/Paradox-Language-Support/zh/config.html)实现核心语言功能。其所使用的 CWT 规则文件与 [CWTools](https://github.com/cwtools/cwtools) 遵循一致的语法与格式，并进行了一定的改进与扩展。插件内置最新版本规则，开箱即用，同时也支持[自定义](https://windea.icu/Paradox-Language-Support/zh/config.html#writing-cwt-config-files)与[导入](https://windea.icu/Paradox-Language-Support/zh/config.html#importing-cwt-config-files)规则文件，满足个性化开发需求。

![](docs/images/preview_1_zh.png)

## 快速开始

### 使用步骤

1. 在 IDE 中打开你的模组根目录。
2. 打开模组描述符文件（`descriptor.mod`，VIC3 中为 `.metadata/metadata.json`）。
3. 点击编辑器右上角的悬浮工具栏中的 *模组配置* 按钮。
4. 配置模组的游戏类型、游戏目录及所需的模组依赖。
5. 确认配置，等待 IDE 完成索引。
6. 开始你的模组编程之旅吧！

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
  - 使用 `Navigate` 菜单（或者编辑器右键菜单中的 `Go To` 选项）快速定位。
  - 使用 `Navigate > Definition Hierarchy` 打开定义的类型层级窗口，从而查看特定类型的定义。
  - 使用 `Navigate > Call Hierarchy` 打开定义的调用层级窗口，从而查看定义、本地化、封装变量等的调用关系。
  - 在项目面板中选择 `Paradox Files` 视图，查看汇总后的游戏与模组文件。
  - 在项目面板中选择 `CWT Config Files` 视图，查看汇总后的规则文件。
- **代码检查**：
  - 在问题面板中查看当前文件的问题。
  - 使用 `Code > Inspect Code…` 执行全局代码检查，并在完成后在问题面板中查看详细报告。
- **设置修改**：
  - 可以通过以下方式打开插件的全局设置页面：
    - 点击设置页面中的 `Languages & Frameworks > Paradox Language Support`。
  - 可以通过以下方式打开模组设置对话框：
    - 点击位于编辑器右上角的悬浮工具栏中的蓝色齿轮图标。
    - 点击编辑器右键菜单中的 `Paradox Language Support > Open Mod Settings...`。
    - 点击主菜单中的 `Tools > Paradox Language Support > Open Mod Settings...`。
  - 可在插件的全局设置页面中，修改默认语言环境、默认游戏类型、默认游戏目录等设置，以及各种功能的细节设置。
  - 可在模组设置对话框中，修改游戏目录、模组依赖等设置。
- **问题排查**：
  - 确保更新 IDE 和插件到最新版本。
  - 如果问题可能与索引有关，请尝试重建索引并重启 IDE（`File > Invalidate Caches… > Invalidate and Restart`）。
  - 如果问题可能与规则有关，请尝试[编写自定义的规则文件](https://windea.icu/Paradox-Language-Support/zh/config.html#writing-cwt-config-files)。
  - 如果问题可能与插件配置有关，请尝试删除插件的配置文件（`paradox-language-support.xml`，可使用 Everything 工具搜索定位）。
  - 欢迎通过 GitHub、Discord 等渠道反馈问题。

### 已知限制

- 对 Stellaris 的部分复杂语言特性的支持仍在完善中。
- 对非 Stellaris 游戏的特有语言特性的支持尚不完善，欢迎反馈和贡献。
- 目前仅为 Stellaris 和 Victoria 3 提供了较为完善的内置规则文件，欢迎提交 Pull Request。

## 技术细节

- 基于 IntelliJ Platform SDK 构建，使用 Kotlin 开发，基于 [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html)（而非 [LSP](https://microsoft.github.io/language-server-protocol)）实现深度的语言解析和操作。
- 使用 BNF 进行语法解析，使用 JFlex 进行词法分析。
- 通过扩展点机制实现功能的动态扩展，方便插件自身与模组开发者定制与增强插件行为。
- 内置自定义的代码注入器，用于达成无法通过常规手段实现的 IDE 功能。
- 内置与图像、翻译和检查工具的集成。用于优化和扩展插件的功能。
- 初步集成 AI 技术，为本地化文本提供翻译和润色支持。

## 参考链接

官方文档：

- [Kotlin Docs | Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Getting started | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/getting-started.html)
- [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [LangChain4j | LangChain4j](https://docs.langchain4j.dev/)

工具与插件：

- [YiiGuxing/TranslationPlugin: Translation plugin for IntelliJ-based IDEs/Android Studio.](https://github.com/YiiGuxing/TranslationPlugin)
- [cwtools/cwtools: A library for parsing, editing, and validating Paradox Interactive script files.](https://github.com/cwtools/cwtools)
- [cwtools/cwtools-vscode: A VS Code extension providing language server support for paradox script files using cwtools](https://github.com/cwtools/cwtools-vscode)
- [bcssov/IronyModManager: Mod Manager for Paradox Games. Official Discord: https://discord.gg/t9JmY8KFrV](https://github.com/bcssov/IronyModManager)
- [amtep/tiger: Checks game mod files for common mistakes and warns about them. Supports Crusader Kings 3, Victoria 3, and Imperator: Rome.](https://github.com/amtep/tiger)
- [OldEnt/stellaris-triggers-modifiers-effects-list: List of Stellaris triggers, modifiers and effects for most game versions since launch.](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)

游戏 Wiki：

- [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
- [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)

## 贡献与支持

我们欢迎任何形式的支持与贡献，包括但不限于：

- ⭐ 在 GitHub 上收藏项目
- 🐛 提交问题反馈（通过 [Discord](https://discord.gg/vBpbET2bXT)、[QQ 群](https://qm.qq.com/q/oRPgLwrTZm)或在 GitHub 上提交 Issue）
- 🔧 提交代码或规则文件 Pull Request（可向[插件仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)（即此项目）或[各个规则仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md)提交）
- 📢 向朋友或在社区中推荐本插件
- 💝 通过[爱发电](https://afdian.com/a/dk_breeze)赞助项目

如果你对提交 PR 感兴趣，但对插件代码或规则编写有任何疑问，欢迎通过邮件或 [Discord](https://discord.gg/vBpbET2bXT) 进行联系！

---

