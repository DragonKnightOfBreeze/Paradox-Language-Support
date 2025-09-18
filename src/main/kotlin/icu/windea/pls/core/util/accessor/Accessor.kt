package icu.windea.pls.core.util.accessor

import kotlin.reflect.KClass

/**
 * 访问器基类。
 *
 * 封装与目标类型相关的元信息与 [AccessorProvider]，用于通过委托统一执行读/写/调用等反射操作。
 *
 * @property targetClass 目标类型。
 * @property accessorProvider 访问器提供者（按需缓存）。
 */
interface Accessor<T : Any> {
    val targetClass: KClass<T>
    val accessorProvider: AccessorProvider<T>
}
