# Changelog

## Unreleased

- [ ] 更新参考文档，更加详细，尽可能全面地介绍插件的功能
- [ ] 兼容更加复杂的本地化命令（例如，带有多个参数、用引号括起的参数）
- [ ] 兼容通过内联脚本声明的定义，兼容相关语言功能（定义的名字在参数值中，而定义的声明在内联脚本文件中）
- [ ] 支持为同一个模组配置多个模组依赖列表
- [ ] 支持通过内置方案、手动乃至AI解决模组冲突，合并模组文件，提供相关的全局代码检查和操作
- [ ] 支持直接在IDE中启动游戏，且启用的模组来自可配置的模组列表
- [ ] 新功能：提供AI驱动的脚本/本地化生成功能（操作） / NEW: Provide AI-driven script/translation generation features (actions)
- [ ] 新功能：提供对魔法注释的初步支持，用于批量处理脚本与本地化文件的内容 / NEW: Provide support for magic comments, which are used to batch process of scripts and localisation files
- [ ] 新功能：支持基于CWT规则的自定义的代码折叠 / NEW: Support custom code folding based on CWT configs
- [ ] 新功能：支持基于CWT规则的自定义的后缀补全 / NEW: Support custom postfix completion based on CWT configs

## 2.0.2-dev

- [X] #165
- [X] 优化：优化自动识别切换类型的声明规则的逻辑 / OP: Optimize the logic for automatically detecting declaration configs for swapped types
- [X] 优化：如果本地化参数可以被解析为本地化，则启用特殊高亮 / OP: If a localization parameter can be resolved to a localization, enable special highlighting
- [X] 优化：优化编制与查询索引时的性能 / OP: Optimize the performance of building and querying indices
- [X] 优化：可以配置可在随处搜索中搜索的符号的类型 / OP: Can configure the types of symbols that can be searched in Search Everywhere
- [X] 优化：随处搜索也支持搜索规则文件中的符号（类型、复杂枚举等） / OP: Search Everywhere also supports searching symbols in config files (types, complex enums, etc.)
- [X] 优化：更好的用于翻译/润色本地化的AI提示 / OP: Better AI prompts for translating and polishing localizations 
- [X] 修复：修复未在必要时渲染切分后的图片的问题 / FIX: Fix an issue that images are not rendered with expected slicing if necessary
- [X] 修复：修复某些场合未忽略字符串大小写的问题 / FIX: Fix an issue that string case is not ignored in some situations
- [X] 修复：修复与图表（Diagrams）相关的一些问题并优化性能 / FIX: Fix some issues related to diagrams, together with performance optimization
- [X] 新功能：适配上移/下移声明的功能，适用于CWT文件和脚本文件中的成员（封装变量、属性、值），以及本地化文件中的属性（即本地化条目）（入口：主菜单，点击`Code > Move Statement Up/Down`） / NEW: Support moving statements up/down, for members (scripted variables, properties, values) in cwt files and script files, and properties (aka localisation items) in localisation files. (Entry: Main menu, click `Code > Move Statement Up/Down`)
- [X] 新功能：提供本地化操作任务，用于从另一语言区域的本地化翻译为当前语言区域 / NEW: Provide the localisation manipulation task to translate localisations from another locale to current locale
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.1

