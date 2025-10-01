package icu.windea.pls.lang.resolving.complexExpression

import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode? = null): Boolean {
        node.nodes.forEach {
            val r = visit(it, node)
            if (!r) return false
        }
        return visitFinished(node, parentNode)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode, parent: ParadoxComplexExpressionNode? = null): Boolean {
        return true
    }
}
