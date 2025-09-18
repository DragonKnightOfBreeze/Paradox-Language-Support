@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * 访问器委托构建器。
 *
 * 提供从 Kotlin/Java 反射对象创建读/写/调用委托的工厂方法，
 * 并在创建时尝试提升可访问性（见 [AccessorDelegate.setAccessible]）。
 */
object AccessorDelegateBuilder {
    /** 读取委托工厂。*/
    object Read {
        /** 空读取委托（始终抛出 [UnsupportedAccessorException]）。*/
        @Suppress("UNCHECKED_CAST")
        fun <T : Any, V> empty(): ReadAccessorDelegate<T, V> = EmptyReadAccessorDelegate as ReadAccessorDelegate<T, V>

        /** 基于 Kotlin 成员属性创建读取委托。*/
        fun <T : Any, V> fromProperty(property: KProperty1<T, *>) = KotlinMemberPropertyReadAccessorDelegate<T, V>(property)

        /** 基于 Kotlin 顶层/静态属性创建读取委托。*/
        fun <T : Any, V> fromProperty(property: KProperty0<*>) = KotlinPropertyReadAccessorDelegate<T, V>(property)

        /** 基于 Kotlin getter 函数创建读取委托。*/
        fun <T : Any, V> fromGetter(getter: KFunction<*>) = KotlinGetterReadAccessorDelegate<T, V>(getter)

        /** 基于 Java 字段创建读取委托。*/
        fun <T : Any, V> fromJavaField(field: Field) = JavaFieldReadAccessorDelegate<T, V>(field)
    }

    /** 写入委托工厂。*/
    object Write {
        /** 空写入委托（始终抛出 [UnsupportedAccessorException]）。*/
        @Suppress("UNCHECKED_CAST")
        fun <T : Any, V> empty(): WriteAccessorDelegate<T, V> = EmptyWriteAccessorDelegate as WriteAccessorDelegate<T, V>

        /** 基于 Kotlin 成员属性创建写入委托（只在属性可变时返回）。*/
        fun <T : Any, V> fromProperty(property: KProperty1<T, *>) = if (property is KMutableProperty1) KotlinMemberPropertyWriteAccessorDelegate<T, V>(property) else null

        /** 基于 Kotlin 可变成员属性创建写入委托。*/
        fun <T : Any, V> fromProperty(property: KMutableProperty1<T, *>) = KotlinMemberPropertyWriteAccessorDelegate<T, V>(property)

        /** 基于 Kotlin 顶层/静态属性创建写入委托（只在属性可变时返回）。*/
        fun <T : Any, V> fromProperty(property: KProperty0<*>) = if (property is KMutableProperty0) KotlinPropertyWriteAccessorDelegate<T, V>(property) else null

        /** 基于 Kotlin 可变顶层/静态属性创建写入委托。*/
        fun <T : Any, V> fromProperty(property: KMutableProperty0<*>) = KotlinPropertyWriteAccessorDelegate<T, V>(property)

        /** 基于 Kotlin setter 函数创建写入委托。*/
        fun <T : Any, V> fromSetter(getter: KFunction<*>) = KotlinSetterWriteAccessorDelegate<T, V>(getter)

        /** 基于 Java 字段创建写入委托。*/
        fun <T : Any, V> fromJavaField(field: Field) = JavaFieldWriteAccessorDelegate<T, V>(field)
    }

    /** 调用委托工厂。*/
    object Invoke {
        /** 空调用委托（始终抛出 [UnsupportedAccessorException]）。*/
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> empty(): InvokeAccessorDelegate<T> = EmptyInvokeAccessorDelegate as InvokeAccessorDelegate<T>

        /** 基于 Kotlin 函数创建调用委托。*/
        fun <T : Any> fromFunction(function: KFunction<*>) = KotlinFunctionInvokeAccessorDelegate<T>(function)

        /** 基于 Java 方法创建调用委托。*/
        fun <T : Any> fromMethod(method: Method) = JavaMethodInvokeAccessorDelegate<T>(method)
    }
}
