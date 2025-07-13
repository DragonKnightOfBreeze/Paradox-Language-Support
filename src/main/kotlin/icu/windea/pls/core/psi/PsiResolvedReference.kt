package icu.windea.pls.core.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*

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
