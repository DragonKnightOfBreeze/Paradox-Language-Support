package icu.windea.pls.core.collections

import java.util.function.BiPredicate

inline fun <reified R> Sequence<*>.findIsInstance(): R? {
    return findIsInstance(R::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <R> Sequence<*>.findIsInstance(klass: Class<R>): R? {
    for(element in this) if(klass.isInstance(element)) return element as R
    return null
}

inline fun <T, reified R> Sequence<T>.mapToArray(transform: (T) -> R): Array<R> {
    return toList().mapToArray(transform)
}

inline fun <reified T> Sequence<T>.toArray() = this.toList().toTypedArray()
