@file:Suppress("unused")

package icu.windea.pls

import com.intellij.codeInsight.documentation.*
import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.documentation.impl.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import java.lang.Integer.*

//region Misc Extensions
fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() = ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

fun getSettings() = service<ParadoxSettings>().state

fun getProfilesSettings() = service<ParadoxProfilesSettings>().state

fun getCwtConfig(project: Project = getTheOnlyOpenOrDefaultProject()) = project.service<CwtConfigProvider>().configGroups

fun getLocale(locale: String): CwtLocalisationLocaleConfig {
    val primaryLocale = locale
    if(primaryLocale.isNotEmpty() && primaryLocale != "auto") {
        val locales = getCwtConfig().core.localisationLocales
        val usedLocale = locales.get(primaryLocale)
        if(usedLocale != null) return usedLocale
    }
    //基于OS得到对应的语言区域，或者使用英文
    val userLanguage = System.getProperty("user.language") ?: "en"
    val localesByCode = getCwtConfig().core.localisationLocalesByCode
    return localesByCode.get(userLanguage) ?: localesByCode.get("en") ?: throw IllegalStateException()
}

fun preferredParadoxLocale(): CwtLocalisationLocaleConfig {
    val primaryLocale = getSettings().preferredLocale.orEmpty()
    return getLocale(primaryLocale)
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

fun FileType.isParadoxFileType() = this == ParadoxScriptFileType || this == ParadoxLocalisationFileType

fun Language.isParadoxLanguage() = this.isKindOf(ParadoxScriptLanguage) || this.isKindOf(ParadoxLocalisationLanguage)

fun PsiReference.canResolveScriptedVariable(): Boolean {
    return when(this) {
        is ParadoxScriptedVariablePsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveDefinition(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.Definition
        }
        is ParadoxTemplateSnippetExpressionReference -> {
            val configExpression = this.configExpression
            configExpression.type == CwtDataType.Definition
        }
        is ParadoxDataExpressionNode.Reference -> {
            this.linkConfigs.any { linkConfig ->
                val configExpression = linkConfig.expression ?: return@any false
                configExpression.type == CwtDataType.Definition
            }
        }
        is ParadoxLocalisationCommandFieldPsiReference -> true //<scripted_loc>
        else -> false
    }
}

fun PsiReference.canResolveLocalisation(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.Localisation || configExpression.type == CwtDataType.InlineLocalisation
        }
        is ParadoxLocalisationPropertyPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveParameter(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.Parameter
        }
        is ParadoxParameterPsiReference -> true
        is ParadoxConditionParameterPsiReference -> true
        is ParadoxScriptValueArgumentExpressionNode.Reference -> true
        is ParadoxLocalisationPropertyPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveValueSetValue(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type.isValueSetValueType()
        }
        is ParadoxTemplateSnippetExpressionReference -> {
            val configExpression = this.configExpression
            configExpression.type.isValueSetValueType()
        }
        is ParadoxDataExpressionNode.Reference -> {
            this.linkConfigs.any { linkConfig ->
                val configExpression = linkConfig.expression ?: return@any false
                configExpression.type.isValueSetValueType()
            }
        }
        is ParadoxValueSetValueExpressionNode.Reference -> true
        is ParadoxLocalisationCommandScopePsiReference -> true //value[event_target], value[global_event_target]
        is ParadoxLocalisationCommandFieldPsiReference -> true //value[variable]
        else -> false
    }
}

fun PsiReference.canResolveComplexEnumValue(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.EnumValue
        }
        is ParadoxTemplateSnippetExpressionReference -> {
            val configExpression = this.configExpression
            configExpression.type == CwtDataType.EnumValue
        }
        is ParadoxDataExpressionNode.Reference -> {
            this.linkConfigs.any { linkConfig ->
                val configExpression = linkConfig.expression ?: return@any false
                configExpression.type == CwtDataType.EnumValue
            }
        }
        is ParadoxComplexEnumValuePsiReference -> true
        else -> false
    }
}
//endregion

//region Select Extensions
tailrec fun selectRootFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
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
        from is ParadoxExpressionInfo -> selectFile(from.file)
        else -> null
    }
}

tailrec fun selectGameType(from: Any?): ParadoxGameType? {
    return when {
        from == null -> null
        from is ParadoxGameType -> from
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
        from is PsiDirectory -> from.fileInfo?.rootInfo?.gameType
        from is PsiFile -> from.fileInfo?.rootInfo?.gameType
        from is ParadoxScriptScriptedVariable -> runCatching { from.stub }.getOrNull()?.gameType
            ?: selectGameType(from.containingFile)
        from is ParadoxScriptDefinitionElement -> runCatching { from.getStub() }.getOrNull()?.gameType
            ?: selectGameType(from.containingFile)
        from is StubBasedPsiElementBase<*> -> selectGameType(from.containingFile)
        from is PsiElement -> selectGameType(from.parent)
        from is ParadoxExpressionInfo -> selectGameType(from.file)
        else -> null
    }
}

