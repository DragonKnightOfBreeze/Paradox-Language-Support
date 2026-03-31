package icu.windea.pls.lang.resolve.complexExpression.attributes

import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkValueNode
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
        var relaxDynamicDataAware = false

        node.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (!dynamicDataAware && isDynamicDataInvolved(node)) {
                    dynamicDataAware = true
                }
                if (!relaxDynamicDataAware && isRelaxDynamicDataInvolved(node)) {
                    relaxDynamicDataAware = true
                }
                return super.visit(node)
            }
        })

        var value = 0
        if (dynamicDataAware) value = value or ParadoxComplexExpressionAttributes.Flags.DYNAMIC_DATA_INVOLVED
        if (relaxDynamicDataAware) value = value or ParadoxComplexExpressionAttributes.Flags.RELAX_DYNAMIC_DATA_INVOLVED
        return ParadoxComplexExpressionAttributes(value)
    }

    private fun isDynamicDataInvolved(node: ParadoxComplexExpressionNode): Boolean {
        // node -> `ParadoxDynamicDataNode`

        return node is ParadoxDynamicDataNode
    }

    private fun isRelaxDynamicDataInvolved(node: ParadoxComplexExpressionNode): Boolean {
        // node -> `ParadoxDynamicDataNode`
        // -parent -> `ParadoxLinkValueNode` (single child node)
        // --parent -> `ParadoxLinkNode` (last one)

        if (node !is ParadoxDynamicDataNode) return false
        val parent1 = node.parent?.castOrNull<ParadoxLinkValueNode>() ?: return false
        if (parent1.nodes.size != 1) return false
        val parent2 = parent1.parent?.castOrNull<ParadoxLinkNode>() ?: return false
        if (parent2.nodes.last() != parent1) return false
        return true
    }

    companion object {
        val DEFAULT = ParadoxComplexExpressionAttributesEvaluator()
    }
}
