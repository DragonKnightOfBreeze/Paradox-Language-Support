# 重构和完善各个 `ElementDescriptionProvider` 和 `FindUsagesProvider`

> - 模块：后端 > 语言功能支持
> - 版本：v3.0.2
> - 日期：2026-09-03
> - 状态：已实施

说明：
- 需要重构和完善各个 `ElementDescriptionProvider` 和 `FindUsagesProvider`，避免意外的 SOF，优化描述。

`ElementDescriptionProvider` 的常规实现通常位于 `findUsages` 子包下（非常规实现可能位于 `codeInsight.highlighting` `refactoring` 子包下）。
然而，为了与 `ElementManipulator` （以及 `...PsiManipulationService`）对齐，应当将这些常规实现直接放到对应的语言基础设施模块（如 `script`）中的 `psi` 子包下，并且另外提供 `...PsiDescriptionService`。
对于语义层的实现（包括与 代码高亮/重构 相关的那些非常规实现），考虑放到语义模块（`lang`）中的对应子包下。

`FindUsagesProvider` 和 `WordsScanner` 的实现应放到 `findUsages` 子包下。
并且，考虑到涉及索引（word index）和查找用法，应当改为将它们放到语义模块（`lang`）中。

任务：
- [x] 需要确定默认行为，以尽可能遵循规范，以及避免不必要的冗余实现。

参考代码位置：
- `com.intellij.psi.ElementDescriptionProvider`
- `com.intellij.lang.findUsages.FindUsagesProvider`
- `com.intellij.lang.cacheBuilder.WordsScanner`
- `com.intellij.refactoring.util.DefaultRefactoringElementDescriptionProvider`
  - 考虑到存在这个（type + descriptiveName），可能没有必要提供 `ParadoxRefactoringElementDescriptorProvider`。

相关代码位置：
- `ElementDescriptionProvider`
  - [x] `icu.windea.pls.script.psi.ParadoxScriptElementDescriptionProvider`
  - [x] `icu.windea.pls.script.psi.ParadoxScriptPsiDescriptionService`
  - [x] `icu.windea.pls.localisation.psi.ParadoxLocalisationElementDescriptionProvider`
  - [x] `icu.windea.pls.localisation.psi.ParadoxLocalisationPsiDescriptionService`
  - [x] `icu.windea.pls.cwt.psi.CwtElementDescriptionProvider`
  - [x] `icu.windea.pls.cwt.psi.CwtPsiDescriptionService`
  - [x] `icu.windea.pls.lang.psi.CwtConfigElementDescriptionProvider`
  - [x] `icu.windea.pls.lang.psi.CwtConfigPsiDescriptionService`
  - [x] `icu.windea.pls.lang.psi.ParadoxElementDescriptionProvider`
  - [x] `icu.windea.pls.lang.psi.ParadoxPsiDescriptionService`
  - [x] ~~`icu.windea.pls.lang.refactoring.ParadoxRefactoringElementDescriptorProvider`~~ - 移除（不需要）
- `FindUsagesProvider`
  - [x] `icu.windea.pls.lang.findUsages.ParadoxScriptFindUsagesProvider`
  - [x] `icu.windea.pls.lang.findUsages.ParadoxLocalisationFindUsagesProvider`
  - [x] `icu.windea.pls.lang.findUsages.ParadoxCsvFindUsagesProvider`
  - [x] `icu.windea.pls.lang.findUsages.CwtFindUsagesProvider`
- `WordsScanner`
  - [x] `icu.windea.pls.lang.findUsages.ParadoxScriptWordsScanner`
  - [x] `icu.windea.pls.lang.findUsages.ParadoxLocalisationWordsScanner`
  - [x] `icu.windea.pls.lang.findUsages.ParadoxCsvWordsScannerr`
  - [x] `icu.windea.pls.lang.findUsages.CwtWordsScanner`
