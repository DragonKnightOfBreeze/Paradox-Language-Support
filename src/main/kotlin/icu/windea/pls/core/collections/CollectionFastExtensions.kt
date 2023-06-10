package icu.windea.pls.core.collections

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

inline fun <T> List<T>.forEachIndexedFast(action: (Int, T) -> Unit) {
    val size = this.size
    for(i in 0 until size) {
        action(i, this[i])
    }
}

inline fun <T, R, C : MutableCollection<in R>> List<T>.mapFastTo(destination: C, transform: (T) -> R): C {
    forEachFast {
        val e = transform(it)
        destination.add(e)
    }
    return destination
}

inline fun <T, R> List<T>.mapFast(transform: (T) -> R): List<R> {
    return mapFastTo(ArrayList(size), transform)
}

inline fun <T, R, C : MutableCollection<in R>> List<T>.mapNotNullFastTo(destination: C, transform: (T) -> R?): C {
    forEachFast {
        val e = transform(it)
        if(e != null) destination.add(e)
    }
    return destination
}

inline fun <T, R> List<T>.mapNotNullFast(transform: (T) -> R?): List<R> {
    return mapNotNullFastTo(ArrayList(), transform)
}

inline fun <T, C : MutableCollection<in T>> List<T>.filterFastTo(destination: C, predicate: (T) -> Boolean): C {
    forEachFast { e ->
        if (predicate(e)) destination.add(e)
    }
    return destination
}

inline fun <T> List<T>.filterFast(predicate: (T) -> Boolean): List<T> {
    return filterFastTo(ArrayList(), predicate)
}
