# 规则分组与规则文件

## CWT规则分组{#cwt-config-group}

### 概述

PLS基于由CWT规则文件组成的CWT规则分组，实现了许多高级语言功能。

规则分组中的数据首先来自特定目录下的CWT规则文件，经过合并与计算后，再用于实现插件的各种功能。

参考链接：

* [仓库一览](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)

### 分类

**内置的规则分组**

其CWT规则文件位于插件jar包中的`config/${gameType}`[^gameType]目录下，并且始终启用。

这些规则文件来自插件仓库以及各自游戏的规则仓库，相较于CWTools所使用的规则文件，它们经过一定的修改和扩展。

**项目本地的规则分组**

其CWT规则文件需要放到项目根目录中的`.config/${gameType}`[^gameType]目录下，并且需要手动确认导入。

如果发生更改，编辑器右上角的上下文悬浮工具栏中会出现刷新按钮。点击确认导入后，即可应用这些自定义的规则文件。

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

### 备注

关于模版表达式：

```cwt
# belows are all valid template expressions

# a string literal, exactly matches 'x'
x
# a template expression which contains a reference to jobs, matches 'a_researcher_b', 'a_farmer_b', etc.
a_<job>_b
# a template expression which contains a references to enum of weight_or_base, matches 'a_weight_b' and 'a_base_b'
a_enum[weight_or_base]_b
# a template expression which contains a references to dynamic value type of anything
# there is no limit for 'value[anything]', so it's equivalent to regex 'a_.*_b'
a_value[anything]_b
```

关于作用域上下文的指定方式：

```cwt
# push 'country' scope to scope stack
# for this example, the next this scope will be 'country'
## push_scope = country
some_config

# replace scopes of specific system scopes into scope context
# not supported for 'prev' system scope (and 'prevprev', etc.)
# for this example, the next this scope will be 'country', so do the next root scope and the next from scope
## replace_scopes = { this = country root = country from = country }
some_config
```

### Definitions

```cwt
definitions = {
    # 'x' or 'x = xxx'
    # 'x' can also be a template expression
    
    ### Some documentation
    ## type = civic_or_origin.civic
    x
}
```

### Game Rules

```cwt
game_rules = {
    # 'x' or 'x = xxx'
    # 'x' can also be a template expression
    # use 'x = xxx' to override declaration config
    
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    x
}
```

### On Actions

```cwt
on_actions = {
    # 'x' or 'x = xxx'
    # 'x' can also be a template expression
    
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    ## event_type = country
    x
}
```

### Inline Scripts

```cwt
inline_scripts = {
    # 'x' or 'x = xxx'
    # 'x' is a inline script expression, e.g., for 'inline_script = jobs/researchers_add', 'x' should be 'jobs/researchers_add'
    # 'x' can also be a template expression
    # use 'x = xxx' to declare context config(s) (add '## context_configs_type = multiple' if there is various context configs)
    
    # note extended documentation is unavailable for inline scripts
    
    x

    # more detailed examples for declaring context config(s)

    ## context_configs_type = multiple
    x = {
        ## cardinality = 0..1
        potential = single_alias_right[trigger_clause]
        ## cardinality = 0..1
        possible = single_alias_right[trigger_clause]
    }
}
```

示例：



### Parameters

```cwt
parameters = {
    # 'x' or 'x = xxx'
    # 'x' is a parameter name, e.g., for '$JOB$', 'x' should be 'JOB'
    # 'x' can also be a template expression
    # use 'x = xxx' to declare context config(s) (add '## context_configs_type = multiple' if there is various context configs)
    
    ### Some documentation
    ## context_key = scripted_trigger@some_trigger
    x
    
    # more detailed examples for declaring context config(s)
    
    x = localistion
    ## context_configs_type = multiple
    x = {
        localisation
        scalar
    }
}
```

示例：

![](../assets/images/config/screenshot_parameters_1.png)

### Dynamic Values

```cwt
values = {
    value[event_target] = {
        # 'x', not 'x = xxx'
        
        ### Some documentation
        ## replace_scopes = { this = country root = country }
        x
    }
}
```


## 导入CWT规则文件{#importing-cwt-config-files}

### 概述

你可以在项目根目录中的`.config/${gameType}`[^gameType]目录下编写自定义的规则文件。这些规则文件需要手动确认导入。

如果发生更改，编辑器右上角的上下文悬浮工具栏中会出现刷新按钮。点击确认导入后，即可应用这些自定义的规则文件。

IDE将会花费一些时间重新解析已打开的文件，
并且请注意，如果规则文件的更改会引发索引逻辑的更改 （例如，新增了一种定义类型，或是更改了某种定义类型的匹配条件），
你可能需要重新索引整个项目（这可能需要花费数分钟），以使在涉及到这些更改的场合，插件能够正常工作。

[^gameType]: 允许的`gameType`的值：`stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3`