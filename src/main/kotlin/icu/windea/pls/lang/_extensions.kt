@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.lang

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.lang.util.ParadoxAnalyzeManager
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaclisationParameterManager
import icu.windea.pls.lang.util.data.ParadoxDataService
import icu.windea.pls.lang.util.presentation.ParadoxPresentationService
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.util.concurrent.atomic.AtomicReference

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

fun String.isInlineScriptUsage(): Boolean {
    return this.equals(ParadoxInlineScriptManager.inlineScriptKey, true)
}

inline fun <T> withState(state: ThreadLocal<Boolean>, action: () -> T): T {
    try {
        state.set(true)
        return action()
    } finally {
        state.remove()
    }
}

inline fun <T> withErrorRef(errorRef: AtomicReference<Throwable>, action: () -> T): Result<T> {
    return runCatchingCancelable { action() }.onFailure { errorRef.compareAndSet(null, it) }
}

val String?.errorDetails get() = this?.orNull()?.let { PlsBundle.message("error.details", it) }.orEmpty()

// 注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
// 如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

inline val VirtualFile.rootInfo: ParadoxRootInfo?
    get() = ParadoxAnalyzeManager.getRootInfo(this)

inline val VirtualFile.fileInfo: ParadoxFileInfo?
    get() = ParadoxAnalyzeManager.getFileInfo(this)

inline val PsiElement.fileInfo: ParadoxFileInfo?
    get() = ParadoxAnalyzeManager.getFileInfo(this)

inline val ParadoxScriptDefinitionElement.definitionInfo: ParadoxDefinitionInfo?
    get() = ParadoxDefinitionManager.getInfo(this)

inline val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueIndexInfo?
    get() = ParadoxComplexEnumValueManager.getInfo(this)

inline fun ParadoxLocalisationParameter.resolveLocalisation(): ParadoxLocalisationProperty? {
    return ParadoxLocaclisationParameterManager.resolveLocalisation(this)
}

inline fun ParadoxLocalisationParameter.resolveScriptedVariable(): ParadoxScriptScriptedVariable? {
    return ParadoxLocaclisationParameterManager.resolveScriptedVariable(this)
}

inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getDefinitionData(relax: Boolean = false): T? {
    return ParadoxDataService.get(this, relax)
}

inline fun <reified T : ParadoxDefinitionPresentation> ParadoxScriptDefinitionElement.getDefinitionPresentation(): T? {
    return ParadoxPresentationService.get(this)
}
