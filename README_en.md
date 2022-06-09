# Summary

[中文文档](README.md) | [English Documentation](README_en.md)

[中文参考文档](https://windea.icu/Paradox-Language-Support/#/zh/) | [English Reference Documentation](https://windea.icu/Paradox-Language-Support/#/en/)

[Github](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support)

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
This plugin shares rule files (`*.cwt`) with [CWTools](https://github.com/cwtools/cwtools-vscode), with some modification and extension, but related functions has not been fully implemented yet.

For functions and usages of this plugin, please refer to the [Reference documentation](https://windea.icu/Paradox-Language-Support/#/en/) .

![](https://windea.icu/Paradox-Language-Support/assets/images/script_file_preview_en.png)

![](https://windea.icu/Paradox-Language-Support/assets/images/localisation_file_preview_en.png)

# Reference

Tools and plugins:

* [cwtools/cwtools](https://github.com/cwtools/cwtools)
* [cwtools/cwtools-vscode](https://github.com/cwtools/cwtools-vscode)
* [OldEnt/stellaris-triggers-modifiers-effects-list](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
* [vincentzhang96/DDS4J](https://github.com/vincentzhang96/DDS4J)

Wiki:

* [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
* [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5)