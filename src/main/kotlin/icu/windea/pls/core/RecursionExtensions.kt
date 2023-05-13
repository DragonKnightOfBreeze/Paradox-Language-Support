package icu.windea.pls.core

private val stackTraceThreadLocal = ThreadLocal.withInitial { mutableListOf<Any>() }

/**
 * 判断当前实例是否出现在之前的堆栈中。
 * 
 * 这里的堆栈是一个本地缓存，检查时会更新，检查全部完成后需要手动清空。
 */
fun checkInstanceRecursion(target: Any): Boolean {
    val stackTrace = stackTraceThreadLocal.get()
    if(stackTrace.contains(target)) {
        return true
    }
    stackTrace.add(target)
    return false
}

fun finishCheckInstanceRecursion() {
    val stackTrace = stackTraceThreadLocal.get().clear()
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