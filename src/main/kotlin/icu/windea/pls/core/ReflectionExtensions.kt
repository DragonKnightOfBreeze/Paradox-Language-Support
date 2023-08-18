@file:Suppress("UNCHECKED_CAST")

package icu.windea.pls.core

import com.intellij.openapi.progress.*
import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}

class SmartMemberKProperty<T : Any, V>(
    val targetClass: KClass<T>,
    val propertyName: String
) {
    private class DelegateProperty<T : Any, V>(
        val getter: (T) -> V,
        val setter: (T, V) -> Unit
    )
    
    private val delegateProperty by lazy { doGetDelegateProperty() }
    
    fun get(target: T): V = delegateProperty.getter(target)
    
    fun set(target: T, value: V) = delegateProperty.setter(target, value)
    
    private fun doGetDelegateProperty(): DelegateProperty<T, V> {
        val allMemberProperties = targetClass.declaredMemberProperties
        val allMemberFunctions = targetClass.declaredMemberFunctions
        val property = allMemberProperties.find { it.name == propertyName }?.also { it.isAccessible = true }
        val javaField = property?.javaField?.also { it.isAccessible = true }
        val getter = allMemberFunctions.find { it.isGetter(propertyName) }?.also { it.isAccessible = true }
        val setter = allMemberFunctions.find { it.isSetter(propertyName) }?.also { it.isAccessible = true }
        try {
            return DelegateProperty({ target ->
                if(targetClass.isInstance(target)) cannotCast(target, targetClass)
                when {
                    javaField != null -> javaField.get(target) as V
                    property != null -> property.get(target) as V
                    getter != null -> getter.call(target) as V
                    else -> unsupported()
                }
            }, { target, value ->
                if(targetClass.isInstance(target)) cannotCast(target, targetClass)
                when {
                    javaField != null -> javaField.set(target, value)
                    property != null && property is KMutableProperty1 -> (property as KMutableProperty1<T, in Any?>).set(target, value)
                    setter != null -> setter.call(target, value)
                    else -> unsupported()
                }
            })
        } catch(e: Throwable) {
            if(e is ProcessCanceledException) throw e
            //ignore
            return DelegateProperty({ unsupported() }, { _, _ -> unsupported() })
        }
    }
}

@Suppress("UNCHECKED_CAST")
class SmartStaticKProperty<T : Any, V>(
    val targetClass: KClass<T>,
    val propertyName: String
) {
    private class DelegateProperty<V>(
        val getter: () -> V,
        val setter: (V) -> Unit
    )
    
    private val delegateProperty by lazy { doGetDelegateProperty() }
    
    fun get(): V = delegateProperty.getter()
    
    fun set(value: V) = delegateProperty.setter(value)
    
    private fun doGetDelegateProperty(): DelegateProperty<V> {
        val allStaticProperties = targetClass.staticProperties
        val allStaticFunctions = targetClass.staticFunctions
        val property = allStaticProperties.find { it.name == propertyName }?.also { it.isAccessible = true }
        val javaField = property?.javaField?.also { it.isAccessible = true }
        val getter = allStaticFunctions.find { it.isGetter(propertyName) }?.also { it.isAccessible = true }
        val setter = allStaticFunctions.find { it.isSetter(propertyName) }?.also { it.isAccessible = true }
        try {
            return DelegateProperty({
                when {
                    javaField != null -> javaField.get(null) as V
                    property != null -> property.get() as V
                    getter != null -> getter.call(null) as V
                    else -> unsupported()
                }
            }, { value ->
                when {
                    javaField != null -> javaField.set(null, value)
                    property != null && property is KMutableProperty0 -> (property as KMutableProperty0<in Any?>).set(value)
                    setter != null -> setter.call(null, value)
                    else -> unsupported()
                }
            })
        } catch(e: Throwable) {
            if(e is ProcessCanceledException) throw e
            //ignore
            return DelegateProperty({ unsupported() }, { unsupported() })
        }
    }
}

class SmartKProperty<T : Any, V>(
    val target: T,
    val property: SmartMemberKProperty<T,V>
) {
    fun get(): V = property.get(target)
    
    fun set(value: V) = property.set(target, value)
}

