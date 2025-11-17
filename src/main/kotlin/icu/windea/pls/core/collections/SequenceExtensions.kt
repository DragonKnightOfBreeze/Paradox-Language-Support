@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused")

package icu.windea.pls.core.collections

/** 将类型为 [R] 且满足 [predicate] 的元素过滤为序列。 */
inline fun <reified R> Sequence<*>.filterIsInstance(noinline predicate: (R) -> Boolean): Sequence<R> {
    return filter { it is R && predicate(it) } as Sequence<R>
}

/** 将类型为 [R] 且满足 [predicate] 的元素过滤到指定的 [destination]。 */
inline fun <reified R, C : MutableCollection<in R>> Sequence<*>.filterIsInstanceTo(destination: C, noinline predicate: (R) -> Boolean): C {
    for (element in this) if (element is R && predicate(element)) destination.add(element)
    return destination
}

/** 查找第一个类型为 [R] 且满足 [predicate] 的元素。*/
inline fun <reified R> Sequence<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return find { it is R && predicate(it) } as R?
}

/** 将当前序列映射为数组。注意：会先转为列表。*/
inline fun <T, reified R> Sequence<T>.mapToArray(transform: (T) -> R): Array<R> {
    return toList().mapToArray(transform)
}

/** 逐个处理元素，若处理函数 [processor] 返回 `false` 则提前终止并返回 `false`。*/
fun <T> Sequence<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}
