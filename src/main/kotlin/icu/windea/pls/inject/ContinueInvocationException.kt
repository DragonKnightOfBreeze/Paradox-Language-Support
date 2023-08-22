package icu.windea.pls.inject

/**
 * 用于在（注入到目标方法之前的）注入方法中使用，让此方法不直接返回而继续执行目标方法中的代码。
 */
class ContinueInvocationException: RuntimeException()