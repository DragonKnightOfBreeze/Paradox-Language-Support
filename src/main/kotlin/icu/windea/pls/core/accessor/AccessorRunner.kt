package icu.windea.pls.core.accessor

import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import java.lang.reflect.InvocationTargetException

object AccessorRunner {
    /** 在“委托”上下文中运行 [action]，按策略转换异常为 [UnsupportedAccessorException]。 */
    fun <T> runInAccessorDelegate(action: () -> T): T {
        try {
            return action()
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }

    /** 在“提供者”上下文中运行 [action]，额外解包 [InvocationTargetException]。 */
    fun <T> runInAccessorProvider(action: () -> T): T {
        try {
            return action()
        } catch (e: Exception) {
            if (e is InvocationTargetException) throw e.cause ?: e
            if (e is ProcessCanceledException || e is CancellationException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }
}
