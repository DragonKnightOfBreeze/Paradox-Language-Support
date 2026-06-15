@file:Suppress("unused")

package icu.windea.pls.lang.resolve.complexExpression.dsl

import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import kotlin.reflect.KClass

sealed interface ParadoxComplexExpressionDsl

/**
 * 复杂表达式的 DSL 的存根。作为占位符。
 */
data class ParadoxComplexExpressionDslStub(
    val type: KClass<out ParadoxComplexExpressionNode>,
    val message: String
) : ParadoxComplexExpressionDsl

/**
 * 复杂表达式的 DSL 的节点。存储了必要的数据，以验证节点的类型、文本、文本范围与结构。
 */
data class ParadoxComplexExpressionDslNode(
    val type: KClass<out ParadoxComplexExpressionNode>,
    val text: String,
    val startOffset: Int,
    val endOffset: Int,
    val nodes: MutableList<ParadoxComplexExpressionDslNode>
) : ParadoxComplexExpressionDsl

inline fun <reified T : ParadoxComplexExpression> buildComplexExpressionStub(
    message: String
): ParadoxComplexExpressionDslStub {
    return ParadoxComplexExpressionDslStub(T::class, message)
}

inline fun <reified T : ParadoxComplexExpression> buildComplexExpression(
    text: String,
    startOffset: Int,
    endOffset: Int,
    block: ParadoxComplexExpressionDslNode.() -> Unit = {}
): ParadoxComplexExpressionDslNode {
    val node = ParadoxComplexExpressionDslNode(T::class, text, startOffset, endOffset, mutableListOf())
    block(node)
    return node
}

inline fun <reified T : ParadoxComplexExpressionNode> ParadoxComplexExpressionDslNode.node(
    text: String,
    startOffset: Int,
    endOffset: Int,
    block: ParadoxComplexExpressionDslNode.() -> Unit = {}
): ParadoxComplexExpressionDslNode {
    val node = ParadoxComplexExpressionDslNode(T::class, text, startOffset, endOffset, mutableListOf())
    block(node)
    nodes += node
    return node
}
