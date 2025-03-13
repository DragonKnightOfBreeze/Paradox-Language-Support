# Changelog

## 1.3.32

* [ ] #123 [CK3] Supports `type_key_prefix` in cwt configs, which is currently only used in ck3's `scripted_effects.cwt`

## 1.3.31

* [X] #121 Complex enums only accept `enum_name = scalar`, not `enum_name = bool` or `enum_name = int`
* [X] #122 Non-Stellaris games raise warning if modifier icon doesn't exist
* [X] #126 [CK3] Types inside other types do not register correctly
* [X] #127 Indexing Errors when working with Localizations
* [X] Optimize color settings for various color schemes
* [X] Optimize indexing performance

## 1.3.30

* [X] #115 优化对DDS图片的支持（基于[iTitus/dds](https://github.com/iTitus/dds)和[Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv)）

***

* [X] #115 Optimize support for DDS images (Based on [iTitus/dds](https://github.com/iTitus/dds) and [Texconv](https://github.com/microsoft/DirectXTex/wiki/Texconv))

## 1.3.29

* [X] #113 修复本地化悬浮工具栏不再显示的问题
* [X] #114 修复存在多个模组依赖时的一个有关解析作用域的问题
* [X] 其他优化与BUG修复

***

* [X] #113 Fixed a bug that the localisation floating toolbar was no longer displayed
* [X] #114 Fixed a bug about resolving scope when there are multiple mod dependencies
* [X] Other optimizations and bug fixes

## 1.3.28

* [X] 提供扩展点以更好地识别各种来源的游戏目录和模组目录（参见 #111）
* [X] 将内置规则统一移到插件压缩包中的单独的jar包中
* [X] #111 Support Victoria 3 metadata.json
* [X] 优化内联操作的逻辑
* [X] 其他优化与BUG修复

***

