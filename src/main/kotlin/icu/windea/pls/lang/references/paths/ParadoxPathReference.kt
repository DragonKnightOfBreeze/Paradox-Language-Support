package icu.windea.pls.lang.references.paths

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ep.codeInsight.navigation.ReferenceLinkProvider

/**
 * @see ParadoxPathReferenceProvider
 */
class ParadoxPathReference(
    element: PsiElement,
    rangeInElement: TextRange,
    val link: String
) : PsiReferenceBase<PsiElement>(element, rangeInElement), EmptyResolveMessageProvider {
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException() //unsupported yet
    }

    override fun resolve(): PsiElement? {
        return ReferenceLinkProvider.resolve(link, element)
    }

    override fun getUnresolvedMessagePattern(): String {
        return ReferenceLinkProvider.getUnresolvedMessage(link)
    }
}
