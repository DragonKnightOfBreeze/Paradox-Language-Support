package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.BaseCodeInjectorSupport

/**
 * 注明注入方法信息。
 *
 * @property value 目标方法的名字。如果为空，则与注入方法的相同。
 * @property pointer 进行代码注入的位置。
 * @property static 是否是静态方法。
 * @see BaseCodeInjectorSupport
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class InjectMethod(
    val value: String = "",
    val pointer: Pointer = Pointer.BODY,
    val static: Boolean = false,
) {
    enum class Pointer {
        BODY, BEFORE, AFTER, AFTER_FINALLY
    }
}
