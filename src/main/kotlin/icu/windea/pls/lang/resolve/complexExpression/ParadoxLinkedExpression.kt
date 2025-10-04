package icu.windea.pls.lang.resolve.complexExpression

import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode

/**
 * 由一组链接节点（[ParadoxLinkNode]）组成的复杂表达式。
 * 链接节点之间用 `.` 分隔（作为标记节点，即 [ParadoxMarkerNode]）。
 */
interface ParadoxLinkedExpression : ParadoxComplexExpression {
    @Suppress("unused")
    val linkNodes: List<ParadoxLinkNode> get() = nodes.filterIsInstance<ParadoxLinkNode>()
}
