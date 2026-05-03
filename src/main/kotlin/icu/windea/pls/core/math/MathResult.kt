package icu.windea.pls.core.math

import kotlin.math.roundToLong

/**
 * 数学结果。
 *
 * @property value 值。使用双精度浮点数表示。
 * @property precision 精确度。用于格式化。
 * @property isFloatingPoint 是否是浮点数。用于规范化和格式化。
 */
@Suppress("unused")
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

    companion object {
        fun fromIntString(text: String): MathResult? {
            return text.toIntOrNull()?.let { MathResult(it.toDouble(), isFloatingPoint = false) }
        }

        fun fromLongString(text: String): MathResult? {
            return text.toLongOrNull()?.let { MathResult(it.toDouble(), isFloatingPoint = false) }
        }

        fun fromFloatString(text: String): MathResult? {
            return text.toFloatOrNull()?.takeIf { it.isFinite() }?.let { MathResult(it.toDouble(), isFloatingPoint = true) }
        }

        fun fromDoubleString(text: String): MathResult? {
            return text.toDoubleOrNull()?.takeIf { it.isFinite() }?.let { MathResult(it, isFloatingPoint = true) }
        }
    }
}
