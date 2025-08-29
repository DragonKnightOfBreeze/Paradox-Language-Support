package icu.windea.pls.lang.expression.complex

import icu.windea.pls.lang.expression.complex.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
        node.nodes.forEach {
            val r = visit(it, node)
            if (!r) return false
        }
        return visitFinished(node, parentNode)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode, parent: ParadoxComplexExpressionNode?): Boolean {
        return true
    }
}
