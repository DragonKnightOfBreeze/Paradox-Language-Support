package icu.windea.pls.injectx.annotations

/**
 * 注明此注入方法的相关信息。
 * @property value 注明目标方法的方法名。如果同名，则为空字符串。
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class InjectMethod(
    val value: String
)
