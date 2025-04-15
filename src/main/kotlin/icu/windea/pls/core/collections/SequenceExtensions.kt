@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.collections

inline fun <reified R> Sequence<*>.filterIsInstance(noinline predicate: (R) -> Boolean): Sequence<R> {
    return filterIsInstance(R::class.java, predicate)
}

inline fun <R> Sequence<*>.filterIsInstance(klass: Class<R>, noinline predicate: (R) -> Boolean): Sequence<R> {
    return filterIsInstance(klass).filter(predicate)
}

inline fun <reified R> Sequence<*>.findIsInstance(predicate: (R) -> Boolean = { true }): R? {
    return findIsInstance(R::class.java, predicate)
}

@Suppress("UNCHECKED_CAST")
inline fun <R> Sequence<*>.findIsInstance(klass: Class<R>, predicate: (R) -> Boolean = { true }): R? {
    for (element in this) if (klass.isInstance(element) && predicate(element as R)) return element
    return null
}

inline fun <T, reified R> Sequence<T>.mapToArray(transform: (T) -> R): Array<R> {
    return toList().mapToArray(transform)
}

inline fun <reified T> Sequence<T>.toArray() = this.toList().toTypedArray()

fun <T> Sequence<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}
