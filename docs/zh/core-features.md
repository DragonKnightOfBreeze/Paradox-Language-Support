# 核心功能

## CWT规则分组{#cwt-config-group}

### 概述

PLS基于由CWT规则文件组成的CWT规则分组，实现了许多高级语言功能。

不同的游戏类型对应不同的CWT规则分组，除此之外，存在一个通用的核心CWT规则分组。

规则分组中的数据首先来自特定目录下的CWT规则文件，经过合并与计算后，再用于实现插件的各种功能。

### 内置的规则分组

其CWT规则文件位于插件jar包中的`config/${gameType}`目录下，并且始终启用。

这些规则文件来自插件仓库以及各自游戏的规则仓库，相较于CWTools所使用的规则文件，它们经过一定的修改和扩展。

### 项目本地的规则分组

其CWT规则文件需要放到项目根目录中的`.config/${gameType}`目录下，需要手动确认导入。

如果发生更改，编辑器右上角的上下文悬浮工具栏中将会出现刷新按钮。点击导入并启用后，即可应用这些自定义的规则文件。

### 覆盖规则

CWT规则会按照文件路径和规则ID进行后序覆盖。

例如，如果你在项目根目录下的规则文件`.config/stellaris/modifiers.cwt`中编写了自定义的规则，它将完全覆盖插件内置的修正规则。
因为插件内置的修正规则位于插件jar包中的规则文件`config/stellaris/modifiers.cwt`中，它们的路径都是`modifiers.cwt`。

如果此自定义的规则文件中没有任何内容，应用后插件将无法解析脚本文件中的任何修正。

### 参考链接

* [指引文档](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
* [仓库一览](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)
