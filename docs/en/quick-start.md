# Quick Start

<-- TODO: updating -->

### Installation

- Using the IDE built-in plugin system: `Settings/Preferences` > `Plugins` > `Marketplace` > Search for "Paradox Language Support" > `Install`
- Using JetBrains Marketplace: Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) and install it by clicking the `Install to ...` button.
- Manual Installation: Download the [latest release](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases/latest) and install it manually (No need to unzip): `Settings/Preferences` > `Plugins` > `⚙️` > `Install plugin from disk...`

### Usage Steps

- Open your mod's root directory in the IDE.
- Open the mod descriptor file (`descriptor.mod`, or `.metadata/metadata.json` for VIC3 and EU5).
- Click the mod settings button in the floating toolbar at the top right of the editor.
- Configure the mod's game type, game directory, and required mod dependencies.
- Confirm the configuration and wait for the IDE to finish project analysis.
- Start your mod development journey.

### Practical Tips

- **Global Search**:
  - Use `Ctrl + Shift + R` or `Ctrl + Shift + F` to search within the current project, directory, or a specified scope.
  - Use `Shift + Shift` (Search Everywhere) to quickly find files, definitions, scripted variables, and other symbols.
- **Code Navigation**:
  - Use `Ctrl + Click` to jump to the declaration or usage of a target.
  - Use `Ctrl + Shift + Click` to jump to the type declaration of a target.
  - Use `Alt + Click` to jump to the declaration of the related configs for a target.
  - Use `Shift + Alt + Click` to jump to the declaration of the related localisations for a target.
  - Use `Ctrl + Shift + Alt + Click` to jump to the declaration of the related images for a target.
  - Use the `Navigate` menu (or the `Go To` option in the editor's right-click menu) for quick navigation.
  - Use `Navigate > Definition Hierarchy` to open the type hierarchy window and view definitions of specific types.
  - Use `Navigate > Call Hierarchy` to open the call hierarchy window and view the call relationships of definitions, localisations, scripted variables, etc.
  - Select the `Paradox Files` view in the project panel to browse aggregated game and mod files.
  - Select the `CWT Config Files` view in the project panel to browse aggregated config files.
- **Code Inspection**:
  - View issues in the current file within the Problems panel.
  - Use `Code > Inspect Code…` to perform a global code inspection and view the detailed report in the Problems panel upon completion.
- **Modifying Settings**:
  - Access the plugin's global settings page via:
    - `Settings > Languages & Frameworks > Paradox Language Support`
  - Open the mod settings dialog via:
    - Clicking the blue gear icon in the editor's top-right floating toolbar.
    - Selecting `Paradox Language Support > Open Mod Settings...` from the editor's right-click menu.
    - Selecting `Tools > Paradox Language Support > Open Mod Settings...` from the main menu.
  - Modify preferred locale, default game type, default game directory and other functional details in the global settings.
  - Adjust game directory, mod dependencies and other configurations in the mod settings.

### Troubleshooting

- Ensure both the IDE and the plugin are updated to the latest versions.
- If the issue might be index-related, try to [invalidate caches and restart the IDE](https://www.jetbrains.com/help/idea/invalidate-caches.html).
- If the issue might be config-related, try to [write custom config files](config.md#write-config-files).
- If the issue might be plugin configuration-related, try deleting the plugin's configuration file (`paradox-language-support.xml`, recommended to locate using [Everything](https://www.voidtools.com)).
- Feedback is welcome through GitHub, Discord and other channels.