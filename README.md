# Paradox Language Support - The Paradox Chronicle

<!-- Here we inscribe the revelations from the old era, the truths and realities of this land. -->
<!-- Ascend to the tower’s peak and open the Book of Prophecy, for within it are recorded all our discoveries and foresights. -->
<!-- Pay no heed to the mists and riddles of the past, for we shall reveal you a radiant horizon. -->

[![中文文档][badge:doc-zh]](README_zh.md)
[![English Documentation][badge:doc-en]](README.md)
[![Reference Docs][badge:doc-ref]][url:doc-ref]
[![Discord][badge:discord]][url:discord]
<br/>
[![GitHub][badge:github]][url:github]
[![Release][badge:release]][url:release]
[![License][badge:license]][url:license]
[![Plugin Homepage][badge:plugin-homepage]][url:plugin-homepage]
[![Plugin Version][badge:plugin-version]][url:plugin-versions]
[![Plugin Downloads][badge:plugin-downloads]][url:plugin-homepage]
[![Plugin Rating][badge:plugin-rating]][url:plugin-homepage]
<br/>
[![Developed by Windea][badge:windea]][url:windea]
[![Supported by JetBrains][badge:jetbrains]][url:jetbrains]

## Overview

Paradox Language Support is the IntelliJ IDEA plugin designed for Paradox game mod developers, which provides an intelligent, efficient and feature-rich development experience to help you easily achieve creativity.

The plugin automatically detects game directories and mod directories, analyzes the files within to build caches and indexes.
After completing the necessary configuration (such as game type, game directory, mod dependencies) and project analysis (scanning files, building indexes), you can enable and experience the full range of language features.

