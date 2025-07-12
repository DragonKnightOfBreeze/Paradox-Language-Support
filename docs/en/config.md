# Config Related

## Config Group {#config-group}

### Summary

PLS implements various advanced language features based on config groups, which consists of many CWT config files.

Config groups can have different sources. For config groups from the same source,
there are config groups for different game types, and the core config group, which is shared by all game types.

You can enable or disable each type of config group as needed in the plugin settings page (`Paradox Language Support > Config Related`).
The parent directories for these config groups, as well as the repository URLs for remote config groups, can also be configured directly from the plugin settings page.

Reference Links:

* [Repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/cwt)

### Sources {#sources}

#### Built-in Config Groups {#builtin-config-groups}

* Location: `config/{gameType}`
* Located inside the plugin jar, which is packaged within the plugin zip.
* `{gameType}` is the [Game Type ID](#game-type-id); for the shared config group, it is `core`.
* The shared built-in config group is always enabled.

#### Remote Config Groups {#remote-config-groups}

* Location: `{configDir}/{dirName}`
* `{configDir}` is the parent directory containing all remote config group directories, and can be customized in the plugin settings.
* `{dirName}` is the repository directory name; for the shared config group, it is `core`.
* After modifying the configuration, PLS will automatically clone and pull these config groups from the configured remote repositories.

#### Local Config Groups {#local-config-groups}

* Location: `{configDir}/{gameType}`
* `{configDir}` is the parent directory containing all global local config group directories, and can be customized in the plugin settings.
* `{gameType}` is the [Game Type ID](#game-type-id); for the shared config group, it is `core`.
* The config files inside are user-defined and apply to all projects. Changes require manual import.

#### Project Local Config Groups {#project-local-config-groups}

* Location: `{configDirName}/{gameType}`
* `{configDirName}` is the name of the directory for project local config groups, located directly under the project root (default is `.config`), and can be customized in the plugin settings.
* `{gameType}` is the [Game Type ID](#game-type-id); for the shared config group, it is `core`.
* The config files inside are user-defined and only apply to the current project. Changes require manual import.

### Overridden Strategy {#overridden-strategy}

Configs use the LIOS overridden strategy based on the file path and the config ID.

When reading configs, the plugin will iterate config groups by following order:
built-in config groups, local config groups, and project config groups.
The shared config group is shared by all game types, and will be iterated before the config group for related game type.

For example, if you have written some custom configs in the config file `.config/stellaris/modifiers.cwt`
(which is in the project root directory), it will completely override the built-in modifier configs.
Since the built-in modifier configs are located in the config file `config/stellaris/modifiers.cwt`
(which is in the plugin jar), and both of their file path is `modifiers.cwt`.
If these are no content in the custom config file, after applied, the plugin will be unable to resolve any modifier in script files.

### 游戏类型ID {#game-type-id}

Here is the list of available game type ids: `stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3` (for shared config group, it is `core`)

## CWT Config File {#cwt-config-file}

### Summary

CWT config file use its own file format, which can be considered as a variant of paradox script language.
Its file extension is `.cwt`.

### Syntax

The basic syntax of a CWT config file is as follows:

```cwt
# both equal sign ('=', '==') and not equal sign ('<>', '!=') can be used as the k-v separator (also available in options)
# options and values can be mixed in option clauses ('{...}')
# properties and values can be mixed in clauses ('{...}')

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

## Writing CWT Config Files {#writing-cwt-config-files}

For more detailed writing specifications for each CWT config,
currently you can refer to the writing style of the built-in config files.

Reference Links:

* [Guidance](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### Specifications

#### Priorities *(New in 1.3.7)*

Priority configs are used to configure the override order for targets (scripted variables, definitions, localisations and complex enums).

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

### Specifications (Extended CWT Configs)

> [!tip]
>
> These configs are mostly provided by users themselves,
> they can be used to enhance plugin's various language features,
> such as quick documentation, inlay hints, code highlighting and code completion.

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

Example:

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

Example:

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

#### About Template Expressions

The template expressions are composed of multiple data expressions (such as data expressions for string literals, definitions or localisations), and can be used for more flexible matching.

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

#### How to Use Ant Expressions in Config Files *(New in 1.3.6)*

Since 1.3.6, ant expressions can be used for more flexible matching.

```cwt
# a ant expression use prefix 'ant:'
ant:/foo/bar?/*
# a ant expression use prefix 'ant.i:' (ignore case)
ant.i:/foo/bar?/*

# wildcards in ant expressions:
# '?' - used to match any single character
# '*' - used to match any characters (exclude '/')
# '**' - used to match any characters
```

#### How to Use Regular Expressions in Config Files *(New in 1.3.6)*

Since 1.3.6, regular expressions can be used for more flexible matching.

```cwt
# a regular expression use prefix 're:'
re:foo.*
# a regular expression use prefix 're.i:' (ignore case)
re.i:foo.*
```

#### How to Specify the Scope Context in Config Files

In config files, the scope context is specified by option `push_scope` and `replace_scopes`.

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

## Importing CWT Config Files {#importing-cwt-config-files}

### Summary

You can create custom config files in the directories of [remote config groups](#remote-config-groups), [local config groups](#local-config-groups), or [project local config groups](#project-local-config-groups).  
These files allow you to enhance or override the plugin's built-in configs, or to extend plugin functionality.

When changes are detected, a refresh button will appear in the context toolbar in the top-right corner of the editor.  
Click it to confirm the import and apply the changes from your custom config files.
Then, the IDE will then reparse open files in the background.

Note: If your changes affect the indexing logic (e.g., adding a new definition type or changing a match condition),
you may need to reindex the entire project (which may take several minutes) to ensure the plugin works properly,
if in the situation that involves these changes.
