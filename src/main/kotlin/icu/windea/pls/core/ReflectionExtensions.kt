@file:Suppress("UNCHECKED_CAST")

package icu.windea.pls.core

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KFunction

/**
 * 判断函数是否为属性 Getter。
 *
 * - 若 [propertyName] 为空，则匹配 `getXxx()` 或 `isXxx()`（布尔类型）命名规则；
 * - 若指定了 [propertyName]，则严格匹配对应的 Getter 命名。
 */
fun KFunction<*>.isGetter(propertyName: String? = null): Boolean {
    if (parameters.size != 1) return false
    if (propertyName == null) {
        if (name.startsWith("get") && name.length > 3) return true
        if (returnType.classifier == Boolean::class && name.startsWith("is") && name.length > 2) return true
    } else {
        val suffix = propertyName.capitalized()
        if (name == "get$suffix") return true
        if (returnType.classifier == Boolean::class && name == "is$suffix") return true
    }
    return false
}

/**
 * 判断函数是否为属性 Setter。
 *
 * - 若 [propertyName] 为空，则匹配 `setXxx()` 命名规则；
 * - 若指定了 [propertyName]，则严格匹配对应的 Setter 命名。
 */
fun KFunction<*>.isSetter(propertyName: String? = null): Boolean {
    if (parameters.size != 2) return false
    if (propertyName == null) {
        if (name.startsWith("set") && name.length > 3) return true
    } else {
        val suffix = propertyName.capitalized()
        if (name == "set$suffix") return true
    }
    return false
}

/**
 * 从泛型类型 [Type] 中提取第 [index] 个实际类型实参并转换为 `Class` 类型。
 *
 * 仅在当前类型为 [ParameterizedType] 时有效，否则返回 `null`。
 */
fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
