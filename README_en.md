# Paradox Language Support

## Summary

[中文文档](README.md) | [English Documentation](README_en.md)

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Reference Documentation](https://windea.icu/Paradox-Language-Support) |
[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)

IDEA plugin: Support for Paradox language.

Features:

* Supports script language (mainly `*.txt` files) and localization language (`*.yml` files),
  providing many language features such as code highlight, code navigation, code completion, code inspection, code refactoring, quick documentation, inlay hints, live templates, code hierarchy, diagram, diff, etc.
* Supports CWT language (`*.cwt` files), providing basic language features.
* Supports viewing DDS images directly in the IDE, providing some useful editor features including converting to PNG images.
* Provides quite comprehensive support for most advanced features of script language, such as parameters, scopes, inline scripts and various complex expressions.
* Renders related type information, scope information, localisation text and DDS images through ways such as quick documentation and inlay hints.
* Automatically recognizes the game directory (containing the launcher settings file `launcher-settings.json`) and mod directory (containing the mod descriptor file `descriptor.mod`).

This plugin implements various advanced language features based on [CWT config groups](https://windea.icu/Paradox-Language-Support/#/en/core-features.md#cwt-config-group), which consists of many CWT rule files.
The latest-version config files are already built into this plugin, make it works right out of the box.
Besides, importing local config files is also supported.

If [Translation](https://github.com/YiiGuxing/TranslationPlugin) is also installed, this plugin can provide some [additional features](https://windea.icu/Paradox-Language-Support/#/zh/plugin-integration.md).

If you encounter any problems during use, feel free to provide feedback via GitHub.

![](https://windea.icu/Paradox-Language-Support/assets/images/preview_1_en.png)

## Quick Start

Usage:

* Open your mod root directory through the IDE. (It needs to directly contain the mod descriptor file `descriptor.mod`)
* Open the mod descriptor file, click the mod settings button in the floating toolbar (or editor right-click menu) at the top right of the editor, and configure the game type, game directory, and additional mod dependencies of the mod.
* Click the OK button to complete the configuration, and then wait for the IDE to complete indexing.
* Start your mod programming journey!

Tips:

* To perform a global search, please refer to the following methods:
  * Click `Ctrl Shift R` or `Ctrl Shift F` to search within the current project, directory, or specified scope.
  * Click `Shift Shift` to find files, definitions, scripted variables, and other symbols.
* To perform a code navigation, please refer to the following methods:
  * Hold down `Ctrl` and click on the target location, to navigate to the declarations or usages of the target.
  * Hold down `Ctrl Shift` and click on the target location, to navigate to the type declarations of the target.
  * Hold down `Alt` and click on the target location, to navigate to the related CWT config declarations of the target.
  * Hold down `Shift Alt` and click on the target location, to navigate to the related localization declarations of the target definition.
  * Hold down `Ctrl Shift Alt` and click on the target location to navigate to the related image declarations of the target definition.
  * Click `Navigate` or `Go To` in the editor's right-click menu, and choose the target to navigate to.
  * Click `Navigate > Definition Hierarchy` to open the definition hierarchy window, to view the definition implementation relationship of a specific definition type.
  * Click `Navigate > Call Hierarchy` to open the call hierarchy window, to view the call relationship of definitions, localizations, scripted variables, etc.
* To run a global code inspection, refer to the following method:
  * Click `Alt 6` or `Problems` Tool window, open the problems panel ,then view problems of current file, or run a global code inspection for whole project.
  * Click `Code > Inspect Code...`, run a global code inspection for whole project.
  * When code inspection is finished ,IDE will show detail inspection result in problems panel.
* To change the global configuration of the plugin, refer to the following method:
  * Click `Settings > Languages & Frameworks > Paradox Language Support` to open the settings page for the plugin.
* To change the configuration of mod types, game directories, mod dependencies, etc., open the Mod Configuration dialog using one of the following methods:
  * Click the blue gear icon in the editor floating toolbar located in the upper right corner of the page.
  * In the editor, open the context menu and click `Paradox Language Support > Open Mod Settings...`.
  * Click `Tools > Paradox Language Support > Open Mod Settings...`.
* If the IDE freezes, or its indices encounter some problems,  or it throws an error caused by the plugin, try the following:
  * Update the IDE and plugin to the latest version.
  * Delete the plugin configuration file `paradox-language-support.xml`. (If you don't known the detail file location, use [Everything](https://www.voidtools.com))
  * Rebuild indices and restart the IDE. (Click `File -> Invalidate Caches... -> Invalidate and Restart`)

Known issues:

* Support for games exclude Stellaris is not yet perfect.

## FAQ

**Why it is suggested to use Intellij IDEA + this plugin, instead of VSCode + CWTools?**

Introducing Idea, a lovely and vibrant girl, with silver-white hair and amber-colored eyes, while often wears a deep blue wind coat.
Meanwhile, she is also a powerful and intelligent mage, skilled in elemental magic, rune magic and modern programming magic.
And moreover, she has an additional identity... a dragon knight!

## Reference

Reference manual:

* [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
* [JFlex - manual](https://www.jflex.de/manual.html)

Tools and plugins:

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Wiki:

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)