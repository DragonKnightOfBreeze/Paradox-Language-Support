@file:Suppress("unused")

package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*

/**
 * 复杂表达式的访问者。
 */
abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode): Boolean {
        return true
    }
}

/**
 * 递归向下遍历的复杂表达式的访问器。
 */
abstract class ParadoxComplexExpressionRecursiveVisitor : ParadoxComplexExpressionVisitor() {
    override fun visit(node: ParadoxComplexExpressionNode): Boolean {
        val r = node.acceptChildren(this)
        if (!r) return false
        return visitFinished(node)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode): Boolean {
        return true
    }
}

abstract class ParadoxComplexExpressionWordSelectionRecursiveVisitor(private val offsetInExpression: Int) : ParadoxComplexExpressionRecursiveVisitor() {
    override fun visitFinished(node: ParadoxComplexExpressionNode): Boolean {
        if (node.nodes.isEmpty()) {
            // 加入当前叶子节点
            if (!isCurrentNode(node)) return true
            visitWordSelection(node, node.rangeInExpression).let { if (!it) return false }
        } else {
            // 首先加入内层的当前节点
            val currentNode = node.nodes.find { isCurrentNode(it) } ?: return true
            visitWordSelection(currentNode, currentNode.rangeInExpression).let { if (!it) return false }
            if (node is ParadoxLinkedExpression && node.rangeInExpression.startOffset != currentNode.rangeInExpression.startOffset) {
                // 链式表达式开始 ~ 当前链接节点结束
                val linkedRange = TextRange.create(node.rangeInExpression.startOffset, currentNode.rangeInExpression.endOffset)
                visitWordSelection(currentNode, linkedRange).let { if (!it) return false }
            }
        }
        return true
    }

    protected open fun visitWordSelection(node: ParadoxComplexExpressionNode, rangeInExpression: TextRange): Boolean {
        return true
    }

    private fun isCurrentNode(node: ParadoxComplexExpressionNode): Boolean {
        // 排除 markerNode/operatorNode
        if (node is ParadoxMarkerNode || node is ParadoxOperatorNode) return false
        // startOffset <= offest <= endOffset
        return offsetInExpression >= node.rangeInExpression.startOffset && offsetInExpression <= node.rangeInExpression.endOffset
    }
}
