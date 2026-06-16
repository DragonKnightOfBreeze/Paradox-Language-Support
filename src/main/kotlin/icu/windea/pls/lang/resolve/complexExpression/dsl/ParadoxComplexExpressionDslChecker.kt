package icu.windea.pls.lang.resolve.complexExpression.dsl

import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import kotlin.reflect.full.isSuperclassOf

object ParadoxComplexExpressionDslChecker {
    fun check(node: ParadoxComplexExpressionNode, dslNode: ParadoxComplexExpressionDsl) {
        if (dslNode !is ParadoxComplexExpressionDslNode) return
        if (!matches(node, dslNode)) {
            mismatched(node, dslNode)
        }
        if (dslNode.nodes.size != node.nodes.size) {
            mismatchedChildrenSize(node, dslNode)
        }
        if (dslNode.nodes.isNotEmpty()) {
            dslNode.nodes.zip(node.nodes).forEach { (node1, targetNode1) -> check(targetNode1, node1) }
        }
    }

    private fun matches(node: ParadoxComplexExpressionNode, dslNode: ParadoxComplexExpressionDslNode): Boolean {
        if (!dslNode.type.isSuperclassOf(node::class)) return false
        if (dslNode.text != node.text) return false
        if (dslNode.startOffset != node.rangeInExpression.startOffset) return false
        if (dslNode.endOffset != node.rangeInExpression.endOffset) return false
        return true
    }

    private fun mismatched(node: ParadoxComplexExpressionNode, dslNode: ParadoxComplexExpressionDslNode): Nothing {
        throw IllegalStateException("Mismatched: ${dslNode.render()} vs ${node.render()}")
    }

    private fun mismatchedChildrenSize(node: ParadoxComplexExpressionNode, dslNode: ParadoxComplexExpressionDslNode): Nothing {
        throw IllegalStateException("Mismatched children size: ${dslNode.render()} vs ${node.render()} (${dslNode.nodes.size} vs ${node.nodes.size})")
    }

    private fun ParadoxComplexExpressionDslNode.render(): String {
        return "${type.simpleName}: ${text} (${startOffset}, ${endOffset})"
    }

    private fun ParadoxComplexExpressionNode.render(): String {
        return "${javaClass.simpleName}: ${text} (${rangeInExpression.startOffset}, ${rangeInExpression.endOffset})"
    }
}
