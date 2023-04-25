# BUGS

## KEEP

* [ ] 复杂表达式中的参数被高亮成了白色（为啥？）

***

一些可能过慢的代码检查：

* `icu.windea.pls.script.inspections.general.MissingExpressionInspection`
* `icu.windea.pls.script.inspections.general.TooManyExpressionInspection`
* `icu.windea.pls.script.inspections.general.UnusedParameterInspection`
* `icu.windea.pls.script.inspections.general.UnusedValueSetValueInspection`
* `icu.windea.pls.script.inspections.general.UnsetValueSetValueInspection`

可能的原因：

* `ReferencesSearch.search()`

***

```
# 无法提示move和moving
propagate_state = { "move" = "moving" }
```

疑难杂症：

* [X] ~~`utility_component_template`定义声明完全无法解析？为啥啊？~~（没有复现）
* [X] ~~本地化图标的读写引用高亮再次失效~~（生成的`ParadoxLowLevelSearchUtil.class`文件不对）
* [ ] 脚本参数名是忽略大小写的
* [ ] scopeContext应当可以跨内联向下传递
* [ ] `enum[feature_flags]` - 这个存在相关的本地化
* [ ] 支持传统中的图标继承（`traditions: inherit icon from parent original tradition if inherit_icon = yes`）
* [ ] 支持事件继承
* [ ] 支持`complex_trigger_modifier`中的`trigger`和`trigger_scope`，后者作为前者的scope

## 0.9.12

* [ ] 兼容在VCS提交记录中正常查看脚本文件、本地化文件、DDS图片，并且如果可能，基于本地版本提供高级语言功能

```
com.intellij.diff.contents.FileContentImpl.FileContentImpl(com.intellij.openapi.project.Project, com.intellij.openapi.vfs.VirtualFile, com.intellij.openapi.vfs.VirtualFile)
com.intellij.diff.contents.FileDocumentContentImpl.FileDocumentContentImpl(com.intellij.openapi.project.Project, com.intellij.openapi.editor.Document, com.intellij.openapi.vfs.VirtualFile, com.intellij.openapi.vfs.VirtualFile)
com.intellij.diff.DiffContentFactoryImpl.DocumentContentBuilderImpl.build

//通过代码注入器注入injectedFileInfo？
```

* [ ] 如果可能，直接在IDE编辑器中渲染DDS图片，而不是基于缓存的PNG图片

## 0.9.9

* [X] 无法索引需要skipRootKey的定义
* [X] 无法代码补全本地封装变量
* [X] 调用层次应当支持配置要查询的定义类型

## 0.9.3

* [X] 正确匹配子句为空以及子句不为空的`resources = {...}`
* [X] 自定义折叠（`region...endregion`）有时无法正确折叠文本

## 0.9.2

* [X] 对于value必要时也要显示scopeContext
* [X] 对于`alias_keys_field[xxx]`进行匹配、提示、解析时也要匹配scopeContext
* [X] 修复无法查找CWT规则的引用的问题

## 0.9.1

* [X] `root.owner`之后的`prev`，得到的作用域并非`root`之后的作用域，而应当是`root`来自的作用域

## 0.8.3

* [X] `int[-inf..inf]` - 允许这样的写法 
* [X] 更新`on_actions.cwt`
* [X] 科技树图表 - item排序不正确 - 改为基于其category在contentCategories中的顺序进行排序
* [X] 事件树图表 - 生成过慢 - 指定名字查询本地化索引最慢可能需要400ms+ - 优化后需要0~5ms
* [X] 无法快速查找event（通过event id）（以及其他任何definitionName不为rootKey的定义，应当如此） - 已解决，需要使用`FakeElement`

## 0.8.2

* [X] 无法对`value:`后的内容进行提示（已解决）
* [X] ~~`MissingParameterInspection` - 修正后不会清除警告~~ （不再复现）

## 0.8.1

