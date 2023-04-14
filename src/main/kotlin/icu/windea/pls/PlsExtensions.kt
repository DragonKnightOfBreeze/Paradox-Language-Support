@file:Suppress("unused")

package icu.windea.pls

import com.intellij.codeInsight.documentation.*
import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
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
        is ParadoxScriptExpressionPsiReference -> true
        is ParadoxDataExpressionNode.Reference -> true
        is ParadoxTemplateExpressionNode.Reference -> true
        is ParadoxLocalisationCommandFieldPsiReference -> true //<scripted_loc>
        else -> false
    }
}

fun PsiReference.canResolveLocalisation(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> true
        is ParadoxLocalisationPropertyPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveParameter(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> this.isKey
        is ParadoxParameterPsiReference -> true
        is ParadoxArgumentPsiReference -> true
        is ParadoxScriptValueParameterExpressionNode.Reference -> true
        is ParadoxLocalisationPropertyPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveValueSetValue(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> true
        is ParadoxValueSetValueExpressionNode.Reference -> true
        is ParadoxDataExpressionNode.Reference -> true
        is ParadoxTemplateExpressionNode.Reference -> true
        is ParadoxLocalisationCommandScopePsiReference -> true //value[event_target], value[global_event_target]
        is ParadoxLocalisationCommandFieldPsiReference -> true //value[variable]
        else -> false
    }
}

fun PsiReference.canResolveComplexEnumValue(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> true
        is ParadoxDataExpressionNode.Reference -> true
        is ParadoxTemplateExpressionNode.Reference -> true
        is ParadoxComplexEnumValuePsiReference -> true
        else -> false
    }
}
//endregion

//region Debug Extensions
val isDebug = System.getProperty("pls.is.debug").toBoolean()

inline fun <T> withMeasureMillis(prefix: String, enable: Boolean = true, action: () -> T): T {
    if(!isDebug || !enable) return action()
    val start = System.currentTimeMillis()
    val result = action()
    val end = System.currentTimeMillis()
    val millis = end - start
    println("$prefix $millis")
    return result
}
//endregion

//region Select Extensions
tailrec fun selectRootFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFile -> from.fileInfo?.rootInfo?.gameRootFile
        from is PsiDirectory -> from.fileInfo?.rootInfo?.gameRootFile
        from is PsiFile -> from.originalFile.fileInfo?.rootInfo?.gameRootFile
        from is PsiElement -> selectRootFile(from.containingFile)
        from is ParadoxScriptExpressionInfo -> selectRootFile(from.file)
        else -> null
    }
}

fun selectFile(from: Any?): VirtualFile? {
    return when {
        from == null -> null
        from is VirtualFile -> from
        from is PsiDirectory -> from.virtualFile
        from is PsiFile -> from.originalFile.virtualFile
        from is PsiElement -> selectFile(from.containingFile)
        from is ParadoxScriptExpressionInfo -> selectFile(from.file)
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
            ?: selectGameType(from.containingFile) //直接转到containingFile，避免不必要的文件解析
        from is ParadoxScriptDefinitionElement -> runCatching { from.getStub() }.getOrNull()?.gameType
            ?: from.definitionInfo?.gameType
            ?: selectGameType(from.containingFile) //直接转到containingFile，避免不必要的文件解析
        from is StubBasedPsiElementBase<*> -> selectGameType(from.containingFile) //直接转到containingFile，避免不必要的文件解析
        from is PsiElement -> selectGameType(from.parent)
        from is ParadoxScriptExpressionInfo -> selectGameType(from.file)
        else -> null
    }
}

