package icu.windea.pls.core.util

import icu.windea.pls.core.EMPTY_OBJECT

/** 拥有3种状态（未初始化/无值/有值）的值包装器。 */
@Suppress("UNCHECKED_CAST")
class StatefulValue<T>{
    private var _value: Any? = EMPTY_OBJECT

    val isInitialized: Boolean
        get() = _value != EMPTY_OBJECT
    var value: T?
        get() = _value as? T
        set(value) { _value = value }
}
