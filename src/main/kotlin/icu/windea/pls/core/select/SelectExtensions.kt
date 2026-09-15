@file:Suppress("unused")

package icu.windea.pls.core.select

fun <T> Sequence<T>.one(): T? {
    return firstOrNull()
}

fun <T> Sequence<T>.list(): List<T> {
    return toList()
}

fun <T> Sequence<T>.oneBy(predicate: (T) -> Boolean): T? {
    return find(predicate)
}

@JvmName("oneByType")
inline fun <reified R> Sequence<Any?>.oneBy(): R? {
    return find { it is R } as R?
}

@JvmName("oneByType")
inline fun <reified R> Sequence<Any?>.oneBy(predicate: (R) -> Boolean): R? {
    return find { it is R && predicate(it) } as R?
}

fun <T> Sequence<T>.listBy(predicate: (T) -> Boolean): List<T> {
    return filter(predicate).toList()
}

@JvmName("listByType")
inline fun <reified R> Sequence<Any?>.listBy(): List<R> {
    @Suppress("UNCHECKED_CAST")
    return filter { it is R }.toList() as List<R>
}

@JvmName("listByType")
inline fun <reified R> Sequence<Any?>.listBy(crossinline predicate: (R) -> Boolean): List<R> {
    @Suppress("UNCHECKED_CAST")
    return filter { it is R && predicate(it) }.toList() as List<R>
}
