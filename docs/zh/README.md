# 介绍

## 概述

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) |
[Discord](https://discord.gg/vBpbET2bXT) |
QQ群：653824651

用于编写群星模组（也支持其他P社游戏）的Intellij IDEA插件，智能、便捷且更具潜力。

特性：

* 支持脚本语言与本地化语言，以及规则文件所使用的CWT语言。
* 为脚本语言与本地化语言提供诸多较为完备的语言功能，包括代码高亮、代码导航、代码补全、代码检查、代码重构、快速文档、内嵌提示、动态模版、代码层级、图表、差异比较等。
* 为CWT语言提供基础的语言功能，包括代码高亮、代码导航、快速文档等。
* 支持预览与渲染DDS和TGA图片，提供不同图片格式（PNG、DDS、TGA）之间的相互转换的操作。
* 支持通过快速文档与内嵌提示等方式，渲染作用域上下文、本地化文本、图片以及其他有用的信息。
* 支持脚本语言与本地化语言的多数高级特性（例如参数、作用域、内联脚本和各种复杂表达式）。
* 支持自定义扩展的规则文件，从而允许插件提供更加完善的语言功能（例如代码导航、代码补全、快速文档和内嵌提示）。
* 自动识别游戏目录和模组目录。

此插件基于由CWT规则文件组成的[规则分组](config.md#config-group)，实现了诸多语言功能。
插件已经内置了最新版本的规则文件，以便开箱即用。
除此之外，插件也支持[自定义](config.md#writing-cwt-config-files)与[导入](config.md#importing-cwt-config-files)的规则文件。

如果同时安装了 [Translation](https://github.com/YiiGuxing/TranslationPlugin) ，此插件可以提供一些[额外的功能](plugin-integration.md)。

![](../images/preview_1_zh.png)

## 参考

参考手册：

* [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
* [JFlex - manual](https://www.jflex.de/manual.html)

工具和插件：

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [bcssov/IronyModManager](https://github.com/bcssov/IronyModManager)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Wiki：

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)
