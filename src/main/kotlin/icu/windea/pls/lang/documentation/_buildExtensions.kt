@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.cwt.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.ep.documentation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxScopeManager.isUnsureScopeId
import icu.windea.pls.model.*

fun DocumentationBuilder.appendBr(): DocumentationBuilder {
    append("<br>")
    return this
}

fun DocumentationBuilder.appendIndent(): DocumentationBuilder {
    append("&nbsp;&nbsp;&nbsp;&nbsp;")
    return this
}

fun DocumentationBuilder.appendExternalLinkIcon(): DocumentationBuilder {
    append("<icon src='ide/external_link_arrow.svg'/>")
    return this
}

fun DocumentationBuilder.appendUnresolvedLink(label: String): DocumentationBuilder {
    append(label.escapeXml()) //直接显示对应的标签文本
    return this
}

fun DocumentationBuilder.appendLink(refText: String, label: String): DocumentationBuilder {
    //不自动转义link的label
    append("<a href=\"").append(refText).append("\">").append(label).append("</a>")
    return this
}

fun DocumentationBuilder.appendPsiLink(refText: String, label: String, plainLink: Boolean = true): DocumentationBuilder {
    DocumentationManagerUtil.createHyperlink(this.content, refText, label, plainLink)
    return this
}

/**
 * @param local 输入的[url]是否是本地绝对路径。
 */
fun DocumentationBuilder.appendImgTag(url: String, local: Boolean = true): DocumentationBuilder {
    val finalUrl = if(local) url.toFileUrl() else url
    append("<img src=\"").append(finalUrl).append("\"/>")
    return this
}

fun DocumentationBuilder.appendImgTag(url: String, width: Int, height: Int, local: Boolean = true): DocumentationBuilder {
    val finalUrl = if(local) url.toFileUrl() else url
    append("<img src=\"").append(finalUrl).append("\"")
    //这里不能使用style="..."
    append(" width=\"").append(width).append("\" height=\"").append(height).append("\" vspace=\"0\" hspace=\"0\"")
    append("/>")
    return this
}

fun DocumentationBuilder.appendFileInfoHeader(element: PsiElement): DocumentationBuilder {
    val file = runReadAction { selectFile(element) } ?: return this
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
            appendLink(UrlProvider.getSteamWorkshopUrlInSteam(remoteFileId), PlsBundle.message("text.steamLinkLabel"))
            appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
            append(" | ")
            appendLink(UrlProvider.getSteamWorkshopUrl(remoteFileId), PlsBundle.message("text.steamWebsiteLinkLabel")) //自带外部链接图标
        }
    }
    append("</span>")
    appendBr()
    //文件信息（路径）
    append("[").append(fileInfo.path).append("]")
    appendBr()
    return this
}

fun DocumentationBuilder.appendCwtConfigFileInfoHeader(element: PsiElement): DocumentationBuilder {
    if(element.language != CwtLanguage) return this
    val file = element.containingFile ?: return this
    val vFile = file.virtualFile ?: return this
    val project = file.project
    val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
    val (fileProvider, configGroup, filePath) = fileProviders.firstNotNullOfOrNull f@{ fileProvider ->
        val configGroup = fileProvider.getContainingConfigGroup(vFile, project) ?: return@f null
        val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f null
        val filePath = VfsUtil.getRelativePath(vFile, rootDirectory)?.substringAfter('/') ?: return@f null
        tupleOf(fileProvider, configGroup, filePath)
    } ?: return this
    //规则分组信息
    val gameType = configGroup.gameType
    append("[").append(gameType.title).append(" Config]")
    grayed {
        append(" ").append(fileProvider.getHintMessage())
    }
    appendBr()
    //文件信息（相对于规则分组根目录的路径）
    append("[").append(filePath.escapeXml()).append("]")
    appendBr()
    return this
}

fun DocumentationBuilder.appendCwtConfigLink(shortLink: String, linkText: String, context: PsiElement? = null): DocumentationBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = CwtConfigLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix$shortLink".escapeXml()
    val finalLinkText = linkText.escapeXml()
    if(context != null && ParadoxDocumentationLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    appendPsiLink(finalLink, finalLinkText)
    return this
}

