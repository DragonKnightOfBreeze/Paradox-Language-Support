# Quick Start

## Usages

* Open your mod root directory through the IDE. (It needs to directly contain the mod descriptor file `descriptor.mod`)
* Open the mod descriptor file, click the mod settings button in the floating toolbar at the top right of the editor.
* Configure the game type, game directory, and additional mod dependencies of the mod.
* Click the OK button to complete the configuration, and then wait for the IDE to complete indexing.
* Start your mod programming journey.

## Tips

* To perform a global search, please refer to the following methods:
    * Click `Ctrl Shift R` or `Ctrl Shift F` to search within the current project, directory, or specified scope.
    * Click `Shift Shift` to find files, definitions, scripted variables, and other symbols.
* To perform a code navigation, please refer to the following methods:
    * Hold down `Ctrl` and click on the target location, to navigate to the declarations or usages of the target.
    * Hold down `Ctrl Shift` and click on the target location, to navigate to the type declarations of the target.
    * Hold down `Alt` and click on the target location, to navigate to the related CWT config declarations of the target.
    * Hold down `Shift Alt` and click on the target location, to navigate to the related localisation declarations of the target definition.
    * Hold down `Ctrl Shift Alt` and click on the target location to navigate to the related image declarations of the target definition.
    * Click `Navigate` or `Go To` in the editor's right-click menu, and choose the target to navigate to.
    * Click `Navigate > Definition Hierarchy` to open the definition hierarchy window, to view the definition implementation relationship of a specific definition type.
    * Click `Navigate > Call Hierarchy` to open the call hierarchy window, to view the call relationship of definitions, localisations, scripted variables, etc.
    * Click `Alt 1` or `Project` Tool window, open the Project View panel, then click `Project > Paradox Files` in the upper left corner, to view the summarized game and mod files.
    * Click `Alt 1` or `Project` Tool window, open the Project View panel, then click `Project > CWT Config Files` in the upper left corner, to view the summarized CWT config files.
* To run a global code inspection, refer to the following method:
    * Click `Alt 6` or `Problems` Tool window, open the Problems panel ,then view problems of current file, or run a global code inspection for whole project.
    * Click `Code > Inspect Code...`, run a global code inspection for whole project.
    * When code inspection is finished ,IDE will show detail inspection result in Problems panel.
* To change the global configuration of the plugin, refer to the following method:
    * Click `Settings > Languages & Frameworks > Paradox Language Support` to open the settings page for the plugin.
* To change the configuration of mod types, game directories, mod dependencies, etc., open the Mod Configuration dialog using one of the following methods:
    * Click `Settings > Languages & Frameworks > Paradox Language Support` to configure default game directories.
    * Click the blue gear icon in the editor floating toolbar located in the upper right corner of the page.
    * In the editor, open the context menu and click `Paradox Language Support > Open Mod Settings...`.
    * Click `Tools > Paradox Language Support > Open Mod Settings...`.
* If you encounter some unexpected problems during use, try the following:
  * Update the IDE and plugin to the latest version.
  * If it may be related to IDE indices, try to rebuild indices and restart the IDE. (Click `File -> Invalidate Caches... -> Invalidate and Restart`)
  * If it ma ybe related to plugin's built-in configs, try to [write custom config files](https://windea.icu/Paradox-Language-Support/#/en/config.md#writing-cwt-config-files).
  * If it may be related to plugin configuration, try to delete the plugin configuration file. (`paradox-language-support.xml`. If you don't know the detailed location, use [Everything](https://www.voidtools.com))
  * Send feedback via GitHub, Discord, etc.

## Known Issues

* Support for games exclude Stellaris is not yet perfect.
