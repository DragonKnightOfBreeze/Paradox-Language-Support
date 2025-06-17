# 规则分组与规则文件

## 规则分组 {#config-group}

### 概述

PLS基于由CWT规则文件组成的规则分组，实现了诸多语言功能。

规则分组可以有不同的来源，而对于同一来源的规则分组，又区分为各自游戏类型的规则分组，以及所有游戏类型共享的规则分组。

你可以在插件的设置页面（`Paradox Language Support > Config Related`）中按需启用或禁用各类规则分组。
这些规则分组的父目录，以及远程规则分组的仓库地址，也都可以在对应的插件设置页面中进行配置。

参考链接：

* [仓库一览](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/cwt)

### 来源

#### 内置的规则分组 {#builtin-config-groups}

* 位置：`config/{gameType}`
* 位于插件压缩包中的插件jar包中。
* `{gameType}`为[游戏类型ID](#game-type-id)，对于共享的规则分组则为`core`。
* 共享的内置规则分组总是会被启用。

#### 远程的规则分组 {#remote-config-groups}

* 位置：`{configDir}/{dirName}`
* `{configDir}`为包含所有远程的规则分组目录的父目录，可在插件设置页面中自定义。
* `{dirName}`为仓库目录的名字，对于共享的规则分组则为`core`。

更改配置后，PLS会自动从配置的远程仓库中克隆和拉取这些规则分组。

#### 本地的规则分组 {#local-config-groups}

* 位置：`{configDir}/{gameType}`
* `{configDir}`为包含所有本地的规则分组目录的父目录，可在插件设置页面中自定义。
* `{gameType}`为[游戏类型ID](#game-type-id)，对于共享的规则分组则为`core`。

其中的规则文件由用户自定义，适用于所有项目。对它们的更改需要手动确认导入。

#### 项目本地的规则分组 {#project-local-config-groups}

* 位置：`{configDirName}/{gameType}`
* `{configDirName}`为项目本地的规则目录的名字，直接位于项目根目录下，默认为`.config`，可在插件设置页面中自定义。
* `{gameType}`为[游戏类型ID](#game-type-id)，对于共享的规则分组则为`core`。

其中的规则文件由用户自定义，仅适用于当前项目。对它们的更改需要手动确认导入。

### 覆盖策略

规则会按照文件路径和规则ID进行后序覆盖。

读取规则时，插件会依次遍历内置的规则分组、本地的规则分组以及项目本地的规则分组。
共享的规则分组由所有游戏类型共享，会在对应游戏类型的规则分组之前被遍历。

例如，如果你在项目根目录下的规则文件`.config/stellaris/modifiers.cwt`中编写了自定义的规则，它将完全覆盖插件内置的修正规则。
因为插件内置的修正规则位于插件jar包中的规则文件`config/stellaris/modifiers.cwt`中，它们的文件路径都是`modifiers.cwt`。
如果此自定义的规则文件中没有任何内容，应用后插件将无法解析脚本文件中的任何修正。

### 游戏类型ID {#game-type-id}

游戏类型ID目前有以下可选值：`stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3`（对于共享的规则分组则为`core`）

## CWT规则文件 {#cwt-config-file}

### 概述

CWT规则文件使用一种特别的文件格式，可以视为Paradox脚本语言的变种。它的文件扩展名为`.cwt`。

### 语法

CWT规则文件的基本语法如下所示：

```cwt
# both equal sign ('=', '==') and not equal sign ('<>', '!=') can be used as the k-v separator (also available in options)
# properties (options) and values can be mixed in clauses (also available in options)

### documentation comment
## option = option_value
## option_0 = { k = v }
## option_value
prop = {
    # line comment
    k = v
    v
}
```

## 编写CWT规则文件 {#writing-cwt-config-files}

### 概述

正在更新中。

关于更详细的编写规范，请参考下方的参考链接以及插件内置的那些规则文件。

参考链接：

* [指引文档](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### 编写规范

#### Priorities *(New in 1.3.7)*

优先级规则可以用来配置目标（封装变量，定义、本地化与复杂枚举）的覆盖顺序。

```cwt
priorities = {
    # LHS - file path (relative to game or mod root directory)
    # RHS - priority (available values: "fios", "lios", "ordered", default value: "lios", ignore case)
    
    # file path - path of specific directory (e.g. ""common/on_actions", "common/scripted_variables", "localisation")
    
    # fios - use the one that reads first, ignore all remaining items
    # lios - use the one that reads last (if not specified, use this as default)
    # ordered - reads by order, no overrides
    
    "events" = fios
    # ...
}
```

#### System Scopes

TODO

#### Locales

TODO

#### Types and Subtypes

TODO

#### Declarations

TODO

#### Enums and Complex Enums

TODO

#### Dynamic Values

TODO

#### Aliases and Single Aliases

TODO

#### Inlines

TODO

#### Modifiers and Modifier Groups

TODO

#### Links

TODO

#### Scopes and Scope Groups

TODO

#### Localisation Links and Localisation Commands

TODO

### 编写规范（扩展的CWT规则）

> [!tip]
>
> 这些规则基本上由用户自行编写，用于强化插件的各项语言功能，例如快速文档、内嵌提示、代码高亮与代码补全。

#### Scripted Variables *(New in 1.3.5)*

```cwt
scripted_variables = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    
    ### Some documentation
    ## hint = §RSome inlay hint text§!
    x
}
```

#### Definitions

```cwt
definitions = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    
    ### Some documentation
    ## type = civic_or_origin.civic
    x
    
    # since 1.3.5, scope context related options are also available here
    ## type = scripted_trigger
    ## replace_scopes = { this = country root = country }
    x
}
```

#### Game Rules

```cwt
game_rules = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to override declaration config
    
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    x
}
```

#### On Actions

```cwt
on_actions = {
    # 'x' or 'x = xxx'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    
    ### Some documentation
    ## replace_scopes = { this = country root = country }
    ## event_type = country
    x
}
```

#### Inline Scripts

```cwt
inline_scripts = {
    # 'x' or 'x = xxx'
    # 'x' is a inline script expression, e.g., for 'inline_script = jobs/researchers_add', 'x' should be 'jobs/researchers_add'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to declare context config(s) (add '## context_configs_type = multiple' if there are various context configs)
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

    # since 1.3.5, scope context related options are also available here

    ## replace_scopes = { this = country root = country }
    x
    
    # since 1.3.6, using single alias at root level is also available here
    
    ## context_configs_type = multiple
    x = single_alias_right[trigger_clause]
}
```

示例：

![](../images/config/inline_scripts_1.png)

#### Parameters

```cwt
parameters = {
    # 'x' or 'x = xxx'
    # 'x' is a parameter name, e.g., for '$JOB$', 'x' should be 'JOB'
    # 'x' can also be a pattern expression (template expression, ant expression or regex)
    # use 'x = xxx' to declare context config(s) (add '## context_configs_type = multiple' if there are various context configs)
    
    # for value of option 'context_key',
    # before '@' is the containing definition type (e.g., 'scripted_trigger'), or 'inline_script' for inline script parameters
    # after '@' is the containing definition name, or the containing inline script path
    # since 1.3.6, value of option 'context_key' can also be a pattern expression (template expression, ant expression or regex)
    
    ### Some documentation
    ## context_key = scripted_trigger@some_trigger
    x
    
    # more detailed examples for declaring context config(s)
    
    ## context_key = scripted_trigger@some_trigger
    x = localistion
    
    ## context_key = scripted_trigger@some_trigger
    ## context_configs_type = multiple
    x = {
        localisation
        scalar
    }
    
    # since 1.3.5, scope context related options are also available here
    
    ## context_key = scripted_trigger@some_trigger
    ## replace_scopes = { this = country root = country }
    x
    
    # since 1.3.6, using single alias at root level is also available here
    
    ## context_key = scripted_trigger@some_trigger
    ## context_configs_type = multiple
    x = single_alias_right[trigger_clause]
    
    # since 1.3.12, a parameter's config context and scope context can be specified to inherit from its context
    # e.g. for parameter 'x' with context key 'scripted_trigger@some_trigger', its context is scripted trigger 'some_trigger'
    
    ## context_key = scripted_trigger@some_trigger
    ## inherit
    x
}
```

示例：

![](../images/config/parameters_1.png)

#### Complex Enum Values

```cwt
complex_enum_values = {
    component_tag = {
        # 'x' or 'x = xxx'
        # 'x' can also be a pattern expression (template expression, ant expression or regex)
        
        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x
    }
}
```

#### Dynamic Values

```cwt
dynamic_values = {
    event_target = {
        # 'x' or 'x = xxx'
        # 'x' can also be a pattern expression (template expression, ant expression or regex)

        ### Some documentation
        ## hint = §RSome inlay hint text§!
        x

        # since 1.3.9, scope context related options are also available here
        # only receive push scope (this scope), ignore others (like root scope, etc.)

        ## push_scope = country
        x
    }
}
```

### FAQ

#### 关于模版表达式

模版表达式由多个数据表达式（如定义、本地化、字符串字面量对应的数据表达式）组合而成，用来进行更加灵活的匹配。

```cwt
# a string literal, exactly matches 'x'
x
# a template expression which contains a reference to jobs, matches 'a_researcher_b', 'a_farmer_b', etc.
a_<job>_b
# a template expression which contains a references to enum values of 'weight_or_base', matches 'a_weight_b' and 'a_base_b'
a_enum[weight_or_base]_b
# a template expression which contains a references to dynamic values of 'anything'
# generally, there is no limit for 'value[anything]', so this expression is equivalent to regex 'a_.*_b'
a_value[anything]_b
```

#### 如何在规则文件中使用ANT表达式 *(New in 1.3.6)*

从1.3.6开始，可以通过ANT表达式进行更加灵活的匹配。

```cwt
# a ant expression use prefix 'ant:'
ant:/foo/bar?/*
# a ant expression use prefix 'ant.i:' (ignore case)
ant.i:/foo/bar?/*

# wildcards in ant expression:
# '?' - used to match any single character
# '*' - used to match any characters (exclude '/')
# '**' - used to match any characters
```

#### 如何在规则文件中使用正则表达式 *(New in 1.3.6)*

从1.3.6开始，可以通过正则表达式进行更加灵活的匹配。

```cwt
# a regex use prefix 're:'
re:foo.*
# a regex use prefix 're.i:' (ignore case)
re.i:foo.*
```

#### 如何在规则文件中指定作用域上下文

在规则文件中，作用域上下文是通过选项`push_scope`与`replace_scopes`来指定的。

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

## 导入CWT规则文件 {#importing-cwt-config-files}

### 概述

你可以在[本地的规则分组](#local-config-groups)、[项目本地的规则分组](#project-local-config-groups)或[远程的规则分组](#remote-config-groups)对应的目录下编写自定义规则文件。  
这些文件可以用于完善插件内置规则，或增强插件功能。

当检测到有变更时，编辑器右上角的悬浮工具栏会出现刷新按钮。  
点击确认导入后，这些自定义规则文件的更改将会被应用。

IDE会在后台重新解析已打开的文件。  
注意：如果规则文件的更改会影响索引逻辑（如新增定义类型、修改某定义类型的匹配条件等），你可能需要重新索引整个项目（这可能需要数分钟），以确保在涉及到这些更改的场合，插件正常工作。
