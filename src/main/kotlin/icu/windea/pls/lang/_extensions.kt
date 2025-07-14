package icu.windea.pls.lang

import com.intellij.extapi.psi.*
import com.intellij.injected.editor.*
import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.testFramework.*
import com.intellij.util.text.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.codeInsight.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*
import java.lang.Integer.*

fun Char.isIdentifierChar(): Boolean {
    return StringUtil.isJavaIdentifierPart(this)
}

fun String.isIdentifier(vararg extraChars: Char): Boolean {
    return this.all { c -> c.isIdentifierChar() || c in extraChars }
}

fun String.isParameterAwareIdentifier(vararg extraChars: Char): Boolean {
    //比较复杂的实现逻辑
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

fun String?.orAnonymous() = if (isNullOrEmpty()) PlsStringConstants.anonymous else this

fun String?.orUnknown() = if (isNullOrEmpty()) PlsStringConstants.unknown else this

fun String?.orUnresolved() = if (isNullOrEmpty()) PlsStringConstants.unresolved else this

tailrec fun selectRootFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFileWindow -> selectRootFile(from.delegate) //for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectRootFile(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile
        else -> selectRootFile(selectFile(from))
    }
}

tailrec fun selectFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFileWindow -> from.castOrNull() //for injected PSI (result is from, not from.delegate)
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
    return when {
        from == null -> null
        from is ParadoxGameType -> from
        from is VirtualFileWindow -> selectGameType(from.delegate) //for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectGameType(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
        from is PsiDirectory -> selectGameType(selectFile(from))
        from is PsiFile -> selectGameType(selectFile(from))
        from is ParadoxScriptScriptedVariable -> {
            runReadAction { from.greenStub }?.gameType?.let { return it }
            selectGameType(from.containingFile)
        }
        from is ParadoxScriptProperty -> {
            runReadAction { from.greenStub }?.gameType?.let { return it }
            selectGameType(from.containingFile)
        }
        from is StubBasedPsiElementBase<*> -> selectGameType(from.containingFile)
        from is PsiElement -> selectGameType(from.parent)
        from is ParadoxIndexInfo -> selectGameType(from.virtualFile)
        else -> null
    }
}

tailrec fun selectLocale(from: Any?): CwtLocaleConfig? {
    return when {
        from == null -> null
        from is CwtLocaleConfig -> from
        from is VirtualFile -> from.getUserData(PlsKeys.injectedLocaleConfig)
        from is PsiDirectory -> ParadoxLocaleManager.getPreferredLocaleConfig()
        from is PsiFile -> ParadoxCoreManager.getLocaleConfig(from.virtualFile ?: return null, from.project)
        from is ParadoxLocalisationLocale -> from.name.toLocale(from)
        from is ParadoxLocalisationPropertyList -> selectLocale(from.locale)
        from is ParadoxLocalisationProperty -> {
            runReadAction { from.greenStub }?.locale?.toLocale(from)?.let { return it }
            selectLocale(from.containingFile)
        }
        from is StubBasedPsiElementBase<*> && from.language is ParadoxLocalisationLanguage -> selectLocale(from.containingFile)
        from is PsiElement && from.language is ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> ParadoxLocaleManager.getPreferredLocaleConfig()
    }
}

private fun String.toLocale(from: PsiElement): CwtLocaleConfig? {
    return PlsFacade.getConfigGroup(from.project, null).localisationLocalesById.get(this)
}

/**
 * 基于注解[WithGameType]判断目标对象是否支持当前游戏类型。
 */
fun ParadoxGameType?.supportsByAnnotation(target: Any): Boolean {
    if (this == null) return true
    val targetGameType = target.javaClass.getAnnotation(WithGameType::class.java)?.value
    return targetGameType == null || this in targetGameType
}

/**
 * 比较游戏版本。允许通配符，如："3.3.*"
 */
infix fun String.compareGameVersion(otherVersion: String): Int {
    val versionSnippets = this.split('.')
    val otherVersionSnippets = otherVersion.split('.')
    val minSnippetSize = min(versionSnippets.size, otherVersionSnippets.size)
    for (i in 0 until minSnippetSize) {
        val versionSnippet = versionSnippets[i]
        val otherVersionSnippet = otherVersionSnippets[i]
        if (versionSnippet == otherVersionSnippet || versionSnippet == "*" || otherVersion == "*") continue
        return versionSnippet.compareTo(otherVersionSnippet)
    }
    return 0
}

val Project.paradoxLibrary: ParadoxLibrary
    get() = this.getOrPutUserData(PlsKeys.library) { ParadoxLibrary(this) }

//注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
//如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

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

/**
 * 获取定义的指定类型的数据。
 */
inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getData(): T? {
    return ParadoxDefinitionDataProvider.getData(T::class.java, this)
}

fun createNotification(content: String, notificationType: NotificationType): Notification {
    return NotificationGroupManager.getInstance().getNotificationGroup("pls")
        .createNotification(content, notificationType)
}

fun createNotification(title: String, content: String, notificationType: NotificationType): Notification {
    return NotificationGroupManager.getInstance().getNotificationGroup("pls")
        .createNotification(title, content, notificationType)
}

inline fun <T> withState(state: ThreadLocal<Boolean>, action: () -> T): T {
    try {
        state.set(true)
        return action()
    } finally {
        state.remove()
    }
}
