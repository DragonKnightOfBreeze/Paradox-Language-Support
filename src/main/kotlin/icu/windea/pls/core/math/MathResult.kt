package icu.windea.pls.core.math

import kotlin.math.roundToLong

/**
 * 数学结果。
 *
 * @property value 值。使用双精度浮点数表示。
 * @property precision 精确度。用于格式化。
 * @property isFloatingPoint 是否是浮点数。用于规范化和格式化。
 */
data class MathResult(
    var value: Double,
    var precision: Int = -3,
    var isFloatingPoint: Boolean = true,
) {
    fun isFloatingPointValue(): Boolean {
        return value.toLong().toDouble() == value
    }

    fun normalized(): Number {
        return if (isFloatingPoint) value else value.roundToLong()
    }

    fun formatted(): String {
        return value.formatted(precision, isFloatingPoint)
    }
}
