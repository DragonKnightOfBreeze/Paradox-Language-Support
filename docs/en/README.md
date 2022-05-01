# Summary

IDEA Plugin: Support for Paradox Language.

Provider syntax parsing, code validation, code completion, navigation, documentation, inlay hints,
localisation text rendering, DDS image rendering and many other functions for
paradox script language (mainly `*.txt` files) and paradox localisation language (`*.yml` files).
Also provider such functions for cwt language (`*.cwt` files),
and some convenient functions for DDS image files.

Enable language support by create the descriptor file `descriptor.mod` in the root directory of your Mod,
and can import game directory and third party mod as dependencies
by adding related libraries to the module of your Mod in the `Project Structure` page.

This plugin is under developing, some complex functions may not be implemented yet, and may cause unexpected bugs.
If you want more perfect script language support, please consider using [VSCode](https://code.visualstudio.com) with [CWTools](https://github.com/cwtools/cwtools-vscode) plugin.
This plugin shares rule files (`*.cwt`) with [CWTools](https://github.com/cwtools/cwtools-vscode), but related functions has not been fully implemented yet.

Introduction:

* Create the descriptor file `descriptor.mod` in the root directory of your Mod to provide language support.
* Create the mark file `.${gameType}` in the root directory of your Mod to specify game type. (e.g. `.stellaris`)  
* Supported game types: ck2 / ck3 / eu4 / hoi4 / ir / stellaris / vic2.
* Supported paradox games: Crusader Kings II / Crusader Kings III / Europa Universalis IV / Hearts of Iron IV / Imperator: Rome / Stellaris / Victoria II.

Tips:

* If you want to add game directory and third party mod as dependencies, just add them as libraries to the project module of your mod, like what Java and Kotlin does.
* If you have met some IDE problems about indices, please try to rebuild indices. (Click `File -> Invalidate Caches... -> Invalidate and Restart`)

# Reference

Related links：

* [cwtools/cwtools: A library for parsing, editing, and validating Paradox Interactive script files.](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode: A VS Code extension providing language server support for paradox script files using cwtools](https://github.com/cwtools/cwtools-vscode)
* [vincentzhang96/DDS4J: Java library for reading and decoding DDS files to raw ARGB and PNG](https://github.com/vincentzhang96/DDS4J)

