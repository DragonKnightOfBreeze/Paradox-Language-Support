@file:Suppress("unused")

package icu.windea.pls.core.util.properties

import icu.windea.pls.core.toDelimitedSet
import icu.windea.pls.core.toDelimitedString
import kotlin.reflect.KMutableProperty0

/**
 * 将可变属性 [KMutableProperty0] 映射为可观察的属性 [ObservableProperty]。
 */
fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V): ObservableProperty<T, V> {
    return ObservableProperty(this, transform)
}

/**
 * 将可变属性 [KMutableProperty0] 映射为可观察的可变属性 [ObservableMutableProperty]。
 */
fun <T, V> KMutableProperty0<T>.observeMutable(transform: (T) -> V, revertedTransform: (V) -> T): ObservableMutableProperty<T, V> {
    return ObservableMutableProperty(this, transform, revertedTransform)
}

/**
 * 转为可观察的可变属性 [ObservableMutableProperty]，其值为按 [delimiter] 分割后的列表的视图。默认使用英文逗号作为分隔符。
 *
 * @see toDelimitedSet
 * @see toDelimitedString
 */
@JvmName("fromDelimitedString")
fun KMutableProperty0<String>.fromDelimitedString(delimiter: Char = ','): ObservableMutableProperty<String, Set<String>> {
    return observeMutable({ it.toDelimitedSet(delimiter) }, { it.toDelimitedString() })
}

/**
 * 转为可观察的可变属性 [ObservableMutableProperty]，其值为按 [delimiter] 分割后的集的视图。默认使用英文逗号作为分隔符。
 *
 * @see toDelimitedSet
 * @see toDelimitedString
 */
@JvmName("fromDelimitedStringNullable")
fun KMutableProperty0<String?>.fromDelimitedString(delimiter: Char = ','): ObservableMutableProperty<String?, Set<String>> {
    return observeMutable({ it?.toDelimitedSet(delimiter).orEmpty() }, { it.toDelimitedString() })
}

/**
 * 转为可观察的可变属性 [ObservableMutableProperty]，其值为按 [delimiter] 分隔后的字符串的视图。
 *
 * @see toDelimitedString
 * @see toDelimitedSet
 */
fun KMutableProperty0<Set<String>>.toDelimitedString(delimiter: Char = ','): ObservableMutableProperty<Set<String>, String> {
    return observeMutable({ it.toDelimitedString() }, { it.toDelimitedSet() })
}
