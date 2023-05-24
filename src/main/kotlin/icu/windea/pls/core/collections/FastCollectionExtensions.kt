package icu.windea.pls.core.collections

import com.intellij.util.*

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

inline fun <T, R> List<T>.mapFast(transform: (T) -> R): List<R> {
    val destination = SmartList<R>()
    forEachFast {
        val e = transform(it)
        destination.add(e)
    }
    return destination
}

inline fun <T, R> List<T>.mapNotNullFast(transform: (T) -> R?): List<R> {
    val destination = SmartList<R>()
    forEachFast {
        val e = transform(it)
        if(e != null) destination.add(e)
    }
    return destination
}