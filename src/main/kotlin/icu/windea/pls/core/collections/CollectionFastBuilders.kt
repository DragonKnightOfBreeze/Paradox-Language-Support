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
 * ## 实现笔记
 *
 * - 0/1 元素直接返回 `ImmutableList.of(...)`，避免创建中间数组。
 * - 其余情况先分配定长数组，再通过 [asImmutableList] 零拷贝包装。
 * - 基准中 64 元素约比 `List(size)` 快 20%～25%，但参考实现返回可变列表，二者语义不完全等价。
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
 * ## 实现笔记
 *
 * - 0/1 元素直接返回 `ImmutableSet.of(...)`，通常比 `buildSet` 更快。
 * - 4 元素以上使用 Guava builder，基准中约慢 7~8.7 倍。
 * - 此方法当前未在生产代码中使用。
 *
 * ## 弃用声明
 *
 * 应改为使用标准的 `buildSet`，因为标准实现在这类构建场景中更轻量。
 */
@Deprecated(
    message = "Use buildSet instead; this builder is slower for four or more elements.",
    replaceWith = ReplaceWith("buildSet(expectedSize) { repeat(expectedSize) { index -> add(init(index)) } }"),
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
 * 将当前数组零拷贝包装为不可变列表。如果当前数组为空，则直接返回空列表。
 *
 * ## 实现笔记
 *
 * - 0 元素直接返回 `emptyList()`，避免创建列表对象。
 * - 其余情况使用 `ObjectImmutableList` 直接包装数组，不复制元素。
 * - 非空时通常远快于 `toList()`。
 * - 调用方不得在包装后继续修改原数组，否则会破坏不可变语义。
 */
@Fast
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
inline fun <T : Any> Array<Any?>.asImmutableList(): List<T> {
    if (isEmpty()) return emptyList()
    this as Array<out T>
    // return (this as Array<out T>).asList() // memory usage: 24 + align8(16 + 4n)
    // @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog") return java.util.List.of(*this) // memory usage: 24 + align8(16 + 4n)
    // return ImmutableList.copyOf(this) // memory usage: 16 + align8(16 + 4n) // note: this will copy the input array
    return ObjectImmutableList(this) // memory usage: 16 + align8(16 + 4n)
}
