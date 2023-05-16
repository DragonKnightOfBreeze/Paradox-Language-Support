# Paradox Language Support

## Summary

[中文文档](README.md) | [English Documentation](README_en.md)

[中文参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) | [English Reference Documentation](https://windea.icu/Paradox-Language-Support/#/en/)

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)

IDEA plugin: Paradox language support.

Features:

* Supports script language (mainly `*.txt` files) and localization language (`*.yml` files),  
  providing many language features such as syntax parsing, reference resolution, quick documentation, code navigation, code checking, code completion, inline hints, live templates, etc.  
  It also provides quite comprehensive support for most advanced features of script language, such as parameters, scopes, inline scripts and various complex expressions.
* Supports CWT language (`*.cwt` files), providing basic language features.
* Supports viewing DDS images directly in the IDE, providing some useful editor features including converting to PNG images.
* Automatically recognizes the game directory (containing the launcher configuration file `launcher-settings.json`) and mod directory (containing the mod descriptor file `descriptor.mod`).
* Renders related localized text and DDS images, as well as prompts some useful information including scope information and parameter information, through ways such as quick documentation and inline hints.

This plugin shares rule files (`*.cwt`) with [CWTools](https://github.com/cwtools/cwtools-vscode). These rule files are currently built into the plugin with some modifications and extensions.

If [Translation](https://github.com/YiiGuxing/TranslationPlugin) is also installed, this plugin can provide some [additional features](https://windea.icu/Paradox-Language-Support/#/zh/plugin-integration.md).

If you encounter any problems during use, feel free to provide feedback via Github.

![](https://windea.icu/Paradox-Language-Support/assets/images/preview_1_zh.png)

## Quick Start

Usage:

* Open your mod root directory through the IDE. (It needs to directly contain the mod descriptor file `descriptor.mod`)
* Open the mod descriptor file, click the mod configuration button in the floating toolbar (or editor right-click menu) at the top right of the editor, and configure the game type, game directory, and additional mod dependencies of the mod.
* Click the OK button to complete the configuration, and then wait for the IDE to complete indexing. (It will be fast)
* Start your mod programming journey!

Tips:

* If you need to change the global configuration of the plugin, please refer to the following method:
  * Click `Settings > Languages & Frameworks > Paradox Language Support` to open the plugin's configuration page.
* If you need to change the mod type, game directory, mod dependencies, etc., you can open the mod configuration dialog in one of the following ways:
  * Click the blue gear icon in the editor floating toolbar at the top right of the page.
  * Open the mail menu in the editor, click `Paradox Language Support > Open Mod Settings...`.
  * Click `Tools > Paradox Language Support > Open Mod Settings...`.
* If a script cannot be recognized, there are usually the following situations:
  * The corresponding CWT rule does not exist. (That is, there is an error in this script)
  * The corresponding CWT rule is not perfect. (Consider feedback on Github)
  * There are references that cannot be resolved. (Consider configuring the corresponding game directory or mod dependency)
* If there are problems with the IDE index or errors involving the IDE index, try to solve it in the following way:
  * Click `File -> Invalidate Caches... -> Invalidate and Restart` to rebuild the index and restart the IDE.

## FAQ

Q: Why is Intellij IDEA + this plugin, instead of VSCode + CWTools?

A: For Idea is so lovely.

## Reference

Reference manual:

* [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
* [JFlex - manual](https://www.jflex.de/manual.html)

Tools and plugins:

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)
* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Wiki:

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)