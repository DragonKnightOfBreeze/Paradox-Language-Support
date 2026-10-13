# 内联脚本文件的文件级别提示

> 所属模块：`后端 > 语言功能支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.0
> - 日期：2026-07-11
> - 状态：已实施

说明：
- 如果一个脚本文件声明了内联脚本，应提供编辑器通知，而非提供代码检查。同时以文本链接的形式提供动作，以便快速查找用法。

原因：
- 代码检查仅适用于项目文件，不适用于外部库中的文件，优点是可以通过快速入口（右侧的齿轮图标）关闭或者更改高亮级别。
- 编辑器通知则没有这条限制，可以自行配置，缺点是无法通过快速入口关闭。
- 如果提供代码检查而非编辑器通知，这会导致在编写模组时，浏览游戏文件时无法得到提示，以及快速查找用法。
- 因此，需要回退，仍然提供编辑器通知，而非代码检查。

相关代码位置：
- [x] `icu.windea.pls.lang.inspections.script.inlineScript.DeclaredInlineScriptInspection` - 需要移除
- [x] `icu.windea.pls.lang.editor.ParadoxInlineScriptEditorNotificationProvider` - 需要新增
