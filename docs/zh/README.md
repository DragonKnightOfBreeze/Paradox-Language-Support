# 概述

IDEA插件：Paradox语言支持。

为脚本语言（主要是`*.txt`文件）和本地化语言（`*.yml`文件）
提供语法解析、代码检查、代码补全、导航、文档注释、内嵌提示、本地化文本渲染、DDS图片渲染以及其他诸多功能。
也为CWT语言提供必要的上述功能，为DDS图片文件提供一些方便的功能。

通过在你的模组根文件夹下创建描述符文件`descriptor.mod`以提供语言支持。
可以通过在`项目结构`页面中将对应的库添加到你的模组所属的模块，以将游戏目录或第三方模组导入作为依赖。

这个插件正在开发中，部分比较复杂的功能可能尚未实现，并且可能会发生意外的BUG。
如果需要更加完善的脚本语言支持，请考虑使用[VSCode](https://code.visualstudio.com)以及[CWTools](https://github.com/cwtools/cwtools-vscode)插件。
这个插件与[CWTools](https://github.com/cwtools/cwtools-vscode)共享规则文件（`*.cwt`），经过一定的修改和扩展，但是相关功能尚未完全实现。

![](https://windea.icu/Paradox-Language-Support/assets/images/script_file_preview_zh.png)

![](https://windea.icu/Paradox-Language-Support/assets/images/localisation_file_preview_zh.png)

说明：

* 在你的模组根文件夹下创建描述符文件`descriptor.mod`，即可提供语言支持。  
* 在你的模组根文件夹下创建标记文件`.${gameType}`，即可指定游戏类型。（如`.stellaris`）  
* 支持的游戏类型：ck2 / ck3 / eu4 / hoi4 / ir / stellaris / vic2。
* 支持的P社游戏：Crusader Kings II（十字军之王 II） / Crusader Kings III（十字军之王 III） / Europa Universalis IV（欧陆风云 IV） / Hearts of Iron IV（钢铁雄心 IV） / Imperator: Rome（帝国：罗马） / Stellaris（群星） / Victoria II（维多利亚 II）。

提示：

* 如果需要添加游戏目录以及第三方模组作为依赖，如同Java和Kotlin一样，将它们作为库添加到你的模组对应的项目模块即可。
* 如果遇到某些有关索引的IDE问题，请尝试重新构建索引。（点击`File -> Invalidate Caches... -> Invalidate and Restart`）

# 参考

工具和插件：

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)

Wiki：

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)