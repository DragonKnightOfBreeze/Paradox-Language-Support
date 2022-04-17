# 更新日志

## 0.5 *

* [X] 更新IDEA版本到2022.1
* [ ] BUG修复 *
* [ ] 语法解析优化 *
* [ ] 完善内嵌提示
* [ ] 完善对CWT配置文件的支持，完善相关功能
* [ ] 不再内置CWT配置文件，改为需要以库的方式添加到项目和模块中（Cwt Config，CWT配置目录）
  * 可以通过内置的Github链接从网络下载需要的CWT配置文件（使用Git克隆远程仓库，然后保存到本地）
  * 包括官方Github链接和镜像Github链接（https://github.com.cnpmjs.org）
  * 检查Github上最新的CWT配置文件仓库，提高CWT配置文件语法解析的兼容性
* [ ] 完善在线参考文档

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
* [X] 实现paradoxScript的InlayHintsProvider，目前为定义提供来自本地文件的名字（如：对于特质`agenda_defensive_focus`，名字是`保卫边境`）
* [X] 实现`DdsToPngConverter`，基于放在jar包中的小工具`dds2png.zip`，可以将dds文件转化成png文件（Windows平台，插件中暂未使用）
* [X] 基于gfx文件中的spriteDefinition（`spriteType = { name = ... texturefile = ... }`）解析图标（`paradoxIcon`）
* [X] 图标（`paradoxIcon`，本地化文件会使用，在gfx文件中通过`spriteType = { ... }`定义）的索引以及代码提示
* [X] 解决BUG：解析图标生成的html无法正常渲染图标，替换成本地解析dds
* [X] 使用本地渲染渲染图标
* [X] 修复BUG，definition.name重新设为区分大小写，判断的时候可能会忽略
* [ ] 本地化图标的查找：
  * [X] 首先查找是否有对应的定义`spriteType = { ... }`，且`name`属性带有前缀`GFX_text_`
  * [X] 然后直接查找是否有对应名字的dds文件，且位于文件夹`gfx/interface/icons`下
  * [ ] 也有可能是生成的，即有对应的定义X，声明使用定义Y的图标，那么本地化图标`...X...`会使用本地化图标`...Y...`
* [X] 提示脚本文件的定义的名称类型信息+本地化名字
* [X] 根据localisation的name进行代码补全时，预先根据关键字过滤结果，防止因为结果太多导致无法正确进行提示
* [X] 区分localisation和localisation_synced
* [X] 兼容cwt规则`only_if_not`
* [X] cwt文件：`option.value`可以无需双引号直接包含空格（主要对于规则`display_name`）
* [X] 优化cwt配置规则`type[...].localisation`的`name`的解析逻辑（可以为`$` `"$_desc"`也可以为`title` `name`）
* [X] 提示定义的本地化名字（对应的localisation.name为name或title）
* [X] 修复cwt规则文件解析问题（有时无法解析optionComment中的`<>`）
* [X] 即使cwt规则文件中没写也为definition补充某些definitionLocalisation，从已有的推断（如果有的话）（name title desc effect）
* [X] 实现LineMarkerProviderDescriptor改为实现RelatedItemLineMarkerProvider，从而实现`Go to related symbols`
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
* [X] definitionProperty对应的规则如果是const（即相同名字的规则），则将规则对应的cwtKey/cwtValue作为它的引用
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
* [X] 编写代码准备从alias/definitionProperty/subtype推断scope和scopeMap
* [x] 为localisation_command/modifier提供关于scope的额外文档注释（未附加psi链接）
* [X] 为definitionProperty提供关于scope的额外文档注释（未附加psi链接）
* [X] 为definitionProperty提供关于scope的额外文档注释（附加psi链接）
* [X] 支持规则类型single_alias
* [X] 支持规则类型alias_match_left
* [X] 提示alias时匹配scope（来自`trigger_docs.log`或`triggers.log`，需要先转化为cwt，从名为`scope`或`scopes`的option中得到）
* [ ] 为alias补充名为`scope`或`scopes`的option（仅对于规则类型`alias_name[xxx]`）
* [ ] 支持规则类型scope和scope_field
* [ ] 支持规则类型value和value_set
* [ ] 支持规则类型complex_enum
* [X] 应用cwt配置文件`folders.cwt`
* [ ] 应用cwt配置文件`scopes.cwt`
* [ ] 应用cwt配置文件`links.cwt`
* [X] 应用cwt配置文件`modifiers.cwt/modifier_categories.cwt`
* [ ] 应用cwt配置文件`values.cwt`
* [ ] 应用cwt配置文件`localisation.cwt` *
* [X] 生成本地化文本的文档注释时考虑并发解析其中的图标（不知为什么还是有点慢）
* [X] 不要缓存CwtKvConfig的scope和scopeGroup（因为内联的原因，parent可能有变动）
* [X] 修复当规则类型为alias_name或者引用为modifier时，无法解析definitionPropertyKey的引用的bug
* [ ] definitionProperty本身就有对应的引用（如definition）的情况时，是否同样将规则对应的cwtKey/cwtValue作为它的引用？
* [ ] 基于cwt规则文件，对脚本文件的结构进行验证
* [ ] scriptProperty的propertyName和definitionName不一致导致重命名scriptProperty时出现问题
* [X] 适配IDEA版本`2021.2`
* [ ] ~~添加新的库作为依赖时（原始游戏目录，模组），兼容zip压缩包和文件夹~~（使用基于jar协议的url打开zip文件，实际测试发现读取不到内容）
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
* [X] 实现本地化属性的`CopyRawTextIntention`、`CopyPlainTextIntention`、`CopyRichTextIntention`
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
