package icu.windea.pls.core.util

/**
 * 区间信息。
 */
interface RangeInfo<T : Comparable<T>> {
    val start: T?
    val end: T?
    val openStart: Boolean
    val openEnd: Boolean

    val expression: String

    operator fun contains(value: T): Boolean
}

/**
 * 整数的区间信息。
 */
data class IntRangeInfo(
    override val start: Int?,
    override val end: Int?,
    override val openStart: Boolean,
    override val openEnd: Boolean
) : RangeInfo<Int> {
    private val prefix = if (openStart) "(" else "["
    private val suffix = if (openEnd) ")" else "]"
    override val expression: String = "$prefix$start..$end$suffix"

    override fun contains(value: Int): Boolean {
        val r1 = when {
            start == null -> true
            openStart -> value > start
            else -> value >= start
        }
        if (!r1) return false
        val r2 = when {
            end == null -> true
            openEnd -> value < end
            else -> value <= end
        }
        if (!r2) return false
        return true
    }

    override fun toString() = expression

    companion object {
        @JvmStatic
        fun from(expression: String): IntRangeInfo? {
            if (expression.length <= 2) return null
            val openStart = when (expression.first()) {
                '(' -> true
                '[' -> false
                else -> return null
            }
            val openEnd = when (expression.last()) {
                ')' -> true
                ']' -> false
                else -> return null
            }
            val values = expression.substring(1, expression.length - 1).trim().split("..", limit = 2)
            val start = values.getOrNull(0)?.trim()?.toIntOrNull()
            val end = values.getOrNull(1)?.trim()?.toIntOrNull()
            return IntRangeInfo(start, end, openStart, openEnd)
        }
    }
}

/**
 * 浮点数的区间信息。
 */
data class FloatRangeInfo(
    override val start: Float?,
    override val end: Float?,
    override val openStart: Boolean,
    override val openEnd: Boolean
) : RangeInfo<Float> {
    private val prefix = if (openStart) "(" else "["
    private val suffix = if (openEnd) ")" else "]"
    override val expression: String = "$prefix$start..$end$suffix"

    override fun contains(value: Float): Boolean {
        val r1 = when {
            start == null -> true
            openStart -> value > start
            else -> value >= start
        }
        if (!r1) return false
        val r2 = when {
            end == null -> true
            openEnd -> value < end
            else -> value <= end
        }
        if (!r2) return false
        return true
    }

    override fun toString() = expression

    companion object {
        @JvmStatic
        fun from(expression: String): FloatRangeInfo? {
            if (expression.length <= 2) return null
            val openStart = when (expression.first()) {
                '(' -> true
                '[' -> false
                else -> return null
            }
            val openEnd = when (expression.last()) {
                ')' -> true
                ']' -> false
                else -> return null
            }
            val values = expression.substring(1, expression.length - 1).trim().split("..", limit = 2)
            val start = values.getOrNull(0)?.trim()?.toFloatOrNull()
            val end = values.getOrNull(1)?.trim()?.toFloatOrNull()
            return FloatRangeInfo(start, end, openStart, openEnd)
        }
    }
}
