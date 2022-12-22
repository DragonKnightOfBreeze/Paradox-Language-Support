# Changelog

> 除非明确说明：
> 
> * 这里提到的路径/filePath指：文件相对于游戏或模组根路径的路径。
> * 脚本文件中定义的本地化名字指：定义的最相关的本地化文本（基于`cwt`规则文件，注有`## primary`的，或者从特定名字的定义属性的值推断来的，这特定名字可以在设置页面中设置）
> * 脚本文件中的变量指：如`@var = 123`这样写在定义结构之外的声明（事实上是常量，另有高级语法）（`scripted_variable`，封装变量）
> * 待办项中右边的星号（`*`）表示这是目前正计划实现的功能。

## TODO

更新计划：

* 完善在线参考文档
* 优化：
  * [ ] 对于忽略大小写的字符串作为键/元素的集合和映射，考虑使用内联类`CaseInsensitiveString`，以保证排序
  * [ ] 编辑本地化文件时提供输入彩色文本、图标等的快捷键（仅在可用的位置生效）
  * [ ] 确认重命名功能能够预期正确进行（如果对应的声明/引用支持重命名）
  * [ ] 确认`inline_math`和`scripted_loc`是否需要进行转义的最终条件，并添加到对应的检查中
  * [ ] ~~将本地化命令（`[xxx]`中的xxx，所有文本）解析为单个元素，如果解析scopeFieldExpression一样，将此作为localisationCommandExpression并解析，实现相关功能：检查、提示等~~
  * [ ] 基于facet或者合成库`SyntheticLibrary`+自定义设置配置模组的游戏类型、游戏目录、依赖模组列表等配置
  * [ ] 内嵌提示的预览文本中不再包括特殊注释，而是通过向psiFile中注入特定userData的方式提供必要的信息（类型、本地化等）
  * [ ] 基于引用的重命名需要考虑存在前后缀的情况（主要是为图标引用考虑）
  * [ ] 需要重新调整对返回的规则列表的排序
  * [ ] 一些检查可以基于当前文件、文件路径（相对于游戏或模组根目录）、定义类型（例如，`event`）、定义成员路径（例如，`event.abc`）等来部分禁用
* 新增功能：
  * [ ] 实现右键菜单&项目视图&工具栏操作：从指定的本地化文件生成其他语言区域的本地化文件（考虑支持指定多个或者整个目录的情况）
  * [ ] ~~添加检查：图标属性的值引用了定义自身（`foo { icon = foo ... }`）（不觉得这有什么意义）~~
  * [ ] 可以通过特殊注释强制指定定义类型（基于文件路径或者基于直接的类型+子类型） - 用于实现差异比较等功能
  * [ ] 实现对`*.gui`文件中的GUI定义的UI预览（参考IDEA的Markdown插件的实现）
  * [ ] 实现对`*.txt`文件中的定义的UI预览（参考游戏中的效果以及灰机Wiki的实现）
  * [ ] 同名的定义、本地化，同路径的文件之间的DIFF（左窗口显示当前的，右窗口显示包括当前在内的所有的）
  * [ ] 同名的定义、本地化，同路径的文件之间的DIFF&MERGE（左窗口显示当前的，右窗口显示包括当前在内的所有的，中间窗口显示合并后的，初始内容来自左窗口）
  * [ ] 通过扩展的CWT规则文件支持生成的modifier、effect、trigger等（理想情况下，不需要解析游戏日志）
    * 没有实际的定义处，如果要导航到定义，改为查找使用 
    * 在快速文档中显示生成源的信息（比如，`pop_job`）
    * 包含生成源的引用，查找生成源的使用时也能查找到此表达式（比如，点击`job_xxx_add`中的`job_xxx`可以导航到对应的定义处，反之也能查找到对应的使用）
* 新增功能 - 低优先级：
  * 取消包围/移除（`Code > Unwrap/Remove...`）
    * [ ] 删除脚本属性或者单独的值（`k = v` > `v`，包括后面的单行注释）
    * [ ] 删除子句或者值为子句的属性并将其中的内容上移（`k = { v }` > `v`）
    * [ ] 删除参数条件块并将其中的内容上移（`[[PARAM] v ]` > `v`）
    * [ ] 删除本地化属性（`KEY:0 "..."`，包括后面的单行注释）
