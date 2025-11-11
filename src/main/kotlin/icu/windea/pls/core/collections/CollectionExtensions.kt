@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "unused", "ERROR_SUPPRESSION")

package icu.windea.pls.core.collections

import icu.windea.pls.core.cast
import java.util.*
import kotlin.reflect.KProperty

/** 如果当前集合为 `null` 或为空，则返回 `null`。否则返回自身。*/
inline fun <T : Collection<*>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 如果当前映射为 `null` 或为空，则返回 `null`。否则返回自身。*/
inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将当前只读 [List] 视为 [MutableList]（要求实际可变，否则抛出异常）。*/
inline fun <T> List<T>.asMutable(): MutableList<T> = this as MutableList<T>

/** 将当前只读 [Set] 视为 [MutableSet]（要求实际可变，否则抛出异常）。*/
inline fun <T> Set<T>.asMutable(): MutableSet<T> = this as MutableSet<T>

/** 将当前只读 [Map] 视为 [MutableMap]（要求实际可变，否则抛出异常）。*/
inline fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as MutableMap<K, V>

/** 返回加锁后的同步的 [MutableList]。*/
inline fun <T> MutableList<T>.synced(): MutableList<T> = Collections.synchronizedList(this)

/** 返回加锁后的同步的 [MutableSet]。*/
inline fun <T> MutableSet<T>.synced(): MutableSet<T> = Collections.synchronizedSet(this)

/** 返回加锁后的同步的 [MutableMap]。*/
inline fun <K, V> MutableMap<K, V>.synced(): MutableMap<K, V> = Collections.synchronizedMap(this)

/** 如果当前集合已是 [List]，则直接返回，否则转化为新的 [List]。*/
inline fun <T> Collection<T>.toListOrThis(): List<T> = this as? List ?: this.toList()

/** 如果当前集合已是 [Set]，则直接返回，否则转化为新的 [Set]。*/
inline fun <T> Collection<T>.toSetOrThis(): Set<T> = this as? Set ?: this.toSet()

/** 过滤为 [R] 类型并附加谓词 [predicate]。*/
inline fun <reified R> Iterable<*>.filterIsInstance(predicate: (R) -> Boolean): List<R> {
    return filterIsInstance(R::class.java, predicate)
}

/** 过滤为 [klass] 类型并附加谓词 [predicate]。*/
inline fun <R> Iterable<*>.filterIsInstance(klass: Class<R>, predicate: (R) -> Boolean): List<R> {
    val result = ArrayList<R>()
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) result.add(element)
    return result
}

/** 查找第一个为 [R] 类型且满足 [predicate] 的元素。*/
inline fun <reified R> Iterable<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return find { it is R && predicate(it) } as R?
}

/** 查找第一个为 [klass] 类型且满足 [predicate] 的元素。*/
inline fun <R> Iterable<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    return find { klass.isInstance(it) && predicate(it as R) } as R?
}

/** 查找最后一个为 [R] 类型且满足 [predicate] 的元素。*/
inline fun <reified R> List<*>.findLastIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findLast { it is R && predicate(it) } as R?
}

/** 查找最后一个为 [klass] 类型且满足 [predicate] 的元素。*/
@Suppress("UNCHECKED_CAST")
inline fun <R> List<*>.findLastIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    return findLast { klass.isInstance(it) && predicate(it as R) } as R?
}

/** 将当前列表映射为数组。*/
inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}

/** 将当前集合映射为数组。如果为列表则走下标路径，否则顺序遍历。*/
inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
    if (this is List) return this.mapToArray(transform)
    val result = arrayOfNulls<R>(this.size)
    for ((i, e) in this.withIndex()) {
        result[i] = transform(e)
    }
    return result.cast()
}

/** 将条目映射为数组；在可迭代器场景下避免中间 [List] 分配。*/
inline fun <K, V, reified R> Map<K, V>.mapToArray(transform: (Map.Entry<K, V>) -> R): Array<R> {
    // 这里不先将Set转为List
    val size = this.size
    val entries = this.entries
    try {
        val iterator = entries.iterator()
        return Array(size) { transform(iterator.next()) }
    } catch (e: Exception) {
        val list = entries.toList()
        return Array(size) { transform(list[it]) }
    }
}

/** 逐个处理元素，如果处理函数 [processor] 返回 `false` 则提前终止并返回 `false`。*/
inline fun <T> Iterable<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}

/** 逐个处理条目，如果处理函数 [processor] 返回 `false` 则提前终止并返回 `false`。*/
inline fun <K, V> Map<K, V>.process(processor: (Map.Entry<K, V>) -> Boolean): Boolean {
    for (entry in this) {
        val result = processor(entry)
        if (!result) return false
    }
    return true
}

/**
 * 将满足指定条件（[predicate]）的元素置顶（保持相对顺序），返回处理后的新列表。
 */
