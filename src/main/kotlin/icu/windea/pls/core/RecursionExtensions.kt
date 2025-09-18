@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.openapi.util.StackOverflowPreventedException

/**
 * 执行一段代码，并通过 [SmartRecursionGuard] 尝试避免递归导致的堆栈溢出。
 *
 * - 为当前线程维护一个命名的递归守卫实例（见 [SmartRecursionGuard.cache]）；
 * - 捕获 [StackOverflowError] 与 [StackOverflowPreventedException] 并返回 `null`；
 * - 调用方可在 [action] 内使用 [SmartRecursionGuard.withRecursionCheck] 做细粒度的重入检测。
 */
fun <T> withRecursionGuard(action: SmartRecursionGuard.() -> T): T? {
    val name = action::class.java.name
    val recursionGuardCache = SmartRecursionGuard.cache.get()
    val cached = recursionGuardCache.get(name)
    try {
        val recursionGuard = cached ?: SmartRecursionGuard(name).also { recursionGuardCache.put(name, it) }
        return recursionGuard.action()
    } catch (e1: StackOverflowError) {
        return null
    } catch (e2: StackOverflowPreventedException) {
        return null
    } finally {
        if (cached == null) {
            recursionGuardCache.remove(name)
        }
    }
}

class SmartRecursionGuard(val name: Any) {
    val stackTrace = ArrayDeque<Any>()

    /**
     * 若 [key] 不在当前调用栈中，则入栈并执行 [action]，结束后出栈；否则直接返回 `null`。
     *
     * 用于在更小粒度（如某个元素/路径级别）避免递归重入。
     */
    inline fun <T> withRecursionCheck(key: Any, action: () -> T): T? {
        if (stackTrace.contains(key)) {
            return null
        }
        stackTrace.addLast(key)
        try {
            return action()
        } finally {
            stackTrace.removeLast()
        }
    }

    companion object {
        val cache: ThreadLocal<MutableMap<String, SmartRecursionGuard>> by lazy { ThreadLocal.withInitial { mutableMapOf() } }
    }
}

/**
 * 得到当前线程的堆栈快照。
 */
inline fun getCurrentStackTrace(): Array<StackTraceElement> {
    return Exception().stackTrace
}

/**
 * 判断当前方法是否已在调用栈中出现（用于简易递归检测）。
 */
fun checkMethodRecursion(): Boolean {
    val currentStackTrace = getCurrentStackTrace()
    val currentStack = currentStackTrace.firstOrNull() ?: return false
    var i = 1
    while (i < currentStackTrace.size) {
        val stack = currentStackTrace[i]
        if (currentStack.className == stack.className && currentStack.methodName == stack.methodName) return true
        i++
    }
    return false
}
