# Changelog

> 除非明确说明：
> 
> * 这里提到的路径/filePath指：文件相对于游戏或模组根路径的路径。
> * 脚本文件中定义的本地化名字指：定义的最相关的本地化文本（基于`cwt`规则文件，注有`## primary`的，或者从特定名字的定义属性的值推断来的，这特定名字可以在设置页面中设置）
> * 脚本文件中的变量指：如`@var = 123`这样写在定义结构之外的声明（事实上是常量，另有高级语法）（`scripted_variable`，封装变量）
> * 待办项中右边的星号（`*`）表示这是目前正计划实现的功能。

## TODO

* 完善在线参考文档
* 功能优化：
  * [ ] 编辑本地化文件时提供输入彩色文本、图标等的快捷键（仅在可用的位置生效）
  * [ ] 确认重命名功能能够预期正确进行（如果对应的声明/引用支持重命名）
  * [ ] 确认`inline_math`和`scripted_loc`是否需要进行转义的最终条件，并添加到对应的检查中
  * [ ] `commandField`额外支持`$@variableName$`的写法，其中`variableName`是全局封装变量（位于`common/scripted_variables`中）（来自CWT：`localisations.log`）
  * [ ] 将本地化命令（`[xxx]`中的xxx，所有文本）解析为单个元素，如果解析scopeFieldExpression一样，将此作为localisationCommandExpression并解析，实现相关功能：检查、提示等
  * [ ] 基于facet或者合成库`SyntheticLibrary`+自定义设置配置模组的游戏类型、游戏目录、依赖模组列表等配置
  * [ ] 内嵌提示的预览文本中不再包括特殊注释，而是通过向psiFile中注入特定userData的方式提供必要的信息（类型、本地化等）
  * [ ] 基于引用的重命名需要考虑存在前后缀的情况（主要是为图标引用考虑）
  * [ ] 需要重新调整对返回的规则列表的排序
* 新增功能：
  * [ ] 实现后缀补全：对于变量操作表达式，如`var.set` → `set_variable = { which = var value = ? }`（需要区分游戏类型，需要基于CWT规则，需要进一步研究） +
  * [ ] 实现代码折叠：对于变量操作表达式，如`set_variable = { which = var value = 1 }` → `var = 1`（需要区分游戏类型，需要基于CWT规则，需要进一步研究） +
  * [ ] ~~添加检查：图标属性的值引用了定义自身（`foo { icon = foo ... }`）（不觉得这有什么意义）~~
  * [ ] ［搁置］添加内嵌提示：定义和定义元素的作用域的内嵌提示（需要研究）
  * [ ] 实现工具栏动作：生成所有缺失语言区域的本地化文件
  * [ ] 实现工具栏动作：生成所有缺失的相关本地化
  * [ ] 可以通过特殊注释强制指定定义类型（基于文件路径或者基于直接的类型+子类型） - 用于实现差异比较等功能
  * [ ] 实现对`*.gui`文件中的GUI定义的UI预览（参考IDEA的Markdown插件的实现）
  * [ ] 实现对`*.txt`文件中的定义的UI预览（参考游戏中的效果以及灰机Wiki的实现）
  * [ ] 实现动作：与重载或者被重载的其他文件作比较（对于文件，在编辑菜单/右键菜单/项目视图右键菜单中）（要比较的文件拥有相同的相对于游戏或模组目录的路径，不限制文件类型）
  * [ ] 实现动作：与重载或者被重载的其他定义/本地化作比较（对于定义/本地化，在编辑菜单/右键菜单/项目视图右键菜单中）（要比较的定义/本地化拥有相同的名字）
  * [ ] 添加引用解析：用于格式化的本地化文本中的格式化分段（`format.xxx: "<some_parts> ...""`中的`<some_parts>`）
