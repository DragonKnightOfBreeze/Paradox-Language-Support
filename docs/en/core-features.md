# Core Features

## CWT Config Group{#cwt-config-group}

### Summary

PLS implements various advanced language features based on CWT config groups, which consists of many CWT rule files.

Different game types have different CWT config groups, and all game types share the core CWT config group.

The data in these config groups first comes from the CWT rule files in specific directories,
after merging and computing, it will be used to implement various features of this plugin.

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

Reference Links:

* [Guidance Documentation](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
* [Summary of Repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)
