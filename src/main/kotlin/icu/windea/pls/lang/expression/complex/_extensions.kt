package icu.windea.pls.lang.expression.complex

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*

fun ParadoxComplexExpression.processAllNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    return doProcessAllNodes(processor)
}

private fun ParadoxComplexExpressionNode.doProcessAllNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    val r = processor.process(this)
    if (!r) return false
    if (nodes.isNotEmpty()) {
        for (node in nodes) {
            val r1 = node.doProcessAllNodes(processor)
            if (!r1) return false
        }
    }
    return true
}

fun ParadoxComplexExpression.processAllLeafNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    return doProcessAllLeafNodes(processor)
}

private fun ParadoxComplexExpressionNode.doProcessAllLeafNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    if (nodes.isNotEmpty()) {
        for (node in nodes) {
            val r1 = node.doProcessAllLeafNodes(processor)
            if (!r1) return false
        }
        return true
    } else {
        return processor.process(this)
    }
}

fun ParadoxComplexExpression.getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError> {
    val errors = mutableListOf<ParadoxComplexExpressionError>()
    errors += this.errors
    this.processAllNodes { node ->
        node.getUnresolvedError()?.let { errors += it }
        if(element != null) node.getUnresolvedError(element)?.let { errors += it }
        true
    }
    return errors
}

fun ParadoxComplexExpression.getAllReferences(element: ParadoxExpressionElement): List<PsiReference> {
    val references = mutableListOf<PsiReference>()
    this.doGetAllReferences(element, references)
    return references
}

private fun ParadoxComplexExpressionNode.doGetAllReferences(element: ParadoxExpressionElement, references: MutableList<PsiReference>) {
    val reference = this.getReference(element)
    if (reference != null) {
        references.add(reference)
    }
    if (nodes.isNotEmpty()) {
        for (node in nodes) {
            node.doGetAllReferences(element, references)
        }
    }
}
