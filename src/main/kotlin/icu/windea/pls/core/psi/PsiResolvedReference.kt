package icu.windea.pls.core.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException

open class PsiResolvedReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    val resolved: T?
) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException()
    }

    override fun resolve() = resolved
}
