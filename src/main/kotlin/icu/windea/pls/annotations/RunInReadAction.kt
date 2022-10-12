package icu.windea.pls.annotations

/**
 * 注明此方法经过测试需要在ReadAction中调用。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class RunInReadAction
