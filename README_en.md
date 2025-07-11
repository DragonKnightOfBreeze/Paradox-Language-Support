# Paradox Language Support

## Summary

[中文文档](README.md) | [English Documentation](README_en.md)

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Reference Documentation](https://windea.icu/Paradox-Language-Support) |
[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) |
[Discord](https://discord.gg/vBpbET2bXT)

The Intellij IDEA plugin for Stellaris modding (also supports other Paradox Interactive games), which is smart, convenient and with more potential.

Features:

* Supports script & localisation languages and CWT language (used by config files).
* Provides various almost excellent language features for script & localisation languages, including code highlight, code navigation, code completion, code inspection, code refactoring, quick documentation, inlay hints, live templates, code hierarchy, diagram, diff and more.
* Provides basic language features for CWT language, including code highlight, code navigation, quick documentation and more.
* Supports previewing and rendering DDS and TGA pictures, and provides actions to convert image formats (PNG, DDS, TGA).
* Supports rendering scope context, localisation text, images and other useful information via quick documentation and inlay hints.
* Supports most advanced features of script & localisation languages (such as parameters, scopes, inline scripts and various complex expressions).
* Supports customizing extended config files, allowing plugin to provide more perfect language features (such as code navigation, code completion, quick documentation and inlay hints).
* Automatically recognizes game directories and mod directories.

This plugin has implemented various advanced language features based on [config groups](https://windea.icu/Paradox-Language-Support/en/config.html#config-group), which consists of CWT config files.
The latest-version config files are already built into this plugin, make it works right out of the box.
Besides, [customizing](https://windea.icu/Paradox-Language-Support/en/config.html#writing-cwt-config-files) and [importing](https://windea.icu/Paradox-Language-Support/en/config.html#importing-cwt-config-files) config files are also supported.

If [Translation](https://github.com/YiiGuxing/TranslationPlugin) is also installed, this plugin can provide some [additional features](https://windea.icu/Paradox-Language-Support/zh/plugin-integration.html).

![](docs/images/preview_1_en.png)

## Quick Start

Usages:

* Open your mod root directory through the IDE. (It needs to directly contain the mod descriptor file `descriptor.mod`)
* Open the mod descriptor file, click the mod settings button in the floating toolbar at the top right of the editor.
* Configure the game type, game directory, and additional mod dependencies of the mod.
* Click the OK button to complete the configuration, and then wait for the IDE to complete indexing.
* Start your mod programming journey.

Tips:

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
* To change the settings of mod types, game directories, mod dependencies, etc., open the mod settings dialog using one of the following methods:
  * Click `Settings > Languages & Frameworks > Paradox Language Support` to configure default game directories.
  * Click the blue gear icon in the editor floating toolbar located in the upper right corner of the page.
  * Open the context menu and click `Paradox Language Support > Open Mod Settings...` in the editor.
  * Click `Tools > Paradox Language Support > Open Mod Settings...`.
* To change the global settings of the plugin, refer to the following method:
  * Click `Settings > Languages & Frameworks > Paradox Language Support` to open the settings page for the plugin.
* If you encounter some unexpected problems during use, try the following:
  * Update the IDE and plugin to the latest version.
  * If it may be related to IDE indices, try to rebuild indices and restart the IDE. (Click `File -> Invalidate Caches... -> Invalidate and Restart`)
  * If it may be related to plugin's built-in configs, try to [write custom config files](https://windea.icu/Paradox-Language-Support/en/config.html#writing-cwt-config-files).
  * If it may be related to plugin configuration, try to delete the plugin configuration file. (`paradox-language-support.xml`. If you don't know the detailed location, use [Everything](https://www.voidtools.com))
  * Send feedback via GitHub, Discord, etc.

Known Issues:

* Support for some of Stellaris' black-magic-like language features is not perfect.
* Support for games exclude Stellaris is not yet perfect.

## References

Reference manuals:

* [Kotlin Docs | Kotlin Documentation](https://kotlinlang.org/docs/home.html)
* [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)


Plugins:

* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Tools:

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [bcssov/IronyModManager](https://github.com/bcssov/IronyModManager)
* [amtep/ck3-tiger](https://github.com/amtep/ck3-tiger)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)

Wikis:

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)

## Contribution

You can support and contribute this project by the following ways:

* Star the project on GitHub
* Send feedbacks (via [Discord](https://discord.gg/vBpbET2bXT), or via sending issues on GitHub directly)
* Submit PRs (to plugin repository aka this project, or [these config repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md))
* Share this plugin with your friends, or in related communities
* If you like this plugin, you can also consider donating via [afdian](https://afdian.com/a/dk_breeze)

Besides, if you are willing to submit PRs, and have any questions about the plugin code and the config files, feel free to ask via email and [Discord](https://discord.gg/vBpbET2bXT).