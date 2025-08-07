package icu.windea.pls.core.util.properties

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
