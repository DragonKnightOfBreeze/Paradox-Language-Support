package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.ep.resolve.ReferenceLinkProvider

@Optimized
object ReferenceLinkService {
    fun supports(link: String): Boolean {
        val eps = ReferenceLinkProvider.EP_NAME.extensionList
        return eps.anyFast { ep ->
            link.startsWith(ep.linkPrefix)
        }
    }

    fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        val eps = ReferenceLinkProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (!link.startsWith(ep.linkPrefix)) return@f
            ep.resolve(link, contextElement)?.let { return it }
        }
        return null
    }

    fun getUnresolvedMessage(link: String): String {
        val eps = ReferenceLinkProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (!link.startsWith(ep.linkPrefix)) return@f
            ep.getUnresolvedMessage(link)?.let { return it }
        }
        return ReferenceLinkProvider.getDefaultUnresolvedMessage(link)
    }

    fun createPsiLink(element: PsiElement, plainLink: Boolean = true): String? {
        val eps = ReferenceLinkProvider.EP_NAME.extensionList
        eps.forEachFast {
            it.createPsiLink(element, plainLink)?.let { return it }
        }
        return null
    }
}
