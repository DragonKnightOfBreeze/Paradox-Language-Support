package icu.windea.pls.core.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.collections.mapToArray

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
