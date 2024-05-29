package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.*
import icu.windea.pls.ep.documentation.*

class ParadoxDocumentationLinkHandler: DocumentationLinkHandler {
    override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
        if(target !is ParadoxDocumentationTarget) return null
        val resolved = ParadoxDocumentationLinkProvider.resolve(url, target.element) ?: return null
        val documentationTarget = ParadoxDocumentationTarget(resolved, null)
        return LinkResolveResult.resolvedTarget(documentationTarget)
    }
}

