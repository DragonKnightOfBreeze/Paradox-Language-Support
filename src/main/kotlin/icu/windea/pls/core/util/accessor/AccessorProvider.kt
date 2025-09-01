package icu.windea.pls.core.util.accessor

import kotlin.reflect.KClass

/**
 * 访问器提供器。
 *
 * 面向具体类型 [targetClass] 提供属性/函数的读取、写入与调用能力。
 */
interface AccessorProvider<T : Any> {
    /** 目标类型。 */
    val targetClass: KClass<T>

    /** 读取属性。 */
    fun <V> get(target: T?, propertyName: String): V

    /** 写入属性。 */
    fun <V> set(target: T?, propertyName: String, value: V)

    /** 调用函数。 */
    fun  invoke(target: T?, functionName: String, vararg args: Any?): Any?
}

