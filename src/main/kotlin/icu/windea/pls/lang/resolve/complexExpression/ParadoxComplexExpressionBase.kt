package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.psi.PsiReference
import com.intellij.util.Processor
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNodeBase
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
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

    override fun validateAllNodes(errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean {
        var result = true
        this.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression && node !== this@ParadoxComplexExpressionBase) {
                    errors += node.errors
                    return true
                }
                if (node is ParadoxErrorTokenNode || node.text.isEmpty()) {
                    result = false
                }
                if (result) result = processor.process(node)
                return super.visit(node)
            }
        })
        return result
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
