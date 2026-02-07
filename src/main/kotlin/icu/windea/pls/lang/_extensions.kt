@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxTagManager
import icu.windea.pls.lang.util.data.ParadoxDataService
import icu.windea.pls.lang.util.presentation.ParadoxPresentationService
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue

// Property and Method Delegates

inline val VirtualFile.rootInfo: ParadoxRootInfo? get() = ParadoxAnalysisManager.getRootInfo(this)

inline val VirtualFile.fileInfo: ParadoxFileInfo? get() = ParadoxAnalysisManager.getFileInfo(this)

inline val PsiElement.fileInfo: ParadoxFileInfo? get() = ParadoxAnalysisManager.getFileInfo(this)

inline fun selectRootFile(from: Any?): VirtualFile? = ParadoxAnalysisManager.selectRootFile(from)

inline fun selectFile(from: Any?): VirtualFile? = ParadoxAnalysisManager.selectFile(from)

inline fun selectGameType(from: Any?): ParadoxGameType? = ParadoxAnalysisManager.selectGameType(from)

inline fun selectLocale(from: Any?): CwtLocaleConfig? = ParadoxAnalysisManager.selectLocale(from)

inline val ParadoxScriptProperty.defineInfo: ParadoxDefineInfo? get() = ParadoxDefineManager.getInfo(this)
inline val ParadoxScriptProperty.defineExpression: String? get() = ParadoxDefineManager.getExpression(this)

inline val ParadoxDefinitionElement.definitionInfo: ParadoxDefinitionInfo? get() = ParadoxDefinitionManager.getInfo(this)
inline val ParadoxDefinitionElement.definitionName: String? get() = ParadoxDefinitionManager.getName(this)
inline val ParadoxDefinitionElement.definitionType: String? get() = ParadoxDefinitionManager.getType(this)

inline val ParadoxScriptProperty.definitionInjectionInfo: ParadoxDefinitionInjectionInfo? get() = ParadoxDefinitionInjectionManager.getInfo(this)
inline val ParadoxScriptProperty.definitionInjectionTarget: String? get() = ParadoxDefinitionInjectionManager.getTarget(this)
inline val ParadoxScriptProperty.definitionInjectionType: String? get() = ParadoxDefinitionInjectionManager.getType(this)

inline val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo? get() = ParadoxComplexEnumValueManager.getInfo(this)

inline val ParadoxScriptValue.tagType: ParadoxTagType? get() = ParadoxTagManager.getTagType(this)

inline fun <reified T : ParadoxDefinitionData> ParadoxDefinitionElement.getDefinitionData(relax: Boolean = false): T? = ParadoxDataService.getDefinitionData(this, relax)

inline fun <reified T : ParadoxDefinitionPresentation> ParadoxDefinitionElement.getDefinitionPresentation(): T? = ParadoxPresentationService.getDefinitionPresentation(this)

// Language Extensions

fun Char.isIdentifierChar(extraChars: String = ""): Boolean {
    return StringUtil.isJavaIdentifierPart(this) || extraChars.isNotEmpty() && this in extraChars
}

fun String.isIdentifier(extraChars: String = ""): Boolean {
    if (isEmpty()) return false
    for ((_, c) in this.withIndex()) {
        if (c.isIdentifierChar(extraChars)) continue
        return false
    }
    return true
}

fun String.isParameterAwareIdentifier(extraChars: String = ""): Boolean {
    // 优化：仅在必要时获取参数范围
    if (isEmpty()) return false
    var parameterRanges: List<TextRange>? = null
    for ((i, c) in this.withIndex()) {
        if (c.isIdentifierChar(extraChars)) continue
        if (parameterRanges == null) parameterRanges = ParadoxExpressionManager.getParameterRanges(this)
        if (parameterRanges.any { it.contains(i) }) continue
        return false
    }
    return true
}

fun String.isParameterized(conditionBlock: Boolean = true, full: Boolean = false): Boolean {
    return ParadoxExpressionManager.isParameterized(this, conditionBlock, full)
}
