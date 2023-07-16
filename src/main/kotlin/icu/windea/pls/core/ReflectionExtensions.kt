@file:Suppress("unused")

package icu.windea.pls.core

import com.intellij.openapi.progress.*
import icu.windea.pls.core.util.*
import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

@Suppress("UNCHECKED_CAST")
fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}

@Suppress("UNCHECKED_CAST")
class SmartKProperty<T : Any, V>(
    val target: T?,
    val targetClass: KClass<T>,
    val propertyName: String
) : MutableProperty<V> {
    private val delegateProperty by lazy {
        if(target != null) {
            doGetDelegateProperty(target)
        } else {
            doGetDelegatePropertyStatic()
        }
    }
    
    override fun get() = delegateProperty.get()
    
    override fun set(value: V) = delegateProperty.set(value)
    
    private fun doGetDelegateProperty(target: T): MutableProperty<V> {
        val allMemberProperties = targetClass.declaredMemberProperties
        val allMemberFunctions = targetClass.declaredMemberFunctions
        val property = allMemberProperties.find { it.name == propertyName }?.also { it.isAccessible = true }
        val javaField = property?.javaField?.also { it.isAccessible = true }
        val getter = allMemberFunctions.find { it.isGetter() }?.also { it.isAccessible = true }
        val setter = allMemberFunctions.find { it.isSetter() }?.also { it.isAccessible = true }
        try {
            return mutableProperty({
                when {
                    javaField != null -> javaField.get(target) as V
                    property != null -> property.get(target) as V
                    getter != null -> getter.call(target) as V
                    else -> unsupported()
                }
            }, {
                when {
                    javaField != null -> javaField.set(target, it)
                    property != null && property is KMutableProperty1 -> (property as KMutableProperty1<T, in Any?>).set(target, it)
                    setter != null -> setter.call(target, it)
                    else -> unsupported()
                }
            })
        } catch(e: Throwable) {
            if(e is ProcessCanceledException) throw e
            //ignore
            return mutableProperty({ unsupported() }, { unsupported() })
        }
    }
    
    private fun doGetDelegatePropertyStatic(): MutableProperty<V> {
        val allStaticProperties = targetClass.staticProperties
        val allStaticFunctions = targetClass.staticFunctions
        val property = allStaticProperties.find { it.name == propertyName }?.also { it.isAccessible = true }
        val javaField = property?.javaField?.also { it.isAccessible = true }
        val getter = allStaticFunctions.find { it.isGetter() }?.also { it.isAccessible = true }
        val setter = allStaticFunctions.find { it.isSetter() }?.also { it.isAccessible = true }
        try {
            return mutableProperty({
                when {
                    javaField != null -> javaField.get(null) as V
                    property != null -> property.get() as V
                    getter != null -> getter.call(null) as V
                    else -> unsupported()
                }
            }, {
                when {
                    javaField != null -> javaField.set(null, it)
                    property != null && property is KMutableProperty0 -> (property as KMutableProperty0<in Any?>).set(it)
                    setter != null -> setter.call(null, it) 
                    else -> unsupported()
                }
            })
        } catch(e: Throwable) {
            if(e is ProcessCanceledException) throw e
            //ignore
            return mutableProperty({ unsupported() }, { unsupported() })
        }
    }
    
    private fun KFunction<*>.isGetter(): Boolean {
        if(parameters.size != 1) return false
        val suffix = propertyName.replaceFirstChar { it.uppercaseChar() }
        if(name == "get$suffix") return true
        if(returnType.classifier == Boolean::class && name == "is$suffix") return true
        return false
    }
    
    private fun KFunction<*>.isSetter(): Boolean {
        if(parameters.size != 2) return false
        val suffix = propertyName.replaceFirstChar { it.uppercaseChar() }
        if(name == "set$suffix") return true
        return false
    }
    
    private fun unsupported(): Nothing {
        throw UnsupportedOperationException()
    }
}

inline fun <reified T : Any, V> T.property(propertyName: String): SmartKProperty<T, V> {
    return SmartKProperty(this, T::class, propertyName)
}

inline fun <reified T : Any, V> staticProperty(propertyName: String): SmartKProperty<T, V> {
    return SmartKProperty(null, T::class, propertyName)
}

class SmartKFunction<T : Any>(
    val target: T?,
    val targetClass: KClass<T>,
    val functionName: String
) {
    operator fun invoke(vararg args: Any?): Any? {
        return if(target != null) {
            doInvoke(target, args)
        } else {
            doInvokeStatic(args)
        }
    }
    
    private fun doInvoke(target: T, args: Array<out Any?>): Any? {
        val allFunctions = targetClass.declaredFunctions
        val expectedArgsSize = args.size + 1
        for(function in allFunctions) {
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
        return null
    }
    
    private fun doInvokeStatic(args: Array<out Any?>): Any? {
        val allFunctions = targetClass.staticFunctions
        val expectedArgsSize = args.size
        for(function in allFunctions) {
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
        unsupported()
    }
    
    private fun unsupported(): Nothing {
        throw UnsupportedOperationException()
    }
}

inline fun <reified T : Any> T.function(functionName: String): SmartKFunction<T> {
    return SmartKFunction(this, T::class, functionName)
}

inline fun <reified T : Any> staticFunction(functionName: String): SmartKFunction<T> {
    return SmartKFunction(null, T::class, functionName)
}