package icu.windea.pls.lang

import com.intellij.extapi.psi.*
import com.intellij.injected.editor.*
import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.testFramework.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.io.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.lang.Integer.*

fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

//from official documentation: Never acquire service instances prematurely or store them in fields for later use.

fun getSettings() = service<ParadoxSettings>().state

fun getProfilesSettings() = service<ParadoxProfilesSettings>().state

fun getConfigGroup(gameType: ParadoxGameType?) = getDefaultProject().service<CwtConfigGroupService>().getConfigGroup(gameType)

fun getConfigGroup(project: Project, gameType: ParadoxGameType?) = project.service<CwtConfigGroupService>().getConfigGroup(gameType)

val PathProvider get() = service<ParadoxPathProvider>()

val UrlProvider get() = service<ParadoxUrlProvider>()

fun FileType.isParadoxFileType() = this == ParadoxScriptFileType || this == ParadoxLocalisationFileType

fun Language.isParadoxLanguage() = this.isKindOf(ParadoxScriptLanguage) || this.isKindOf(ParadoxLocalisationLanguage)

fun String.isExactParameterAwareIdentifier(vararg extraChars: Char): Boolean {
    var isParameter = false
    this.forEachFast { c ->
        when {
            c == '$' -> isParameter = !isParameter
            isParameter -> {}
            c.isExactIdentifierChar() || c in extraChars -> {}
            else -> return false
        }
    }
    return true
}

fun String.isParameterized(): Boolean {
    var isEscaped = false
    this.forEachFast { c ->
        when {
            // a_$PARAM$_b - 高级插值语法 A
            c == '$' -> {
                if(!isEscaped) return true
            }
            // a_[[PARAM]b]_c - 高级插值语法 B
            c == '[' -> {
                if(!isEscaped) return true
            }
        }
        if(c == '\\') {
            isEscaped = true
        } else if(isEscaped) {
            isEscaped = false
        }
    }
    return false
}

fun String.isFullParameterized() : Boolean {
    return this.length >= 2 && surroundsWith('$', '$') && this[lastIndex - 1] != '\\'
}

private val regex1 = """(?<!\\)\$.*?\$""".toRegex()
private val regex2 = """(?<!\\)\[\[.*?](.*?)]""".toRegex()

fun String.toRegexWhenIsParameterized(): Regex {
    var s = this
    s = """\Q$s\E"""
    s = s.replace(regex1, """\\E.*\\Q""")
    s = s.replace(regex2) { g ->
        val dv = g.groupValues[1]
        when {
            dv == """\E.*\Q""" -> """\E.*\Q"""
            else -> """\E(?:\Q$dv\E)?\Q"""
        }
    }
    s = s.replace("""\Q\E""", "")
    return s.toRegex(RegexOption.IGNORE_CASE)
}

fun String.isInlineUsage(): Boolean {
    return this.equals(ParadoxInlineScriptHandler.inlineScriptKey, true)
}

/**
 * 基于注解[WithGameType]判断目标对象是否支持当前游戏类型。
 */
fun ParadoxGameType?.supportsByAnnotation(target: Any): Boolean {
    if(this == null) return true
    val targetGameType = target.javaClass.getAnnotation(WithGameType::class.java)?.value
    return targetGameType == null || this in targetGameType
}

inline fun <T : Any> Ref<T?>.mergeValue(value: T?, mergeAction: (T, T) -> T?): Boolean {
    val oldValue = this.get()
    val newValue = value
    if(newValue == null) {
        return true
    } else if(oldValue == null) {
        this.set(newValue)
        return true
    } else {
        val mergedValue = mergeAction(oldValue, newValue)
        this.set(mergedValue)
        return mergedValue != null
    }
}

/**
 * 比较游戏版本。允许通配符，如："3.3.*"
 */
infix fun String.compareGameVersion(otherVersion: String): Int {
    val versionSnippets = this.split('.')
    val otherVersionSnippets = otherVersion.split('.')
    val minSnippetSize = min(versionSnippets.size, otherVersionSnippets.size)
    for(i in 0 until minSnippetSize) {
        val versionSnippet = versionSnippets[i]
        val otherVersionSnippet = otherVersionSnippets[i]
        if(versionSnippet == otherVersionSnippet || versionSnippet == "*" || otherVersion == "*") continue
        return versionSnippet.compareTo(otherVersionSnippet)
    }
    return 0
}

fun String?.orAnonymous() = if(isNullOrEmpty()) PlsConstants.anonymousString else this
fun String?.orUnknown() = if(isNullOrEmpty()) PlsConstants.unknownString else this
fun String?.orUnresolved() = if(isNullOrEmpty()) PlsConstants.unresolvedString else this

