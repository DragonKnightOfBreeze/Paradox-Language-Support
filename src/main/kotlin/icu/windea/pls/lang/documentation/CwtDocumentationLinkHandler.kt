package icu.windea.pls.lang.documentation

import com.intellij.platform.backend.documentation.*
import icu.windea.pls.ep.documentation.*

class CwtDocumentationLinkHandler: DocumentationLinkHandler {
    override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
        if(target !is CwtDocumentationTarget) return null
        val resolved = ParadoxDocumentationLinkProvider.resolve(url, target.element) ?: return null
        val documentationTarget = CwtDocumentationTarget(resolved, null) 
        return LinkResolveResult.resolvedTarget(documentationTarget)
    }
}
