# Quick Start

## Usage Steps

1. Open your mod's root directory in the IDE.
2. Open the mod descriptor file (`descriptor.mod`, or `.metadata/metadata.json` for VIC3).
3. Click the *Mod Settings* button in the floating toolbar at the top right of the editor.
4. Configure the mod's game type, game directory, and required mod dependencies.
5. Confirm the configuration and wait for the IDE to finish indexing.
6. Begin your mod development journey!

## Practical Tips

- **Global Search**:
  - Use `Ctrl + Shift + R` or `Ctrl + Shift + F` to search within the current project, directory, or a specified scope.
  - Use `Shift + Shift` (Search Everywhere) to quickly find files, definitions, scripted variables, and other symbols.
- **Code Navigation**:
  - Use `Ctrl + Click` to jump to the declaration or usage of a target.
  - Use `Ctrl + Shift + Click` to jump to the type declaration of a target.
  - Use `Alt + Click` to jump to the declaration of the relevant config for a target.
  - Use `Shift + Alt + Click` to jump to the declaration of the relevant localization for a target.
  - Use `Ctrl + Shift + Alt + Click` to jump to the declaration of the relevant image for a target.
  - Use the `Navigate` menu (or the `Go To` option in the editor's right-click menu) for quick navigation.
  - Use `Navigate > Definition Hierarchy` to open the type hierarchy window and view definitions of specific types.
  - Use `Navigate > Call Hierarchy` to open the call hierarchy window and view the call relationships of definitions, localizations, scripted variables, etc.
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
  - Modify default language environment, default game type, default game directory, and other functional details in the global settings.
  - Adjust game directory, mod dependencies, and other configurations in the mod settings.
- **Troubleshooting**:
  - Ensure both the IDE and the plugin are updated to the latest versions.
  - If the issue might be index-related, try to [invalidate caches and restart the IDE](https://www.jetbrains.com/help/idea/invalidate-caches.html).
  - If the issue might be config-related, try to [write custom config files](https://windea.icu/Paradox-Language-Support/en/config.html#write-cwt-config-files).
  - If the issue might be plugin configuration-related, try deleting the plugin's configuration file (`paradox-language-support.xml`, recommended to locate using the [Everything](https://www.voidtools.com) tool).
  - Feedback is welcome through GitHub, Discord, and other channels.

## Known Limitations

- Support for some complex language features in Stellaris is still being improved.
- Support for unique language features in non-Stellaris games is not yet complete; feedback and contributions are welcome.
- Currently, only Stellaris and Victoria 3 have relatively comprehensive built-in config files; Pull Requests are welcome.
