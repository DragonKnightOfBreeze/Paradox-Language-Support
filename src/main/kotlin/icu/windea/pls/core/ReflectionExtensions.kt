@file:Suppress("UNCHECKED_CAST", "unused")

package icu.windea.pls.core

import java.lang.reflect.*

inline fun tryGetField(action: () -> Field): Field? {
    try {
        return action()
    } catch (_: NoSuchFieldException) {
        return null
    }
}

inline fun tryGetMethod(action: () -> Method): Method? {
    try {
        return action()
    } catch (_: NoSuchMethodException) {
        return null
    }
}

fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
