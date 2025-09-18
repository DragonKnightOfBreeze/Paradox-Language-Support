@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

import icu.windea.pls.core.cast
import icu.windea.pls.core.toKClass
import kotlin.reflect.KClass

/**
 * 访问器构建器。
 *
 * 提供按目标对象/类型与成员名创建属性/函数访问器的便捷工厂方法，
 * 以统一封装反射读取、写入与调用的细节。
 */
object AccessorBuilder {
    /** 基于运行时对象 [target] 构建属性访问器。*/
    fun <T : Any, V> property(target: T, propertyName: String, targetClass: KClass<T>): PropertyAccessor<T, V> {
        return PropertyAccessor(target, propertyName) { targetClass }
    }

    /** 基于运行时对象 [target] 构建属性访问器，目标类型名可选 [targetClassName]。*/
    fun <V> property(target: Any, propertyName: String, targetClassName: String?): PropertyAccessor<Any, V> {
        return PropertyAccessor(target, propertyName) { targetClassName?.toKClass()?.cast() ?: target::class.cast() }
    }

    /** 基于成员属性名 [propertyName] 与目标类型 [targetClass] 构建成员属性访问器。*/
    fun <T : Any, V> memberProperty(propertyName: String, targetClass: KClass<T>): MemberPropertyAccessor<T, V> {
        return MemberPropertyAccessor(propertyName) { targetClass }
    }

    /** 基于成员属性名 [propertyName] 与类型名 [targetClassName] 构建成员属性访问器。*/
    fun <V> memberProperty(propertyName: String, targetClassName: String?): MemberPropertyAccessor<Any, V> {
        return MemberPropertyAccessor(propertyName) { targetClassName?.toKClass()?.cast() }
    }

    /** 基于静态属性名 [propertyName] 与目标类型 [targetClass] 构建静态属性访问器。*/
    fun <T : Any, V> staticProperty(propertyName: String, targetClass: KClass<T>): StaticPropertyAccessor<T, V> {
        return StaticPropertyAccessor(propertyName) { targetClass }
    }

    /** 基于静态属性名 [propertyName] 与类型名 [targetClassName] 构建静态属性访问器。*/
    fun <V> staticProperty(propertyName: String, targetClassName: String): StaticPropertyAccessor<Any, V> {
        return StaticPropertyAccessor(propertyName) { targetClassName.toKClass().cast() }
    }

    /** 基于运行时对象 [target] 构建成员函数访问器。*/
    fun <T : Any> function(target: T, functionName: String, targetClass: KClass<T>): FunctionAccessor<T> {
        return FunctionAccessor(target, functionName) { targetClass }
    }

    /** 基于运行时对象 [target] 构建成员函数访问器，类型名可选 [targetClassName]。*/
    fun function(target: Any, functionName: String, targetClassName: String?): FunctionAccessor<Any> {
        return FunctionAccessor(target, functionName) { targetClassName?.toKClass()?.cast() ?: target::class.cast() }
    }

    /** 基于函数名 [functionName] 与目标类型 [targetClass] 构建成员函数访问器。*/
    fun <T : Any> memberFunction(functionName: String, targetClass: KClass<T>): MemberFunctionAccessor<T> {
        return MemberFunctionAccessor(functionName) { targetClass }
    }

    /** 基于函数名 [functionName] 与类型名 [targetClassName] 构建成员函数访问器。*/
    fun memberFunction(functionName: String, targetClassName: String?): MemberFunctionAccessor<Any> {
        return MemberFunctionAccessor(functionName) { targetClassName?.toKClass()?.cast() }
    }

    /** 基于函数名 [functionName] 与目标类型 [targetClass] 构建静态函数访问器。*/
    fun <T : Any> staticFunction(functionName: String, targetClass: KClass<T>): StaticFunctionAccessor<T> {
        return StaticFunctionAccessor(functionName) { targetClass }
    }

    /** 基于函数名 [functionName] 与类型名 [targetClassName] 构建静态函数访问器。*/
    fun staticFunction(functionName: String, targetClassName: String): StaticFunctionAccessor<Any> {
        return StaticFunctionAccessor(functionName) { targetClassName.toKClass().cast() }
    }

}
