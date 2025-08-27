package icu.windea.pls.config.configExpression

/**
 * CWT位置表达式。用于定位目标的来源。
 *
 * @property location 位置表达式。
 * @property isPlaceholder 位置表达式是否是占位符（包含`$`）。
 */
interface CwtLocationExpression : CwtConfigExpression {
    val location: String
    val isPlaceholder: Boolean

    operator fun component1() = location
    operator fun component2() = isPlaceholder
}
