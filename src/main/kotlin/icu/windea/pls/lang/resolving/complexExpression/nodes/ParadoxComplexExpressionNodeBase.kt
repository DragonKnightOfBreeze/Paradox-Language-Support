package icu.windea.pls.lang.resolving.complexExpression.nodes

import icu.windea.pls.lang.resolving.complexExpression.ParadoxComplexExpressionVisitor

abstract class ParadoxComplexExpressionNodeBase : ParadoxComplexExpressionNode {
    override var parent: ParadoxComplexExpressionNode? = null

    init {
        nodes.forEach { if (it is ParadoxComplexExpressionNodeBase) it.parent = this }
    }

    override fun accept(visitor: ParadoxComplexExpressionVisitor) {
        visitor.visit(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxComplexExpressionNode && this.javaClass.isAssignableFrom(other.javaClass) && text == other.text)
    }

    override fun hashCode() = text.hashCode()

    override fun toString() = text
}
