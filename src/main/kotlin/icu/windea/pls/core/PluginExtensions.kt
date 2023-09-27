package icu.windea.pls.core

import com.intellij.codeInsight.documentation.*
import com.intellij.extapi.psi.*
import com.intellij.injected.editor.*
import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.documentation.impl.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.lang.Integer.*

//region Stdlib Extensions
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
//endregion

//region Common Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

//from official documentation: Never acquire service instances prematurely or store them in fields for later use.

fun getSettings() = service<ParadoxSettings>().state

fun getProfilesSettings() = service<ParadoxProfilesSettings>().state

fun getConfigGroups() = getDefaultProject().service<CwtConfigProvider>().configGroups
fun getConfigGroups(project: Project) = project.service<CwtConfigProvider>().configGroups

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
            c == '\\' -> {
                isEscaped = true
                return@forEachFast
            }
            // a_$PARAM$_b - 高级插值语法 A
            c == '$' -> {
                if(!isEscaped) return true
            }
            // a_[[PARAM]b]_c - 高级插值语法 B
            c == '[' -> {
                if(!isEscaped) return true
            }
        }
        if(isEscaped) isEscaped = false
    }
    return false
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
//endregion

//region Select Extensions
tailrec fun selectRootFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFileWindow -> selectRootFile(from.delegate) //for injected PSI
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameRootFile
        else -> selectRootFile(selectFile(from))
    }
}

tailrec fun selectFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFile -> from
        from is PsiDirectory -> from.virtualFile
        from is PsiFile -> from.originalFile.virtualFile
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
        from is PsiDirectory -> ParadoxLocaleHandler.getPreferredLocale()
        from is PsiFile -> ParadoxCoreHandler.getLocaleConfig(from.virtualFile ?: return null, from.project)
        from is ParadoxLocalisationLocale -> from.name.toLocale(from)
        from is ParadoxLocalisationPropertyList -> selectLocale(from.locale)
        from is ParadoxLocalisationProperty -> runCatchingCancelable { from.greenStub }.getOrNull()?.locale?.toLocale(from)
            ?: selectLocale(from.containingFile)
        from is StubBasedPsiElementBase<*> && from.language == ParadoxLocalisationLanguage -> selectLocale(from.containingFile)
        from is PsiElement && from.language == ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> ParadoxLocaleHandler.getPreferredLocale()
    }
}

private fun String.toLocale(from: PsiElement): CwtLocalisationLocaleConfig? {
    return getConfigGroups(from.project).core.localisationLocalesById.get(this)
}
//endregion

//region PsiElement Extensions
val Project.paradoxLibrary: ParadoxLibrary
    get() {
        return this.getOrPutUserData(PlsKeys.library) {
            ParadoxLibrary(this)
        }
    }

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
//endregion

//region Documentation Extensions
private const val cwtLinkPrefix = "#cwt/"
private const val definitionLinkPrefix = "#definition/"
private const val localisationLinkPrefix = "#localisation/"
private const val filePathLinkPrefix = "#path/"

fun StringBuilder.appendIf(condition: Boolean, text: String): StringBuilder {
    if(condition) append(text)
    return this
}

fun StringBuilder.appendExternalLinkIcon(): StringBuilder {
    append("<icon src='ide/external_link_arrow.svg'/>")
    return this
}

fun StringBuilder.appendUnresolvedLink(label: String): StringBuilder {
    append(label.escapeXml()) //直接显示对应的标签文本
    return this
}

fun StringBuilder.appendLink(refText: String, label: String): StringBuilder {
    //不自动转义link的label
    append("<a href=\"").append(refText).append("\">").append(label).append("</a>")
    return this
}

fun StringBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = true): StringBuilder {
    DocumentationManagerUtil.createHyperlink(this, refText, label, plainLink)
    return this
}

