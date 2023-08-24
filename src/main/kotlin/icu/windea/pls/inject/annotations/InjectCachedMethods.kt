package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.*
import icu.windea.pls.inject.support.*

/**
 * 注明需要基于字段缓存的一组方法。
 * 
 * 调用这些方法时，如果对应的字段的值不为空值，则直接返回对应字段的值，否则执行原始方法代码并将结果缓存到对应字段中。
 * 这些方法必须没有任何参数且拥有一个返回值，并且允许返回null。
 * 这些字段的值会在指定的清理方法中被清空。
 * @property methods 目标方法的名字。
 * @property cleanupMethod 清理方法的名字。如果为空字符串，则表示不指定清理方法。
 * @see CachedMethodsInjectorSupport
 */
annotation class InjectCachedMethods(
    val methods: Array<String>,
    val cleanupMethod: String = ""
)
