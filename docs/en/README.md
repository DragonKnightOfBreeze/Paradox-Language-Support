# Summary

Support for Paradox Language.

This plugin is under developing, some complex functions may not be implemented yet, and may cause unexpected bugs.
If you want more perfect language support, please consider using [VSCode](https://code.visualstudio.com) with [CWTools](https://github.com/cwtools/cwtools-vscode) plugin.
This plugin shares rule files (`*.cwt`) with [CWTools](https://github.com/cwtools/cwtools-vscode), but related functions has not been fully implemented yet.

Introduction:

* Create the descriptor file `descriptor.mod` in the root directory of your Mod to provide language support.
* Create the mark file `.${gameType}` in the root directory of your Mod to specify game type. (e.g. `.stellaris`)  
* Supported game types: ck2 / ck3 / eu4 / hoi4 / ir / stellaris / vic2.
* Supported paradox games: Crusader Kings II / Crusader Kings III / Europa Universalis IV / Hearts of Iron IV / Imperator: Rome / Stellaris / Victoria II.

Tip:

* If you want to add game directory and third party mod as dependencies, just add them as libraries to the project module of your mod, like what Java and Kotlin does.
* If you have met some IDE problems about indices, please try to rebuild indices. (Click `File -> Invalidate Caches... -> Invalidate and Restart`)

# Reference

About this plugin

* Plugin Information: [plugin.xml](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/src/main/resources/META-INF/plugin.xml)
* Change Log: [CHANGELOG.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/CHANGELOG.md) (Written in Simple Chinese)

Related links

* [cwtools](https://github.com/cwtools/cwtools)
* [cwtools vscode](https://github.com/cwtools/cwtools-vscode)
* [.cwt config file guidance](https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance)

