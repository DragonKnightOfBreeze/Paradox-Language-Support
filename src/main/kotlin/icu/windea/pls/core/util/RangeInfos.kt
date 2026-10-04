package icu.windea.pls.core.util

import icu.windea.pls.core.cache.CacheBuilder

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
        private val cache = CacheBuilder("maximumSize=1000").build<String, IntRangeInfo>()

        /**
         * 从表达式字符串直接解析并创建实例。不对结果进行缓存。
         */
        @JvmStatic
        fun create(expression: String): IntRangeInfo? {
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

        /**
         * 从表达式字符串解析实例，并对成功解析的结果进行缓存和去重。
         */
        @JvmStatic
        fun from(expression: String): IntRangeInfo? {
            cache.getIfPresent(expression)?.let { return it }
            val result = create(expression) ?: return null
            cache.put(expression, result)
            return result
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
        private val cache = CacheBuilder("maximumSize=1000").build<String, FloatRangeInfo>()

        /**
         * 从表达式字符串直接解析并创建实例。不对结果进行缓存。
         */
        @JvmStatic
        fun create(expression: String): FloatRangeInfo? {
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

        /**
         * 从表达式字符串解析实例，并对成功解析的结果进行缓存和去重。
         */
        @JvmStatic
        fun from(expression: String): FloatRangeInfo? {
            cache.getIfPresent(expression)?.let { return it }
            val result = create(expression) ?: return null
            cache.put(expression, result)
            return result
        }
    }
}
