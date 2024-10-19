package icu.windea.pls.core

import kotlin.reflect.*

inline fun <T> withProperties(block: PropertiesContainer.() -> T): T {
    val container = PropertiesContainer()
    try {
        return container.block()
    } finally {
        for ((property, oldValue) in container.properties2OldValues) {
            @Suppress("UNCHECKED_CAST")
            (property as KMutableProperty0<Any?>).set(oldValue)
        }
    }
}

class PropertiesContainer {
    val properties2OldValues: MutableMap<KMutableProperty0<*>, Any?> = mutableMapOf()

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> KMutableProperty0<T>.register(): T {
        val oldValue = this.get()
        properties2OldValues[this] = oldValue
        return oldValue
    }
}
