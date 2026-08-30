package icu.windea.pls.core.math

object NumberConverter {
    inline fun convertIntToInt(input: String?, defaultValue: Int, range: ClosedRange<Int>? = null, transform: (Int) -> Int = { it }): Int {
        return convertIntToInt(input?.toIntOrNull(), defaultValue, range, transform)
    }

    inline fun convertIntToInt(input: Int?, defaultValue: Int, range: ClosedRange<Int>? = null, transform: (Int) -> Int = { it }): Int {
        return input?.let { transform(it) }?.let { if (range != null) it.coerceIn(range) else it } ?: defaultValue
    }

    inline fun convertFloatToInt(input: String?, defaultValue: Int, range: ClosedRange<Int>? = null, transform: (Float) -> Float = { it }): Int {
        return convertFloatToInt(input?.toFloatOrNull(), defaultValue, range, transform)
    }

    inline fun convertFloatToInt(input: Float?, defaultValue: Int, range: ClosedRange<Int>? = null, transform: (Float) -> Float = { it }): Int {
        return input?.let { transform(it) }?.toInt()?.let { if (range != null) it.coerceIn(range) else it } ?: defaultValue
    }

    inline fun convertIntToFloat(input: String?, defaultValue: Float, range: ClosedRange<Float>? = null, transform: (Float) -> Float = { it }): Float {
        return convertIntToFloat(input?.toIntOrNull(), defaultValue, range, transform)
    }

    inline fun convertIntToFloat(input: Int?, defaultValue: Float, range: ClosedRange<Float>? = null, transform: (Float) -> Float = { it }): Float {
        return input?.toFloat()?.let { transform(it) }?.let { if (range != null) it.coerceIn(range) else it } ?: defaultValue
    }

    inline fun convertFloatToFloat(input: String?, defaultValue: Float, range: ClosedRange<Float>? = null, transform: (Float) -> Float = { it }): Float {
        return convertFloatToFloat(input?.toFloatOrNull(), defaultValue, range, transform)
    }

    inline fun convertFloatToFloat(input: Float?, defaultValue: Float, range: ClosedRange<Float>? = null, transform: (Float) -> Float = { it }): Float {
        return input?.let { transform(it) }?.let { if (range != null) it.coerceIn(range) else it } ?: defaultValue
    }
}
