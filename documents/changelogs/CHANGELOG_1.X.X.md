# Changelog 1.X.X

## 1.4.2 - 2025-06-05

- [x] 修复：尝试避免某些特殊情况下的 SOF / FIX: Try to avoid SOF in some special cases
- [x] 优化：优化插件性能 / OP: Optimize plugin performance
- [x] 优化：进一步优化本地化文件的 lexer 和 parser 的实现 / OP: Further optimization for lexer and parser implementations of localisation files
- [x] 优化：区分出单独的规则与集成的设置页面 / OP: Move config related and integrations settings into separate settings pages
- [x] 优化：整理与完善本地化的复制、替换、生成、翻译等操作 / OP: Optimize and improve localisation related actions such as copying, replacing, generating and translating
- [x] 新功能：支持来自规则仓库的远程规则分组，允许配置仓库地址 / NEW: Support remote config groups from config repositories, and allow to configure repository urls
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.4.1 - 2025-05-24

- [x] cwt图片渲染规则待支持 ([#143](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/143))
- [x] cwt规则的type_key_filter <> xxx失效 ([#144](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/144))
- [x] 修复：优化`PlsFileListener`，尝试避免某些特殊情况下的 SOF / FIX: Optimize `PlsFileListener` to avoid SOF in some special cases
- [x] 修复：修复封装变量引用的代码折叠规则失效的问题 / FIX: Fixed the problem that the code folding rule for scripted variable references is inactive
- [x] 优化：完善适用于规则文件的代码补全 / OP: Optimize code completion for config files
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.4.0 - 2025-05-17

- [x] 优化CWT文件解析器的性能 / Optimize performance for CWT file parser ([#94](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/94))
- [x] [VIC3/CK3] Support special localizations - Basic support ([#137](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137))
- [x] 修复与本地化命令连接相关的一些问题（规则解析、代码导航等） / Fixed some problems about localisation command links (Config resolving, code navigation, etc.) ([#140](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/140))
- [x] [Stellaris] Indexing process should not rely on non-indexed file data ([#141](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/141))
- [x] 通过懒解析CWT文件中的选项注释来尝试优化性能 / Try to optimize performance by lazily parsing option comments in CWT files
- [x] 通过懒解析本地化文件中的本地化文本来尝试优化性能与提高代码灵活性 / Try to optimize performance and improve code flexibility by lazily parsing localisation text in localisation files
- [x] 修复：本地化图标如果对应一个sprite，无法正常适用用法高亮 / FIX: Localisation icons could not be properly highlighted if it will be resolved to a sprite
- [x] 修复：本地化命令中的动态值无法查找用法，无法正常适用用法高亮 / Fix: Dynamic values in localisation commands could not find usages, could not be properly highlighted
- [x] 修复：修复关于本地化的语言环境的一些问题 / FIX: Fix some problems about localisation locales
- [x] 修复：修复某些场合下可能无法提示动态值的问题 / FIX: Fix a problem that dynamic values may not be completed in some cases
- [x] 优化：`icon[path]`现在优先匹配直接位于`path`下的图标 / OP: `icon[path]` now prefer to match icons directly under `path`
- [x] 优化：内嵌提示设置中的 iconHeightLimit 的默认值改为36 / OP: Change the default value of iconHeightLimit in inlay hint settings to 36
- [x] 优化：兼容job作为本地化命令连接的情况 / OP: Compatible with jobs as localisation command links
- [x] 优化：在必要时先尝试获取图标的原始高度 / OP: Try to get the original height of the icon first when necessary
- [x] 优化：提供扩展点以更加灵活地解析本地化图标 / OP: Provides EP to resolve localisation icons more flexibly
- [x] 优化：可以从复杂表达式节点所在位置导航到相关规则 / OP: Allow to navigate to related configs from the position of complex expression nodes
- [x] 新功能：支持解析本地化文本中的属性引用&命令的传入参数中的文本颜色ID为引用 / NEW: Support parsing text color ids in arguments of references & commands in localisation text as references
- [x] 新功能：新增代码检查，以在本地化文件中提示缺失指定的其他语言环境的本地化 / NEW: Add code inspection to prompt missing localisations for specified locales in localisation files
- [x] 新功能：新增代码检查，以提示不支持在内联脚本文件中使用参数条件块与带默认值的参数用法 / NEW: Add code inspection to prompt unsupported parameter condition blocks and parameter usages (with the default value) in inline script files
- [x] 新功能：[VIC3/CK3] 初步支持本地化文本中的文本格式（示例：`#v text#!`，其中`v`对应规则表达式`<text_format>`，`text`是富文本的组合） / NEW: [VIC3/CK3] Basic support for text formats in localisation text (e.g., `#v text#!`, where `v` corresponds to the config expression `<text_format>`, and `text` is a combination of rich text)
- [x] 新功能：[VIC3/CK3] 初步支持本地化文本中的文本图标（示例：`@icon!`，其中`icon`对应规则表达式`<text_icon>`） / NEW: [VIC3/CK3] Basic support for text icons in localisation text (e.g., `@icon!`, where `icon` corresponds to the config expression `<text_icon>`)
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.37 - 2025-05-10

- [x] 完善Stellaris 4.0的CWT规则文件 / Optimize CWT config files for Stellaris 4.0
- [x] 修复某些情况下无法正常渲染与预览DDS图片的问题 / Fixed a problem that DDS images could not be properly rendered and previewed in some situations ([#139](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/139))
- [x] 可以分别配置是否启用内置、全局本地、项目的本地规则分组 / Allow to configure whether to enable built-in, global-local, and project-local config groups separately
- [x] 修复无法显示text color的装订线图标（指示对应的颜色）的问题 / Fixed a problem that cannot show the gutter icon (implies corresponding color) for text colors
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.36 - 2025-05-06

- [x] 更新CWT规则文件以适配Stellaris 4.0 / Update CWT config files to match Stellaris 4.0

## 1.3.35 - 2025-05-02

- [x] 优化：可以配置是否在层级视图中显示位置信息与显示名称 / OP: Allow to configure whether to show position info and localized name in hierarchy views
- [x] 实现高级类型层级视图，支持过滤、分组、展开等操作 / Implement advanced type hierarchy views, support operations such as filtering, grouping, expansion, etc.
- [x] 实现事件树与科技树对应的高级层级视图 / Implement advanced hierarchy views for event trees and technology trees
- [x] 可以配置是否在高级层级视图中显示相关信息（事件信息或科技信息） / Allow to configure whether to show related info in advanced type hierarchy views (event info or technology info)
- [x] 为封装变量、定义和本地化提供一些额外的意图操作 / Provide some extra intentions for scripted variables, definitions and localizations
- [x] 提供对本地化的相关定义的支持（快速文档、导航操作、装订线图标） / Provide support for related definitions of localisations (quick doc, navigation actions, gutter icons)
- [x] 修复某些插件配置在IDE重启后会被重置为默认的问题 / Fixed a problem that some plugin settings will be reset to default after IDE restart
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.34 - 2025-04-25

- [x] 更新IDEA版本到2025.1 / Update IDEA version to 2025.1
- [x] 提供适配新UI的图标 / Provide icons that fit the new UI
- [x] 提供简体中文的本地化 / Provide localization for Simplified Chinese
- [x] 优化：有关重载的代码检查不应适用于匿名的定义 / OP: Code checks about overridden should not be applied to anonymous definitions
- [x] 修复与导航到重载目标的快速修正有关的一个问题 / Fixed a problem related to quick fixes for navigation to overridden targets ([#138](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/138))
- [x] 修复依赖发生变化时，相关状态未正确刷新的问题 / Fixed the problem that related statuses are not refreshed correctly when dependencies changes
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.33 - 2025-04-14

- [x] 更新VIC3的CWT规则文件，基于最新的文档 / Update CWT config files for VIC3, based on latest documentation
- [x] [VIC3/CK3] Problems with ?= operator ([#136](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/136))
- [x] 将插件的代码折叠设置合并到插件的设置页面与文件 / Merge plugin code folding settings into plugin settings page and file
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.32 - 2025-04-05

- [x] [CK3] Supports `type_key_prefix` in cwt configs, which is currently only used in ck3's `scripted_effects.cwt` ([#123](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/123))
- [x] 更好的对模板表达式规则的支持 / Better template expression config support ([#129](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/129))
- [x] 更好的对嵌套的块的支持 / Better support for nested blocks ([#130](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/130))
- [x] Only numbers in event names ([#131](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/131))
- [x] [Vic3] Mods are not correctly detected ([#134](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/134))
- [x] 修复无法识别本地化文件中的数据库对象表达式的BUG / Fixed the bug that database object expression in loc files cannot be resolved
- [x] 如果游戏目录未配置，打开模组文件后显示编辑器横幅通知，而非显示全局通知 / If game directory is not configured, show an editor notification after opening mod files, instead of showing a global notification
- [x] 细化代码折叠配置 / Refine code folding configuration
- [x] 代码折叠：允许折叠连续的注释，可配置，默认不启用 / Code folding: Allow to fold continuous comments, configurable, disabled by default
- [x] 智能推断：允许快速推断规则上下文，可配置，默认不启用 / Smart inference: Allow to use quick inference for config context, configurable, disabled by default
- [x] 将内置规则重新移到主要的jar包中以免去不必要的麻烦 / Move built-in configs into main jar to make things easy
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.31 - 2025-03-11

- [x] Complex enums only accept `enum_name = scalar`, not `enum_name = bool` or `enum_name = int` ([#121](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/121))
- [x] Non-Stellaris games raise warning if modifier icon doesn't exist ([#122](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/122))
- [x] [CK3] Types inside other types do not register correctly ([#126](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/126))
- [x] Indexing Errors when working with Localizations ([#127](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/127))
- [x] Optimize color settings for various color schemes
- [x] Optimize indexing performance

## 1.3.30 - 2025-03-01

- [x] 优化对DDS图片的支持（基于[iTitus/dds](https://github.com/iTitus/dds)和[Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv)）/ Optimize support for DDS images (Based on [iTitus/dds](https://github.com/iTitus/dds) and [Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv)) ([#115](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/115))

## 1.3.29 - 2025-02-20

- [x] 修复本地化悬浮工具栏不再显示的问题 / Fixed a bug that the localisation floating toolbar was no longer displayed ([#113](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/113))
- [x] 修复存在多个模组依赖时的一个有关解析作用域的问题 / Fixed a bug about resolving scope when there are multiple mod dependencies ([#114](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/114))
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.28 - 2025-02-11

- [x] 提供扩展点以更好地识别各种来源的游戏目录和模组目录（参见 #111）/ Provides EP to better identify game directories and mod directories from various sources (See #111)
- [x] 将内置规则统一移到插件压缩包中的单独的jar包中 / Move built-in configs into individual jar package which is inside the plugin's zip package
- [x] Support Victoria 3 metadata.json ([#111](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/111))
- [x] 优化内联操作的逻辑 / Optimize the logic of inline operations
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.27 - 2025-01-09

- [x] 支持使用内联数学块作为封装变量的值 / Support using inline math block as scripted variable value ([#108](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/108))
- [x] 优化脚本文件的索引逻辑，修复未索引不在顶层的封装变量的BUG / Optimize indexing logic for script files, fix a bug that non-root level local scripted variable are not indexed

## 1.3.26 - 2024-12-06

- [x] Victoria 3 has one more type of color: hsv360 ([#103](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/103))
- [x] There is a problem with the new ?= operator when it is directly on a value. ([#104](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/104))
- [x] 优化`IncorrectFileEncodingInspection` (参见 #102) / Optimize `IncorrectFileEncodingInspection` (See #102)
- [x] 优化规则匹配逻辑，以更准确地匹配与区分`scope_field`与`value_field` / Optimize rule match logic to match and differ `scope_field` and `value_field` more exactly
- [x] 兼容在复杂表达式中使用内联的参数条件表达式（如，`value:xxx|P|[[P1]$V$]V|`）/ Compatible with inline parameter condition expressions in complex expressions (e.g., `value:xxx|P|[[P1]$V$]V|`)
- [x] 修复查询修正相关本地化时没有忽略大小写的问题 / Fixed the issue that case was not ignored when searching modifier-related localisations
- [x] Victoria 3 link Comparisons - 兼容传参格式的连接（如，`relations(root.owner)`）/ Compatible with argument-style links (e.g., `relations(root.owner)`) ([#101](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/101))
- [x] Read access is allowed from inside read-action only - 相关优化 / Related optimizations ([#105](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/105))

## 1.3.25 - 2024-11-25

- [x] 兼容脚本文件中的内联模板表达式（如，`has_ethic = ethic_[[fanatic]fanatic_]pacifist`）/ Compatible with inline template expressions in script files (e.g., `has_ethic = ethic_[[fanatic]fanatic_]pacifist`)
- [x] [#96](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/96)
- [x] 兼容VIC3中的定值（define）引用（对应新的规则类型`$define_reference`）/ Support Vic3 define references (corresponding to new data type `$define_reference`) ([#97](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/97))
- [x] 删除`disableLogger`以免忽略某些重要的报错 / Remove `disableLogger` to avoid ignoring some important errors. ([#100](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/100))

## 1.3.24 - 2024-11-11

- [x] 更新CWT规则文件以适配Stellaris 3.14.1 / Update CWT config files to match Stellaris 3.14.1
- [x] 允许在内联脚本文件中引用其使用位置的本地封装变量 / Allow to use local scripted variables from usage locations in inline script files ([#93](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/93))
- [x] 合并规则表达式时兼容`alias_name[x]`和`alias_keys_field[x]` / When merge config expressions, support merge `alias_name[x]` and `alias_keys_field[x]` ([#95](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/95))
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.23 - 2024-10-25

- [x] 修复无法补全封装变量（scripted variable）的问题 / Fix a problem that cannot complete scripted variables
- [x] 排除特定的根目录以免解析与索引一些意外的文件 / Exclude some specific root file paths to avoid parsing and indexing unexpected files ([#90](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/90))
- [x] 可以配置是否用对应的颜色高亮本地化颜色ID（默认启用）/ Provide the configuration that whether to highlight localisation color ids by corresponding color (enabled by default) ([#92](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/92))
- [x] 修复渲染本地化文本时某些文本会被重复渲染的问题 / Fixed a problem that some text will be rendered repeatedly when rendering localisation text
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.22 - 2024-10-15

- [x] 新的代码检查：`NonTriggeredEventInspection` / New code inspection: `NonTriggeredEventInspection` ([#88](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/88))
- [x] BUG修复：修复不正确地缓存了基于扩展的规则推断的参数的上下文规则的问题 / BUG fix: fixed an issue that context configs for parameters inferred based on extended configs were cached incorrectly
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.21 - 2024-09-14

- [x] 更新CWT规则文件以适配Stellaris 3.13.0 / Update CWT config files to match Stellaris 3.13.0
- [x] 优化性能与内存占用 / Optimize performance and memory
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.20 - 2024-09-06

- [x] 适用于规则文件的代码补全，也适用于插件或者规则仓库中的CWT文件 / Code completion for config files: also for CWT files in plugin or config repositories
- [x] 完善脚本文件与本地化文件的词法器与解析器，修复存在的一些问题 / Optimize lexer and parser for script and localisation fixes, fix some existing problems
- [x] 完善对本地化命令的支持 / Optimize support for localisation commands
- [x] 完善参数对应的规则的推断与合并的逻辑 / Optimize inference and merging logic for configs of parameters
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes
- [x] Optimize inference and merging logic for configs of parameters
- [x] Other optimizations and bug fixes

## 1.3.19 - 2024-08-24

- [x] 完善适用于规则文件的代码补全 / Optimize code completion for config files
- [x] 修复插件内置的VIC3的规则文件实际上是VIC2的规则文件的问题 / Fix an issue that builtin VIC3 config files are VIC2's ([#83](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/83))
- [x] 解析规则文件中的路径时需要移除"game/"路径前缀 / Remove path prefix "game/" when resolve paths in config files ([#84](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/84))
- [x] 允许在声明规则的顶级使用`single_alias_right[x]` / Allow to use `single_alias_right[x]` on top level of declaration config ([#85](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/85))
- [x] Date Validation and ?= does not work for Victoria 3 ([#86](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/86))

## 1.3.18 - 2024-08-18

- [x] 新功能：适用于规则文件的代码补全（初步支持）/ New feature: Code completion for config files (initial support)
- [x] 修复解析脚本文件时，会在特定情况下进入无限循环的问题 / Fix an issue that it will enter an infinite loop in specific situation when parsing script files. ([#82](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/82))
- [x] 修复与作用域解析相关的一些问题 / Fix some problems about parsing scopes
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.17 - 2024-08-06

- [x] BUG修复 / BUG fixes

## 1.3.16 - 2024-08-06

- [x] BUG修复 / BUG fixes

## 1.3.15 - 2024-08-05

- [x] 正确渲染从数据库对象生成的概念的文本 / Render concepts from database objects correctly
- [x] 修复对于`param = "$param$"`，当可以推断`param`的规则上下文时，无法推断`$param$`的规则上下文的问题 / Fix a problem that for `param = "$param$"`, when config context of `param` can be inferred, config context of `$param$` cannot be inferred
- [x] 支持在数据库对象表达式中重复引用基础数据库对象，以在游戏中强制显示为非转换形式（如`authority:auth_oligarchic:auth_oligarchic`）/ Supports referencing base database objects in database object expression repeatedly to force show non-swapped form in the game (e.g., `authority:auth_oligarchic:auth_oligarchic`)
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.14 - 2024-07-18

- [x] BUG修复 / BUG fixes

## 1.3.13 - 2024-07-17

- [x] 支持内联脚本块中的带参数的封装变量引用（如`k = @[ foo$p$bar + 1 ]`）/ Supports parameterized scripted variable references in inline math blocks (e.g., `k = @[ foo$p$bar + 1 ]`)
- [x] 支持数据库对象表达式以及从数据库对象生成的概念（如`civic:some_civic`）/ Supports database object expression and concepts from database objects (e.g., `civic:some_civic`) ([#56](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/56))
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.12 - 2024-07-05

- [x] 尝试推断在条件表达式以及内联数学块中使用过的参数的上下文规则 / Try to infer context configs of parameters which are used in condition expression and inline math blocks ([#67](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/67))
- [x] 考虑到`PARAM = $PARAM|no$`等同于`[[PARAM] PARAM = $PARAM$ ]`，为此添加一些额外的意向操作 / Add some extra intention actions during to the equality of `PARAM = $PARAM|no$` and `[[PARAM] PARAM = $PARAM$ ]` ([#67](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/67))
- [x] 允许指定参数的规则上下文与作用域上下文，使其与参数上下文的保持一致（基于`## inherit`，详见参考文档）/ Allow to specify a parameter's config context and scope context, to make them consistent with the parameter contexts (Base on `## inherit`, see reference documentation for details) ([#68](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/68))

## 1.3.11 - 2024-06-25

- [x] BUG修复：修复图表（如科技树）无法正常显示的问题 / BUG fix: Fix a problem that diagrams (e.g., Technology Tree Diagram) cannot be opened correctly
- [x] 优化：优化对游戏目录的判断 / Optimization: Optimize the logic to determine game directory

## 1.3.10 - 2024-06-13

- [x] 新功能：支持全局的本地规则分组（可在插件配置页面中配置所在的目录）/ New feature: Support for global local config groups (Can be configured in plugin's settings page)
- [x] 优化：在项目视图中显示规则分组的位置（作为外部库）/ Optimization: Show locations of config groups in project view (as external libraries)

## 1.3.9 - 2024-06-05

- [x] 更新CWT规则文件以适配Stellaris 3.12.3 / Update CWT config files to match Stellaris 3.12.3
- [x] 新功能：支持切换快速文档中的本地化使用的语言环境（如果可用，打开快速文档后，在右键菜单或者右下角更多菜单中，点击`Change Localisation Locale`）/ New feature: Supports to change localisation locale in quick documentation (If available, open quick documentation, then click `Change Localisation Locale` in Right Click Menu or More Menus in the bottom-right corner)
- [x] 支持通过扩展的CWT规则文件为动态值（如`event_target`）指定作用域上下文 - 提供更加完善的支持（详见参考文档）/ Support for specifying the scope context for dynamic values (e.g., `event_target`) via extended CWT configs - more perfect support (See reference documentation for details) ([#78](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/78))
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.8 - 2024-05-27

- [x] 优化查询，提高性能，修复有关重载的一些BUG / Optimize search implementation, performance improvement, bug fixes about override
- [x] 优化代码格式化功能，修复一些细节上的BUG / Optimize code reformat implementation, bug fixes about details
- [x] 优化插件设置页面 / Optimize plugin settings page
- [x] 涉及CWT选项注释与文档注释时，粘贴文本以及行注释/取消行注释能够得到正确的结果 / Paste, comment / uncomment with line comment now work correctly when CWT option comments or documentation comments are involved
- [x] 允许折叠本地化文件中的本地化引用与本地化图标（完全折叠，可配置，默认不启用，搭配相关的内嵌提示使用）/ Now it's available to fold localisation references & icons in localisation files (fully folded, configurable, disabled by default, use with relevant inlay hints)
- [x] 允许折叠本地化文件中的本地化命令与本地化概念（可配置，默认不启用）/ Now it's available to fold localisation commands & concepts (configurable, disabled by default)
- [x] 也将`"$PARAM$"`中的`$PARAM$`识别为参数 / Treat `$PARAM$` in `"$PARAM$"` as a parameter ([#72](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/72))
- [x] 匹配脚本内容与规则时，如果带参数的键的规则类型是唯一确定的，则需要继续向下匹配 / When match script content with configs, if the config type of parameterized key can be determined uniquely, it's necessary to continue matching down ([#79](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/79))
- [x] 匹配脚本内容与规则时，如果作为参数的键的规则类型可以（从扩展的CWT规则）推断出来且是匹配的，则需要继续向下匹配 / When match script content with configs, if the config type of parameterized key can be inferred and matched (via extended CWT configs), it's necessary to continue matching down ([#79](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/79))
- [x] 如果`$scope$`表示一个作用域连接，也尝试（从扩展的CWT规则）推断其作用域上下文 / If `$scope$` represents a scope link, also try to infer its scope context (via extended CWT configs) ([#79](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/79))
- [x] 对于作用域的正确性的检查应当尽可能地适用 / Incorrect expression inspection for scope field expressions should be available wherever possible ([#80](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/80))
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.7 - 2024-05-12

- [x] 更新CWT规则文件以适配Stellaris 3.12.2（基本完成）/ Update CWT config files to match Stellaris 3.12.2 (almost done)
- [x] 优化代码格式化功能，修复一些细节上的BUG / Optimize code reformatting feature, fixes some bugs in details
- [x] 基于CWT规则文件来确定目标（定义、本地化等）的覆盖策略（可以自定义，参见参考文档）/ Determine override order for targets (definitions, localisations, etc.) based on CWT configs files (Can be customized, see reference documentation for details)
- [x] 对于本地化文件，本地化文本中的双引号不需要转义（直到本行最后一个双引号之前为止，视为本地化文本）/ For localisation file, it's unnecessary to escape double quotes in localisation text
- [x] 如果目标无法解析，但是存在对应的扩展的CWT规则，可以配置是否忽略相关的代码检查 / If a target cannot be resolved, but related extended CWT configs exist, related code inspection can be configured to be ignored
- [x] 如果可以从扩展的CWT规则文件推断作用域上下文，就不要再尝试从使用推断 / If the scope context can be inferred from extended CWT configs, do not continue to be inferred from usages
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.6 - 2024-04-28

- [x] 在内置规则文件的编辑器通知中提供操作，点击可以生成或者导航到对应的项目本地的规则文件 / Provide an action in editor notification bar for builtin config files, click to generate (or navigate to) related project-local config file
- [x] 支持扩展的数据类型`AntExpression`，用于匹配ANT路径表达式（详见参考文档）/ Supports extended data type `AntExpression`, which is used to match ANT expression (see reference documentation for details) ([#73](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/73))
- [x] 支持扩展的数据类型`Regex`，用于匹配正则表达式（详见参考文档）/ Supports extended data type `Regex`, which is used to match regex (see reference documentation for details) ([#73](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/73))
- [x] 支持在`## context_key`的值中使用模板表达式（详见参考文档）/ Supports for using template expressions for value of `## context_key` (see reference documentation for details) ([#73](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/73))
- [x] 编写扩展的CWT规则时，可以通过字符串字面量以及模板表达式进行匹配的地方，现在也可以通过ANT路径表达式以及正则表达式进行匹配（详见参考文档）/ When writing extended CWT configs, in places which can use string literals and template expressions, now can also use ANT expressions and regular expressions (see reference documentation for details) ([#73](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/73))
- [x] 修复一个有关作用域上下文切换的问题 / Fix a problem about scope context switching ([#71](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/71))
- [x] 修复一个有关索引以及引用解析的问题 / Fix a problem about indexing and reference resolving ([#74](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/74))
- [x] 通过扩展的CWT规则提供上下文规则时，允许直接在顶级规则使用`single_alias`（示例：`extended_param = single_alias_right[trigger_clause]`）/ When provide context configs by extended configs, allows to use `single_alias` at top level directly (e.g. `extended_param = single_alias_right[trigger_clause]`) ([#76](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/76))
- [x] 如果需要，对于已打开的文件，其文件内容应当及时重新解析，其中的内嵌提示应当及时更新（注意这可能需要先在后台花费一定时间）/ If necessary, for opened files, reparse its file context and refresh its inlay hints (Note that this may take some times in background first)
- [x] 优化代码补全的用时 / Optimize time cost of code completion

## 1.3.5 - 2024-04-20

- [x] 为操作"导航到相关的CWT规则"提供更加完善的支持（额外适用于封装变量、参数等）/ Improved support for the action "Goto to related CWT configs" (For scripted variables, parameters, etc.)
- [x] 支持通过扩展的CWT规则为封装变量（`scripted_variable`）提供扩展的快速文档（详见参考文档）/ Support for providing extended quick documentation for scripted variables via extended CWT configs (see reference documentation for details)
- [x] 支持通过扩展的CWT规则为封装变量（`scripted_variable`）提供扩展的内嵌提示（详见参考文档）/ Support for providing extended inlay hints for scripted variables via extended CWT configs (see reference documentation for details)
- [x] 支持为一些目标额外提供基于扩展的CWT规则的代码补全（包括封装变量、定义、内联脚本、参数、复杂枚举值与动态值，可配置，默认不启用）/ Support for providing additional code completion for various targets via extended CWT configs (For scripted variables, definitions, inline scripts, parameters, complex enum values and dynamic values; Configurable; Disabled by default) ([#66](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/66))
- [x] 支持通过扩展的CWT规则为一些目标指定作用域上下文（包括定义、参数与内联脚本，基于`## replace_scopes`和`## push_scope`，忽略选项不合法的情况）/ Support for specifying the scope context for various targets via extended CWT configs (For definitions, parameters and inline scripts; Via `## replace_scopes` and `## push_scope`; Ignore invalid situations) ([#69](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/69))
- [x] 提供内联脚本用法的代码补全（`inline_script = ...`，可配置，默认不启用，目前适用于所有游戏类型）/ Provide code completion for inline script usages (`inline_script = ...`; Configurable; Disabled by default; Currently for all game types) ([#70](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/70))
- [x] 添加代码检查：未使用的内联脚本（弱警告级别）/ New code inspection: Unused inline scripts (level: weak warning)
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.4 - 2024-03-31

- [x] 为操作"导航到相关的CWT规则"提供更加完善的支持 / Improved support for the action "Goto to related CWT configs"
- [x] 支持通过扩展的CWT规则为复杂枚举值（如`component_tag`）提供扩展的快速文档（详见参考文档）/ Support for providing extended quick documentation for complex enum values (e.g. `component_tag`) via extended CWT configs (see reference documentation for details)
- [x] 支持通过扩展的CWT规则为动态值（如`variable`）提供扩展的快速文档（变更了对应CWT规则的格式，详见参考文档）/ Support for providing extended quick documentation for dynamic values (e.g. `variable`) via extended CWT configs (format of relevant CWT configs is changed, see reference document for details)
- [x] 支持通过扩展的CWT规则为复杂枚举值（如`component_tag`）提供扩展的内嵌提示（详见参考文档）/ Support for providing extended inlay hints for complex enum values (e.g. `component_tag`) via extended CWT configs (see reference documentation for details)
- [x] 支持通过扩展的CWT规则为动态值（如`variable`）提供扩展的内嵌提示（详见参考文档）/ Support for providing extended inlay hints for dynamic values (e.g. `variable`) via extended CWT configs (see reference documentation for details)
- [x] 添加代码检查：重载的文件（弱警告级别，默认不开启）/ New code inspection: Overridden for files (level: weak warning, enabled by default: no)
- [x] 修复当CWT规则涉及`single_alias`时，应用代码补全后，不会正确地自动插入` = {}`的问题 / Fixed an issue that `= {}` would not be inserted correctly when applying code completion for script snippets matching CWT config of `single_alias`
- [x] 尝试修复涉及内联脚本与适用语言注入的参数时，IDE可能卡死的问题 / Try to fix an issue that when inline scripts and parameters (with language injection) are involved, IDE may be freezing.

## 1.3.3 - 2024-03-04

- [x] 更新CWT规则文件以适配Stellaris 3.11.1（基本完成）
- [x] 完善对作用域上下文的支持
- [x] 提供汇总的规则文件的项目视图
- [x] 添加代码检查：重载的封装变量（scripted_variable）（弱警告级别，默认不开启）
- [x] 添加代码检查：重载的定义（弱警告级别，默认不开启）
- [x] BUG修复：对于作用域切换，`prev.prev`应当等同于`this`，而非`prevprev`
- [x] BUG修复：对于本地化文件，本地化文本中的双引号不需要转义（直到本行最后一个双引号之前为止，视为本地化文本）
- [x] BUG修复：修复在插件新增的项目视图中无法正确选中已打开的文件的问题
- [x] 其他优化与BUG修复

## 1.3.2 - 2024-02-06

- [x] 完善对全局的默认游戏目录的配置的支持
- [x] 优化CWT规则的匹配逻辑

## 1.3.1 - 2024-01-11

- [x] 为菜单`Code -> Unwrap/Remove...`提供更多可选择的操作
- [x] 修复插件可能无法正确解析新添加的本地化在脚本文件中的引用的问题
- [x] 为操作“导航到相关的CWT规则”提供更加完善的支持
- [x] 支持通过扩展的CWT规则文件为参数指定CWT规则上下文
- [x] 支持通过扩展的CWT规则文件为内联脚本指定CWT规则上下文
- [x] 可以在插件配置中配置全局的默认游戏目录

## 1.3.0 - 2023-12-30

- [x] 兼容IDEA 233，避免一些IDE启动时的报错
- [x] 支持通过扩展的CWT规则文件为定义（如`event`）提供扩展的快速文档
- [x] 支持通过扩展的CWT规则文件为动态值（如`event_target`）提供扩展的快速文档
- [x] 支持通过扩展的CWT规则文件为定义（如`event`）指定作用域上下文
- [x] 支持通过扩展的CWT规则文件为动态值（如`event_target`）指定作用域上下文
- [x] 支持通过扩展的CWT规则文件为参数提供扩展的快速文档
- [x] 完善对脚本文件和本地化文件中的转义字符的支持
- [x] 支持在多行的脚本参数值中使用内联脚本 ([#55](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/55))
- [x] 将条件参数加入参数上下文，用于代码补全、快速文档等功能 ([#58](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/58))
- [x] 避免在获取上下文规则列表时递归加载缓存 ([#59](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/59))

## 1.2.6 - 2023-11-28

- [x] 优化为脚本文件提供的关键字的代码补全
- [x] 优化为脚本文件提供的参数值的代码补全
- [x] 修复无法正常支持整行或多行的脚本参数的参数值的问题

## 1.2.5 - 2023-11-28

- [x] 修复覆盖了`economic_category`之后，无法正常解析因覆盖而新生成的修正的问题
- [x] 优化索引查询的逻辑 - 预先经过必要的排序、过滤和去重
- [x] 其他优化与BUG修复

## 1.2.4 - 2023-11-21

- [x] 更新CWT规则文件以适配Stellaris 3.10.0（进一步完善）
- [x] 优化：在CWT规则的快速文档中显示CWT规则文件信息
- [x] BUG修复：修复无法渲染内嵌提示中的图标的问题
- [x] 其他优化与BUG修复

## 1.2.3 - 2023-11-19

- [x] 更新CWT规则文件以适配Stellaris 3.10.0（基本完成）
- [x] 优化与BUG修复

## 1.2.2 - 2023-11-17

- [x] 支持内联`scripted_variable`（即封装变量）（`编辑器右键菜单 -> Refactor -> Inline...`）
- [x] 支持内联`scripted_trigger`和`scripted_effect` （`编辑器右键菜单 -> Refactor -> Inline...`，仅限作为调用的引用）
- [x] 支持内联本地化（`编辑器右键菜单 -> Refactor -> Inline...`，仅限本地化文本中的引用）
- [x] 支持内联`inline_script`（即内联脚本）（`编辑器右键菜单 -> Refactor -> Inline...`）
- [x] 可以从项目视图或者模组依赖配置页面中的游戏或模组根目录打开其本地目录或者Steam创意工坊页面，以及复制对应路径/URL
- [x] 可以在工具菜单（`编辑器右键菜单 -> Paradox Language Support`）打开和复制数种路径/URL
- [x] （仅限Stellaris）支持表达式`technology@level` - 参见：[\[Stellaris\] Could support tech@level grammar? · Issue #58](https://github.com/cwtools/cwtools-vscode/issues/58)
- [x] 其他优化与BUG修复

## 1.2.1 - 2023-11-07

- [x] 支持语法`@a = @[ 1 + 2 ]` - 参见：[The tool cannot recognize in-script flag variables (Vic3) · Issue #76)](https://github.com/cwtools/cwtools-vscode/issues/76)
- [x] （仅限VIC3）支持运算符`?=` - 参见：[Parsing issues in Vic3 · Issue #53](https://github.com/cwtools/cwtools/issues/53)
- [x] 其他优化与BUG修复

## 1.2.0 - 2023-11-05

- [x] 优化对[规则分组](https://windea.icu/Paradox-Language-Support/zh/config.html#config-group)的支持
- [x] 其他优化与BUG修复

## 1.1.13 - 2023-09-29

- [x] BUG修复

## 1.1.12 - 2023-09-29

- [x] 尝试优化插件性能 - 尝试优化编制索引时的速率与内存使用

## 1.1.11 - 2023-09-27

- [x] 无法解析使用了scripted_variables的参数定值 ([#51](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/51))
- [x] 兼容脚本文件的高级插值语法（`a[[b]c]d$e|f$g`） ([#30](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/30))

## 1.1.10 - 2023-09-15

- [x] 修复进行代码提示时列出的提示项可能不全的问题
- [x] 其他优化与BUG修复

## 1.1.9 - 2023-09-13

- [x] 更新CWT规则文件以适配Stellaris 3.9.1
- [x] 其他优化与BUG修复

## 1.1.8 - 2023-09-12

- [x] 优化：完善对修正的名字和描述的本地化，以及图标的图片支持 ([#50](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/50))
- [x] 优化进行代码补全时提示项的排序
- [x] 优化代码检查
- [x] 其他优化与BUG修复

## 1.1.7 - 2023-08-26

- [x] 优化：生成本地化时，也可以基于脚本文件中无法解析的本地化（适用于对应的快速修复和生成操作）
- [x] 优化：重命名定义时，也重命名相关的本地化和图片的引用，以及由其生成的修正的引用，以及这些修正的相关本地化和图片的引用（可以配置并且默认启用）
- [x] 优化：提示封装变量和本地化时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限 -> 改为仅限本地化
- [x] 优化：如果需要，渲染图片时，先按照正确的帧数和总帧数切分原始图片
- [x] 尝试优化插件性能 - 避免在编制索引时访问索引（例如，如果编制索引时需要匹配定义，只要格式上匹配，就直接认为匹配）
- [x] 尝试优化插件性能 - 优化规则对象的内存占用
- [x] 其他优化与BUG修复

## 1.1.6 - 2023-08-09

- [x] 修复`some_scripted_trigger`可能被插件认为同时匹配`<scripted_trigger>`和`scope_field`的问题（以及类似问题）
- [x] 优化对嵌套的定义的支持（如，`swapped_civic`）
- [x] 提示封装变量和本地化时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限 ([#48](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/48))
- [x] 尝试优化插件性能

## 1.1.5 - 2023-08-03

- [x] 更新IDEA版本到2023.2
- [x] 匹配规则时如果发生索引异常，防止IDE报错
- [x] 修复DDS图片无法正确渲染的问题
- [x] 修复更新到IDEA 2023.2后，按住Ctrl并点击参数（以及其他类似目标）后，无法查找用法的问题
- [x] 修复更新到IDEA 2023.2后，无法打开事件树和科技树图表的问题
- [x] Parameters with defaults passed to script values cause highlight errors ([#47](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/47))
- [x] 优化：检查定义的相关本地化和图片是否缺失时，也检查由其生成的修正的相关本地化和图片是否缺失（可以配置并且默认不启用）
- [x] 优化：生成本地化时，可以配置需要生成哪些本地化（参考重载/实现方法时弹出的对话框）

## 1.1.4 - 2023-07-30

- [x] 修复构建的事件树和科技树中没有任何内容的问题
- [x] 尝试优化插件的性能 - 极大优化了进行查询和代码检查时的性能
- [x] 尝试优化插件的内存占用

## 1.1.3 - 2023-07-18

- [x] 兼容带有参数的相关本地化和相关图片（不进行进一步的解析，进行代码检查时也不报错）
- [x] 支持渲染嵌入到本地化文本中的任意大小的图片（DDS/PNG）
- [x] 修复构建科技树图时报错的问题
- [x] 修复无法查询自身为文件的定义的问题
- [x] 进行代码补全时，如果需要，自动补上双引号
- [x] 修复可能无法解析顶层的作用域切换中的表达式对应的规则的问题（`some_scripted_effect = { root = { ... } }`）
- [x] 如果可能，尝试推断模组的游戏类型（例如，当模组目录位于对应游戏的创意工坊目录下时）（如果可以推断，就不允许通过模组设置更改）
- [x] 尝试使用语言注入功能推断脚本参数的传入值对应的CWT规则上下文，从而提供高级语言功能（待完善）
- [x] 尝试使用语言注入功能推断脚本参数的默认值对应的CWT规则上下文，从而提供高级语言功能（待完善）
- [x] 其他优化和BUG修复

## 1.1.2 - 2023-07-02

- [x] 优化相关本地化和图片的CWT规则的解析逻辑：默认视为必须的，加上`## optional`后视为可选的
- [x] 优化相关本地化和图片的CWT规则的解析逻辑：对于`name = X`中的`X`，如果包含占位符`$`，将占位符替换成定义的名字后，尝试得到对应名字的本地化或者对应路径的图片，否则尝试得到对应名字的属性的值对应的本地化或者图片
- [x] 优化相关图片的CWT规则的解析逻辑：对于`name = X`中的`X`，如果包含占位符`$`并且以`GFX_`开头，将占位符替换成定义的名字后，尝试得到对应名字的sprite定义对应的图片
- [x] 优化相关本地化的CWT规则的解析逻辑：对于`name = "$|u"`，将占位符替换成定义的名字且全部转为大写后，尝试得到对应名字的本地化或者对应路径的图
- [x] 完善CWT规则 - 清理和完善基本的相关本地化、相关图片的规则
- [x] 其他优化与BUG修复

## 1.1.1 - 2023-06-28

- [x] 修复方法`isSamePosition`的实现中的问题 ([#36](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/36))
- [x] value_field和variable_field应当可接受`-$PARAM$` ([#38](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/38))
- [x] 允许`$SCOPE|this$.modifier:xxx` ([#39](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/39))
- [x] 修复可能无法正确解析嵌套的定义声明中的内容的问题 ([#44](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/44))
- [x] 修复更改游戏类型后无法打开模组设置的BUG
- [x] 更新CWT规则
- [x] 其他BUG修复

## 1.1.0 - 2023-06-19

- [x] 应当先尝试将`k = {...}`解析为内联脚本用法，再解析为定义声明 ([#26](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/26))
- [x] 重命名内联脚本的文件名时，相关缓存未正确刷新 ([#32](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/32))
- [x] 重命名内联脚本的文件名时，相关的表达式未正确更新文本 ([#33](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/33))
- [x] 添加代码检查：未正确进行重载的全局封装变量（例如，游戏文件`test.txt`中声明了一个全局封装变量`@v`，而模组在文件`zzz_test.txt`中进行了重载）
- [x] 添加代码检查：未正确进行重载的定义 （例如，游戏文件`test.txt`中声明了一个事件`e.1`，而模组在文件`zzz_test.txt`中进行了重载）
- [x] 兼容直接在脚本文件中使用内联脚本，而非必须在定义声明中（或者内联后在定义声明中）
- [x] 尝试在更多情况下推断脚本参数对应的CWT规则（上下文）

目前支持在以下情况下推断脚本参数对应的CWT规则（上下文）：

- 支持脚本参数作为整个表达式，且可以推断此表达式对应的CWT规则的情况
- 支持脚本参数作为整个表达式，且所在位置对应的参数值可以是脚本片段（值或者属性的值），且可以推断此脚本片段对应的CWT规则上下文的情况
- 如果推断得到的CWT规则（上下文）并不完全一致，尝试进行合并
  - 对于CWT规则，如果规则的表达式相兼容（如`value[event_target]`和`value_field`），则认为可以合并
  - 对于CWT规则上下文，如果规则在文件中是同一处，或者内联后是同一处（如`single_alias_right[trigger_clause]`），则认为可以合并
- 如果合并后得到的CWT规则（上下文）为空，则认为存在冲突
  - 某些内容相兼容的CWT规则（如不是同一处规则的`possible = {...}`），目前版本仍然会被认为存在冲突

不再在标准参考文档和标准CHANGELOG中写明非标准的功能和扩展。因此，不保证以下功能仍然可用：

- 快速文档链接也能作为html/markdown等文件中的超链接使用，从而引用和跳转到指定的定义/本地化等（参见之前的CHANGELOG）
- 通过特定的注释注入文件路径以及文件元素路径，以便把脚本片段识别为特定的定义声明（参见之前的CHANGELOG）

## 1.0.8 - 2023-06-15

- [x] 提供一种项目视图，用于显示合并后的所有游戏和模组文件（`Project Pane -> Paradox Files`）
- [x] 初步提供对覆盖策略的支持，用于在查询文件、封装变量、定义、本地化时基于覆盖进行排序（除了代码补全等需要逐步遍历的地方）
- [x] 修复可能无法从字符串常量规则和枚举值规则反向查找在脚本文件中的使用的问题
- [x] 修复DIFF视图中没有正确重载文件类型的问题
- [x] 修复在输入本地化引用时（以及其他类型情况）可能因为读写锁冲突导致IDE卡死的问题
- [x] 修复在进行代码补全时可能不会提示某些应当匹配的本地化的问题
- [x] 修复初次打开项目时某些文件路径引用可能会被显示无法解析的问题
- [x] 优化：也可以在本地化引用上调用复制本地化文本的意向
- [x] 尝试优化性能
- [x] 其他优化和BUG修复

## 1.0.7 - 2023-06-08

- [x] 在脚本文件中，应当允许多行的用双引号括起的字符串 ([#26](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/26))
- [x] 生成科技树时报错 ([#28](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/28))
- [x] 应当允许在SV表达式（以及其他各种复杂表达式）中为参数指定默认值 ([#29](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/29))
- [x] 优化基于显示名称进行代码补全时的性能
- [x] 尝试优化性能

## 1.0.6 - 2023-06-03

- [x] 改为仅在必要时才对一些索引使用懒加载（如需要内联的情况）
- [x] 不再推断`static_modifier`的作用域上下文（可以不匹配，同`modifier`和`scripted_modifier`）
- [x] 尝试优化性能

## 1.0.5 - 2023-05-31

- [x] 完善对定义的作用域上下文推断的支持 - 完善实现逻辑，优化性能，支持更多的定义类型
- [x] 改为默认不启用作用域上下文推断（因为可能性能不佳）
- [x] 重新将一些索引改为懒加载（因为它们在索引时可能需要访问其他索引，从而可能导致无法避免的报错）
- [x] 尝试优化性能 - 优化解析脚本参数和本地化参数时的性能
- [x] 其他优化和BUG修复

## 1.0.4 - 2023-05-29

- [x] 尝试优化性能：如果可行，直接在索引时获取定义的子类型
- [x] 尝试优化性能：`ParadoxValueSetValueFastIndex` - 改为使用`FileBasedIndex`（在项目启动时就完成索引，避免之后卡住文件解析和代码检查）
- [x] 尝试优化性能：`ParadoxComplexEnumValueIndex` - 改为使用`FileBasedIndex`（在项目启动时就完成索引，避免之后卡住文件解析和代码检查）
- [x] 空指针 ([#25](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/25))
- [x] 更新CWT规则文件以适配Stellaris 3.8.3

## 1.0.3 - 2023-05-25

- [x] 尝试优化内存占用
- [x] 尝试优化性能

## 1.0.2 - 2023-05-22

- [x] 尝试优化性能
- [x] 其他优化和BUG修复

## 1.0.1 - 2023-05-22

- [x] 修复代码检查`UnusedValueSetValueInspection`和`UnsetValueSetValueInspection`运行不正确的问题
- [x] 修复初次打开项目时可能无法正确解析各种复杂表达式的问题
- [x] 索引valueSetValue（如，`event_target`）时，也包含仅在本地化文件中使用到的`event_target`和`variable`
- [x] 其他优化和BUG修复

## 1.0.0 - 2023-05-20

- [x] 提示定义和修正时，也可以基于它们的显示名称进行提示。（在插件配置页面中勾选`Code Completion > Complete by localized names` 以启用此功能）
- [x] 尝试基于在其他事件中的调用推断事件的作用域上下文（如果推断发生递归或者存在冲突则取消推断）
- [x] 完善Stellaris的CWT规则文件 - 完善`localisation.cwt`
- [x] 尝试优化性能
- [x] 其他优化和BUG修复