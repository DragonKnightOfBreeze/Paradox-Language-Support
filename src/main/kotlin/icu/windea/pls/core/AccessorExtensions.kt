@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import icu.windea.pls.core.util.accessor.AccessorBuilder
import icu.windea.pls.core.util.accessor.FunctionAccessor
import icu.windea.pls.core.util.accessor.MemberFunctionAccessor
import icu.windea.pls.core.util.accessor.MemberPropertyAccessor
import icu.windea.pls.core.util.accessor.PropertyAccessor
import icu.windea.pls.core.util.accessor.StaticFunctionAccessor
import icu.windea.pls.core.util.accessor.StaticPropertyAccessor

/** 基于运行时对象 [this] 构建属性访问器。*/
inline fun <reified T : Any, V> T.property(propertyName: String): PropertyAccessor<T, V> {
    return AccessorBuilder.property(this, propertyName, T::class)
}

/** 基于运行时对象 [target] 构建属性访问器，目标类型可通过 [targetClassName] 指定。*/
inline fun <V> Any.property(propertyName: String, targetClassName: String?): PropertyAccessor<Any, V> {
    return AccessorBuilder.property(this, propertyName, targetClassName)
}

/** 基于成员属性名与目标类型 [T] 构建成员属性访问器（调用时传入实例）。*/
inline fun <reified T : Any, V> memberProperty(propertyName: String): MemberPropertyAccessor<T, V> {
    return AccessorBuilder.memberProperty(propertyName, T::class)
}

/** 基于成员属性名与类型名 [targetClassName] 构建成员属性访问器。*/
inline fun <V> memberProperty(propertyName: String, targetClassName: String?): MemberPropertyAccessor<Any, V> {
    return AccessorBuilder.memberProperty(propertyName, targetClassName)
}

/** 基于静态属性名与目标类型 [T] 构建静态属性访问器。*/
inline fun <reified T : Any, V> staticProperty(propertyName: String): StaticPropertyAccessor<T, V> {
    return AccessorBuilder.staticProperty(propertyName, T::class)
}

/** 基于静态属性名与类型名 [targetClassName] 构建静态属性访问器。*/
inline fun <V> staticProperty(propertyName: String, targetClassName: String): StaticPropertyAccessor<Any, V> {
    return AccessorBuilder.staticProperty(propertyName, targetClassName)
}

/** 基于运行时对象 [this] 构建成员函数访问器。*/
inline fun <reified T : Any> T.function(functionName: String): FunctionAccessor<T> {
    return AccessorBuilder.function(this, functionName, T::class)
}

/** 基于运行时对象 [target] 构建成员函数访问器，目标类型可通过 [targetClassName] 指定。*/
inline fun function(target: Any, functionName: String, targetClassName: String?): FunctionAccessor<Any> {
    return AccessorBuilder.function(target, functionName, targetClassName)
}

/** 基于成员函数名与目标类型 [T] 构建成员函数访问器（调用时传入实例）。*/
inline fun <reified T : Any> memberFunction(functionName: String): MemberFunctionAccessor<T> {
    return AccessorBuilder.memberFunction(functionName, T::class)
}

/** 基于成员函数名与类型名 [targetClassName] 构建成员函数访问器。*/
inline fun memberFunction(functionName: String, targetClassName: String?): MemberFunctionAccessor<Any> {
    return AccessorBuilder.memberFunction(functionName, targetClassName)
}

/** 基于静态函数名与目标类型 [T] 构建静态函数访问器。*/
inline fun <reified T : Any> staticFunction(functionName: String): StaticFunctionAccessor<T> {
    return AccessorBuilder.staticFunction(functionName, T::class)
}

/** 基于静态函数名与类型名 [targetClassName] 构建静态函数访问器。*/
inline fun staticFunction(functionName: String, targetClassName: String): StaticFunctionAccessor<Any> {
    return AccessorBuilder.staticFunction(functionName, targetClassName)
}
