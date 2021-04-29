# Summary

[中文文档](README.md) | [English Document](README_en.md)

Support for Paradox Language.

This plugin is under developing, so some complex functions may not be implemented yet.
If you want more perfect language support, please consider using **VSCode** with **CWTools** plugin.

Create the descriptor file **descriptor.mod** in the root directory of your Mod to provide language support.
If you want to add game directory and third party mod as dependencies, just add them as libraries to the project module of your mod, like what Java and Kotlin does.

Currently Only Support **Stellaris**.
Currently the rule file to implement functions such as validate and provide code completion is not perfect yet.
(Plan to use the same rule files with **CWTools** later.)

The [reference documents](https://dragonknightofbreeze.github.io/paradox-language-support) will be provided in the future.

If you have found any bugs or problems, feel free to report them in project's [Github Issue Page](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues).

Welcome to have a look at [this project](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) and [my other projects](https://github.com/DragonKnightOfBreeze?tab=repositories).

# Reference

About this plugin

* Plugin Information: [plugin.xml](src/main/resources/META-INF/plugin.xml)
* Change Log: [CHANGELOG.md](CHANGELOG.md) (Written in Simple Chinese)

Related links

* [cwtools](https://github.com/cwtools/cwtools)
* [cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [cwtools-stellaris-config](https://github.com/cwtools/cwtools-stellaris-config)

