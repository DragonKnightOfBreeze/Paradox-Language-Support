package icu.windea.pls.inject

/**
 * 用于表示需要继续执行目标方法中的代码。
 */
class ContinueInvocationException(message: String) : RuntimeException(message)
