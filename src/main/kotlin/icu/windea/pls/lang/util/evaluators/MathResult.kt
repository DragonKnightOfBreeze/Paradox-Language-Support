package icu.windea.pls.lang.util.evaluators

import icu.windea.pls.core.formatted

data class MathResult(
    var value: Float,
    var isInt: Boolean,
    var precision: Int = -3, // 默认保留3位小数
) {
    fun normalized(): Number {
        return if (isInt) value.toInt() else value
    }

    fun formatted(): String {
        return if (isInt) value.toInt().toString() else value.formatted(precision)
    }
}
