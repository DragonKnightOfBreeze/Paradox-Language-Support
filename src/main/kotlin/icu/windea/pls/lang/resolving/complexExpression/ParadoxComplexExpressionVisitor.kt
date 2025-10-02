package icu.windea.pls.lang.resolving.complexExpression

import icu.windea.pls.lang.resolving.complexExpression.nodes.ParadoxComplexExpressionNode

abstract class ParadoxComplexExpressionVisitor {
    open fun visit(node: ParadoxComplexExpressionNode): Boolean {
        node.nodes.forEach {
            if (!visit(it)) return false
        }
        return visitFinished(node)
    }

    open fun visitFinished(node: ParadoxComplexExpressionNode): Boolean {
        return true
    }
}
