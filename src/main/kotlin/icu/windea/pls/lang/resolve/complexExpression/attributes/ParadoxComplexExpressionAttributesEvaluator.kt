package icu.windea.pls.lang.resolve.complexExpression.attributes

import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor

/**
 * 复杂表达式的综合属性的评估器。
 *
 * @see ParadoxComplexExpression
 * @see ParadoxComplexExpressionAttributes
 */
class ParadoxComplexExpressionAttributesEvaluator {
    /**
     * 递归向下遍历 [node]，评估复杂表达式在节点级别的综合属性。
     */
    fun evaluate(node: ParadoxComplexExpressionNode): ParadoxComplexExpressionAttributes {
        var dynamicDataAware = false
        var lenientDynamicDataAware = false

        node.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (!dynamicDataAware && isDynamicDataInvolved(node)) {
                    dynamicDataAware = true
                }
                if (!lenientDynamicDataAware && isLenientDynamicDataInvolved(node)) {
                    lenientDynamicDataAware = true
                }
                return super.visit(node)
            }
        })

        var value = 0
        if (dynamicDataAware) value = value or ParadoxComplexExpressionAttributes.Flags.DYNAMIC_DATA_INVOLVED
        if (lenientDynamicDataAware) value = value or ParadoxComplexExpressionAttributes.Flags.LENIENT_DYNAMIC_DATA_INVOLVED
        return ParadoxComplexExpressionAttributes(value)
    }

    private fun isDynamicDataInvolved(node: ParadoxComplexExpressionNode): Boolean {
        // Example:
        // root.event_target:target
        // root.var
        //
        // Example flow:
        // node -> `ParadoxDynamicDataNode`

        return node is ParadoxDynamicDataNode
    }

    private fun isLenientDynamicDataInvolved(node: ParadoxComplexExpressionNode): Boolean {
        // Example:
        // root.var
        //
        // Example flow:
        // node -> `ParadoxDynamicDataNode`
        // -parent -> `ParadoxLinkValueNode` (single child node of its parent)
        // --parent -> `ParadoxLinkNode` (last one of its parent)

        if (node !is ParadoxDynamicDataNode) return false
        val parent1 = node.parent?.castOrNull<ParadoxLinkValueNode>() ?: return false
        val parent2 = parent1.parent?.castOrNull<ParadoxLinkNode>() ?: return false
        val parent3 = parent2.parent?.castOrNull<ParadoxLinkedExpression>() ?: return false
        if (parent2.nodes.size != 1) return false
        if (parent3.nodes.last() != parent2) return false
        return true
    }

    companion object {
        val DEFAULT = ParadoxComplexExpressionAttributesEvaluator()
    }
}
