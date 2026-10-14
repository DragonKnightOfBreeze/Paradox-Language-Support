# 重构 service/manager 层的 relatedItems/presentableItems 相关的方法

> - 模块：后端 > 其他语义相关
> - 版本：v3.0.4
> - 日期：2026-10-06
> - 状态：已实施（告一段落）

关于：
- relatedItems：包括相关的封装变量、定义、本地化、图片等。
- presentableItems：主要包括展示名字，通常从相关的本地化获取，将对应的本地化文本渲染为纯文本。

说明：
- 规范化 API，按需使用合适的缓存策略。
- 注意这里如果直接缓存了 `PsiElement`，需要确保不会发生内存泄露（例如，依赖项包含 `PsiModificationTracker.MODIFICATION_COUNT`）。
- 为了简化 API，以展示名字为例，不再通过不同的方法区分 `getPresentableName` 和 `getPresentableNames`，而是通过参数（这里是 `onlyOne`）区分。
- 后续可以考虑进一步改为使用 processor-style 或 searcher-style（但是目前暂无必要，没必要把事情弄得更加麻烦……）。

任务：
- [x] 初步重构（`getPresentableName` + `getPresentableNames`） - 2026-10-06
- [ ] 进一步重构 - TODO

相关代码位置：
- `icu.windea.pls.lang.util.ParadoxLocalisationManager`
  - [x] `getRelatedScriptedVariables(element)` - 经过缓存
  - [x] `getRelatedDefinitions(element)` - 经过缓存
  - [x] `getRelatedLocalisationsFrom(name, contextElement, preferredLocale, onlyOne)`
  - [x] `getPresentableText(element)` - 经过缓存
  - [x] `getPresentableText(elements)` - 经过缓存
- `icu.windea.pls.lang.util.ParadoxScriptedVariableManager`
  - [x] `getRelatedLocalisations(element, preferredLocale, onlyOne)`
  - [x] `getRelatedLocalisations(name, contextElement, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(element, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(name, contextElement, preferredLocale, onlyOne)`
- `icu.windea.pls.lang.util.ParadoxComplexEnumValueManager`
  - [x] `getRelatedLocalisations(element, preferredLocale, onlyOne)`
  - [x] `getRelatedLocalisations(name, contextElement, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(element, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(name, contextElement, preferredLocale, onlyOne)`
- `icu.windea.pls.lang.util.ParadoxDynamicValueManager`
  - [x] `getRelatedLocalisations(element, preferredLocale, onlyOne)`
  - [x] `getRelatedLocalisations(name, contextElement, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(element, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(name, contextElement, preferredLocale, onlyOne)`
- `icu.windea.pls.lang.util.ParadoxModifierManager`
  - [x] `getRelatedLocalisations(element, preferredLocale, onlyOne, forName, forDesc)`
  - [x] `getRelatedLocalisations(name, contextElement, preferredLocale, onlyOne, forName, forDesc)`
  - [x] `getRelatedImages(element, onlyOne)`
  - [x] `getRelatedImages(name, contextElement, onlyOne)`
  - [x] `getPresentableNames(element, preferredLocale, onlyOne)`
  - [x] `getPresentableNames(name, contextElement, preferredLocale, onlyOne)`
- `icu.windea.pls.lang.util.ParadoxDefinitionManager`
  - [x] `getRelatedLocalisations(element, preferredLocale, onlyOne, onlyPrimary)`
  - [x] `getRelatedImages(element, onlyOne, onlyPrimary)`
  - [x] `getPresentableNames(element, preferredLocale, onlyOne)`
- `icu.windea.pls.lang.util.ParadoxSnippetManager`
  - [x] `getRelatedDefinitions(element)`
  - [x] `getRelatedLocalisations(element, preferredLocale)`
- （以及 service 层的对应方法）

相关代码位置（确保应用）：
- [x] `icu.windea.pls.lang.codeInsight.navigation` - 其中的 navigationHandler
- （目前不检查其他代码位置）

测试：
- [x] `icu.windea.pls.test.features.RelatedItemsTest`
- [x] `icu.windea.pls.test.features.PresentableItemsTest`
- [x] `src/test/testData/features/relatedItems/`
