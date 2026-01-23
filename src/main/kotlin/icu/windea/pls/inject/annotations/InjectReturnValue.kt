package icu.windea.pls.inject.annotations

/**
 * 用于标记注入方法的参数，以接收被注入目标方法的返回值。
 *
 * @see InjectMethod
 */
@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectReturnValue
