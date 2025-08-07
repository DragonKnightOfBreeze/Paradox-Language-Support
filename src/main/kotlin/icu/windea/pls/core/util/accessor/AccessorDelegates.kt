@file:Suppress("UNCHECKED_CAST")

package icu.windea.pls.core.util.accessor

import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.jvm.*

//region Interfaces

interface ReadAccessorDelegate<T : Any, V> : AccessorDelegate {
    fun get(target: T?): V
}

interface WriteAccessorDelegate<T : Any, V> : AccessorDelegate {
    fun set(target: T?, value: V)
}

interface InvokeAccessorDelegate<T : Any> : AccessorDelegate {
    fun invoke(target: T?, vararg args: Any?): Any?
}

//endregion

//region Read Accessor Delegates

object EmptyReadAccessorDelegate : ReadAccessorDelegate<Any, Any?> {
    override fun setAccessible() = true

    override fun get(target: Any?) = throw UnsupportedAccessorException()
}

class KotlinMemberPropertyReadAccessorDelegate<T : Any, V>(
    val property: KProperty1<T, *>
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    override fun get(target: T?): V {
        if (target == null) throw UnsupportedAccessorException()
        return AccessorRunner.runInAccessorDelegate {
            property.get(target) as V
        }
    }
}

class KotlinPropertyReadAccessorDelegate<T : Any, V>(
    val property: KProperty0<*>
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    override fun get(target: T?): V {
        if (target != null) throw UnsupportedAccessorException()
        return AccessorRunner.runInAccessorDelegate {
            property.get() as V
        }
    }
}

class KotlinGetterReadAccessorDelegate<T : Any, V>(
    val getter: KFunction<*>
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        getter.isAccessible = true
        return true
    }

    override fun get(target: T?): V {
        return AccessorRunner.runInAccessorDelegate {
            getter.call(target) as V
        }
    }
}

class JavaFieldReadAccessorDelegate<T : Any, V>(
    val field: Field
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        return field.trySetAccessible()
    }

    override fun get(target: T?): V {
        return AccessorRunner.runInAccessorDelegate {
            field.get(target) as V
        }
    }
}

//endregion

//region Write Accessor Delegates

object EmptyWriteAccessorDelegate : WriteAccessorDelegate<Any, Any?> {
    override fun setAccessible() = true

    override fun set(target: Any?, value: Any?) = throw UnsupportedAccessorException()
}

class KotlinMemberPropertyWriteAccessorDelegate<T : Any, V>(
    val property: KMutableProperty1<T, *>
) : WriteAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    override fun set(target: T?, value: V) {
        if (target == null) throw UnsupportedAccessorException()
        AccessorRunner.runInAccessorDelegate {
            property as KMutableProperty1<T, in Any?>
            property.set(target, value)
        }
    }
}

class KotlinPropertyWriteAccessorDelegate<T : Any, V>(
    val property: KMutableProperty0<*>
) : WriteAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    override fun set(target: T?, value: V) {
        if (target != null) throw UnsupportedAccessorException()
        AccessorRunner.runInAccessorDelegate {
            property as KMutableProperty0<in Any?>
            property.set(value)
        }
    }
}

class KotlinSetterWriteAccessorDelegate<T : Any, V>(
    val getter: KFunction<*>
) : WriteAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        getter.isAccessible = true
        return true
    }

    override fun set(target: T?, value: V) {
        AccessorRunner.runInAccessorDelegate {
            getter.call(target, value)
        }
    }
}

class JavaFieldWriteAccessorDelegate<T : Any, V>(
    val field: Field
) : WriteAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        return field.trySetAccessible()
    }

    override fun set(target: T?, value: V) {
        AccessorRunner.runInAccessorDelegate {
            field.set(target, value)
        }
    }
}

//endregion

//region Invoke Accessor Delegates

object EmptyInvokeAccessorDelegate : InvokeAccessorDelegate<Any> {
    override fun setAccessible() = true

    override fun invoke(target: Any?, vararg args: Any?) = throw UnsupportedAccessorException()
}

class KotlinFunctionInvokeAccessorDelegate<T : Any>(
    val function: KFunction<*>
) : InvokeAccessorDelegate<T> {
    override fun setAccessible(): Boolean {
        function.isAccessible = true
        return true
    }

    override fun invoke(target: T?, vararg args: Any?): Any? {
        return AccessorRunner.runInAccessorDelegate {
            function.call(target, *args)
        }
    }
}

class JavaMethodInvokeAccessorDelegate<T : Any>(
    val method: Method
) : InvokeAccessorDelegate<T> {
    override fun setAccessible(): Boolean {
        return method.trySetAccessible()
    }

    override fun invoke(target: T?, vararg args: Any?): Any? {
        return AccessorRunner.runInAccessorDelegate {
            method.invoke(target, *args)
        }
    }
}

//endregion
