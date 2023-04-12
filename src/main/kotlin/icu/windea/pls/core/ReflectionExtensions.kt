@file:Suppress("unused")

package icu.windea.pls.core

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

inline fun <reified T : Any> T.member(memberName: String): Any? {
    val member = T::class.declaredMemberProperties.find { it.name == memberName } ?: return null
    member.isAccessible = true
    return member.get(this)
}

inline fun <reified T : Any> T.member(memberName: String, value: Any?) {
    val member = T::class.declaredMemberProperties.find { it.name == memberName } ?: return
    if(member !is KMutableProperty1) return
    @Suppress("UNCHECKED_CAST")
    member as KMutableProperty1<T,  in Any?>
    member.isAccessible = true
    member.set(this, value)
}

inline fun <reified T : Any> T.function(functionName: String, vararg args: Any?): FunctionsHolder {
    val functions = T::class.declaredMemberFunctions.filter { it.name == functionName && it.parameters.size == args.size }
    return FunctionsHolder(functions)
}

class FunctionsHolder(private val functions: List<KFunction<*>>) {
    operator fun invoke(vararg args: Any?): Any? {
        for(function in functions) {
            try {
                return function.call(*args)
            } catch(e: Throwable) {
                //ignore
            }
        }
        return null
    }
}