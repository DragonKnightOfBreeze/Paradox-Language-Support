# 概述

[中文文档](README.md) | [English Document](README_en.md)

Paradox语言支持。

这个插件正在开发中，部分比较复杂的功能可能尚未实现。
如果需要更加完善的语言支持，请考虑使用**VSCode** 以及**CWTools**插件。

在你的模组根文件夹下创建描述符文件**descriptor.mod**，即可提供语言支持。
如果需要添加游戏目录以及第三方模组作为依赖，如同Java和Kotlin一样，将它们作为库添加到你的模组对应的项目模块即可。

目前仅支持**Stellaris**。
目前用于提供验证和代码补全等功能的规则文件尚不完善。

[参考文档](https://dragonknightofbreeze.github.io/paradox-language-support)将会在未来提供。

如果遇到BUG或问题，欢迎在项目的[Github Issue页面](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues)提出。

欢迎查看[本项目](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)和[我的其他项目](https://github.com/DragonKnightOfBreeze?tab=repositories)。

# 参考

关于本插件

* 插件信息：[plugin.xml](src/main/resources/META-INF/plugin.xml)
* 更新日志：[CHANGELOG.md](CHANGELOG.md)

相关链接

* [cwtools](https://github.com/cwtools/cwtools)
* [cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [cwtools-stellaris-config](https://github.com/cwtools/cwtools-stellaris-config)
