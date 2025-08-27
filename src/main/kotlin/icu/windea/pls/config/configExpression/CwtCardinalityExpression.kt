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
 * 语义：`min..max` 表示允许的出现次数范围；`~` 为“宽松”标记（越界仅发出警告而非错误）；`inf` 表示无限。
 * 该表达式常见于 `.cwt` 规则的注释或元数据中，用于约束后续键值或块的出现次数。
 *
 * @property min 最小值。
 * @property max 最大值，null表示无限。
 * @property relaxMin 当为 `true` 时，小于最小值仅作出警告（宽松，不视为错误）。
 * @property relaxMax 当为 `true` 时，大于最大值仅作出警告（宽松，不视为错误）。
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
