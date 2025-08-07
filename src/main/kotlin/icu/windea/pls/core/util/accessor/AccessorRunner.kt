package icu.windea.pls.core.util.accessor

import com.intellij.openapi.progress.ProcessCanceledException
import java.lang.reflect.InvocationTargetException

object AccessorRunner {
    fun <T> runInAccessorDelegate(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            if (e is UnsupportedAccessorException) throw e
            throw UnsupportedAccessorException(e)
        }
    }

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
