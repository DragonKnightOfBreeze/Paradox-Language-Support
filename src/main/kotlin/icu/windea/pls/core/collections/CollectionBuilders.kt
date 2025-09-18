package icu.windea.pls.core.collections

import icu.windea.pls.core.isNotNullOrEmpty
import java.util.*

/**
 * 创建一个可变集合（[MutableSet]）。
 *
 * - 若提供 [comparator]，则返回基于 [TreeSet] 的有序集合；
 * - 否则返回标准库默认实现的可变集合。
 */
fun <T> MutableSet(comparator: Comparator<T>? = null): MutableSet<T> {
    return if (comparator == null) mutableSetOf() else TreeSet(comparator)
}

// inline fun <reified T : Enum<T>> enumSetOf(vararg values: T): EnumSet<T> {
//    return EnumSet.noneOf(T::class.java).apply { values.forEach { add(it) } }
// }

/**
 * 将多个集合合并到 [destination]，忽略 `null` 或空集合，返回 [destination] 本身。
 */
fun <T : Any, C : MutableCollection<T>> mergeTo(destination: C, vararg collections: Collection<T>?): C {
    collections.forEach { collection ->
        if (collection.isNotNullOrEmpty()) destination.addAll(collection)
    }
    return destination
}

/**
 * 合并多个集合得到新的 [List]，忽略 `null` 或空集合。
 */
fun <T : Any> merge(vararg collections: Collection<T>?): List<T> {
    return mergeTo(ArrayList(), *collections)
}
