@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core

import com.intellij.openapi.util.StackOverflowPreventedException
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import java.util.*

@Optimized
object RecursionGuardScope {
    private val cache = ThreadLocal.withInitial { FastMap<String, RecursionGuard>() }
    private val cacheKey = createKey<MutableMap<String, RecursionGuard>>("RecursionGuardScope.cacheKey")

    @PublishedApi
    internal fun getRecursionGuardCache(): MutableMap<String, RecursionGuard> {
        return cache.get()
    }

    @PublishedApi
    internal fun getContextRecursionGuardCache(context: UserDataHolder): MutableMap<String, RecursionGuard> {
        return context.getOrPutUserData(cacheKey) { FastMap() }
    }

    @PublishedApi
    internal fun createRecursionGuard(recursionGuardCache: MutableMap<String, RecursionGuard>, name: String): RecursionGuard {
        val recursionGuard = RecursionGuard(name)
        recursionGuardCache.put(name, recursionGuard)
        return recursionGuard
    }

    @PublishedApi
    internal fun clearRecursionGuard(recursionGuardCache: MutableMap<String, RecursionGuard>, name: String) {
        val removed = recursionGuardCache.remove(name)
        removed?.stackTrace?.clear()
        if (recursionGuardCache.isEmpty()) cache.remove()
    }

    @PublishedApi
    internal fun clearContextRecursionGuard(context: UserDataHolder, recursionGuardCache: MutableMap<String, RecursionGuard>, name: String) {
        val removed = recursionGuardCache.remove(name)
        removed?.stackTrace?.clear()
        if (recursionGuardCache.isEmpty()) context.putUserData(cacheKey, null)
    }
}

/**
 * 递归守卫。
 */
class RecursionGuard(val name: Any) {
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
        if (stackTrace.contains(key)) throw StackOverflowPreventedException("")
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
        if (stackTrace.contains(key)) return null
        stackTrace.addLast(key)
        try {
            return action()
        } finally {
            stackTrace.removeLast()
        }
    }
}

/**
 * 执行一段代码，通过维护在当前线程中的命名的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 */
inline fun <T> withRecursionGuard(name: String, action: RecursionGuard.() -> T): T? {
    val recursionGuardCache = RecursionGuardScope.getRecursionGuardCache()
    val cached = recursionGuardCache.get(name)
    try {
        val recursionGuard = cached ?: RecursionGuardScope.createRecursionGuard(recursionGuardCache, name)
        return recursionGuard.action()
    } catch (e1: StackOverflowError) {
        return null
    } catch (e2: StackOverflowPreventedException) {
        return null
    } finally {
        if (cached == null) RecursionGuardScope.clearRecursionGuard(recursionGuardCache, name)
    }
}

/**
 * 执行一段代码，通过维护在当前线程中的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 *
 * 这个方法直接使用 [action] 的类名作为递归守卫实例的名字。
 */
fun <T> withRecursionGuard(action: RecursionGuard.() -> T): T? {
    return withRecursionGuard(action::class.java.name, action)
}

/**
 * 执行一段代码，通过维护在当前上下文对象中的命名的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 *
 * 适合在序列构建器和协程上下文中使用。
 */
inline fun <T> withContextRecursionGuard(context: UserDataHolder, name: String, action: RecursionGuard.() -> T): T? {
    val recursionGuardCache = RecursionGuardScope.getContextRecursionGuardCache(context)
    val cached = recursionGuardCache.get(name)
    try {
        val recursionGuard = cached ?: RecursionGuardScope.createRecursionGuard(recursionGuardCache, name)
        return recursionGuard.action()
    } catch (e1: StackOverflowError) {
        return null
    } catch (e2: StackOverflowPreventedException) {
        return null
    } finally {
        if (cached == null) RecursionGuardScope.clearContextRecursionGuard(context, recursionGuardCache, name)
    }
}

/**
 * 执行一段代码，通过维护在当前上下文对象中的 [RecursionGuard] 尝试避免递归导致的堆栈溢出。
 * 捕获 [StackOverflowError] 和 [StackOverflowPreventedException] 并返回 `null`。
 *
 * 这个方法直接使用 [action] 的类名作为递归守卫实例的名字。
 *
 * 适合在序列构建器和协程上下文中使用。
 */
fun <T> withContextRecursionGuard(context: UserDataHolder, action: RecursionGuard.() -> T): T? {
    return withContextRecursionGuard(context, action::class.java.name, action)
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
