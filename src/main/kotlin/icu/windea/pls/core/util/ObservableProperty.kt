package icu.windea.pls.core.util

import icu.windea.pls.core.EMPTY_OBJECT
import kotlin.properties.*
import kotlin.reflect.*

/**
 * 可观察属性。
 *
 * 监听另一个属性的更改，如果发生，此属性的值也会同步进行更改。
 */
open class ObservableProperty<T, V>(
    protected val target: KMutableProperty0<T>,
    protected val transform: (T) -> V
) : ReadOnlyProperty<Any?, V> {
    @Volatile
    protected var targetValue: T? = null
    @Volatile
    protected var value: Any? = EMPTY_OBJECT

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        val newTargetValue = target.get()
        if (value === EMPTY_OBJECT) {
            targetValue = newTargetValue
            value = transform(newTargetValue)
            return value as V
        } else {
            if (targetValue === newTargetValue) {
                return value as V
            } else {
                targetValue = newTargetValue
                value = transform(newTargetValue)
                return value as V
            }
        }
    }
}

fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V): ObservableProperty<T, V> {
    return ObservableProperty(this, transform)
}
