# 概述

[中文文档](README.md) | [English Documentation](README_en.md)

[中文参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) | [English Reference Documentation](https://windea.icu/Paradox-Language-Support/#/en/)

[Github](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)

[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)

IDEA插件：Paradox语言支持。

支持Paradox脚本语言（以`*.txt`文件为主）和本地化语言（`*.yml`文件），
提供语法解析、引用解析、快速文档、代码导航、代码检查、代码补全、内嵌提示、动态模版、本地化文本渲染、DDS图片渲染等功能。

支持CWT语言（`*.cwt`文件），
提供语法解析、引用解析、快速文档、代码导航等功能。

支持直接在IDE中查看DDS图片，提供必要的编辑器功能，如同IDE对普通图片的支持一样。

通过在你的模组根文件夹下创建描述符文件`descriptor.mod`，以将其中匹配的文件识别为脚本文件或本地化文件。

通过在`项目结构`页面中将游戏目录或者第三方模组目录作为库添加到所属的项目或模块，以将其导入作为你的模组的依赖。

通过配置页面`Settings > Languages & Frameworks > Paradox Language Support`（以及某些通用配置页面），可以变更插件的一些配置。

这个插件正在开发中，部分比较复杂的功能可能尚未实现，并且使用过程中可能会发生意外的BUG。
如果需要使用这个插件尚未实现或者尚不完善的功能，
请考虑使用 [VSCode](https://code.visualstudio.com) 及其插件 [CWTools](https://github.com/cwtools/cwtools-vscode) 。

这个插件与 [CWTools](https://github.com/cwtools/cwtools-vscode) 共享规则文件（`*.cwt`），
这些规则文件目前由插件内置，经过一定的修改和扩展。当插件发布新版本时，规则文件也会同步到最新版本。

如果同时安装了 [Translation](https://github.com/YiiGuxing/TranslationPlugin) ，这个插件可以提供一些[额外的功能](https://windea.icu/Paradox-Language-Support/#/zh/plugin-integration.md)。

关于插件的功能和用法，请参阅 [参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) （有待详细编写）。

![](https://windea.icu/Paradox-Language-Support/assets/images/script_file_preview_zh.png)

![](https://windea.icu/Paradox-Language-Support/assets/images/localisation_file_preview_zh.png)

# 参考

工具和插件：

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)
* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Wiki：

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)