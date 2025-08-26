package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.documentation.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.documentation.DocumentationLinkHandler
import icu.windea.pls.core.*
import icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider

class CwtDocumentationLinkHandler : DocumentationLinkHandler {
    override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
        if (target !is CwtDocumentationTarget) return null
        val link = url.removePrefixOrNull(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL) ?: return null
        val resolved = ReferenceLinkProvider.resolve(link, target.element) ?: return null
        return LinkResolveResult.resolvedTarget(getDocumentationTargets(resolved, null).first())
    }
}
