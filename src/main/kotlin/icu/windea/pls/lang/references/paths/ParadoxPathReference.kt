package icu.windea.pls.lang.references.paths

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.annotations.api.*
import icu.windea.pls.ep.documentation.*

@HiddenApi
class ParadoxPathReference(
    element: PsiElement,
    rangeInElement: TextRange,
    val link: String
): PsiReferenceBase<PsiElement>(element, rangeInElement), EmptyResolveMessageProvider {
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException() //unsupported yet
    }
    
    override fun resolve(): PsiElement? {
        return DocumentationElementLinkProvider.resolve(link, element)
    }
    
    override fun getUnresolvedMessagePattern(): String {
        return DocumentationElementLinkProvider.getUnresolvedMessage(link)
    }
}