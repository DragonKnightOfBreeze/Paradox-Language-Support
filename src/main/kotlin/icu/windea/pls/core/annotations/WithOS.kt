package icu.windea.pls.core.annotations

import icu.windea.pls.core.util.*

/**
 * 注明此功能仅适用于特定的OS。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class WithOS(
    vararg val value: OS
)
