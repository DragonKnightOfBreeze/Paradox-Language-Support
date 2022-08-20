package icu.windea.pls.annotations

import kotlin.reflect.*

/**
 * 注明此类型期望的并集类型。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE)
annotation class UnionType(
	vararg val types: KClass<*>
)