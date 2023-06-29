package icu.windea.pls.core.util

import kotlin.reflect.*

interface MutableProperty<T> {
    fun get(): T
    fun set(value: T)
}

inline fun <T> mutableProperty(crossinline getter: () -> T, crossinline setter: (T) -> Unit): MutableProperty<T> {
    return object : MutableProperty<T> {
        override fun get() = getter()
        
        override fun set(value: T) = setter(value)
    }
}

operator fun <T> MutableProperty<T>.getValue(thisRef: Any?, property: KProperty<*>) = this.get()

operator fun <T> MutableProperty<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) = this.set(value)