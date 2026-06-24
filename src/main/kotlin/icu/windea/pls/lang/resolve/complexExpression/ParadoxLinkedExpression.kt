package icu.windea.pls.lang.resolve.complexExpression

import icu.windea.pls.lang.resolve.complexExpression.nodes.*

/**
 * 链式的复杂表达式。
 *
 * 由一组链接节点（[ParadoxLinkNode]）组成，链接节点之间使用作为运算符节点（[ParadoxOperatorNode]）的点号分隔。
 */
interface ParadoxLinkedExpression : ParadoxComplexExpression {
    val linkNodes: List<ParadoxLinkNode>
}
