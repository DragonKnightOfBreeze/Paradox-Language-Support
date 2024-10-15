package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.collections.*

open class PsiResolvedPolyVariantReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    val resolved: List<T>
) : PsiPolyVariantReferenceBase<PsiElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException()
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return resolved.mapToArray { PsiElementResolveResult(it) }
    }
}
