@file:Suppress("unused")

package icu.windea.pls.core.util.properties

import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringSet
import kotlin.reflect.KMutableProperty0

/** 将可变属性 [KMutableProperty0] 映射为只读的可观察属性。*/
fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V): ObservableProperty<T, V> {
    return ObservableProperty(this, transform)
}

/** 将可变属性 [KMutableProperty0] 映射为可写的可观察属性，需提供正反转换。*/
fun <T, V> KMutableProperty0<T>.observeMutable(transform: (T) -> V, revertedTransform: (V) -> T): ObservableMutableProperty<T, V> {
    return ObservableMutableProperty(this, transform, revertedTransform)
}

/**
 * 将 `String` 属性与“逗号分隔集合”视图互转。
 *
 * - 读取时按逗号分隔得到 `Set<String>`；
 * - 写入时将集合以逗号连接写回；
 * - [ignoreCase] 为 `true` 时使用不区分大小写的 `MutableSet`。
 */
@JvmName("fromCommandDelimitedString")
fun KMutableProperty0<String>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String, Set<String>> {
    return observeMutable({ it.toCommaDelimitedStringSet(stringSet(ignoreCase)) }, { it.toCommaDelimitedString() })
}

/** 与上类似，但源属性可为空；为空时读取为 `emptySet()`。*/
@JvmName("fromCommandDelimitedStringNullable")
fun KMutableProperty0<String?>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String?, Set<String>> {
    return observeMutable({ it?.toCommaDelimitedStringSet(stringSet(ignoreCase)).orEmpty() }, { it.toCommaDelimitedString() })
}

/**
 * 将 `Set<String>` 属性与“逗号分隔字符串”视图互转。
 *
 * - 读取时连接为字符串；
 * - 写入时按逗号分隔为集合；
 * - [ignoreCase] 为 `true` 时使用不区分大小写的 `MutableSet`。
 */
fun KMutableProperty0<Set<String>>.toCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<Set<String>, String> {
    return observeMutable({ it.toCommaDelimitedString() }, { it.toCommaDelimitedStringSet(stringSet(ignoreCase)) })
}

private fun stringSet(ignoreCase: Boolean) = if (ignoreCase) caseInsensitiveStringSet() else mutableSetOf()
