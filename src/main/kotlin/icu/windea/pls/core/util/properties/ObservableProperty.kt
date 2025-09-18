
package icu.windea.pls.core.util.properties

import icu.windea.pls.core.EMPTY_OBJECT
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * 可观察属性。
 *
 * 监听另一个可变属性 [target] 的变化，读取时使用 [transform] 将其值投影为类型 [V]。
 * 简单的“脏值”判断通过比较引用相等（`===`）完成；非线程安全，必要时在外层加锁。
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
    /** 读取当前投影值，若源值发生变化则重新计算。*/
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

