package icu.windea.pls.lang.resolving.complexExpression

import com.intellij.psi.PsiReference
import com.intellij.util.Processor
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxComplexExpressionNodeBase
import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxErrorTokenNode

abstract class ParadoxComplexExpressionBase : ParadoxComplexExpressionNodeBase(), ParadoxComplexExpression {
    override fun validateAllNodes(errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean {
        var result = true
        this.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
                if (node is ParadoxComplexExpression && node !== this@ParadoxComplexExpressionBase) {
                    errors += node.errors
                    return true
                }
                if (node is ParadoxErrorTokenNode || node.text.isEmpty()) {
                    result = false
                }
                if (result) result = processor.process(node)
                return super.visit(node, parentNode)
            }
        })
        return result
    }

    override fun getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        errors += this.errors
        this.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
                node.getUnresolvedError()?.let { errors += it }
                if (element != null) node.getUnresolvedError(element)?.let { errors += it }
                return super.visit(node, parentNode)
            }
        })
        return errors
    }

    override fun getAllReferences(element: ParadoxExpressionElement): List<PsiReference> {
        val references = mutableListOf<PsiReference>()
        this.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
                node.getReference(element)?.let { references += it }
                return super.visit(node, parentNode)
            }
        })
        return references
    }
}