- [X] #159 [CK3] Add file `lines.lines` to Paradox script file name patterns
- [X] #161 Using single alias for a type
- [X] #164 Plugin breaks Menus and Toolbars editor
- [X] 完善与Markdown的集成，涉及链接、内联代码、代码块等，详见[参考文档](https://windea.icu/Paradox-Language-Support/zh/extensions.html#md) / Complete integration with Markdown, including links, inline codes, code blocks, etc. See [reference document](https://windea.icu/Paradox-Language-Support/en/extensions.html#md) for details
- [X] 修复：修复与概念相关的一些问题 / FIX: Fix some issues related to concepts
- [X] 新功能：提供对规则文件中的类型、枚举、别名等的引用解析和查找用法的支持 / NEW: Provide support for reference parsing and finding usages of types, enums, aliases, etc. in config files
- [X] 新功能：支持Paradox CSV语言，提供各种必要的语言功能 / NEW: Support Paradox CSV language, and provide various necessary language features
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.0

- [X] #128 集成Tiger检查工具 / Integrate Tiger linting tools
- [X] #148 更改`date_field`的默认日期模式为`y.M.d`，且允许声明日期模式（`date_field[y.M.d]`） / Change the default date format of `date_field` to `y.M.d`, and allow declaring date formats (`date_field[y.M.d]`)
- [X] #151 Local variable, defined inside inline_script but passed from the outside as a parameter is not recognized.
- [X] #153 可以配置是否在注入的文件（如，参数值）中、内联脚本文件中忽略各种无法解析的引用的代码检查 / Can configure whether to ignore unresolved references inspections in injected files (e.g., parameter values) and inline script files
- [X] #154 [2.0] Issues with the Dev Build
- [X] 修复：修复插件可能无法正确解析json，从而无法识别游戏目录的问题 / FIX: Fix a bug that plugin may not be able to correctly parse json, causing the game directory to be incorrectly recognized
- [X] 修复：修复对复杂表达式进行代码补全时可能出现的NPE / FIX: Fix a NPE that may occur when performing code completion on a complex expression 
- [X] 优化：将插件的内部设置重构为`registryKey`，允许用户调整 / OP: Refactor the internal settings of the plugin to `registryKey`, allowing users to configure them
- [X] 优化：通过快速文档为本地化参数和命令的传入参数提供其中使用的格式标签的说明 / OP: Provide information about format tags used in arguments of localisation parameters and commands by quick documentation
- [X] 优化：为封装变量、复杂枚举值和动态值提供快速文档与内嵌提示，来自同名的本地化 / OP: Also provide quick documentation and inlay hints for scripted variables, complex enum values and dynamic values, from the same name localisation
- [X] 优化：可以配置是否在内嵌提示中显示定义的子类型 / Can configure whether to show subtypes of definitions in inlay hints
- [X] 优化：可以配置处理图片时使用的工具（默认且内置：Texconv，可选：Image Magick） / OP: Can configure the tool used to process images (Default and built-in: Texconv, Available: Image Magick)
- [X] 优化：可以配置翻译本地化文本时使用的工具（目前仅限Translation Plugin） / OP: Can configure the tool used to translate localisation text (Currently limited to Translation Plugin)
- [X] 优化：可以配置要启用的检查工具（目前仅限Tiger） / OP: Can configure which linting tools to enable (Currently limited to Tiger)
- [X] 优化：更好的对图片的支持，优化实现，支持预览与渲染DDS、TGA图片，提供不同图片格式（PNG、DDS、TGA）之间的相互转换的操作 / OP: Better support for images, optimize the implementation, support to preview and render DDS, TGA images, and provide actions to convert image formats (PNG, DDS, TGA)
- [X] 新功能：集成AI，提供对应的设置页面 / NEW: Integrate AI, provide corresponding settings page
- [X] 新功能：提供AI驱动的本地化翻译功能（意向） / NEW: Provide AI-driven localisation translation features (intentions)
- [X] 新功能：提供AI驱动的本地化润色功能（意向） / NEW: Provide AI-driven localisation polishing features (intentions)
- [X] 新功能：提供动作，用于批量操作本地化（翻译、润色等） / NEW: Provide actions for batch manipulation of localisations (translating, polishing, etc.)
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes
- 注意：建议在执行各类本地化操作之前，先将更改提交到VCS / NOTE: It is recommended to commit changes to VCS before performing various localisation manipulations

## 1.4.2

- [X] 修复：尝试避免某些特殊情况下的 SOF / FIX: Try to avoid SOF in some special cases
- [X] 优化：优化插件性能 / OP: Optimize plugin performance
- [X] 优化：进一步优化本地化文件的 lexer 和 parser 的实现 / OP: Further optimization for lexer and parser implementations of localisation files
- [X] 优化：区分出单独的规则与集成的设置页面 / OP: Move config related and integrations settings into separate settings pages
- [X] 优化：整理与完善本地化的复制、替换、生成、翻译等操作 / OP: Optimize and improve localisation related actions such as copying, replacing, generating and translating
- [X] 新功能：支持来自规则仓库的远程规则分组，允许配置仓库地址 / NEW: Support remote config groups from config repositories, and allow to configure repository urls
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.4.1

- [X] #143 cwt图片渲染规则待支持
- [X] #144 cwt规则的type_key_filter <> xxx失效
- [X] 修复：优化`PlsFileListener`，尝试避免某些特殊情况下的 SOF / FIX: Optimize `PlsFileListener` to avoid SOF in some special cases
- [X] 修复：修复封装变量引用的代码折叠规则失效的问题 / FIX: Fixed the problem that the code folding rule for scripted variable references is inactive
- [X] 优化：完善适用于规则文件的代码补全 / OP: Optimize code completion for config files
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.4.0

- [X] #94 优化CWT文件解析器的性能 / Optimize performance for CWT file parser
- [X] #137 [VIC3/CK3] Support special localizations - Basic support
- [X] #140 修复与本地化命令连接相关的一些问题（规则解析、代码导航等） / Fixed some problems about localisation command links (Config resolving, code navigation, etc.)
- [X] #141 [Stellaris] Indexing process should not rely on non-indexed file data
- [X] 通过懒解析CWT文件中的选项注释来尝试优化性能 / Try to optimize performance by lazily parsing option comments in CWT files
- [X] 通过懒解析本地化文件中的本地化文本来尝试优化性能与提高代码灵活性 / Try to optimize performance and improve code flexibility by lazily parsing localisation text in localisation files
- [X] 修复：本地化图标如果对应一个sprite，无法正常适用用法高亮 / FIX: Localisation icons could not be properly highlighted if it will be resolved to a sprite
- [X] 修复：本地化命令中的动态值无法查找使用，无法正常适用用法高亮 / Fix: Dynamic values in localisation commands could not find usages, could not be properly highlighted
- [X] 修复：修复关于本地化的语言区域的一些问题 / FIX: Fix some problems about localisation locales
- [X] 修复：修复某些场合下可能无法提示动态值的问题 / FIX: Fix a problem that dynamic values may not be completed in some cases
- [X] 优化：`icon[path]`现在优先匹配直接位于`path`下的图标 / OP: `icon[path]` now prefer to match icons directly under `path`
- [X] 优化：内嵌提示设置中的 iconHeightLimit 的默认值改为36 / OP: Change the default value of iconHeightLimit in inlay hint settings to 36
- [X] 优化：兼容job作为本地化命令连接的情况 / OP: Compatible with jobs as localisation command links
- [X] 优化：在必要时先尝试获取图标的原始高度 / OP: Try to get the original height of the icon first when necessary
- [X] 优化：提供扩展点以更加灵活地解析本地化图标 / OP: Provides EP to resolve localisation icons more flexibly
- [X] 优化：可以从复杂表达式节点所在位置导航到相关规则 / OP: Allow to navigate to related configs from the position of complex expression nodes
- [X] 新功能：支持解析本地化文本中的属性引用&命令的传入参数中的文本颜色ID为引用 / NEW: Support parsing text color ids in arguments of references & commands in localisation text as references
- [X] 新功能：新增代码检查，以在本地化文件中提示缺失指定的其他语言区域的本地化 / NEW: Add code inspection to prompt missing localisations for specified locales in localisation files
- [X] 新功能：新增代码检查，以提示不支持在内联脚本文件中使用参数条件块与带默认值的参数用法 / NEW: Add code inspection to prompt unsupported parameter condition blocks and parameter usages (with the default value) in inline script files
- [X] 新功能：[VIC3/CK3] 初步支持本地化文本中的文本格式（示例：`#v text#!`，其中`v`对应规则表达式`<text_format>`，`text`是富文本的组合） / NEW: [VIC3/CK3] Basic support for text formats in localisation text (e.g., `#v text#!`, where `v` corresponds to the config expression `<text_format>`, and `text` is a combination of rich text)
- [X] 新功能：[VIC3/CK3] 初步支持本地化文本中的文本图标（示例：`@icon!`，其中`icon`对应规则表达式`<text_icon>`） / NEW: [VIC3/CK3] Basic support for text icons in localisation text (e.g., `@icon!`, where `icon` corresponds to the config expression `<text_icon>`)
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.37

- [X] 完善Stellaris 4.0的CWT规则文件 / Optimize CWT config files for Stellaris 4.0
- [X] #139 修复某些情况下无法正常渲染与预览DDS图片的问题 / Fixed a problem that DDS images could not be properly rendered and previewed in some situations
- [X] 可以分别配置是否启用内置、全局本地、项目本地的规则分组 / Allow to configure whether to enable built-in, global-local, and project-local config groups separately
- [X] 修复无法显示text color的装订线图标（指示对应的颜色）的问题 / Fixed a problem that cannot show the gutter icon (implies corresponding color) for text colors
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.36

- [X] 更新CWT规则文件以适配Stellaris 4.0 / Update CWT config files to match Stellaris 4.0

## 1.3.35

- [X] 优化：可以配置是否在层级视图中显示位置信息与本地化后的名字 / OP: Allow to configure whether to show position info and localized name in hierarchy views
- [X] 实现高级类型层级视图，支持过滤、分组、展开等操作 / Implement advanced type hierarchy views, support operations such as filtering, grouping, expansion, etc.
- [X] 实现事件树与科技树对应的高级层级视图 / Implement advanced hierarchy views for event trees and technology trees
- [X] 可以配置是否在高级层级视图中显示相关信息（事件信息或科技信息） / Allow to configure whether to show related info in advanced type hierarchy views (event info or technology info)
- [X] 为封装变量、定义和本地化提供一些额外的意图操作 / Provide some extra intentions for scripted variables, definitions and localizations
- [X] 提供对本地化的相关定义的支持（快速文档、导航操作、装订线图标） / Provide support for related definitions of localisations (quick doc, navigation actions, gutter icons)
- [X] 修复某些插件配置在IDE重启后会被重置为默认的问题 / Fixed a problem that some plugin settings will be reset to default after IDE restart
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.34

- [X] 更新IDEA版本到2025.1 / Update IDEA version to 2025.1
- [X] 提供适配新UI的图标 / Provide icons that fit the new UI
- [X] 提供简体中文的本地化 / Provide localization for Simplified Chinese
- [X] 优化：有关重载的代码检查不应适用于匿名的定义 / OP: Code checks about overridden should not be applied to anonymous definitions
- [X] #138 修复与导航到重载目标的快速修正有关的一个问题 / Fixed a problem related to quick fixes for navigation to overridden targets
- [X] 修复依赖发生变化时，相关状态未正确刷新的问题 / Fixed the problem that related statuses are not refreshed correctly when dependencies changes
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.33

- [X] 更新VIC3的CWT规则文件，基于最新的文档 / Update CWT config files for VIC3, based on latest documentation
- [X] #136 [VIC3/CK3] Problems with ?= operator
- [X] 将插件的代码折叠设置合并到插件的设置页面与文件 / Merge plugin code folding settings into plugin settings page and file
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.32

- [X] #123 [CK3] Supports `type_key_prefix` in cwt configs, which is currently only used in ck3's `scripted_effects.cwt`
- [X] #129 更好的对模版表达式规则的支持 / Better template expression config support
- [X] #130 更好的对嵌套的块的支持 / Better support for nested blocks
- [X] #131 Only numbers in event names
- [X] #134 [Vic3] Mods are not correctly detected
- [X] 修复无法识别本地化文件中的数据库对象表达式的BUG / Fixed the bug that database object expression in loc files cannot be resolved
- [X] 如果游戏目录未配置，打开模组文件后显示编辑器横幅通知，而非显示全局通知 / If game directory is not configured, show an editor notification after opening mod files, instead of showing a global notification
- [X] 细化代码折叠配置 / Refine code folding configuration
- [X] 代码折叠：允许折叠连续的注释，可配置，默认不启用 / Code folding: Allow to fold continuous comments, configurable, disabled by default
- [X] 智能推断：允许快速推断规则上下文，可配置，默认不启用 / Smart inference: Allow to use quick inference for config context, configurable, disabled by default
- [X] 将内置规则重新移到主要的jar包中以免去不必要的麻烦 / Move built-in configs into main jar to make things easy
- [X] 其他优化与BUG修复 / Other optimizations and bug fixes

## 1.3.31

- [X] #121 Complex enums only accept `enum_name = scalar`, not `enum_name = bool` or `enum_name = int`
- [X] #122 Non-Stellaris games raise warning if modifier icon doesn't exist
- [X] #126 [CK3] Types inside other types do not register correctly
- [X] #127 Indexing Errors when working with Localizations
- [X] Optimize color settings for various color schemes
- [X] Optimize indexing performance

## 1.3.30

- [X] #115 优化对DDS图片的支持（基于[iTitus/dds](https://github.com/iTitus/dds)和[Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv)）

***

- [X] #115 Optimize support for DDS images (Based on [iTitus/dds](https://github.com/iTitus/dds) and [Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv))

## 1.3.29

- [X] #113 修复本地化悬浮工具栏不再显示的问题
- [X] #114 修复存在多个模组依赖时的一个有关解析作用域的问题
- [X] 其他优化与BUG修复

***

- [X] #113 Fixed a bug that the localisation floating toolbar was no longer displayed
- [X] #114 Fixed a bug about resolving scope when there are multiple mod dependencies
- [X] Other optimizations and bug fixes

## 1.3.28

- [X] 提供扩展点以更好地识别各种来源的游戏目录和模组目录（参见 #111）
- [X] 将内置规则统一移到插件压缩包中的单独的jar包中
- [X] #111 Support Victoria 3 metadata.json
- [X] 优化内联操作的逻辑
- [X] 其他优化与BUG修复

***

- [X] Provides EP to better identify game directories and mod directories from various sources (See #111)
- [X] Move built-in configs into individual jar package which is inside which is inside the plugin's zip package
- [X] #111 Support Victoria 3 metadata.json
- [X] Optimize the logic of inline operations
- [X] Other optimizations and bug fixes

## 1.3.27

- [X] #108 支持使用内联数学块作为封装变量的值
- [X] 优化脚本文件的索引逻辑，修复未索引不在顶层的封装变量的BUG

***

- [X] #108 Support using inline math block as scripted variable value
- [X] Optimize indexing logic for script files, fix a bug that non-root level local scripted variable are not indexed

## 1.3.26

- [X] #103 Victoria 3 has one more type of color: hsv360
- [X] #104 There is a problem with the new ?= operator when it is directly on a value.
- [X] 优化`IncorrectFileEncodingInspection` (参见 #102)
- [X] 优化规则匹配逻辑，以更准确地匹配与区分`scope_field`与`value_field`
- [X] 兼容在复杂表达式中使用内联的参数条件表达式（如，`value:xxx|P|[[P1]$V$]V|`）
- [X] 修复查询修正相关本地化时没有忽略大小写的问题
- [X] #101 Victoria 3 link Comparisons - 兼容传参格式的连接（如，`relations(root.owner)`）
- [X] #105 Read access is allowed from inside read-action only - 相关优化

***

- [X] #103 Victoria 3 has one more type of color: hsv360
- [X] #104 There is a problem with the new ?= operator when it is directly on a value.
- [X] Optimize `IncorrectFileEncodingInspection` (See #102)
- [X] Optimize rule match logic to match and differ `scope_field` and `value_field` more exactly
- [X] Compatible with inline parameter condition expressions in complex expressions (e.g., `value:xxx|P|[[P1]$V$]V|`)
- [X] Fixed the issue that case was not ignored when searching modifier-related localisations
- [X] #101 Victoria 3 link Comparisons - Compatible with argument-style links (e.g., `relations(root.owner)`)
- [X] #105 Read access is allowed from inside read-action only - Related optimizations

## 1.3.25

- [X] 兼容脚本文件中的内联模版表达式（如，`has_ethic = ethic_[[fanatic]fanatic_]pacifist`）
- [X] #96
- [X] #97 兼容VIC3中的定义（define）引用（对应新的规则类型`$define_reference`）
- [X] #100 删除`disableLogger`以免忽略某些重要的报错

***

- [X] Compatible with inline template expressions in script files (e.g., `has_ethic = ethic_[[fanatic]fanatic_]pacifist`)
- [X] #96
- [X] #97 Support Vic3 define references (corresponding to new data type `$define_reference`)
- [X] #100 Remove `disableLogger` to avoid ignoring some important errors.

## 1.3.24

- [X] 更新CWT规则文件以适配Stellaris 3.14.1
- [X] #93 允许在内联脚本文件中引用其使用位置的本地封装变量
- [X] #95 合并规则表达式时兼容`alias_name[x]`和`alias_keys_field[x]`
- [X] 其他优化与BUG修复

***

- [X] Update CWT config files to match Stellaris 3.14.1
- [X] #93 Allow to use local scripted variables from usage locations in inline script files
- [X] #95 When merge config expressions, support merge `alias_name[x]` and `alias_keys_field[x]`
- [X] Other optimizations and bug fixes

## 1.3.23

- [X] 修复无法补全封装变量（scripted variable）的问题
- [X] #90 排除特定的根目录以免解析与索引一些意外的文件
- [X] #92 可以配置是否用对应的颜色高亮本地化颜色ID（默认启用）
- [X] 修复渲染本地化文本时某些文本会被重复渲染的问题
- [X] 其他优化与BUG修复

***

- [X] Fix a problem that cannot complete scripted variables
- [X] #90 Exclude some specific root file paths to avoid parsing and indexing unexpected files
- [X] #92 Provide the configuration that whether to highlight localisation color ids by corresponding color (enabled by default)
- [X] Fixed a problem that some text will be rendered repeatedly when rendering localisation text
- [X] Other optimizations and bug fixes

## 1.3.22

- [X] #88 新的代码检查：`NonTriggeredEventInspection`
- [X] BUG修复：修复不正确地缓存了基于扩展的规则推断的参数的上下文规则的问题
- [X] 其他优化与BUG修复

***

- [X] #88 New code inspection: `NonTriggeredEventInspection`
- [X] BUG fix: fixed an issue that context configs for parameters inferred based on extended configs were cached incorrectly
- [X] Other optimizations and bug fixes

## 1.3.21

- [X] 更新CWT规则文件以适配Stellaris 3.13.0
- [X] 优化性能与内存占用
- [X] 其他优化与BUG修复

***

- [X] Update CWT config files to match Stellaris 3.13.0
- [X] Optimize performance and memory
- [X] Other optimizations and bug fixes

## 1.3.20

- [X] 适用于规则文件的代码补全，也适用于插件或者规则仓库中的CWT文件
- [X] 完善脚本文件与本地化文件的词法器与解析器，修复存在的一些问题
- [X] 完善对本地化命令的支持
- [X] 完善参数对应的规则的推断与合并的逻辑
- [X] 其他优化与BUG修复

***

- [X] Code completion for config files: also for CWT files in plugin or config repositories
- [X] Optimize lexer and parser for script and localisation fixes, fix some existing problems
- [X] Optimize support for localisation commands
- [X] Optimize inference and merging logic for configs of parameters
- [X] Other optimizations and bug fixes

## 1.3.19

- [X] 完善适用于规则文件的代码补全
- [X] #83 修复插件内置的VIC3的规则文件实际上是VIC2的规则文件的问题
- [X] #84 解析规则文件中的路径时需要移除"game/"路径前缀
- [X] #85 允许在声明规则的顶级使用`single_alias_right[x]`
- [X] #86 Date Validation and ?= does not work for Victoria 3

***

- [X] Optimize code completion for config files
- [X] #83 Fix an issue that builtin VIC3 config files are VIC2's
- [X] #84 Remove path prefix "game/" when resolve paths in config files
- [X] #85 Allow to use `single_alias_right[x]` on top level of declaration config
- [X] #86 Date Validation and ?= does not work for Victoria 3

## 1.3.18

- [X] 新功能：适用于规则文件的代码补全（初步支持）
- [X] #82 修复解析脚本文件时，会在特定情况下进入无限循环的问题
- [X] 修复与作用域解析相关的一些问题
- [X] 其他优化与BUG修复

***

- [X] New feature: Code completion for config files (initial support)
- [X] #82 Fix an issue that it will enter an infinite loop in specific situation when parsing script files.
- [X] Fix some problems about parsing scopes
- [X] Other optimizations and bug fixes

## 1.3.17

- BUG修复

***

- BUG fixes

## 1.3.16

- BUG修复

***

- BUG fixes

## 1.3.15

- [X] 正确渲染从数据库对象生成的概念的文本
- [X] 修复对于`param = "$param$"`，当可以推断`param`的规则上下文时，无法推断`$param$`的规则上下文的问题
- [X] 支持在数据库对象表达式中重复引用基础数据库对象，以在游戏中强制显示为非转换形式（如`authority:auth_oligarchic:auth_oligarchic`）
- [X] 其他优化与BUG修复

***

- [X] Render concepts from database objects correctly
- [X] Fix a problem that for `param = "$param$"`, when config context of `param` can be inferred, config context of `$param$` cannot be inferred
- [X] Supports referencing base database objects in database object expression repeatedly to force show non-swapped form in the game (e.g., `authority:auth_oligarchic:auth_oligarchic`)
- [X] Other optimizations and bug fixes

## 1.3.14

- BUG修复

***

- BUG fixes

## 1.3.13

- [X] 支持内联脚本块中的带参数的封装变量引用（如`k = @[ foo$p$bar + 1 ]`）
- [X] #56 支持数据库对象表达式以及从数据库对象生成的概念（如`civic:some_civic`）
- [X] 其他优化与BUG修复

***

- [X] Supports parameterized scripted variable references in inline math blocks (e.g., `k = @[ foo$p$bar + 1 ]`)
- [X] #56 Supports database object expression and concepts from database objects (e.g., `civic:some_civic`)
- [X] Other optimizations and bug fixes

## 1.3.12

- [X] #67 尝试推断在条件表达式以及内联数学块中使用过的参数的上下文规则
- [X] #67 考虑到`PARAM = $PARAM|no$`等同于`[[PARAM] PARAM = $PARAM$ ]`，为此添加一些额外的意向操作
- [X] #68 允许指定参数的规则上下文与作用域上下文，使其与参数上下文的保持一致（基于`## inherit`，详见参考文档）

***

- [X] #67 Try to infer context configs of parameters which are used in condition expression and inline math blocks
- [X] #68 Add some extra intention actions during to the equality of `PARAM = $PARAM|no$` and `[[PARAM] PARAM = $PARAM$ ]`
- [X] #68 Allow to specify a parameter's config context and scope context, to make them consistent with the parameter contexts (Base on `## inherit`, see reference documentation for details)

## 1.3.11

- [X] BUG修复：修复图表（如科技树）无法正常显示的问题
- [X] 优化：优化对游戏目录的判断

***

- [X] BUG fix: Fix a problem that diagrams (e.g., Technology Tree Diagram) cannot be opened correctly
- [X] Optimization: Optimize the logic to determine game directory

## 1.3.10

- [X] 新功能：支持全局的本地规则分组（可在插件配置页面中配置所在的目录）
- [X] 优化：在项目视图中显示规则分组的位置（作为外部库）

***

- [X] New feature: Support for global local config groups (Can be configured in plugin's settings page)
- [X] Optimization: Show locations of config groups in project view (as external libraries)

## 1.3.9

- [X] 更新CWT规则文件以适配Stellaris 3.12.3
- [X] 新功能：支持切换快速文档中的本地化使用的语言区域（如果可用，打开快速文档后，在右键菜单或者右下角更多菜单中，点击`Change Localisation Locale`）
- [X] #78 优化：支持通过扩展的CWT规则文件为动态值（如`event_target`）指定作用域上下文 - 提供更加完善的支持（详见参考文档）
- [X] 其他优化与BUG修复

***

- [X] Update CWT config files to match Stellaris 3.12.3
- [X] New feature: Supports to change localisation locale in quick documentation (If available, open quick documentation, then click `Change Localisation Locale` in Right Click Menu or More Menus in the bottom-right corner)
- [X] #78 Optimization: Support for specifying the scope context for dynamic values (e.g., `event_target`) via extended CWT configs - more perfect support (See reference documentation for details)
- [X] Other optimizations and bug fixes

## 1.3.8

- [X] 优化：优化查询，提高性能，修复有关重载的一些BUG
- [X] 优化：优化代码格式化功能，修复一些细节上的BUG
- [X] 优化：优化插件设置页面
- [X] 优化：涉及CWT选项注释与文档注释时，粘贴文本以及行注释/取消行注释能够得到正确的结果
- [X] 优化：允许折叠本地化文件中的本地化引用与本地化图标（完全折叠，可配置，默认不启用，搭配相关的内嵌提示使用）
- [X] 优化：允许折叠本地化文件中的本地化命令与本地化概念（可配置，默认不启用）
- [X] #72 也将`"$PARAM$"`中的`$PARAM$`识别为参数
- [X] #79 优化：匹配脚本内容与规则时，如果带参数的键的规则类型是唯一确定的，则需要继续向下匹配
- [X] #79 优化：匹配脚本内容与规则时，如果作为参数的键的规则类型可以（从扩展的CWT规则）推断出来且是匹配的，则需要继续向下匹配
- [X] #79 优化：如果`$scope$`表示一个作用域连接，也尝试（从扩展的CWT规则）推断其作用域上下文
- [X] #80 BUG修复：对于作用域的正确性的检查应当尽可能地适用
- [X] 其他优化与BUG修复

***

- [X] Optimization: Optimize search implementation, performance improvement, bug fixes about override
- [X] Optimization: Optimize code reformat implementation, bug fixes about details
- [X] Optimization: Optimize plugin settings page
- [X] Optimization: Paste, comment / uncomment with line comment now work correctly when CWT option comments or documentation comments are involved
- [X] Optimization: Now it's available to fold localisation references & icons in localisation files (fully folded, configurable, disabled by default, use with relevant inlay hints)
- [X] Optimization: Now it's available to fold localisation commands & concepts (configurable, disabled by default)
- [X] #72 Treat `$PARAM$` in `"$PARAM$"` as a parameter
- [X] #79 Optimization: When match script content with configs, if the config type of parameterized key can be determined uniquely, it's necessary to continue matching down
- [X] #79 Optimization: When match script content with configs, if the config type of parameterized key can be inferred and matched (via extended CWT configs), it's necessary to continue matching down
- [X] #79 Optimization: If `$scope$` represents a scope link, also try to infer its scope context (via extended CWT configs)
- [X] #80 Bug fix: Incorrect expression inspection for scope field expressions should be available wherever possible
- [X] Other optimizations and bug fixes

## 1.3.7

- [X] 更新CWT规则文件以适配Stellaris 3.12.2（基本完成）
- [X] 优化：优化代码格式化功能，修复一些细节上的BUG
- [X] 优化：基于CWT规则文件来确定目标（定义、本地化等）的覆盖顺序（可以自定义，参见参考文档）
- [X] 优化：对于本地化文件，本地化文本中的双引号不需要转义（直到本行最后一个双引号之前为止，视为本地化文本）
- [X] 优化：如果目标无法解析，但是存在对应的扩展的CWT规则，可以配置是否忽略相关的代码检查
- [X] 优化：如果可以从扩展的CWT规则文件推断作用域上下文，就不要再尝试从使用推断
- [X] 其他优化与BUG修复

***

- [X] Update CWT config files to match Stellaris 3.12.2 (almost done)
- [X] Optimization: Optimize code reformatting feature, fixes some bugs in details
- [X] Optimization: Determine override order for targets (definitions, localisations, etc.) based on CWT configs files (Can be customized, see reference documentation for details)
- [X] Optimization: For localisation file, it's unnecessary to escape double quotes in localisation text
- [X] Optimization: It a target cannot be resolved, but related extended CWT configs exist, related code inspection can be configured to be ignored
- [X] Optimization: If the scope context can be inferred from extended CWT configs, do not continue to be inferred from usages
- [X] Other optimizations and bug fixes

## 1.3.6

- [X] 在内置规则文件的编辑器通知中提供操作，点击可以生成或者导航到对应的项目本地的规则文件
- [X] #73 扩展CWT规则：支持扩展的数据类型`AntExpression`，用于匹配ANT路径表达式（详见参考文档）
- [X] #73 扩展CWT规则：支持扩展的数据类型`Regex`，用于匹配正则表达式（详见参考文档）
- [X] #73 扩展CWT规则：支持在`## context_key`的值中使用模版表达式（详见参考文档）
- [X] #73 扩展CWT规则：编写扩展的CWT规则时，可以通过字符串字面量以及模版表达式进行匹配的地方，现在也可以通过ANT路径表达式以及正则表达式进行匹配（详见参考文档）
- [X] #71 修复一个有关作用域上下文切换的问题
- [X] #74 修复一个有关索引以及引用解析的问题
- [X] #76 通过扩展的CWT规则提供上下文规则时，允许直接在顶级规则使用`single_alias`（示例：`extended_param = single_alias_right[trigger_clause]`）
- [X] 优化：如果需要，对于已打开的文件，其文件内容应当及时重新解析，其中的内嵌提示应当及时更新（注意这可能需要先在后台花费一定时间）
- [X] 优化：优化代码补全的用时

***

- [X] Provide an action in editor notification bar for builtin config files, click to generate (or navigate to) related project-local config file
- [X] #73 Extend CWT Config: Supports extended data type `AntExpression`, which is used to match ANT expression (see reference documentation for details)
- [X] #73 Extend CWT Config: Supports extended data type `Regex`, which is used to match regex (see reference documentation for details)
- [X] #73 Extend CWT Config: Supports for using template expressions for value of `## context_key` (see reference documentation for details)
- [X] #73 Extend CWT Config: When writing extended CWT configs, in places which can use string literals and template expressions, now can also use ANT expressions and regular expressions (see reference documentation for details)
- [X] #71 Fix a problem about scope context switching
- [X] #74 Fix a problem about indexing and reference resolving
- [X] #76 When provide context configs by extended configs, allows to use `single_alias` at top level directly (e.g. `extended_param = single_alias_right[trigger_clause]`)
- [X] Optimization: If necessary, for opened files, reparse its file context and refresh its inlay hints (Note that this may take some times in background first)
- [x] Optimization: Optimize time cost of code completion

## 1.3.5

- [X] 为操作“导航到相关的CWT规则”提供更加完善的支持（额外适用于封装变量、参数等）
- [X] 支持通过扩展的CWT规则为封装变量（`scripted_variable`）提供扩展的快速文档（详见参考文档）
- [X] 支持通过扩展的CWT规则为封装变量（`scripted_variable`）提供扩展的内嵌提示（详见参考文档）
- [X] #66 支持为一些目标额外提供基于扩展的CWT规则的代码补全（包括封装变量、定义、内联脚本、参数、复杂枚举值与动态值，可配置，默认不启用）
- [X] #69 支持通过扩展的CWT规则为一些目标指定作用域上下文（包括定义、参数与内联脚本，基于`## replace_scopes`和`## push_scope`，忽略选项不合法的情况）
- [X] #70 提供内联脚本调用的代码补全（`inline_script = ...`，可配置，默认不启用，目前适用于所有游戏类型）
- [X] 添加代码检查：未使用的内联脚本（弱警告级别）
- [X] 其他优化与BUG修复

***

- [X] Improved support for the action "Goto to related CWT configs" (For scripted variables, parameters, etc.)
- [X] Support for providing extended quick documentation for scripted variables via extended CWT configs (see reference documentation for details)
- [X] Support for providing extended inlay hints for scripted variables via extended CWT configs (see reference documentation for details)
- [X] #66 Support for providing additional code completion for various targets via extended CWT configs (For scripted variables, definitions, inline scripts, parameters, complex enum values and dynamic values; Configurable; Disabled by default)
- [X] #69 Support for specifying the scope context for various targets via extended CWT configs (For definitions, parameters and inline scripts; Via `## replace_scopes` and `## push_scope`; Ignore invalid situations)
- [X] #70 Provide code completion for inline script invocations (`inline_script = ...`; Configurable; Disabled by default; Currently for all game types)
- [X] New code inspection: Unused inline scripts (level: weak warning)
- [X] Other optimizations and bug fixes

## 1.3.4

- [X] 为操作“导航到相关的CWT规则”提供更加完善的支持
- [X] 支持通过扩展的CWT规则为复杂枚举值（如`component_tag`）提供扩展的快速文档（详见参考文档）
- [X] 支持通过扩展的CWT规则为动态值（如`variable`）提供扩展的快速文档（变更了对应CWT规则的格式，详见参考文档）
- [X] 支持通过扩展的CWT规则为复杂枚举值（如`component_tag`）提供扩展的内嵌提示（详见参考文档）
- [X] 支持通过扩展的CWT规则为动态值（如`variable`）提供扩展的内嵌提示（详见参考文档）
- [X] 添加代码检查：重载的文件（弱警告级别，默认不开启）
- [X] 修复当CWT规则涉及`single_alias`时，应用代码补全后，不会正确地自动插入` = {}`的问题
- [X] 尝试修复涉及内联脚本与适用语言注入的参数时，IDE可能卡死的问题

***

- [X] Improved support for the action "Goto to related CWT configs"
- [X] Support for providing extended quick documentation for complex enum values (e.g. `component_tag`) via extended CWT configs (see reference documentation for details)
- [X] Support for providing extended quick documentation for dynamic values (e.g. `variable`) via extended CWT configs (format of relevant CWT configs is changed, see reference document for details)
- [X] Support for providing extended inlay hints for complex enum values (e.g. `component_tag`) via extended CWT configs (see reference documentation for details)
- [X] Support for providing extended inlay hints for dynamic values (e.g. `variable`) via extended CWT configs (see reference documentation for details)
- [X] New code inspection: Overridden for files (level: weak warning, enabled by default: no)
- [X] Fixed an issue that `= {}` would not be inserted correctly when applying code completion for script snippets matching CWT config of `single_alias`
- [X] Try to fix an issue that when inline scripts and parameters (with language injection) are involved, IDE may be freezing.

## 1.3.3

- [X] 更新CWT规则文件以适配Stellaris 3.11.1（基本完成）
- [X] 完善对作用域上下文的支持
- [X] 提供汇总的规则文件的项目视图
- [X] 添加代码检查：重载的封装变量（scripted_variable）（弱警告级别，默认不开启）
- [X] 添加代码检查：重载的定义（弱警告级别，默认不开启）
- [X] BUG修复：对于作用域切换，`prev.prev`应当等同于`this`，而非`prevprev`
- [X] BUG修复：对于本地化文件，本地化文本中的双引号不需要转义（直到本行最后一个双引号之前为止，视为本地化文本）
- [X] BUG修复：修复在插件新增的项目视图中无法正确选中已打开的文件的问题
- [X] 其他优化与BUG修复

## 1.3.2

- [X] 完善对全局的默认游戏目录的配置的支持
- [X] 优化CWT规则的匹配逻辑

## 1.3.1

- [X] 为菜单`Code -> Unwrap/Remove...`提供更多可选择的操作
- [X] 修复插件可能无法正确解析新添加的本地化在脚本文件中的引用的问题
- [X] 为操作“导航到相关的CWT规则”提供更加完善的支持
- [X] 支持通过扩展的CWT规则文件为参数指定CWT规则上下文
- [X] 支持通过扩展的CWT规则文件为内联脚本指定CWT规则上下文
- [X] 可以在插件配置中配置全局的默认游戏目录

## 1.3.0

- [X] 兼容IDEA 233，避免一些IDE启动时的报错
- [X] 支持通过扩展的CWT规则文件为定义（如`event`）提供扩展的快速文档
- [X] 支持通过扩展的CWT规则文件为动态值（如`event_target`）提供扩展的快速文档
- [X] 支持通过扩展的CWT规则文件为定义（如`event`）指定作用域上下文
- [X] 支持通过扩展的CWT规则文件为动态值（如`event_target`）指定作用域上下文
- [X] 支持通过扩展的CWT规则文件为参数提供扩展的快速文档
- [X] 完善对脚本文件和本地化文件中的转义字符的支持
- [X] #55 支持在多行的脚本参数值中使用内联脚本
- [X] #58 将条件参数加入参数上下文，用于代码补全、快速文档等功能
- [X] #59 避免在获取上下文规则列表时递归加载缓存

## 1.2.6

- [X] 优化为脚本文件提供的关键字的代码补全
- [X] 优化为脚本文件提供的参数值的代码补全
- [X] 修复无法正常支持整行或多行的脚本参数的参数值的问题

## 1.2.5

- [X] 修复覆盖了`economic_category`之后，无法正常解析因覆盖而新生成的修正的问题
- [X] 优化索引查询的逻辑 - 预先经过必要的排序、过滤和去重
- [X] 其他优化与BUG修复

## 1.2.4

- [X] 更新CWT规则文件以适配Stellaris 3.10.0（进一步完善）
- [X] 优化：在CWT规则的快速文档中显示CWT规则文件信息
- [X] BUG修复：修复无法渲染内嵌提示中的图标的问题
- [X] 其他优化与BUG修复

## 1.2.3

- [X] 更新CWT规则文件以适配Stellaris 3.10.0（基本完成）
- [X] 优化与BUG修复

## 1.2.2

- [X] 支持内联`scripted_variable`（即封装变量）（`编辑器右键菜单 -> Refactor -> Inline...`）
- [X] 支持内联`scripted_trigger`和`scripted_effect` （`编辑器右键菜单 -> Refactor -> Inline...`，仅限作为调用的引用）
- [X] 支持内联本地化（`编辑器右键菜单 -> Refactor -> Inline...`，仅限本地化文本中的引用）
- [X] 支持内联`inline_script`（即内联脚本）（`编辑器右键菜单 -> Refactor -> Inline...`）
- [X] 可以从项目视图或者模组依赖配置页面中的游戏或模组根目录打开其本地目录或者Steam创意工坊页面，以及复制对应路径/URL
- [X] 可以在工具菜单（`编辑器右键菜单 -> Paradox Language Support`）打开和复制数种路径/URL
- [X] （仅限Stellaris）支持表达式`technology@level` -
  参见：[\[Stellaris\] Could support tech@level grammar? · Issue #58](https://github.com/cwtools/cwtools-vscode/issues/58)
- [X] 其他优化与BUG修复

## 1.2.1

- [X] 支持语法`@a = @[ 1 + 2 ]` - 参见：[The tool cannot recognize in-script flag variables (Vic3) · Issue #76)](https://github.com/cwtools/cwtools-vscode/issues/76)
- [X] （仅限VIC3）支持操作符`?=` - 参见：[Parsing issues in Vic3 · Issue #53](https://github.com/cwtools/cwtools/issues/53)
- [X] 其他优化与BUG修复

## 1.2.0

- [X] 优化对[规则分组](https://windea.icu/Paradox-Language-Support/zh/config.html#config-group)的支持
- [X] 其他优化与BUG修复

## 1.1.13

- [X] BUG修复

## 1.1.12

- [X] 尝试优化插件性能 - 尝试优化编制索引时的速率与内存使用

## 1.1.11

- [X] 修复 #51 - 无法解析使用了scripted_variables的参数预设值
- [X] 优化 #30 - 兼容脚本文件的高级插值语法（`a[[b]c]d$e|f$g`）

## 1.1.10

- [X] 修复进行代码提示时列出的提示项可能不全的问题
- [X] 其他优化与BUG修复

## 1.1.9

- [X] 更新CWT规则文件以适配Stellaris 3.9.1
- [X] 其他优化与BUG修复

## 1.1.8

- [X] #50 优化：完善对修正的名字和描述的本地化，以及图标的图片支持
- [X] 优化进行代码补全时提示项的排序
- [X] 优化代码检查
- [X] 其他优化与BUG修复

## 1.1.7

- [X] 优化：生成本地化时，也可以基于脚本文件中无法解析的本地化（适用于对应的快速修复和生成操作）
- [X] 优化：重命名定义时，也重命名相关的本地化和图片的引用，以及由其生成的修正的引用，以及这些修正的相关本地化和图片的引用（可以配置并且默认启用）
- [X] 优化：提示封装变量和本地化时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限 -> 改为仅限本地化
- [X] 优化：如果需要，渲染图片时，先按照正确的帧数和总帧数切分原始图片
- [X] 尝试优化插件性能 - 避免在编制索引时访问索引（例如，如果编制索引时需要匹配定义，只要格式上匹配，就直接认为匹配）
- [X] 尝试优化插件性能 - 优化规则对象的内存占用
- [X] 其他优化与BUG修复

## 1.1.6

- [X] 修复`some_scripted_trigger`可能被插件认为同时匹配`<scripted_trigger>`和`scope_field`的问题（以及类似问题）
- [X] 优化对嵌套的定义的支持（如，`swapped_civic`）
- [X] 优化 #48 - 提示封装变量和本地化时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限
- [X] 尝试优化插件性能

## 1.1.5

- [X] 更新IDEA版本到2023.2
- [X] 匹配规则时如果发生索引异常，防止IDE报错
- [X] 修复DDS图片无法正确渲染的问题
- [X] 修复更新到IDEA 2023.2后，按住Ctrl并点击参数（以及其他类似目标）后，无法查找使用的问题
- [X] 修复更新到IDEA 2023.2后，无法打开事件树和科技树图表的问题
- [X] 修复 #47 - Parameters with defaults passed to script values cause highlight errors
- [X] 优化：检查定义的相关本地化和图片是否缺失时，也检查由其生成的修正的相关本地化和图片是否缺失（可以配置并且默认不启用）
- [X] 优化：生成本地化时，可以配置需要生成哪些本地化（参考重载/实现方法时弹出的对话框）

## 1.1.4

- [X] 修复构建的事件树和科技树中没有任何内容的问题
- [X] 尝试优化插件的性能 - 极大优化了进行查询和代码检查时的性能
- [X] 尝试优化插件的内存占用

## 1.1.3

- [X] 兼容带有参数的相关本地化和相关图片（不进行进一步的解析，进行代码检查时也不报错）
- [X] 支持渲染嵌入到本地化文本中的任意大小的图片（DDS/PNG）
- [X] 修复构建科技树图时报错的问题
- [X] 修复无法查询自身为文件的定义的问题
- [X] 进行代码补全时，如果需要，自动补上双引号
- [X] 修复可能无法解析顶层的作用域切换中的表达式对应的规则的问题（`some_scripted_effect = { root = { ... } }`）
- [X] 如果可能，尝试推断模组的游戏类型（例如，当模组目录位于对应游戏的创意工坊目录下时）（如果可以推断，就不允许通过模组配置更改）
- [X] 尝试使用语言注入功能推断脚本参数的传入值对应的CWT规则上下文，从而提供高级语言功能（待完善）
- [X] 尝试使用语言注入功能推断脚本参数的默认值对应的CWT规则上下文，从而提供高级语言功能（待完善）
- [X] 其他优化和BUG修复

## 1.1.2

- [X] 优化相关本地化和图片的CWT规则的解析逻辑：默认视为必须的，加上`## optional`后视为可选的
- [X] 优化相关本地化和图片的CWT规则的解析逻辑：对于`name = X`中的`X`，如果包含占位符`$`
  ，将占位符替换成定义的名字后，尝试得到对应名字的本地化或者对应路径的图片，否则尝试得到对应名字的属性的值对应的本地化或者图片
- [X] 优化相关图片的CWT规则的解析逻辑：对于`name = X`中的`X`，如果包含占位符`$`并且以`GFX_`
  开头，将占位符替换成定义的名字后，尝试得到对应名字的sprite定义对应的图片
- [X] 优化相关本地化的CWT规则的解析逻辑：对于`name = "$|u"`，将占位符替换成定义的名字且全部转为大写后，尝试得到对应名字的本地化或者对应路径的图
- [X] 完善CWT规则 - 清理和完善基本的相关本地化、相关图片的规则
- [X] 其他优化与BUG修复

## 1.1.1

- [X] 修复 #36 - 修复方法`isSamePosition`的实现中的问题
- [X] 修复 #38 - value_field和variable_field应当可接受`-$PARAM$`
- [X] 修复 #39 - 允许`$SCOPE|this$.modifier:xxx`
- [X] 修复 #44 - 修复可能无法正确解析嵌套的定义声明中的内容的问题
- [X] 修复更改游戏类型后无法打开模组配置的BUG
- [X] 更新CWT规则
- [X] 其他BUG修复

## 1.1.0

- [X] 修复 #26 P1 - 应当先尝试将`k = {...}`解析为内联脚本使用，再解析为定义声明
- [X] 修复 #32 - 重命名内联脚本的文件名时，相关缓存未正确刷新
- [X] 修复 #33 - 重命名内联脚本的文件名时，相关的表达式未正确更新文本
- [X] 添加代码检查：未正确进行重载的全局封装变量（例如，游戏文件`test.txt`中声明了一个全局封装变量`@v`
  ，而模组在文件`zzz_test.txt`中进行了重载）
- [X] 添加代码检查：未正确进行重载的定义 （例如，游戏文件`test.txt`中声明了一个事件`e.1`，而模组在文件`zzz_test.txt`中进行了重载）
- [X] 兼容直接在脚本文件中使用内联脚本，而非必须在定义声明中（或者内联后在定义声明中）
- [X] 尝试在更多情况下推断脚本参数对应的CWT规则（上下文）

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

## 1.0.8

- [X] 提供一种项目视图，用于显示合并后的所有游戏和模组文件（`Project Pane -> Paradox Files`）
- [X] 初步提供对覆盖顺序的支持，用于在查询文件、封装变量、定义、本地化时基于覆盖进行排序（除了代码补全等需要逐步遍历的地方）
- [X] 修复可能无法从字符串常量规则和枚举值规则反向查找在脚本文件中的使用的问题
- [X] 修复DIFF视图中没有正确重载文件类型的问题
- [X] 修复在输入本地化引用时（以及其他类型情况）可能因为读写锁冲突导致IDE卡死的问题
- [X] 修复在进行代码补全时可能不会提示某些应当匹配的本地化的问题
- [X] 修复初次打开项目时某些文件路径引用可能会被显示无法解析的问题
- [X] 优化：也可以在本地化引用上调用复制本地化文本的意向
- [X] 尝试优化性能
- [X] 其他优化和BUG修复

## 1.0.7

- [X] 修复 #26 P2 - 在脚本文件中，应当允许多行的用双引号括起的字符串
- [X] 修复 #28 - 生成科技树时报错
- [X] 修复 #29 - 应当允许在SV表达式（以及其他各种复杂表达式）中为参数指定默认值
- [X] 优化基于本地化后的名字进行代码补全时的性能
- [X] 尝试优化性能

## 1.0.6

- [X] 改为仅在必要时才对一些索引使用懒加载（如需要内联的情况）
- [X] 不再推断`static_modifier`的作用域上下文（可以不匹配，同`modifier`和`scripted_modifier`）
- [X] 尝试优化性能

## 1.0.5

- [X] 完善对定义的作用域上下文推断的支持 - 完善实现逻辑，优化性能，支持更多的定义类型
- [X] 改为默认不启用作用域上下文推断（因为可能性能不佳）
- [X] 重新将一些索引改为懒加载（因为它们在索引时可能需要访问其他索引，从而可能导致无法避免的报错）
- [X] 尝试优化性能 - 优化解析脚本参数和本地化参数时的性能
- [X] 其他优化和BUG修复

## 1.0.4

- [X] 尝试优化性能：如果可行，直接在索引时获取定义的子类型
- [X] 尝试优化性能：`ParadoxValueSetValueFastIndex` - 改为使用`FileBasedIndex`（在项目启动时就完成索引，避免之后卡住文件解析和代码检查）
- [X] 尝试优化性能：`ParadoxComplexEnumValueIndex` - 改为使用`FileBasedIndex`（在项目启动时就完成索引，避免之后卡住文件解析和代码检查）
- [X] 修复 #25 空指针
- [X] 更新CWT规则文件以适配Stellaris 3.8.3

## 1.0.3

- [X] 尝试优化内存占用
- [X] 尝试优化性能

## 1.0.2

- [X] 尝试优化性能
- [X] 其他优化和BUG修复

## 1.0.1

- [X] 修复代码检查`UnusedValueSetValueInspection`和`UnsetValueSetValueInspection`运行不正确的问题
- [X] 修复初次打开项目时可能无法正确解析各种复杂表达式的问题
- [X] 索引valueSetValue（如，`event_target`）时，也包含仅在本地化文件中使用到的`event_target`和`variable`
- [X] 其他优化和BUG修复

## 1.0.0

- [X] 
  提示定义和修正时，也可以基于它们的本地化后的名字进行提示。（在插件配置页面中勾选`Code Completion > Complete by localized names`
  以启用此功能）
- [X] 尝试基于在其他事件中的调用推断事件的作用域上下文（如果推断发生递归或者存在冲突则取消推断）
- [X] 完善Stellaris的CWT规则文件 - 完善`localisation.cwt`
- [X] 尝试优化性能
- [X] 其他优化和BUG修复
