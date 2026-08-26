package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * @see LazyValue
 */
class LazyValueTest {

    // region 基础行为

    @Test
    fun initialize_and_value_test() {
        val v = LazyValue<String>()
        Assert.assertFalse(v.isInitialized())
        Assert.assertNull(v.value)
        Assert.assertEquals("x", v.initialize { "x" })
        Assert.assertTrue(v.isInitialized())
        Assert.assertEquals("x", v.value)
        Assert.assertEquals("x", v.get())
    }

    @Test
    fun initialize_runsOnce_test() {
        val v = LazyValue<String>()
        var count = 0
        v.initialize { count++; "x" }
        v.initialize { count++; "y" }
        Assert.assertEquals(1, count)
        Assert.assertEquals("x", v.value)
    }

    @Test
    fun clear_test() {
        val v = LazyValue<String>()
        v.initialize { "x" }
        v.clear()
        Assert.assertFalse(v.isInitialized())
        Assert.assertNull(v.value)
    }

    @Test
    fun check_test() {
        val v = LazyValue<Int>()
        v.initialize { 10 }
        v.check { it > 5 } // 满足，不清除
        Assert.assertTrue(v.isInitialized())
        v.check { it > 20 } // 不满足，清除
        Assert.assertFalse(v.isInitialized())
    }

    @Test
    fun reinitialize_test() {
        val v = LazyValue<String>()
        v.initialize { "x" }
        Assert.assertEquals("y", v.reinitialize { "y" })
        Assert.assertEquals("y", v.value)
    }

    @Test
    fun toString_test() {
        val v = LazyValue<Int>()
        Assert.assertEquals("Lazy value is not initialized.", v.toString())
        v.initialize { 5 }
        Assert.assertEquals("5", v.toString())
    }

    // endregion

    // region 细节与边界

    @Test
    fun initialize_withNull_cachesAndDoesNotRecompute_test() {
        val v = LazyValue<String>()
        var count = 0
        Assert.assertNull(v.initialize { count++; null })
        Assert.assertNull(v.initialize { count++; "x" })
        Assert.assertTrue(v.isInitialized()) // null 同样视为已初始化
        Assert.assertNull(v.value)
        Assert.assertEquals(1, count)
    }

    @Test
    fun value_setter_marksInitialized_test() {
        val v = LazyValue<String>()
        v.value = "x" // 直接赋值同样视为已初始化
        Assert.assertTrue(v.isInitialized())
        Assert.assertEquals("x", v.value)
    }

    @Test
    fun check_uninitialized_doesNothing_test() {
        val v = LazyValue<Int>()
        var invoked = false
        v.check { invoked = true; it > 0 }
        Assert.assertFalse(invoked) // 未初始化时谓词不被调用
        Assert.assertFalse(v.isInitialized())
    }

    @Test
    fun clear_thenInitialize_recomputes_test() {
        val v = LazyValue<String>()
        var count = 0
        v.initialize { count++; "x" }
        v.clear()
        Assert.assertEquals("y", v.initialize { count++; "y" })
        Assert.assertEquals(2, count)
    }

    // endregion

    // region 并发

    @Test
    fun initialize_concurrent_runsOnce_test() {
        val v = LazyValue<String>()
        val count = AtomicInteger(0)
        val results = runConcurrently(32) {
            v.initialize {
                count.incrementAndGet()
                Thread.sleep(1) // 拉宽竞争窗口，确保真正发生竞争
                "x"
            }
        }
        Assert.assertEquals(1, count.get()) // 双重检查锁定保证仅初始化一次
        Assert.assertEquals(List(32) { "x" }, results)
        Assert.assertEquals("x", v.value)
    }

    // endregion

    private fun <T> runConcurrently(threadCount: Int, action: () -> T): List<T> {
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val results = java.util.Collections.synchronizedList(mutableListOf<T>())
        val failures = java.util.Collections.synchronizedList(mutableListOf<Throwable>())
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    startLatch.await()
                    results.add(action())
                } catch (t: Throwable) {
                    failures.add(t)
                } finally {
                    doneLatch.countDown()
                }
            }
        }
        startLatch.countDown()
        doneLatch.await()
        executor.shutdown()
        failures.firstOrNull()?.let { throw AssertionError("并发执行出现异常", it) }
        return results.toList()
    }
}
