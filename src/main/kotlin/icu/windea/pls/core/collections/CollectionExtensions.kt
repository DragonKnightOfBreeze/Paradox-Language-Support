@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.collections

import icu.windea.pls.core.cast
import java.util.*

/** 如果当前集合为 `null` 或为空，则返回 `null`。否则返回自身。*/
inline fun <T : Collection<*>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

/** 将只读 [List] 视为 [MutableList]（要求实际类型可变，否则抛出异常）。*/
inline fun <T> List<T>.asMutable(): MutableList<T> = this as MutableList<T>

/** 将只读 [Set] 视为 [MutableSet]（要求实际类型可变，否则抛出异常）。*/
inline fun <T> Set<T>.asMutable(): MutableSet<T> = this as MutableSet<T>

// only for empty list (since, e.g., string elements may ignore case)
/** 若列表为空则返回标准库空列表，否则返回自身（减少空集合分配）。*/
inline fun <T> List<T>.optimized(): List<T> = ifEmpty { emptyList() }

// only for empty list (since, e.g., string elements may ignore case)
/** 若集合为空则返回标准库空集合，否则返回自身（减少空集合分配）。*/
inline fun <T : Any> Set<T>.optimized(): Set<T> = ifEmpty { emptySet() }

/** 返回加锁包装的 [MutableList]，基于 [Collections.synchronizedList]。*/
inline fun <T> MutableList<T>.synced(): MutableList<T> = Collections.synchronizedList(this)

/** 返回加锁包装的 [MutableSet]，基于 [Collections.synchronizedSet]。*/
inline fun <T> MutableSet<T>.synced(): MutableSet<T> = Collections.synchronizedSet(this)

/** 若当前已是 [List] 则直接返回，否则拷贝为新的 [List]。*/
inline fun <T> Collection<T>.toListOrThis(): List<T> = if (this is List) this else this.toList()

/** 若当前已是 [Set] 则直接返回，否则拷贝为新的 [Set]。*/
inline fun <T> Collection<T>.toSetOrThis(): Set<T> = if (this is Set) this else this.toSet()

/** 过滤为 [R] 类型并附加谓词 [predicate]。*/
inline fun <reified R> Iterable<*>.filterIsInstance(predicate: (R) -> Boolean): List<R> {
    return filterIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
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

@Suppress("UNCHECKED_CAST")
/** 查找第一个为 [klass] 类型且满足 [predicate] 的元素。*/
inline fun <R> Iterable<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    return find { klass.isInstance(it) && predicate(it as R) } as R?
}

/** 查找最后一个为 [R] 类型且满足 [predicate] 的元素。*/
inline fun <reified R> List<*>.findLastIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findLast { it is R && predicate(it) } as R?
}

@Suppress("UNCHECKED_CAST")
/** 查找最后一个为 [klass] 类型且满足 [predicate] 的元素。*/
inline fun <R> List<*>.findLastIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    return findLast { klass.isInstance(it) && predicate(it as R) } as R?
}

/** 将当前列表映射为数组。*/
inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R): Array<R> {
    return Array(size) { transform(this[it]) }
}

/** 将当前集合映射为数组。若为列表则走下标路径，否则顺序遍历。*/
inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
    if (this is List) return this.mapToArray(transform)
    val result = arrayOfNulls<R>(this.size)
    for ((i, e) in this.withIndex()) {
        result[i] = transform(e)
    }
    return result.cast()
}

/** 将满足 [predicate] 的元素“置顶”（保持相对顺序）。*/
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

/** 将满足 [predicate] 的元素“置底”（保持相对顺序）。*/
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

/** 逐个处理元素，若处理函数 [processor] 返回 `false` 则提前终止并返回 `false`。*/
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
