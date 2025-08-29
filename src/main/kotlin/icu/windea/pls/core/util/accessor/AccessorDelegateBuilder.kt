@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

object AccessorDelegateBuilder {
    object Read {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any, V> empty(): ReadAccessorDelegate<T, V> = EmptyReadAccessorDelegate as ReadAccessorDelegate<T, V>

        fun <T : Any, V> fromProperty(property: KProperty1<T, *>) = KotlinMemberPropertyReadAccessorDelegate<T, V>(property)

        fun <T : Any, V> fromProperty(property: KProperty0<*>) = KotlinPropertyReadAccessorDelegate<T, V>(property)

        fun <T : Any, V> fromGetter(getter: KFunction<*>) = KotlinGetterReadAccessorDelegate<T, V>(getter)

        fun <T : Any, V> fromJavaField(field: Field) = JavaFieldReadAccessorDelegate<T, V>(field)
    }

    object Write {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any, V> empty(): WriteAccessorDelegate<T, V> = EmptyWriteAccessorDelegate as WriteAccessorDelegate<T, V>

        fun <T : Any, V> fromProperty(property: KProperty1<T,*>) = if (property is KMutableProperty1) KotlinMemberPropertyWriteAccessorDelegate<T, V>(property) else null

        fun <T : Any, V> fromProperty(property: KMutableProperty1<T,*>) = KotlinMemberPropertyWriteAccessorDelegate<T, V>(property)

        fun <T : Any, V> fromProperty(property: KProperty0<*>) = if (property is KMutableProperty0) KotlinPropertyWriteAccessorDelegate<T, V>(property) else null

        fun <T : Any, V> fromProperty(property: KMutableProperty0<*>) = KotlinPropertyWriteAccessorDelegate<T, V>(property)

        fun <T : Any, V> fromSetter(getter: KFunction<*>) = KotlinSetterWriteAccessorDelegate<T, V>(getter)

        fun <T : Any, V> fromJavaField(field: Field) = JavaFieldWriteAccessorDelegate<T, V>(field)
    }

    object Invoke {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> empty(): InvokeAccessorDelegate<T> = EmptyInvokeAccessorDelegate as InvokeAccessorDelegate<T>

        fun <T : Any> fromFunction(function: KFunction<*>) = KotlinFunctionInvokeAccessorDelegate<T>(function)

        fun <T : Any> fromMethod(method: Method) = JavaMethodInvokeAccessorDelegate<T>(method)
    }
}
