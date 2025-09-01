package icu.windea.pls.core.util.accessor

import kotlin.reflect.KClass

/**
 * 访问器接口。
 *
 * 抽象对目标类型成员（属性/函数）的访问能力，由具体的 [accessorProvider] 提供实现。
 */
interface Accessor<T : Any> {
    /** 访问的目标类型。 */
    val targetClass: KClass<T>
    /** 提供实际访问能力的提供器。 */
    val accessorProvider: AccessorProvider<T>
}