tailrec fun selectLocale(from: Any?): CwtLocalisationLocaleConfig? {
    return when {
        from == null -> null
        from is CwtLocalisationLocaleConfig -> from
        from is VirtualFile -> from.getUserData(PlsKeys.injectedLocaleConfigKey)
        from is ParadoxLocalisationFile -> from.virtualFile?.getUserData(PlsKeys.injectedLocaleConfigKey)
            ?: selectLocale(from.propertyLists.singleOrNull()?.locale) //尝试获取文件中声明的唯一的语言区域
        from is ParadoxLocalisationLocale -> from.name
            .let { getCwtConfig(from.project).core.localisationLocales.get(it) } //这里需要传入project
        from is ParadoxLocalisationProperty -> runCatching { from.stub }.getOrNull()?.locale
            ?.let { getCwtConfig().core.localisationLocales.get(it) } //这里不需要传入project
            ?: selectLocale(from.containingFile) //直接转到containingFile，避免不必要的文件解析
        from is StubBasedPsiElementBase<*> && from.language == ParadoxLocalisationLanguage -> selectLocale(from.containingFile) //直接转到containingFile，避免不必要的文件解析
        from is PsiElement && from.language == ParadoxLocalisationLanguage -> selectLocale(from.parent)
        else -> preferredParadoxLocale()
    }
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

val VirtualFile.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreHandler.getFileInfo(this)
val PsiElement.fileInfo: ParadoxFileInfo?
    get() = ParadoxCoreHandler.getFileInfo(this)

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

fun resolveLink(link: String, sourceElement: PsiElement): PsiElement? {
    //e.g. #cwt/stellaris/types/civic_or_origin/civic
    link.removePrefixOrNull(cwtLinkPrefix)?.let { return resolveCwtLink(it, sourceElement) }
    //e.g. #definition/stellaris/civic_or_origin.origin/origin_default
    link.removePrefixOrNull(definitionLinkPrefix)?.let { return resolveDefinitionLink(it, sourceElement) }
    //e.g. #localisation/stellaris/KEY
    link.removePrefixOrNull(localisationLinkPrefix)?.let { return resolveLocalisationLink(it, sourceElement) }
    //e.g. #path/stellaris/path
    link.removePrefixOrNull(filePathLinkPrefix)?.let { return resolveFilePathLink(it, sourceElement) }
    return null
}

private fun resolveCwtLink(linkWithoutPrefix: String, sourceElement: PsiElement): PsiElement? {
    ProgressManager.checkCanceled()
    val tokens = linkWithoutPrefix.split('/')
    if(tokens.size > 4) return null
    val gameType = tokens.getOrNull(0) ?: return null
    val category = tokens.getOrNull(1) ?: return null
    val project = sourceElement.project
    return when(category) {
        "types" -> {
            val name = tokens.getOrNull(2)
            val subtypeName = tokens.getOrNull(3)
            val config = when {
                name == null -> null
                subtypeName == null -> getCwtConfig(project).getValue(gameType).types[name]
                else -> getCwtConfig(project).getValue(gameType).types.getValue(name).subtypes[subtypeName]
            } ?: return null
            return config.pointer.element
        }
        "enums" -> {
            val name = tokens.getOrNull(2) ?: return null
            val valueName = tokens.getOrNull(3)
            val config = getCwtConfig(project).getValue(gameType).enums[name] ?: return null
            if(valueName == null) return config.pointer.element
            return config.valueConfigMap.get(valueName)?.pointer?.element
        }
        "complex_enums" -> {
            val name = tokens.getOrNull(2) ?: return null
            val config = getCwtConfig(project).getValue(gameType).complexEnums[name] ?: return null
            return config.pointer.element
        }
        "values" -> {
            val name = tokens.getOrNull(2) ?: return null
            val valueName = tokens.getOrNull(3)
            val config = getCwtConfig(project).getValue(gameType).values[name] ?: return null
            if(valueName == null) return config.pointer.element
            return config.valueConfigMap.get(valueName)?.pointer?.element
        }
        "scopes" -> {
            val name = tokens.getOrNull(2) ?: return null
            val config = getCwtConfig(project).getValue(gameType).scopeAliasMap[name] ?: return null
            return config.pointer.element
        }
        "system_links" -> {
            val name = tokens.getOrNull(2) ?: return null
            val config = getCwtConfig(project).getValue(gameType).systemLinks[name] ?: return null
            return config.pointer.element
        }
        "links" -> {
            val name = tokens.getOrNull(2) ?: return null
            val config = getCwtConfig(project).getValue(gameType).links[name] ?: return null
            return config.pointer.element
        }
        "modifier_categories" -> {
            val name = tokens.getOrNull(2) ?: return null
            val config = getCwtConfig(project).getValue(gameType).modifierCategories[name] ?: return null
            return config.pointer.element
        }
        "modifiers" -> {
            val name = tokens.getOrNull(2) ?: return null
            val config = getCwtConfig(project).getValue(gameType).modifiers[name] ?: return null
            return config.pointer.element
        }
        else -> null
    }
}

private fun resolveDefinitionLink(linkWithoutPrefix: String, sourceElement: PsiElement): ParadoxScriptDefinitionElement? {
    ProgressManager.checkCanceled()
    val tokens = linkWithoutPrefix.split('/')
    if(tokens.size > 3) return null
    val typeExpression = tokens.getOrNull(1) ?: return null
    val name = tokens.getOrNull(2) ?: return null
    val project = sourceElement.project
    val selector = definitionSelector(project, sourceElement).contextSensitive()
    return ParadoxDefinitionSearch.search(name, typeExpression, selector).find()
}

private fun resolveLocalisationLink(linkWithoutPrefix: String, sourceElement: PsiElement): ParadoxLocalisationProperty? {
    ProgressManager.checkCanceled()
    val tokens = linkWithoutPrefix.split('/')
    if(tokens.size > 2) return null
    val name = tokens.getOrNull(1) ?: return null
    val project = sourceElement.project
    val selector = localisationSelector(project, sourceElement).contextSensitive().preferLocale(sourceElement.localeConfig)
    return ParadoxLocalisationSearch.search(name, selector).find()
}

private fun resolveFilePathLink(linkWithoutPrefix: String, sourceElement: PsiElement): PsiFile? {
    ProgressManager.checkCanceled()
    val tokens = linkWithoutPrefix.split('/', limit = 2)
    val filePath = tokens.getOrNull(1) ?: return null
    val project = sourceElement.project
    val selector = fileSelector(project, sourceElement).contextSensitive()
    return ParadoxFilePathSearch.search(filePath, null, selector).find()
        ?.toPsiFile(project)
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
    append("<a href=\"").append(refText).append("\">").append(label).append("</a>")
    return this
}

fun StringBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = true): StringBuilder {
    DocumentationManagerUtil.createHyperlink(this, refText, label, plainLink)
    return this
}

