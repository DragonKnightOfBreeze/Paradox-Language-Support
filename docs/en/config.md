# Config Groups & Config Files

## CWT Config Group{#cwt-config-group}

### Summary

PLS implements various advanced language features based on CWT config groups, which consists of many CWT rule files.

The data in these config groups first comes from the CWT rule files in specific directories,
after merging and computing, it will be used to implement various features of this plugin.

Reference Links:

* [Repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)

### Built-in config groups

Their CWT config files are located in the 'config/${gameType}' directory (which is in the plugin jar), and they will always be enabled.

These config files are from plugin repository and config repositories of each game. Compare to the config files used by CWTools, there are several modifications and extensions. 

### Project local config groups

Their CWT config files should be placed in the '.config/${gameType}' directory (which is in the project root directory), and they will be enabled after manually confirmation and importation.

If some changes are happened, the refresh button will be appeared in the context float toolbar in the upper right corner of the editor. Click it to confirm and import, so these custom config files will be enabled.

### Overridden strategy

The CWT config files use the LIOS overridden strategy based on the file path and the config ID.

For example, if you have written some custom configs in the config file `.config/stellaris/modifiers.cwt` (which is in the project root directory), it will completely override the built-in modifier rules.
Since the built-in modifier configs are located in the config file `config/stellaris/modifiers.cwt` (which is in the plugin jar), and both of their path is `modifiers.cwt`.

If these are no content in the custom config file, after applied, the plugin will be unable to resolve any modifier in script files.

## CWT Config File{#cwt-config-file}

### Summary

CWT config file use its own file format, which can be considered as a variant of paradox script language. Its file extension is `.cwt`.

### Syntax

The basic syntax of a CWT rules file is as follows:

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

## Writing CWT Config Files{#writing-cwt-config-files}

### Summary

In progress.

Listed below are just some of the writing specifications used for config customization,
For more detailed writing specifications, please refer to the reference links below and the built-in config files.

Reference Links:

* [Guidance](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### Note

About template expression:

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