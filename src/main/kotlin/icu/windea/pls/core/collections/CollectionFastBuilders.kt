@file:Suppress("unused")
@file:Fast

package icu.windea.pls.core.collections

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import icu.windea.pls.core.annotations.Fast
import it.unimi.dsi.fastutil.objects.ObjectImmutableList

/**
 * 创建一个期望大小为 [expectedSize] 的不可变列表，并通过 [init] 初始化。
 *
 * 性能笔记：0/1 元素走快速返回；64 元素时基准中约比 `List(size)` 快 20%～25%。
 * 注意参考实现返回可变列表，二者语义不完全等价。
 */
@Fast
inline fun <T : Any> buildImmutableList(expectedSize: Int, init: (index: Int) -> T): List<T> {
    require(expectedSize >= 0) { "expectedSize must be non-negative" }
    if (expectedSize == 0) return ImmutableList.of() // optimize: fast return
    if (expectedSize == 1) return ImmutableList.of(init(0)) // optimize: fast return
    val elements = arrayOfNulls<Any?>(expectedSize) // optimize: construct sized array directly for better performance and memory
    repeat(expectedSize) { index -> elements[index] = init(index) }
    return elements.asImmutableList()
}

/**
 * 创建一个期望大小为 [expectedSize] 的不可变集，并通过 [init] 初始化。
 *
 * 性能笔记：
 * - 0/1 元素走快速返回，通常比 `buildSet` 更快；
 * - 4 元素以上使用 Guava builder，基准中约慢 7～8.7 倍；
 * - 该方法当前未在生产代码中使用，推荐改用标准 `buildSet`。
 */
@Deprecated(
    message = "Use buildSet instead; this builder is slower for four or more elements.",
    replaceWith = ReplaceWith(
        "buildSet(expectedSize) { repeat(expectedSize) { index -> add(init(index)) } }",
        "kotlin.collections.buildSet",
    ),
    level = DeprecationLevel.ERROR,
)
@Fast
inline fun <T : Any> buildImmutableSet(expectedSize: Int, init: (index: Int) -> T): Set<T> {
    require(expectedSize >= 0) { "expectedSize must be non-negative" }
    if (expectedSize == 0) return ImmutableSet.of() // optimize: fast return
    if (expectedSize == 1) return ImmutableSet.of(init(0)) // optimize: fast return
    val builder = ImmutableSet.builderWithExpectedSize<T>(expectedSize)
    repeat(expectedSize) { index -> builder.add(init(index)) }
    return builder.build()
}

/**
 * 将当前数组零拷贝包装为不可变列表。
 *
 * 性能笔记：非空时通常远快于 `toList()`；空数组时可能更慢。
 * 调用方不得在包装后继续修改原数组。
 */
@Fast
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> Array<Any?>.asImmutableList(): List<T> {
    @Suppress("UNCHECKED_CAST")
    this as Array<out T>
    // return result.asList() // memory usage: 24 + align8(16 + 4n)
    // @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog") return java.util.List.of(*elements) // memory usage: 24 + align8(16 + 4n)
    // return ImmutableList.copyOf(result) // memory usage: 16 + align8(16 + 4n) // note: this will copy the input array
    return ObjectImmutableList(this) // memory usage: 16 + align8(16 + 4n)
}
