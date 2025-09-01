@file:Suppress("unused")

package icu.windea.pls.core.util.properties

import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringSet
import kotlin.reflect.KMutableProperty0

/**
 * 将可变属性包装为只读的可观察属性。
 * 当目标属性变化时，使用 [transform] 计算派生值。
 */
fun <T, V> KMutableProperty0<T>.observe(transform: (T) -> V): ObservableProperty<T, V> {
    return ObservableProperty(this, transform)
}

/**
 * 将可变属性包装为可读写的可观察属性。
 * - 读取：当目标属性变化时，使用 [transform] 计算派生值。
 * - 写入：使用 [revertedTransform] 将派生值转换回目标属性类型并写入。
 */
fun <T, V> KMutableProperty0<T>.observeMutable(transform: (T) -> V, revertedTransform: (V) -> T): ObservableMutableProperty<T, V> {
    return ObservableMutableProperty(this, transform, revertedTransform)
}

@JvmName("fromCommandDelimitedString")
/**
 * 将以逗号分隔的 `String` 属性映射为 `Set<String>` 视图。
 *
 * @param ignoreCase 是否使用不区分大小写的集合语义。
 */
fun KMutableProperty0<String>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String, Set<String>> {
    return observeMutable({ it.toCommaDelimitedStringSet(stringSet(ignoreCase)) }, { it.toCommaDelimitedString() })
}

@JvmName("fromCommandDelimitedStringNullable")
/**
 * 将可空的以逗号分隔的 `String?` 属性映射为 `Set<String>` 视图（null 视为空集）。
 *
 * @param ignoreCase 是否使用不区分大小写的集合语义。
 */
fun KMutableProperty0<String?>.fromCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<String?, Set<String>> {
    return observeMutable({ it?.toCommaDelimitedStringSet(stringSet(ignoreCase)).orEmpty() }, { it.toCommaDelimitedString() })
}

/**
 * 将 `Set<String>` 属性映射为以逗号分隔的 `String` 视图。
 *
 * @param ignoreCase 是否使用不区分大小写的集合语义（仅影响写回时的集合类型）。
 */
fun KMutableProperty0<Set<String>>.toCommandDelimitedString(ignoreCase: Boolean = false): ObservableMutableProperty<Set<String>, String> {
    return observeMutable({ it.toCommaDelimitedString() }, { it.toCommaDelimitedStringSet(stringSet(ignoreCase)) })
}

private fun stringSet(ignoreCase: Boolean) = if (ignoreCase) caseInsensitiveStringSet() else mutableSetOf()
