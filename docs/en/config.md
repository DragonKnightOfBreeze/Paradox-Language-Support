# Config Related

## Config Group {#config-group}

### Summary {#config-group-summary}

PLS implements various advanced language features based on config groups, which consists of many CWT config files.

Config groups can have different sources. For config groups from the same source, there are config groups for different game types, and the core config group, which is shared by all game types.

You can enable or disable each type of config group as needed in the plugin settings page (`Paradox Language Support > Config Related`).
The parent directories for these config groups, as well as the repository URLs for remote config groups, can also be configured directly from the plugin settings page.

Reference Links:

- [Repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/cwt)

### Sources {#config-group-sources}

#### Built-in Config Groups {#config-group-builtin}

- Location: `config/{gameType}`
- Located inside the plugin jar, which is packaged within the plugin zip.
- `{gameType}` is the [Game Type ID](#game-type-id); for the shared config group, it is `core`.
- The shared built-in config group is always enabled.

#### Remote Config Groups {##config-group-remote}

- Location: `{configDir}/{dirName}`
- `{configDir}` is the parent directory containing all remote config group directories, and can be customized in the plugin settings.
- `{dirName}` is the repository directory name; for the shared config group, it is `core`.
- After modifying the configuration, PLS will automatically clone and pull these config groups from the configured remote repositories.

#### Local Config Groups {##config-group-local}

- Location: `{configDir}/{gameType}`
- `{configDir}` is the parent directory containing all global local config group directories, and can be customized in the plugin settings.
- `{gameType}` is the [Game Type ID](#game-type-id); for the shared config group, it is `core`.
- The config files inside are user-defined and apply to all projects. Changes require manual import.

#### Project Local Config Groups {##config-group-project-local}

- Location: `{configDirName}/{gameType}`
- `{configDirName}` is the name of the directory for project local config groups, located directly under the project root (default is `.config`), and can be customized in the plugin settings.
- `{gameType}` is the [Game Type ID](#game-type-id); for the shared config group, it is `core`.
- The config files inside are user-defined and only apply to the current project. Changes require manual import.

### Overridden Strategy {#config-group-overridden}

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

Here is the list of available game type ids: `stellaris`, `ck2`, `ck3`, `eu4`, `eu5`, `hoi4`, `ir`, `vic2`, `vic3`. (for shared config group, it is `core`)

## CWT Config File {#config-file}

### Summary {#config-file-summary}

CWT config file use its own file format, which can be considered as a variant of paradox script language.
Its file extension is `.cwt`.

### Syntax {#config-file-syntax}

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

## Customization {#customization}

### Writing CWT Config Files {#write-config-files}

For more detailed writing specifications for each CWT config,
currently you can refer to the writing style of the built-in config files.

Reference Links:

- [Guidance](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)

### Importing CWT Config Files {#import-config-files}

You can create custom config files in the directories of [remote config groups](##config-group-remote), [local config groups](##config-group-local), or [project local config groups](##config-group-project-local).  
These files allow you to enhance or override the plugin's built-in configs, or to extend plugin functionality.

When changes are detected, a refresh button will appear in the context toolbar in the top-right corner of the editor.  
Click it to confirm the import and apply the changes from your custom config files.
Then, the IDE will then reparse open files in the background.

Note: If your changes affect the indexing logic (e.g., adding a new definition type or changing a match condition),
you may need to reindex the entire project (which may take several minutes) to ensure the plugin works properly,
if in the situation that involves these changes.
