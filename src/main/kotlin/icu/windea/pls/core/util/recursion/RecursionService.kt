package icu.windea.pls.core.util.recursion

import com.intellij.openapi.util.StackOverflowPreventedException
import com.intellij.openapi.util.UserDataHolder

object RecursionService {
    /**
     * 执行一段代码，通过维护在当前线程中的命名的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
     * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
     */
    inline fun <T> withRecursionGuard(name: String, action: RecursionGuard.() -> T): T? {
        val recursionGuardCache = RecursionGuardContext.getRecursionGuardCache()
        val cached = recursionGuardCache.get(name)
        try {
            val recursionGuard = cached ?: RecursionGuardContext.createRecursionGuard(recursionGuardCache, name)
            return recursionGuard.action()
        } catch (_: StackOverflowError) {
            return null
        } catch (_: StackOverflowPreventedException) {
            return null
        } finally {
            if (cached == null) RecursionGuardContext.clearRecursionGuard(recursionGuardCache, name)
        }
    }

    /**
     * 执行一段代码，通过维护在当前上下文对象中的命名的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
     * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
     *
     * 适合在序列构建器和协程上下文中使用。
     */
    inline fun <T> withContextRecursionGuard(context: UserDataHolder, name: String, action: RecursionGuard.() -> T): T? {
        val recursionGuardCache = RecursionGuardContext.getContextRecursionGuardCache(context)
        val cached = recursionGuardCache.get(name)
        try {
            val recursionGuard = cached ?: RecursionGuardContext.createRecursionGuard(recursionGuardCache, name)
            return recursionGuard.action()
        } catch (_: StackOverflowError) {
            return null
        } catch (_: StackOverflowPreventedException) {
            return null
        } finally {
            if (cached == null) RecursionGuardContext.clearContextRecursionGuard(context, recursionGuardCache, name)
        }
    }

    /**
     * 得到当前线程的堆栈快照。
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun getCurrentStackTrace(): Array<StackTraceElement> {
        return Exception().stackTrace
    }

    // /**
    //  * 判断当前方法是否已在调用栈中出现（用于简易递归检测）。
    //  */
    // fun checkMethodRecursion(): Boolean {
    //     val currentStackTrace = getCurrentStackTrace()
    //     val currentStack = currentStackTrace.firstOrNull() ?: return false
    //     var i = 1
    //     while (i < currentStackTrace.size) {
    //         val stack = currentStackTrace[i]
    //         if (currentStack.className == stack.className && currentStack.methodName == stack.methodName) return true
    //         i++
    //     }
    //     return false
    // }
}