* 完善CWT配置支持：
  * [ ] ~~为复杂枚举如`complex_enum[policy_option]`提供相关本地化支持（类似定义）~~（搁置，不能很好地显示出来，复杂枚举名可能本身就是一个本地化引用）
  * [ ] 优化：更好地兼容嵌套的定义
  * [ ] 编写工具类支持解析`localistions.log` `modifiers.log` `scopes.log` `trigger_docs.log`等日志文件，生成对应的cwt文件
  * [ ] 优化：scope的名字（准确来说是别名）可以包含点号
  * [ ] 优化：检查scopeFieldExpression时也检查是否匹配对应的scopeName或者scopeGroupName（归类到`MismatchedScopeInspection`）
  * [ ] 对于link `pop_faction_parameter`和complexEnum `pop_faction_parameters`的特殊处理：前者只能在`pop_faction`中使用且作为数据源的complexEnumValue只能来自同一定义声明中
  * [ ] 兼容CWT规则文件中的错误级别`severity = warning`或`## severity = warning`（PLS和CWTools实现有所不同，需要分析）

## 0.7.9

* BUG修复
  * [X] 修复：[Cannot choose path for library using CK3 #7](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/7)
* 新增功能 - 概述
  * [ ] 支持处理作用域（`scope`），以及相关的快速文档、内嵌提示、代码检查等功能（基于已有的CWT规则中的选项，以及从扩展的CWT规则推断）
  * [ ] 支持处理生成的修饰符（`modifier`），以及相关的引用解析、代码补全、代码高亮等功能（基于扩展的CWT规则）
  * [ ] 支持处理内联脚本（`inline_script`）
* 新增功能
  * 快速文档（`Quick Documentation`）
    * [ ] 如果存在，在快速文档中显示作用域上下文信息
    * [ ] 如果是生成的修饰符，在快速文档中显示生成源的信息，包括名字和类型（例如，`job_xxx: job`）
  * 内嵌提示（`Inlay Hints`）
    * [X] 如果存在，提供作用域上下文的内嵌提示（参考Kotlin Lambda内嵌提示的实现）
  * 引用解析
    * [ ] 如果是生成的修饰符，可以通过`Ctrl+Click`查找使用，对于其中生成源对应的部分（如`job_xxx_add`中的`xxx`），则会导航到生成源的声明
  * 代码检查（`Code > Inspect Code...`）
    * [ ] 检查作用域上下文与当前定义成员的作用域是否匹配（警告级别）
    * [ ] 检查作用域上下文切换是否正确（警告级别）

## 0.7.8

* BUG修复
  * 为了避免意外，解析获取表达式对应的CWT规则时，不再使用全局缓存，而是使用基于表达式对应的PSI元素的缓存（当所在文件被修改时会失效）
* 功能优化
  * [X] 可以通过导航到实现功能（`Navigate > Implementation(s)`）来从某一封装变量/定义/本地化/复杂枚举值跳转到所有同名的重载或者被重载的声明处
    * 注意对于非顶级属性名的定义名（如`some_event_id`）和复杂枚举值（如`policy_flag`），从声明处出发，需要间接通过意向（`Intentions`）来查找使用/导航到实现/导航到类型声明
  * [X] 查找使用时，即使声明处与引用处的名字文本不同，也能正确进行，同时鼠标放到声明或使用处时也能正确显示引用高亮（例如：`GFX_text_unity` > `unity`）
  * [X] 优化如何提供类型信息（`View > Type Info`）和快速类型声明（`View > Quick Type Definition`）
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
* BUG修复：
  * [X] 修复无法识别直接作为字符串写在脚本文件顶层的复杂枚举值的问题（例如，`component_tags`）
  * [X] 修复格式化时对嵌套的块（子句）的缩进问题
* 优化：
  * [X] 进行代码补全时，尽可能兼容表达式用引号括起的各种情况
  * [X] 进行代码补全时，在输入`definition_root_key = `之后，也可以应用代码补全
  * [X] 进行代码补全时，在提供定义声明中的定义成员的补全之外，也可以提示定义声明之外的属性名（如果允许）
  * [X] 进行代码补全时，如果要基于从句内联模版进行代码补全，首先弹出对话框，让用户选择需要插入的内容
    * 需要插入的内容（属性/值）可多选，可排序，可重复（不基于CWT规则判断是否允许重复）
  * [X] 导航到相关的CWT规则时，位置也可以是定义的rootKey
* 新增功能： 
  * [X] 支持CWT规则：`stellaris_name_format[xxx]`
    * （需要确定3.6是否仍然支持）如果未用引号括起，则对应一个本地化的名称，本地化文本中可以使用格式化引用`<some_parts>`，而`some_parts`对应CWT规则`value[x]`
    * （3.6开始支持）如果用引号括起，则是一个特殊的表达式，例如`"{AofB{<imperial_mil> [This.Capital.GetName]}}"`
    * 需要进一步完善……
  * [X] 支持语法：Stellaris格式化引用（`format.xxx: "<some_parts> ..."`中的`<some_parts>`），实现相关的代码高亮，引用解析、代码补全、代码检查等功能
    * 当游戏类型为Stellaris，且本地化的名字以`format.` `format_prefix.`或者`format_noun.`开始时，认为对应的本地化文本中支持格式化引用语法
    * 3.6开始似乎已经弃用这种语法？但是本地化文件中仍然保留。

## 0.7.6

* 更新IDEA版本到2022.3
* 优化：
  * [X] 整合内部配置到CWT配置（作为全局的配置，要求检查文件名）
  * [X] 如果无法解析值表达式（`k = v`中的`v`），如果存在，在代码检查的错误信息中提示可能的CWT规则表达式（如，`possible: <event>, {...}`）
  * [X] 将非法的表达式`k = k1 = v`中的`k1`解析成key而非value，以便在某些情况下，例如使用从句内联模版进行代码补全时，能提供正确的高亮
* 新增功能：
  * [X] 提供包围选项：包围成从句（`k = v` → `{ k = v }`）和包围成值为从句的属性（`k = v` → `key = { k = v }`）
  * [X] 提供包围选项：用参数条件块包围（`k = v` → `[[PARAM] k = v ]`）
* 新增功能 - 群友提出：
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

* BUG修复：
  * [X] 修复无法从项目文件中的声明导航到库中的引用的问题（考虑使用`UseScopeEnlarger`或`ResolveScopeManager`）
  * [X] 可以从定义名并非rootKey的定义（如event）的声明处导航到所有使用处（鼠标放到定义的rootKey上，然后Ctrl+鼠标左键）
  * [X] 从任意同名同类型的封装变量/定义/本地化/文件路径出发，可以通过查找使用导航到所有那个名字的使用
  * [X] 兼容更复杂的表达式的情况，如：`root.owner.event_target:target@root.owner`
  * [X] 兼容`value_field`或者`int_value_field`需要被识别为变量的情况（`root.owner.var_name`）（通过更新CWT规则文件`links.cwt`）
  * [X] 脚本文件中来自CWT文件的引用需要能被同时高亮出来，同时一般情况下不能从CWT文件中的规则查找引用（`modifier`等除外）
  * [X] 兼容localisationCommandScope需要被识别为`value[event_target]`或者`value[global_event_target]`的情况，以及代码提示
  * [X] 兼容localisationCommandField需要被识别为`value[variable]`的情况，以及代码提示
  * [X] 修复不能为对应的CWT规则是别名（alias）的表达式（key / value）提供正确的引用读写高亮的问题（当鼠标放到表达式上时应当显示）
  * [X] 修复无法跳转到某些定义成员对应的CWT规则的问题
* 功能优化：
  * [X] 对CWT别名规则（dataType=alias/single_alias）使用特殊的别名图标，以便区分内联前后的CWT规则
  * [X] 在单纯地匹配CWT规则以找到对应的CWT规则时，不应该要求索引，否则可能会引发IDE异常：`java.lang.Throwable: Indexing process should not rely on non-indexed file data.`
  * [X] （可配置是否启用）进行代码补全时，如果在提示定义属性的键时，如果其值可能是常量字符串，应用补全后可以自动插入
  * [X] （可配置是否启用）进行代码补全时，如果在提示定义属性的键时，如果其值可能是从句，应用补全后可以自动插入花括号并将鼠标放到花括号中
  * [X] 本地化文件：兼容`$@some_scripted_variable$`、`£$SOME_REF$£`这样的语法
  * [X] 优化插件配置页面
* 功能变更：
  * [X] ~~支持额外的CWT选项：`## icon = <icon_type>`，用于重载进行代码补全时需要显示的图标，如`## icon = tag`~~ → 使用CWT选项`## tag`标记特殊标签，如`optimize_memory`
  * [X] 移除`icu.windea.pls.core.ParadoxPathReferenceProvider` （用于兼容markdown锚点）
* 新增功能：
  * [X] 实现检查：参数（`$PARAM$`）被设置/引用但未被使用（例如：有`some_effecFt = { PARAM = some_value }`但没有`some_effect = { some_prop = $PARAM$ }`，后者是定义的声明。）
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
  * [X] 可以提示本地化颜色的ID（由于颜色ID只有一个字符，当光标在本地化文本中且前一个字符是`"§"`时，手动调用代码提示功能（例如，按下`Ctrl+空格`），才会进行提示
  * [X] 提供布尔值的代码补全（在定义声明中不为属性或者块中的值提供）
* 完善CWT配置支持：
  * [X] 支持`complex_enum`，以及相关功能：匹配、代码提示
  * [X] 支持高亮`definitionName` `complexEnumValueName`（对应的PSI元素可能本身就对应着特定的CWT规则，需要同时高亮出来）
  * [X] 为`complexEnumValue`的引用（而非声明）提供特殊文档
  * [X] 以某种方式另外实现`definitionName` `complexEnumValueName`的文档、查找使用、导航到类型声明等功能 - 通过意向（intention）
  * [X] 为预定义的`modifier`提供相关本地化支持（`mod_$` `mod_$_desc`等，需要确定具体规则是什么） *

## 0.7.3

* [X] 更新cwt规则到最新版本（2022/10/13）
* 功能优化：
  * [X] 如果通过代码检查发现一个定义上有多个缺失的相关本地化/图片，将每个单独作为一个问题
  * [X] 参数（`$PARAM$`）和值集值值（`some_flag`）并不存在一个事实上的声明处，点击需要导航到所有引用处
  * [X] 测试通过对参数（`$PARAM$`）和值集值值（`some_flag`)基于引用的统一重命名
