@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

import icu.windea.pls.core.cast
import icu.windea.pls.core.toKClass
import kotlin.reflect.KClass

object AccessorBuilder {
    fun <T : Any, V> property(target: T, propertyName: String, targetClass: KClass<T>): PropertyAccessor<T, V> {
        return PropertyAccessor(target, propertyName) { targetClass }
    }

    fun <V> property(target: Any, propertyName: String, targetClassName: String?): PropertyAccessor<Any, V> {
        return PropertyAccessor(target, propertyName) { targetClassName?.toKClass()?.cast() ?: target::class.cast() }
    }

    fun <T : Any, V> memberProperty(propertyName: String, targetClass: KClass<T>): MemberPropertyAccessor<T, V> {
        return MemberPropertyAccessor(propertyName) { targetClass }
    }

    fun <V> memberProperty(propertyName: String, targetClassName: String?): MemberPropertyAccessor<Any, V> {
        return MemberPropertyAccessor(propertyName) { targetClassName?.toKClass()?.cast() }
    }

    fun <T : Any, V> staticProperty(propertyName: String, targetClass: KClass<T>): StaticPropertyAccessor<T, V> {
        return StaticPropertyAccessor(propertyName) { targetClass }
    }

    fun <V> staticProperty(propertyName: String, targetClassName: String): StaticPropertyAccessor<Any, V> {
        return StaticPropertyAccessor(propertyName) { targetClassName.toKClass().cast() }
    }

    fun <T : Any> function(target: T, functionName: String, targetClass: KClass<T>): FunctionAccessor<T> {
        return FunctionAccessor(target, functionName) { targetClass }
    }

    fun function(target: Any, functionName: String, targetClassName: String?): FunctionAccessor<Any> {
        return FunctionAccessor(target, functionName) { targetClassName?.toKClass()?.cast() ?: target::class.cast() }
    }

    fun <T : Any> memberFunction(functionName: String, targetClass: KClass<T>): MemberFunctionAccessor<T> {
        return MemberFunctionAccessor(functionName) { targetClass }
    }

    fun memberFunction(functionName: String, targetClassName: String?): MemberFunctionAccessor<Any> {
        return MemberFunctionAccessor(functionName) { targetClassName?.toKClass()?.cast() }
    }

    fun <T : Any> staticFunction(functionName: String, targetClass: KClass<T>): StaticFunctionAccessor<T> {
        return StaticFunctionAccessor(functionName) { targetClass }
    }

    fun staticFunction(functionName: String, targetClassName: String): StaticFunctionAccessor<Any> {
        return StaticFunctionAccessor(functionName) { targetClassName.toKClass().cast() }
    }

}
