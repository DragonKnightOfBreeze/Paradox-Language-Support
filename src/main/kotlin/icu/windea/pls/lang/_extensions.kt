@file:Suppress("unused")

package icu.windea.pls.lang

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.LightVirtualFileBase
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.config.configGroup.localisationLocalesById
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.ep.data.ParadoxDefinitionData
import icu.windea.pls.ep.data.ParadoxDefinitionDataProvider
import icu.windea.pls.ep.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.ep.presentation.ParadoxDefinitionPresentationProvider
import icu.windea.pls.lang.psi.mock.CwtConfigMockPsiElement
import icu.windea.pls.lang.psi.mock.ParadoxMockPsiElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxCoreManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.indexInfo.CwtConfigIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

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

fun String.isInlineUsage(): Boolean {
    return this.equals(ParadoxInlineScriptManager.inlineScriptKey, true)
}

tailrec fun selectRootFile(from: Any?): VirtualFile? {
    if (from == null) return null
    return when {
        from is VirtualFileWindow -> selectRootFile(from.delegate) // for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectRootFile(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile
        else -> selectRootFile(selectFile(from))
    }
}

tailrec fun selectFile(from: Any?): VirtualFile? {
    if (from == null) return null
    return when {
        from is VirtualFileWindow -> from.castOrNull() // for injected PSI (result is from, not from.delegate)
        from is LightVirtualFileBase && from.originalFile != null -> selectFile(from.originalFile)
        from is VirtualFile -> from
        from is PsiDirectory -> selectFile(from.virtualFile)
        from is PsiFile -> selectFile(from.originalFile.virtualFile)
        from is PsiElement -> selectFile(from.containingFile)
        from is ParadoxIndexInfo -> selectFile(from.virtualFile)
        else -> null
    }
}

tailrec fun selectGameType(from: Any?): ParadoxGameType? {
    if (from == null) return null
    if (from is ParadoxGameType) return from
    if (from is VirtualFile) PlsVfsManager.getInjectedGameTypeForTestDataFile(from)
    if (from is UserDataHolder) from.getUserData(PlsKeys.injectedGameType)?.let { return it }
    return when {
        from is VirtualFileWindow -> selectGameType(from.delegate) // for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectGameType(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
        from is PsiDirectory -> selectGameType(selectFile(from))
        from is PsiFile -> selectGameType(selectFile(from))
        from is CwtConfigMockPsiElement -> from.gameType
        from is ParadoxMockPsiElement -> from.gameType
        from is StubBasedPsiElementBase<*> -> selectGameType(getStubToSelectGameType(from) ?: from.containingFile)
        from is ParadoxStub<*> -> from.gameType
        from is PsiElement -> selectGameType(from.parent)
        from is ParadoxIndexInfo -> from.gameType
        from is CwtConfigIndexInfo -> from.gameType
        else -> null
    }
}

private fun getStubToSelectGameType(from: StubBasedPsiElementBase<*>): ParadoxStub<*>? {
    return runReadAction { from.greenStub?.castOrNull<ParadoxStub<*>>() }
}

tailrec fun selectLocale(from: Any?): CwtLocaleConfig? {
    if (from == null) return null
    if (from is CwtLocaleConfig) return from
    if (from is UserDataHolder) from.getUserData(PlsKeys.injectedLocaleConfig)?.let { return it }
    return when {
        from is PsiDirectory -> ParadoxLocaleManager.getPreferredLocaleConfig()
        from is PsiFile -> ParadoxCoreManager.getLocaleConfig(from.virtualFile ?: return null, from.project)
        from is ParadoxLocalisationLocale -> toLocale(from.name, from)
        from is StubBasedPsiElementBase<*> -> selectLocale(getStubToSelectLocale(from) ?: from.parent)
        from is ParadoxLocaleAwareStub<*> -> toLocale(from.locale, from.containingFileStub?.psi)
        from is PsiElement && from.language is ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> ParadoxLocaleManager.getPreferredLocaleConfig()
    }
}

private fun getStubToSelectLocale(from: StubBasedPsiElementBase<*>): ParadoxLocaleAwareStub<*>? {
    return runReadAction { from.greenStub?.castOrNull<ParadoxLocaleAwareStub<*>>() }
}

private fun toLocale(localeId: String?, from: PsiElement?): CwtLocaleConfig? {
    if (localeId == null || from == null) return null
    return PlsFacade.getConfigGroup(from.project, null).localisationLocalesById.get(localeId)
}

/**
 * 基于注解[WithGameType]判断目标对象是否支持当前游戏类型。
 */
fun ParadoxGameType?.supportsByAnnotation(target: Any): Boolean {
    if (this == null) return true
    val targetGameType = target.javaClass.getAnnotation(WithGameType::class.java)?.value
    return targetGameType == null || this in targetGameType
}

val Project.paradoxLibrary: ParadoxLibrary
    get() = this.getOrPutUserData(PlsKeys.library) { ParadoxLibrary(this) }

// 注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
// 如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

val VirtualFile.rootInfo: ParadoxRootInfo?
    get() = ParadoxCoreManager.getRootInfo(this)

val VirtualFile.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreManager.getFileInfo(this)

val PsiElement.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreManager.getFileInfo(this)

val ParadoxScriptDefinitionElement.definitionInfo: ParadoxDefinitionInfo?
    get() = ParadoxDefinitionManager.getInfo(this)

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo?
    get() = ParadoxLocalisationManager.getInfo(this)

val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueIndexInfo?
    get() = ParadoxComplexEnumValueManager.getInfo(this)

fun ParadoxLocalisationParameter.resolveLocalisation(): ParadoxLocalisationProperty? {
    return reference?.castOrNull<ParadoxLocalisationParameterPsiReference>()?.resolveLocalisation()
}

fun ParadoxLocalisationParameter.resolveScriptedVariable(): ParadoxScriptScriptedVariable? {
    return scriptedVariableReference?.reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
}

inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getData(): T? {
    return ParadoxDefinitionDataProvider.getData(T::class.java, this)
}

inline fun <reified T : ParadoxDefinitionPresentation> ParadoxScriptDefinitionElement.getPresentation(): T? {
    return ParadoxDefinitionPresentationProvider.getPresentation(T::class.java, this)
}

inline fun <T> withState(state: ThreadLocal<Boolean>, action: () -> T): T {
    try {
        state.set(true)
        return action()
    } finally {
        state.remove()
    }
}
