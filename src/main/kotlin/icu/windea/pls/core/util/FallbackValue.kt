@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

@JvmInline
value class FallbackValue<T>(val value: T)

inline val String?.or get() = FallbackValue(this)

inline fun FallbackValue<String?>.fallback(fallback: String) = if (!value.isNullOrEmpty()) value else fallback

inline fun FallbackValue<String?>.anonymous() = fallback("(anonymous)")

inline fun FallbackValue<String?>.unknown() = fallback("(unknown)")

inline fun FallbackValue<String?>.unresolved() = fallback("(unresolved)")
