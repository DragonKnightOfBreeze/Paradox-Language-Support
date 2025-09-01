package icu.windea.pls.core.collections

import icu.windea.pls.core.isNotNullOrEmpty
import java.util.*

/**
 * 创建可变集合。
 *
 * @param comparator 指定时使用 `TreeSet`（按比较器有序），否则使用默认可变集合类型
 */
fun <T> MutableSet(comparator: Comparator<T>? = null): MutableSet<T> {
    return if (comparator == null) mutableSetOf() else TreeSet(comparator)
}

//inline fun <reified T : Enum<T>> enumSetOf(vararg values: T): EnumSet<T> {
//    return EnumSet.noneOf(T::class.java).apply { values.forEach { add(it) } }
//}

/**
 * 合并多个集合到 [destination]（忽略 null 或空集合）。
 */
fun <T : Any, C : MutableCollection<T>> mergeTo(destination: C, vararg collections: Collection<T>?): C {
    collections.forEach { collection ->
        if (collection.isNotNullOrEmpty()) destination.addAll(collection)
    }
    return destination
}

/**
 * 合并多个集合为新的 `List`（忽略 null 或空集合）。
 */
fun <T : Any> merge(vararg collections: Collection<T>?): List<T> {
    return mergeTo(ArrayList(), *collections)
}
