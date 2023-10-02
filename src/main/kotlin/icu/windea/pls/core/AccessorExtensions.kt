@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.intellij.openapi.progress.*
import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

class SmartProperty<T : Any, V>(
    val target: T,
    val property: SmartMemberProperty<T, V>
) {
    fun get(): V = property.get(target)
    
    fun set(value: V) = property.set(target, value)
}

class SmartMemberProperty<T : Any, V>(
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
        try {
            val memberProperties = buildSet { addAll(targetClass.declaredMemberProperties); addAll(targetClass.memberProperties) }
            val memberFunctions = buildSet { addAll(targetClass.declaredMemberFunctions); addAll(targetClass.memberFunctions) }
            val property = memberProperties.find { it.name == propertyName }?.also { it.isAccessible = true }
            val getter = memberFunctions.find { it.isGetter(propertyName) }?.also { it.isAccessible = true }
            val setter = memberFunctions.find { it.isSetter(propertyName) }?.also { it.isAccessible = true }
            return DelegateProperty({ target ->
                if(!targetClass.isInstance(target)) cannotCast(target, targetClass)
                when {
                    property != null -> property.get(target) as V
                    getter != null -> getter.call(target) as V
                    else -> unsupported()
                }
            }, { target, value ->
                if(!targetClass.isInstance(target)) cannotCast(target, targetClass)
                when {
                    property != null && property is KMutableProperty1 -> (property as KMutableProperty1<T, in Any?>).set(target, value)
                    setter != null -> setter.call(target, value)
                    else -> unsupported()
                }
            })
        } catch(e: UnsupportedOperationException) {
            //java.lang.UnsupportedOperationException: Packages and file facades are not yet supported in Kotlin reflection.
            
            //TODO
        }
        
        return DelegateProperty({ unsupported() }, { _, _ -> unsupported() })
    }
}

class SmartStaticProperty<T : Any, V>(
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
        try {
            val staticProperties = targetClass.staticProperties
            val staticFunctions = targetClass.staticFunctions
            val property = staticProperties.find { it.name == propertyName }?.also { it.isAccessible = true }
            val getter = staticFunctions.find { it.isGetter(propertyName) }?.also { it.isAccessible = true }
            val setter = staticFunctions.find { it.isSetter(propertyName) }?.also { it.isAccessible = true }
            return DelegateProperty({
                when {
                    property != null -> property.get() as V
                    getter != null -> getter.call(null) as V
                    else -> unsupported()
                }
            }, { value ->
                when {
                    property != null && property is KMutableProperty0 -> (property as KMutableProperty0<in Any?>).set(value)
                    setter != null -> setter.call(null, value)
                    else -> unsupported()
                }
            })
        } catch(e: UnsupportedOperationException) {
            //java.lang.UnsupportedOperationException: Packages and file facades are not yet supported in Kotlin reflection.
            
            //TODO
        }
        
        return DelegateProperty({ unsupported() }, { unsupported() })
    }
}

class SmartFunction<T : Any>(
    val target: T,
    val function: SmartMemberFunction<T>
) {
    operator fun invoke(vararg args: Any?): Any? = function.invoke(target, *args)
}

class SmartMemberFunction<T : Any>(
    val targetClass: KClass<T>,
    val functionName: String
) {
    operator fun invoke(target: T, vararg args: Any?): Any? {
        if(!targetClass.isInstance(target)) cannotCast(target, targetClass)
        val expectedArgsSize = args.size + 1
        
        try {
            val declaredFunctions = buildSet { addAll(targetClass.declaredFunctions); addAll(targetClass.functions) }
            for(function in declaredFunctions) {
                if(function.name != functionName) continue
                if(function.parameters.size != expectedArgsSize) continue
                try {
                    function.isAccessible = true
                    return function.call(target, *args)
                } catch(e: Throwable) {
                    if(e is ProcessCanceledException) throw e
                    //ignore
                }
            }
        } catch(e: UnsupportedOperationException) {
            //java.lang.UnsupportedOperationException: Packages and file facades are not yet supported in Kotlin reflection.
            
            val targetJavaClass = targetClass.java
            val declaredMethods = buildSet { addAll(targetJavaClass.declaredMethods); addAll(targetJavaClass.methods) }
            for(method in declaredMethods) {
                if(Modifier.isStatic(method.modifiers)) continue
                if(method.name != functionName) continue
                if(method.parameters.size != expectedArgsSize) continue
                try {
                    method.isAccessible = true
                    return method.invoke(target, *args)
                } catch(e: Throwable) {
                    if(e is ProcessCanceledException) throw e
                    //ignore
                }
            }
        }
        
        unsupported()
    }
}

