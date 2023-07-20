package icu.windea.pls.core.search

import com.intellij.psi.*
import icu.windea.pls.core.*

class ParadoxFilteredRequestResultProcessor(
    target: PsiElement,
    private val constraint: ParadoxResolveConstraint
): FilteredRequestResultProcessor(target) {
    override fun applyFor(element: PsiElement): Boolean {
        return element.language.isParadoxLanguage()
    }
    
    override fun acceptElement(element: PsiElement): Boolean {
        return element.canResolveReference(constraint)
    }
    
    override fun acceptReference(reference: PsiReference): Boolean {
        return reference.canResolve(constraint)
    }
}
