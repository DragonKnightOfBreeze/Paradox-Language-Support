package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.psi.PsiReference
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNodeBase
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionVisitor

abstract class ParadoxComplexExpressionBase : ParadoxComplexExpressionNodeBase(), ParadoxComplexExpression {
    fun finishResolving() {
        bindParentRecursively(this)
    }

    private fun bindParentRecursively(node: ParadoxComplexExpressionNode) {
        node.nodes.forEach {
            if (it is ParadoxComplexExpressionNodeBase) it.parent = node
            bindParentRecursively(it)
        }
    }

    override fun getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        errors += this.errors
        this.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                node.getUnresolvedError()?.let { errors += it }
                if (element != null) node.getUnresolvedError(element)?.let { errors += it }
                return super.visit(node)
            }
        })
        return errors
    }

    override fun getAllReferences(element: ParadoxExpressionElement): List<PsiReference> {
        val references = mutableListOf<PsiReference>()
        this.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                node.getReference(element)?.let { references += it }
                return super.visit(node)
            }
        })
        return references
    }
}