* BUG修复：
  * [X] 进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
  * [X] 修复解析本地化位置表达式（如`$_desc`）时把占位符`$`解析成定义的rootKey而非定义的名字的问题
  * [X] 解析本地化位置表达式（如`$_desc`）时如果存在占位符`$`但对应的定义是匿名的，则应直接忽略，返回空结果
  * [X] 修复`CwtConfigResolver`中的NPE
  * [X] 修复`CwtImageLocationExpression`中的SOF
  * [X] 修复valueSetValue索引在索引时会被IDE认为栈溢出的问题，改为基于`ParadoxValueSetValuesSearch`和索引进行查找
  * [X] 脚本文件 - 基于注解器的语法高亮，不高亮带有参数的表达式
  * [X] 修复无法从项目文件中的声明导航到库中的引用的问题（默认情况下，对应的库需要导出）
* 新增功能：
  * [x] 实现动作：导航到相关本地化和导航到相关图片（对于定义，在导航菜单/右键菜单中，在动作"导航到相关符号/Go to related symbol"下面）
* 功能变更：
  * 支持一些特殊注释：
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
* 功能优化：
  * [X] multiResolve本地化时不指定偏好的语言区域
  * [X] 优化paradoxSelector
  * [X] 补充内嵌提示的预览文本
* 新增功能：
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
* BUG修复：
  * [X] 修复：[.${gameType} file is ignored #3](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/3)
  * [X] 修复：[Cyan color support in localisation #4](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/4)
  * [X] 修复：[Bugs in translation #6](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/6)
  * [X] 修复：对声明的查找使用的结果不完整（定义，本地化，参数等）
  * [X] 尝试修复：访问缓存（CachedValue）时导致的PsiInvalidElementAccessException（限制太多，暂时避免使用）
* 代码优化：
  * [X] 提供方法以及检查代码：当需要获取定义或代码块的属性/值的时候，可以获取参数表达式中的属性/值
  * [X] 检查代码：同一代码块（block）中允许同时存在属性和值，且可以同时被CWT规则校验
  * [X] 绝不把本地化文件夹（如`localisation`）中的文件视为脚本文件，绝不把本地化文件夹之外的文件视为本地化文件
  * [X] 本地化颜色ID可以是数字
  * [X] 优化文件类型的重载逻辑（使用`ParadoxRootInfo`和`ParadoxFileInfo`保存上下文信息，监听描述符文件和游戏类型标记文件）
  * [X] 脚本文件中的文件路径分隔符兼容"/" "\" "\\"混用
  * [X] 解析引用时限定相同的游戏类型，并且如果可用，优先选用同一游戏或模组根目录下的
* 功能优化：
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
  * ~~将游戏类型和游戏/模组目录依赖的配置保存到游戏或模组根目录下的特定配置文件（暂定为`.pls.settings.json`）中，将游戏/模组目录依赖视为合成库（参见`AdditionalLibraryRootsProvider`）~~
* 新增功能：
  * ~~当用户新打开的项目中被识别包含模组文件夹时，如果没有将对应的游戏目录作为依赖添加到对应的项目/模块，弹出右下角通知要求用户添加，如同CWTools一样。~~ （需要延迟判断，何时判断？）
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
* 完善内嵌提示：
  * [X] 脚本文件：值集值值的内嵌提示（值的类型即值集的名字，`xxx = value_set[event_target]`中的`event_target`）
* 脚本文件语法解析优化：
  * 兼容`common/scripted_effects/99_advanced_documentation.txt`中提到的高级语法
  * [X] 对于`stellaris v3.4`开始新增的`tag`（`optimize_memory`），提供特殊图标和代码高亮，代码提示和验证功能另外由CWT规则提供
  * [X] 兼容`inline_math`语法，以及相关功能：代码高亮、代码补全、代码折叠、引用解析、格式化、代码风格设置
  * [X] 兼容`parameter`语法，以及相关功能：代码高亮
  * [X] 兼容`string_template`语法，以及相关功能：代码高亮
  * [X] 兼容`parameter_condition`语法，以及相关功能：代码高亮、代码折叠、格式化、代码风格设置
  * [X] 获取封装变量名时不再包含作为前缀的"@"
  * [X] 封装变量的值也可以是bool类型（`yes`和`no`）
* 本地化文件语法解析优化：
  * [X] 删除`sequentialNumber`，作为一种特殊的`propertyReference`（如`$VALUE$`）（`stellaris v3.4`开始其语法由`%O%`改成了`$O$`）
  * [X] 支持`iconFrame`（`£leader_skill|3£`中的`3`，可以为变量，如`$LEVEL$`）
  * [X] 支持本地化语言`l_japanese`和`l_korean`
  * [ ] 兼容作为format的本地化中的`<some_part>`语法，其中`some_part`对应特定类型的定义（需要修改原始的CWT规则）
* 内置配置：
  * [X] 添加`predefined_variables`，表示预定义的变量，在本地化文本中使用，实现相关功能：引用解析
* CWT配置：
  * [X] ~~支持额外的CWT选项：`## since = "3.3"`，用于在文档注释中显示开始支持的游戏版本号~~（已移除）
  * [X] 支持额外的CWT选项：`## format = html`，用于指定将文档注释渲染为HTML
  * [X] 支持额外的CWT选项：`## icon = <icon_type>`，用于重载进行代码补全时需要显示的图标，如`## icon = tag`
  * [X] 支持额外的CWT选项：`## color_type = rgb / rgba`，用于以装订线图标的方式显示对应的颜色
  * [X] 仅当对应的数据类型是`bool`时，才会提示`yes`和`no`
  * [X] 支持的额外的CWT规则：`types.type[*].pictures`改为`types.type[*].images`
* ［长期］完善CWT配置支持
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
  * 可以通过内置的Github链接从网络下载需要的CWT配置文件（使用Git克隆远程仓库，然后保存到本地）
  * 包括官方Github链接~~和镜像Github链接（https://github.com.cnpmjs.org）~~
  * 检查Github上最新的CWT配置文件仓库，提高CWT配置文件语法解析的兼容性
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
* [X] 实现paradoxScript的InlayHintsProvider，目前为定义提供来自本地文件的名字（如：对于特质`agenda_defensive_focus`，名字是`保卫边境`）
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
