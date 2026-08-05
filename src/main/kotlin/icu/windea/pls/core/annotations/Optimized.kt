package icu.windea.pls.core.annotations

import kotlin.annotation.AnnotationTarget.*

/**
 * 注明这里的实现代码经过专门的优化，相比常规实现拥有更好的性能和/或更少的内存开销。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, PROPERTY, FUNCTION, FILE, EXPRESSION)
annotation class Optimized
