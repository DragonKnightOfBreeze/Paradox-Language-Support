package icu.windea.pls.core.util.recursion

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.util.StackOverflowPreventedException
import java.util.*

/**
 * 递归守卫。
 *
 * 用于根据一组键来避免意外的递归。同一上下文中可以存在多个不同名字的递归守卫。
 *
 * @see RecursionService
 */
class RecursionGuard(val name: String) {
    val stackTrace = ArrayDeque<Any>() // 来自 GPT：小深度用线性结构（数组/栈）线性扫，往往比哈希集合更快（分支预测友好、少间接寻址）

    /**
     * 用于进行直接的递归检测。
     *
     * 如果 [key] 为 `null` 则直接返回。
     * 如果 [key] 不在当前调用栈中，则入栈并直接返回。
     * 否则抛出 [StackOverflowPreventedException]。
     */
    fun recursionCheck(key: Any?) {
        if (key == null) return
        if (stackTrace.contains(key)) {
            RecursionGuardContext.logger.debug { "Unexpected recursion prevented (key: $key, stackTrace: $stackTrace)" }
            throw StackOverflowPreventedException("Unexpected recursion prevented (key: $key, stackTrace: $stackTrace)")
        }
        stackTrace.addLast(key)
    }

    /**
     * 用于进行更小粒度的递归检测。
     *
     * 如果 [key] 为 `null` 则直接执行 [action] 并返回。
     * 如果 [key] 不在当前调用栈中，则入栈并执行 [action] 并返回，结束后出栈。
     * 否则直接返回 `null`。
     */
    inline fun <T> withRecursionCheck(key: Any?, action: () -> T): T? {
        if (key == null) return action()
        if (stackTrace.contains(key)) {
            RecursionGuardContext.logger.debug { "Unexpected recursion prevented (key: $key, stackTrace: $stackTrace)" }
            return null
        }
        stackTrace.addLast(key)
        try {
            return action()
        } finally {
            stackTrace.removeLast()
        }
    }
}
