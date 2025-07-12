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
import icu.windea.pls.ep.reference.*
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

fun DocumentationBuilder.appendPsiLinkOrUnresolved(refText: String, label: String, plainLink: Boolean = true, context: PsiElement? = null): DocumentationBuilder {
    if (context != null && ParadoxReferenceLinkProvider.resolve(refText, context) == null) return appendUnresolvedLink(label)
    DocumentationManagerUtil.createHyperlink(this.content, refText, label, plainLink)
    return this
}

/**
 * @param local 输入的[url]是否是本地绝对路径。
 */
fun DocumentationBuilder.appendImgTag(url: String, local: Boolean = true): DocumentationBuilder {
    val finalUrl = if (local) url.toFileUrl() else url
    append("<img src=\"").append(finalUrl).append("\"/>")
    return this
}

fun DocumentationBuilder.appendImgTag(url: String, width: Int, height: Int, local: Boolean = true): DocumentationBuilder {
    val finalUrl = if (local) url.toFileUrl() else url
    append("<img src=\"").append(finalUrl).append("\"")
    //这里不能使用style="..."
    append(" width=\"").append(width).append("\" height=\"").append(height).append("\" vspace=\"0\" hspace=\"0\"")
    append("/>")
    return this
}

fun DocumentationBuilder.appendFileInfoHeader(element: PsiElement): DocumentationBuilder {
    val file = runReadAction { selectFile(element) } ?: return this
    if (PlsFileManager.isInjectedFile(file)) return this //ignored for injected PSI
    val fileInfo = file.fileInfo ?: return this
    val rootInfo = fileInfo.rootInfo
    if (rootInfo !is ParadoxRootInfo.MetadataBased) return this
    append("<span>")
    //描述符信息（模组名、版本等）
    append("[")
    append(rootInfo.qualifiedName.escapeXml())
    append("]")
    grayed {
        //相关链接
        //通过这种方式获取需要的url，使用rootPath而非gameRootPath
        val rootUri = rootInfo.rootPath.toUri().toString()
        append(" ")
        appendLink(rootUri, PlsBundle.message("text.localLinkLabel"))

        val steamId = rootInfo.steamId
        if (steamId != null) {
            append(" | ")
            val dataProvider = PlsFacade.getDataProvider()
            val workshopUrlInSteam = when (rootInfo) {
                is ParadoxRootInfo.Game -> dataProvider.getSteamGameStoreUrlInSteam(steamId)
                is ParadoxRootInfo.Mod -> dataProvider.getSteamWorkshopUrlInSteam(steamId)
            }
            appendLink(workshopUrlInSteam, PlsBundle.message("text.steamLinkLabel")) //自带外部链接图标
            appendExternalLinkIcon() // 使用翻译插件翻译文档注释后，这里会出现不必要的换行 - 已被修复
            append(" | ")
            val workshopUrl = when (rootInfo) {
                is ParadoxRootInfo.Game -> dataProvider.getSteamGameStoreUrl(steamId)
                is ParadoxRootInfo.Mod -> dataProvider.getSteamWorkshopUrl(steamId)
            }
            appendLink(workshopUrl, PlsBundle.message("text.steamWebsiteLinkLabel")) //自带外部链接图标
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
    if (element.language !is CwtLanguage) return this
    val file = element.containingFile ?: return this
    val vFile = file.virtualFile ?: return this
    val project = file.project
    val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
    val (fileProvider, configGroup, filePath) = fileProviders.firstNotNullOfOrNull f@{ fileProvider ->
        val configGroup = fileProvider.getContainingConfigGroup(vFile, project) ?: return@f null
        val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f null
        val relativePath = VfsUtil.getRelativePath(vFile, rootDirectory) ?: return@f null
        val filePath = relativePath.substringAfter('/', "").orNull() ?: return@f null
        tupleOf(fileProvider, configGroup, filePath)
    } ?: return this
    //规则分组信息
    val gameType = configGroup.gameType
    append("[").append(gameType.title).append(" Config]")
    val hintMessage = fileProvider.getHintMessage()
    if (hintMessage.isNotNullOrEmpty()) {
        grayed {
            append(" ").append(hintMessage)
        }
    }
    appendBr()
    //文件信息（相对于规则分组根目录的路径）
    append("[").append(filePath.escapeXml()).append("]")
    appendBr()
    return this
}

fun DocumentationBuilder.buildScopeDoc(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement): DocumentationBuilder {
    when {
        isUnsureScopeId(scopeId) -> append(scopeId)
        else -> {
            val link = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "scopes", scopeId)
            appendPsiLinkOrUnresolved(link.escapeXml(), scopeId.escapeXml(), context = contextElement)
        }
    }
    return this
}

fun DocumentationBuilder.buildScopeContextDoc(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement): DocumentationBuilder {
    var appendSeparator = false
    scopeContext.toScopeMap().forEach { (systemScope, scope) ->
        if (appendSeparator) appendBr() else appendSeparator = true
        val systemScopeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "system_scopes", systemScope)
        appendPsiLinkOrUnresolved(systemScopeLink.escapeXml(), systemScope.escapeXml(), context = contextElement)
        append(" = ")
        if (isUnsureScopeId(scope.id)) {
            append(scope)
        } else {
            val scopeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "scopes", scope.id)
            appendPsiLinkOrUnresolved(scopeLink.escapeXml(), scope.id.escapeXml(), context = contextElement)
        }
    }
    return this
}

fun DocumentationBuilder.getModifierCategoriesText(categories: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
    if (categories.isEmpty()) return ""
    return buildDocumentation {
        append("<pre>")
        var appendSeparator = false
        for (category in categories) {
            if (appendSeparator) append(", ") else appendSeparator = true
            val link = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "modifier_categories", category)
            appendPsiLinkOrUnresolved(link.escapeXml(), category.escapeXml(), context = contextElement)
        }
        append("</pre>")
    }
}

fun DocumentationBuilder.getScopeText(scopeId: String, gameType: ParadoxGameType, contextElement: PsiElement): String {
    return buildDocumentation {
        append("<pre>")
        buildScopeDoc(scopeId, gameType, contextElement)
        append("</pre>")
    }
}

fun DocumentationBuilder.getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
    if (scopeIds.isEmpty()) return ""
    return buildDocumentation {
        append("<pre>")
        var appendSeparator = false
        for (scopeId in scopeIds) {
            if (appendSeparator) append(", ") else appendSeparator = true
            buildScopeDoc(scopeId, gameType, contextElement)
        }
        append("</pre>")
    }
}

fun DocumentationBuilder.getScopeContextText(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement): String {
    return buildDocumentation {
        append("<pre>")
        buildScopeContextDoc(scopeContext, gameType, contextElement)
        append("</pre>")
    }
}

