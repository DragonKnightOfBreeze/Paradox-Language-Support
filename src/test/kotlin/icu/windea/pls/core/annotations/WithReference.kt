package icu.windea.pls.core.annotations

import org.intellij.lang.annotations.Language

/**
 * 注明用作参考的文件、类、属性或者方法。
 */
@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class WithReference(
    @Language("jvm-class-name")
    val value: String
)
