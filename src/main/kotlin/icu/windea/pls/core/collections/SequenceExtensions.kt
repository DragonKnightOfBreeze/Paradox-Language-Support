@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

/** 过滤为 [R] 类型并附加谓词 [predicate]。*/
inline fun <reified R> Sequence<*>.filterIsInstance(noinline predicate: (R) -> Boolean): Sequence<R> {
    return filterIsInstance(R::class.java, predicate)
}

/** 过滤为 [klass] 类型并附加谓词 [predicate]。*/
inline fun <R> Sequence<*>.filterIsInstance(klass: Class<R>, noinline predicate: (R) -> Boolean): Sequence<R> {
    return filterIsInstance(klass).filter(predicate)
}

/** 查找第一个为 [R] 类型且满足 [predicate] 的元素。*/
inline fun <reified R> Sequence<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
/** 查找第一个为 [klass] 类型且满足 [predicate] 的元素。*/
inline fun <R> Sequence<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) return element
    return null
}

/** 将序列映射为数组。注意：会收集到 [List] 后再转换为数组。*/
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
