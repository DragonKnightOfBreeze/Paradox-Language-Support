package icu.windea.pls.core

private val stackTraceThreadLocal = ThreadLocal.withInitial { mutableListOf<Any>() }

/**
 * 判断当前实例是否出现在之前的堆栈中。
 * 
 * 这里的堆栈是一个本地缓存并在检查时更新。如果检查结果为`true`，则会清空。
 */
fun Any.checkInstanceRecursion(): Boolean {
    val stackTrace = stackTraceThreadLocal.get()
    if(stackTrace.contains(this)) {
        stackTrace.clear()
        return true
    }
    stackTrace.add(this)
    return false
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