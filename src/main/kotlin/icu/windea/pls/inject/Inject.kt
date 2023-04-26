package icu.windea.pls.inject

/**
 * 注明此方法用于进行代码注入。
 * @property pointer 进行代码注入的位置。
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
annotation class Inject(
    val pointer: Pointer
) {
    enum class Pointer {
        BODY, BEFORE, AFTER, AFTER_FINALLY
    }
}
