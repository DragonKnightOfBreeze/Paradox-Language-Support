@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import icu.windea.pls.core.util.accessor.AccessorBuilder
import icu.windea.pls.core.util.accessor.FunctionAccessor
import icu.windea.pls.core.util.accessor.MemberFunctionAccessor
import icu.windea.pls.core.util.accessor.MemberPropertyAccessor
import icu.windea.pls.core.util.accessor.PropertyAccessor
import icu.windea.pls.core.util.accessor.StaticFunctionAccessor
import icu.windea.pls.core.util.accessor.StaticPropertyAccessor

/** 为当前实例构建属性访问器。 */
inline fun <reified T : Any, V> T.property(propertyName: String): PropertyAccessor<T, V> {
    return AccessorBuilder.property(this, propertyName, T::class)
}

/** 按类名为当前实例构建属性访问器。 */
inline fun <V> Any.property(propertyName: String, targetClassName: String?): PropertyAccessor<Any, V> {
    return AccessorBuilder.property(this, propertyName, targetClassName)
}

/** 构建成员属性访问器。 */
inline fun <reified T : Any, V> memberProperty(propertyName: String): MemberPropertyAccessor<T, V> {
    return AccessorBuilder.memberProperty(propertyName, T::class)
}

/** 按类名构建成员属性访问器。 */
inline fun <V> memberProperty(propertyName: String, targetClassName: String?): MemberPropertyAccessor<Any, V> {
    return AccessorBuilder.memberProperty(propertyName, targetClassName)
}

/** 构建静态属性访问器。 */
inline fun <reified T : Any, V> staticProperty(propertyName: String): StaticPropertyAccessor<T, V> {
    return AccessorBuilder.staticProperty(propertyName, T::class)
}

/** 按类名构建静态属性访问器。 */
inline fun <V> staticProperty(propertyName: String, targetClassName: String): StaticPropertyAccessor<Any, V> {
    return AccessorBuilder.staticProperty(propertyName, targetClassName)
}

/** 为当前实例构建函数访问器。 */
inline fun <reified T : Any> T.function(functionName: String): FunctionAccessor<T> {
    return AccessorBuilder.function(this, functionName, T::class)
}

/** 按类名为指定对象构建函数访问器。 */
inline fun function(target: Any, functionName: String, targetClassName: String?): FunctionAccessor<Any> {
    return AccessorBuilder.function(target, functionName, targetClassName)
}

/** 构建成员函数访问器。 */
inline fun <reified T : Any> memberFunction(functionName: String): MemberFunctionAccessor<T> {
    return AccessorBuilder.memberFunction(functionName, T::class)
}

/** 按类名构建成员函数访问器。 */
inline fun memberFunction(functionName: String, targetClassName: String?): MemberFunctionAccessor<Any> {
    return AccessorBuilder.memberFunction(functionName, targetClassName)
}

/** 构建静态函数访问器。 */
inline fun <reified T : Any> staticFunction(functionName: String): StaticFunctionAccessor<T> {
    return AccessorBuilder.staticFunction(functionName, T::class)
}

/** 按类名构建静态函数访问器。 */
inline fun staticFunction(functionName: String, targetClassName: String): StaticFunctionAccessor<Any> {
    return AccessorBuilder.staticFunction(functionName, targetClassName)
}
