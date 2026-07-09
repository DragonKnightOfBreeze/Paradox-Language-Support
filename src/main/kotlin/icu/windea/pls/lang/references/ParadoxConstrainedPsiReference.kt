package icu.windea.pls.lang.references

import com.intellij.psi.PsiReference
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint

/**
 * @see ParadoxReferenceConstraint
 */
interface ParadoxConstrainedPsiReference : PsiReference {
    fun canResolveFor(constraint: ParadoxReferenceConstraint): Boolean {
        return true
    }
}
