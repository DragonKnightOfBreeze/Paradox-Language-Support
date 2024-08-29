@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.intellij.openapi.util.*

class SmartRecursionGuard(val name: Any) {
    val stackTrace = ArrayDeque<Any>()
    
    companion object {
        val cache: ThreadLocal<MutableMap<String, SmartRecursionGuard>> by lazy { ThreadLocal.withInitial { mutableMapOf() } }
    }
}

/**
 * 执行一段代码，并通过[SmartRecursionGuard]尝试避免堆栈溢出。
 */
inline fun <T> withRecursionGuard(name: String, action: SmartRecursionGuard.() -> T): T? {
    val recursionGuardCache = SmartRecursionGuard.cache.get()
    val cached = recursionGuardCache.get(name)
    try {
        val recursionGuard = cached ?: SmartRecursionGuard(name).also { recursionGuardCache.put(name, it) }
        return recursionGuard.action()
    } catch(e1: StackOverflowError) {
        return null
    } catch(e2: StackOverflowPreventedException) {
        return null
    } finally {
        if(cached == null) {
            recursionGuardCache.remove(name)
        }
    }
}

/**
 * 如果指定的[key]未存在于[SmartRecursionGuard.stackTrace]中，则入栈并执行指定的一段代码[action]，否则直接返回null。
 */
inline fun <T> SmartRecursionGuard.withRecursionCheck(key: Any, action: () -> T): T? {
    if(stackTrace.contains(key)) {
        return null
    }
    stackTrace.addLast(key)
    try {
        return action()
    } finally {
        stackTrace.removeLast()
    }
}

/**
 * 得到当前堆栈。
 */
inline fun getCurrentStackTrace(): Array<StackTraceElement> {
    return Exception().stackTrace
}

/**
 * 判断当前堆栈对应的方法是否出现在之前的堆栈中。
 */
fun checkMethodRecursion(): Boolean {
    val currentStackTrace = getCurrentStackTrace()
    val currentStack = currentStackTrace.firstOrNull() ?: return false
    var i = 1
    while(i < currentStackTrace.size) {
        val stack = currentStackTrace[i]
        if(currentStack.className == stack.className && currentStack.methodName == stack.methodName) return true
        i++
    }
    return false
}
