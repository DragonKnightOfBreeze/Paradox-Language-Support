# Config Related

<!-- TODO Manual improvement and polish -->

## Config Group {#config-group}

### Summary {#config-group-summary}

PLS implements various advanced language features based on config groups composed of CWT config files.

Config groups can come from different sources. For the same source, there are config groups for each game type, and the shared config group for all game types.

You can enable or disable each type of config group on the plugin settings page (`Paradox Language Support > Config Related`).
The parent directories for these config groups, as well as the repository URLs for remote config groups, can also be configured there.

Reference links:

- [Repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/cwt)

### Sources {#config-group-sources}

#### Built-in Config Groups {#config-group-builtin}

- Location: `config/{gameType}`
- Packaged inside the plugin jar (within the plugin zip).
- `{gameType}` is the game type id[^1]; for the shared config group, it is `core`.
- The shared built-in config group is always enabled.

#### Remote Config Groups {#config-group-remote}

- Location: `{configDir}/{dirName}`
- `{configDir}` is the parent directory that contains all remote config group directories and can be customized in the plugin settings.
- `{dirName}` is the repository directory name; for the shared config group, it is `core`.

After you change the settings, PLS will automatically clone and pull these config groups from the configured remote repositories.

#### Local Config Groups {#config-group-local}

- Location: `{configDir}/{gameType}`
- `{configDir}` is the parent directory that contains all global local config group directories and can be customized in the plugin settings.
- `{gameType}` is the game type id[^1]; for the shared config group, it is `core`.

The config files inside are user-defined and apply to all projects. Changes require manual import.

#### Project Local Config Groups {#config-group-project-local}

- Location: `{configDirName}/{gameType}`
- `{configDirName}` is the name of the directory for project local config groups, located directly under the project root (default is `.config`), and can be customized in the plugin settings.
- `{gameType}` is the game type id[^1]; for the shared config group, it is `core`.

The config files inside are user-defined and only apply to the current project. Changes require manual import.

### Override Strategy {#config-group-override}

Configs are overridden in a later-wins manner based on file path and config ID.

When reading configs, the plugin iterates config groups in this order: built-in config groups, local config groups, and project local config groups. The shared config group is shared by all game types and is iterated before the config group for the specific game type.

For example, if you write custom configs in the file `.config/stellaris/modifiers.cwt` (under the project root), they will completely override the built-in modifier configs.
Because the built-in modifier configs are located in `config/stellaris/modifiers.cwt` (inside the plugin JAR), and both have the same file path `modifiers.cwt`.
If this custom config file is empty, after applied, the plugin will be unable to resolve any modifiers in script files.

## CWT Config File {#config-file}

### Summary {#config-file-summary}

CWT config files use a dedicated file format that can be considered a variant of the Paradox script language. The file extension is `.cwt`.

### Syntax {#config-file-syntax}

The basic syntax of a CWT config file is as follows:

```cwt
# both equal sign (`=`, `==`) and not equal sign (`<>`, `!=`) can be used as the k-v separator (also available in options)
# options and values can be mixed in option clauses (`{...}`)
# properties and values can be mixed in clauses (`{...}`)

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

For more detailed syntax references, see:

- [Appendix: Syntax Reference](ref-syntax.md), the [corresponding section](ref-syntax.md#cwt)

## Customization {#customization}

### Writing CWT Config Files {#write-config-files}

For more detailed writing specifications for each CWT config, you can currently refer to:

- Built-in config files of the plugin. They are located under the `cwt/core` directory in the plugin repository and in the individual remote repositories.
- [Appendix: Config Format Reference](ref-config-format.md)
- CWTools [Guidance](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### Importing CWT Config Files {#import-config-files}

You can choose to enable [remote config groups](#config-group-remote), whose config files come from various remote repositories.
Alternatively, you can enable [local config groups](#config-group-local) or [project local config groups](#config-group-project-local) and write custom config files under the corresponding directories.
These files can be used to enhance the built-in configs or extend plugin functionality.

When changes are detected, a refresh button will appear in the floating toolbar in the top-right corner of the editor.
After you confirm the import, the changes from your custom config files will be applied.
The IDE will then reparse open files in the background.

Note: If changes to the config files affect indexing logic (e.g., adding a new definition type, changing a match condition for a definition type), you may need to reindex the entire project (which may take several minutes) to ensure the plugin works properly in situations involving these changes.

[^1]: Available game type ids: `stellaris`, `ck2`, `ck3`, `eu4`, `eu5`, `hoi4`, `ir`, `vic2`, `vic3`. For the shared config group, it is `core`.
