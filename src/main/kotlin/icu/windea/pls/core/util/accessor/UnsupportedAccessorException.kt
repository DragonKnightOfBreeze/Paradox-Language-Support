@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

/**
 * 不支持的访问器异常。
 *
 * 当无法为给定目标/成员找到可访问的读取/写入/调用路径，或访问失败需要向上层屏蔽实现细节时抛出。
 */
class UnsupportedAccessorException : UnsupportedOperationException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
