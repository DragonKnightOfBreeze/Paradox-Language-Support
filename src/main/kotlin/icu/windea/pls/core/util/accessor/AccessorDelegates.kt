package icu.windea.pls.core.util.accessor

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

//region Interfaces

/** 读取委托：从目标实例或静态上下文读取值。*/
interface ReadAccessorDelegate<T : Any, V> : AccessorDelegate {
    fun get(target: T?): V
}

/** 写入委托：向目标实例或静态上下文写入值。*/
interface WriteAccessorDelegate<T : Any, V> : AccessorDelegate {
    fun set(target: T?, value: V)
}

/** 调用委托：以反射方式调用函数/方法。*/
interface InvokeAccessorDelegate<T : Any> : AccessorDelegate {
    fun invoke(target: T?, vararg args: Any?): Any?
}

//endregion

//region Read Accessor Delegates

/** 空读取委托：调用将抛出 [UnsupportedAccessorException]。*/
object EmptyReadAccessorDelegate : ReadAccessorDelegate<Any, Any?> {
    override fun setAccessible() = true

    override fun get(target: Any?) = throw UnsupportedAccessorException()
}

/** 基于 Kotlin 成员属性的读取委托。*/
class KotlinMemberPropertyReadAccessorDelegate<T : Any, V>(
    val property: KProperty1<T, *>
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(target: T?): V {
        if (target == null) throw UnsupportedAccessorException()
        return AccessorRunner.runInAccessorDelegate {
            property.get(target) as V
        }
    }
}

/** 基于 Kotlin 顶层/静态属性的读取委托。*/
class KotlinPropertyReadAccessorDelegate<T : Any, V>(
    val property: KProperty0<*>
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(target: T?): V {
        if (target != null) throw UnsupportedAccessorException()
        return AccessorRunner.runInAccessorDelegate {
            property.get() as V
        }
    }
}

/** 基于 Kotlin getter 函数的读取委托。*/
class KotlinGetterReadAccessorDelegate<T : Any, V>(
    val getter: KFunction<*>
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        getter.isAccessible = true
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(target: T?): V {
        return AccessorRunner.runInAccessorDelegate {
            getter.call(target) as V
        }
    }
}

/** 基于 Java 字段的读取委托。*/
class JavaFieldReadAccessorDelegate<T : Any, V>(
    val field: Field
) : ReadAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        return field.trySetAccessible()
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(target: T?): V {
        return AccessorRunner.runInAccessorDelegate {
            field.get(target) as V
        }
    }
}

//endregion

//region Write Accessor Delegates

/** 空写入委托：调用将抛出 [UnsupportedAccessorException]。*/
object EmptyWriteAccessorDelegate : WriteAccessorDelegate<Any, Any?> {
    override fun setAccessible() = true

    override fun set(target: Any?, value: Any?) = throw UnsupportedAccessorException()
}

/** 基于 Kotlin 可变成员属性的写入委托。*/
class KotlinMemberPropertyWriteAccessorDelegate<T : Any, V>(
    val property: KMutableProperty1<T, *>
) : WriteAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(target: T?, value: V) {
        if (target == null) throw UnsupportedAccessorException()
        AccessorRunner.runInAccessorDelegate {
            property as KMutableProperty1<T, in Any?>
            property.set(target, value)
        }
    }
}

/** 基于 Kotlin 可变顶层/静态属性的写入委托。*/
class KotlinPropertyWriteAccessorDelegate<T : Any, V>(
    val property: KMutableProperty0<*>
) : WriteAccessorDelegate<T, V> {
    override fun setAccessible(): Boolean {
        property.isAccessible = true
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(target: T?, value: V) {
        if (target != null) throw UnsupportedAccessorException()
        AccessorRunner.runInAccessorDelegate {
            property as KMutableProperty0<in Any?>
            property.set(value)
        }
    }
}

/** 基于 Kotlin setter 函数的写入委托。*/
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

/** 基于 Java 字段的写入委托。*/
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

/** 空调用委托：调用将抛出 [UnsupportedAccessorException]。*/
object EmptyInvokeAccessorDelegate : InvokeAccessorDelegate<Any> {
    override fun setAccessible() = true

    override fun invoke(target: Any?, vararg args: Any?) = throw UnsupportedAccessorException()
}

/** 基于 Kotlin 函数的调用委托。*/
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

/** 基于 Java 方法的调用委托。*/
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