* [X] Provides EP to better identify game directories and mod directories from various sources (See #111)
* [X] Move built-in configs into individual jar package which is inside which is inside the plugin's zip package
* [X] #111 Support Victoria 3 metadata.json
* [X] Optimize the logic of inline operations
* [X] Other optimizations and bug fixes

## 1.3.27

* [X] #108 支持使用内联数学块作为封装变量的值
* [X] 优化脚本文件的索引逻辑，修复未索引不在顶层的封装变量的BUG

***

* [X] #108 Support using inline math block as scripted variable value
* [X] Optimize indexing logic for script files, fix a bug that non-root level local scripted variable are not indexed

## 1.3.26

* [X] #103 Victoria 3 has one more type of color: hsv360
* [X] #104 There is a problem with the new ?= operator when it is directly on a value.
* [X] 优化`IncorrectFileEncodingInspection` (参见 #102)
* [X] 优化规则匹配逻辑，以更准确地匹配与区分`scope_field`与`value_field`
* [X] 兼容在复杂表达式中使用内联的参数条件表达式（如，`value:xxx|P|[[P1]$V$]V|`）
* [X] 修复查询修正相关本地化时没有忽略大小写的问题
* [X] #101 Victoria 3 link Comparisons - 兼容传参格式的连接（如，`relations(root.owner)`）
* [X] #105 Read access is allowed from inside read-action only - 相关优化

***

* [X] #103 Victoria 3 has one more type of color: hsv360
* [X] #104 There is a problem with the new ?= operator when it is directly on a value.
* [X] Optimize `IncorrectFileEncodingInspection` (See #102)
* [X] Optimize rule match logic to match and differ `scope_field` and `value_field` more exactly
* [X] Compatible with inline parameter condition expressions in complex expressions (e.g., `value:xxx|P|[[P1]$V$]V|`)
* [X] Fixed the issue that case was not ignored when searching modifier-related localisations
* [X] #101 Victoria 3 link Comparisons - Compatible with argument-style links (e.g., `relations(root.owner)`)
* [X] #105 Read access is allowed from inside read-action only - Related optimizations

## 1.3.25

* [X] 兼容脚本文件中的内联模版表达式（如，`has_ethic = ethic_[[fanatic]fanatic_]pacifist`）
* [X] #96
* [X] #97 兼容VIC3中的定义（define）引用（对应新的规则类型`$define_reference`）
* [X] #100 删除`disableLogger`以免忽略某些重要的报错

***

* [X] Compatible with inline template expressions in script files (e.g., `has_ethic = ethic_[[fanatic]fanatic_]pacifist`)
* [X] #96
* [X] #97 Support Vic3 define references (corresponding to new data type `$define_reference`)
* [X] #100 Remove `disableLogger` to avoid ignoring some important errors.

## 1.3.24

* [X] 更新CWT规则文件以适配Stellaris 3.14.1
* [X] #93 允许在内联脚本文件中引用其使用位置的本地封装变量
* [X] #95 合并规则表达式时兼容`alias_name[x]`和`alias_keys_field[x]`
* [X] 其他优化与BUG修复

***

* [X] Update CWT configs files to match Stellaris 3.14.1
* [X] #93 Allow to use local scripted variables from usage locations in inline script files
* [X] #95 When merge config expressions, support merge `alias_name[x]` and `alias_keys_field[x]`
* [X] Other optimizations and bug fixes

## 1.3.23

* [X] 修复无法补全封装变量（scripted variable）的问题
* [X] #90 排除特定的根目录以免解析与索引一些意外的文件
* [X] #92 可以配置是否用对应的颜色高亮本地化颜色ID（默认启用）
* [X] 修复渲染本地化文本时某些文本会被重复渲染的问题
* [X] 其他优化与BUG修复

***

* [X] Fix a problem that cannot complete scripted variables
* [X] #90 Exclude some specific root file paths to avoid parsing and indexing unexpected files
* [X] #92 Provide the configuration that whether to highlight localisation color ids by corresponding color (enabled by default)
* [X] Fixed a problem that some text will be rendered repeatedly when rendering localisation text
* [X] Other optimizations and bug fixes

## 1.3.22

* [X] #88 新的代码检查：`NonTriggeredEventInspection`
* [X] BUG修复：修复不正确地缓存了基于扩展的规则推断的参数的上下文规则的问题
* [X] 其他优化与BUG修复

***

* [X] #88 New code inspection: `NonTriggeredEventInspection`
* [X] BUG fix: fixed an issue that context configs for parameters inferred based on extended configs were cached incorrectly
* [X] Other optimizations and bug fixes

## 1.3.21

* [X] 更新CWT规则文件以适配Stellaris 3.13.0
* [X] 优化性能与内存占用
* [X] 其他优化与BUG修复

***

* [X] Update CWT configs files to match Stellaris 3.13.0
* [X] Optimize performance and memory
* [X] Other optimizations and bug fixes

## 1.3.20

* [X] 适用于规则文件的代码补全，也适用于插件或者规则仓库中的CWT文件
* [X] 完善脚本文件与本地化文件的词法器与解析器，修复存在的一些问题
* [X] 完善对本地化命令的支持
* [X] 完善参数对应的规则的推断与合并的逻辑
* [X] 其他优化与BUG修复

***

* [X] Code completion for config files: also for CWT files in plugin or config repositories
* [X] Optimize lexer and parser for script and localisation fixes, fix some existing problems
* [X] Optimize support for localisation commands
* [X] Optimize inference and merging logic for configs of parameters
* [X] Other optimizations and bug fixes

## 1.3.19

* [X] 完善适用于规则文件的代码补全
* [X] #83 修复插件内置的VIC3的规则文件实际上是VIC2的规则文件的问题
* [X] #84 解析规则文件中的路径时需要移除"game/"路径前缀
* [X] #85 允许在声明规则的顶级使用`single_alias_right[x]`
* [X] #86 Date Validation and ?= does not work for Victoria 3

***

* [X] Optimize code completion for config files
* [X] #83 Fix an issue that builtin VIC3 config files are VIC2's
* [X] #84 Remove path prefix "game/" when resolve paths in config files
* [X] #85 Allow to use `single_alias_right[x]` on top level of declaration config
* [X] #86 Date Validation and ?= does not work for Victoria 3

## 1.3.18

* [X] 新功能：适用于规则文件的代码补全（初步支持）
* [X] #82 修复解析脚本文件时，会在特定情况下进入无限循环的问题
* [X] 修复与作用域解析相关的一些问题
* [X] 其他优化与BUG修复

***

* [X] New feature: Code completion for config files (initial support)
* [X] #82 Fix an issue that it will enter an infinite loop in specific situation when parsing script files.
* [X] Fix some problems about parsing scopes
* [X] Other optimizations and bug fixes

## 1.3.17

* BUG修复

***

* BUG fixes

## 1.3.16

* BUG修复

***

* BUG fixes

## 1.3.15

* [X] 正确渲染从数据库对象生成的概念的文本
* [X] 修复对于`param = "$param$"`，当可以推断`param`的规则上下文时，无法推断`$param$`的规则上下文的问题
* [X] 支持在数据库对象表达式中重复引用基础数据库对象，以在游戏中强制显示为非转换形式（如`authority:auth_oligarchic:auth_oligarchic`）
* [X] 其他优化与BUG修复

***

* [X] Render concepts from database objects correctly
* [X] Fix a problem that for `param = "$param$"`, when config context of `param` can be inferred, config context of `$param$` cannot be inferred
* [X] Supports referencing base database objects in database object expression repeatedly to force show non-swapped form in the game (e.g., `authority:auth_oligarchic:auth_oligarchic`)
* [X] Other optimizations and bug fixes

## 1.3.14

* BUG修复

***

* BUG fixes

## 1.3.13

* [X] 支持内联脚本块中的带参数的封装变量引用（如`k = @[ foo$p$bar + 1 ]`）
* [X] #56 支持数据库对象表达式以及从数据库对象生成的概念（如`civic:some_civic`）
* [X] 其他优化与BUG修复

***

* [X] Supports parameterized scripted variable references in inline math blocks (e.g., `k = @[ foo$p$bar + 1 ]`)
* [X] #56 Supports database object expression and concepts from database objects (e.g., `civic:some_civic`)
* [X] Other optimizations and bug fixes

## 1.3.12

* [X] #67 尝试推断在条件表达式以及内联数学块中使用过的参数的上下文规则
* [X] #67 考虑到`PARAM = $PARAM|no$`等同于`[[PARAM] PARAM = $PARAM$ ]`，为此添加一些额外的意向操作
* [X] #68 允许指定参数的规则上下文与作用域上下文，使其与参数上下文的保持一致（基于`## inherit`，详见参考文档）

***

* [X] #67 Try to infer context configs of parameters which are used in condition expression and inline math blocks
* [X] #68 Add some extra intention actions during to the equality of `PARAM = $PARAM|no$` and `[[PARAM] PARAM = $PARAM$ ]`
* [X] #68 Allow to specify a parameter's config context and scope context, to make them consistent with the parameter contexts (Base on `## inherit`, see reference documentation for details)

## 1.3.11

* [X] BUG修复：修复图表（如科技树）无法正常显示的问题
* [X] 优化：优化对游戏目录的判断

***

* [X] BUG fix: Fix a problem that diagrams (e.g., Technology Tree Diagram) cannot be opened correctly
* [X] Optimization: Optimize the logic to determine game directory

## 1.3.10

* [X] 新功能：支持全局的本地规则分组（可在插件配置页面中配置所在的目录）
* [X] 优化：在项目视图中显示规则分组的位置（作为外部库）

***

* [X] New feature: Support for global local config groups (Can be configured in plugin's settings page)
* [X] Optimization: Show locations of config groups in project view (as external libraries)

## 1.3.9

* [X] 更新CWT规则文件以适配Stellaris 3.12.3
* [X] 新功能：支持切换快速文档中的本地化使用的语言区域（如果可用，打开快速文档后，在右键菜单或者右下角更多菜单中，点击`Change Localisation Locale`）
* [X] #78 优化：支持通过扩展的CWT规则文件为动态值（如`event_target`）指定作用域上下文 - 提供更加完善的支持（详见参考文档）
* [X] 其他优化与BUG修复

***

* [X] Update CWT configs files to match Stellaris 3.12.3
* [X] New feature: Supports to change localisation locale in quick documentation (If available, open quick documentation, then click `Change Localisation Locale` in Right Click Menu or More Menus in the bottom-right corner)
* [X] #78 Optimization: Support for specifying the scope context for dynamic values (e.g., `event_target`) via extended CWT configs - more perfect support (See reference documentation for details)
* [X] Other optimizations and bug fixes

## 1.3.8

* [X] 优化：优化查询，提高性能，修复有关重载的一些BUG
* [X] 优化：优化代码格式化功能，修复一些细节上的BUG
* [X] 优化：优化插件设置页面
* [X] 优化：涉及CWT选项注释与文档注释时，粘贴文本以及行注释/取消行注释能够得到正确的结果
* [X] 优化：允许折叠本地化文件中的本地化引用与本地化图标（完全折叠，可配置，默认不启用，搭配相关的内嵌提示使用）
* [X] 优化：允许折叠本地化文件中的本地化命令与本地化概念（可配置，默认不启用）
* [X] #72 也将`"$PARAM$"`中的`$PARAM$`识别为参数
* [X] #79 优化：匹配脚本内容与规则时，如果带参数的键的规则类型是唯一确定的，则需要继续向下匹配
* [X] #79 优化：匹配脚本内容与规则时，如果作为参数的键的规则类型可以（从扩展的CWT规则）推断出来且是匹配的，则需要继续向下匹配
* [X] #79 优化：如果`$scope$`表示一个作用域连接，也尝试（从扩展的CWT规则）推断其作用域上下文
* [X] #80 BUG修复：对于作用域的正确性的检查应当尽可能地适用
* [X] 其他优化与BUG修复

***

* [X] Optimization: Optimize search implementation, performance improvement, bug fixes about override
* [X] Optimization: Optimize code reformat implementation, bug fixes about details
* [X] Optimization: Optimize plugin settings page
* [X] Optimization: Paste, comment / uncomment with line comment now work correctly when CWT option comments or documentation comments are involved
* [X] Optimization: Now it's available to fold localisation references & icons in localisation files (fully folded, configurable, disabled by default, use with relevant inlay hints)
* [X] Optimization: Now it's available to fold localisation commands & concepts (configurable, disabled by default)
* [X] #72 Treat `$PARAM$` in `"$PARAM$"` as a parameter
* [X] #79 Optimization: When match script content with configs, if the config type of parameterized key can be determined uniquely, it's necessary to continue matching down
* [X] #79 Optimization: When match script content with configs, if the config type of parameterized key can be inferred and matched (via extended CWT configs), it's necessary to continue matching down
* [X] #79 Optimization: If `$scope$` represents a scope link, also try to infer its scope context (via extended CWT configs)
* [X] #80 Bug fix: Incorrect expression inspection for scope field expressions should be available wherever possible
* [X] Other optimizations and bug fixes

## 1.3.7

* [X] 更新CWT规则文件以适配Stellaris 3.12.2（基本完成）
* [X] 优化：优化代码格式化功能，修复一些细节上的BUG
* [X] 优化：基于CWT规则文件来确定目标（定义、本地化等）的覆盖顺序（可以自定义，参见参考文档）
* [X] 优化：对于本地化文件，本地化文本中的双引号不需要转义（直到本行最后一个双引号之前为止，视为本地化文本）
* [X] 优化：如果目标无法解析，但是存在对应的扩展的CWT规则，可以配置是否忽略相关的代码检查
* [X] 优化：如果可以从扩展的CWT规则文件推断作用域上下文，就不要再尝试从使用推断
* [X] 其他优化与BUG修复

***

* [X] Update CWT configs files to match Stellaris 3.12.2 (almost done)
* [X] Optimization: Optimize code reformatting feature, fixes some bugs in details
* [X] Optimization: Determine override order for targets (definitions, localisations, etc.) based on CWT configs files (Can be customized, see reference documentation for details)
* [X] Optimization: For localisation file, it's unnecessary to escape double quotes in localisation text
* [X] Optimization: It a target cannot be resolved, but related extended CWT configs exist, related code inspection can be configured to be ignored
* [X] Optimization: If the scope context can be inferred from extended CWT configs, do not continue to be inferred from usages
* [X] Other optimizations and bug fixes

## 1.3.6

* [X] 在内置规则文件的编辑器通知中提供操作，点击可以生成或者导航到对应的项目本地的规则文件
* [X] #73 扩展CWT规则：支持扩展的数据类型`AntExpression`，用于匹配ANT路径表达式（详见参考文档）
* [X] #73 扩展CWT规则：支持扩展的数据类型`Regex`，用于匹配正则表达式（详见参考文档）
* [X] #73 扩展CWT规则：支持在`## context_key`的值中使用模版表达式（详见参考文档）
* [X] #73 扩展CWT规则：编写扩展的CWT规则时，可以通过字符串字面量以及模版表达式进行匹配的地方，现在也可以通过ANT路径表达式以及正则表达式进行匹配（详见参考文档）
* [X] #71 修复一个有关作用域上下文切换的问题
* [X] #74 修复一个有关索引以及引用解析的问题
* [X] #76 通过扩展的CWT规则提供上下文规则时，允许直接在顶级规则使用`single_alias`（示例：`extended_param = single_alias_right[trigger_clause]`）
* [X] 优化：如果需要，对于已打开的文件，其文件内容应当及时重新解析，其中的内嵌提示应当及时更新（注意这可能需要先在后台花费一定时间）
* [X] 优化：优化代码补全的用时

***

* [X] Provide an action in editor notification bar for builtin config files, click to generate (or navigate to) related project-local config file
* [X] #73 Extend CWT Config: Supports extended data type `AntExpression`, which is used to match ANT expression (see reference documentation for details)
* [X] #73 Extend CWT Config: Supports extended data type `Regex`, which is used to match regex (see reference documentation for details)
* [X] #73 Extend CWT Config: Supports for using template expressions for value of `## context_key` (see reference documentation for details)
* [X] #73 Extend CWT Config: When writing extended CWT configs, in places which can use string literals and template expressions, now can also use ANT expressions and regular expressions (see reference documentation for details)
* [X] #71 Fix a problem about scope context switching
* [X] #74 Fix a problem about indexing and reference resolving
* [X] #76 When provide context configs by extended configs, allows to use `single_alias` at top level directly (e.g. `extended_param = single_alias_right[trigger_clause]`)
* [X] Optimization: If necessary, for opened files, reparse its file context and refresh its inlay hints (Note that this may take some times in background first)
* [x] Optimization: Optimize time cost of code completion

## 1.3.5

* [X] 为操作“导航到相关的CWT规则”提供更加完善的支持（额外适用于封装变量、参数等）
* [X] 支持通过扩展的CWT规则为封装变量（`scripted_variable`）提供扩展的快速文档（详见参考文档）
* [X] 支持通过扩展的CWT规则为封装变量（`scripted_variable`）提供扩展的内嵌提示（详见参考文档）
* [X] #66 支持为一些目标额外提供基于扩展的CWT规则的代码补全（包括封装变量、定义、内联脚本、参数、复杂枚举值与动态值，可配置，默认不启用）
* [X] #69 支持通过扩展的CWT规则为一些目标指定作用域上下文（包括定义、参数与内联脚本，基于`## replace_scopes`和`## push_scope`，忽略选项不合法的情况）
* [X] #70 提供内联脚本调用的代码补全（`inline_script = ...`，可配置，默认不启用，目前适用于所有游戏类型）
* [X] 添加代码检查：未使用的内联脚本（弱警告级别）
* [X] 其他优化与BUG修复

***

* [X] Improved support for the action "Goto to related CWT configs" (For scripted variables, parameters, etc.)
* [X] Support for providing extended quick documentation for scripted variables via extended CWT configs (see reference documentation for details)
* [X] Support for providing extended inlay hints for scripted variables via extended CWT configs (see reference documentation for details)
* [X] #66 Support for providing additional code completion for various targets via extended CWT configs (For scripted variables, definitions, inline scripts, parameters, complex enum values and dynamic values; Configurable; Disabled by default)
* [X] #69 Support for specifying the scope context for various targets via extended CWT configs (For definitions, parameters and inline scripts; Via `## replace_scopes` and `## push_scope`; Ignore invalid situations)
* [X] #70 Provide code completion for inline script invocations (`inline_script = ...`; Configurable; Disabled by default; Currently for all game types)
* [X] New code inspection: Unused inline scripts (level: weak warning)
* [X] Other optimizations and bug fixes

## 1.3.4

* [X] 为操作“导航到相关的CWT规则”提供更加完善的支持
* [X] 支持通过扩展的CWT规则为复杂枚举值（如`component_tag`）提供扩展的快速文档（详见参考文档）
* [X] 支持通过扩展的CWT规则为动态值（如`variable`）提供扩展的快速文档（变更了对应CWT规则的格式，详见参考文档）
* [X] 支持通过扩展的CWT规则为复杂枚举值（如`component_tag`）提供扩展的内嵌提示（详见参考文档）
* [X] 支持通过扩展的CWT规则为动态值（如`variable`）提供扩展的内嵌提示（详见参考文档）
* [X] 添加代码检查：重载的文件（弱警告级别，默认不开启）
* [X] 修复当CWT规则涉及`single_alias`时，应用代码补全后，不会正确地自动插入` = {}`的问题
* [X] 尝试修复涉及内联脚本与适用语言注入的参数时，IDE可能卡死的问题

***

* [X] Improved support for the action "Goto to related CWT configs"
* [X] Support for providing extended quick documentation for complex enum values (e.g. `component_tag`) via extended CWT configs (see reference documentation for details)
* [X] Support for providing extended quick documentation for dynamic values (e.g. `variable`) via extended CWT configs (format of relevant CWT configs is changed, see reference document for details)
* [X] Support for providing extended inlay hints for complex enum values (e.g. `component_tag`) via extended CWT configs (see reference documentation for details)
* [X] Support for providing extended inlay hints for dynamic values (e.g. `variable`) via extended CWT configs (see reference documentation for details)
* [X] New code inspection: Overridden for files (level: weak warning, enabled by default: no)
* [X] Fixed an issue that `= {}` would not be inserted correctly when applying code completion for script snippets matching CWT config of `single_alias`
* [X] Try to fix an issue that when inline scripts and parameters (with language injection) are involved, IDE may be freezing.

## 1.3.3

* [X] 更新CWT规则文件以适配Stellaris 3.11.1（基本完成）
* [X] 完善对作用域上下文的支持
* [X] 提供汇总的规则文件的项目视图
* [X] 添加代码检查：重载的封装变量（scripted_variable）（弱警告级别，默认不开启）
* [X] 添加代码检查：重载的定义（弱警告级别，默认不开启）
* [X] BUG修复：对于作用域切换，`prev.prev`应当等同于`this`，而非`prevprev`
* [X] BUG修复：对于本地化文件，本地化文本中的双引号不需要转义（直到本行最后一个双引号之前为止，视为本地化文本）
* [X] BUG修复：修复在插件新增的项目视图中无法正确选中已打开的文件的问题
* [X] 其他优化与BUG修复

## 1.3.2

* [X] 完善对全局的默认游戏目录的配置的支持
* [X] 优化CWT规则的匹配逻辑

## 1.3.1

* [X] 为菜单`Code -> Unwrap/Remove...`提供更多可选择的操作
* [X] 修复插件可能无法正确解析新添加的本地化在脚本文件中的引用的问题
* [X] 为操作“导航到相关的CWT规则”提供更加完善的支持
* [X] 支持通过扩展的CWT规则文件为参数指定CWT规则上下文
* [X] 支持通过扩展的CWT规则文件为内联脚本指定CWT规则上下文
* [X] 可以在插件配置中配置全局的默认游戏目录

## 1.3.0

* [X] 兼容IDEA 233，避免一些IDE启动时的报错
* [X] 支持通过扩展的CWT规则文件为定义（如`event`）提供扩展的快速文档
* [X] 支持通过扩展的CWT规则文件为动态值（如`event_target`）提供扩展的快速文档
* [X] 支持通过扩展的CWT规则文件为定义（如`event`）指定作用域上下文
* [X] 支持通过扩展的CWT规则文件为动态值（如`event_target`）指定作用域上下文
* [X] 支持通过扩展的CWT规则文件为参数提供扩展的快速文档
* [X] 完善对脚本文件和本地化文件中的转义字符的支持
* [X] #55 支持在多行的脚本参数值中使用内联脚本
* [X] #58 将条件参数加入参数上下文，用于代码补全、快速文档等功能
* [X] #59 避免在获取上下文规则列表时递归加载缓存

## 1.2.6

* [X] 优化为脚本文件提供的关键字的代码补全
* [X] 优化为脚本文件提供的参数值的代码补全
* [X] 修复无法正常支持整行或多行的脚本参数的参数值的问题

## 1.2.5

* [X] 修复覆盖了`economic_category`之后，无法正常解析因覆盖而新生成的修正的问题
* [X] 优化索引查询的逻辑 - 预先经过必要的排序、过滤和去重
* [X] 其他优化与BUG修复

## 1.2.4

* [X] 更新CWT规则文件以适配Stellaris 3.10.0（进一步完善）
* [X] 优化：在CWT规则的快速文档中显示CWT规则文件信息
* [X] BUG修复：修复无法渲染内嵌提示中的图标的问题
* [X] 其他优化与BUG修复

## 1.2.3

* [X] 更新CWT规则文件以适配Stellaris 3.10.0（基本完成）
* [X] 优化与BUG修复

## 1.2.2

* [X] 支持内联`scripted_variable`（即封装变量）（`编辑器右键菜单 -> Refactor -> Inline...`）
* [X] 支持内联`scripted_trigger`和`scripted_effect` （`编辑器右键菜单 -> Refactor -> Inline...`，仅限作为调用的引用）
* [X] 支持内联本地化（`编辑器右键菜单 -> Refactor -> Inline...`，仅限本地化文本中的引用）
* [X] 支持内联`inline_script`（即内联脚本）（`编辑器右键菜单 -> Refactor -> Inline...`）
* [X] 可以从项目视图或者模组依赖配置页面中的游戏或模组根目录打开其本地目录或者Steam创意工坊页面，以及复制对应路径/URL
* [X] 可以在工具菜单（`编辑器右键菜单 -> Paradox Language Support`）打开和复制数种路径/URL
* [X] （仅限Stellaris）支持表达式`technology@level` -
  参见：[\[Stellaris\] Could support tech@level grammar? · Issue #58](https://github.com/cwtools/cwtools-vscode/issues/58)
* [X] 其他优化与BUG修复

## 1.2.1

* [X] 支持语法`@a = @[ 1 + 2 ]` - 参见：[The tool cannot recognize in-script flag variables (Vic3) · Issue #76)](https://github.com/cwtools/cwtools-vscode/issues/76)
* [X] （仅限VIC3）支持操作符`?=` - 参见：[Parsing issues in Vic3 · Issue #53](https://github.com/cwtools/cwtools/issues/53)
* [X] 其他优化与BUG修复

## 1.2.0

* [X] 优化对[规则分组](https://windea.icu/Paradox-Language-Support/zh/config.md#config-group)的支持
* [X] 其他优化与BUG修复

## 1.1.13

* [X] BUG修复

## 1.1.12

* [X] 尝试优化插件性能 - 尝试优化编制索引时的速率与内存使用

## 1.1.11

* [X] 修复 #51 - 无法解析使用了scripted_variables的参数预设值
* [X] 优化 #30 - 兼容脚本文件的高级插值语法（`a[[b]c]d$e|f$g`）

## 1.1.10

* [X] 修复进行代码提示时列出的提示项可能不全的问题
* [X] 其他优化与BUG修复

## 1.1.9

* [X] 更新CWT规则文件以适配Stellaris 3.9.1
* [X] 其他优化与BUG修复

## 1.1.8

* [X] #50 优化：完善对修正的名字和描述的本地化，以及图标的图片支持
* [X] 优化进行代码补全时提示项的排序
* [X] 优化代码检查
* [X] 其他优化与BUG修复

## 1.1.7

* [X] 优化：生成本地化时，也可以基于脚本文件中无法解析的本地化（适用于对应的快速修复和生成操作）
* [X] 优化：重命名定义时，也重命名相关的本地化和图片的引用，以及由其生成的修正的引用，以及这些修正的相关本地化和图片的引用（可以配置并且默认启用）
* [X] 优化：提示封装变量和本地化时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限 -> 改为仅限本地化
* [X] 优化：如果需要，渲染图片时，先按照正确的帧数和总帧数切分原始图片
* [X] 尝试优化插件性能 - 避免在编制索引时访问索引（例如，如果编制索引时需要匹配定义，只要格式上匹配，就直接认为匹配）
* [X] 尝试优化插件性能 - 优化规则对象的内存占用
* [X] 其他优化与BUG修复

## 1.1.6

* [X] 修复`some_scripted_trigger`可能被插件认为同时匹配`<scripted_trigger>`和`scope_field`的问题（以及类似问题）
* [X] 优化对嵌套的定义的支持（如，`swapped_civic`）
* [X] 优化 #48 - 提示封装变量和本地化时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限
* [X] 尝试优化插件性能

## 1.1.5

* [X] 更新IDEA版本到2023.2
* [X] 匹配规则时如果发生索引异常，防止IDE报错
* [X] 修复DDS图片无法正确渲染的问题
* [X] 修复更新到IDEA 2023.2后，按住Ctrl并点击参数（以及其他类似目标）后，无法查找使用的问题
* [X] 修复更新到IDEA 2023.2后，无法打开事件树和科技树图表的问题
* [X] 修复 #47 - Parameters with defaults passed to script values cause highlight errors
* [X] 优化：检查定义的相关本地化和图片是否缺失时，也检查由其生成的修正的相关本地化和图片是否缺失（可以配置并且默认不启用）
* [X] 优化：生成本地化时，可以配置需要生成哪些本地化（参考重载/实现方法时弹出的对话框）

## 1.1.4

* [X] 修复构建的事件树和科技树中没有任何内容的问题
* [X] 尝试优化插件的性能 - 极大优化了进行查询和代码检查时的性能
* [X] 尝试优化插件的内存占用

## 1.1.3

* [X] 兼容带有参数的相关本地化和相关图片（不进行进一步的解析，进行代码检查时也不报错）
* [X] 支持渲染嵌入到本地化文本中的任意大小的图片（DDS/PNG）
* [X] 修复构建科技树图时报错的问题
* [X] 修复无法查询自身为文件的定义的问题
* [X] 进行代码补全时，如果需要，自动补上双引号
* [X] 修复可能无法解析顶层的作用域切换中的表达式对应的规则的问题（`some_scripted_effect = { root = { ... } }`）
* [X] 如果可能，尝试推断模组的游戏类型（例如，当模组目录位于对应游戏的创意工坊目录下时）（如果可以推断，就不允许通过模组配置更改）
* [X] 尝试使用语言注入功能推断脚本参数的传入值对应的CWT规则上下文，从而提供高级语言功能（待完善）
* [X] 尝试使用语言注入功能推断脚本参数的默认值对应的CWT规则上下文，从而提供高级语言功能（待完善）
* [X] 其他优化和BUG修复

## 1.1.2

* [X] 优化相关本地化和图片的CWT规则的解析逻辑：默认视为必须的，加上`## optional`后视为可选的
* [X] 优化相关本地化和图片的CWT规则的解析逻辑：对于`name = X`中的`X`，如果包含占位符`$`
  ，将占位符替换成定义的名字后，尝试得到对应名字的本地化或者对应路径的图片，否则尝试得到对应名字的属性的值对应的本地化或者图片
* [X] 优化相关图片的CWT规则的解析逻辑：对于`name = X`中的`X`，如果包含占位符`$`并且以`GFX_`
  开头，将占位符替换成定义的名字后，尝试得到对应名字的sprite定义对应的图片
* [X] 优化相关本地化的CWT规则的解析逻辑：对于`name = "$|u"`，将占位符替换成定义的名字且全部转为大写后，尝试得到对应名字的本地化或者对应路径的图
* [X] 完善CWT规则 - 清理和完善基本的相关本地化、相关图片的规则
* [X] 其他优化与BUG修复

## 1.1.1

* [X] 修复 #36 - 修复方法`isSamePosition`的实现中的问题
* [X] 修复 #38 - value_field和variable_field应当可接受`-$PARAM$`
* [X] 修复 #39 - 允许`$SCOPE|this$.modifier:xxx`
* [X] 修复 #44 - 修复可能无法正确解析嵌套的定义声明中的内容的问题
* [X] 修复更改游戏类型后无法打开模组配置的BUG
* [X] 更新CWT规则
* [X] 其他BUG修复

## 1.1.0

* [X] 修复 #26 P1 - 应当先尝试将`k = {...}`解析为内联脚本使用，再解析为定义声明
* [X] 修复 #32 - 重命名内联脚本的文件名时，相关缓存未正确刷新
* [X] 修复 #33 - 重命名内联脚本的文件名时，相关的表达式未正确更新文本
* [X] 添加代码检查：未正确进行重载的全局封装变量（例如，游戏文件`test.txt`中声明了一个全局封装变量`@v`
  ，而模组在文件`zzz_test.txt`中进行了重载）
* [X] 添加代码检查：未正确进行重载的定义 （例如，游戏文件`test.txt`中声明了一个事件`e.1`，而模组在文件`zzz_test.txt`中进行了重载）
* [X] 兼容直接在脚本文件中使用内联脚本，而非必须在定义声明中（或者内联后在定义声明中）
* [X] 尝试在更多情况下推断脚本参数对应的CWT规则（上下文）

目前支持在以下情况下推断脚本参数对应的CWT规则（上下文）：

* 支持脚本参数作为整个表达式，且可以推断此表达式对应的CWT规则的情况
* 支持脚本参数作为整个表达式，且所在位置对应的参数值可以是脚本片段（值或者属性的值），且可以推断此脚本片段对应的CWT规则上下文的情况
* 如果推断得到的CWT规则（上下文）并不完全一致，尝试进行合并
    * 对于CWT规则，如果规则的表达式相兼容（如`value[event_target]`和`value_field`），则认为可以合并
    * 对于CWT规则上下文，如果规则在文件中是同一处，或者内联后是同一处（如`single_alias_right[trigger_clause]`），则认为可以合并
* 如果合并后得到的CWT规则（上下文）为空，则认为存在冲突
    * 某些内容相兼容的CWT规则（如不是同一处规则的`possible = {...}`），目前版本仍然会被认为存在冲突

不再在标准参考文档和标准CHANGELOG中写明非标准的功能和扩展。因此，不保证以下功能仍然可用：

* 快速文档链接也能作为html/markdown等文件中的超链接使用，从而引用和跳转到指定的定义/本地化等（参见之前的CHANGELOG）
* 通过特定的注释注入文件路径以及文件元素路径，以便把脚本片段识别为特定的定义声明（参见之前的CHANGELOG）

## 1.0.8

* [X] 提供一种项目视图，用于显示合并后的所有游戏和模组文件（`Project Pane -> Paradox Files`）
* [X] 初步提供对覆盖顺序的支持，用于在查询文件、封装变量、定义、本地化时基于覆盖进行排序（除了代码补全等需要逐步遍历的地方）
* [X] 修复可能无法从字符串常量规则和枚举值规则反向查找在脚本文件中的使用的问题
* [X] 修复DIFF视图中没有正确重载文件类型的问题
* [X] 修复在输入本地化引用时（以及其他类型情况）可能因为读写锁冲突导致IDE卡死的问题
* [X] 修复在进行代码补全时可能不会提示某些应当匹配的本地化的问题
* [X] 修复初次打开项目时某些文件路径引用可能会被显示无法解析的问题
* [X] 优化：也可以在本地化引用上调用复制本地化文本的意向
* [X] 尝试优化性能
* [X] 其他优化和BUG修复

## 1.0.7

* [X] 修复 #26 P2 - 在脚本文件中，应当允许多行的用双引号括起的字符串
* [X] 修复 #28 - 生成科技树时报错
* [X] 修复 #29 - 应当允许在SV表达式（以及其他各种复杂表达式）中为参数指定默认值
* [X] 优化基于本地化名字进行代码补全时的性能
* [X] 尝试优化性能

## 1.0.6

* [X] 改为仅在必要时才对一些索引使用懒加载（如需要内联的情况）
* [X] 不再推断`static_modifier`的作用域上下文（可以不匹配，同`modifier`和`scripted_modifier`）
* [X] 尝试优化性能

## 1.0.5

* [X] 完善对定义的作用域上下文推断的支持 - 完善实现逻辑，优化性能，支持更多的定义类型
* [X] 改为默认不启用作用域上下文推断（因为可能性能不佳）
* [X] 重新将一些索引改为懒加载（因为它们在索引时可能需要访问其他索引，从而可能导致无法避免的报错）
* [X] 尝试优化性能 - 优化解析脚本参数和本地化参数时的性能
* [X] 其他优化和BUG修复

## 1.0.4

* [X] 尝试优化性能：如果可行，直接在索引时获取定义的子类型
* [X] 尝试优化性能：`ParadoxValueSetValueFastIndex` - 改为使用`FileBasedIndex`（在项目启动时就完成索引，避免之后卡住文件解析和代码检查）
* [X] 尝试优化性能：`ParadoxComplexEnumValueIndex` - 改为使用`FileBasedIndex`（在项目启动时就完成索引，避免之后卡住文件解析和代码检查）
* [X] 修复 #25 空指针
* [X] 更新CWT规则文件以适配Stellaris 3.8.3

## 1.0.3

* [X] 尝试优化内存占用
* [X] 尝试优化性能

## 1.0.2

* [X] 尝试优化性能
* [X] 其他优化和BUG修复

## 1.0.1

* [X] 修复代码检查`UnusedValueSetValueInspection`和`UnsetValueSetValueInspection`运行不正确的问题
* [X] 修复初次打开项目时可能无法正确解析各种复杂表达式的问题
* [X] 索引valueSetValue（如，`event_target`）时，也包含仅在本地化文件中使用到的`event_target`和`variable`
* [X] 其他优化和BUG修复

## 1.0.0

* [X] 
  提示定义和修正时，也可以基于它们的本地化名字进行提示。（在插件配置页面中勾选`Code Completion > Complete by localized names`
  以启用此功能）
* [X] 尝试基于在其他事件中的调用推断事件的作用域上下文（如果推断发生递归或者存在冲突则取消推断）
* [X] 完善Stellaris的CWT规则文件 - 完善`localisation.cwt`
* [X] 尝试优化性能
* [X] 其他优化和BUG修复

## 0.10.3

* [X] 在本地化文件中允许`KEY:"..."`这样没有冒号和引号之间没有空格的写法
* [X] 
  实现扩展点以在某些极个别情况下基于特定逻辑获取脚本表达式对应的CWT规则以及作用域上下文（如，对于`complex_trigger_modifier`
  和`switch`）
* [X] 实现扩展点以兼容定义继承，可在快速文档中或者通过导航菜单转到父 定义（如，事件的继承）
* [X] 实现扩展点以在匹配CWT规则的基础上，进一步检查脚本表达式是否正确（如，对于`switch`中的`trigger`的值）
* [X] 可以通过`Navigate > Super Definition`跳转到父定义（如果存在）
* [X] 为本地化文件中的本地化引用提供内嵌提示（如果可以被解析为一个本地化且其存在本地化文本）
* [X] 对于内嵌提示，改为默认不渲染本地化文本（但不包括本地化名字，例如事件标题）
* [X] 完善Stellaris的CWT规则文件
* [X] 尝试优化性能

## 0.10.2

* [X] 在更多情况下尝试推断脚本参数对应的CWT规则，从而提供各种高级语言功能（如，基于CWT规则的代码高亮、引用解析和代码补全）
* [X] 在本地化文件的悬浮工具栏（选中一段本地化文本以显示）中，提供额外的工具按钮以快速插入一些特殊语法（如，`$LOC$`）
* [X] 编辑本地化文件时，可以不打开悬浮工具栏，直接按下特定快捷键以快速插入一些特殊语法（如，`$LOC$`）
* [X] 修复类似`$P1$xxx$P2$`这样的脚本表达式无法正确地应用代码高亮的问题

在当前版本中，插件可以通过以下几种情况推断脚本参数对应的CWT规则：

```
icon = $ICON$ # 脚本参数直接作为整个脚本表达式，且可以得到这个脚本表达式对应的CWT规则
some_script = { JOB = $JOB$ } # 脚本参数传递给另一个脚本参数，且可以推断得到这另一个脚本参数对应的CWT规则
mult = modifier:$MODIFIER$ # 脚本参数作为某个复杂表达式中的整个节点，且可以得到这个节点对应的的CWT规则
```

注意：插件不会处理推断得到的CWT规则存在冲突（推断结果存在多个且互不兼容）的情况。

本地化文件的悬浮工具栏中提供的操作：

* `Pls.Localisation.Styling.CreateReference` - 快速插入引用（`$FOO$`） - `ctrl alt r`
* `Pls.Localisation.Styling.CreateIcon` - 快速插入图标（`£foo£`） - `ctrl alt i`
* `Pls.Localisation.Styling.CreateCommand` - 快速插入命令（`[Foo]`） - `ctrl alt c`
* `Pls.Localisation.Styling.SetColor` - 快速插入彩色文本（`§X...§!`） - 将会列出所有可选的颜色

## 0.10.1

* [X] 优化代码，提高版本兼容性
* [X] 优化代码 - 尝试缓存修正的解析结果
* [X] 完善Stellaris的CWT规则文件
* [X] 当脚本参数整个对应一个脚本表达式时，尝试推断得到对应的CWT规则，从而提供各种高级语言功能（如，基于CWT规则的代码高亮、引用解析和代码补全）
* [X] 推断脚本参数对应的CWT规则时，尝试缓存结果
* [X] 添加代码检查：递归的内联脚本使用

## 0.10.0

* [X] 更新CWT规则文件以适配Stellaris 3.8（尚未充分验证）
* [X] 脚本参数可以直接在用双引号括起的字符串中使用（但是如何转义？）
* [X] 脚本参数也可以在路径引用中使用，包括内联脚本的路径引用
* [X] 实现扩展点以为特定名字的定义指定不同的CWT规则（如3.8开始某些特定的`game_rule`）
* [X] 支持Stellaris 3.8新增的concept语法以及相关功能（如代码高亮、引用解析、快速文档）
* [X] 检查传入参数是否缺失时兼容参数传递的情况（不检查这种情况）
* [X] 修复 #24

注意：

目前不会尝试去推断作为脚本参数的表达式对应的CWT规则，或者包含脚本参数的表达式对应的CWT规则，
因此如果一个内联脚本路径引用中包含参数，插件不会尝试推断可能对应的那些内联脚本中的表达式对应的CWT规则。
（典型的例子就是3.8开始`leader_trait`定义声明中使用到的那些）

## 0.9.17

* [X] 优化：如果一个定义是匿名的，不要尝试获取和检测基于定义名称的相关本地化和图片
* [X] 优化：完善脚本参数是否可选的判断逻辑
* [X] 删除代码检查：`IncorrectDefinitionNameInspection`
* [X] 修复初次打开项目时，已打开的文件中的部分路径引用可能无法被正确解析的问题
* [X] 修复可能将预定义的修正解析为基于复杂枚举值的生成的修正的问题（如：`army_damage_mult`）
* [X] 其他优化和BUG修复

## 0.9.16

* [X] 修复引用了枚举的模版规则表达式无法被正常解析的问题（如`enum[component_tag]_weapon_damage_mult`）
* [X] 修复进行全局代码检查时可能导致`StackOverflowError`和`NoClassDefFoundError`的问题
* [X] 完善扩展点提供对脚本参数和本地化参数的支持
* [X] 提取扩展点提供对快速文档链接的支持，用于点击跳转到对应的定义/本地化等
* [X] 快速文档链接也能作为html/markdown等文件中的超链接使用，从而引用和跳转到指定的定义/本地化等（参见下面的示例）
* [X] 点击快速文档中渲染的本地化中的相关文本也能跳转到相关声明（如scope和scripted_loc）（内嵌提示同理）

以下是一些超链接的示例：

```
* [some_scripted_variable](pdx-sv:some_scripted_variable)
* [origin_default](pdx-def:origin_default)
* [origin_default](pdx-loc:origin_default)
* [gfx/interface/icons/origins/origins_default.dds](pdx-path:gfx/interface/icons/origins/origins_default.dds)
```

## 0.9.15

* [X] 兼容在脚本文件中的封装变量或其引用的名字中使用参数（如，`@var$n$`）
* [X] 更好地兼容在脚本文件中的作为键或值的字符串中使用参数（如，`$p1$s = s$p2$s`）
* [X] 更新CK3、EU4、IR和VIC3的CWT规则文件
* [X] 优化valueSetValue的索引，现在也将`event_target:xxx`中的`xxx`加入索引
* [X] 对于本地化命令，兼容`event_target:`前缀
* [X] 添加代码检查：（对于脚本文件）在不支持的地方使用了参数（仅限在scripted_effect/scripted_trigger）等支持参数的定义声明中以及内联脚本中使用
* [X] 添加代码检查：（对于脚本文件）在不支持的地方使用了封装变量（不能在asset文件里面使用全局封装变量）
* [X] 添加代码检查：（对于脚本文件）在不支持的地方使用了内联数学表达式（不能在asset文件里面使用，~~
  在每个scripted_trigger/scripted_effect中最多只能使用1次~~）
* [X] 添加代码检查：（对于脚本文件）在不支持的地方使用了内联脚本（不能在asset文件里面使用）
* [X] 添加代码检查：（对于脚本文件）递归调用的scripted_trigger/scripted_effect（不支持）
* [X] 添加代码检查：（对于本地化文件）递归调用的本地化引用（不支持）

## 0.9.14

* [X] 匹配定义时，rootKey忽略大小写（例如对于事件，rootKey可以是`event`，也可以是`Event`）
* [X] 可以在插件设置中配置是否显示编辑器悬浮工具栏，默认显示
* [X] 优化何时提示本地化名字以及提示本地化名字时的性能
* [X] 优化当前文件能否引用另一个文件中的内容的判断（例如，模组文件不能引用游戏目录下`pdx_launcher/game`目录中的文件中的内容）

## 0.9.13

* [X] 兼容在VCS提交记录中正常查看脚本文件、本地化文件、DDS图片，并且如果可能，基于本地版本提供高级语言功能
* [X] 如果可能，直接在IDE编辑器中渲染DDS图片，而不是基于缓存的PNG图片

## 0.9.12

* [X] 修复 #23

## 0.9.11

* [X] 修复定义子类型表达式忘记实现的问题

## 0.9.10

* [X] 实现操作（`Action`） - 从指定的本地化文件生成其他语言区域的本地化文件
* [X] 选择文件或目录时，为游戏或模组根目录提供额外的信息文本
* [X] 添加模组依赖时，可以一次性添加多个（例如从Steam创意工坊目录下一次性添加多个）
* [X] 修复：[群星，新建目录 #21](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/21)

## 0.9.9

* [X] 优化基于脚本文件的StubIndex（统一使用`ILightStubElementType`）
* [X] 实现调用层级（`Navigate > Call Hierarchy`） - 用于递归列出封装变量/定义/本地化的调用或者被调用关系，可以在插件配置页面中做进一步的配置

## 0.9.8

* [X] 完善对图表的支持 - 支持通过作用域过滤显示结果，提供另外的更加详细的配置页面（需要专业版IDE）
* [X] 为Stellaris以外的蠢驴游戏提供标准的事件树图表支持
* [X] 构建图表时可取消
* [X] 修复：[stellaris颜色模板还是没法用 #18](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/18)

## 0.9.7

* [X] 
  修复：[群星文本颜色插件失效（没有弹出选框），同时报错 #15](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/15)
* [X] 实现内嵌提示以提示复杂枚举值的信息（枚举名，如`policy_flag`，默认启用）
* [X] 实现类型层级（`Navigate > Type Hierarchy`） - 用于基于定义类型列出定义（光标位置在定义声明中时，此功能可用）
* [X] 完善对类型层次的支持 - 支持通过作用域过滤显示结果
* [X] 优化索引，重新索引后，对于复杂枚举值（如`policy_flag`）和值集值（如`variable`），应当不会再出现应当可以解析但有时无法解析的情况了
* [X] 可以在插件配置页面中配置进行DIFF时，初始打开的初始打开的DIFF分组。默认初始打开VS副本的DIFF分组

## 0.9.6

* 优化与BUG修复
* [X] 优化何时才会为本地化文件显示悬浮工具栏的判定
* [X] 兼容嵌套的内联脚本使用
* [X] 优化值集值（`valueSetValue`，如，`event_target`和`variable`）的索引
* [X] 优化复杂枚举值（`complexEnumValue`，如`policy_flag`）的索引
* [X] 不再可以通过`Navigate | Class or Navigate | Symbol`快速查找匹配名字的复杂枚举值
* [X] 优化索引，现在应该不会报错`Indexing process should not rely on non-indexed file data`了

## 0.9.5

* 新增功能
    * [X] 可以基于特殊位置（文件开始、直接在定义之前、直接在定义成员之前、直接在本地化属性之前）的特殊注释禁用指定ID的代码检查
    * [X] 在游戏目录或模组根目录下创建目录时，可以提示目录名称
    * [X] 支持本地化参数（即本地化文件中的解析为脚本参数而非本地化引用的`$PARAM$`）
* 新增功能 - 取消包围/移除（`Code > Unwrap/Remove...`）
    * [X] CWT文件：删除属性、单独的值
    * [X] 脚本文件：删除封装变量声明、属性、单独的值、参数条件块
    * [X] 本地化文件：删除本地化属性、图标引用、属性引用、本地化命令
    * [X] 本地化文件：移除颜色标记

## 0.9.4

* 更新到IDEA版本2023.1
* 优化索引时的规则匹配逻辑
* 修复：[只能读取动态肖像而不能读取静态肖像 #13](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/13)

## 0.9.3

* BUG修复
* 代码优化
* 完善Stellaris的CWT规则文件（检查游戏目录中的所有脚本文件） - 基本完成
* 提供对Stellaris的`scripted_effect`的修正分类和支持的作用域的支持（涉及快速文档、代码补全等功能）
* 优化对本地化文本的渲染 - 如果可以解析，渲染解析后的封装变量（`scripted_variable`）的值
* 修复有时无法解析本地化命令中的事件对象引用和变量引用的问题
* 修复无法为本地化图标提供引用高亮的问题
* 判断定义的类型和子类型时，支持选项`## type_key_regex = .*`
* 优化脚本文件和CWT文件的解析器，将"a#b"中的"#b"视为注释
* 修复自定义折叠（`region...endregion`）有时无法正确折叠文本的问题
* 支持规则`filepath[./]`，用于匹配相对于脚本文件所在目录的路径
* 优化查找可用的本地封装变量的逻辑 - 只要在使用处之前声明就可以

## 0.9.2

* BUG修复
* 代码优化
* 完善Stellaris的CWT规则文件（检查游戏目录中的所有脚本文件） - 更新中
* 完善对脚本文件和本地化文件的文件结构（`File Structure`）的支持
* 完善对脚本文件和本地化文件的代码检查页面中按目录分组（`Group By Directory`）的支持

## 0.9.1

* BUG修复
* 代码优化
* 更新Stellaris的基于script_documentation日志的CWT规则文件到最新游戏版本3.7.2
* 完善Stellaris的CWT规则文件（检查游戏目录中的所有脚本文件） - 更新中

## 0.9.0

* BUG修复
* 代码重构
    * [X] 将为脚本表达式提供各种功能的代码提取成扩展点
* 功能优化
    * [X] 在GOTO菜单中也提供导航到重载或者被重载的文件/定义/本地化的操作
    * [X] 更加完善的DIFF支持 - 比较定义和本地化时也提供高级语言功能（如代码提示、引用解析）
    * [X] 初步支持通过快速修复和工具菜单生成本地化（可以在插件配置中配置如何生成本地化文本）

## 0.8.3

* BUG修复
* 功能优化
    * 完善Stellaris的CWT规则文件（检查游戏目录中的所有脚本文件） - 更新中
    * [X] 定义的subtype可能需要通过访问索引获取，不能在索引时就获取
    * [X] 如果CWT规则文件中的一行文档注释以`\`结束，则解析时不在这里换行
    * [X] 优化作用域上下文的解析逻辑，在快速文档和类型信息中也显示prev系列作用域
    * [X] 在进行代码提示时就预测并提示作用域上下文
    * [X] 基于事件的调用处（on_action）推断事件的作用域上下文，如果存在冲突，则转而给出警告（此检查默认不启用）
    * [X] 在`alias_name[effect]`匹配的子句中可以配置直接提示变量名（与后缀补全配套使用，直接输入并不合法，默认启用）
* 功能优化 - CWT规则支持
    * [X] 添加扩展的CWT规则类型`$shader_effect`，对应`.shader`文件中的effect，暂时作为一般的字符串处理
    * [X] 由基于`on_actions.csv`获取`on_action`的事件类型、作用域上下文、文档等信息，改为基于`on_actions.cwt`获取
* 新增功能
    * [X] 初步支持生成科技树图表（通过项目视图或者编辑器的右键菜单中的`Diagrams > Show Diagram...`打开，需要专业版IDE）
    * [X] 初步支持生成事件树图表（通过项目视图或者编辑器的右键菜单中的`Diagrams > Show Diagram...`打开，需要专业版IDE）

## 0.8.2

* BUG修复
* 参考Irony Mod Manager的实现添加一些导入导出模组依赖的方式（忽略需要读取sqlite数据库的方式）

## 0.8.1

计划内容：

* 游戏/模组配置以及相关功能

更新项：

* 功能优化
    * [X] 改为基于合成库`SyntheticLibrary`和游戏/模组配置对话框的方式，配置游戏类型、游戏目录、依赖模组列表等配置项
    * [X] 如果检测到项目中的模组目录未配置对应的游戏目录，界面右下角给出提示
    * [X] 可以从启动器模组列表文件一键导入模组依赖列表
    * [X] 可以导出模组依赖列表为启动器模组列表文件
    * [X] 优化查询逻辑 - 指定查询上下文以及限定查询作用域（例如，此模组，此模组+游戏目录，此模组+游戏目录+模组依赖）
    * [X] 可以直接在悬浮工具栏中打开模组配置页面（显示在编辑器右上角）
    * [X] 在项目视图中为模组根目录显示模组的名称和版本信息
    * [X] 通过模组配置对话框选择游戏目录/模组目录时，为模组目录显示模组的名称和版本信息
    * [X] 脚本解析优化：valueSetName（如各种flag的名字）允许包含点号
* 功能优化 - CWT规则支持
    * [X] CWT数据类型`filename`：要求是文件名，如果指定了目录（如`filename[gfx/models]`），则需要位于该目录中，否则需要直接位于对应的脚本文件所在的目录中
* 新增功能
    * [X] 在编辑器右键菜单和工具菜单中新增操作项：打开模组配置
    * [X] 在编辑器右键菜单和工具菜单中新增操作项：在Steam应用/网站中打开游戏商店页面、创意工坊页面和模组页面
    * [X] 在编辑器右键菜单和工具菜单中新增操作项：复制各种路径到系统剪贴板

## 0.8.0

计划内容：

* 扩展编辑器右侧菜单和工具菜单

更新项：

* 功能优化
    * [X] 可以直接在悬浮工具栏中打开各种DIFF页面（显示在编辑器右上角）
* 功能优化 - CWT规则支持
    * [X] 匹配CWT规则时，如果规则可能是多个不同的子句时，需要根据其中的属性规则是否匹配任意子句中的属性的名字，来判断到底匹配哪个子句规则
        * 缺失的表达式/过多的表达式的检查 - 匹配任意则使用匹配的规则，空子句或者都不匹配则使用合并的规则，匹配多个时不适用这些检查
        * 引用解析 - 如果可以直接匹配子句或子句中的属性则使用匹配的规则进行引用解析，否则默认使用首个子句规则
        * 代码补全 - 匹配任意则仅补全匹配的子句规则中的内容，空子句或者都不匹配则使用合并的规则，使用子句模版时如有必要当前的待补全项列表应当可以切换
    * [X] 判断定义的类型和子类型时，支持选项`## starts_with = likes_`（不忽略大小写）
    * [X] 可以从CWT属性/字符串查找使用（从CWT规则查找脚本文件中对应的属性和值）
* 新增功能
    * [X] 在编辑器右键菜单和工具菜单中新增一些额外的操作选项，包括：打开Steam和游戏的相关目录
    * [X] 也可以通过Ctrl+鼠标左键点击子句的左花括号（`{ ... }`中的`{`）导航到对应的CWT规则，不要求精确对应
* 新增功能 - 代码检查（`Code > Inspect Code...`）
    * [X] 定义名不匹配指定前缀的检查（基于扩展的CWT选项`## prefix = xxx`，不忽略大小写，CWT规则文件待完善）
    * [X] 路径引用的文件扩展名不匹配的检查（基于扩展的CWT选项`## file_extensions = xxx`） - 改为单独的检查
    * [X] 对应的CWT规则存在冲突的检查（如果可以精确匹配多个CWT规则，例如`size = { x = 1 width = 1 }`，目前仅限匹配多个子句规则的情况）

## 0.7.13

计划内容：

* 支持推断inline_script的调用位置从而为其提供各种功能
* 支持通过economic_category生成的修饰符
* 提供更加完善的颜色支持（可以通过颜色装订线图标显示和设置颜色）

更新项：

* BUG修复
    * [X] 
      修复：[Continued Inability to set the CK3 game folder as a library #8](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/8)
    * [X] 修复：通过颜色装订线图标修改颜色时，颜色在第一次被设置后，不关闭对话框再次设置会无法生效
* 功能优化
    * [X] 优化对复杂表达式的处理：特殊代码高亮绝不高亮参数部分，增强兼容性
    * [X] 匹配CWT规则使用静态匹配，需要访问索引时，大部分情况下认为直接匹配
    * [X] 优化游戏目录（基于`launcher-settings.json`）和模组目录（基于`descriptor.mod`）的判断逻辑
    * [X] 优化在快速文档中或者新建库对话框中，点击文本超链接或者工具栏图标按钮以导航到Steam和游戏相关目录时的导航逻辑，并且可以快速选择游戏目录
    * [X] 优化对颜色的支持（示例：`rgb { 255 255 255 }`，`color = { 255 255 255 }`
      ，基于扩展的CWT选项`## color_type = rgb/hsv/hex`）
    * [X] 兼容用文件路径匹配规则文件路径时，文件路径需要相对于入口目录（如`game` `jomini`）而非游戏根目录的情况
    * [X] 支持扩展的CWT数据类型`filename`，用于匹配直接位于脚本文件所在目录下的指定名称的文件（待完善）
    * [X] 支持复杂枚举的查询作用域`search_scope_type_type = xxx`，认为仅该作用域下的复杂枚举值是等同的。（目前支持：definition）
* 功能优化 - 智能推断
    * [X] 基于使用处推断`inline_script`的位置（即需要对应的CWT规则文件入口，同时如果可以推断，在内联脚本文件上方提示，同时也提供对参数的支持）
    * [X] 可扩展的参数解析器（提供推断参数的上下文、调用表达式中传入参数的上下文等功能）
* 新增功能
    * 完善支持处理生成的修饰符（`modifier`），以及相关的引用解析、代码补全、代码高亮等功能
        * [X] 支持通过Stellaris的`economic_category`生成修饰符
        * [X] 通过Stellaris的`economic_category`生成修饰符，兼容继承的mult修饰符（如，`starbase_outpost_cost_mult`）
        * [X] 提供修饰符解析器的扩展点，便于后续扩展
    * 代码检查（`Code > Inspect Code...`）
        * [X] 缺少的传参的检查（警告级别，在调用表达式、SV表达式中，如果参数不存在默认值且未传递，则认为缺少传参）
        * [X] 推断的`inline_script`的位置存在冲突（使用处的父节点对应的CWT规则存在不一致的情况）
        * [X] 定义声明中缺失/过多的表达式的检查 - 现在可以配置仅显示第一个错误
        * [X] 定义声明中缺失/过多的表达式的检查 - 现在可以配置是否解析内联的内联脚本，内联后进行检查
        * [X] 路径引用的文件扩展名不匹配的检查（归类到`IncorrectExpressionInspection`
          ，基于扩展的CWT选项`## file_extensions = xxx`）

## 0.7.12

计划内容

* 支持比较同名的文件、定义、本地化等

更新项：

* 提高代码兼容性
* BUG修复
    * [X] 修复：[Exception on project load #9](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/9)
* 功能优化
    * [X] 语法解析优化 - 支持脚本文件中的`!=`
    * [X] 语法解析优化 - CWT文件的文档注释中可以任意使用`#`
    * [X] CWT规则文件的`cardinality`
      选项也可以引用define的数值（如，`## cardinality_max_define = "NGameplay/ETHOS_MAX_POINTS"`）
* 新增功能
    * 操作（`Action`）
        * [X] ~~在文件上使用`Goto Implementations`操作可以导航到同名的重载或者被重载的文件~~（IDE默认不可行，无法生效）
        * [X] 同名的定义、本地化，同路径的文件之间的DIFF（左窗口显示当前的，右窗口显示包括当前的只读副本在内的所有的）
            * 显示在编辑器右键菜单/结构视图右键菜单/顶部导航栏`View`下拉菜单中
            * 选择DIFF对象时显示对应的图标，且高亮只读副本对应的选项
            * 对于定义和本地化，目前仅提供代码高亮，无法提供更多的语言功能（如代码解析、代码补全）
    * 装订线图标（`Gutter Icon`）
        * [X] 也为封装变量（`@a = 1`，本地化&全局）提供装订线图标，以便导航到同名的重载或者被重载的封装变量

## 0.7.11

* BUG修复
    * [X] 修复无法查找定义的引用和本地化图标中引用的问题
    * [X] 修复无法全局查找复杂枚举值的问题
    * [X] 修复动态模版的上下文范围判定的问题（脚本文件的keyExpressions/valueExpressions）
    * [X] 
      修复：[Cannot choose path for library using CK3 #7](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/7)
* 功能优化
    * [X] 对于名称忽略大小写的CWT规则，在读取时保留规则文件中声明的顺序
    * [X] 如果某个游戏的规则文件未写明localisation_link规则，则使用from_data=no的link规则
    * [X] CWT规则类型`scalar`也可以对应一个数字，不需要用引号括起（因此也可以对应一个scripted_variable）
    * [X] 兼容可能的动态的相关本地化和相关图片（例如，相关本地化基于触发器（`trigger`）时需要跳过检查）
    * [X] 实现生成器基于日志文件生成CWT规则文件（`modifiers.cwt` `effects.cwt` `triggers.cwt`等）
    * [X] 完善Stellaris的CWT规则文件（`modifiers.cwt` `effects.cwt` `triggers.cwt`等）
* 新增功能 - 概述
    * [X] 初步支持CWT规则类型`variable_field`和`int_variable_field`，以及相关的引用解析、代码补全、代码高亮等功能
        * 作为`value_field`和`int_value_field`的子级
        * 仅支持`root.var`的格式
    * [X] 初步支持处理生成的修饰符（`modifier`），以及相关的引用解析、代码补全、代码高亮等功能
        * 基于CWT类型规则中的`modifiers`规则（例如：`modifiers = { job_<job>_add = Planets }`）（认为没有实际上的声明处，并使用特殊的高亮）
        * 基于`modifiers.log`生成`modifiers.gen.cwt`，并且整理编写`modifiers.cwt`
          ，以处理生成的修饰符（包括由Stellaris的`economic_category`生成的）
        * 注意：暂不支持通过Stellaris的`economic_category`生成修饰符（原版游戏会生成的照常支持）
* 新增功能
    * 快速文档（`Quick Documentation`）
        * [X] 优化CWT规则的快速文档显示
        * [X] 如果是修饰符，在快速文档中显示相关本地化、图标、分类、支持的作用域、作用域上下文等信息
        * [X] 如果是生成的修饰符，在快速文档中显示生成模版（例如，`job_<job>_add`）和生成源的信息（例如，`job_xxx_add`）
    * 代码补全（`Code > Code Completion`）
        * [X] 可以补全预定义/生成的修饰符
    * 引用解析
        * [X] 可以通过`Ctrl+Click`查找修饰符使用
    * 意向（`Intention`）
        * [X] 用双引号括起/不再用双引号括起（对于属性名、数字和字符串）
    * 代码检查（`Code > Inspect Code...`）
        * [X] 不充分的表达式的检查 - 检查作用域和作用域组是否匹配

## 0.7.10

* BUG修复
* 新增功能
    * 代码折叠（`Code > Folding`）
        * [X] 可以折叠封装变量引用（`@v`，并且默认折叠）
        * [X] 默认折叠内联数学块（`@[...]`）
        * [X] 默认折叠变量操作表达式（`set_variable = {...}`）

## 0.7.9

* BUG修复
* 功能优化
    * [X] 默认将基于子句内联模版的提示项放到前面
    * [X] 实现生成器从`modifiers.log`生成或更新`modifiers.cwt`和`modifier_categories.cwt`
    * [X] 如果定义类型的顶级属性名是限定且存在多种情况的，如有必要，解析定义信息时总是要求顶级属性名是可能的情况之一，而不是任意字符串（如stellaris中的event）
    * [X] 为修饰符、触发器和效果（`modifier trigger effect`）提供特殊的高亮
    * [X] 为修饰符、触发器和效果（`modifier trigger effect`）提供特殊的图标
    * [X] 定义引用也可以是一个整数，例如，对于`<technology_tier>`
    * [X] 兼容切换类型（例如，`swapped_civic`），包括引用解析、代码提示等功能
* 功能变更
    * 补充当前声明（`Code > Code Completion > Complete Current Statement`）
        * [X] ~~对于脚本文件：`k` > `k = `~~（删除，不适合且不是特别必要）
        * [X] ~~对于CWT文件：`k` > `k = `~~（删除，不适合且不是特别必要）
* 新增功能 - 概述
    * [X] 初步支持处理作用域（`scope`），以及相关的快速文档、内嵌提示、代码检查等功能
        * 基于已有的CWT规则文件
        * （对于`on_action`）基于`on_actions.csv`（注意，不一定是这个文件名）
        * 有时作用域信息是cwtools在运行时基于日志文件生成的，暂不支持
    * [X] 支持处理内联脚本（`inline_script`）
        * 尽管官方的说明文档中提到并非如此，认为内联脚本可以在任意定义声明中调用
        * 基于上一条说明，进行代码补全时，不会补全`inline_script`，照常补全其值、作为值的子句以及子句中的内容
* 新增功能
    * 快速文档（`Quick Documentation`）
        * [X] 如果支持，在快速文档中显示作用域上下文信息
    * 快速类型信息（`View > Type Info`）
        * [X] 如果支持，在类型信息中显示作用域上下文信息
    * 内嵌提示（`Inlay Hints`）
        * [X] 如果支持且位置合适，提供作用域上下文的内嵌提示（参考Kotlin Lambda内嵌提示的实现）
    * 代码补全（`Code > Code Completion`）
        * [X] 基于别名规则进行提示时，如果设置要求匹配作用域，则仅提示匹配的
        * [X] 提示修饰符时，如果设置要求匹配作用域，则仅提示匹配的
        * [X] 在各种复杂表达式中进行提示时，如果设置要求匹配作用域，则仅提示匹配的（作用域字段表达式、值字段表达式和本地化命令表达式）
    * 代码检查（`Code > Inspect Code...`）
        * [X] 检查作用域上下文与当前作用域是否匹配（警告级别，部分完成，适用于`effect`、`trigger`
          以及本地化命令`localisationCommand`）
        * [X] 
          检查作用域上下文切换是否正确（警告级别，部分完成，适用于各种带作用域信息的脚本表达式以及本地化作用域`localisationCommand`）
        * [X] 不充分的表达式（弱警告级别，部分完成，匹配某一CWT规则，但是值不在指定的范围内，或者作用域不匹配指定的作用域或作用域组，等等）
        * [X] 过长的作用域连接（弱警告级别，超过5个作用域，即4个点号的连接认为是过长的）
        * 目前不会基于使用推断作用域上下文（例如，基于`event_target`在哪里被设值进行推断，基于本地化在哪里被使用进行推断）

## 0.7.8

* BUG修复
    * 为了避免意外，解析获取表达式对应的CWT规则时，不再使用全局缓存，而是使用基于表达式对应的PSI元素的缓存（当所在文件被修改时会失效）
* 功能优化
    * [X] 可以通过导航到实现功能（`Navigate > Implementation(s)`）来从某一封装变量/定义/本地化/复杂枚举值跳转到所有同名的重载或者被重载的声明处
        * 注意对于非顶级属性名的定义名（如`some_event_id`）和复杂枚举值（如`policy_flag`
          ），从声明处出发，需要间接通过意向（`Intentions`）来查找使用/导航到实现/导航到类型声明
    * [X] 
      查找使用时，即使声明处与引用处的名字文本不同，也能正确进行，同时鼠标放到声明或使用处时也能正确显示引用高亮（例如：`GFX_text_unity` > `unity`）
    * [X] 优化如何提供类型信息（`View > Type Info`）和快速类型声明（`View > Quick Type Definition`）
* 功能变更
    * [X] ~~Stellaris格式化引用（`format.xxx: "<some_parts> ..."`中的`<some_parts>`）~~ （移除支持，stellaris 3.6已经不再使用）
* 新增功能
    * 代码补全（`Code > Code Completion`）
        * [X] 当可能正在输入一个定义名（非引用，作为顶级属性名）时，可以自动插入后面的等号、花括号以及从句内联模版，并将光标放到合适的位置
        * [X] 当可能正在输入一个封装变量名（非引用）时，提供基于已索引的封装变量名的代码补全，以便进行覆盖
        * [X] 当可能正在输入一个定义名（非引用）时，提供基于已索引的定义名的代码补全，以便进行覆盖
        * [X] 当可能正在输入一个本地化名（非引用）时，提供基于已索引的本地化名的代码补全，以便进行覆盖
    * 补充当前声明（`Code > Code Completion > Complete Current Statement`）
        * [X] 对于脚本文件：`k` > `k = `
        * [X] 对于本地化文件：`KEY` > `KEY:0 ""`（光标放到引号之间）
        * [X] 对于CWT文件：`k` > `k = `
    * 代码检查（`Code > Inspect Code...`）
        * [X] 实现代码检查：定义声明中无法解析的表达式（基于CWT规则文件，错误级别）
        * [X] 实现代码检查：定义声明中缺失的表达式（基于CWT规则文件，错误级别）
        * [X] 实现代码检查：定义声明中过多的表达式（基于CWT规则文件，弱警告级别）
    * 动态模版（`Code > Insert Live Template...`）
        * [X] 可以自定义脚本文件的动态模版
        * [X] 可以自定义本地化文件的动态模版
        * 可以通过`Code > Save as Live Template...`将选中的文本保存为动态模版，以便快速编写脚本或者文本
    * 其他
        * [X] 支持事件ID中对事件命名空间的引用
        * [X] 支持通过快速文档和内嵌提示显示修饰符（`modifier`）的图标和本地化名字（需要确定具体规则是什么）

## 0.7.7

* 支持VIC3（基于CWT规则文件提供基本的游戏类型支持）
* BUG修复
    * [X] 修复无法识别直接作为字符串写在脚本文件顶层的复杂枚举值的问题（例如，`component_tags`）
    * [X] 修复格式化时对嵌套的块（子句）的缩进问题
* 优化
    * [X] 进行代码补全时，尽可能兼容表达式用引号括起的各种情况
    * [X] 进行代码补全时，在输入`definition_root_key = `之后，也可以应用代码补全
    * [X] 进行代码补全时，在提供定义声明中的定义成员的补全之外，也可以提示定义声明之外的属性名（如果允许）
    * [X] 进行代码补全时，如果要基于从句内联模版进行代码补全，首先弹出对话框，让用户选择需要插入的内容
        * 需要插入的内容（属性/值）可多选，可排序，可重复（不基于CWT规则判断是否允许重复）
    * [X] 导航到相关的CWT规则时，位置也可以是定义的rootKey
* 新增功能
    * [X] 支持CWT规则：`stellaris_name_format[xxx]`
        * （需要确定3.6是否仍然支持）如果未用引号括起，则对应一个本地化的名称，本地化文本中可以使用格式化引用`<some_parts>`
          ，而`some_parts`对应CWT规则`value[x]`
        * （3.6开始支持）如果用引号括起，则是一个特殊的表达式，例如`"{AofB{<imperial_mil> [This.Capital.GetName]}}"`
        * 需要进一步完善……
    * [X] 支持语法：Stellaris格式化引用（`format.xxx: "<some_parts> ..."`中的`<some_parts>`），实现相关的代码高亮，引用解析、代码补全、代码检查等功能
        * 当游戏类型为Stellaris，且本地化的名字以`format.` `format_prefix.`或者`format_noun.`开始时，认为对应的本地化文本中支持格式化引用语法
        * 3.6开始似乎已经弃用这种语法？但是本地化文件中仍然保留。

## 0.7.6

* 更新IDEA版本到2022.3
* 优化
    * [X] 整合内部配置到CWT配置（作为全局的配置，要求检查文件名）
    * [X] 如果无法解析值表达式（`k = v`中的`v`
      ），如果存在，在代码检查的错误信息中提示可能的CWT规则表达式（如，`possible: <event>, {...}`）
    * [X] 将非法的表达式`k = k1 = v`中的`k1`解析成key而非value，以便在某些情况下，例如使用从句内联模版进行代码补全时，能提供正确的高亮
* 新增功能
    * [X] 提供包围选项：包围成从句（`k = v` → `{ k = v }`）和包围成值为从句的属性（`k = v` → `key = { k = v }`）
    * [X] 提供包围选项：用参数条件块包围（`k = v` → `[[PARAM] k = v ]`）
* 新增功能 - 群友提出
    * [X] 实现后缀补全：对于变量操作表达式，如`var.setv` → `set_variable = { which = var value = ? }`
        * 基于CWT配置（配置文件：`postfix_template_settings.pls.cwt`）
        * 目前用处不大：在可以进行后缀补全的位置，并不会提供对变量名的提示
    * [X] 实现代码折叠：对于变量操作表达式，如`set_variable = { which = var value = 1 }` → `var = 1`
        * 基于CWT配置（配置文件：`folding_settings.pls.cwt`）
    * [X] （可配置是否启用）进行代码补全时，如果在提示定义属性的键时，如果其值可能是从句，应用补全后可以自动插入从句内联模版，让用户依次输入各个未定的值
        * 仅当从句中允许键为常量字符串的属性时才会提示
        * 实际上，如果key或value表示数字（但其文本不一定要是数字），那么key和value之间应当可以是任意比较操作符
        * 不需要另外编写cwt规则文件，在加入提示项时判断即可

## 0.7.5

* 更新CWT规则文件到最新版本

## 0.7.4

* BUG修复
    * [X] 修复无法从项目文件中的声明导航到库中的引用的问题（考虑使用`UseScopeEnlarger`或`ResolveScopeManager`）
    * [X] 可以从定义名并非rootKey的定义（如event）的声明处导航到所有使用处（鼠标放到定义的rootKey上，然后Ctrl+鼠标左键）
    * [X] 从任意同名同类型的封装变量/定义/本地化/文件路径出发，可以通过查找使用导航到所有那个名字的使用
    * [X] 兼容更复杂的表达式的情况，如：`root.owner.event_target:target@root.owner`
    * [X] 兼容`value_field`或者`int_value_field`需要被识别为变量的情况（`root.owner.var_name`
      ）（通过更新CWT规则文件`links.cwt`）
    * [X] 脚本文件中来自CWT文件的引用需要能被同时高亮出来，同时一般情况下不能从CWT文件中的规则查找引用（`modifier`等除外）
    * [X] 兼容localisationCommandScope需要被识别为`value[event_target]`或者`value[global_event_target]`的情况，以及代码提示
    * [X] 兼容localisationCommandField需要被识别为`value[variable]`的情况，以及代码提示
    * [X] 修复不能为对应的CWT规则是别名（alias）的表达式（key / value）提供正确的引用读写高亮的问题（当鼠标放到表达式上时应当显示）
    * [X] 修复无法跳转到某些定义成员对应的CWT规则的问题
* 功能优化
    * [X] 对CWT别名规则（dataType=alias/single_alias）使用特殊的别名图标，以便区分内联前后的CWT规则
    * [X] 
      在单纯地匹配CWT规则以找到对应的CWT规则时，不应该要求索引，否则可能会引发IDE异常：`java.lang.Throwable: Indexing process should not rely on non-indexed file data.`
    * [X] （可配置是否启用）进行代码补全时，如果在提示定义属性的键时，如果其值可能是常量字符串，应用补全后可以自动插入
    * [X] （可配置是否启用）进行代码补全时，如果在提示定义属性的键时，如果其值可能是从句，应用补全后可以自动插入花括号并将鼠标放到花括号中
    * [X] 本地化文件：兼容`$@some_scripted_variable$`、`£$SOME_REF$£`这样的语法
    * [X] 优化插件配置页面
* 功能变更
    * [X] ~~支持额外的CWT选项：`## icon = <icon_type>`，用于重载进行代码补全时需要显示的图标，如`## icon = tag`~~ →
      使用CWT选项`## tag`标记特殊标签，如`optimize_memory`
    * [X] 移除`icu.windea.pls.core.ParadoxPathReferenceProvider` （用于兼容markdown锚点）
* 新增功能
    * [X] 实现检查：参数（`$PARAM$`）被设置/引用但未被使用（例如：有`some_effecFt = { PARAM = some_value }`
      但没有`some_effect = { some_prop = $PARAM$ }`，后者是定义的声明。）
    * [X] 实现检查：值集值值（`some_flag`）被设置但未被使用（例如，有`set_flag = xxx`但没有`has_flag = xxx`。）
    * [X] 实现检查：值集值值（`some_flag`）被使用但未被设置（例如，有`has_flag = xxx`但没有`set_flag = xxx`。） - 默认不启用
    * [X] 实现检查：无法解析的命令作用域（unresolvedCommandScope）
    * [X] 实现检查：无法解析的命令字段（unresolvedCommandField）
    * [X] 实现内嵌提示：本地化图标（渲染出选用的内嵌图标，如果对应图标的大小合适）
    * [X] 实现内嵌提示：预定义修饰符的本地化名字（`mod_$`）
    * [X] 实现动作：导航到（对应的）CWT规则（对于定义成员，在导航菜单/右键菜单中）（作为一个更加统一的入口，包括内联前后的CWT规则，包括所有完全匹配的规则）
    * [X] 在查找使用中，区分参数和值集值值的读/写使用
    * [X] 在查找使用中，区分使用的使用类型（基于读写和对应的CWT规则）（待日后完善） *
    * [X] 可设置要忽略的本地化文件的名字
    * [X] 为图标提供提示（tooltip），例如，鼠标悬浮到结构视图（Structure）中的节点图标上即可看到
    * [X] 可以提示本地化颜色的ID（由于颜色ID只有一个字符，当光标在本地化文本中且前一个字符是`"§"`
      时，手动调用代码提示功能（例如，按下`Ctrl+空格`），才会进行提示
    * [X] 提供布尔值的代码补全（在定义声明中不为属性或者块中的值提供）
* 完善CWT规则支持
    * [X] 支持`complex_enum`，以及相关功能：匹配、代码提示
    * [X] 支持高亮`definitionName` `complexEnumValueName`（对应的PSI元素可能本身就对应着特定的CWT规则，需要同时高亮出来）
    * [X] 为`complexEnumValue`的引用（而非声明）提供特殊文档
    * [X] 以某种方式另外实现`definitionName` `complexEnumValueName`的文档、查找使用、导航到类型声明等功能 -
      通过意向（intention）
    * [X] 为预定义的`modifier`提供相关本地化支持（`mod_$` `mod_$_desc`等，需要确定具体规则是什么） *

## 0.7.3

* [X] 更新cwt规则到最新版本（2022/10/13）
* 功能优化
    * [X] 如果通过代码检查发现一个定义上有多个缺失的相关本地化/图片，将每个单独作为一个问题
    * [X] 参数（`$PARAM$`）和值集值值（`some_flag`）并不存在一个事实上的声明处，点击需要导航到所有引用处
    * [X] 测试通过对参数（`$PARAM$`）和值集值值（`some_flag`)基于引用的统一重命名
* BUG修复
    * [X] 进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
    * [X] 修复解析本地化位置表达式（如`$_desc`）时把占位符`$`解析成定义的rootKey而非定义的名字的问题
    * [X] 解析本地化位置表达式（如`$_desc`）时如果存在占位符`$`但对应的定义是匿名的，则应直接忽略，返回空结果
    * [X] 修复`CwtConfigResolver`中的NPE
    * [X] 修复`CwtImageLocationExpression`中的SOF
    * [X] 修复valueSetValue索引在索引时会被IDE认为栈溢出的问题，改为基于`ParadoxValueSetValuesSearch`和索引进行查找
    * [X] 脚本文件 - 基于注解器的语法高亮，不高亮带有参数的表达式
    * [X] 修复无法从项目文件中的声明导航到库中的引用的问题（默认情况下，对应的库需要导出）
* 新增功能
    * [x] 实现动作：导航到相关本地化和导航到相关图片（对于定义，在导航菜单/右键菜单中，在动作"导航到相关符号/Go to related
      symbol"下面）
* 功能变更
    * 支持一些特殊注释
        * [X] 通过在脚本文件的第一行添加特殊注释`@path:{gameType}:{path}`，可以强制指定脚本文件相对于模组根目录的路径
            * 仅当PLS无法得到此脚本文件的文件信息，从而进一步解析其中的定义时才允许这样处理
            * 特殊注释不能包含任何其他非空白文本，合法的特殊注释文本示例：`@file:stellaris:events/dummy.txt`
        * [X] 通过在脚本属性的上一行添加特殊注释`@type:{gameType}:{type}`，可以强制指定定义的类型（不要同时指定子类型）
            * 仅当PLS无法得到此脚本文件的文件信息，从而进一步解析其中的定义时才允许这样处理
            * 特殊注释不能包含任何其他非空白文本，合法的特殊注释文本示例：`@type:stellaris:civic_or_origin`
            * 定义的rootKey仍然需要匹配

## 0.7.2

* BUG修复

## 0.7.1

* BUG修复
* 功能优化
    * [X] multiResolve本地化时不指定偏好的语言区域
    * [X] 优化paradoxSelector
    * [X] 补充内嵌提示的预览文本
* 新增功能
    * [X] 通过在脚本文件的第一行添加特殊注释`@path:{gameType}:{path}`，可以强制指定脚本文件相对于模组根目录的路径
        * 仅当PLS无法得到此脚本文件的文件信息，从而进一步解析其中的定义时才允许这样处理
        * 特殊注释不能包含任何其他非空白文本，合法的特殊注释文本示例：`@file:stellaris:events/dummy.txt`
    * [X] 通过在脚本属性的上一行添加特殊注释`@type:{gameType}:{type}`，可以强制指定定义的类型（不要同时指定子类型）
        * 仅当PLS无法得到此脚本文件的文件信息，从而进一步解析其中的定义时才允许这样处理
        * 特殊注释不能包含任何其他非空白文本，合法的特殊注释文本示例：`@type:stellaris:civic_or_origin`
        * 定义的rootKey仍然需要匹配

## 0.7.1

* BUG修复

## 0.7.0

* 更新IDEA版本到2022.2
* 对模组描述符文件使用特定的图标

## 0.6

* [X] 更新cwt规则到最新版本（2022/6/10）
* [X] 将CWT配置移动到项目根目录下`cwt`目录中，以便随远程GIT仓库更新CWT配置
* BUG修复
    * [X] 
      修复：[.${gameType} file is ignored #3](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/3)
    * [X] 
      修复：[Cyan color support in localisation #4](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/4)
    * [X] 修复：[Bugs in translation #6](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/6)
    * [X] 修复：对声明的查找使用的结果不完整（定义，本地化，参数等）
    * [X] 尝试修复：访问缓存（CachedValue）时导致的PsiInvalidElementAccessException（限制太多，暂时避免使用）
* 代码优化
    * [X] 提供方法以及检查代码：当需要获取定义或代码块的属性/值的时候，可以获取参数表达式中的属性/值
    * [X] 检查代码：同一代码块（block）中允许同时存在属性和值，且可以同时被CWT规则校验
    * [X] 绝不把本地化文件夹（如`localisation`）中的文件视为脚本文件，绝不把本地化文件夹之外的文件视为本地化文件
    * [X] 本地化颜色ID可以是数字
    * [X] 优化文件类型的重载逻辑（使用`ParadoxRootInfo`和`ParadoxFileInfo`保存上下文信息，监听描述符文件和游戏类型标记文件）
    * [X] 脚本文件中的文件路径分隔符兼容"/" "\" "\\"混用
    * [X] 解析引用时限定相同的游戏类型，并且如果可用，优先选用同一游戏或模组根目录下的
* 功能优化
    * [X] 结构视图-脚本文件：如果存在，显示定义的本地化名字（最相关的本地化文本）（以方便定位需要浏览的定义）
    * [X] 本地化文件：仅允许在正确的位置（`string_token`）插入动态模版
    * [X] 优化代码补全性能：尽可能确保提示本地化图标和属性引用时不会导致`ProcessCanceledException`
    * [X] 可以根据扩展的CWT规则显示和设置block所对应的颜色
    * [X] 脚本文件：可以显示类型信息以及导航到类型定义，如果可用 - 支持定义成员，显示其规则表达式以及导航到规则声明
    * [X] 解析本地化颜色直接基于`textcolor`类型的定义，在`shared.cwt`中定义，移除内置规则中的`colors`
    * [X] 对于修饰符`X`，如果存在本地化`mod_X`，在文档注释中渲染对应的本地化文本
    * [X] 优化导入游戏或模组目录时弹出的对话框
    * [X] 优化导入游戏或模组目录时弹出的对话框 - 在文件选择器中提供额外的工具栏按钮，可以快速选中Steam游戏目录、Steam创意工坊目录等
    * [X] 优化动态模版，移除硬编码的颜色码枚举（尽管仍然不够直观，或许应该考虑顶部工具类 / 悬浮工具栏？）
    * [X] 初步集成[Translation](https://github.com/YiiGuxing/TranslationPlugin)插件，用于翻译本地化文本
    * [X] 如果定义支持参数且拥有参数，则在文档中显示
    * [X] 优化：`Navigate > Related Symbol...`显示的弹出框中，使用所属文件的绝对路径+模组名（如果有）+模组版本（如果有）表示位置信息
    * [X] 优化：`View > Quick Definition`显示的文本中也包含定义的相关注释
    * [X] 完善快速类型定义功能（`View > Quick Type Definition`）
    * ~~将游戏类型和游戏/模组目录依赖的配置保存到游戏或模组根目录下的特定配置文件（暂定为`.pls.settings.json`
      ）中，将游戏/模组目录依赖视为合成库（参见`AdditionalLibraryRootsProvider`）~~
* 新增功能
    * ~~
      当用户新打开的项目中被识别包含模组文件夹时，如果没有将对应的游戏目录作为依赖添加到对应的项目/模块，弹出右下角通知要求用户添加，如同CWTools一样。~~
      （需要延迟判断，何时判断？）
    * [X] 本地化文件：提供快速更改文本颜色的悬浮工具栏
    * [X] 支持自定义的本地化颜色
    * [X] 脚本文件：本地封装变量（`scripted_variable`）的提取和快速修复（无法解析时）
    * [X] 脚本文件：全局封装变量（`scripted_variable`）的提取和快速修复（无法解析时）
    * [X] 实现快速修复：导入游戏目录或模组目录
    * [X] 实现代码检查-本地化文件：不正确的文件名
    * [X] 在项目视图和右键菜单中添加“将DDS图片转化为PNG”图片的动作，可以批量转化
    * [X] 实现代码检查-脚本文件：无法解析的路径
    * [X] 实现代码检查-脚本文件：缺失的相关本地化
    * [X] 实现代码检查-脚本文件：缺失的相关图片
    * [X] 实现意向：复制本地化（复制的文本格式为：`KEY:0 "TEXT"`）
    * [X] 兼容通过翻译插件翻译定义、本地化、CWT属性或值的文档
    * [X] 脚本文件：对于effect/trigger等的参数名的参数提示
    * [X] 提供定义和本地化语言区域的文件结构支持（`Navigate > File Structure`）
    * [X] 脚本文件：对于SV表达式中的SV参数名的参数提示
    * [X] 实现代码检查-脚本文件；不正确的脚本结构
    * [X] 实现查找实现功能 - 可导航到所有同名的封装变量/定义/本地化
    * [X] 实现`ParadoxQualifiedNameProvider`，用于复制路径/引用（Edit > Copy Path/Reference...）
* 完善内嵌提示
    * [X] 脚本文件：值集值值的内嵌提示（值的类型即值集的名字，`xxx = value_set[event_target]`中的`event_target`）
* 脚本文件语法解析优化
    * 兼容`common/scripted_effects/99_advanced_documentation.txt`中提到的高级语法
    * [X] 对于`stellaris v3.4`开始新增的`tag`（`optimize_memory`），提供特殊图标和代码高亮，代码提示和验证功能另外由CWT规则提供
    * [X] 兼容`inline_math`语法，以及相关功能：代码高亮、代码补全、代码折叠、引用解析、格式化、代码风格设置
    * [X] 兼容`parameter`语法，以及相关功能：代码高亮
    * [X] 兼容`string_template`语法，以及相关功能：代码高亮
    * [X] 兼容`parameter_condition`语法，以及相关功能：代码高亮、代码折叠、格式化、代码风格设置
    * [X] 获取封装变量名时不再包含作为前缀的"@"
    * [X] 封装变量的值也可以是bool类型（`yes`和`no`）
* 本地化文件语法解析优化
    * [X] 删除`sequentialNumber`，作为一种特殊的`propertyReference`（如`$VALUE$`）（`stellaris v3.4`开始其语法由`%O%`
      改成了`$O$`）
    * [X] 支持`iconFrame`（`£leader_skill|3£`中的`3`，可以为变量，如`$LEVEL$`）
    * [X] 支持本地化语言`l_japanese`和`l_korean`
    * [ ] 兼容作为format的本地化中的`<some_part>`语法，其中`some_part`对应特定类型的定义（需要修改原始的CWT规则）
* 内置配置
    * [X] 添加`predefined_variables`，表示预定义的变量，在本地化文本中使用，实现相关功能：引用解析
* CWT配置
    * [X] ~~支持额外的CWT选项：`## since = "3.3"`，用于在文档注释中显示开始支持的游戏版本号~~（已移除）
    * [X] 支持额外的CWT选项：`## format = html`，用于指定将文档注释渲染为HTML
    * [X] 支持额外的CWT选项：`## icon = <icon_type>`，用于重载进行代码补全时需要显示的图标，如`## icon = tag`
    * [X] 支持额外的CWT选项：`## color_type = rgb / rgba`，用于以装订线图标的方式显示对应的颜色
    * [X] 仅当对应的数据类型是`bool`时，才会提示`yes`和`no`
    * [X] 支持的额外的CWT规则：`types.type[*].pictures`改为`types.type[*].images`
* ［长期］完善CWT规则支持
    * [X] 支持解析scopeExpression（如`from.owner`）
    * [X] 支持额外的值类型：`value`和`value_set`，以及相关功能：匹配、代码提示、引用解析、索引（多对多）
    * [X] 支持值类型：`scope` `scope_field` `scope_group`，以及相关功能：匹配、代码提示、引用解析、代码检查（暂不精确匹配scope）
    * [X] 支持值类型：`value_field`，以及相关功能：匹配、代码提示、引用解析（暂不精确匹配scope，不要求值类型（float/int）必须匹配）
    * [X] 支持值类型：`value_field`的特殊SV语法（`value:xxx|PN|PV|`）以及相关功能
    * [X] 支持在文档注释中提示alias modifier localisation_command的所有支持的作用域的名字（supported scopes）
    * [X] 优化获取和应用定义成员（属性和值）的CWT规则的代码逻辑（基于CWT规则和elementPath，用于实现代码提示、代码检查等）
    * [X] 支持匹配和提示参数名（`some_effect = { PARAM = xxx }`中的`PARAM`）
    * [X] 优化：支持匹配、提示和解析参数，以及显示参数信息 （当可用时）
    * [X] 本地化文件：支持提示和解析本地化命令作用域（commandScope）
    * [X] 优化：提示modifier时排除不匹配supported_scopes的，提示scope时排除其input_scopes不匹配上一个scope的output_scope的
    * [X] 优化：提示scopeLinkPrefix和scopeFieldDataSource时排除其input_scopes不匹配上一个scope的output_scope的
    * [X] 优化：提示valueLinkPrefix和valueFieldDataSource时排除其input_scopes不匹配上一个scope的output_scope的
    * [X] 优化：`scope_field`支持`event_target:revolt_situation.owner`的写法
* ~~不再内置CWT配置文件，改为需要以库的方式添加到项目和模块中（Cwt Config，CWT配置）~~（没有必要提供）
    * 可以通过内置的GitHub链接从网络下载需要的CWT配置文件（使用Git克隆远程仓库，然后保存到本地）
    * 包括官方GitHub链接~~和镜像GitHub链接（https://github.com.cnpmjs.org）~~
    * 检查GitHub上最新的CWT配置文件仓库，提高CWT配置文件语法解析的兼容性
    * 每次打开项目时，检查已添加的CWT配置库的更新（`git pull`）

## 0.5

* [X] 更新IDEA版本到2022.1
* [X] 更新内置的CWT配置
* [X] BUG修复
* [X] 语法解析优化
* [X] 从注解器提取出单独的本地检查
* [X] 优化DDS图片渲染
* [X] 优化DDS图片渲染：监听DDS文件内容的更改以移除缓存
* [X] 优化DDS图片渲染：现在可以渲染切分后的DDS图片（如果需要）
* [X] 脚本文件：可以显示类型信息以及导航到类型定义，如果可用
* [X] 对模组描述符文件`descriptor.mod`提供插件额外提供的（共用的）规则支持
* [X] 不再通过`ParadoxFileTypeOverrider`自动添加或移除文件的BOM，而是基于`ParadoxUtf8BomOptionProvider`判断创建文件时是否需要添加BOM
* [X] 同一本地化文件中允许有多个语言区域，以及各自的本地化（如游戏目录中的`localisation/languages.yml`），提供重复的语言区域的代码检查
* [X] 支持解析和提示filePath和iconName（相对于游戏或模组根路径的文件路径，仅文件）
* [X] 完善文件视图
    * [X] 脚本文件：对于本身是定义的脚本文件，显示定义的额外信息（定义的名字和类型）
* [X] 完善结构视图
    * [X] 脚本文件：显示变量、属性的名字，值的截断后文本
    * [X] 脚本文件：显示变量的名字和额外信息（变量的值）
    * [X] 脚本文件：显示定义的额外信息（定义的名字和类型），覆盖属性的，并使用定义的图标
    * [X] 脚本文件：对于本身是定义的脚本文件，也显示定义的额外信息（定义的名字和类型）
    * [X] 脚本文件：显示语言区域的ID和描述，作为属性和本地化的父节点
    * [X] 脚本文件：显示属性的键名
    * [X] 脚本文件：显示本地化的键名，覆盖属性的
* [X] 支持显示上下文信息
    * [X] 定义成员的上下文信息（如：`definitionKey = {`）
    * [X] 本地化（属性）的上下文信息（如：`l_english:`）
* [X] 完善对本地化图标的支持，尽可能保证：
    * [X] 鼠标悬浮能在文档中渲染出对应的DDS图片，或者对应的sprite定义的文档
    * [X] 点击可导航到正确的位置
    * [X] 提供代码补全
    * [X] 添加检查：无法解析的本地化图标（暂不支持某些生成的图标）
* [X] 完善装订线图标
    * [X] 定义的装订线图标（鼠标悬浮显示定义信息，可点击导航到同名定义）
    * [X] 相关本地化的装订线图标（鼠标悬浮显示相关本地化信息，可点击导航到对应本地化信息）
    * [X] 相关图片的装订线图标（鼠标悬浮显示相关图片信息（或者DDS文件的filePath），可点击导航到对应的DDS文件）
    * [X] 仅显示且仅可导航到有对应的定义、本地化或DDS文件的
    * [X] 优化：添加分组
    * [ ] 按照覆盖优先级排序，优先级最高的放到前面，自身需要置顶
    * [ ] 如果覆盖或者被覆盖，需要以某种形式显示出来（图标和文字提示）
* [X] 完善内嵌提示
    * [X] 脚本文件：定义信息的内嵌提示（定义的名字和类型）
    * [X] 脚本文件：定义的本地化名字的内嵌提示（最相关的本地化文本）
    * [X] 脚本文件：变量引用信息的内嵌提示（变量的值）
    * [X] 脚本文件：定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）
    * [X] 脚本文件：定义引用的本地化名字的内嵌提示（最相关的本地化文本）
    * [X] 脚本文件：本地化引用信息的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）
    * [X] 如有必要，内嵌提示中的相关文本可通过`Ctrl+鼠标左键`导航到对应的位置
    * [X] 如有必要，将内嵌提示中的相关文本渲染为富文本，并且需要考虑截断（多行的话从第二行开始必定被截断掉）
* [X] 提供对DDS文件的部分支持（文件图标，文档注释，预览页面等）
    * 参考IDEA对图片文件的支持
* [X] 在重载文件类型时（`ParadoxFileTypeOverrider`），缓存模组描述符中的信息，以便在文档注释中显示出来
* [X] 在需要显示文件信息的地方显示更详细的文件信息（如所属游戏或者所属模组的名字和版本）
* [X] 可以配置主要的语言区域
* [X] 可以配置要忽略（识别为脚本或本地化文件）的文件
* [X] 为`descriptor.mod`默认注册文件类型为脚本文件
* [X] ~~提供无法解析的文件路径（abs_filepath, filepath, icon）的检查（默认忽略lua文件）~~（忽略，考虑整合到CWT检查中）
* [X] 完善对内置配置文件的支持，完善相关功能
    * [X] 将配置文件格式由YAML改为CWT，以便可以直接导航到文件中的相关规则
* [X] 完善对CWT配置文件的支持，完善相关功能
    * [X] 支持额外的值类型：`abs_filepath[...]`，以及相关功能：引用解析
    * [X] 支持额外的CWT规则：`types.type[*].pictures`，用于提供对定义的相关图片的支持，以及相关功能：装订线图标，渲染到文档注释
    * [X] 类型为枚举值、常量、别名名字的键/值的解析需要忽略大小写
* [X] 显示相关信息时基于更合理更完善的`ItemPresentation`

## 0.4

* [X] 更新IDEA版本到2021.3

## 0.3

* [X] 初步支持cwt config语言
* [X] 更新并统一文件图标
* [X] 解析cwt config语言的optionComment和documentComment
* [X] 修复格式化功能的BUG
* [X] 初步实现`CwtConfigResolver`
* [X] 实现`CwtConfigGroupsProvider`
* [X] 更换包名
* [X] 支持基于cwt配置文件识别脚本文件中的定义（definition），并在gutterIcon、documentation等地方中显示
* [X] 解决BUG：origin和civic同时匹配了，可选的config的匹配逻辑存在BUG
* [X] 解决BUG：config的name可能重复，这时只要匹配其中之一即可（要求是连续的）
* [X] 更新克隆脚本，增强本地化文件语法兼容性，更新规则文件
* [X] 实现paradoxScript的InlayHintsProvider，目前为定义提供来自本地文件的名字（如：对于特质`agenda_defensive_focus`
  ，名字是`保卫边境`）
* [X] 实现`DdsToPngConverter`，基于放在jar包中的小工具`dds2png.zip`，可以将dds文件转化成png文件（Windows平台，插件中暂未使用）
* [X] 基于gfx文件中的spriteDefinition（`spriteType = { name = ... texturefile = ... }`）解析图标（`paradoxIcon`）
* [X] 图标（`paradoxIcon`，本地化文件会使用，在gfx文件中通过`spriteType = { ... }`定义）的索引以及代码提示
* [X] 解决BUG：解析图标生成的html无法正常渲染图标，替换成本地解析dds
* [X] 使用本地渲染渲染图标
* [X] 修复BUG，definition.name重新设为区分大小写，判断的时候可能会忽略
* [X] 本地化图标的查找：
    * [X] 首先查找是否有对应的定义`spriteType = { ... }`，且`name`属性带有前缀`GFX_text_`
    * [X] 然后直接查找是否有对应名字的dds文件，且位于文件夹`gfx/interface/icons`下
    * [X] 也有可能是生成的，即有对应的定义X，声明使用定义Y的图标，那么本地化图标`...X...`会使用本地化图标`...Y...`（忽略）
* [X] 提示脚本文件的定义的名称类型信息+本地化名字
* [X] 根据localisation的name进行代码补全时，预先根据关键字过滤结果，防止因为结果太多导致无法正确进行提示
* [X] 区分localisation和localisation_synced
* [X] 兼容cwt规则`only_if_not`
* [X] cwt文件：`option.value`可以无需双引号直接包含空格（主要对于规则`display_name`）
* [X] 优化cwt配置规则`type[...].localisation`的`name`的解析逻辑（可以为`$` `"$_desc"`也可以为`title` `name`）
* [X] 提示定义的本地化名字（对应的localisation.name为name或title）
* [X] 修复cwt规则文件解析问题（有时无法解析optionComment中的`<>`）
* [X] 即使cwt规则文件中没写也为definition补充某些definitionLocalisation，从已有的推断（如果有的话）（name title desc effect）
* [X] 实现LineMarkerProviderDescriptor改为实现RelatedItemLineMarkerProvider，从而实现`Goto related symbols`
* [X] 基于cwt规则文件进一步解析cwt规则，尽管其中一部分暂未使用
* [X] 完善代码风格设置
* [X] 可以通过定义的文档中的链接跳转到定义的类型和子类型所在的cwt文件
* [X] 为cwtProperty提供文档注释（以`###`开头的）
* [X] 支持提示definition的顶级的key（尚未支持所有的cwt数据类型，即`int`,`scalar`之类）
* [X] 支持提示definition的所有级别的key（尚未支持所有的cwt数据类型，即`int`,`scalar`之类）
* [X] 支持提示definition的value（尚未支持所有的cwt数据类型，即`int`,`scalar`之类）
* [X] 代码提示大小写兼容性解决（判断属性是否已存在以及存在数量时忽略大小写）
* [X] ~~避免得到缓存数据的过程中抛出ProcessCanceledException~~
* [X] 兼容引号括起的key和value，同样提示并妥善处理引号
* [X] 支持提示block中的value
* [X] 仅当有必要进行提示时提示
* [X] 基于cwt规则文件的key和value的代码提示的支持
* [X] 基于cwt规则文件的key和value的引用的支持（提供查找使用，定位定义，颜色高亮等功能）
* [X] 支持cwt规则文件中的enum的引用（提供查找是引用，定位定义，颜色高亮等功能）
* [X] 进行匹配时支持aliasName和aliasMatchLeft
* [X] 进行代码提示时支持aliasName
* [X] 进行代码提示时支持aliasName 兼容aliasSubName为为expression的情况 仅提示必要的
* [X] 进行代码提示时支持valueName
* [X] 提示localisationCommand（commandField）
* [X] definitionElement对应的规则如果是const（即相同名字的规则），则将规则对应的cwtKey/cwtValue作为它的引用
* [X] 修改脚本文件的类型提示逻辑
* [X] 图标更改
* [X] cwt规则文件中兼容`key == value`的格式，等价于`key = value` *
* [X] ~~解析`config/stellaris/setup.log`中的`modifierDefinitions`~~
* [X] 允许在克隆过来的cwt配置文件（在`config/{gameType}`中）的基础上，提供额外的cwt配置文件（在`config/{gmeType}-ext`中） *
* [X] 解析`modifiers.log`中的modifierDefinitions，并进行匹配和提示
* [X] 如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
* [X] 优化插件设置相关代码
* [X] 重载文件类型时自动处理bom（改为正确的bom，不改变编码）
* [X] 支持规则类型alias_keys_field（匹配alias的aliasSubName，和alias_name应该没有区别）
* [X] 编写脚本从`modifiers.log`生成`modifiers.cwt`（已经编写了从`setup.log`生成`modifiers.log`的脚本）
* [X] modifiers从cwt规则文件中的`modifiers = { $name = $scope }`中解析，而非从`modifiers.log`
* [X] 匹配、提示和解析modifier
* [X] 提示modifier时匹配scope
* [X] 为modifier提供特殊的文档注释
* [X] 编写代码准备从alias/definitionElement/subtype推断scope和scopeMap
* [x] 为localisation_command/modifier提供关于scope的额外文档注释（未附加psi链接）
* [X] 为definitionElement提供关于scope的额外文档注释（未附加psi链接）
* [X] 为definitionElement提供关于scope的额外文档注释（附加psi链接）
* [X] 支持规则类型single_alias
* [X] 支持规则类型alias_match_left
* [X] 提示alias时匹配scope（来自`trigger_docs.log`或`triggers.log`，需要先转化为cwt，从名为`scope`或`scopes`的option中得到）
* [ ] 为alias补充名为`scope`或`scopes`的option（仅对于规则类型`alias_name[xxx]`）
* [X] 支持规则类型icon
* [X] 支持规则类型path
* [X] 应用cwt配置文件`folders.cwt`
* [X] 应用cwt配置文件`scopes.cwt`
* [X] 应用cwt配置文件`links.cwt`
* [X] 应用cwt配置文件`modifiers.cwt/modifier_categories.cwt`
* [X] 应用cwt配置文件`values.cwt`
* [X] 应用cwt配置文件`localisation.cwt` *
* [X] 生成本地化文本的文档注释时考虑并发解析其中的图标（不知为什么还是有点慢）
* [X] 不要缓存CwtKvConfig的scope和scopeGroup（因为内联的原因，parent可能有变动）
* [X] 修复当规则类型为alias_name或者引用为modifier时，无法解析definitionElementKey的引用的bug
* [ ] definitionElement本身就有对应的引用（如definition）的情况时，是否同样将规则对应的cwtKey/cwtValue作为它的引用？
* [ ] 基于cwt规则文件，对脚本文件的结构进行验证
* [X] scriptProperty的propertyName和definitionName不一致导致重命名scriptProperty时出现问题
* [X] 适配IDEA版本`2021.2`
* [X] ~~添加新的库作为依赖时（原始游戏目录，模组），兼容zip压缩包和文件夹~~（使用基于jar协议的url打开zip文件，实际测试发现读取不到内容）
* [X] 实现或重构用于提示definition的nameTypeInfo的InlayHint，可以单独启用
* [X] 实现或重构用于提示definition的localizedName的InlayHint，可以单独启用
* [X] 添加检查，检查事件的脚本文件中的namespace必须存在
* [X] 添加检查，检查事件的脚本文件中的event的id需要与namespace匹配
* [X] 基于cwt文件匹配scriptPropertyKey时，如果规则类型为常量，匹配时忽略大小写
* [X] 输入eventId时，提示`"${namespace}."`
* [X] 判断是否是游戏目录基于准确的执行文件名称，如`stellaris.exe` *

## 0.2

* [X] 更新IDEA版本到2021.1
* [X] 修复类加载错误

## 0.1

* [X] 从模块`stellaris-modding-support`拷贝文件到该模块
* [X] 文件名和文件内容的重命名
* [X] 重构`intentions`
* [X] 初步实现功能：添加库（基于游戏类型，包括游戏标准库）
* [X] 初步实现功能：库会显示在`External Libraries`中（自动适用，但是图标是默认的）
* [X] 初步实现功能：基于库的索引（自动适用，但需要进行一些调整？）
* [X] 修复外部引用能导航但不能正常渲染文档注释的bug
* [X] 解析`propertyPath`
* [X] 编写规则文件`types.yml`
* [X] 脚本文件兼容不等号`<>`
* [X] 解析`typeMetadata`的`name` `type` `localisation` `scope` `fromVersion`，TODO：`subTypes`
* [X] 解决`scriptProperty.paradoxTypeMetadata.name`和`scriptProperty.name`的兼容性问题
* [X] 编写规则文件`locations.txt`并应用
* [X] `typeMetadata`重命名为`definitionInfo`
* [X] 修复索引的相关bug（不保证索引的key完全正确）
* [X] 修复`00_edicts.txt`无限重复解析的bug（应当是scriptVariable索引的问题）
* [X] definition文档中列出definitionLocalisation
* [X] definition文档中列出definitionLocalisation并且绑定psi链接
* [X] 修复索引的相关bug
* [X] 添加配置`renderLineCommentText`
* [X] 尝试得到`definitionInfo`时，`element`必须是`scriptProperty`且`value`必须是`scriptBlock`
* [X] 解析`localisationCommandKey`
* [X] 让`PsiNamedElement`也实现`PsiCheckNameElement`
* [X] 改为使用自定义的`com.windea.plugin.idea.paradox.core.psi.PsiCheckRenameElement`，整理目录
* [X] 兼容`\u00a0`的空格
* [X] 解决`commandKey`无法自动补全的bug（lexer不够完善，没有兼容空格的情况？）
* [X] 解析规则文件`definitions.yml`的规则`value_type`
* [X] 解析规则文件`definitions.yml`的规则`name_from_value`
* [X] 为`types.yml`规则文件的schema补充描述。
* [X] 为`locations.yml`规则文件的schema补充描述。
* [X] 调试可以自动提示commandKey，但暂时不能自动提示localisation，操作被取消（？？？）
* [X] 移除标准库后，调试可以自动提示localisation，怀疑是调试环境性能原因
* [X] 修复规则文件`locations.yml`解析代码中的bug
* [X] 改为从规则文件读取枚举数据
* [X] 为本地化文件提示`locale` `serialNumber` `colorCode` `commandScope` `commandField`
* [X] 验证`commandScope` `commandField`
* [X] 修复不能changeLocale之类的bug
* [X] 完善对于`commandScope`和`commandField`的提示和验证
* [X] 解析规则文件`types.yml`的规则`predicate`
* [X] 解析规则文件`types.yml`的规则`subTypes`
* [X] 将`serialNumber`重命名为`sequentialNumber`
* [X] 实现`ParadoxScriptExpressionTypeProvider`
* [X] 实现`ParadoxPathReferenceProvider`
* [X] 修复`InvalidFileEncodingInspection`中的NPE
* [X] 完成规则文件`types.yml`
* [X] 改为根据正则指定需要排除的脚本文件
* [X] 提高脚本文件语法兼容性
* [X] 本地化文件使用正确的`localisationColor`
* [X] 添加`com.windea.plugin.idea.script.psi.ParadoxDefinitionTypeIndex`
* [X] 提高脚本文件和本地化文件的语法兼容性
* [X] 本地化文件渲染`propertyReference`时，如果有颜色参数，即使`propertyReference`未解析或者是变量，仍然正确渲染颜色，保留颜色参数
* [X] 实现本地化属性的`CopyRawTextIntention`、`CopyStringIntention`、`CopyRichTextIntention`
* [X] `DefinitionInfo`改为`TypeInfo`
* [X] `TypeInfo`改为`Definition`，支持解析`alias`，部分提取buildString的扩展
* [X] 更新项目文档和说明
* [X] 脚本文件支持根据规则文件提示propertyName（未测试，规则文件未完成）
* [X] 脚本文件支持根据规则文件提示propertyName，处理不能重复的情况（未测试，规则文件未完成）
* [X] 脚本文件支持根据规则文件提示propertyName，初步实现（已测试，规则文件未完成）
* [X] 脚本文件支持根据规则文件验证propertyName（未测试，规则文件未完成）
* [ ] ~~为规则文件`types.yml`添加规则`name_prefix`和`name_suffix`（已完成`common`目录）~~
* [ ] ~~为规则文件`types.yml`添加规则`icon`~~
* [ ] ~~解析规则文件`types.yml`的规则`type_from_file`~~
* [ ] ~~解析规则文件`types.yml`的规则`type_per_file`~~
* [ ] ~~编写规则文件`enums.yml`~~
* [ ] ~~修复图标能加载但不能正常渲染的bug（IDE新版本底层代码错误？）~~
* [ ] ~~初步实现功能：添加模块（基于游戏类型）~~
* [ ] ~~让`scriptRootBlock`直接或间接继承自`scriptProperty`~~
