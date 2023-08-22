package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.*

/**
 * 注明此方法用于进行代码注入。
 * @property pointer 进行代码注入的位置。
 * @see CodeInjector
 * @see CodeInjectorBase
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class InjectMethod(
    val pointer: Pointer = Pointer.BODY
) {
    enum class Pointer {
        BODY, BEFORE, AFTER, AFTER_FINALLY
    }
}