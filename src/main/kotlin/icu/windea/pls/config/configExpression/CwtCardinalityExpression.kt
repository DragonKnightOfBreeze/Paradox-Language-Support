package icu.windea.pls.config.configExpression

import icu.windea.pls.config.configExpression.impl.CwtCardinalityExpressionResolverImpl

/**
 * CWT基数表达式。
 *
 * 示例：
 *
 * ```
 * ## cardinality = 0..1
 * ## cardinality = 0..inf
 * ## cardinality = ~1..10
 * ```
 *
 * @property min 最小值。
 * @property max 最大值，null表示无限。
 * @property relaxMin 如果值为`false`，则当实际数量小于最小值时仅会作出（弱）警告。
 * @property relaxMax 如果值为`false`，则当实际数量大于最大值时仅会作出（弱）警告。
 */
interface CwtCardinalityExpression : CwtConfigExpression {
    val min: Int
    val max: Int?
    val relaxMin: Boolean
    val relaxMax: Boolean

    operator fun component1() = min
    operator fun component2() = max
    operator fun component3() = relaxMin
    operator fun component4() = relaxMax

    fun isRequired() = min > 0

    interface Resolver {
        fun resolveEmpty(): CwtCardinalityExpression
        fun resolve(expressionString: String): CwtCardinalityExpression
    }

    companion object : Resolver by CwtCardinalityExpressionResolverImpl()
}
