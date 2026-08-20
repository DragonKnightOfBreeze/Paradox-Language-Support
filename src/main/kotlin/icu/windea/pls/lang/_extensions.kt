@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.data.ParadoxDataService
import icu.windea.pls.lang.presentation.ParadoxPresentationService
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxDefinitionCandidateManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxTagManager
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

// region Analysis Extensions

/** @see ParadoxAnalysisManager.getRootInfo */
inline val VirtualFile.rootInfo: ParadoxRootInfo? get() = ParadoxAnalysisManager.getRootInfo(this)
/** @see ParadoxAnalysisManager.getFileInfo */
inline val VirtualFile.fileInfo: ParadoxFileInfo? get() = ParadoxAnalysisManager.getFileInfo(this)
/** @see ParadoxAnalysisManager.getFileInfo */
inline val PsiElement.fileInfo: ParadoxFileInfo? get() = ParadoxAnalysisManager.getFileInfo(this)

/** @see ParadoxAnalysisManager.selectRootFile */
inline fun selectRootFile(from: Any?): VirtualFile? = ParadoxAnalysisManager.selectRootFile(from)
/** @see ParadoxAnalysisManager.selectFile */
inline fun selectFile(from: Any?): VirtualFile? = ParadoxAnalysisManager.selectFile(from)
/** @see ParadoxAnalysisManager.selectGameType */
inline fun selectGameType(from: Any?): ParadoxGameType? = ParadoxAnalysisManager.selectGameType(from)
/** @see ParadoxAnalysisManager.selectLocale */
inline fun selectLocale(from: Any?): CwtLocaleConfig? = ParadoxAnalysisManager.selectLocale(from)

// endregion

// region Resolution Extensions

/** @see ParadoxDefineManager.getInfo */
inline val ParadoxScriptProperty.defineInfo: ParadoxDefineInfo? get() = ParadoxDefineManager.getInfo(this)
/** @see ParadoxDefineManager.getNamespaceInfo */
inline val ParadoxScriptProperty.defineNamespaceInfo: ParadoxDefineNamespaceInfo? get() = ParadoxDefineManager.getNamespaceInfo(this)
/** @see ParadoxDefineManager.getVariableInfo */
inline val ParadoxScriptProperty.defineVariableInfo: ParadoxDefineVariableInfo? get() = ParadoxDefineManager.getVariableInfo(this)

/** @see ParadoxDefinitionCandidateManager.getInfo */
inline val ParadoxDefinitionElement.definitionCandidateInfo: ParadoxDefinitionCandidateInfo? get() = ParadoxDefinitionCandidateManager.getInfo(this)
/** @see ParadoxDefinitionManager.getInfo */
inline val ParadoxDefinitionElement.definitionInfo: ParadoxDefinitionInfo? get() = ParadoxDefinitionManager.getInfo(this)
/** @see ParadoxDefinitionInjectionManager.getInfo */
inline val ParadoxScriptProperty.definitionInjectionInfo: ParadoxDefinitionInjectionInfo? get() = ParadoxDefinitionInjectionManager.getInfo(this)

/** @see ParadoxComplexEnumValueManager.getInfo */
inline val ParadoxScriptExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo? get() = ParadoxComplexEnumValueManager.getInfo(this)
/** @see ParadoxComplexEnumValueManager.getInfo */
inline val ParadoxCsvExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo? get() = ParadoxComplexEnumValueManager.getInfo(this)

/** @see ParadoxTagManager.getTagType */
inline val ParadoxScriptValue.tagType: ParadoxTagType? get() = ParadoxTagManager.getTagType(this)

inline fun <reified T : ParadoxDefinitionData> ParadoxDefinitionElement.getDefinitionData(lenient: Boolean = false): T? = ParadoxDataService.getDefinitionData(this, lenient)
inline fun <reified T : ParadoxDefinitionPresentation> ParadoxDefinitionElement.getDefinitionPresentation(): T? = ParadoxPresentationService.getDefinitionPresentation(this)

// endregion

// region Expression Extensions

/** @see ParadoxExpressionManager.isParameterized */
fun String.isParameterized(conditionBlock: Boolean = true, full: Boolean = false): Boolean {
    return ParadoxExpressionManager.isParameterized(this, conditionBlock, full)
}

/** @see ParadoxExpressionManager.isParameterAwareIdentifier */
fun String.isParameterAwareIdentifier(extraChars: String = ""): Boolean {
    return ParadoxExpressionManager.isParameterAwareIdentifier(this, extraChars)
}

/** @see ParadoxExpressionManager.getParameterRanges */
fun String.getParameterRanges(conditionBlock: Boolean = true): List<TextRange> {
    return ParadoxExpressionManager.getParameterRanges(this, conditionBlock)
}

// endregion
