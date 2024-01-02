# 规则分组与规则文件

## CWT规则分组{#cwt-config-group}

### 概述

PLS基于由CWT规则文件组成的CWT规则分组，实现了许多高级语言功能。

规则分组中的数据首先来自特定目录下的CWT规则文件，经过合并与计算后，再用于实现插件的各种功能。

参考链接：

* [仓库一览](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)

### 分类

**内置的规则分组**

其CWT规则文件位于插件jar包中的`config/${gameType}`目录下，并且始终启用。

这些规则文件来自插件仓库以及各自游戏的规则仓库，相较于CWTools所使用的规则文件，它们经过一定的修改和扩展。

**项目本地的规则分组**

其CWT规则文件需要放到项目根目录中的`.config/${gameType}`目录下，并且需要手动确认导入。

如果发生更改，编辑器右上角的上下文悬浮工具栏中将会出现刷新按钮。点击导入并启用后，即可应用这些自定义的规则文件。

### 覆盖规则

CWT规则会按照文件路径和规则ID进行后序覆盖。

例如，如果你在项目根目录下的规则文件`.config/stellaris/modifiers.cwt`中编写了自定义的规则，它将完全覆盖插件内置的修正规则。
因为插件内置的修正规则位于插件jar包中的规则文件`config/stellaris/modifiers.cwt`中，它们的路径都是`modifiers.cwt`。

如果此自定义的规则文件中没有任何内容，应用后插件将无法解析脚本文件中的任何修正。

## CWT规则文件{#cwt-config-file}

### 概述

CWT规则文件使用一种特别的文件格式，可以视为Paradox脚本语言的变种。它的文件扩展名为`.cwt`。

### 语法

CWT规则文件的基本语法如下所示：

```cwt
### documentation comment
## option = option_value
## option_value
prop = {
	# line comment
    # properties and values can be mixed in clauses
    # both equal sign ('=', '=='), not equal sign ('<>', '!=')can be used for the property separator
    
    k = v
    v
}
```

## 编写CWT规则文件{#writing-cwt-config-files}

### 概述

正在更新中。

以下列出的只是一些用于自定义规则的编写规范，关于更详细的编写规范，请参考下方的参考链接以及插件内置的那些规则文件。

参考链接：

* [指引文档](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### Definitions

```cwt
definitions = {
    ### Some documentation
	## type = civic_or_origin.civic
	# 'x' or 'x = xxx'
    # 'x' can also be a template expression (e.g. for 'job_<job>_add', '<job>' matches any job name)
    x
}
```

### Game Rules

```cwt
game_rules = {
    ### Some documentation
    ## replace_scopes = { this = country root = country }
	# 'x' or 'x = xxx'
	# 'x' can also be a template expression (e.g. for 'job_<job>_add', '<job>' matches any job name)
    # use 'x = xxx' to override declaration config
    x
}
```

### On Actions

```cwt
on_actions = {
    ### Some documentation
    ## replace_scopes = { this = country root = country }
	## event_type = country
    # 'x' or 'x = xxx'
	# 'x' can also be a template expression (e.g. for 'job_<job>_add', '<job>' matches any job name)
    x
}
```

### Parameters

```cwt
parameters = {
    ### Some documentation
    ## context_key = scripted_trigger@some_trigger
	# 'p' or 'p = xxx'
	# 'p' can also be a template expression (e.g. for 'job_<job>_add', '<job>' matches any job name)
    p
}
```

### Dynamic Values

```cwt
values = {
    value[event_target] = {
        ### Some documentation
		## replace_scopes = { this = country root = country }
        # 'v', not 'v = xxx'
        v
    }
}
```
