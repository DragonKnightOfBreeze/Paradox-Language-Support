# Paradox Language Support

## Summary

[中文文档](README.md) | [English Documentation](README_en.md)

[中文参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) | [English Reference Documentation](https://windea.icu/Paradox-Language-Support/#/en/)

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)

IDEA Plugin: Support for Paradox Language.

Support Paradox script language (mainly `*.txt` files) and localisation language (`*.yml` files),
provide features such as syntax parsing, reference resolving, quick documentation, code navigation, code inspection,
inlay hints, live template,
localisation text rendering and DDS image rendering.

Support cwt language (`*.cwt` files),
provide features such as syntax parsing, reference resolving, quick documentation and code navigation.

Support DDS image viewing in IDE, and provide necessary editor features, as common image support by IDE.

This plugin automatically recognizes script files and localization files in game directories (containing the launcher configuration file `launcher-settings.json`) and mod directories (containing the mod descriptor file `description.mod`).

This plugin shares rule files (`*.cwt`) with [CWTools](https://github.com/cwtools/cwtools-vscode).
These rule files are build-in provided by this plugin, with some modifications and extensions.

By game / mod settings dialog opened from editor's floating toolbar or popup menu,
you can change game type, game directory and mod dependencies of that game / mod.

By plugin's settings page `Settings > Languages & Frameworks > Paradox Language Support`
and some IDE's general settings page,
you can change some global settings.

By also install [Translation](https://github.com/YiiGuxing/TranslationPlugin),
this plugin could provide some [additional features](https://windea.icu/Paradox-Language-Support/#/end/plugin-integration.md)。

This plugin is under development, if you encounter any problem during use, feel free to send feedback on Github.

For features and usages of this plugin, please refer to the [Reference documentation](https://windea.icu/Paradox-Language-Support/#/en/) (To be written in detail).

![](https://windea.icu/Paradox-Language-Support/assets/images/preview_1_en.png)

# Quick Start

* Open the root directory of your mod by IDE. (Should contain the mod descriptor file `descriptor.mod` directly)
* Open the mod descriptor file, click the mod settings button in the editor's floating toolbar (or the editor's popup menu),
  configure game type, game directory and additional mod dependencies of your mod.
* Click OK to apply changed settings and then wait for IDE's indexing to complete. (Get well soon)
* Start your mod programming journey.

Tips:

* If a script snippet cannot be recognized, generally there are following situations:
  * Corresponding CWT rule does not exist. (Indicates that this script snippet contains errors)
  * Corresponding CWT rule is not perfect. (Consider sending fallback to me on Github)
  * There are some unresolved references. (Consider configuring related game directory or mod dependencies)
* If there is a problem with IDE's index or an error occurs involving IDE's index, please try to rebuild index.
  (Click `File -> Invalidate Caches... -> Invalidate and Restart`)

## FAQ

Q: Why is Intellij IDEA + this plugin, instead of VSCode + CWTools?

A: Because Idea is very lovely, and I'm her fervent fan.

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