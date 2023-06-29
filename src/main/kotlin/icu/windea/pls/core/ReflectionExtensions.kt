@file:Suppress("unused")

package icu.windea.pls.core

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
        val property = allMemberProperties.find { it.name == propertyName }
        if(property != null) {
            try {
                property.isAccessible = true
                return mutableProperty({ property.get(target) as V }, { unsupported() })
            } catch(e: Exception) {
                //ignore
            }
        }
        val allMemberFunctions = targetClass.declaredMemberFunctions
        val getter = allMemberFunctions.find { it.isGetter() }
        val setter = allMemberFunctions.find { it.isSetter() }
        if(getter != null || setter != null) {
            try {
                getter?.isAccessible = true
                setter?.isAccessible = true
                return mutableProperty({ (getter?.call(target) ?: unsupported()) as V }, { setter?.call(target, it) ?: unsupported() })
            } catch(e: Exception) {
                //ignore
            }
        }
        return mutableProperty({ unsupported() }, { unsupported() })
    }
    
    private fun doGetDelegatePropertyStatic(): MutableProperty<V> {
        val allStaticProperties = targetClass.staticProperties
        val property = allStaticProperties.find { it.name == propertyName }
        if(property != null) {
            try {
                property.isAccessible = true
                return mutableProperty({ property.get() as V }, { unsupported() })
            } catch(e: Exception) {
                //ignore
            }
        }
        val allStaticFunctions = targetClass.staticFunctions
        val getter = allStaticFunctions.find { it.isGetter() }
        val setter = allStaticFunctions.find { it.isSetter() }
        if(getter != null || setter != null) {
            try {
                getter?.isAccessible = true
                setter?.isAccessible = true
                return mutableProperty({ (getter?.call(null) ?: unsupported()) as V }, { setter?.call(null, it) ?: unsupported() })
            } catch(e: Exception) {
                //ignore
            }
        }
        return mutableProperty({ unsupported() }, { unsupported() })
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