inline fun <T> Iterable<T>.pinned(predicate: (T) -> Boolean): List<T> {
    if (this is Collection && this.size <= 1) return this.toList()
    val result = mutableListOf<T>()
    val elementsToPin = mutableListOf<T>()
    for (e in this) {
        if (predicate(e)) {
            elementsToPin += e
        } else {
            result += e
        }
    }
    return elementsToPin + result
}

/**
 * 将满足指定条件（[predicate]）的元素置底（保持相对顺序），返回处理后的新列表。
 */
inline fun <T> Iterable<T>.pinnedLast(predicate: (T) -> Boolean): List<T> {
    if (this is Collection && this.size <= 1) return this.toList()
    val result = mutableListOf<T>()
    val elementsToPin = mutableListOf<T>()
    for (e in this) {
        if (predicate(e)) {
            elementsToPin += e
        } else {
            result += e
        }
    }
    return result + elementsToPin
}

/**
 * 将满足指定条件（[predicate]）的元素作为分隔符，分块输入的集合。
 *
 * @param keepEmpty 是否在输出中保留空的分块。
 */
inline fun <T> Iterable<T>.chunkedBy(keepEmpty: Boolean = true, predicate: (T) -> Boolean): List<List<T>> {
    val result = mutableListOf<List<T>>()
    val list = mutableListOf<T>()
    for (e in this) {
        if (predicate(e)) {
            if (keepEmpty || list.isNotEmpty()) {
                result += list.toList()
                list.clear()
            }
        } else {
            list += e
        }
    }
    if (keepEmpty || list.isNotEmpty()) {
        result += list.toList()
    }
    return result
}

// fun <T> List<T>.toMutableIfNotEmptyInActual(): List<T> {
//    // make List<T> properties mutable in actual if not empty (to hack them if necessary)
//    if (this.isEmpty()) return this
//    if (this is ArrayList || this is LinkedList || this is CopyOnWriteArrayList) return this
//    try {
//        this as MutableList
//        this.removeAt(-1)
//    } catch (e: UnsupportedOperationException) {
//        return this.toMutableList()
//    } catch (e: Exception) {
//        return this
//    }
//    return this
// }

/** 如果当前列表存在指定的作为后缀的子列表（可以为空），则去除并返回。否则，返回 `null`。*/
fun <T> List<T>.removePrefixOrNull(prefix: List<T>): List<T>? {
    if (prefix.isEmpty()) return this
    if (prefix.size > this.size) return null
    for ((i, e) in prefix.withIndex()) {
        if (e != this[i]) return null
    }
    return this.drop(prefix.size)
}

/** 如果当前列表存在指定的作为后缀的子列表（可以为空），则去除并返回，否则返回 `null`。*/
fun <T> List<T>.removeSuffixOrNull(suffix: List<T>): List<T>? {
    if (suffix.isEmpty()) return this
    if (suffix.size > this.size) return null
    for ((i, e) in suffix.withIndex()) {
        if (e != this[this.size - suffix.size + i]) return null
    }
    return this.dropLast(suffix.size)
}

/**
 * 如果对应键的值不存在，则先将指定的默认值放入映射（当实例化对应的委托属性时即会放入），再提供委托。
 */
class MapWithDefaultValueDelegate<V>(val map: MutableMap<String, V>, val defaultValue: V)

inline operator fun <V> MapWithDefaultValueDelegate<V>.provideDelegate(thisRef: Any?, property: KProperty<*>): MutableMap<String, V> {
    map.putIfAbsent(property.name, defaultValue)
    return map
}

/** 为 [MutableMap] 提供默认值委托构建器，语法：`myMap withDefault defaultValue`。*/
inline infix fun <V> MutableMap<String, V>.withDefault(defaultValue: V) = MapWithDefaultValueDelegate(this, defaultValue)

/** 得到指定键 [key] 对应的类型为 [List] 的值中的最后一个元素，如果不存在则返回 `null`。 */
fun <K, V> Map<K, List<V>>.getOne(key: K): V? = get(key)?.lastOrNull()

/** 得到指定键 [key] 对应的类型为 [List] 的值中的所有元素，如果不存在则返回空列表。 */
fun <K, V> Map<K, List<V>>.getAll(key: K): List<V> = get(key).orEmpty()

/** 得到指定键 [key] 对应的类型为 [MutableList] 的值，如果不存在则先初始化。*/
fun <K, E, M : MutableMap<K, MutableList<E>>> M.getOrInit(key: K): MutableList<E> = getOrPut(key) { mutableListOf() }

/** 得到指定键 [key] 对应的类型为 [MutableMap] 的值，如果不存在则先初始化。*/
fun <K, K1, V1, M : MutableMap<K, MutableMap<K1, V1>>> M.getOrInit(key: K): MutableMap<K1, V1> = getOrPut(key) { mutableMapOf() }
