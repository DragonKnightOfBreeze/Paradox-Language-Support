package icu.windea.pls.lang.resolve.complexExpression.dsl

import icu.windea.pls.core.util.TypedTuple2
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import kotlin.reflect.KClass

/**
 * 复杂表达式的 DSL 的节点。存储了必要的数据，以验证节点的类型、文本、文本范围与结构。
 */
data class ParadoxComplexExpressionDslNode(
    val type: KClass<out ParadoxComplexExpressionNode>,
    val text: String,
    val rangeInExpression: TypedTuple2<Int>,
    val nodes: MutableList<ParadoxComplexExpressionDslNode>
)

inline fun <reified T : ParadoxComplexExpression> buildComplexExpression(
    text: String,
    rangeInExpression: TypedTuple2<Int>,
    block: ParadoxComplexExpressionDslNode.() -> Unit = {}
): ParadoxComplexExpressionDslNode {
    val node = ParadoxComplexExpressionDslNode(T::class, text, rangeInExpression, mutableListOf())
    block(node)
    return node
}

inline fun <reified T : ParadoxComplexExpressionNode> ParadoxComplexExpressionDslNode.node(
    text: String,
    rangeInExpression: TypedTuple2<Int>,
    block: ParadoxComplexExpressionDslNode.() -> Unit = {}
): ParadoxComplexExpressionDslNode {
    val node = ParadoxComplexExpressionDslNode(T::class, text, rangeInExpression, mutableListOf())
    block(node)
    nodes += node
    return node
}
