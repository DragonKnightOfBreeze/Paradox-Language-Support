# 高级插值

> - 模块：后端（语法支持） > 脚本文件
> - 版本：v3.0.x
> - 日期：TODO
> - 状态：TODO

对于脚本文件中的特殊语法（高级插值），需要研究在不同游戏中的支持情况，从而进行相关的重构和优化。

脚本文件中存在以下几种高级插值语法（不同游戏的支持情况可能不同）：
- **参数（parameter）**：如 `$PARAM$` 或 `$PARAM|default_value$`。这不一定被所有主要游戏类型支持，需要研究。
- **内联条件化块（inlineConditionalBlock）**：如 `[[PARAM]text]`。这不一定被所有主要游戏类型支持，可能仅适用于 Stellaris（以及基于 Jomini 的游戏类型？），需要研究。
- **通配参数（wildcardParameter）**：暂时如此命名。如 `[?PARAM]`。已知这在 Hoi4 中被使用。似乎没有对应的传入参数。应当没有在其他任何主要游戏类型中被使用（应当如此……），需要研究。
- **本地化命令（localisationCommand）**：如 `[GetName]`。已知这在 Hoi4 中被使用。应当没有在其他任何主要游戏类型中被使用（应当如此……），需要研究。

基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以在以下位置中任意组合使用：
- **属性键（propertyKey）**：如 `key = value` 中的 `key`。
- **值（value）**：如 `key = value` 中的 `value`。
- **封装变量名（scriptedVariableName）**：如 `@var = value` 中的 `@var`。
- **封装变量引用（scriptedVariableReference）**：如 `key = @var` 中的 `@var`。
- **内联数学中的封装变量引用（inlineMathScriptedVariableReference）**：如 `key = @[ var + 1 ]` 中的 `var`。

插件目前尚不兼容 Hoi4 中的那些特殊的高级插值语法。这无疑是一些非常不友好的边界情况……
