# Introduce

## Summary

[Github](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)

[Plugin Marketplace Page](https://plugins.jetbrains.com/plugin/16825-paradox-language-support)

IDEA Plugin: Support for Paradox Language.

Supports Paradox script language (mainly `*.txt` files) and localisation language (`*.yml` files),
provide functions such as syntax parsing, navigation, documentation, code validation, reference resolving,
inlay hints, localisation text rendering and DDS image rendering.

Supports cwt language (`*.cwt` files),
provide functions such as syntax parsing, navigation and documentation.

Supports DDS image viewing in IDE, and provide necessary editor functions, as common image support by IDE.

By creating the descriptor file `descriptor.mod` in the root directory of your Mod,
can recognize inside matched files as script files or localization files.

By adding a game directory or third party mod directory as a library to the project or module of your mod
in the `Project Structure` page, can import it as a dependency of your mod.

This plugin is under developing, some complex functions may not be implemented yet, and may cause unexpected bugs during use.
If you need some functions that this plugin is not implemented or not implemented perfectly,  
please consider using [VSCode](https://code.visualstudio.com) with its plugin [CWTools](https://github.com/cwtools/cwtools-vscode).

This plugin shares rule files (`*.cwt`) with [CWTools](https://github.com/cwtools/cwtools-vscode),
These rule files are build-in provided by this plugin, with some modifications and extensions,
and will be synchronized to the latest version when a new version of this plugin is published.

By also install [Translation](https://github.com/YiiGuxing/TranslationPlugin),
will provide some [additional functions](https://windea.icu/Paradox-Language-Support/#/end/plugin-integration.md)。

![](../assets/images/script_file_preview_en.png)

![](../assets/images/localisation_file_preview_en.png)

Introduction:

* Create the descriptor file `descriptor.mod` in the root directory of your Mod to provide language support.
* Create the mark file `.${gameType}` in the root directory of your Mod to specify game type. (e.g. `.stellaris`)  
* Supported game types: ck2 / ck3 / eu4 / hoi4 / ir / stellaris / vic2.
* Supported paradox games: Crusader Kings II / Crusader Kings III / Europa Universalis IV / Hearts of Iron IV / Imperator: Rome / Stellaris / Victoria II.

Tips:

* If you want to add game directory and third party mod as dependencies, just add them as libraries to the project module of your mod, like what Java and Kotlin does.
* If you have met some IDE problems about indices, please try to rebuild indices. (Click `File -> Invalidate Caches... -> Invalidate and Restart`)

## Reference

Tools and plugins:

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)
* [YiiGuxing/TranslationPlugin](https://github.com/YiiGuxing/TranslationPlugin)

Wiki:

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)