fun StringBuilder.appendCwtLink(shortLink: String, linkText: String, context: PsiElement? = null): StringBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = CwtConfigLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix$shortLink".escapeXml()
    val finalLinkText = linkText.escapeXml()
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendDefinitionLink(gameType: ParadoxGameType, name: String, typeExpression: String, context: PsiElement? = null, label: String = name.escapeXml()): StringBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxDefinitionLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.linkToken}$typeExpression/$name".escapeXml()
    val finalLinkText = label
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendLocalisationLink(gameType: ParadoxGameType, name: String, context: PsiElement? = null, label: String = name.escapeXml()): StringBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxLocalisationLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.linkToken}$name".escapeXml()
    val finalLinkText = label
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendFilePathLink(gameType: ParadoxGameType, filePath: String, linkText: String, context: PsiElement? = null, label: String = linkText.escapeXml()): StringBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxFilePathLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.linkToken}$filePath".escapeXml()
    val finalLinkText = label
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendModifierLink(name: String, label: String = name.escapeXml()) : StringBuilder {
    val linkPrefix = ParadoxFilePathLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix$name".escapeXml()
    val finalLinkText = label
    return appendPsiLink(finalLink, finalLinkText)
}

/**
 * @param local 输入的[url]是否是本地绝对路径。
 */
fun StringBuilder.appendImgTag(url: String, local: Boolean = true): StringBuilder {
    append("<img src=\"")
    if(local) append(url.toFileUrl()) else append(url)
    append("\" />")
    return this
}

fun StringBuilder.appendImgTag(url: String, width: Int, height: Int, local: Boolean = true): StringBuilder {
    append("<img src=\"")
    if(local) append(url.toFileUrl()) else append(url)
    //这里不能使用style="..."
    append("\" width=\"").append(width).append("\" height=\"").append(height)
    append(" vspace=\"0\" hspace=\"0\"")
    append("\"/>")
    return this
}

fun StringBuilder.appendFileInfoHeader(element: PsiElement): StringBuilder {
    val file = selectFile(element) ?: return this
    if(ParadoxFileManager.isInjectedFile(file)) return this //ignored for injected PSI
    val fileInfo = file.fileInfo ?: return this
    
    val rootInfo = fileInfo.rootInfo
    append("<span>")
    //描述符信息（模组名、版本等）
    append("[")
    append(rootInfo.qualifiedName.escapeXml())
    append("]")
    grayed {
        val remoteFileId = (rootInfo as? ParadoxModRootInfo)?.descriptorInfo?.remoteFileId
        //remoteFileId（暂不显示）
        //if(remoteFileId != null) {
        //	append(" ").append(PlsBundle.message("name.core.remoteFileId")).append(": ").append(remoteFileId).append(" )
        //}
        //相关链接
        //通过这种方式获取需要的url，使用rootPath而非gameRootPath
        val rootUri = fileInfo.rootInfo.rootPath.toUri().toString()
        append(" ")
        appendLink(rootUri, PlsBundle.message("text.localLinkLabel"))
        if(remoteFileId != null) {
            append(" | ")
            appendLink(getSteamWorkshopLinkOnSteam(remoteFileId), PlsBundle.message("text.steamLinkLabel"))
            appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
            append(" | ")
            appendLink(getSteamWorkshopLink(remoteFileId), PlsBundle.message("text.steamWebsiteLinkLabel")) //自带外部链接图标
        }
    }
    append("</span>")
    appendBr()
    //文件信息（相对于游戏或模组根目录的路径）
    append("[").append(fileInfo.path).append("]")
    appendBr()
    return this
}

fun StringBuilder.appendBr(): StringBuilder {
    return append("<br>")
}

fun StringBuilder.appendIndent(): StringBuilder {
    return append("&nbsp;&nbsp;&nbsp;&nbsp;")
}

fun getDocumentation(documentationLines: List<String>?, html: Boolean): String? {
    if(documentationLines.isNullOrEmpty()) return null
    return buildString {
        var isLineBreak = false
        for(line in documentationLines) {
            if(!isLineBreak) {
                isLineBreak = true
            } else {
                append("<br>")
            }
            if(line.endsWith('\\')) {
                isLineBreak = false
            }
            val l = line.trimEnd('\\')
            if(html) append(l) else append(l.escapeXml())
        }
    }
}
//endregion