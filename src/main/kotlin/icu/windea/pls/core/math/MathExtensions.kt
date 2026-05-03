package icu.windea.pls.core.math

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 对当前数字按照指定位数的精确度进行格式化，返回格式化后的数字字符串。
 *
 * - `digits == 0`：四舍五入到整数部分。
 * - `digits > 0`：四舍五入到整数部分的第 `|digits|` 位。
 * - `digits < 0`：四舍五入到小数点后第 `|digits|` 位。
 *
 * @param precision 精确度。
 * @param isFloatingPoint 是否是浮点数。如果为 `true`，则强制输出为浮点数形式。
 */
fun Number.formatted(precision: Int = -3, isFloatingPoint: Boolean = true): String {
    val number = BigDecimal(this.toString())    // 使用字符串构造，避免精度丢失

    return when {
        precision == 0 -> {
            val result = number.setScale(0, RoundingMode.HALF_UP)
            val output = result.toPlainString()
            if (isFloatingPoint) "$output.0" else output
        }
        precision > 0 -> {
            val factor = BigDecimal.TEN.pow(precision)
            val divided = number.divide(factor, 0, RoundingMode.HALF_UP)
            val result = divided.multiply(factor)
            val output = result.stripTrailingZeros().toPlainString()
            if (isFloatingPoint) "$output.0" else output
        }
        else -> {
            val scale = if (isFloatingPoint) -precision else 0
            val result = number.setScale(scale, RoundingMode.HALF_UP)
            val output = result.stripTrailingZeros().toPlainString()
            if (isFloatingPoint) "$output.0" else output
        }
    }
}
