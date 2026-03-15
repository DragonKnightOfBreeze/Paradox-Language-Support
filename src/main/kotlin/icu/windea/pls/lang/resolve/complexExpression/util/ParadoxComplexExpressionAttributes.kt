package icu.windea.pls.lang.resolve.complexExpression.util

import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression

/**
 * 复杂表达式的综合属性。
 *
 * @see ParadoxComplexExpression
 * @see ParadoxComplexExpressionAttributesEvaluator
 */
object ParadoxComplexExpressionAttributes {
    /** 涉及动态数据。例如，涉及动态值（如 `event_target`）。 */
    const val DYNAMIC_DATA_AWARE = 0x1
    /** 涉及动态数据，并且存在另一种更精确的格式。例如，存在另一种带前缀或带参数的格式。 */
    const val RELAX_DYNAMIC_DATA_AWARE = 0x2
}
