@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core.util.accessor

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * 基于运行时对象的属性访问器。
 *
 * @property target 目标实例。
 * @property propertyName 属性名。
 * @property targetClassProvider 目标类型提供者（用于懒加载 [targetClass]）。
 */
class PropertyAccessor<T : Any, V>(
    val target: T,
    val propertyName: String,
    val targetClassProvider: () -> KClass<T>
): Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    /** 读取属性值。*/
    fun get(): V {
        return accessorProvider.get(target, propertyName)
    }

    /** 写入属性值。*/
    fun set(value: V) {
        accessorProvider.set(target, propertyName, value)
    }
}

/**
 * 基于目标类型与成员属性名的访问器（调用时再给出实例）。
 *
 * @property propertyName 属性名。
 * @property targetClassProvider 目标类型提供者；若为 `null` 则首次调用时从实例推断。
 */
class MemberPropertyAccessor<T : Any, V>(
    val propertyName: String,
    val targetClassProvider: () -> KClass<T>?
) : Accessor<T> {
    private var runtimeTarget: T? = null

    @Suppress("UNCHECKED_CAST")
    override val targetClass by lazy { targetClassProvider() ?: runtimeTarget!!::class as KClass<T> }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    /** 读取属性值。*/
    fun get(target: T): V {
        if (runtimeTarget == null) runtimeTarget = target
        return accessorProvider.get(target, propertyName)
    }

    /** 写入属性值。*/
    fun set(target: T, value: V) {
        if (runtimeTarget == null) runtimeTarget = target
        accessorProvider.set(target, propertyName, value)
    }

    /** 委托读取：`val v by memberProperty(...)`。*/
    inline operator fun getValue(thisRef: T, property: KProperty<*>): V {
        return get(thisRef)
    }

    /** 委托写入：`var v by memberProperty(...)`。*/
    inline operator fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        set(thisRef, value)
    }
}

/**
 * 基于目标类型与静态属性名的访问器。
 *
 * @property propertyName 属性名。
 * @property targetClassProvider 目标类型提供者。
 */
class StaticPropertyAccessor<T : Any, V>(
    val propertyName: String,
    val targetClassProvider: () -> KClass<T>
) : Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    /** 读取静态属性值。*/
    fun get(): V {
        return accessorProvider.get(null, propertyName)
    }

    /** 写入静态属性值。*/
    fun set(value: V) {
        accessorProvider.set(null, propertyName, value)
    }

    /** 委托读取静态属性值。*/
    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return this.get()
    }

    /** 委托写入静态属性值。*/
    inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.set(value)
    }
}

/**
 * 基于运行时对象的函数访问器。
 *
 * @property target 目标实例。
 * @property functionName 函数名。
 * @property targetClassProvider 目标类型提供者。
 */
class FunctionAccessor<T : Any>(
    val target: T,
    val functionName: String,
    val targetClassProvider: () -> KClass<T>
) : Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    /** 调用成员函数。*/
    operator fun invoke(vararg args: Any?): Any? {
        return accessorProvider.invoke(target, functionName, *args)
    }
}

/**
 * 基于目标类型与成员函数名的访问器（调用时再给出实例）。
 *
 * @property functionName 函数名。
 * @property targetClassProvider 目标类型提供者；若为 `null` 则首次调用时从实例推断。
 */
class MemberFunctionAccessor<T : Any>(
    val functionName: String,
    val targetClassProvider: () -> KClass<T>?
) {
    private var runtimeTarget: T? = null
    @Suppress("UNCHECKED_CAST")
    private val targetClass by lazy { targetClassProvider() ?: runtimeTarget!!::class as KClass<T> }
    private val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    /** 调用成员函数。*/
    operator fun invoke(target: T, vararg args: Any?): Any? {
        runtimeTarget = target
        return accessorProvider.invoke(target, functionName, *args)
    }
}

/**
 * 基于目标类型与静态函数名的访问器。
 *
 * @property functionName 函数名。
 * @property targetClassProvider 目标类型提供者。
 */
class StaticFunctionAccessor<T : Any>(
    val functionName: String,
    private val targetClassProvider: () -> KClass<T>
) : Accessor<T> {
    override val targetClass by lazy { targetClassProvider() }
    override val accessorProvider by lazy { AccessorProviderCache.get(targetClass) }

    /** 调用静态函数。*/
    operator fun invoke(vararg args: Any?): Any? {
        return accessorProvider.invoke(null, functionName, *args)
    }
}

