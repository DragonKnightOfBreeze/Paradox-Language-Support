# 概述

[中文文档](README.md) | [English Documentation](README_en.md)

[中文参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) | [English Reference Documentation](https://windea.icu/Paradox-Language-Support/#/en/)

[Github](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)

[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)

IDEA插件：Paradox语言支持。

为脚本语言（主要是`*.txt`文件）和本地化语言（`*.yml`文件）
提供语法解析、代码检查、代码补全、导航、文档注释、内嵌提示、本地化文本渲染、DDS图片渲染以及其他诸多功能。
也为CWT语言提供必要的上述功能，为DDS图片文件提供一些方便的功能。

通过在你的模组根文件夹下创建描述符文件`descriptor.mod`以提供语言支持。
可以通过在`项目结构`页面中将对应的库添加到你的模组所属的模块，以将游戏目录或第三方模组导入作为依赖。

这个插件正在开发中，部分比较复杂的功能可能尚未实现，部分功能如代码检查、代码补全等可能不正确，并且可能发生意外的BUG。
这个插件目前尚未对群星以外的P社游戏进行充分的调试和验证，由于可能需要编写特定的解析器、规则文件等，当编写其他P社游戏的模组时，可能会发生意外的BUG。
如果需要更加完善的脚本语言支持，或是编写群星以外的P社游戏的模组，
请考虑使用 [VSCode](https://code.visualstudio.com) 以及 [CWTools](https://github.com/cwtools/cwtools-vscode) 插件。

这个插件与 [CWTools](https://github.com/cwtools/cwtools-vscode) 共享规则文件（`*.cwt`），但是相关功能尚未完全实现。
这些规则文件目前由插件内置，经过一定的修改和扩展，当发布时会自动同步到最新版本。

关于插件的功能和用法，请参阅 [参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) 。

![](https://windea.icu/Paradox-Language-Support/assets/images/script_file_preview_zh.png)

![](https://windea.icu/Paradox-Language-Support/assets/images/localisation_file_preview_zh.png)

# 参考

工具和插件：

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)

Wiki：

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)