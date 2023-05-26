package icu.windea.pls.core.annotations

import kotlin.reflect.*

/**
 * 注明此数字类型用于表示一个枚举值。
 */
@MustBeDocumented
@Target(AnnotationTarget.TYPE)
annotation class EnumId(
    val value: KClass<out Enum<*>>
)