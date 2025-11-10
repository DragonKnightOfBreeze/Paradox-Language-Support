@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:Optimized

package icu.windea.pls.core.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import icu.windea.pls.core.annotations.Optimized

/** 单值包装器：提供从单值到集合/序列的便捷转换。*/
@JvmInline
value class SingletonValue<T>(val value: T)

/** 将任意值包装为 [SingletonValue]。*/
inline val <T> T.singleton get() = SingletonValue(this)

/** 转换为单元素 `List`。*/
inline fun <T> SingletonValue<T>.list() = listOf(value)

/** 转换为单元素 `List`。*/
inline fun <T : Any> SingletonValue<T>.list() = ImmutableList.of(value)

/** 若值非空则转换为单元素 `List`，否则返回空列表。*/
inline fun <T> SingletonValue<T>.listOrEmpty() = if (value != null) ImmutableList.of(value) else emptyList()

/** 转换为单元素 `Set`。*/
inline fun <T> SingletonValue<T>.set() = setOf(value)

/** 转换为单元素 `Set`。*/
inline fun <T : Any> SingletonValue<T>.set() = ImmutableSet.of(value)

/** 若值非空则转换为单元素 `Set`，否则返回空集合。*/
inline fun <T> SingletonValue<T>.setOrEmpty() = if (value != null) ImmutableSet.of(value) else emptySet()

/** 转换为单元素 `Sequence`。*/
inline fun <T> SingletonValue<T>.sequence() = sequenceOf(value)

/** 若值非空则转换为单元素 `Sequence`，否则返回空序列。*/
inline fun <T> SingletonValue<T>.sequenceOrEmpty() = if (value != null) sequenceOf(value) else emptySequence()
