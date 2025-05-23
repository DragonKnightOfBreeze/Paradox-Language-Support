package icu.windea.pls.core.collections

import icu.windea.pls.core.*
import java.util.*

fun <T> MutableSet(comparator: Comparator<T>? = null): MutableSet<T> {
    return if (comparator == null) mutableSetOf() else TreeSet(comparator)
}

inline fun <reified T : Enum<T>> enumSetOf(vararg values: T): EnumSet<T> {
    return EnumSet.noneOf(T::class.java).apply { values.forEach { add(it) } }
}

inline fun <reified T> T.toSingletonArray(): Array<T> {
    return arrayOf(this)
}

fun <T> T.toSingletonList(): List<T> {
    return Collections.singletonList(this)
}

fun <T : Any> T?.toSingletonListOrEmpty(): List<T> {
    return if (this == null) Collections.emptyList() else Collections.singletonList(this)
}

fun <T> T.toSingletonSet(): Set<T> {
    return Collections.singleton(this)
}

fun <T : Any> T?.toSingletonSetOrEmpty(): Set<T> {
    return if (this == null) Collections.emptySet() else Collections.singleton(this)
}

fun <T : Any, C : MutableCollection<T>> mergeTo(destination: C, vararg collections: Collection<T>?): C {
    collections.forEach { collection ->
        if (collection.isNotNullOrEmpty()) destination.addAll(collection)
    }
    return destination
}

fun <T : Any> merge(vararg collections: Collection<T>?): List<T> {
    return mergeTo(ArrayList(), *collections)
}