* 完善CWT配置支持：
  * [ ] ~~为复杂枚举如`complex_enum[policy_option]`提供相关本地化支持（类似定义）~~（搁置，不能很好地显示出来，复杂枚举名可能本身就是一个本地化引用）
  * [ ] 优化：更好地兼容嵌套的定义
  * [ ] 编写工具类支持解析`localistions.log` `modifiers.log` `scopes.log` `trigger_docs.log`等日志文件，生成对应的cwt文件
  * [ ] 优化：scope的名字（准确来说是别名）可以包含点号
  * [ ] 优化：检查scopeFieldExpression时也检查是否匹配对应的scopeName或者scopeGroupName（归类到`MismatchScopeInspection`）
  * [ ] 支持基于CWT规则校验脚本结构（仅限定义元素）
  * [ ] 优化：支持处理`value`和`value_set`自带的作用域信息（支持valueSetValueExpression，如`val@root.owner`）
  * [ ] 对于link `pop_faction_parameter`和complexEnum `pop_faction_parameters`的特殊处理 ，前者只能在`pop_faction`中使用且作为数据源的complexEnumValue只能来自同一定义声明中
* 遗留问题：
  * [ ] 有时候会把`DISTRICT = district_arcology_housing`的`DISTRICT`被识别为scope_expression而非参数名，为什么？
  * [ ] 有时候`event_target:mechanocalibrator_country`中的`event_target:`无法点击导航到CWT，为什么？

## 0.7.4

* BUG修复：
  * [X] 修复无法从项目文件中的声明导航到库中的引用的问题（考虑使用`UseScopeEnlarger`或`ResolveScopeManager`）
  * [X] 可以从定义名并非rootKey的定义（如event）的声明处导航到所有使用处（鼠标放到定义的rootKey上，然后Ctrl+鼠标左键）
  * [X] 从任意同名同类型的封装变量/定义/本地化/文件路径出发，可以通过查找使用导航到所有那个名字的使用
    * [X] 存在定义/本地化的引用高亮不正确的奇怪BUG - 已修复，需要使用`queryParameters.effectiveSearchScope`而非`target.useScope`
    * [X] 必要时需要重载`PsiElement.isEquivalentTo()`方法
  * [ ] 兼容更复杂的表达式的情况，如：`root.owner.event_target:target@root.owner`
  * [X] 兼容`value_field`或者`int_value_field`需要被识别为变量的情况（`root.owner.var_name`）（通过更新CWT规则文件`links.cwt`）
  * [X] 脚本文件中来自CWT文件的引用需要能被同时高亮出来，同时一般情况下不能从CWT文件中的规则查找引用（`modifier`等除外）
    * 对于别名无法正常工作（`if` != `alias[effect:if]`）
  * [X] 兼容localisationCommandScope需要被识别为`value_set[event_target]`或者`value_set[global_event_target]`的情况
  * [X] 兼容localisationCommandField需要被识别为`value_set[variable]`的情况
* 功能优化：
  * [X] 对CWT别名规则（dataType=alias/single_alias）使用特殊的别名图标，以便区分内联前后的CWT规则
  * [X] 在单纯地匹配CWT规则以找到对应的CWT规则时，不应该要求索引，否则可能会引发IDE异常：`java.lang.Throwable: Indexing process should not rely on non-indexed file data.`
* 功能变更：
  * [X] ~~支持额外的CWT选项：`## icon = <icon_type>`，用于重载进行代码补全时需要显示的图标，如`## icon = tag`~~ → 使用CWT选项`## tag`标记特殊标签，如`optimize_memory`
