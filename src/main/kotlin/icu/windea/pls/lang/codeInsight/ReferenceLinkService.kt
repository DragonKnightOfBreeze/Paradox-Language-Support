package icu.windea.pls.lang.codeInsight

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider

object ReferenceLinkService {
    fun supports(link: String): Boolean {
        return ReferenceLinkProvider.EP_NAME.extensionList.any { ep ->
            link.startsWith(ep.linkPrefix)
        }
    }

    fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        return ReferenceLinkProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!link.startsWith(ep.linkPrefix)) return@f null
            ep.resolve(link, contextElement)
        }
    }

    fun getUnresolvedMessage(link: String): String {
        return ReferenceLinkProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!link.startsWith(ep.linkPrefix)) return@f null
            ep.getUnresolvedMessage(link)
        } ?: PlsBundle.message("path.reference.unresolved", link)
    }

    fun createPsiLink(element: PsiElement, plainLink: Boolean = true): String? {
        return ReferenceLinkProvider.EP_NAME.extensionList.firstNotNullOfOrNull {
            it.createPsiLink(element, plainLink)
        }
    }
}
