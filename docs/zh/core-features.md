# 核心功能

## CWT规则分组{#cwt-config-group}

### 概述

PLS基于CWT规则分组实现了许多高级语言功能。

不同的游戏类型对应不同的CWT规则分组，此外所有游戏类型共享核心的CWT规则分组。
规则分组中的数据首先来自特定目录下的CWT规则文件，经过合并与计算后，再用于实现插件的各种功能。

目前支持以下数种CWT规则分组：

* 内置的CWT规则分组 - 其CWT规则文件位于插件jar包中的`config/${gameType}`目录下。始终启用。
* 项目本地的CWT规则分组 - 其CWT规则文件需要放到项目根目录的`.config/${gameType}`目录下。需要手动确认导入。

当发生对应的更改后，会在编辑器右上角的上下文悬浮工具栏中出现刷新按钮，点击确认导入更改后的CWT规则分组。

注意，CWT规则会按照文件路径和规则ID进行后序覆盖。

例如，如果你在项目根目录下的规则文件`.config/stellaris/modifiers.cwt`中编写了自定义的规则，它将完全覆盖插件内置的修正规则。
因为插件内置的修正规则位于插件jar包中的规则文件`config/stellaris/modifiers.cwt`中，它们的路径都是`modifiers.cwt`。

参考链接：

* [指引文档](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
* [Github仓库](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)
