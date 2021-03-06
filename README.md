# 概述

[中文文档](README.md) | [English Documentation](README_en.md)

[中文参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) | [English Reference Documentation](https://windea.icu/Paradox-Language-Support/#/en/)

[Github](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)

Paradox语言支持。

这个插件正在开发中，部分比较复杂的功能可能尚未实现，并且可能会发生意外的BUG。
如果需要更加完善的语言支持，请考虑使用[VSCode](https://code.visualstudio.com)以及[CWTools](https://github.com/cwtools/cwtools-vscode)插件。
这个插件与[CWTools](https://github.com/cwtools/cwtools-vscode)共享规则文件（`*.cwt`），但是相关功能尚未完全实现。

说明：

* 在你的模组根文件夹下创建描述符文件`descriptor.mod`，即可提供语言支持。  
* 在你的模组根文件夹下创建标记文件`.${gameType}`，即可指定游戏类型。（如`.stellaris`）  
* 支持的游戏类型：ck2 / ck3 / eu4 / hoi4 / ir / stellaris / vic2。
* 支持的P社游戏：Crusader Kings II（十字军之王 II） / Crusader Kings III（十字军之王 III） / Europa Universalis IV（欧陆风云 IV） / Hearts of Iron IV（钢铁雄心 IV） / Imperator: Rome（帝国：罗马） / Stellaris（群星） / Victoria II（维多利亚 II）。

提示：

* 如果需要添加游戏目录以及第三方模组作为依赖，如同Java和Kotlin一样，将它们作为库添加到你的模组对应的项目模块即可。
* 如果遇到某些有关索引的IDE问题，请尝试重新构建索引。（点击`File -> Invalidate Caches... -> Invalidate and Restart`）

# 参考

关于本插件

* 插件信息：[plugin.xml](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/src/main/resources/META-INF/plugin.xml)
* 更新日志：[CHANGELOG.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/CHANGELOG.md)

相关链接

* [cwtools](https://github.com/cwtools/cwtools)
* [cwtools vscode](https://github.com/cwtools/cwtools-vscode)
* [.cwt config file guidance](https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance)

