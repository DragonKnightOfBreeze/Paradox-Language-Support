@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.lang

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaclisationParameterManager
import icu.windea.pls.lang.util.ParadoxTagManager
import icu.windea.pls.lang.util.data.ParadoxDataService
import icu.windea.pls.lang.util.presentation.ParadoxPresentationService
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
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

inline val ParadoxScriptDefinitionElement.definitionInfo: ParadoxDefinitionInfo? get() = ParadoxDefinitionManager.getInfo(this)

inline val ParadoxScriptProperty.definitionInjectionInfo: ParadoxDefinitionInjectionInfo? get() = ParadoxDefinitionInjectionManager.getInfo(this)

inline val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueIndexInfo? get() = ParadoxComplexEnumValueManager.getInfo(this)

inline val ParadoxScriptValue.tagType: ParadoxTagType? get() = ParadoxTagManager.getTagType(this)

inline fun ParadoxLocalisationParameter.resolveLocalisation(): ParadoxLocalisationProperty? = ParadoxLocaclisationParameterManager.resolveLocalisation(this)

inline fun ParadoxLocalisationParameter.resolveScriptedVariable(): ParadoxScriptScriptedVariable? = ParadoxLocaclisationParameterManager.resolveScriptedVariable(this)

inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getDefinitionData(relax: Boolean = false): T? = ParadoxDataService.get(this, relax)

inline fun <reified T : ParadoxDefinitionPresentation> ParadoxScriptDefinitionElement.getDefinitionPresentation(): T? = ParadoxPresentationService.get(this)

// Language Extensions

fun Char.isIdentifierChar(): Boolean {
    return StringUtil.isJavaIdentifierPart(this)
}

fun String.isIdentifier(vararg extraChars: Char): Boolean {
    return this.all { c -> c.isIdentifierChar() || c in extraChars }
}

fun String.isParameterAwareIdentifier(vararg extraChars: Char): Boolean {
    // 比较复杂的实现逻辑
    val fullRange = TextRange.create(0, this.length)
    val parameterRanges = ParadoxExpressionManager.getParameterRanges(this)
    val ranges = TextRangeUtil.excludeRanges(fullRange, parameterRanges)
    ranges.forEach f@{ range ->
        for (i in range.startOffset until range.endOffset) {
            if (i >= this.length) continue
            val c = this[i]
            if (c.isIdentifierChar() || c in extraChars) continue
            return false
        }
    }
    return true
}

fun String.isParameterized(conditionBlock: Boolean = true, full: Boolean = false): Boolean {
    return ParadoxExpressionManager.isParameterized(this, conditionBlock, full)
}