class SmartMemberKFunction<T : Any>(
    val targetClass: KClass<T>,
    val functionName: String
) {
    val matchedFunctions by lazy { targetClass.declaredFunctions.filter { it.name == functionName } } 
    
    operator fun invoke(target: T, vararg args: Any?): Any? = doInvoke(target, args)
    
    private fun doInvoke(target: T, args: Array<out Any?>): Any? {
        if(!targetClass.isInstance(target)) cannotCast(target, targetClass)
        val expectedArgsSize = args.size + 1
        for(function in matchedFunctions) {
            if(function.parameters.size != expectedArgsSize) continue
            try {
                function.isAccessible = true
                return function.call(target, *args)
            } catch(e: Throwable) {
                if(e is ProcessCanceledException) throw e
                //ignore
            }
        }
        return null
    }
}

class SmartStaticKFunction<T : Any>(
    val targetClass: KClass<T>,
    val functionName: String
) {
    val matchedFunctions by lazy { targetClass.staticFunctions.filter { it.name == functionName } }
    
    operator fun invoke(vararg args: Any?): Any? = doInvoke(args)
    
    private fun doInvoke(args: Array<out Any?>): Any? {
        val expectedArgsSize = args.size
        for(function in matchedFunctions) {
            if(function.parameters.size != expectedArgsSize) continue
            try {
                function.isAccessible = true
                return function.call(null, *args)
            } catch(e: Throwable) {
                if(e is ProcessCanceledException) throw e
                //ignore
            }
        }
        unsupported()
    }
}

class SmartKFunction<T : Any>(
    val target: T,
    val function: SmartMemberKFunction<T>
) {
    operator fun invoke(vararg args: Any?): Any? = function.invoke(target, args)
}

private fun KFunction<*>.isGetter(propertyName: String): Boolean {
    if(parameters.size != 1) return false
    val suffix = propertyName.replaceFirstChar { it.uppercaseChar() }
    if(name == "get$suffix") return true
    if(returnType.classifier == Boolean::class && name == "is$suffix") return true
    return false
}

private fun KFunction<*>.isSetter(propertyName: String): Boolean {
    if(parameters.size != 2) return false
    val suffix = propertyName.replaceFirstChar { it.uppercaseChar() }
    if(name == "set$suffix") return true
    return false
}

private fun unsupported(): Nothing {
    throw UnsupportedOperationException()
}

private fun cannotCast(target: Any, targetClass: KClass<out Any>): Nothing {
    throw ClassCastException("Actual target class ${target::class.qualifiedName} cannot cast to target class ${targetClass.qualifiedName}")
}

inline fun <reified T : Any, V> memberProperty(propertyName: String, targetClassName: String? = null): SmartMemberKProperty<T, V> {
    return SmartMemberKProperty(targetClassName?.toKClass()?.cast() ?: T::class, propertyName)
}

inline fun <reified T : Any, V> staticProperty(propertyName: String, targetClassName: String? = null): SmartStaticKProperty<T, V> {
    return SmartStaticKProperty(targetClassName?.toKClass()?.cast() ?: T::class, propertyName)
}

inline fun <reified T : Any, V> T.property(propertyName: String, targetClassName: String? = null): SmartKProperty<T, V> {
    return SmartKProperty(this, SmartMemberKProperty(targetClassName?.toKClass()?.cast() ?: T::class, propertyName))
}

inline fun <reified T : Any> memberFunction(functionName: String, targetClassName: String? = null): SmartMemberKFunction<T> {
    return SmartMemberKFunction(targetClassName?.toKClass()?.cast() ?: T::class, functionName)
}

inline fun <reified T : Any> staticFunction(functionName: String, targetClassName: String? = null): SmartStaticKFunction<T> {
    return SmartStaticKFunction(targetClassName?.toKClass()?.cast() ?: T::class, functionName)
}

inline fun <reified T : Any> T.function(functionName: String, targetClassName: String? = null): SmartKFunction<T> {
    return SmartKFunction(this, SmartMemberKFunction(targetClassName?.toKClass()?.cast() ?: T::class, functionName))
}

operator fun <T : Any, V> SmartMemberKProperty<T, V>.getValue(thisRef: T, property: KProperty<*>): V {
    return this.get(thisRef)
}

operator fun <T : Any, V> SmartMemberKProperty<T, V>.setValue(thisRef: T, property: KProperty<*>, value: V) {
    this.set(thisRef, value)
}

operator fun <T : Any, V> SmartStaticKProperty<T, V>.getValue(thisRef: Any?, property: KProperty<*>): V {
    return this.get()
}

operator fun <T : Any, V> SmartStaticKProperty<T, V>.setValue(thisRef: Any?, property: KProperty<*>, value: V) {
    this.set(value)
}