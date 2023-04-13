@file:Suppress("unused")

package icu.windea.pls.core

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

inline fun <reified T : Any> T.member(memberName: String): Any? {
    val kClass = T::class
    val member = kClass.declaredMemberProperties.find { it.name == memberName } 
    if(member != null) {
        member.isAccessible = true
        return member.get(this)
    }
    val getter = kClass.declaredMemberFunctions.find p@{ 
        if(it.parameters.size != 1) return@p false
        val suffix = memberName.replaceFirstChar { it.uppercaseChar() }
        if(it.name == "get$suffix") return@p true
        if(it.returnType.classifier == Boolean::class && it.name == "is$suffix") return@p true
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

inline fun <reified T : Any> T.member(memberName: String, value: Any?) {
    val kClass = T::class
    val member = kClass.declaredMemberProperties.find { it.name == memberName }
    if(member is KMutableProperty1) {
        @Suppress("UNCHECKED_CAST")
        member as KMutableProperty1<T, in Any?>
        member.isAccessible = true
        return member.set(this, value)
    }
    val setters = kClass.declaredMemberFunctions.filter p@{
        if(it.parameters.size != 2) return@p false
        val suffix = memberName.replaceFirstChar { it.uppercaseChar() }
        if(it.name == "set$suffix") return@p true
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

inline fun <reified T : Any> T.function(functionName: String, vararg args: Any?): FunctionsHolder {
    val kClass = T::class
    val functions = kClass.declaredFunctions.filter { it.name == functionName && it.parameters.size == args.size + 1 }
    return FunctionsHolder(this, functions)
}

class FunctionsHolder(private val target: Any, private val functions: List<KFunction<*>>) {
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