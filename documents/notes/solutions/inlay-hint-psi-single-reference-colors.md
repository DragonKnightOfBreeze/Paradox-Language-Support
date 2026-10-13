# 对于 non-declarative 的内嵌提示，使用 `psiSingleReference` 不会自动更改配色

> 所属类别：`后端`
> 上级导航：[问题解决与避坑索引 (Solutions Index)](README.md)

对于 `PresentationFactory.psiSingleReference`，按住 ctrl 并将鼠标放到对应的文本上，不会自动更改配色为 `EditorColors.REFERENCE_HYPERLINK_COLOR`

备注：如果要让鼠标同时变成手形，需要手动调用 `PresentationFactory.withCursorOnHoverWhenControlDown`

调用堆栈：
- `com.intellij.codeInsight.hints.presentation.WithAttributesPresentation#paint` 似乎在切换时未被正确调用
- `com.intellij.codeInsight.hints.presentation.InlayPresentation.paint` 
  - `com.intellij.codeInsight.hints.presentation.PresentationRenderer.paint`
  - `com.intellij.codeInsight.hints.LinearOrderInlayRenderer.paint`
- `com.intellij.codeInsight.hints.InlayContentListener` 这个监听器才会真正重新绘制
- `com.intellij.openapi.editor.impl.InlayImpl.repaint` 这里是重新绘制的方法
  - `com.intellij.openapi.editor.impl.InlayImpl.update` 在这里被调用（如果 `changeFlags` 为0）
- `com.intellij.codeInsight.hints.InlayHintsPass.Companion.postprocessInlay`
- `com.intellij.codeInsight.hints.presentation.RecursivelyUpdatingRootPresentation.paint`

- 按住 ctrl 并将鼠标放到对应的文本上时，确实会调用 `com.intellij.codeInsight.hints.LinearOrderInlayRenderer.paint`
- 此时的 presentation 的堆栈，从 root 开始：
  - `RecursivelyUpdatingRootPresentation`
  - -> `MenuOnClickPresentation`
  - -> `ScaledInsetPresentation`
  - -> `DynamicInsetPresentation`
  - -> `WithAttributesPresentation` （textAttributesKey = INLAY_DEFAULT）
  - -> `RoundWithBackgroundPresentation`
  - -> `InsetPresentation` 
  - -> `ScaledInsetPresentation`
  - -> `SequencePresentation` （`this  =  <hovered><hovered>country   root  =  country`）

结论：
- 在 `WithAttributesPresentation` 中，内层的 `INLAY_DEFAULT` 会覆盖掉外层的 `CTRL_CLICKABLE` 的 `myForeground` 字段（从蓝色覆盖为灰色）
- 如果 `other` 是 `INLAY_DEFAULT`（或者 `INLAY_TEXT_WITHOUT_BACKGROUND`），应当优先使用 `attributes` 进行渲染
- 否则，`PresentationFactory.withReferenceAttributes` 等会无法正常生效

后续：
- 存在一个微妙的问题，如果已经按下 ctrl，再让鼠标进入对应的区域，并不会更改配色。必须要鼠标首先就在区域内才行。
- 应该是由 non-declarative 的内嵌提示的实现代码决定的，影响不大，算了算了
