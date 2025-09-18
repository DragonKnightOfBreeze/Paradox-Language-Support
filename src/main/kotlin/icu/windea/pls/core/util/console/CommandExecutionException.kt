@file:Suppress("unused")

package icu.windea.pls.core.util.console

/**
 * 命令执行异常。
 *
 * 当外部命令执行失败（非零退出码或错误输出）时抛出，用于携带错误信息。
 */
class CommandExecutionException : IllegalStateException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
