package icu.windea.pls.lang

import com.intellij.extapi.psi.*
import com.intellij.injected.editor.*
import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.testFramework.*
import com.intellij.util.text.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.io.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
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

fun Char.isIdentifierChar(): Boolean {
    return StringUtil.isJavaIdentifierPart(this)
}

fun String.isIdentifier(vararg extraChars: Char): Boolean {
    return this.all { c -> c.isIdentifierChar() || c in extraChars }
}

fun String.isParameterAwareIdentifier(vararg extraChars: Char): Boolean {
    //比较复杂的实现逻辑
    val fullRange = TextRange.create(0, this.length)
    val parameterRanges = this.getParameterRanges()
    val ranges = TextRangeUtil.excludeRanges(fullRange, parameterRanges)
    ranges.forEach f@{ range ->
        for(i in range.startOffset until range.endOffset) {
            if(i >= this.length) continue
            val c = this[i]
            if(c.isIdentifierChar() || c in extraChars) continue
            return false
        }
    }
    return true
}

fun String.isParameterized(): Boolean {
    //快速判断，不检测带参数后的语法是否合法
    if(this.length < 2) return false
    // a_$PARAM$_b - 高级插值语法 A
    if(this.indexOf('$').let { c -> c != -1 && !isEscapedCharAt(c) }) return true
    // a_[[PARAM]b]_c - 高级插值语法 B
    if(this.indexOf('[').let { c -> c != -1 && !isEscapedCharAt(c) }) return true
    return false
}

fun String.isFullParameterized(): Boolean {
    //快速判断，不检测带参数后的语法是否合法
    if(this.length < 2) return false
    // $PARAM$ - 仅限 高级插值语法 A
    if(!this.startsWith('$')) return false
    if(this.indexOf('$', 1).let { c -> c != lastIndex || isEscapedCharAt(c) }) return false
    return true
}

fun String.getParameterRanges(): List<TextRange> {
    //比较复杂的实现逻辑
    val ranges = mutableListOf<TextRange>()
    // a_$PARAM$_b - 高级插值语法 A - 深度计数
    var depth1 = 0
    // a_[[PARAM]b]_c - 高级插值语法 B - 深度计数
    var depth2 = 0
    var startIndex = -1
    var endIndex = -1
    for((i, c) in this.withIndex()) {
        if(c == '$' && !isEscapedCharAt(i)) {
            if(depth2 > 0) continue
            if(depth1 == 0) {
                startIndex = i
                endIndex = -1
                depth1++
            } else {
                endIndex = i
                ranges += TextRange.create(startIndex, endIndex + 1)
                depth1--
                
            }
        } else if(c == '[' && !isEscapedCharAt(i)) {
            if(depth1 > 0) continue
            if(depth2 == 0) {
                startIndex = i
                endIndex = -1
            }
            depth2++
        } else if(c == ']' && !isEscapedCharAt(i)) {
            if(depth1 > 0) continue
            if(depth2 <= 0) continue
            depth2--
            if(depth2 == 0) {
                endIndex = i
                ranges += TextRange.create(startIndex, endIndex + 1)
            }
        }
    }
    if(startIndex != -1 && endIndex == -1) {
        ranges += TextRange.create(startIndex, this.length)
    }
    return ranges
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
    return this.equals(ParadoxInlineScriptManager.inlineScriptKey, true)
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
        from is VirtualFileWindow -> from.castOrNull() //for injected PSI (result is from, not from.delegate)
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
        from is LightVirtualFileBase && from.originalFile != null -> selectGameType(from.originalFile)
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
        from is PsiDirectory -> ParadoxLocaleManager.getPreferredLocaleConfig()
        from is PsiFile -> ParadoxCoreManager.getLocaleConfig(from.virtualFile ?: return null, from.project)
        from is ParadoxLocalisationLocale -> from.name.toLocale(from)
        from is ParadoxLocalisationPropertyList -> selectLocale(from.locale)
        from is ParadoxLocalisationProperty -> runCatchingCancelable { from.greenStub }.getOrNull()?.locale?.toLocale(from)
            ?: selectLocale(from.containingFile)
        from is StubBasedPsiElementBase<*> && from.language == ParadoxLocalisationLanguage -> selectLocale(from.containingFile)
        from is PsiElement && from.language == ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> ParadoxLocaleManager.getPreferredLocaleConfig()
    }
}

private fun String.toLocale(from: PsiElement): CwtLocalisationLocaleConfig? {
    return getConfigGroup(from.project, null).localisationLocalesById.get(this)
}

/**
 * 基于注解[WithGameType]判断目标对象是否支持当前游戏类型。
 */
fun ParadoxGameType?.supportsByAnnotation(target: Any): Boolean {
    if(this == null) return true
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
    for(i in 0 until minSnippetSize) {
        val versionSnippet = versionSnippets[i]
        val otherVersionSnippet = otherVersionSnippets[i]
        if(versionSnippet == otherVersionSnippet || versionSnippet == "*" || otherVersion == "*") continue
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

val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo?
    get() = ParadoxComplexEnumValueManager.getInfo(this)

val ParadoxLocalisationPropertyReference.colorConfig: ParadoxTextColorInfo?
    get() = ParadoxTextColorManager.getInfo(this)

val ParadoxLocalisationColorfulText.colorConfig: ParadoxTextColorInfo?
    get() = ParadoxTextColorManager.getInfo(this)

/**
 * 获取定义的指定类型的数据。
 */
inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getData(): T? {
    return ParadoxDefinitionDataProvider.getData(T::class.java, this)
}
