package icu.windea.pls.lang.expression

import com.intellij.psi.PsiReference
import com.intellij.util.Processor
import icu.windea.pls.lang.expression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.expression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.psi.ParadoxExpressionElement

abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
        node.nodes.forEach {
            val r = visit(it, node)
            if (!r) return false
        }
        return visitFinished(node, parentNode)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode, parent: ParadoxComplexExpressionNode?): Boolean {
        return true
    }
}

fun ParadoxComplexExpression.accept(visitor: ParadoxComplexExpressionVisitor) {
    visitor.visit(this, null)
}

fun ParadoxComplexExpression.validateAllNodes(errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    var result = true
    this.accept(object : ParadoxComplexExpressionVisitor() {
        override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
            if (node is ParadoxComplexExpression && node !== this@validateAllNodes) {
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

fun ParadoxComplexExpression.getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError> {
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

fun ParadoxComplexExpression.getAllReferences(element: ParadoxExpressionElement): List<PsiReference> {
    val references = mutableListOf<PsiReference>()
    this.accept(object : ParadoxComplexExpressionVisitor() {
        override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
            node.getReference(element)?.let { references += it }
            return super.visit(node, parentNode)
        }
    })
    return references
}