fun StringBuilder.appendCwtLink(name: String, link: String, context: PsiElement? = null): StringBuilder {
    //如果name为空字符串，需要特殊处理
    if(name.isEmpty()) return append(PlsConstants.unresolvedString)
    //如果可以被解析为CWT规则，则显示链接（context传null时总是显示）
    val isResolved = context == null || resolveCwtLink(link, context) != null
    if(isResolved) return appendPsiLink("$cwtLinkPrefix$link".escapeXml(), name.escapeXml())
    //否则显示未解析的链接
    return appendUnresolvedLink(name)
}

fun StringBuilder.appendDefinitionLink(gameType: ParadoxGameType, name: String, typeExpression: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
    //如果name为空字符串，需要特殊处理
    if(name.isEmpty()) return append(PlsConstants.unresolvedString)
    //如果可以被解析为定义，则显示链接
    val isResolved = resolved == true || (resolved == null && ParadoxDefinitionSearch.search(name, null, definitionSelector(context.project, context)).findFirst() != null)
    if(isResolved) return appendPsiLink("$definitionLinkPrefix${gameType.id}/$typeExpression/$name", name)
    //否则显示未解析的链接
    return appendUnresolvedLink(name)
}

fun StringBuilder.appendLocalisationLink(gameType: ParadoxGameType, name: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
    //如果name为空字符串，需要特殊处理
    if(name.isEmpty()) return append(PlsConstants.unresolvedString)
    //如果可以被解析为本地化，则显示链接
    val isResolved = resolved == true || resolved == null && ParadoxLocalisationSearch.search(name, localisationSelector(context.project, context)).findFirst() != null
    if(isResolved) return appendPsiLink("$localisationLinkPrefix${gameType.id}/$name", name)
    //否则显示未解析的链接
    return appendUnresolvedLink(name)
}

fun StringBuilder.appendFilePathLink(text: String, gameType: ParadoxGameType, filePath: String, context: PsiElement, resolved: Boolean? = null): StringBuilder {
    //如果可以定位到绝对路径，则显示链接
    val isResolved = resolved == true || resolved == null && ParadoxFilePathSearch.search(filePath, null, fileSelector(context.project, context)).findFirst() != null
    if(isResolved) return appendPsiLink("$filePathLinkPrefix${gameType.id}/$filePath", text)
    //否则显示未解析的链接
    return appendUnresolvedLink(filePath)
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
            //	append(" ").append(PlsDocBundle.message("name.core.remoteFileId")).append(": ").append(remoteFileId).append(" )
            //}
            //相关链接
            //通过这种方式获取需要的url，使用rootPath而非gameRootPath
            val rootUri = fileInfo.rootInfo.rootPath.toUri().toString()
            append(" ")
            appendLink(rootUri, PlsDocBundle.message("text.localLinkLabel"))
            if(remoteFileId != null) {
                append(" | ")
                appendLink(getSteamWorkshopLinkOnSteam(remoteFileId), PlsDocBundle.message("text.steamLinkLabel"))
                appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
                append(" | ")
                appendLink(getSteamWorkshopLink(remoteFileId), PlsDocBundle.message("text.steamWebsiteLinkLabel")) //自带外部链接图标
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
//endregion
