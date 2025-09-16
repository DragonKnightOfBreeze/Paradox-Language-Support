package icu.windea.pls.config.configExpression

import icu.windea.pls.config.configExpression.impl.CwtCardinalityExpressionResolverImpl

/**
 * CWT 基数表达式。
 *
 * 用于约束定义成员的出现次数，驱动代码检查、代码补全等功能。
 * `min..max` 表示允许的出现次数范围，`~` 为宽松标记，`inf` 表示无限。
 *
 * 适用对象：`## cardinality` 选项的值。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 *  * ## cardinality = 0..1
 *  * ## cardinality = 0..inf
 *  * ## cardinality = ~1..10
 * ```
 *
 * @property min 最小值。
 * @property max 最大值，null表示无限。
 * @property relaxMin 宽松标记。当为 `true` 时，小于最小值仅视为（弱）警告而非错误。
 * @property relaxMax 宽松标记。当为 `true` 时，大于最大值仅视为（弱）警告而非错误。
 *
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.cardinality
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
