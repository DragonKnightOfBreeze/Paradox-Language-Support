package icu.windea.pls.core.annotations

import kotlin.annotation.AnnotationTarget.*

/**
 * 注明这里的实现是专门优化过的，相比常规实现拥有更好的性能和/或更少的内存占用。
 * 仅作重点标记，其他地方可能也拥有专门的优化。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, PROPERTY, FUNCTION, FILE, EXPRESSION)
annotation class Optimized
