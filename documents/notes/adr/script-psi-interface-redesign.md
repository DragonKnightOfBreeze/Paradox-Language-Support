# 重新设计 PSI 接口（脚本文件）

> - 模块：后端 > 语言基础设施
> - 版本：v2.2.0
> - 日期：2026-07-03
> - 状态：已实施

说明：
- 成员（member） - 包括属性（property）和值（value）。脚本文件目前也被视为成员。
- 成员上下文（memberContext） - 可能直接或间接包含成员（也可能直接或间接包含特定的语句），包括文件、成员、成员容器。
- 成员容器（memberContainer） - 可以直接包含成员（也可能直接包含特定的语句），包括块（block）、顶级块（rootBlock）、条件化块（conditionalBlock）。
- 语句（statement） - 包括成员（member）、封装变量声明（scriptedVariable）、条件化块（conditionalBlock）。
- 表达式元素（expressionElement） - 包括属性键（propertyKey）和值（value）。
- 值（value） - 包括布尔值（boolean）、整数（int）、浮点数（float）、字符串（string）、块（block）等。
- 在成员结构中，值可能作为属性值，也可能直接位于成员容器内。

---

> - 更新日期：2026-08-28

关于插值和插值容器的说明：
- 插值容器（interpolationContainer） - 包括属性键、值、封装变量名、封装变量引用等。
- 插值（interpolation） - 包括参数（parameter）、内联条件化块（inlineConditionalParameter）等。
- 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
- 基本上，这些高级插值语法事实上可以传入和接收任何文本，并不要求相关语法在展开前是合法的。
- 认为在内联数学的封装变量引用（inlineMathScriptedVariableReference）中，仅能使用参数（inlineMathParameter）.
