package icu.windea.pls.core.annotations

import kotlin.annotation.AnnotationTarget.*

/**
 * 注明这里的实现代码使用了某种推断逻辑，而这可能需要进一步的优化。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, PROPERTY, FUNCTION, FILE, EXPRESSION)
annotation class Inferred
