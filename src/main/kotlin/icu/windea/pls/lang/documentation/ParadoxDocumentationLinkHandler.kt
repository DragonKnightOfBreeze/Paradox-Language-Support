package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.documentation.DocumentationManagerProtocol
import com.intellij.platform.backend.documentation.DocumentationLinkHandler
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.LinkResolveResult
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider

class ParadoxDocumentationLinkHandler : DocumentationLinkHandler {
    override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
        if (target !is ParadoxDocumentationTarget) return null
        val link = url.removePrefixOrNull(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL) ?: return null
        val resolved = ReferenceLinkProvider.resolve(link, target.element) ?: return null
        return LinkResolveResult.resolvedTarget(getDocumentationTargets(resolved, null).first())
    }
}
