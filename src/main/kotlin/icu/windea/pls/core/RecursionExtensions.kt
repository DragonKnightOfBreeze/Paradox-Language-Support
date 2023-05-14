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
    val cached = recursionGuardCache.get(key)
    try {
        val recursionGuard = cached
            ?: SmartRecursionGuard().also { recursionGuardCache.put(key, it) }
        return recursionGuard.action()
    } catch(e1: StackOverflowError) {
        return null
    } catch(e2: StackOverflowPreventedException) {
        return null
    } finally {
        if(cached == null) {
            recursionGuardCache.remove(key)
        }
    }
}

/**
 * 用于基于传入的键避免SOE。
 */
class SmartRecursionGuard {
    val stackTrace = LinkedList<Any>()
    var checkStatus = false
    
    /**
     * 如果将要发生SOE则执行指定的一段代码。
     */
    inline fun onRecursion(key: Any, action: () -> Unit) {
        if(stackTrace.contains(key)) {
            action()
        }
    }
    
    /**
     * 判断当前键是否出现在之前的堆栈中。如果没有出现，添加到堆栈中。
     */
    inline fun <T> withCheckRecursion(key: Any, action: () -> T): T? {
        if(stackTrace.contains(key)) {
            checkStatus = true
            return null
        }
        stackTrace.addLast(key)
        try {
            return action()
        } finally {
            stackTrace.removeLast()
        }
    }
    
    inline fun <T> withCheckRecursion(target: Any, keySuffix: String, action: () -> T): T? {
        return withCheckRecursion(target.javaClass.name + "@" + keySuffix, action)
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