class SmartStaticFunction<T : Any>(
    val targetClass: KClass<T>,
    val functionName: String
) {
    operator fun invoke(vararg args: Any?): Any? {
        val expectedArgsSize = args.size
        
        try {
            val staticFunctions = targetClass.staticFunctions
            for(function in staticFunctions) {
                if(function.name != functionName) continue
                if(function.parameters.size != expectedArgsSize) continue
                try {
                    function.isAccessible = true
                    return function.call(null, *args)
                } catch(e: Throwable) {
                    if(e is ProcessCanceledException) throw e
                    //ignore
                }
            }
        } catch(e: UnsupportedOperationException) {
            //java.lang.UnsupportedOperationException: Packages and file facades are not yet supported in Kotlin reflection.
            
            val targetJavaClass = targetClass.java
            val staticMethods = targetJavaClass.declaredMethods.filter { Modifier.isStatic(it.modifiers) }
            for(method in staticMethods) {
                if(method.name != functionName) continue
                if(method.parameters.size != expectedArgsSize) continue
                try {
                    method.isAccessible = true
                    return method.invoke(null, *args)
                } catch(e: Throwable) {
                    if(e is ProcessCanceledException) throw e
                    //ignore
                }
            }
        }
        
        unsupported()
    }
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


inline fun <reified T : Any, V> T.property(propertyName: String): SmartProperty<T, V> {
    return SmartProperty(this, SmartMemberProperty(T::class, propertyName))
}

inline fun <V> Any.property(propertyName: String, targetClassName: String): SmartProperty<Any, V> {
    return SmartProperty(this, SmartMemberProperty(targetClassName.toKClass().cast(), propertyName))
}

inline fun <reified T : Any, V> memberProperty(propertyName: String): SmartMemberProperty<T, V> {
    return SmartMemberProperty(T::class, propertyName)
}

inline fun <V> memberProperty(propertyName: String, targetClassName: String): SmartMemberProperty<Any, V> {
    return SmartMemberProperty(targetClassName.toKClass().cast(), propertyName)
}

inline fun <reified T : Any, V> staticProperty(propertyName: String): SmartStaticProperty<T, V> {
    return SmartStaticProperty(T::class, propertyName)
}

inline fun <V> staticProperty(propertyName: String, targetClassName: String): SmartStaticProperty<Any, V> {
    return SmartStaticProperty(targetClassName.toKClass().cast(), propertyName)
}

inline fun <reified T : Any> T.function(functionName: String): SmartFunction<T> {
    return SmartFunction(this, SmartMemberFunction(T::class, functionName))
}

inline fun Any.function(functionName: String, targetClassName: String): SmartFunction<Any> {
    return SmartFunction(this, SmartMemberFunction(targetClassName.toKClass().cast(), functionName))
}

inline fun <reified T : Any> memberFunction(functionName: String): SmartMemberFunction<T> {
    return SmartMemberFunction(T::class, functionName)
}

inline fun memberFunction(functionName: String, targetClassName: String): SmartMemberFunction<Any> {
    return SmartMemberFunction(targetClassName.toKClass().cast(), functionName)
}

inline fun <reified T : Any> staticFunction(functionName: String): SmartStaticFunction<T> {
    return SmartStaticFunction(T::class, functionName)
}

inline fun staticFunction(functionName: String, targetClassName: String): SmartStaticFunction<Any> {
    return SmartStaticFunction(targetClassName.toKClass().cast(), functionName)
}


inline operator fun <T : Any, V> SmartMemberProperty<T, V>.getValue(thisRef: T, property: KProperty<*>): V {
    return this.get(thisRef)
}

inline operator fun <T : Any, V> SmartMemberProperty<T, V>.setValue(thisRef: T, property: KProperty<*>, value: V) {
    this.set(thisRef, value)
}

inline operator fun <T : Any, V> SmartStaticProperty<T, V>.getValue(thisRef: Any?, property: KProperty<*>): V {
    return this.get()
}

inline operator fun <T : Any, V> SmartStaticProperty<T, V>.setValue(thisRef: Any?, property: KProperty<*>, value: V) {
    this.set(value)
}