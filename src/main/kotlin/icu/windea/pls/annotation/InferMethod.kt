package icu.windea.pls.annotation

/**
 * 注明此方法用于进行某种推断。可能需要继续优化。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class InferMethod
