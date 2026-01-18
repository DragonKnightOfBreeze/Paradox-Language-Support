@file:Suppress("unused")

package icu.windea.pls.lang.resolve.complexExpression.util

import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode): Boolean {
        return true
    }
}

abstract class ParadoxComplexExpressionRecursiveVisitor: ParadoxComplexExpressionVisitor() {
    override fun visit(node: ParadoxComplexExpressionNode): Boolean {
        val r = node.acceptChildren(this)
        if (!r) return false
        return visitFinished(node)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode): Boolean {
        return true
    }
}
