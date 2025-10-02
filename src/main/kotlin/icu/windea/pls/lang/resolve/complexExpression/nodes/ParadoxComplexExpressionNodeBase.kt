package icu.windea.pls.lang.resolve.complexExpression.nodes

import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionVisitor

abstract class ParadoxComplexExpressionNodeBase : ParadoxComplexExpressionNode {
    override val nodes: List<ParadoxComplexExpressionNode> get() = emptyList()
    override var parent: ParadoxComplexExpressionNode? = null

    override fun accept(visitor: ParadoxComplexExpressionVisitor) {
        visitor.visit(this)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxComplexExpressionNode && this.javaClass.isAssignableFrom(other.javaClass) && text == other.text)
    }

    override fun hashCode() = text.hashCode()

    override fun toString() = text
}