tailrec fun selectRootFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFileWindow -> selectRootFile(from.delegate) //for injected PSI
        from is LightVirtualFileBase && from.originalFile != null -> selectRootFile(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameRootFile
        else -> selectRootFile(selectFile(from))
    }
}

tailrec fun selectFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFileWindow -> from.castOrNull() //for injected PSI (and not from.delegate)
        from is LightVirtualFileBase && from.originalFile != null -> selectFile(from.originalFile)
        from is VirtualFile -> from
        from is PsiDirectory -> selectFile(from.virtualFile)
        from is PsiFile -> selectFile(from.originalFile.virtualFile)
        from is StubBasedPsiElementBase<*> -> selectFile(from.containingFileStub?.psi ?: from.containingFile)
        from is PsiElement -> selectFile(from.containingFile)
        from is ParadoxExpressionInfo -> selectFile(from.virtualFile)
        else -> null
    }
}

tailrec fun selectGameType(from: Any?): ParadoxGameType? {
    return when {
        from == null -> null
        from is ParadoxGameType -> from
        from is VirtualFileWindow -> selectGameType(from.delegate) //for injected PSI
        from is LightVirtualFileBase && from.originalFile != null  -> selectGameType(from.originalFile)
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
        from is PsiDirectory -> selectGameType(selectFile(from))
        from is PsiFile -> selectGameType(selectFile(from))
        from is ParadoxScriptScriptedVariable -> runCatchingCancelable { from.greenStub }.getOrNull()?.gameType
            ?: selectGameType(from.containingFile)
        from is ParadoxScriptDefinitionElement -> runCatchingCancelable { from.greenStub }.getOrNull()?.gameType
            ?: selectGameType(from.containingFile)
        from is StubBasedPsiElementBase<*> -> selectGameType(from.containingFile)
        from is PsiElement -> selectGameType(from.parent)
        from is ParadoxExpressionInfo -> selectGameType(from.virtualFile)
        else -> null
    }
}

tailrec fun selectLocale(from: Any?): CwtLocalisationLocaleConfig? {
    return when {
        from == null -> null
        from is CwtLocalisationLocaleConfig -> from
        from is VirtualFile -> from.getUserData(PlsKeys.injectedLocaleConfig)
        from is PsiDirectory -> ParadoxLocaleHandler.getPreferredLocaleConfig()
        from is PsiFile -> ParadoxCoreHandler.getLocaleConfig(from.virtualFile ?: return null, from.project)
        from is ParadoxLocalisationLocale -> from.name.toLocale(from)
        from is ParadoxLocalisationPropertyList -> selectLocale(from.locale)
        from is ParadoxLocalisationProperty -> runCatchingCancelable { from.greenStub }.getOrNull()?.locale?.toLocale(from)
            ?: selectLocale(from.containingFile)
        from is StubBasedPsiElementBase<*> && from.language == ParadoxLocalisationLanguage -> selectLocale(from.containingFile)
        from is PsiElement && from.language == ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> ParadoxLocaleHandler.getPreferredLocaleConfig()
    }
}

private fun String.toLocale(from: PsiElement): CwtLocalisationLocaleConfig? {
    return getConfigGroup(from.project, null).localisationLocalesById.get(this)
}

val Project.paradoxLibrary: ParadoxLibrary
    get() = this.getOrPutUserData(PlsKeys.library) { ParadoxLibrary(this) }

//注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
//如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

val VirtualFile.rootInfo: ParadoxRootInfo?
    get() = ParadoxCoreHandler.getRootInfo(this)
val VirtualFile.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreHandler.getFileInfo(this)
val PsiElement.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreHandler.getFileInfo(this)

val ParadoxScriptDefinitionElement.definitionInfo: ParadoxDefinitionInfo?
    get() = ParadoxDefinitionHandler.getInfo(this)
val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo?
    get() = ParadoxLocalisationHandler.getInfo(this)
val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo?
    get() = ParadoxComplexEnumValueHandler.getInfo(this)

val ParadoxLocalisationPropertyReference.colorConfig: ParadoxTextColorInfo?
    get() {
        //大写或小写字母，不限定位置
        val colorId = this.propertyReferenceParameter?.text?.find { it.isExactLetter() } ?: return null
        return ParadoxTextColorHandler.getInfo(colorId.toString(), project, this)
    }
val ParadoxLocalisationColorfulText.colorConfig: ParadoxTextColorInfo?
    get() {
        val colorId = this.name ?: return null
        return ParadoxTextColorHandler.getInfo(colorId, project, this)
    }