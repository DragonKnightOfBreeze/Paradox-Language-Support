@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

/**
 * 访问器不受支持的异常。
 *
 * 在访问器委托/提供器执行过程中出现未支持或无法处理的情况时抛出，用于统一异常类型。
 */
class UnsupportedAccessorException : UnsupportedOperationException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
