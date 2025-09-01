package icu.windea.pls.core.util.properties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * 可修改的可观察属性。
 *
 * 在 [ObservableProperty] 的基础上支持写入：当设置此属性时，使用 [revertedTransform] 将值反向映射回目标属性 [target]。
 */
class ObservableMutableProperty<T, V>(
    target: KMutableProperty0<T>,
    transform: (T) -> V,
    private val revertedTransform: (V) -> T
) : ObservableProperty<T, V>(target, transform), ReadWriteProperty<Any?, V> {
    /**
     * 设置值时，先将 `V` 通过 [revertedTransform] 转换为 `T`，并同步更新目标属性与缓存。
     */
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        val newValue = revertedTransform(value)
        this.value = value
        targetValue = newValue
        target.set(newValue)
    }
}
