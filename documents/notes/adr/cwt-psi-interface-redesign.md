# 重新设计 PSI 接口（CWT 文件）

> - 模块：后端 > 语言基础设施
> - 版本：v2.2.0
> - 日期：2026-07-03
> - 状态：已实施

说明：
- 成员（member） - 包括属性（property）和值（value）。
- 成员上下文（memberContext） - 可能直接或间接包含成员，包括文件、成员、成员容器。
- 成员容器（memberContainer） - 可以直接包含成员，包括块（block）、顶级块（rootBlock）。
- 语句（statement） - 仅包括成员（member）。
- 表达式元素（expressionElement） - 包括属性键（propertyKey）和值（value）。
- 值（value） - 包括布尔值（boolean）、整数（int）、浮点数（float）、字符串（string）、块（block）。
- 在成员结构中，值可能作为属性值，也可能直接位于成员容器内。
