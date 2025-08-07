@file:Suppress("unused")

package icu.windea.pls.core.util

import icu.windea.pls.core.*
import kotlin.properties.*
import kotlin.reflect.*

/**
 * 可修改的可观察属性。
 */
class ObservableMutableProperty<T, V>(
    target: KMutableProperty0<T>,
    transform: (T) -> V,
    private val revertedTransform: (V) -> T
) : ObservableProperty<T, V>(target, transform), ReadWriteProperty<Any?, V> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        val newValue = revertedTransform(value)
        this.value = value
        targetValue = newValue
        target.set(newValue)
    }
}

fun <T, V> KMutableProperty0<T>.observeMutable(transform: (T) -> V, revertedTransform: (V) -> T): ObservableMutableProperty<T, V> {
    return ObservableMutableProperty(this, transform, revertedTransform)
}

@JvmName("fromCommandDelimitedString")
fun KMutableProperty0<String>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String, Set<String>> {
    return ObservableMutableProperty(this, { it.toCommaDelimitedStringSet(destination(ignoreCase)) }, { it.toCommaDelimitedString() })
}

@JvmName("fromCommandDelimitedStringNullable")
fun KMutableProperty0<String?>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String?, Set<String>> {
    return ObservableMutableProperty(this, { it?.toCommaDelimitedStringSet(destination(ignoreCase)).orEmpty() }, { it.toCommaDelimitedString() })
}

fun KMutableProperty0<Set<String>>.toCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<Set<String>, String> {
    return ObservableMutableProperty(this, { it.toCommaDelimitedString() }, { it.toCommaDelimitedStringSet(destination(ignoreCase)) })
}

private fun destination(ignoreCase: Boolean) = if(ignoreCase) caseInsensitiveStringSet() else mutableSetOf()
