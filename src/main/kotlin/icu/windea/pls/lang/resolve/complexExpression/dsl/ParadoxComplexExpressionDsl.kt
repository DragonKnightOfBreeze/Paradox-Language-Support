package icu.windea.pls.lang.resolve.complexExpression.dsl

import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import kotlin.reflect.KClass

/**
 * 复杂表达式的 DSL，用于对其结构进行严格的验证。
 */
@DslMarker
annotation class ParadoxComplexExpressionDsl

/**
 * 复杂表达式的 DSL 的节点。存储了必要的数据，以验证节点的类型、文本、文本范围与结构。
 */
data class ParadoxComplexExpressionDslNode(
    val type: KClass<out ParadoxComplexExpressionNode>,
    val text: String,
    val rangeInExpression: IntRange,
    val nodes: MutableList<ParadoxComplexExpressionDslNode>
)

object ParadoxComplexExpressionDslBuilder {
    @ParadoxComplexExpressionDsl
    inline fun <reified T : ParadoxComplexExpression> buildExpression(
        text: String,
        rangeInExpression: IntRange,
        block: ParadoxComplexExpressionDslNode.() -> Unit = {}
    ): ParadoxComplexExpressionDslNode {
        val node = ParadoxComplexExpressionDslNode(T::class, text, rangeInExpression, mutableListOf())
        block(node)
        return node
    }
}

@ParadoxComplexExpressionDsl
inline fun <reified T : ParadoxComplexExpression> ParadoxComplexExpressionDslNode.expression(
    text: String,
    rangeInExpression: IntRange,
    block: ParadoxComplexExpressionDslNode.() -> Unit = {}
): ParadoxComplexExpressionDslNode {
    val node = ParadoxComplexExpressionDslNode(T::class, text, rangeInExpression, mutableListOf())
    block(node)
    nodes += node
    return node
}

@ParadoxComplexExpressionDsl
inline fun <reified T : ParadoxComplexExpressionNode> ParadoxComplexExpressionDslNode.node(
    text: String,
    rangeInExpression: IntRange,
    block: ParadoxComplexExpressionDslNode.() -> Unit = {}
): ParadoxComplexExpressionDslNode {
    val node = ParadoxComplexExpressionDslNode(T::class, text, rangeInExpression, mutableListOf())
    block(node)
    nodes += node
    return node
}
