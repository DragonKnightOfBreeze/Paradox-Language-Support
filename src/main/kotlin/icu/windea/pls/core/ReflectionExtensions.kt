@file:Suppress("UNCHECKED_CAST", "unused")

package icu.windea.pls.core

import java.lang.reflect.*
import kotlin.reflect.*

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

fun KFunction<*>.isSetter(propertyName: String? = null): Boolean {
    if (parameters.size != 2) return false
    if (propertyName == null) {
        if (name.startsWith("set") && name.length > 3) return true
    } else {
        val suffix = propertyName.decapitalized()
        if (name == "set$suffix") return true
    }
    return false
}

fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
