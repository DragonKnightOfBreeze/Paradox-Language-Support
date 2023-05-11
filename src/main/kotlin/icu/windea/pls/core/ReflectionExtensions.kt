@file:Suppress("unused")

package icu.windea.pls.core

import java.lang.reflect.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

@Suppress("UNCHECKED_CAST")
fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}

inline fun <reified T : Any> T.member(memberName: String): Any? {
    val kClass = T::class
    val property = kClass.declaredMemberProperties.find { p -> p.name == memberName }
    if(property != null) {
        property.isAccessible = true
        return property.get(this)
    }
    val getter = kClass.declaredMemberFunctions.find p@{ f ->
        if(f.parameters.size != 1) return@p false
        val suffix = memberName.replaceFirstChar { it.uppercaseChar() }
        if(f.name == "get$suffix") return@p true
        if(f.returnType.classifier == Boolean::class && f.name == "is$suffix") return@p true
        false
    }
    if(getter != null) {
        try {
            getter.isAccessible = true
            return getter.call(this)
        } catch(e: Exception) {
            //ignore
        }
    }
    return null
}

inline fun <reified T : Any> staticProperty(memberName: String): Any? {
    val kClass = T::class
    val property = kClass.staticProperties.find { p -> p.name == memberName }
    if(property != null) {
        property.isAccessible = true
        return property.get()
    }
    val getter = kClass.staticFunctions.find p@{ f ->
        if(f.parameters.size != 1) return@p false
        val suffix = memberName.replaceFirstChar { it.uppercaseChar() }
        if(f.name == "get$suffix") return@p true
        if(f.returnType.classifier == Boolean::class && f.name == "is$suffix") return@p true
        false
    }
    if(getter != null) {
        try {
            getter.isAccessible = true
            return getter.call()
        } catch(e: Exception) {
            //ignore
        }
    }
    return null
}

inline fun <reified T : Any> T.member(memberName: String, value: Any?) {
    val kClass = T::class
    val property = kClass.declaredMemberProperties.find { p -> p.name == memberName }
    if(property is KMutableProperty1) {
        @Suppress("UNCHECKED_CAST")
        property as KMutableProperty1<T, in Any?>
        property.isAccessible = true
        return property.set(this, value)
    }
    val setters = kClass.declaredMemberFunctions.filter p@{ f ->
        if(f.parameters.size != 2) return@p false
        val suffix = memberName.replaceFirstChar { it.uppercaseChar() }
        if(f.name == "set$suffix") return@p true
        false
    }
    if(setters.isNotEmpty()) {
        for(setter in setters) {
            try {
                setter.isAccessible = true
                setter.call(this)
                return
            } catch(e: Exception) {
                //ignore
            }
        }
    }
}

inline fun <reified T : Any> staticProperty(memberName: String, value: Any?) {
    val kClass = T::class
    val property = kClass.staticProperties.find { p -> p.name == memberName }
    if(property is KMutableProperty0) {
        @Suppress("UNCHECKED_CAST")
        property as KMutableProperty0<in Any?>
        property.isAccessible = true
        return property.set(value)
    }
    val setters = kClass.staticFunctions.filter p@{ f ->
        if(f.parameters.size != 2) return@p false
        val suffix = memberName.replaceFirstChar { it.uppercaseChar() }
        if(f.name == "set$suffix") return@p true
        false
    }
    if(setters.isNotEmpty()) {
        for(setter in setters) {
            try {
                setter.isAccessible = true
                setter.call()
                return
            } catch(e: Exception) {
                //ignore
            }
        }
    }
}

inline fun <reified T : Any> T.function(functionName: String, vararg args: Any?): FunctionsHolder {
    val kClass = T::class
    val functions = kClass.declaredFunctions.filter { it.name == functionName && it.parameters.size == args.size + 1 }
    return FunctionsHolder(this, functions)
}

inline fun <reified T : Any> staticFunction(functionName: String, vararg args: Any?): FunctionsHolder {
    val kClass = T::class
    val functions = kClass.staticFunctions.filter { it.name == functionName && it.parameters.size == args.size + 1 }
    return FunctionsHolder(null, functions)
}

class FunctionsHolder(private val target: Any?, private val functions: List<KFunction<*>>) {
    operator fun invoke(vararg args: Any?): Any? {
        for(function in functions) {
            try {
                function.isAccessible = true
                return function.call(target, *args)
            } catch(e: Throwable) {
                //ignore
            }
        }
        return null
    }
}