@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.cast
import java.util.*

/** 非空且非空集合时返回自身，否则返回 null。 */
inline fun <T : Collection<*>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将不可变 List 视为 MutableList（未做拷贝，需确保实际类型可变）。 */
inline fun <T> List<T>.asMutable(): MutableList<T> = this as MutableList<T>

/** 将不可变 Set 视为 MutableSet（未做拷贝，需确保实际类型可变）。 */
inline fun <T> Set<T>.asMutable(): MutableSet<T> = this as MutableSet<T>

// only for empty list (since, e.g., string elements may ignore case)
/** 若为空则返回共享空列表以优化内存。仅适用于空列表场景。 */
inline fun <T> List<T>.optimized(): List<T> = ifEmpty { emptyList() }

// only for empty list (since, e.g., string elements may ignore case)
/** 若为空则返回共享空集合以优化内存。仅适用于空集合场景。 */
inline fun <T : Any> Set<T>.optimized(): Set<T> = ifEmpty { emptySet() }

/** 返回同步包装的可变 List。 */
inline fun <T> MutableList<T>.synced(): MutableList<T> = Collections.synchronizedList(this)

/** 返回同步包装的可变 Set。 */
inline fun <T> MutableSet<T>.synced(): MutableSet<T> = Collections.synchronizedSet(this)

/** 若已是 List 则直接返回，否则拷贝为 List。 */
inline fun <T> Collection<T>.toListOrThis(): List<T> = if (this is List) this else this.toList()

/** 若已是 Set 则直接返回，否则拷贝为 Set。 */
inline fun <T> Collection<T>.toSetOrThis(): Set<T> = if (this is Set) this else this.toSet()

/** 过滤并仅保留类型为 R 且满足谓词的元素，返回 List。 */
inline fun <reified R> Iterable<*>.filterIsInstance(predicate: (R) -> Boolean): List<R> {
    return filterIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
/** 过滤并仅保留类型为 [klass] 且满足谓词的元素，返回 List。 */
inline fun <R> Iterable<*>.filterIsInstance(klass: Class<R>, predicate: (R) -> Boolean): List<R> {
    val result = ArrayList<R>()
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) result.add(element)
    return result
}

/** 查找首个类型为 R 且满足谓词的元素。 */
inline fun <reified R> Iterable<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
/** 查找首个类型为 [klass] 且满足谓词的元素。 */
inline fun <R> Iterable<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) return element
    return null
}

/** 将 List 按转换函数映射为新数组。 */
inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}

/** 将 Collection 按转换函数映射为新数组（避免不必要的装箱）。 */
inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
    if (this is List) return this.mapToArray(transform)
    val result = arrayOfNulls<R>(this.size)
    for ((i, e) in this.withIndex()) {
        result[i] = transform(e)
    }
    return result.cast()
}

/** 将满足谓词的元素置顶（保持相对顺序）。 */
inline fun <T> Iterable<T>.pinned(predicate: (T) -> Boolean): List<T> {
    if (this is Collection && this.size <= 1) return this.toListOrThis()
    if (this.none()) return emptyList()
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

/** 将满足谓词的元素置底（保持相对顺序）。 */
inline fun <T> Iterable<T>.pinnedLast(predicate: (T) -> Boolean): List<T> {
    if (this is Collection && this.size <= 1) return this.toListOrThis()
    if (this.none()) return emptyList()
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

/** 逐个处理元素，处理器返回 false 时提前终止。 */
fun <T> Iterable<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}

//fun <T> List<T>.toMutableIfNotEmptyInActual(): List<T> {
//    //make List<T> properties mutable in actual if not empty (to hack them if necessary)
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
//}
