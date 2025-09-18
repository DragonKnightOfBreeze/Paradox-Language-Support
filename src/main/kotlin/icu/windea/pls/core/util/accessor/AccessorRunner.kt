package icu.windea.pls.core.util.accessor

import com.intellij.openapi.progress.ProcessCanceledException
import java.lang.reflect.InvocationTargetException

/**
 * 访问器运行器：统一包装访问器执行过程中的异常。
 *
 * - 委托内部异常（如反射失败）会被包装为 [UnsupportedAccessorException]；
 * - [ProcessCanceledException] 原样抛出；
 * - 对于提供者侧，若捕获到 [InvocationTargetException]，则抛出其 `cause`（若存在）。
 */
object AccessorRunner {
    /** 在“委托”上下文中运行 [block]，按策略转换异常为 [UnsupportedAccessorException]。*/
    fun <T> runInAccessorDelegate(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }

    /** 在“提供者”上下文中运行 [block]，额外解包 [InvocationTargetException]。*/
    fun <T> runInAccessorProvider(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            if (e is InvocationTargetException) throw e.cause ?: e
            if (e is ProcessCanceledException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }
}
