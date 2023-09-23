@file:Suppress("UNCHECKED_CAST")

package icu.windea.pls.core

import java.lang.reflect.*

fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