* [X] 默认游戏类型或者模组游戏类型变更时，检查是否会强制重新解析和索引相关文件（如果有必要） - 会
* [X] 默认游戏类型或者模组游戏类型变更时，检查是否也需要强制刷新inlayHints（如果有必要） - 不需要
* [X] 偏好语言区域变更时，检查是否会强制刷新inlayHints
* [X] 模组或游戏配置未提交更改时，不应该更改真正的配置
* [X] 使用合成库后，快速文档等地方显示的库名是空的 - 优化为显示游戏/模组的名字和版本信息
* [X] 游戏/模组配置 - 路径为空时的检查
* [X] 游戏/模组配置 - 忽略添加的已有的模组依赖
* [X] 补充复制各种路径的操作性（编辑器右键菜单、工具菜单）
* [X] 重新启动项目时，需要索引已配置的游戏目录和模组目录
* [X] 查找使用时，可以配置作用域以及其他一些选项（参见`FindUsagesHandlerFactory`）
* [X] `preferSameRoot`改为`contextSensitive`（首先尝试选用同一根目录下的，然后尝试选用同一文件下的）

## 0.7

* [X] 有时候会把`DISTRICT = district_arcology_housing`的`DISTRICT`被识别为scope_expression而非参数名，为什么？（缓存原因）
* [X] 有时候`event_target:mechanocalibrator_country`中的`event_target:`无法点击导航到CWT，为什么？（应当也是缓存原因）
* [X] 将文件中UTF8编码不为32的空格视为语法错误，但允许跳过语法错误继续解析（例：UTF8编码为160的空格，`\u00a0`）
* [X] 对于脚本颜色（scriptColor），颜色设置应当同步更改到文件中（目前仅限于第一次更改会同步）
* [X] 当项目中存在模组文件夹（基于`descriptor.mod`）时，应当弹出通知要求玩家选择对应的原版游戏文件夹（参考CwTools）
  ![](BUGS.assets/image-20220404120657429.png)
  ![](BUGS.assets/image-20220404120720948.png)

## 0.5

* [X] 完善本地化文本（localisationText）的解析规则：允许单独的美元符号（`$`）
* [X] 完善本地化文本（localisationText）的解析规则：本地化文本中连续的左方括号会起到转义的效果，之后的方括号中的内容不再视为本地化命令（localisationCommand）
* [X] 完善脚本文件（scriptFile）的解析规则：分隔符之前允许换行
* [X] 对于脚本颜色（scriptColor），颜色设置应该保持颜色类型（`rgb` `hsv`等）
* [X] 是否缺失event namespace的检查，应当仅对于events目录下的脚本文件（scriptFile）生效
      ![](BUGS.assets/image-20220404120448300.png)
* [X] BUG：查找引用的对象名不准确
      ![](BUGS.assets/image-20220404115700617.png)
* [X] BUG：当要添加或移除文件的BOM时，没有在物理层面上添加机票移除
* [X] ~~BUG：当进行代码提示时，无法显示对应的快速文档和快速定义~~（测试环境问题或者IDE BUG？）
* [X] BUG：无法解析`trait.species_trait`（定义的子类型识别不准确）
* [X] CWT规则解析：兼容值不为代码块的定义（`key = value`）
* [X] CWT规则解析：`starts_with` `skip_root_key` `type_key_filter`的值需要忽略大小写
* [X] 脚本文件语法解析：变量不需要放在顶层（尽管一般放在顶层，应当是放在定义外面即可）
* [X] 兼容同一个event脚本文件中有多个event_namespace的情况
* [X] 兼容同一个本地化文件中有多个locale的情况
* [X] CWT规则解析：类型为枚举值、常量、别名名字的键/值的解析需要忽略大小写
* [X] BUG：CWT规则解析：无法匹配类型为`tradition_swap`的定义的属性的规则
* [X] BUG：CWT规则解析：对于数组中的对象（`a = { { b = c } }`），无法匹配定义的属性的规则
* [X] BUG：`EN_Re = 3`中无法补全`EN_Re`