package icu.windea.pls.core.annotations

import kotlin.annotation.AnnotationTarget.*

/**
 * 注明这里的工具代码经过专门的优化，相比常规实现拥有更好的性能和/或更少的内存开销。
 * 通常来说，仅需在必要时使用，如热点路径上。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, PROPERTY, FUNCTION, FILE, EXPRESSION)
annotation class Fast
