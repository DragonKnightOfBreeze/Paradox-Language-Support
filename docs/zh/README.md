# 简介

## 概述

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) |
[Discord](https://discord.gg/pbPYSK4n) |
QQ群：653824651

IDEA插件：Paradox语言支持。

特性：

* 支持脚本语言（主要是`*.txt`文件）和本地化语言（`*.yml`文件），
  提供代码高亮、代码导航、代码补全、代码检查、代码重构、快速文档、内嵌提示、动态模版、代码层级、图表、差异比较等诸多语言功能。
* 支持CWT语言（`*.cwt`文件），提供基础的语言功能。
* 支持直接在IDE中查看DDS图片，提供包括转为PNG图片在内的一些有用的编辑器功能。
* 对于脚本语言和本地化语言的多数高级特性（例如参数、作用域、内联脚本和各种复杂表达式），也提供了相当完善的支持。
* 通过快速文档和内嵌提示等方式，渲染相关的类型信息、作用域信息、本地化文本和DDS图片。
* 自动识别游戏目录（包含启动器配置文件`launcher-settings.json`）和模组目录（包含模组描述符文件`descriptor.mod`）。

此插件基于由CWT规则文件组成的[CWT规则分组](https://windea.icu/Paradox-Language-Support/#/zh/config.md#cwt-config-group)，实现了诸多语言功能。
插件已经内置了最新版本的规则文件，以便开箱即用。
除此之外，插件也支持[自定义](https://windea.icu/Paradox-Language-Support/#/zh/config.md#writing-cwt-config-files)与[导入](https://windea.icu/Paradox-Language-Support/#/zh/config.md#importing-cwt-config-files)本地的规则文件。

如果同时安装了 [Translation](https://github.com/YiiGuxing/TranslationPlugin) ，此插件可以提供一些[额外的功能](https://windea.icu/Paradox-Language-Support/#/zh/plugin-integration.md)。

如果在使用过程中遇到任何问题，欢迎通过GitHub、Discord或者QQ群进行反馈。

![](https://windea.icu/Paradox-Language-Support/assets/images/preview_1_zh.png)

## 快速上手

使用：

* 通过IDE打开你的模组根目录。（需要直接包含模组描述符文件`descriptor.mod`）
* 打开模组描述符文件，点击位于编辑器右上角的悬浮工具栏（或者编辑器右键菜单）中的模组配置按钮，配置模组的游戏类型、游戏目录和额外的模组依赖。
* 点击确定按钮完成配置，然后等待IDE索引完成。
* 开始你的模组编程之旅吧！

提示：

* 如果需要进行全局搜索，请参考以下方式：
  * 点击`Ctrl Shift R`或者`Ctrl Shift F`在当前项目、目录或者指定作用域中搜索。
  * 点击`Shift Shift`查找文件、定义、封装变量（scripted_variable）以及其他各种符号。
* 如果需要进行代码导航，请参考以下方式：
  * 按住`Ctrl`并点击目标位置，从而导航到目标的声明或使用处。
  * 按住`Ctrl Shift`并点击目标位置，从而导航到目标的类型声明处。
  * 按住`Alt`并点击目标位置，从而导航到目标对应的CWT规则的声明处。
  * 按住`Shift Alt`并点击目标位置，从而导航到目标定义的相关本地化的声明处。
  * 按住`Ctrl Shift Alt`并点击目标位置，从而导航到目标定义的相关图片的声明处。
  * 点击`Navigate`或者编辑器右键菜单中的`Go To`，选择要导航到的目标。
  * 点击`Navigate > Definition Hierarchy`，打开定义层级窗口，从而查看某一类型的定义的实现关系。
  * 点击`Navigate > Call Hierarchy`，打开调用层级窗口，从而查看定义、本地化、封装变量（scripted_variable）等的调用关系。
  * 点击`Alt 1`或者`Project`工具窗口，打开项目视图面板，然后点击左上角的`Project > Paradox Files`，从而查看汇总后的游戏与模组文件。
  * 点击`Alt 1`或者`Project`工具窗口，打开项目视图面板，然后点击左上角的`Project > CWT Config Files`，从而查看汇总后的CWT规则文件。
* 如果需要进行全局代码检查，请参考以下方式：
  * 点击`Alt 6`或者`Problems`工具窗口，打开问题面板，然后查看当前文件存在的问题，或者进行整个项目的全局代码检查。
  * 点击`Code > Inspect Code...`，进行整个项目的全局代码检查。
  * 完成代码检查后，IDE将会在问题面板中显示详细的检查结果。
* 如果需要更改插件的全局配置，请参考以下方式：
  * 点击`Settings > Languages & Frameworks > Paradox Language Support`，打开插件的配置页面。
* 如果需要更改模组类型、游戏目录、模组依赖等配置，请通过以下方式之一打开模组配置对话框：
  * 点击`Settings > Languages & Frameworks > Paradox Language Support`，可配置默认的游戏目录。
  * 点击位于页面右上方的编辑器悬浮工具栏中的蓝色齿轮图标。
  * 在编辑器中打开右键菜单，点击`Paradox Language Support > Open Mod Settings...`。
  * 点击`Tools > Paradox Language Support > Open Mod Settings...`。
* 如果IDE卡死，或者IDE索引出现问题，或者发生了插件引起的报错，请尝试通过以下方式解决：
  * 更新IDE和插件到最新版本。
  * 删除插件的配置文件`paradox-language-support.xml`。（如果不知道具体的文件位置，请使用[Everything](https://www.voidtools.com)）
  * 重建索引并重启IDE。（点击`File -> Invalidate Caches... -> Invalidate and Restart`）

已知问题：

* 对Stellaris以外的游戏的支持尚不完善。

## 参考

参考手册：

* [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
* [JFlex - manual](https://www.jflex.de/manual.html)

工具和插件：

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Wiki：

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)