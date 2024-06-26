package icu.windea.pls.core.collections

@PublishedApi
internal fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default

inline fun String.forEachFast(action: (Char) -> Unit) {
    val length = this.length
    for(i in 0 until length) {
        action(this[i])
    }
}

inline fun <T> Array<T>.forEachFast(action: (T) -> Unit) {
    val size = this.size
    for(i in 0 until size) {
        action(this[i])
    }
}

inline fun <T> List<T>.forEachFast(action: (T) -> Unit) {
    val size = this.size
    for(i in 0 until size) {
        action(this[i])
    }
}

inline fun String.forEachIndexedFast(action: (Int, Char) -> Unit) {
    val size = this.length
    for(i in 0 until size) {
        action(i, this[i])
    }
}

inline fun <T> Array<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    val size = this.size
    for(i in 0 until size) {
        action(i, this[i])
    }
}

inline fun <T> List<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    val size = this.size
    for(i in 0 until size) {
        action(i, this[i])
    }
}

inline fun <T, R, C : MutableCollection<in R>> List<T>.mapToFast(destination: C, transform: (T) -> R): C {
    forEachFast {
        val e = transform(it)
        destination.add(e)
    }
    return destination
}

inline fun <T, R> List<T>.mapFast(transform: (T) -> R): List<R> {
    if(isEmpty()) return emptyList() //optimize
    return mapToFast(ArrayList(collectionSizeOrDefault(10)), transform)
}

inline fun <T, R, C : MutableCollection<in R>> List<T>.mapNotNullToFast(destination: C, transform: (T) -> R?): C {
    forEachFast {
        val e = transform(it)
        if(e != null) destination.add(e)
    }
    return destination
}

inline fun <T, R> List<T>.mapNotNullFast(transform: (T) -> R?): List<R> {
    if(isEmpty()) return emptyList() //optimize
    return mapNotNullToFast(ArrayList(), transform)
}

inline fun <T, C : MutableCollection<in T>> List<T>.filterToFast(destination: C, predicate: (T) -> Boolean): C {
    forEachFast { e ->
        if (predicate(e)) destination.add(e)
    }
    return destination
}

inline fun <T> List<T>.filterFast(predicate: (T) -> Boolean): List<T> {
    if(isEmpty()) return emptyList() //optimize
    return filterToFast(ArrayList(), predicate)
}

inline fun <T, C : MutableCollection<in T>> List<T>.filterNotToFast(destination: C, predicate: (T) -> Boolean): C {
    forEachFast { e ->
        if (!predicate(e)) destination.add(e)
    }
    return destination
}

inline fun <T> List<T>.filterNotFast(predicate: (T) -> Boolean): List<T> {
    if(isEmpty()) return emptyList() //optimize
    return filterNotToFast(ArrayList(), predicate)
}

inline fun <reified R, C : MutableCollection<in R>> List<*>.filterIsInstanceToFast(destination: C): C {
    forEachFast { e ->
        if(e is R) destination.add(e)
    }
    return destination
}

inline fun <reified R> List<*>.filterIsInstanceFast(): List<R> {
    if(isEmpty()) return emptyList() //optimize
    return filterIsInstanceToFast(ArrayList())
}

inline fun <T> List<T>.findFast(predicate: (T) -> Boolean): T? {
    forEachFast { e ->
        if (predicate(e)) return e
    }
    return null
}