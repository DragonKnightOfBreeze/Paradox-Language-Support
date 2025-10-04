package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.psi.PsiReference
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

interface ParadoxIdentifierNode : ParadoxComplexExpressionNode {
    override fun getReference(element: ParadoxExpressionElement): Reference?

    interface Reference : PsiReference {
        fun canResolveFor(constraint: ParadoxResolveConstraint): Boolean {
            return false
        }
    }
}
