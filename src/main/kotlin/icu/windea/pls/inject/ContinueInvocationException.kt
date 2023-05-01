package icu.windea.pls.inject

/**
 * 用于在注入方法（注入到目标方法之前）中使用，让方法不直接返回而继续执行目标方法中的代码。
 */
class ContinueInvocationException: RuntimeException() {
    companion object {
        @JvmField val INSTANCE = ContinueInvocationException() //避免不必要的实例化
    }
}