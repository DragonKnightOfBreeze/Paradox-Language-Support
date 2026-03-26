@file:Suppress("unused")

package icu.windea.pls.core.execution

/**
 * 命令行执行异常。
 *
 * 当外部命令执行失败（非零退出码或错误输出）时抛出，用于携带错误信息。
 *
 * @see CommandLineExecutor
 */
class CommandLineExecutionException : IllegalStateException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
