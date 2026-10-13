# 重构和完善与 `ItemPresentation` 相关的代码

> 所属模块：`后端 > 语言功能支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.3
> - 日期：2026-09-16
> - 状态：已实施

思路（旧）：
- 引入或规范化为统一的 presentationService。
- `script.psi.ParadoxScriptPsiPresentationService` 等用于提供各种场景下的 icon/presentableText/locationString 等，适用于结构视图/导航栏/面包屑等。它们默认是语法级别的，但有时需要提升到语义级别，这也意味着一些目前无法避免的模块之间的循环依赖。
- `lang.psi.presentation.ParadoxPsiPresentationService` 则提供各种语义级别的 name/presentebleName 等，例如定义的名字/展示名字。
- 对于语义级别的展示，即使匹配语义，展示名字仍然可能为 null。需要考虑如何兼容这种情况，也是是一个可选的 marker 参数？

思路（2026-09-15）：
- 以 CWT 语言为例，让 `NavigatablePsiElement` 的 `getPresentation` 方法返回 `CwtElementPresentation`，而其中的方法又委托给 `CwtElementPresentationService` 中的对应方法。
- 以 CWT 语言为例，`CwtElementPresentationService` 也提供适用于 structureView/navigationToolbar/breadcrumbs 的对应方法，但是具体逻辑可能有所不同。
- 除了语法级别的 presentationService 之外，另外也有语义级别的 presentationService。
- 语法级别的展示有时需要提升到语义级别，这也意味着一些目前无法避免的模块之间的循环依赖。

笔记（2026-09-15）：
- 参考平台源码，对于适用于 `PsiElement` 的 `ItemPresentation`，并不需要为其创建锚点/指针（如，通过使用 `TreeAnchorizer.getService().createAnchor(element)`）。
- 对于语义级别的展示，如果匹配语义，需要保证不返回 null（例如，改为返回空字符串）。
- 这项重构并不是非常必要，不过实施了之后，可以一定程度上缓存强迫症……

级联更改（2026-09-15）：
- [x] 以 CWT 语言为例，将 `CwtElementManipulationService` 重命名为 `CwtElementManipulationService`（更加底层的服务类 + 与 `CwtElementManipulators` 对齐）。
- [x] 以 CWT 语言为例，将 `CwtElementDescriptionService` 重命名为 `CwtElementDescriptionService`（更加底层的服务类 + 与 `CwtElementDescriptionProvider` 对齐）。
- [x] 以 CWT 语言为例，将 `CwtElementPresentationService` 重命名为 `CwtElementPresentationService`（更加底层的服务类 + 与 `CwtElementPresentation` 以及上述服务类对齐）。
- [x] 以 CWT 语言为例，将 `CwtFile` 分离为 `CwtFile`（接口）和 `CwtFileImpl`（实现，其中的部分方法实现委托给 `CwtPsiImplUtil` 中的对应方法）。

参考代码位置：
- `com.intellij.navigation.ItemPresentation`
- `com.intellij.navigation.ItemPresentationProviders`
- `com.intellij.ide.structureView.TextEditorBasedStructureViewModel`
- `com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension`
- `com.intellij.ui.breadcrumbs.BreadcrumbsProvider`
- `icu.windea.pls.cwt.psi.CwtPsiPresentationService`

相关代码位置（以 CWT 语言为例）：
- [x] `icu.windea.pls.cwt.psi.CwtElementPresentation`
- [x] `icu.windea.pls.cwt.psi.CwtElementPresentationService`

相关代码位置（语义级别）：
- [x] `icu.windea.pls.lang.psi.CwtConfigPresentationService`
- [x] `icu.windea.pls.lang.psi.ParadoxPresentationService`
