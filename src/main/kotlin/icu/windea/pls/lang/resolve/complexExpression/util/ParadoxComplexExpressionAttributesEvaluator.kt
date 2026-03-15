package icu.windea.pls.lang.resolve.complexExpression.util

import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkValueNode

/**
 * 复杂表达式的综合属性的评估器。
 *
 * @see ParadoxComplexExpression
 * @see ParadoxComplexExpressionAttributes
 */
object ParadoxComplexExpressionAttributesEvaluator {
    /** 评估指定的复杂表达式节点（[node]）的综合属性。 */
    fun evaluate(node: ParadoxComplexExpressionNode): Int {
        var r = 0
        node.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (isDynamicDataAware(node)) {
                    r = r or ParadoxComplexExpressionAttributes.DYNAMIC_DATA_AWARE
                    if (isRelaxDynamicDataAware(node)) {
                        r = r or ParadoxComplexExpressionAttributes.RELAX_DYNAMIC_DATA_AWARE
                    }
                }

                return super.visit(node)
            }
        })
        return r
    }

    private fun isDynamicDataAware(node: ParadoxComplexExpressionNode): Boolean {
        // node -> `ParadoxDynamicDataNode`

        return node is ParadoxDynamicDataNode
    }

    private fun isRelaxDynamicDataAware(node: ParadoxComplexExpressionNode): Boolean {
        // node -> `ParadoxDynamicDataNode`
        // -parent -> `ParadoxLinkValueNode` (single child node)
        // --parent -> `ParadoxLinkNode` (last one)

        // if(node !is ParadoxDynamicDataNode) return false // unnecessary
        val parent1 = node.parent?.castOrNull<ParadoxLinkValueNode>() ?: return false
        if (parent1.nodes.size != 1) return false
        val parent2 = parent1.parent?.castOrNull<ParadoxLinkNode>() ?: return false
        if (parent2.nodes.last() != parent1) return false
        return true
    }
}
