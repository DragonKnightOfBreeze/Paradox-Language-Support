package icu.windea.pls.lang.resolve.complexExpression.attributes

import com.intellij.util.BitUtil
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode

/**
 * 复杂表达式的综合属性。
 *
 * 用于优化索引时的性能，以及进行更准确的语义解析与匹配。
 *
 * @see ParadoxComplexExpression
 * @see ParadoxComplexExpressionAttributesEvaluator
 */
object ParadoxComplexExpressionAttributes {
    /** 涉及动态数据。例如，涉及动态值（如 `event_target`）。 */
    const val DYNAMIC_DATA_INVOLVED = 0x1
    /** 涉及动态数据，并且存在另一种更精确的格式。例如，存在另一种带前缀或带参数的格式。 */
    const val RELAX_DYNAMIC_DATA_INVOLVED = 0x2

    inline fun check(attributes: Int, provider: ParadoxComplexExpressionAttributes.() -> Int): Boolean {
        return BitUtil.isSet(attributes, ParadoxComplexExpressionAttributes.provider())
    }

    fun get(node: ParadoxComplexExpressionNode): Int {
        return ParadoxComplexExpressionAttributesEvaluator.evaluate(node)
    }
}
