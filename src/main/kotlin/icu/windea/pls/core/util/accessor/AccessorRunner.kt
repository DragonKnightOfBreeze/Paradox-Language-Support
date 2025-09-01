package icu.windea.pls.core.util.accessor

import com.intellij.openapi.progress.ProcessCanceledException
import java.lang.reflect.InvocationTargetException

/**
 * 访问器运行器。
 *
 * 统一拦截并归类访问器相关异常，将其包装为 [UnsupportedAccessorException] 或透传取消/目标异常。
 */
object AccessorRunner {
    /** 在访问器委托上下文中执行代码块，统一异常处理。 */
    fun <T> runInAccessorDelegate(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }

    /** 在访问器提供器上下文中执行代码块，统一异常处理并展开 [InvocationTargetException]。 */
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
