package icu.windea.pls.inject.annotations

import icu.windea.pls.inject.support.BaseCodeInjectorSupport

/**
 * 要注入的方法的信息。
 *
 * 说明：
 * - 如果注入方法是扩展方法，则传入注入目标到接收者（目标方法是静态方法时，传入 `null`）。
 * - 如果注入方法的某个参数注有 [InjectReturnValue]，则传入目标方法的返回值到该参数（目标方法没有返回值时，传入 `null`）。
 * - 注入方法的余下参数按顺序传递到目标方法，其数量可以少于或等于目标方法的参数数量，但类型必须按顺序匹配。
 *
 * @property value 目标方法的名字。如果为空，则与注入方法的相同。
 * @property pointer 进行代码注入的位置。
 * @property static 是否是静态方法。
 *
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
