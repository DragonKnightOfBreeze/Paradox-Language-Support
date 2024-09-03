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
    if(!r) return false
    if(nodes.isNotEmpty()) {
        for(node in nodes) {
            val r1 = node.doProcessAllNodes(processor)
            if(!r1) return false
        }
    }
    return true
}

fun ParadoxComplexExpression.processAllLeafNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    return doProcessAllLeafNodes(processor)
}

private fun ParadoxComplexExpressionNode.doProcessAllLeafNodes(processor: Processor<ParadoxComplexExpressionNode>): Boolean {
    if(nodes.isNotEmpty()) {
        for(node in nodes) {
            val r1 = node.doProcessAllLeafNodes(processor)
            if(!r1) return false
        }
        return true
    } else {
        return processor.process(this)
    }
}

fun ParadoxComplexExpression.getReferences(element: ParadoxExpressionElement): Array<PsiReference> {
    val references = mutableListOf<PsiReference>()
    this.doGetReferences(element, references)
    return references.toTypedArray()
}

private fun ParadoxComplexExpressionNode.doGetReferences(element: ParadoxExpressionElement, references: MutableList<PsiReference>) {
    val reference = this.getReference(element)
    if(reference != null) {
        references.add(reference)
    }
    if(nodes.isNotEmpty()) {
        for(node in nodes) {
            node.doGetReferences(element, references)
        }
    }
}
