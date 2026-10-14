# 重新设计 PSI 接口（本地化文件）

> - 模块：后端 > 语言基础设施
> - 版本：v3.0.1
> - 日期：2026-07-25
> - 状态：已实施

说明：
- 富文本（richText） - 包括普通文本（text）以及各类可能仅适用于特定游戏类型的富文本（command/textFormat/等等）。
- 富文本容器（richTextContainer） - 可以直接包含富文本，包括彩色文本（colorfulText）、概念的嵌套字符串（conceptString）、文本格式的嵌套字符串（textFormatString）。也包括属性值（propertyValue）本身。

---

> - 更新日期：2026-08-28

关于插值和插值容器的说明：
- 插值容器（interpolationContainer） - 包括多种富文本（richText）和传入参数（argument）。
- 插值（interpolation） - 包括参数（parameter）、命令（command）等。
- 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
- 认为参数（parameter）不能在参数自身（包括变体，以及任意深度的子节点）中使用。
- 认为命令（command）不能在命令自身（包括变体，以及任意深度的子节点）中使用。
