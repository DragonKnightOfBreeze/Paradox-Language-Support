package icu.windea.pls.core

import icu.windea.pls.*
import java.util.*

val PlsThreadLocals.recursionGuardThreadLocal: ThreadLocal<MutableMap<String, SmartRecursionGuard>> by lazy { ThreadLocal.withInitial { mutableMapOf() } }
val PlsThreadLocals.stackTraceThreadLocal: ThreadLocal<MutableList<Any>> by lazy { ThreadLocal() }

inline fun <T> withRecursionGuard(key: String, action: SmartRecursionGuard.() -> T): T {
    val cachedRecursionGuards = PlsThreadLocals.recursionGuardThreadLocal.get()
    val cached = cachedRecursionGuards.containsKey(key)
    try {
        val recursionGuard = cachedRecursionGuards.getOrPut(key) { SmartRecursionGuard() }
        return recursionGuard.action()
    } finally {
        if(!cached) {
            cachedRecursionGuards.remove(key)
        }
    }
}

class SmartRecursionGuard {
    val stackTrace = LinkedList<Any>()
    
    /**
     * 判断当前键是否出现在之前的堆栈中。如果没有出现，添加到堆栈中。
     */
    inline fun <T> withCheckRecursion(key: Any, action: () -> T): T? {
        if(stackTrace.contains(key)) return null
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