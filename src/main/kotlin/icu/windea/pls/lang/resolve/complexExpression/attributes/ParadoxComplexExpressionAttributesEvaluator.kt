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
object ParadoxComplexExpressionAttributesEvaluator {
    data class Context(
        var dynamicDataAware: Boolean = false,
        var relaxDynamicDataAware: Boolean = false,
    )

    /**
     * 递归向下遍历 [node]，评估复杂表达式在节点级别的综合属性。
     */
    fun evaluate(node: ParadoxComplexExpressionNode): Int {
        val context = Context()
        node.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (!context.dynamicDataAware && isDynamicDataAware(node)) {
                    context.dynamicDataAware = true
                }
                if (!context.relaxDynamicDataAware && isRelaxDynamicDataAware(node)) {
                    context.relaxDynamicDataAware = true
                }
                return super.visit(node)
            }
        })

        var r = 0
        if (context.dynamicDataAware) r = r or ParadoxComplexExpressionAttributes.DYNAMIC_DATA_AWARE
        if (context.relaxDynamicDataAware) r = r or ParadoxComplexExpressionAttributes.RELAX_DYNAMIC_DATA_AWARE
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

        if (node !is ParadoxDynamicDataNode) return false
        val parent1 = node.parent?.castOrNull<ParadoxLinkValueNode>() ?: return false
        if (parent1.nodes.size != 1) return false
        val parent2 = parent1.parent?.castOrNull<ParadoxLinkNode>() ?: return false
        if (parent2.nodes.last() != parent1) return false
        return true
    }
}