* 新增功能：
  * [X] 实现检查：参数（`$PARAM$`）被设置/引用但未被使用（例如：有`some_effecFt = { PARAM = some_value }`但没有`some_effect = { some_prop = $PARAM$ }`，后者是定义的声明。）
  * [X] 实现检查：值集中的值（`some_flag`）被设置但未被使用（例如，有`set_flag = xxx`但没有`has_flag = xxx`。）
  * [X] 实现检查：值集中的值（`some_flag`）被使用但未被设置（例如，有`has_flag = xxx`但没有`set_flag = xxx`。） - 默认不启用
  * [X] 实现内嵌提示：本地化图标（渲染出选用的内嵌图标，如果对应图标的大小合适）
  * [X] 实现内嵌提示：预定义修饰符的本地化名字（`mod_$`）
  * [X] 实现动作：导航到（对应的）CWT规则（对于定义元素，在导航菜单/右键菜单中）（作为一个更加统一的入口，包括内联前后的CWT规则，包括所有完全匹配的规则）
  * [X] 在查找使用中，区分参数和值集中的值的读/写使用
  * [X] 在查找使用中，区分使用的使用类型（基于读写和对应的CWT规则）（待日后完善） *
  * [X] 可设置要忽略的本地化文件的名字
  * [X] 为图标提供提示（tooltip），例如，鼠标悬浮到结构视图（Structure）中的节点图标上即可看到
  * [ ] 可以提示本地化颜色的ID（由于颜色ID只有一个字符，在本地化文本中输入`"§"`后，手动调用代码提示功能（例如，按下`Ctrl+空格`），才会进行提示
* 完善CWT配置支持：
  * [X] 支持`complex_enum`，以及相关功能：匹配、代码提示
  * [X] 支持高亮`definitionName` `complexEnumValueName`（对应的PSI元素可能本身就对应着特定的CWT规则，需要同时高亮出来）
  * [X] 为`complexEnumValue`的引用（而非声明）提供特殊文档
  * [X] 以某种方式另外实现`definitionName` `complexEnumValueName`的文档、查找使用、导航到类型声明等功能 - 通过意向（intention）
  * [X] 为预定义的`modifier`提供相关本地化支持（`mod_$` `mod_$_desc` `mod_category_$`等，需要确定具体规则是什么） *

## 0.7.3

* [X] 更新cwt规则到最新版本（2022/10/13）
* 功能优化：
  * [X] 如果通过代码检查发现一个定义上有多个缺失的相关本地化/图片，将每个单独作为一个问题
  * [X] 参数（`$PARAM$`）和值集中的值（`some_flag`）并不存在一个事实上的声明处，点击需要导航到所有引用处
  * [X] 测试通过对参数（`$PARAM$`）和值集中的值（`some_flag`)基于引用的统一重命名
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
  * [X] 脚本文件：可以显示类型信息以及导航到类型定义，如果可用 - 支持定义元素，显示其规则表达式以及导航到规则声明
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
  * [X] 脚本文件：值集中的值的内嵌提示（值的类型即值集的名字，`xxx = value_set[event_target]`中的`event_target`）
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
  * [X] 支持额外的CWT选项：`## since = "3.3"`，用于在文档注释中显示开始支持的游戏版本号
  * [X] 支持额外的CWT选项：`## loc_format = html`，用于指定将文档注释渲染为HTML
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
  * [X] 优化获取和应用定义元素（属性和值）的CWT规则的代码逻辑（基于CWT规则和elementPath，用于实现代码提示、代码检查等）
  * [X] 支持匹配和提示参数名（`some_effect = { PARAM = xxx }`中的`PARAM`）
  * [X] 优化：支持匹配、提示和解析参数，以及显示参数信息 （当可用时）
  * [X] 本地化文件：支持提示和解析本地化命令作用域（commandScope）
  * [X] 优化：提示modifier时排除不匹配supported_scopes的，提示scope时排除其input_scopes不匹配上一个scope的output_scope的
  * [X] 优化：提示scopeFieldPrefix和scopeFieldDataSource时排除其input_scopes不匹配上一个scope的output_scope的
  * [X] 优化：提示valueFieldPrefix和valueFieldDataSource时排除其input_scopes不匹配上一个scope的output_scope的
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
  * [X] 定义元素的上下文信息（如：`definitionKey = {`）
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
* [X] 修复当规则类型为alias_name或者引用为modifier时，无法解析definitionPropertyKey的引用的bug
* [ ] definitionProperty本身就有对应的引用（如definition）的情况时，是否同样将规则对应的cwtKey/cwtValue作为它的引用？
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
