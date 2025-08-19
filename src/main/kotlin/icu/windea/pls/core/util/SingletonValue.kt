@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

@JvmInline
value class SingletonValue<T>(val value: T)

inline val <T> T.singleton get() = SingletonValue(this)

inline fun <T> SingletonValue<T>.list() = listOf(value)

inline fun <T> SingletonValue<T>.listOrEmpty() = if (value != null) listOf(value) else emptyList()

inline fun <T> SingletonValue<T>.set() = setOf(value)

inline fun <T> SingletonValue<T>.setOrEmpty() = if (value != null) setOf(value) else emptySet()

inline fun <T> SingletonValue<T>.sequence() = sequenceOf(value)

inline fun <T> SingletonValue<T>.sequenceOrEmpty() = if (value != null) sequenceOf(value) else emptySequence()