The plugin implements core language features based on its own [config system](https://windea.icu/Paradox-Language-Support/en/config.html).
The CWT config files it uses follow a syntax and format largely consistent with [CWTools](https://github.com/cwtools/cwtools), with certain improvements and extensions.
The plugin comes with the latest version of built-in configs, ready to use out of the box, while also supporting [customization](https://windea.icu/Paradox-Language-Support/en/config.html#write-config-files) and [importing](https://windea.icu/Paradox-Language-Support/en/config.html#import-config-files) of config files to meet personalized development needs.

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

![](docs/assets/preview_1_en.png)

![](docs/assets/preview_1_zh.png)

## Getting Started

<-- TODO: updating -->

### Installation

- Using the IDE built-in plugin system: `Settings/Preferences` > `Plugins` > `Marketplace` > Search for "Paradox Language Support" > `Install`
- Using JetBrains Marketplace: Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/16825-paradox-language-support) and install it by clicking the `Install to ...` button.
- Manual Installation: Download the [latest release](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases/latest) and install it manually (No need to unzip): `Settings/Preferences` > `Plugins` > `⚙️` > `Install plugin from disk...`

### Usage Steps

- Open your mod's root directory in the IDE.
- Open the mod descriptor file (`descriptor.mod`, or `.metadata/metadata.json` for VIC3 and EU5).
- Click the mod settings button in the floating toolbar at the top right of the editor.
- Configure the mod's game type, game directory, and required mod dependencies.
- Confirm the configuration and wait for the IDE to finish indexing.
- Start your mod development journey.

### Practical Tips

- **Global Search**:
  - Use `Ctrl + Shift + R` or `Ctrl + Shift + F` to search within the current project, directory, or a specified scope.
  - Use `Shift + Shift` (Search Everywhere) to quickly find files, definitions, scripted variables, and other symbols.
- **Code Navigation**:
  - Use `Ctrl + Click` to jump to the declaration or usage of a target.
  - Use `Ctrl + Shift + Click` to jump to the type declaration of a target.
  - Use `Alt + Click` to jump to the declaration of the relevant config for a target.
  - Use `Shift + Alt + Click` to jump to the declaration of the relevant localisation for a target.
  - Use `Ctrl + Shift + Alt + Click` to jump to the declaration of the relevant image for a target.
  - Use the `Navigate` menu (or the `Go To` option in the editor's right-click menu) for quick navigation.
  - Use `Navigate > Definition Hierarchy` to open the type hierarchy window and view definitions of specific types.
  - Use `Navigate > Call Hierarchy` to open the call hierarchy window and view the call relationships of definitions, localisations, scripted variables, etc.
  - Select the `Paradox Files` view in the project panel to browse aggregated game and mod files.
  - Select the `CWT Config Files` view in the project panel to browse aggregated config files.
- **Code Inspection**:
  - View issues in the current file within the Problems panel.
  - Use `Code > Inspect Code…` to perform a global code inspection and view the detailed report in the Problems panel upon completion.
- **Modifying Settings**:
  - Access the plugin's global settings page via:
    - `Settings > Languages & Frameworks > Paradox Language Support`
  - Open the mod settings dialog via:
    - Clicking the blue gear icon in the editor's top-right floating toolbar.
    - Selecting `Paradox Language Support > Open Mod Settings...` from the editor's right-click menu.
    - Selecting `Tools > Paradox Language Support > Open Mod Settings...` from the main menu.
  - Modify preferred locale, default game type, default game directory and other functional details in the global settings.
  - Adjust game directory, mod dependencies and other configurations in the mod settings.

### Troubleshooting

- Ensure both the IDE and the plugin are updated to the latest versions.
- If the issue might be index-related, try to [invalidate caches and restart the IDE](https://www.jetbrains.com/help/idea/invalidate-caches.html).
- If the issue might be config-related, try to [write custom config files](https://windea.icu/Paradox-Language-Support/en/config.html#write-config-files).
- If the issue might be plugin configuration-related, try deleting the plugin's configuration file (`paradox-language-support.xml`, recommended to locate using [Everything](https://www.voidtools.com)).
- Feedback is welcome through GitHub, Discord and other channels.

## Technical Details

- Primarily developed using the [Kotlin](https://kotlinlang.org/) programming language.
- Built on the [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html), implementing deep semantic analysis and rich language features based on [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) (rather than [LSP](https://microsoft.github.io/language-server-protocol)).
- Uses JFlex for lexing and BNF for grammar parsing.
- Built-in rich custom extension points for flexible feature extension, making it easy to customize and enhance plugin behavior.
- Built-in code injection system for implementing functionalities, fixes, and optimizations that cannot be achieved through conventional means.
- Built-in image processing module (DDS, TGA) for previewing, rendering, and processing additional image formats.
- Built-in tool integration module (image processing, translation, inspection tools) for optimizing and extending plugin capabilities.
- Built-in AI integration module (preliminary, MVP status) for translating and polishing localisation text.

## Known Limitations

<-- TODO: updating -->

- The plugin's support for some complex language constructs of script files and localisation files is not yet complete, and is still being improved. Feedback is welcome.
- The quality of config-driven features (such as code completion, code inspection and quick documentation) depends on the completeness and up-to-dateness of the CWT config files for each game.
  Currently, configs are actively maintained for **Stellaris**, **Victoria 3** and **Europa Universalis V**.
  Configs for other games (such as Crusader Kings III and Hearts of Iron IV) may be outdated, which can result in false warnings or missing completions.
  Contributions to the [config repositories](cwt/README.md) are very welcome.
- Some advanced or uncommon scripting patterns (e.g. definitions declared within inline scripts, complex localisation commands) are not yet fully supported, and are being improved progressively.

## Contribution and Support

We welcome various forms of contribution and support, including but not limited to:

- 📢 Recommending this plugin to friends or the community.
- ⭐ Starring this project on GitHub.
- 💝 Sponsoring this project via [Afdian][url:afdian].
- 🐛 Submitting feedback and issues (via [GitHub Issues][url:issues], [Discord][url:discord], or [email][mailto]).
- 🔧 Submitting pull requests (to the [plugin repository][url:github] (this project), or the [respective config repositories](cwt/README.md)).

For a detailed guidance for contributing, please see [`CONTRIBUTING.md`](CONTRIBUTING.md).

For a detailed list of contributors, please see [`CONTRIBUTORS.md`](CONTRIBUTORS.md).

### Contributing Code

Given the plugin's extremely large codebase (~11k Kotlin production code) and complexity (platform & domain & architecture), contributing core code is highly challenging.
Nevertheless, by referencing existing documentation, code, configuration files and config files, leveraging AI assistance, and exploring on your own, it is still possible to consider and practice contributions to certain parts of the codebase.
For example, by inspecting the reference documentation and configuration files (`plugin.xml` and the including XML configuration files), combined with keyword searching, you can try adding and improving various language features, or enhancing the plugin's capabilities and compatibility.

### Contributing to Documentation

The project documentation is mainly divided into general documentation (such as `README.md`), maintainer documentation (located in the [documents](documents) directory), and reference documentation (located in the [docs](docs) directory).
These documents inevitably contain errors, shortcomings, missing details, and areas needing supplementation. Contributions to improve documentation quality are welcome, especially for the project's reference documentation.

### Contributing Config Files

Given the plugin's config-driven architecture, contributing config files is one of the most effective ways to improve support for specific games.
Config files define the semantic information (definitions, corrections, scopes, effects, triggers, etc.) that drives code completion, code inspections, and documentation hints. Keeping them up to date directly improves the user experience for all users of that game.

For reference, please read the [documentation for the config system](https://windea.icu/Paradox-Language-Support/en/config.html), the [syntax reference manual](https://windea.icu/Paradox-Language-Support/en/ref-syntax.html), and the [config format reference manual](https://windea.icu/Paradox-Language-Support/en/ref-config-format.html).

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

## About

### Developed by

**Windea** - the dragon knight who with the title of breeze.

### Powered by

[![JetBrains logo.](https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg)][url:jetbrains]

[badge:doc-zh]: https://img.shields.io/badge/中文文档-2f89d7.svg
[badge:doc-en]: https://img.shields.io/badge/English%20Documentation-2f89d7.svg
[badge:doc-ref]: https://img.shields.io/badge/Reference%20Docs-2f89d7.svg
[badge:github]: https://img.shields.io/badge/GitHub-blue.svg?logo=github
[badge:release]: https://img.shields.io/github/release/DragonKnightOfBreeze/Paradox-Language-Support.svg?sort=semver
[badge:license]: https://img.shields.io/github/license/DragonKnightOfBreeze/Paradox-Language-Support.svg
[badge:plugin-homepage]: https://img.shields.io/badge/Plugin%20Homepage-orange.svg?logo=jetbrains
[badge:plugin-version]: https://img.shields.io/jetbrains/plugin/v/16825.svg?label=version
[badge:plugin-downloads]: https://img.shields.io/jetbrains/plugin/d/16825.svg
[badge:plugin-rating]: https://img.shields.io/jetbrains/plugin/r/rating/16825.svg
[badge:discord]: https://img.shields.io/badge/Discord-Community-blue.svg?logo=discord
[badge:windea]: https://img.shields.io/badge/Developed%20by-Windea-2f89d7.svg?style=flat
[badge:jetbrains]: https://img.shields.io/badge/Supported%20by-JetBrains-000000.svg?style=flat&logo=jetbrains

[url:doc-ref]: https://windea.icu/Paradox-Language-Support
[url:github]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support
[url:issues]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues
[url:release]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/rleeases
[url:license]: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/LICENSE
[url:plugin-homepage]: https://plugins.jetbrains.com/plugin/16825-paradox-language-support
[url:plugin-versions]: https://plugins.jetbrains.com/plugin/16825-paradox-language-support/versions
[url:discord]: https://discord.gg/vBpbET2bXT
[url:afdian]: https://afdian.com/a/dk_breeze
[url:windea]: https://github.com/DragonKnightOfBreeze
[url:jetbrains]: https://jb.gg/OpenSource

[mailto]: mailto:dk_breeze@qq.com