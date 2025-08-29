package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.FieldBasedCacheCodeInjectorSupport

/**
 * 注明字段缓存信息。
 *
 * 调用目标方法时，如果对应字段的值不为空值，则直接返回对应字段的值，否则执行原始方法的代码并将结果缓存到对应字段中。
 * 这些方法必须没有任何参数且拥有返回值。
 * 这些字段的值会在指定的清理方法中被清空。
 *
 * @property value 目标方法的名字。
 * @property cleanup 清理方法的名字。如果为空字符串，则表示不指定清理方法。
 * @see FieldBasedCacheCodeInjectorSupport
 */
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class InjectFieldBasedCache(
    val value: String,
    val cleanup: String = ""
)
