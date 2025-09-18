package icu.windea.pls.core.util.accessor

import kotlin.reflect.KClass

/**
 * 访问器提供者。
 *
 * 面向某个目标类型 [targetClass] 提供统一的读/写/调用能力，
 * 内部可缓存反射信息与委托以提升访问性能。
 */
interface AccessorProvider<T : Any> {
    val targetClass: KClass<T>

    /** 读取属性 [propertyName] 的值（[target] 为 `null` 表示静态上下文）。*/
    fun <V> get(target: T?, propertyName: String): V

    /** 写入属性 [propertyName] 的值（[target] 为 `null` 表示静态上下文）。*/
    fun <V> set(target: T?, propertyName: String, value: V)

    /** 调用函数 [functionName]，参数为 [args]（[target] 为 `null` 表示静态上下文）。*/
    fun  invoke(target: T?, functionName: String, vararg args: Any?): Any?
}

