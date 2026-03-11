package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.psi.PsiReference
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNodeBase
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor

abstract class ParadoxComplexExpressionBase : ParadoxComplexExpressionNodeBase(), ParadoxComplexExpression {
    @Optimized
    fun finishResolution() {
        bindParentRecursively(this)
    }

    private fun bindParentRecursively(node: ParadoxComplexExpressionNode) {
        node.nodes.forEachFast {
            if (it is ParadoxComplexExpressionNodeBase) it.parent = node
            bindParentRecursively(it)
        }
    }

    @Optimized
    override fun getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError> {
        val errors = FastList<ParadoxComplexExpressionError>()
        errors += getErrors(element)
        accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                node.getUnresolvedError()?.let { errors += it }
                if (element != null) node.getUnresolvedError(element)?.let { errors += it }
                return super.visit(node)
            }
        })
        return errors
    }

    @Optimized
    override fun getAllReferences(element: ParadoxExpressionElement): List<PsiReference> {
        val references = FastList<PsiReference>()
        accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                node.getReference(element)?.let { references += it }
                return super.visit(node)
            }
        })
        return references
    }
}
