@file:Suppress("unused")

package icu.windea.pls.core.util.properties

import icu.windea.pls.core.*
import kotlin.reflect.*

fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V): ObservableProperty<T, V> {
    return ObservableProperty(this, transform)
}

fun <T, V> KMutableProperty0<T>.observeMutable(transform: (T) -> V, revertedTransform: (V) -> T): ObservableMutableProperty<T, V> {
    return ObservableMutableProperty(this, transform, revertedTransform)
}

@JvmName("fromCommandDelimitedString")
fun KMutableProperty0<String>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String, Set<String>> {
    return observeMutable({ it.toCommaDelimitedStringSet(stringSet(ignoreCase)) }, { it.toCommaDelimitedString() })
}

@JvmName("fromCommandDelimitedStringNullable")
fun KMutableProperty0<String?>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String?, Set<String>> {
    return observeMutable({ it?.toCommaDelimitedStringSet(stringSet(ignoreCase)).orEmpty() }, { it.toCommaDelimitedString() })
}

fun KMutableProperty0<Set<String>>.toCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<Set<String>, String> {
    return observeMutable({ it.toCommaDelimitedString() }, { it.toCommaDelimitedStringSet(stringSet(ignoreCase)) })
}

private fun stringSet(ignoreCase: Boolean) = if (ignoreCase) caseInsensitiveStringSet() else mutableSetOf()
