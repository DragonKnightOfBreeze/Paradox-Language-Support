package icu.windea.pls.core.util.accessor

import kotlin.reflect.*

interface AccessorProvider<T : Any> {
    val targetClass: KClass<T>

    fun <V> get(target: T?, propertyName: String): V

    fun <V> set(target: T?, propertyName: String, value: V)

    fun  invoke(target: T?, functionName: String, vararg args: Any?): Any?
}