tailrec fun selectLocale(from: Any?): CwtLocalisationLocaleConfig? {
    return when {
        from == null -> null
        from is CwtLocalisationLocaleConfig -> from
        from is VirtualFile -> from.getUserData(PlsKeys.injectedLocaleConfigKey)
        from is PsiFile -> from.virtualFile?.getUserData(PlsKeys.injectedLocaleConfigKey)
            ?: selectLocaleFromPsiFile(from)
        from is ParadoxLocalisationLocale -> from.name.toLocale(from)
        from is ParadoxLocalisationPropertyList -> selectLocale(from.locale)
        from is ParadoxLocalisationProperty -> runCatching { from.stub }.getOrNull()?.locale?.toLocale(from)
            ?: selectLocale(from.containingFile)
        from is StubBasedPsiElementBase<*> && from.language == ParadoxLocalisationLanguage -> selectLocale(from.containingFile)
        from is PsiElement && from.language == ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> preferredParadoxLocale()
    }
}

private fun selectLocaleFromPsiFile(from: PsiFile): CwtLocalisationLocaleConfig? {
    //这里改为使用索引以优化性能（尽可能地避免访问PSI）
    val indexKey = ParadoxFileLocaleIndex.NAME
    val virtualFile = from.virtualFile ?: return null
    val project = from.project
    val localeId = FileBasedIndex.getInstance().getFileData(indexKey, virtualFile, project).keys.singleOrNull() ?: return null
    return getCwtConfig(project).core.localisationLocales.get(localeId)
}

private fun String.toLocale(from: PsiElement): CwtLocalisationLocaleConfig? {
    return getCwtConfig(from.project).core.localisationLocales.get(this)
}
//endregion

//region PsiElement Extensions
val Project.paradoxLibrary: ParadoxLibrary
    get() {
        return this.getOrPutUserData(PlsKeys.libraryKey) {
            ParadoxLibrary(this)
        }
    }

//注意：不要更改直接调用CachedValuesManager.getCachedValue(...)的那个顶级方法（静态方法）的方法声明，IDE内部会进行检查
//如果不同的输入参数得到了相同的输出值，或者相同的输入参数得到了不同的输出值，IDE都会报错

val VirtualFile.rootInfo: ParadoxRootInfo?
    get() = ParadoxCoreHandler.getRootInfo(this)
val VirtualFile.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreHandler.getFileInfo(this, refresh = false)
val PsiElement.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreHandler.getFileInfo(this, refresh = false)

val ParadoxScriptDefinitionElement.definitionInfo: ParadoxDefinitionInfo?
    get() = ParadoxDefinitionHandler.getInfo(this)

val ParadoxScriptMemberElement.definitionMemberInfo: ParadoxDefinitionMemberInfo?
    get() = ParadoxDefinitionMemberHandler.getInfo(this)

val ParadoxLocalisationProperty.localisationInfo: ParadoxLocalisationInfo?
    get() = ParadoxLocalisationHandler.getInfo(this)

val ParadoxScriptStringExpressionElement.complexEnumValueInfo: ParadoxComplexEnumValueInfo?
    get() = ParadoxComplexEnumValueHandler.getInfo(this)

val PsiElement.localeConfig: CwtLocalisationLocaleConfig?
    get() = selectLocale(this)

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

fun ParadoxScriptValue.isNullLike(): Boolean {
    return when {
        this is ParadoxScriptBlock -> this.isEmpty
        this is ParadoxScriptString -> this.textMatches("")
        this is ParadoxScriptInt -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
        this is ParadoxScriptFloat -> this.text.toIntOrNull() == 0 //兼容0.0和0.00这样的情况
        this is ParadoxScriptBoolean -> this.textMatches("no")
        else -> false
    }
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
    //如果context不为null切链接无法被解析，则显示未解析的链接
    val linkPrefix = CwtConfigLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix$shortLink".escapeXml()
    val finalLinkText = linkText.escapeXml()
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendDefinitionLink(gameType: ParadoxGameType, name: String, typeExpression: String, context: PsiElement? = null, label: String = name.escapeXml()): StringBuilder {
    //如果context不为null切链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxDefinitionLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.linkToken}$typeExpression/$name".escapeXml()
    val finalLinkText = label
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendLocalisationLink(gameType: ParadoxGameType, name: String, context: PsiElement? = null, label: String = name.escapeXml()): StringBuilder {
    //如果context不为null切链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxLocalisationLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.linkToken}$name".escapeXml()
    val finalLinkText = label
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun StringBuilder.appendFilePathLink(gameType: ParadoxGameType, filePath: String, linkText: String, context: PsiElement? = null, label: String = linkText.escapeXml()): StringBuilder {
    //如果context不为null切链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxFilePathLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.linkToken}$filePath".escapeXml()
    val finalLinkText = label
    if(context != null && DocumentationElementLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
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

fun StringBuilder.appendFileInfoHeader(fileInfo: ParadoxFileInfo?): StringBuilder {
    if(fileInfo != null) {
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
    }
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