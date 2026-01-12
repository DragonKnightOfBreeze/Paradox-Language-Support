package icu.windea.pls.lang.resolve.complexExpression.util

import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode): Boolean {
        for (child in node.nodes) {
            if (!visit(child)) return false
        }
        return visitFinished(node)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode): Boolean {
        return true
    }
}

fun ParadoxComplexExpressionNode.accept(visitor: ParadoxComplexExpressionVisitor): Boolean {
    return visitor.visit(this)
}

fun ParadoxComplexExpressionNode.acceptChildren(visitor: ParadoxComplexExpressionVisitor): Boolean {
    return nodes.process { visitor.visit(it) }
}
