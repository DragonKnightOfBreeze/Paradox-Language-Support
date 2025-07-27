@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util

@JvmInline
value class SingletonElement<T>(
    val element: T
)

inline fun <T> T.singleton() = SingletonElement(this)

inline fun <T> SingletonElement<T>.list() = listOf(element)

inline fun <T> SingletonElement<T>.set() = setOf(element)

inline fun <T> SingletonElement<T>.listOrEmpty() = if (element == null) emptyList() else listOf(element)

inline fun <T> SingletonElement<T>.setOrEmpty() = if (element == null) emptySet() else setOf(element)

inline fun <T> SingletonElement<T>.sequence() = sequenceOf(element)

inline fun <T> SingletonElement<T>.sequenceOrEmpty() = if (element == null) emptySequence() else sequenceOf(element)
