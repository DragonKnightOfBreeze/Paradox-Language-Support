@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package icu.windea.pls.core.util.accessor

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class PropertyAccessor<T : Any, V>(
    val target: T,
    val propertyName: String,
    val targetClassProvider: () -> KClass<T>
): Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    fun get(): V {
        return accessorProvider.get(target, propertyName)
    }

    fun set(value: V) {
        accessorProvider.set(target, propertyName, value)
    }
}

class MemberPropertyAccessor<T : Any, V>(
    val propertyName: String,
    val targetClassProvider: () -> KClass<T>?
) : Accessor<T> {
    private var runtimeTarget: T? = null

    override val targetClass by lazy { targetClassProvider() ?: runtimeTarget!!::class as KClass<T> }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    fun get(target: T): V {
        if (runtimeTarget == null) runtimeTarget = target
        return accessorProvider.get(target, propertyName)
    }

    fun set(target: T, value: V) {
        if (runtimeTarget == null) runtimeTarget = target
        accessorProvider.set(target, propertyName, value)
    }

    inline operator fun getValue(thisRef: T, property: KProperty<*>): V {
        return get(thisRef)
    }

    inline operator fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        set(thisRef, value)
    }
}

class StaticPropertyAccessor<T : Any, V>(
    val propertyName: String,
    val targetClassProvider: () -> KClass<T>
) : Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    fun get(): V {
        return accessorProvider.get(null, propertyName)
    }

    fun set(value: V) {
        accessorProvider.set(null, propertyName, value)
    }

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return this.get()
    }

    inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.set(value)
    }
}

class FunctionAccessor<T : Any>(
    val target: T,
    val functionName: String,
    val targetClassProvider: () -> KClass<T>
) : Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    operator fun invoke(vararg args: Any?): Any? {
        return accessorProvider.invoke(target, functionName, *args)
    }
}

class MemberFunctionAccessor<T : Any>(
    val functionName: String,
    val targetClassProvider: () -> KClass<T>?
) {
    private var runtimeTarget: T? = null
    private val targetClass by lazy { targetClassProvider() ?: runtimeTarget!!::class as KClass<T> }
    private val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    operator fun invoke(target: T, vararg args: Any?): Any? {
        runtimeTarget = target
        return accessorProvider.invoke(target, functionName, *args)
    }
}

class StaticFunctionAccessor<T : Any>(
    val functionName: String,
    private val targetClassProvider: () -> KClass<T>
) : Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    operator fun invoke(vararg args: Any?): Any? {
        return accessorProvider.invoke(null, functionName, *args)
    }
}

