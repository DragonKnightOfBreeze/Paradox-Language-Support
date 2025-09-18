@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util

@JvmInline
value class FallbackValue<T>(val value: T)

/** 为可空字符串提供链式“后备值”语义的入口属性。*/
inline val String?.or get() = FallbackValue(this)

/** 若原值非空且非空串则返回原值，否则返回 [fallback]。*/
inline fun FallbackValue<String?>.fallback(fallback: String) = if (!value.isNullOrEmpty()) value else fallback

/** 若原值为空或空串则返回 "(anonymous)"。*/
inline fun FallbackValue<String?>.anonymous() = fallback("(anonymous)")

/** 若原值为空或空串则返回 "(unknown)"。*/
inline fun FallbackValue<String?>.unknown() = fallback("(unknown)")

/** 若原值为空或空串则返回 "(unresolved)"。*/
inline fun FallbackValue<String?>.unresolved() = fallback("(unresolved)")
