# Changelog

## Unreleased

## 2.1.10-dev

- [ ] 提供动作，以进行定义/定义注入之间的差异比较 / Provide actions to compare differences between definitions and definition injections
- [ ] 支持形如 `event_target:target@root` 的动态链接节点 / Support dynamic link nodes in the form of `event_target:target@root`
- [ ] 支持数组定值引用表达式（`arrayDefineReferenceExpression`） / Support array define reference expressions (`arrayDefineReferenceExpression`)
- [ ] 支持动态值集合表达式（`dynamicValueSetExpression`） / Support dynamic value set expressions (`dynamicValueSetExpression`) ([#314](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/314))
- [x] 在推断游戏类型时，同时提供描述信息，并在模组设置对话框中显示 / Provide description info when inferring game type, and display it in the mod settings dialog
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes
- [x] 同步规则文件 / Synchronize config files

## 2.1.9 (2026-05-21)

- [x] 修复游戏类型发生变更时，分析数据可能未被正确刷新的问题 / Fixed an issue that analysis data may not be refreshed when game type is changed ([#326](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/326))
- [x] 支持在类型展示规则中使用嵌套的 `subtype[x] = {...}` / Supported nested `subtype[x] = {...}` in type presentation configs ([#324](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/324))
- [x] 支持在 CSV 文件中使用动态值（匹配与解析） / Supported to use dynamic values in CSV files (match and resolution)
- [x] 增强对数据类型 `ShaderEffect` （数据表达式：`$shader_effect`）的支持，视为动态引用 / Enhanced support for data type `ShaderEffect` (data expression: `$shader_effect`), treats as dynamic references
- [x] 增强对数据类型 `MeshLocator` （数据表达式：`$mesh_locator`）的支持，视为动态引用 / Enhanced support for data type `MeshLocator` (data expression: `$mesh_locator`), treats as dynamic references
- [x] 完善规则文件，适用 `$shader_effect` 和 `$mesh_locator` / Optimized config files, applying `$shader_effect` and `$mesh_locator`
- [x] 重新设计浏览、复制与打开各种特殊路径与地址的动作 / Redesigned actions to browse, copy and open various special paths and urls
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.1.8 (2026-05-10)

- [x] 修复模组描述符文件的图标与显示文本与预期不符的回归BUG / Fixed a regression bug where the icon and presentable text of the mod descriptor file were not as expected
- [x] 优化生成的修正的解析逻辑，兼容存在多个非精确匹配的候选项的情况 / Optimized resolution logic for generated modifiers, compatible with situations where there are multiple non-exact matching candidates
- [x] 在语法层面兼容内联数学表达式中的封装变量引用的前导的 `@` (如 `@[ @v + 1]`)，通过注解器检查 / Made compatible with leading `@` of scripted variable references in inline math expressions (e.g., `@[ @v + 1]`) on syntax level, checked by the annotator
- [x] 补充意向，以复制当前语言环境下的本地化列表到剪贴板 / Added intentions to copy localization list of current locale to the clipboard
- [x] 如果 CWT 文档注释至少存在4个前导的 `#`，则将注释文本视为 Markdown 文本 / If a CWT documentation comment has at least 4 leading `#`, the comment text is treated as Markdown text
- [x] 为规则选项提供文档注释，基于 schema 规则文件 / Provided doc comments for config options, from the schema config file
- [x] 优化配色方案 / Optimized color scheme
- [x] 优化本地化的生成逻辑，提供额外的配置项 / Optimized localisation generation logic, providing extra settings ([#296](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/296))
- [x] 优化 rootInfo 和 rootMetadata 的解析逻辑 / Optimized resolution logic for rootInfo and rootMetadata
- [x] 为定值的命名空间和变量提供特殊的图标、代码高亮和快速文档 / Provided special icons, code highlight and quick docs for define namespaces and variables
- [x] 为定值的命名空间和变量提供代码补全 / Provided code completion for define namespaces and variables
- [x] 为定值变量提供导航动作、装订线图标和实现查询 / Provided navigation action, gutter icons and implementations search for define variables
- [x] 为定值变量提供相关代码检查（重载、不正确的重载） / Provided related code inspections for define variables (override, incorrect override)
- [x] 引入定值规则，从而为定值的命名空间和变量提供快速文档文本和规则上下文 / Introduced define configs, to provide quick doc text and config context for define namespace and variables
- [x] 添加并更新 Stellaris 的定值的规则文件 / Added and updated define config files for Stellaris
- [x] 修复在某些情况下无法添加模组依赖的问题 / Fixed an issue that cannot add mod dependencies in some situations ([#323](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/323)) 
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.1.7 (2026-03-28)

- [x] 同步规则文件 / Synchronize config files
- [x] Refactoring via inlining doesn't substitute parametersk ([#289](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/289))
- [x] 修复：修复自 IDEA 2026.1 开始，无法预期地从 Steam 超链接跳转到客户端或启动游戏的问题 / FIX: Fixed an issue where starting from IDEA 2026.1, cannot jump to the client or launch the game from Steam hyperlinks as expected
- [x] 修复：修复自 IDEA 2026.1 开始，无法预期地在快速文档中渲染无需切分的 DDS/TGA 图片的问题 / FIX: Fixed an issue where starting from IDEA 2026.1, cannot render DDS / TGA images without splitting in quick doc as expected
- [x] 优化：为 `launcher-settings.json` 和 `metadata.json` 提供内置的 JSON Schema / OP: Provide built-in JSON Schema for `lanucher-settings.json` and `metadata.json`
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.1.6 (2026-03-21)

- [x] [EU5] Validating Required Fields for INJECT Statements ([#288](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/288))
- [x] 新功能：新增意向 `ChangeDefinitionInjectionModeIntention` / NEW: New intention `ChangeDefinitionInjectionModeIntention`
- [x] 优化：优化索引时的性能，将耗时降低到旧版本的 (Stellaris/VIC3/EU5 65%/47%/25%) / OP: Optimize performance during indexing, reducing the time taken to (Stellaris/VIC3/EU5 65%/47%/25%) of the old version
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes
- [x] 性能分析与优化 / Performance analysis and optimization
- [x] 完善参考文档 / Improve reference documentation

## 2.1.5 (2026-03-12)

- [x] 同步规则文件 / Synchronize config files
- [x] [Vic3] Scripted triggers not being recognized ([#279](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/279))
- [x] [Vic3] Link regression because of script value implementation ([#284](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/284))
- [x] 修复：修复无法正确地重载同路径的规则文件的回归 BUG / FIX: Fixed a bug that cannot correctly override same-path config files
- [x] 修复：修复和优化创建目录时的补全，兼容多种入口形式 / FIX: Fix and optimize create directory completion, compatible with various entry forms
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.1.4 (2026-03-04)

- [x] [Vic3/EU5] Script Value and Static Value ([#264](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/264))
- [x] Arguments with value no are not recognized as required arguments ([#278](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/278))
- [x] 修复：修复游戏概念未被正确解析的回归 BUG / FIX: Fixed a regression bug that game concepts were not correctly resolved
- [x] 优化：优化配色方案（尤其是默认主题 Islands Dark 的） / Optimize color scheme (Especially for the default theme Islands Dark)
- [x] 优化：优化脚本文件的 `IncorrectSyntaxInspection`（更严格的对比较运算符的检查） / OP: Optimize `IncorrectSyntaxInspection` for script files (More strict check for comparison operators)
- [x] 优化：允许通过扩展规则为内联脚本文件提供快速文档文本 / OP: Make it possible to provide quick doc text for inline script files via extended configs
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.1.3 (2026-02-20)

- [x] 修复：修复解析生成的修正时来源没有排除匿名定义的问题 / FIX: Fixed a bug that the source of the correction was not excluded when parsing generated corrections
- [x] 修复：修复条件块中的参数在调用时是否必须传入的判断逻辑的相关问题 / FIX: Fixed a bug that the parameters in the condition block were not correctly identified ([#272](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/272))
- [x] 优化：格式化 CWT 文件时，`!=` `<>` 周围始终保留空格，避免格式化前后的语法不等效 / OP: Optimize the formatting of CWT files, always keep spaces around `!=` and `<>`, to avoid syntax inequality before and after formatting
- [x] 优化：与重命名有关的相关优化与BUG修复 / OP: Optimizations and bug fixes related to renaming
- [x] 优化：补充更多缺失的 `AutomaticRenamer`（关联重命名） / OP: Add more missing `AutomaticRenamer` (association-rename)
- [x] 优化：为定义注入检测子类型 / OP: Detect subtypes for definition injection ([#274](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/274))
- [x] 优化：将模式为 `REPLACE_OR_CREATE` 的定义注入识别为定义声明 / OP: Detect definition injections with mode `REPLACE_OR_CREATE` as definition declarations ([#273](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/273))
- [x] 新功能：新增 `PutMembersOnOneLineIntention` 和 `PutMembersOnSeparateLinesIntention` / NEW: add `PutMembersOnOneLineIntention` and `PutMembersOnSeparateLinesIntention`
- [x] 新功能：新增 `ReplaceInlineMathWithEvaluatedValueIntention` / NEW: add `ReplaceInlineMathWithEvaluatedValueIntention`
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes
- [x] 性能分析与优化 / Performance analysis and optimization

## 2.1.2 (2026-01-24)

- [x] 同步规则文件 / Synchronize config files
- [x] 修复：修复 Steam 路径缓存未被正确预加载的回归 BUG / FIX: Fixed a regression bug that the Steam path cache was not preloaded correctly
- [x] 修复：修复无法正确识别文件定义的成员的回归 BUG / FIX: Fixed a regression bug that members defined in a file could not be correctly recognized
- [x] 修复：修复无法正确识别 `sprite` 的相关图片的回归 BUG / FIX: Fixed a regression bug that could not correctly identify images related to `sprite`
- [x] 修复：修复涉及索引的表达式可能无法被正确匹配与解析的回归 BUG / FIX: Fixed a regression bug that expressions involving indexes may not be correctly matched and parsed
- [x] 修复：避免渲染（数据库对象表达式中）存在递归的本地化文本时的 SOF / FIX: Avoided a SOF that occurred when rendering localisation text in database object expressions
- [x] 修复：重命名文件后，文件信息（如定义的快速文档中的）不会正确更新 / FIX: Fixed a bug that file information (e.g., in quick doc) was not updated correctly after renaming a file
- [x] 修复：修复对于对应多个 PSI 引用的表达式（如 `religion:catholic`），光标位置的用法高亮可能不正确的问题 / FIX: Fixed a bug that the usage highlight at cursor position may be incorrect for expressions that were resolved to multiple PSI references (e.g., `religion:catholic`)
- [x] 修复：修复未正确识别动态的规则上下文的回归 BUG / FIX: Fixed a regression bug that dynamic config contexts were not correctly identified ([#271](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/271))
- [x] 优化：优化插件的设置页面（排序子页面，规范化提示和注释，区分出扩展的设置页面） / OP: Optimize the plugin settings page (sort subpages, normalize tips and comments, separate extensions settings pages)
- [x] 优化：优化内嵌提示的渲染效果 / OP: Optimize the rendering of inlay hints
- [x] 优化：对于基于规则数据的功能，尽可能地预先检查规则数据是否已加载完毕（目前包括：代码检查，引用解析） / OP: For config-data-based features, Pre-check if the config data has been fully initialized when possible (Currently include: code inspections, reference resolving)
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes
- [x] 性能分析与优化 / Performance analysis and optimization

## 2.1.1 (2026-01-18)

- [x] 同步规则文件 / Synchronize config files
- [x] [Vic 3] PLS Hangs while indexing game files ([#259](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/259))
- [x] Case Insensitive Enums ([#261](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/261))
- [x] 优化：继续完善对链式的复杂表达式的支持 / OP: Continue to optimize support for linked complex expressions
- [x] 优化：优化从 PSI 解析规则数据时的性能，将耗时降低到旧版本的 30% / OP: Optimize the performance when parsing config data from PSI, reducing the time taken to 30% of the old version
- [x] 优化：优化索引时的性能，将耗时降低到旧版本的 12% / OP: Optimize performance during indexing, reducing the time taken to 12% of the old version
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.1.0 (2026-01-07)

- [x] 更新 IDEA 版本到 2025.2 / Update IDEA version to 2025.2
- [x] 同步规则文件 / Synchronize config files
- [x] Exception in version 2.0.7 ([#241](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/241))
- [x] Possible IDE freeze involves to `ParadoxAnalyzeManager.getFileInfo` ([#242](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/242))
- [x] 生成本地化时，某些缺失的本地化未被包含 ([#243](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/243))
- [x] 优化：可配置 - 如果远程仓库地址已配置，覆盖相关的内置规则分组 / OP: Configurable - Override related builtin config group if remote repository urls are configured
- [x] 优化：为内联脚本表达式提供特殊的快速文档 / OP: Provide special quick doc for inline script expressions
- [x] 优化：补充更多装订线图标和内嵌提示，补充部分内嵌提示的预览（在对应的配置页面中） / OP: Add more gutter icons and inlay hints, add several inlay hint previews (in related settings page)
- [x] 优化：以 `####` （或者更多的 `#`）开始的 CWT 文档注释会被直接渲染为 HTML / OP: CWT documentation comments start with `####` (or more `#`) will be rendered directly as HTML
- [x] 新功能：支持通过意向动作，根据传参信息求值内联数学表达式 / NEW: Support to evaluate inline math expressions with argument information via intention actions
- [x] 新功能：为静态的内联数学表达式提供内嵌提示，显示评估后的值 / NEW: Provide inlay hints for static inline math expressions, showing the evaluation result
- [x] 新功能：支持在规则文件中通过特定的选项注释注入规则（如 `## inject = some/file.cwt@some/property`） / NEW: Support to inject configs in config files via specific option comments (e.g., `## inject = some/file.cwt@some/property`) ([#251](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/251))
- [x] 新功能：支持 VIC3 和 EU5 中的定义注入特性（如 `inject:xxx = {...}` ） / NEW: Support the definition injection feature in VIC3 and EU5 (e.g., `inject:xxx = {...}`) ([#252](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/252))
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.7 (2025-11-18)

- [x] 优化：在 Stellaris 中，如果一个事件继承自另一个事件，那么此事件也会继承部分子类型 / OP: In Stellaris, if an event inherits from another event, then this event will also inherit specific subtypes
- [x] 优化：优化对游戏和模组的入口目录的识别，并修复相关BUG / OP: Optimize detection for game and mod entry directories, with related bug fixes
- [x] 新功能：支持在 Steam 中启动游戏（通过编辑器上下文工具栏，或者工具菜单） / NEW: Support to launch game in Steam (via editor context toolbar, or tool menu)
- [x] 优化：继续优化索引与解析时的性能和内存占用 / OP: Continue to optimize performance and memory usage during indexing and resolving
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.6 (2025-11-08)

- [x] 完善 Stellaris 的规则文件以匹配最新游戏版本 4.1 / Optimize config files for Stellaris to the lastest game version 4.1
- [x] 同步其他游戏的规则文件 / Synchronize config files of other games
- [x] `key0 = { { key1 = …… key2 = …… } }` 写法解析异常，需要修复 ([#196](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/196))
- [x] [EU5] Multi Parameter Link/Trigger ([#220](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/220))
- [x] 优化：优化复杂表达式的实现，对于 `relations(x)` 格式的节点，允许单引号括起的传参、多个传参 / OP: Optimize the implementation of complex expressions, for nodes in the `relations(x)` format, literal arguments (enclosed in single quotes) and multiple arguments are allowed
- [x] 优化：对于脚本文件中用引号括起的字符串，如有必要，尝试通过语言注入将其识别为本地化文本 / OP: For quoted strings in script files, if necessary, try to recognize it as localisation text via language injection
- [x] 优化：可以在插件设置页面中配置是否启用自动语言注入 / OP: Can configure whether to enable auto language injection in plugin settings page
- [x] 优化：支持在快速文档和类型信息中显示使用的覆盖方式 / OP: Support to show used override strategy in quick doc and type info
- [x] 优化：优化与重载/覆盖相关的代码检查 / OP: Optimize override related inspections
- [x] 优化：支持渲染规则文件中的文档注释（从而兼容阅读器模式和 Translation 插件） / OP: Support to render documentation comments in config files (thus compatible with reader mode and Translation plugin)
- [x] 优化：继续优化索引与解析时的性能和内存占用 / OP: Continue to optimize performance and memory usage during indexing and resolving
- [x] 新功能：支持 Stellaris 中的命名格式表达式 / NEW: Support the name format expressions in Stellaris ([#193](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/193))
- [x] 新功能：提供工具动作，用于从日志或脚本文件生成规则文件的维护提示 / NEW: Provide tool actions to generate maintenance hints for config files from log or script files
- [x] 新功能：提供快速修复，将无法解析的表达式替换为合法且相似的字面量 / NEW: Provide quick fix to replace unresolved expressions with valid similar literals
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.5 (2025-09-24)

- [x] [#186](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/186)
- [x] 可以按 *严重度 x 置信度* 详细配置 Tiger 代码检查的报错级别 / Can configure detailed highlight severity levels by *Severity x Confidence* ([#187](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/187))
- [x] 优化：可在设置页面中通过 OpenAI API、Anthropic API 或本地模型提供 AI 服务 / OP: AI Service can be provided via OpenAI API, Anthropic API or local LLM in settings page
- [x] 优化：优化模组依赖的导入导出功能 / OP: Optimize the import & export feature for mod dependencies
- [x] 优化：在某些情况下禁用脚本文件中的语言注入，避免意外的报错 / OP: Disable language injection in script files in some situations to avoid unexpected error reports
- [x] 优化：规则的数据类型 `Int` 和 `Float` 支持开区间写法（如 `float(0..inf]`） / OP: config data type `Int` and `Float` can accept open range format (e.g., `float(0..inf]`)
- [x] 优化：支持需要在匹配时先加上一组后缀的数据类型（如`"<sprite>|locked,unlocked"` `"localisation|_enemy"`） ([#162](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/162) [#193](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/193))
- [x] 修复：一些地方（如调用 Tiger 时）应使用最终的游戏类型与游戏目录 / FIX: Some locations (like when calling Tiger) should use the final game type and game directory.
- [x] 修复：修复一些地方可能无法正确获取游戏类型，导致无法正确匹配与解析的问题 / FIX: Fixed an issue where some places may not be able to correctly obtain the game type, resulting in the inability to match and parse correctly
- [x] 新功能：提供基于本地化文本片段的随处搜索 / NEW: Provide localisation text snippet based Search Everywhere
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.4 (2025-09-09)

- [x] [#184](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/184)

## 2.0.3 (2025-09-08)

- [x] 修复：修复与切换类型有关的一处 BUG / FIX: Fix an issue related to swapped types
- [x] 修复：修复规则数据可能在重启项目后被清空的问题 / FIX: Fix an issue that config data may be cleared after project reopening

## 2.0.2 (2025-09-04)

- [x] [#165](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/165)
- [x] 优化：优化自动识别切换类型的声明规则的逻辑 / OP: Optimize the logic for automatically detecting declaration configs for swapped types
- [x] 优化：如果本地化参数可以被解析为本地化，则启用特殊高亮 / OP: If a localization parameter can be resolved to a localization, enable special highlighting
- [x] 优化：优化编制与查询索引时的性能 / OP: Optimize the performance of building and querying indices
- [x] 优化：可以配置可在随处搜索中搜索的符号的类型 / OP: Can configure the types of symbols that can be searched in Search Everywhere
- [x] 优化：随处搜索也支持搜索规则文件中的符号（类型、复杂枚举等） / OP: Search Everywhere also supports searching symbols in config files (types, complex enums, etc.)
- [x] 优化：更好的用于翻译/润色本地化的AI提示 / OP: Better AI prompts for translating and polishing localizations 
- [x] 修复：修复未在必要时渲染切分后的图片的问题 / FIX: Fix an issue that images are not rendered with expected slicing if necessary
- [x] 修复：修复某些场合未忽略字符串大小写的问题 / FIX: Fix an issue that string case is not ignored in some situations
- [x] 修复：修复与图表（Diagrams）相关的一些问题并优化性能 / FIX: Fix some issues related to diagrams, together with performance optimization
- [x] 新功能：适配上移/下移声明的功能，适用于CWT文件和脚本文件中的成员（封装变量、属性、值），以及本地化文件中的属性（即本地化条目）（入口：主菜单，点击`Code > Move Statement Up/Down`） / NEW: Support moving statements up/down, for members (scripted variables, properties, values) in cwt files and script files, and properties (aka localisation items) in localisation files. (Entry: Main menu, click `Code > Move Statement Up/Down`)
- [x] 新功能：提供本地化操作任务，用于从另一语言环境的本地化翻译为当前语言环境 / NEW: Provide the localisation manipulation task to translate localisations from another locale to current locale
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.1 (2025-08-11)

- [x] [CK3] Add file `lines.lines` to Paradox script file name patterns ([#159](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/159))
- [x] Using single alias for a type ([#161](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/161))
- [x] Plugin breaks Menus and Toolbars editor ([#164](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/164))
- [x] 完善与Markdown的集成，涉及链接、内联代码、代码块等，详见[参考文档](https://windea.icu/Paradox-Language-Support/zh/extensions.html#md) / Complete integration with Markdown, including links, inline codes, code blocks, etc. See [reference document](https://windea.icu/Paradox-Language-Support/en/extensions.html#md) for details
- [x] 修复：修复与概念相关的一些问题 / FIX: Fix some issues related to concepts
- [x] 新功能：提供对规则文件中的类型、枚举、别名等的引用解析和查找用法的支持 / NEW: Provide support for reference parsing and finding usages of types, enums, aliases, etc. in config files
- [x] 新功能：支持Paradox CSV语言，提供各种必要的语言功能 / NEW: Support Paradox CSV language, and provide various necessary language features
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes

## 2.0.0 (2025-07-10)

- [x] 集成Tiger检查工具 / Integrate Tiger linting tools ([#128](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/128))
- [x] 更改`date_field`的默认日期模式为`y.M.d`，且允许声明日期模式（`date_field[y.M.d]`） / Change the default date format of `date_field` to `y.M.d`, and allow declaring date formats (`date_field[y.M.d]`) ([#148](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/148))
- [x] Local variable, defined inside inline_script but passed from the outside as a parameter is not recognized. ([#151](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/151))
- [x] 可以配置是否在注入的文件（如，参数值）中、内联脚本文件中忽略各种无法解析的引用的代码检查 / Can configure whether to ignore unresolved references inspections in injected files (e.g., parameter values) and inline script files ([#153](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/153))
- [x] [2.0] Issues with the Dev Build ([#154](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/154))
- [x] 修复：修复插件可能无法正确解析json，从而无法识别游戏目录的问题 / FIX: Fix a bug that plugin may not be able to correctly parse json, causing the game directory to be incorrectly recognized
- [x] 修复：修复对复杂表达式进行代码补全时可能出现的NPE / FIX: Fix a NPE that may occur when performing code completion on a complex expression 
- [x] 优化：将插件的内部设置重构为`registryKey`，允许用户调整 / OP: Refactor the internal settings of the plugin to `registryKey`, allowing users to configure them
- [x] 优化：通过快速文档为本地化参数和命令的传入参数提供其中使用的格式标签的说明 / OP: Provide information about format tags used in arguments of localisation parameters and commands by quick documentation
- [x] 优化：为封装变量、复杂枚举值和动态值提供快速文档与内嵌提示，来自同名的本地化 / OP: Also provide quick documentation and inlay hints for scripted variables, complex enum values and dynamic values, from the same name localisation
- [x] 优化：可以配置是否在内嵌提示中显示定义的子类型 / Can configure whether to show subtypes of definitions in inlay hints
- [x] 优化：可以配置处理图片时使用的工具（默认且内置：Texconv，可选：Image Magick） / OP: Can configure the tool used to process images (Default and built-in: Texconv, Available: Image Magick)
- [x] 优化：可以配置翻译本地化文本时使用的工具（目前仅限Translation Plugin） / OP: Can configure the tool used to translate localisation text (Currently limited to Translation Plugin)
- [x] 优化：可以配置要启用的检查工具（目前仅限Tiger） / OP: Can configure which linting tools to enable (Currently limited to Tiger)
- [x] 优化：更好的对图片的支持，优化实现，支持预览与渲染DDS、TGA图片，提供不同图片格式（PNG、DDS、TGA）之间的相互转换的操作 / OP: Better support for images, optimize the implementation, support to preview and render DDS, TGA images, and provide actions to convert image formats (PNG, DDS, TGA)
- [x] 新功能：集成AI，提供对应的设置页面 / NEW: Integrate AI, provide corresponding settings page
- [x] 新功能：提供AI驱动的本地化翻译功能（意向） / NEW: Provide AI-driven localisation translation features (intentions)
- [x] 新功能：提供AI驱动的本地化润色功能（意向） / NEW: Provide AI-driven localisation polishing features (intentions)
- [x] 新功能：提供动作，用于批量操作本地化（翻译、润色等） / NEW: Provide actions for batch manipulation of localisations (translating, polishing, etc.)
- [x] 其他优化与BUG修复 / Other optimizations and bug fixes
- 注意：建议在执行各类本地化操作之前，先将更改提交到VCS / NOTE: It is recommended to commit changes to VCS before performing various localisation manipulations

## Previous Changelogs

### [Changelog 1.X.X](documents/changelogs/CHANGELOG_1.X.X.md)
### [Changelog 0.X.X](documents/changelogs/CHANGELOG_0.X.X.md)