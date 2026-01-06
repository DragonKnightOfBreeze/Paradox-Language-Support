package icu.windea.pls.core.util

import icu.windea.pls.core.EMPTY_OBJECT

class LazyValue<T> {
    @Volatile
    private var _value: Any? = EMPTY_OBJECT

    @Suppress("UNCHECKED_CAST")
    var value: T?
        get() = if (isInitialized()) _value as? T else null
        set(value) {
            _value = value
        }

    fun isInitialized(): Boolean = _value != EMPTY_OBJECT

    inline fun check(crossinline predicate: (T) -> Boolean) {
        val value = value ?: return
        if (predicate(value)) return
        clear()
    }

    inline fun initialize(crossinline initializer: () -> T?): T? {
        if (isInitialized()) return value
        synchronized(this) {
            if (isInitialized()) return value
            val newValue = initializer()
            value = newValue
            return newValue
        }
    }

    fun clear() {
        _value = EMPTY_OBJECT
    }
}
