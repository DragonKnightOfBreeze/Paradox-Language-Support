@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util.values

/**
 * 可以回退的值（包装类）。
 *
 * 说明：
 * - 使用内联类实现，避免运行时开销。同时扩展方法也尽可能地使用内联函数实现。
 */
@JvmInline
value class FallbackValue<T>(val value: T)

inline val String?.or get() = FallbackValue(this)

object FallbackStrings {
    const val anonymous = "(anonymous)"
    const val unknown = "(unknown)"
    const val unresolved = "(unresolved)"
}

inline fun FallbackValue<String?>.anonymous() = if (value.isNullOrEmpty()) FallbackStrings.anonymous else value

inline fun FallbackValue<String?>.unknown() = if (value.isNullOrEmpty()) FallbackStrings.unknown else value

inline fun FallbackValue<String?>.unresolved() = if (value.isNullOrEmpty()) FallbackStrings.unresolved else value

