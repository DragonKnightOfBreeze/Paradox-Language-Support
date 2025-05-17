package icu.windea.pls.lang.expression.complex

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*

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
        override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?):Boolean {
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
