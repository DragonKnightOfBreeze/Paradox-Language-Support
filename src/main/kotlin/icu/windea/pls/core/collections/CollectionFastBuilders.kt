@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core.collections

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import icu.windea.pls.core.annotations.Fast

/**
 * 创建一个期望大小为 [expectedSize] 的不可变列表，并通过 [init] 初始化。
 */
@Fast
inline fun <T : Any> ImmutableList(expectedSize: Int, init: (index: Int) -> T): List<T> {
    require(expectedSize >= 0) { "expectedSize must be non-negative" }
    if (expectedSize == 0) return ImmutableList.of() // optimize: fast return
    if (expectedSize == 1) return ImmutableList.of(init(0)) // optimize: fast return
    val builder = ImmutableList.builderWithExpectedSize<T>(expectedSize)
    repeat(expectedSize) { index -> builder.add(init(index)) }
    return builder.build()
}

/**
 * 创建一个期望大小为 [expectedSize] 的不可变集，并通过 [init] 初始化。
 */
@Fast
inline fun <T : Any> ImmutableSet(expectedSize: Int, init: (index: Int) -> T): Set<T> {
    require(expectedSize >= 0) { "expectedSize must be non-negative" }
    if (expectedSize == 0) return ImmutableSet.of() // optimize: fast return
    if (expectedSize == 1) return ImmutableSet.of(init(0)) // optimize: fast return
    val builder = ImmutableSet.builderWithExpectedSize<T>(expectedSize)
    repeat(expectedSize) { index -> builder.add(init(index)) }
    return builder.build()
}
