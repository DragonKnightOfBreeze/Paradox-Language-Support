package icu.windea.pls.lang.search

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.psi.FilteredRequestResultProcessor
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

class ParadoxFilteredRequestResultProcessor(
    target: PsiElement,
    private val constraint: ParadoxResolveConstraint
) : FilteredRequestResultProcessor(target) {
    override fun applyFor(element: PsiElement): Boolean {
        return element.language is ParadoxBaseLanguage
    }

    override fun acceptElement(element: PsiElement): Boolean {
        return constraint.canResolveReference(element)
    }

    override fun acceptReference(reference: PsiReference): Boolean {
        return constraint.canResolve(reference)
    }
}
