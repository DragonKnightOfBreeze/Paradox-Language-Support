# Core Features

### CWT Config Group{#cwt-config-group}

### Summary

PLS implements many advanced language functions based on CWT config groups.

Different game types have different CWT config groups, and all game types share the core CWT config group.
The data in these config groups first comes from the CWT rule files in specific directories,
after merging and computing, it will be used to implement various features of the plugin.

The following types of CWT config groups are supported:

* Built-in CWT config groups - their CWT config files are located in the 'config/${gameType}' directory in the plugin jar. Always enabled.
* Project local config groups - their CWT config files should be placed in the '.config/${gameType}' directory in the project root directory. Require to be manually confirmed and imported.

When the corresponding change happens, the refresh button will be appeared in the context float toolbar in the upper right corner of the editor.
Click to confirm and import the changed CWT config group(s).

Note that the CWT config file use the LIOS overwritten strategy by the file path (relative to the root directory of the CWT config group) and the name and type of the config.
For example, if you have written some custom configs in the config file `.config/stellaris/modifiers.cwt` (which is in the project root directory), it will completely override the built-in modifier-related rules,
since the built-in modifier-related configs are located in the config file `config/stellaris/modifiers.cwt` (which is in the plugin jar) , both of their paths are `modifiers.cwt`.

Reference Links:

* [Guidance Documentation](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
* [GitHub Repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config)
