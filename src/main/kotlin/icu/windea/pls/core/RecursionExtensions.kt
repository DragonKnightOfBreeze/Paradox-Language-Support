package icu.windea.pls.core

import com.intellij.openapi.util.*
import icu.windea.pls.*
import java.util.*

val PlsThreadLocals.recursionGuardCacheThreadLocal: ThreadLocal<MutableMap<String, SmartRecursionGuard>> by lazy { ThreadLocal.withInitial { mutableMapOf() } }
val PlsThreadLocals.stackTraceThreadLocal: ThreadLocal<MutableList<Any>> by lazy { ThreadLocal() }

/**
 * 执行一段代码并尝试避免SOE。
 */
inline fun <T> withRecursionGuard(key: String, action: SmartRecursionGuard.() -> T): T? {
    val recursionGuardCache = PlsThreadLocals.recursionGuardCacheThreadLocal.get()
    val cached = recursionGuardCache.containsKey(key)
    try {
        val recursionGuard =  recursionGuardCache.getOrPut(key) { SmartRecursionGuard() }
        return recursionGuard.action()
    } catch(e1: StackOverflowError) {
        return null
    } catch(e2: StackOverflowPreventedException) {
        return null
    } finally {
        if(!cached) {
            PlsThreadLocals.recursionGuardCacheThreadLocal.remove()
        }
    }
}

/**
 * 用于基于传入的键避免SOE。
 */
class SmartRecursionGuard {
    val stackTrace = LinkedList<Any>()
    var fallbackValue: Any? = null
    
    /**
     * 判断当前键是否出现在之前的堆栈中。如果没有出现，添加到堆栈中。
     * @param fallback 如果为`true`，当将会发生SOE时，返回缓存的最后一个不为null的结果，否则返回null。
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <T> withCheckRecursion(key: Any, fallback: Boolean = false, action: () -> T): T? {
        if(stackTrace.contains(key)) {
            return if(fallback) fallbackValue as? T? else null
        }
        stackTrace.addLast(key)
        try {
            val r = action()
            if(r != null) fallbackValue = r
            return r
        } finally {
            stackTrace.removeLast()
        }
    }
    
    inline fun <T> withCheckRecursion(target: Any, keySuffix: String, fallback: Boolean = false, action: () -> T): T? {
        return withCheckRecursion(target.javaClass.name + "@" + keySuffix, fallback, action)
    }
}

/**
 * 判断当前堆栈对应的方法是否出现在之前的堆栈中。
 */
fun checkMethodRecursion(): Boolean {
    val stackTrace = Thread.currentThread().stackTrace
    if(stackTrace.size <= 2) return false
    val currentStack = stackTrace[1]
    val size = stackTrace.size
    var i = 2
    while(i < size) {
        val stack = stackTrace[i]
        if(currentStack.className == stack.className && currentStack.methodName == stack.methodName) return true
        i++
    }
    return false
}