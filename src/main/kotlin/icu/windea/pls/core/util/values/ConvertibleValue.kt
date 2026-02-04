@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.util.values

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet

/**
 * 可以转化的值（包装类）。
 *
 * 说明：
 * - 使用内联类实现，避免运行时开销。同时扩展方法也尽可能地使用内联函数实现。
 */
@JvmInline
value class ConvertibleValue<T>(val value: T)

inline val <T> T.to get() = ConvertibleValue(this)

inline fun <T> ConvertibleValue<T>.singletonList() = listOf(value)

inline fun <T : Any> ConvertibleValue<T>.singletonList() = ImmutableList.of(value)

inline fun <T> ConvertibleValue<T>.singletonListOrEmpty() = if (value != null) ImmutableList.of(value) else emptyList()

inline fun <T> ConvertibleValue<T>.singletonSet() = setOf(value)

inline fun <T : Any> ConvertibleValue<T>.singletonSet() = ImmutableSet.of(value)

inline fun <T> ConvertibleValue<T>.singletonSetOrEmpty() = if (value != null) ImmutableSet.of(value) else emptySet()

inline fun <T : Pair<K, V>, K, V> ConvertibleValue<T>.singletonMap() = mapOf(value)

inline fun <T : Pair<K, V>, K : Any, V : Any> ConvertibleValue<T>.singletonMap() = ImmutableMap.of(value.first, value.second)

inline fun <T> ConvertibleValue<T>.singletonSequence() = sequenceOf(value)

inline fun <T> ConvertibleValue<T>.singletonSequenceOrEmpty() = if (value != null) sequenceOf(value) else emptySequence()
