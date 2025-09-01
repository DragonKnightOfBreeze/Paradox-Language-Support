@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

/** 过滤并仅保留类型为 R 且满足谓词的元素，返回 Sequence。 */
inline fun <reified R> Sequence<*>.filterIsInstance(noinline predicate: (R) -> Boolean): Sequence<R> {
    return filterIsInstance(R::class.java, predicate)
}

/** 过滤并仅保留类型为 [klass] 且满足谓词的元素，返回 Sequence。 */
inline fun <R> Sequence<*>.filterIsInstance(klass: Class<R>, noinline predicate: (R) -> Boolean): Sequence<R> {
    return filterIsInstance(klass).filter(predicate)
}

/** 查找首个类型为 R 且满足谓词的元素。 */
inline fun <reified R> Sequence<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
/** 查找首个类型为 [klass] 且满足谓词的元素。 */
inline fun <R> Sequence<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) return element
    return null
}

/** 将序列按转换函数映射为新数组。 */
inline fun <T, reified R> Sequence<T>.mapToArray(transform: (T) -> R): Array<R> {
    return toList().mapToArray(transform)
}

/** 逐个处理元素，处理器返回 false 时提前终止。 */
fun <T> Sequence<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}
