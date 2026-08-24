package icu.windea.pls.core

import com.intellij.openapi.progress.ProcessCanceledException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class ConcurrentExtensionsTest {
    // 借助普通（非内联）助手方法吸收内联函数中的非局部返回，
    // 避免其 return 提前退出测试方法本身。
    private fun <T> invokeSync(lock: Any, flag: AtomicBoolean, action: () -> T) {
        lock.withDoubleLock(flag, action)
    }

    private suspend fun invokeCoroutine(mutex: Mutex, flag: AtomicBoolean, action: () -> Unit) {
        mutex.withDoubleLock(flag, action)
    }

    @Test
    fun withDoubleLock_sync_runsOnce_test() {
        val flag = AtomicBoolean(false)
        var count = 0
        val lock = Any()
        invokeSync(lock, flag) { count++ }
        invokeSync(lock, flag) { count++ }
        Assert.assertEquals(1, count)
        Assert.assertTrue(flag.get())
    }

    @Test
    fun withDoubleLock_sync_exceptionSwallowed_test() {
        val flag = AtomicBoolean(false)
        val lock = Any()
        // 普通异常被吞掉，且标志位不会被设置
        invokeSync(lock, flag) { throw RuntimeException("boom") }
        Assert.assertFalse(flag.get())
    }

    @Test
    fun withDoubleLock_sync_cancellationRethrown_test() {
        val flag = AtomicBoolean(false)
        val lock = Any()
        Assert.assertThrows(ProcessCanceledException::class.java) {
            invokeSync(lock, flag) { throw ProcessCanceledException() }
        }
        Assert.assertThrows(CancellationException::class.java) {
            invokeSync(lock, flag) { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun withDoubleLock_coroutine_runsOnce_test() = runBlocking {
        val flag = AtomicBoolean(false)
        var count = 0
        val mutex = Mutex()
        invokeCoroutine(mutex, flag) { count++ }
        invokeCoroutine(mutex, flag) { count++ }
        Assert.assertEquals(1, count)
        Assert.assertTrue(flag.get())
    }
}
