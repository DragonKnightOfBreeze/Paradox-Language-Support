package icu.windea.pls.lang.annotations

import icu.windea.pls.core.util.OS

/**
 * 注明此功能仅适用于特定的 OS。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class WithOS(
    vararg val value: OS
)