fun DocumentationBuilder.appendDefinitionLink(
    gameType: ParadoxGameType,
    name: String,
    typeExpression: String,
    context: PsiElement? = null,
    label: String = name.escapeXml()
): DocumentationBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxDefinitionLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.prefix}$typeExpression/$name".escapeXml()
    val finalLinkText = label
    if(context != null && ParadoxDocumentationLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun DocumentationBuilder.appendLocalisationLink(
    gameType: ParadoxGameType,
    name: String,
    context: PsiElement? = null,
    label: String = name.escapeXml()
): DocumentationBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxLocalisationLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.prefix}$name".escapeXml()
    val finalLinkText = label
    if(context != null && ParadoxDocumentationLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun DocumentationBuilder.appendFilePathLink(
    gameType: ParadoxGameType,
    filePath: String,
    linkText: String,
    context: PsiElement? = null,
    label: String = linkText.escapeXml()
): DocumentationBuilder {
    //如果context不为null且链接无法被解析，则显示未解析的链接
    val linkPrefix = ParadoxFilePathLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix${gameType.prefix}$filePath".escapeXml()
    val finalLinkText = label
    if(context != null && ParadoxDocumentationLinkProvider.resolve(finalLink, context) == null) return appendUnresolvedLink(finalLinkText)
    return appendPsiLink(finalLink, finalLinkText)
}

fun DocumentationBuilder.appendModifierLink(
    name: String,
    label: String = name.escapeXml()
): DocumentationBuilder {
    val linkPrefix = ParadoxModifierLinkProvider.LINK_PREFIX
    val finalLink = "$linkPrefix$name".escapeXml()
    val finalLinkText = label
    return appendPsiLink(finalLink, finalLinkText)
}

fun DocumentationBuilder.buildScopeDoc(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement): DocumentationBuilder {
    when {
        isUnsureScopeId(scopeId) -> append(scopeId)
        else -> appendCwtConfigLink("${gameType.prefix}scopes/$scopeId", scopeId, contextElement)
    }
    return this
}

fun DocumentationBuilder.buildScopeContextDoc(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement): DocumentationBuilder {
    var appendSeparator = false
    scopeContext.toScopeMap().forEach { (systemScope, scope) ->
        if(appendSeparator) appendBr() else appendSeparator = true
        appendCwtConfigLink("${gameType.prefix}system_scopes/$systemScope", systemScope, contextElement)
        append(" = ")
        when {
            isUnsureScopeId(scope.id) -> append(scope)
            else -> appendCwtConfigLink("${gameType.prefix}scopes/${scope.id}", scope.id, contextElement)
        }
    }
    return this
}

fun DocumentationBuilder.getModifierCategoriesText(categories: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
    if(categories.isEmpty()) return ""
    return buildDocumentation {
        append("<code>")
        var appendSeparator = false
        for(category in categories) {
            if(appendSeparator) append(", ") else appendSeparator = true
            appendCwtConfigLink("${gameType.prefix}modifier_categories/$category", category, contextElement)
        }
        append("</code>")
    }
}

fun DocumentationBuilder.getScopeText(scopeId: String, gameType: ParadoxGameType, contextElement: PsiElement): String {
    return buildDocumentation {
        append("<code>")
        buildScopeDoc(scopeId, gameType, contextElement)
        append("</code>")
    }
}

fun DocumentationBuilder.getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
    if(scopeIds.isEmpty()) return ""
    return buildDocumentation {
        append("<code>")
        var appendSeparator = false
        for(scopeId in scopeIds) {
            if(appendSeparator) append(", ") else appendSeparator = true
            buildScopeDoc(scopeId, gameType, contextElement)
        }
        append("</code>")
    }
}

fun DocumentationBuilder.getScopeContextText(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement): String {
    return buildDocumentation {
        append("<code>")
        buildScopeContextDoc(scopeContext, gameType, contextElement)
        append("</code>")
    }
}

