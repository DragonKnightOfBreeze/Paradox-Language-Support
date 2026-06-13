# Introduce

<!-- With the guidebook currently being written, we are fully prepared — welcome to the real world. -->

[GitHub](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support) |
[Plugin Home](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) |
[Discord](https://discord.gg/vBpbET2bXT) |
[By me a coffee ☕](https://afdian.com/a/dk_breeze)

## Overview

Paradox Language Support is the IntelliJ IDEA plugin designed for Paradox game mod developers, which provides an intelligent, efficient and feature-rich development experience to help you easily achieve creativity.

The plugin automatically detects game directories and mod directories, analyzes the files within to build caches and indexes.  
After completing the necessary configuration (such as game type, game directory, mod dependencies) and project analysis (scanning files, building indexes), you can enable and experience the full range of language features.

The plugin implements core language features based on its own [config system](config.md).  
The CWT config files it uses follow a syntax and format largely consistent with [CWTools](https://github.com/cwtools/cwtools), with certain improvements and extensions.  
The plugin comes with the latest version of built-in configs, ready to use out of the box, while also supporting [customization](config.md#write-config-files) and [importing](config.md#import-config-files) of config files to meet personalized development needs.

**Core Features**:

- **Multi-language support**: Supports the script language, localisation language, and CSV language used for mod development, as well as the CWT language for writing configs.
- **Advanced language construct support**: Supports parameters, scopes, complex expressions, inline scripts, definition injection, and other advanced language constructs.
- **Rich language features**: Supports code highlighting, code navigation, code completion, code refactoring, intention actions, code inspections, quick documentation, inlay hints, code hierarchy, diff viewing, diagrams, and many other language features.
- **Flexible code navigation**: Quickly navigate to various targets, including files, definitions, localisations, related localisations, related images, related configs, etc., through navigation actions, quick documentation, inlay hints, code hierarchy, search everywhere, and more.
- **Enhanced information hints**: Intuitively show key information such as localisation text, images, scopes, parameters, etc., through quick documentation, inlay hints, and more.
- **Extensible config system**: Supports customizing and importing config files to enhance features like code navigation, code completion, and documentation hints.
- **Image processing**: Supports previewing and rendering DDS and TGA images, and can convert between different image formats (PNG, DDS, TGA).
- **Tool integration**: Integrates useful tools such as [Image Magick](https://www.imagemagick.org), [Translation Plugin](https://github.com/yiiguxing/TranslationPlugin), [Tiger](https://github.com/amtep/tiger), etc., to boost development efficiency.
- **AI assistance**: Initial integration of AI technology for translating and polishing localisation text.

![](../assets/preview_1_en.png)

![](../assets/preview_1_zh.png)

## Known Limitations

- The plugin's support for some complex language constructs of script files and localisation files is not yet complete, and is still being improved. Feedback is welcome.
- The quality of config-driven features (such as code completion, code inspection and quick documentation) depends on the completeness and up-to-dateness of the CWT config files for each game.
  Currently, configs are actively maintained for **Stellaris**, **Victoria 3** and **Europa Universalis V**.
  Configs for other games (such as Crusader Kings III and Hearts of Iron IV) may be outdated, which can result in false warnings or missing completions.
  Contributions to the [config repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md) are very welcome.
- Some advanced or uncommon scripting patterns (e.g. definitions declared within inline scripts, complex localisation commands) are not yet fully supported, and are being improved progressively.

## Technical Details

- Built on the IntelliJ Platform SDK, developed with Kotlin, and utilizes [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) (rather than [LSP](https://microsoft.github.io/language-server-protocol)) for deep language parsing and manipulation.
- Uses BNF for grammar parsing and JFlex for lexical analysis.
- Employs an extension point mechanism for dynamic functional expansion, facilitating customization and enhancement of plugin behavior by both the plugin itself and mod developers.
- Includes a built-in custom code injectors for achieving IDE features that cannot be accomplished by conventional means.
- Integrates image processing, translation and lint tools to optimize and extend plugin capabilities.
- Preliminary integration of AI technology for translating and polishing localisation text.

## Contribution & Support

We welcome all forms of contribution and support, including but not limited to:

- ⭐ Star the project on GitHub.
- 🐛 Submit feedback and issues (via [Discord][url:discord] or [GitHub Issues][url:issues]).
- 🔧 Submit Pull Requests (to the [plugin repository][url:github] (this project), or to the [config repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md)).
- 📢 Recommend this plugin to friends or in the community.
- 💝 Sponsor the project via [Afdian][url:afdian].

If you are interested in submitting a PR, but have any questions about plugin development or config writing, feel free to contact us via [email][mailto] or [Discord][url:discord].

**Contributing to Config Files**:

Given the plugin's config-driven architecture, contributing to the CWT config files is one of the most impactful ways to improve support for a specific game.
Config files define the semantics (definitions, modifiers, scopes, effects, triggers, etc.) that power code completion, code inspection and documentation — keeping them up to date directly improves the experience for all users of that game.

See the [config repositories](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md) for links, and the [config documentation](config.md) for guidance on the config format.

For a list of current maintainers and contributors, see [CONTRIBUTORS.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/CONTRIBUTORS.md).

## Reference Links

### Official Documentation

- [Kotlin Docs | Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Getting started | IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/getting-started.html)
- [IntelliJ Platform SDK | IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [LangChain4j | LangChain4j](https://docs.langchain4j.dev/)

### Tools & Plugins

- [cwtools/cwtools: A library for parsing, editing, and validating Paradox Interactive script files.](https://github.com/cwtools/cwtools)
- [cwtools/cwtools-vscode: A VS Code extension providing language server support for paradox script files using cwtools](https://github.com/cwtools/cwtools-vscode)
- [bcssov/IronyModManager: Mod Manager for Paradox Games. Official Discord: https://discord.gg/t9JmY8KFrV](https://github.com/bcssov/IronyModManager)
- [amtep/tiger: Checks game mod files for common mistakes and warns about them. Supports Crusader Kings 3, Victoria 3, and Imperator: Rome.](https://github.com/amtep/tiger)
- [nickbabcock/jomini: Parses Paradox files into javascript objects](https://github.com/nickbabcock/jomini)
- [OldEnt/stellaris-triggers-modifiers-effects-list: List of Stellaris triggers, modifiers and effects for most game versions since launch.](https://github.com/OldEnt/stellaris-triggers-modifiers-effects-list)
- [Victoria-3-Modding-Co-op/Modding-Digests: This repository contains modding digests for Victoria 3 updates.](https://github.com/Victoria-3-Modding-Co-op/Modding-Digests)

### Tutorials & Wiki

- [Stellaris Wiki](https://stellaris.paradoxwikis.com/Stellaris_Wiki)
- [群星中文维基 | Stellaris 攻略资料指南 - 灰机wiki](https://qunxing.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5) (Stellaris Chinese Wiki)
- [Stellaris Mod 教程](https://main--pdxdoc-next.netlify.app) (Stellaris Mod Tutorial, written in Simplified Chinese)

[url:github]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support
[url:issues]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues
[url:release-latest]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases/latest
[url:plugin-homepage]: https://plugins.jetbrains.com/plugin/16825-paradox-language-support
[url:discord]: https://discord.gg/vBpbET2bXT
[url:qq-group]: https://qm.qq.com/q/oRPgLwrTZm
[url:afdian]: https://afdian.com/a/dk_breeze

[mailto]: mailto:dk_breeze@qq.com