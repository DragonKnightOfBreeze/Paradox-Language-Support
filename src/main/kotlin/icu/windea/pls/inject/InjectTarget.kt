package icu.windea.pls.inject

/**
 * 注明此代码注入器的目标信息。
 * @property value 目标类名。
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
annotation class InjectTarget(
    val value: String
)