@file:Suppress("UNCHECKED_CAST")

package icu.windea.pls.core

import java.lang.reflect.*

inline fun tryGetField(action: () -> Field): Field? {
    try {
        return action()
    } catch(e: NoSuchFieldException) {
        return null
    }
}

inline fun tryGetMethod(action: () -> Method): Method? {
    try {
        return action()
    } catch(e: NoSuchMethodException) {
        return null
    }
}

fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
