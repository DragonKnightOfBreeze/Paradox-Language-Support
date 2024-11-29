package icu.windea.pls.lang.expression.complex

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*

data class ParadoxComplexExpressionProcessContext(
    var parent: ParadoxComplexExpressionNode? = null,
    var index: Int = -1,
    var processChildren: Boolean = true,
)

fun ParadoxComplexExpression.processAllNodes(
    context: ParadoxComplexExpressionProcessContext? = null,
    processor: Processor<ParadoxComplexExpressionNode>
): Boolean {
    return doProcessAllNodes(context, processor)
}

private fun ParadoxComplexExpressionNode.doProcessAllNodes(
    context: ParadoxComplexExpressionProcessContext? = null,
    processor: Processor<ParadoxComplexExpressionNode>
): Boolean {
    context?.processChildren = true
    val r = processor.process(this)
    if (!r) return false
    if (context?.processChildren == false || nodes.isEmpty()) return true
    for ((index, node) in nodes.withIndex()) {
        context?.parent = this
        context?.index = index
        val r1 = node.doProcessAllNodes(context, processor)
        if (!r1) return false
    }
    return true
}

fun ParadoxComplexExpression.processAllNodesToValidate(
    errors: MutableList<ParadoxComplexExpressionError>,
    context: ParadoxComplexExpressionProcessContext,
    processor: Processor<ParadoxComplexExpressionNode>
): Boolean {
    var result = true
    processAllNodes p@{
        when {
            it is ParadoxComplexExpression -> {
                if(it === this) return@p true
                errors += it.errors
                context.processChildren = false
            }
            it is ParadoxErrorTokenNode -> result = false
            it.text.isEmpty() -> result = false
        }
        if(result) result = processor.process(it)
        true
    }
    return result
}

fun ParadoxComplexExpression.getAllErrors(element: ParadoxExpressionElement?): List<ParadoxComplexExpressionError> {
    val errors = mutableListOf<ParadoxComplexExpressionError>()
    errors += this.errors
    this.processAllNodes { node ->
        node.getUnresolvedError()?.let { errors += it }
        if (element != null) node.getUnresolvedError(element)?.let { errors += it }
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
