package icu.windea.pls.core.util.accessor

import kotlin.reflect.*

interface Accessor<T : Any> {
    val targetClass: KClass<T>
    val accessorProvider: AccessorProvider<T>
}
