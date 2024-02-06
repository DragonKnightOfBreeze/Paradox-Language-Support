# TODO

## BUG记录

来自调试：

* [X] 需要兼容整行或多行参数值中对双引号的特别转义
* [X] 需要兼容本地化文本中对左方括号的转义（`[[`）
* [ ] 在`PARAM = xxx`后面输入回车会产生不正确的自动缩进（应当与语言注入有关）
* [ ] `QuoteIdentifierIntention`和`UnquoteIdentifierIntention`不直接适用于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""`中的的`\"v\"`）
* [ ] 对于用引号括起的参数值中的那些字面量（例如，`p = "\"v\""`中的的`\"v\"`），如果用引号括起，由于已被转义一次，会导致中断规则匹配 - IDEA自身BUG？

来自Github仓库：

* [X] 无法及时检测项目本地的规则文件的更改？ - 基本修复？
* [ ] 无法正确注入`on_action`中事件引用的事件类型？ - 未复现，很神奇
* [ ] 重新打开项目后，IDE无法完成对脚本文件的解析？ - 可能是内存不足导致
* [ ] `scope[species]`应当始终可以匹配`this` - 如果无法准确匹配，需要直接匹配同类型的第一个规则 - 待验证
* [X] 作用域上下文的内嵌提示显示有问题 - 应当显示在`possible = {`后面，且要求`{`之后不存在空白以外的字符
* [X] `some_trigger = value`中的`value`不应当被特别高亮

来自CWTools的Github仓库：

* [X] [The tool cannot recognize in-script flag variables (Vic3) · Issue #76 · cwtools/cwtools-vscode (github.com)](https://github.com/cwtools/cwtools-vscode/issues/76)
* [X] [[Stellaris\] Could support tech@level grammar? · Issue #58 · cwtools/cwtools-vscode (github.com)](https://github.com/cwtools/cwtools-vscode/issues/58)
* [X] [Parsing issues in Vic3 · Issue #53 · cwtools/cwtools (github.com)](https://github.com/cwtools/cwtools/issues/53)

## 更新计划

* 完善在线参考文档
* BUG修复
* 功能优化
  * [ ] 有些地方获取的作用域是可能的（`from = country?`）
  * [ ] 确认重命名功能能够预期正确进行（如果对应的声明/引用支持重命名）
  * [ ] 基于引用的重命名需要考虑存在前后缀的情况（主要是为图标引用考虑）
  * [ ] ~~一些检查可以基于当前文件、文件路径（相对于游戏或模组根目录）、定义类型（例如，`event`）、定义成员路径（例如，`event.abc`）等来部分禁用~~（已经没有必要）
  * [ ] 可扩展的对本地化命令、本地化命令作用域的支持（包括引用解析、作用域获取、代码补全等）
* 功能优化 - CWT规则支持
  * [ ] ~~兼容CWT规则文件中的错误级别`severity = warning`或`## severity = warning`~~（PLS和CWTools实现有所不同，需要分析）
  * [ ] CWT规则文件中有些之前被认为必定是常量字符串的地方（如枚举值的名字），也可能是一个表达式而非仅仅是常量字符串
  * [ ] ［待确定］作为trigger的值的CWT规则`scope_field` `scope[xxx]` `scope_group[xxx]`也可以匹配一个布尔值？
* 功能优化 - 智能推断
  * [X] 基于使用处推断一些定义（如`scripted_effect`）的作用域上下文
  * [ ] 基于使用处推断本地化命令的作用域上下文
  * [ ] ~~可以通过特殊注释强制指定定义类型（基于文件路径或者基于直接的类型+子类型） - 用于实现差异比较等功能~~（不考虑）
* 新增功能
  * [X] 操作：从指定的本地化文件生成其他语言区域的本地化文件（右键菜单&项目视图&工具栏操作，考虑支持指定多个或者整个目录的情况）
  * [ ] 操作：从封装变量/定义/本地化/文件进行重载（通过对话框选择生成的位置）
  * [ ] ［待确定］实现对`*.gui`文件中的GUI定义的UI预览（参考IDEA的Markdown插件的实现）
  * [ ] ［待确定］实现对`*.txt`文件中的定义的UI预览（参考游戏中的效果以及灰机Wiki的实现）
  * [ ] ［低优先级］对于封装变量、定义、本地化和内联脚本实现安全删除功能
  * [ ] ［低优先级］对于封装变量、scripted_trigger、scripted_effect、inline_script等实现内联功能

## 更新计划 - 追踪中

* [ ] #46 优化：尝试基于使用推断特定类型的`valueSetValue`对应的作用域上下文（如`event_target`和`global_event_target`）
* [ ] 提供一种项目视图，用于显示合并后的所有用于自定义CWT规则分组的CWT文件（`Project Pane -> CWT Config Files`）
* [ ] 兼容直接在键或字符串中使用参数条件块（`foo_[[bar]bar]_desc`） - 兼容语法以及相关功能
* [X] 完善对定义的作用域上下文推断的支持 - 完善实现逻辑，优化性能，支持`scripted_trigger`、`scripted_effect`和`event`
* [ ] DDS文件路径以及符合条件的快速文档链接也能作为html/markdown等文件中的图片超链接使用，从而渲染DDS图片和本地化
* [ ] 改为基于语言注入功能（`Language Injection`）支持脚本文件中的各种复杂表达式以及本地化文件中的本地化命令表达式
* [ ] 将获取作用域上下文的代码提取成扩展点
* [ ] 对任何带有作用域上下文的声明或引用（包括CWT规则的引用），统一提示作用域上下文
* [X] 参照Irony Mod Manager的实现，实现扩展点以在查询时，如果有必要，按照覆盖顺序排序封装变量/定义/本地化
* [ ] 添加代码检查，基于下列规则检查脚本文件中是否存在可能的BUG
  * ~~scripted_trigger/scripted_effect不能递归调用~~（已实现对应的代码检查）
  * scripted_trigger/scripted_effect的调用层数最大只能有5层
  * ~~内联数学表达式（inline_math）在每个scripted_trigger/scripted_effect中最多只能使用1次~~（当前游戏版本已无此限制）
  * 对于valueSetValue，只能通过后缀的`@xxx`切换flag和event_target的作用域
  * ~~不能在asset文件中使用scripted_variable和inline_math~~（已实现对应的代码检查）
* [ ] 在更多情况下尝试推断脚本参数对应的CWT规则，从而提供各种高级语言功能（如，基于CWT规则的代码高亮、引用解析和代码补全）
* [ ] 支持语法`$ARG1|ARG2$`（在3.10.0的更新日志中被提及，但是太过神秘，暂不考虑兼容）

## 更新计划 - 规则文件

* CK2 - 搁置
* CK3 - 搁置
* EU4 - 搁置
* HOI4 - 搁置
* IR - 搁置
* Stellaris - 3.10.0
  * 基于日志文件的规则 ✔
  * 基于游戏文件的规则 ✔
  * 检查官方更新日志 ✔
  * 检查作用域 ✔
* VIC2 - 搁置
* VIC3 - 搁置
