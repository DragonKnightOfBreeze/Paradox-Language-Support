@file:Suppress("UNCHECKED_CAST", "unused")

package icu.windea.pls.core

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KFunction

/** 判断是否为 getter 方法；可选按属性名匹配（支持 getXxx / isXxx）。 */
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

/** 判断是否为 setter 方法；可选按属性名匹配（支持 setXxx）。 */
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

/** 获取泛型参数类型（若存在且为参数化类型）。 */
fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
