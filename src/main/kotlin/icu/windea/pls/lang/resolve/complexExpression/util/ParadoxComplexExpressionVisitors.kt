@file:Suppress("unused")

package icu.windea.pls.lang.resolve.complexExpression.util

import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

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
