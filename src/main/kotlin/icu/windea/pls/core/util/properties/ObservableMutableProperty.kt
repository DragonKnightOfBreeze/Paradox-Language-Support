package icu.windea.pls.core.util.properties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * 可修改的可观察属性。
 *
 * 基于目标可变属性 [target] 构建一个值类型为 [V] 的“投影视图”，
 * 读取时应用正向转换 [transform]，写入时应用反向转换 [revertedTransform] 并同步回原属性。
 */
class ObservableMutableProperty<T, V>(
    target: KMutableProperty0<T>,
    transform: (T) -> V,
    private val revertedTransform: (V) -> T
) : ObservableProperty<T, V>(target, transform), ReadWriteProperty<Any?, V> {
    /** 设置投影值 [value]，将其映射到原属性并写回。*/
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        val newValue = revertedTransform(value)
        this.value = value
        targetValue = newValue
        target.set(newValue)
    }
}
