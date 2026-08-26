package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * 针对 [LazyValue.Builder] 提供的懒加载工具方法进行更严格的测试，
 * 覆盖缓存状态（字段值、`false` / `null` 的缓存）与并发性。
 *
 * @see LazyValue
 * @see LazyValue.Builder
 */
class LazyValueBuilderTest {
    // region ofBoolean

    @Test
    fun ofBoolean_caches_true_test() {
        var field = LazyValue.UNINITIALIZED_BOOLEAN
        var count = 0
        Assert.assertTrue(LazyValue.ofBoolean({ field }, { field = it }) { count++; true })
        Assert.assertTrue(LazyValue.ofBoolean({ field }, { field = it }) { count++; true })
        Assert.assertEquals(1, count)
        Assert.assertEquals(1, field.toInt()) // true 缓存为 1
    }

    @Test
    fun ofBoolean_caches_false_test() {
        var field = LazyValue.UNINITIALIZED_BOOLEAN
        var count = 0
        Assert.assertFalse(LazyValue.ofBoolean({ field }, { field = it }) { count++; false })
        Assert.assertFalse(LazyValue.ofBoolean({ field }, { field = it }) { count++; false })
        Assert.assertEquals(1, count) // false 同样会被缓存（0 != UNINITIALIZED_BOOLEAN）
        Assert.assertEquals(0, field.toInt())
    }

    @Test
    fun ofBoolean_lock_caches_value_test() {
        val lock = Any()
        var field = LazyValue.UNINITIALIZED_BOOLEAN
        var count = 0
        Assert.assertTrue(LazyValue.ofBoolean(lock, { field }, { field = it }) { count++; true })
        Assert.assertTrue(LazyValue.ofBoolean(lock, { field }, { field = it }) { count++; true })
        Assert.assertEquals(1, count)
        Assert.assertEquals(1, field.toInt())
    }

    @Test
    fun ofBoolean_lock_concurrent_runsOnce_test() {
        val lock = Any()
        var field = LazyValue.UNINITIALIZED_BOOLEAN
        val count = AtomicInteger(0)
        runConcurrently(32) {
            LazyValue.ofBoolean(lock, { field }, { field = it }) {
                count.incrementAndGet()
                Thread.sleep(1) // 拉宽竞争窗口，确保真正发生竞争
                true
            }
        }
        Assert.assertEquals(1, count.get()) // 双重检查锁定保证仅计算一次
        Assert.assertEquals(1, field.toInt())
    }

    @Test
    fun ofBoolean_lock_concurrent_caches_false_test() {
        val lock = Any()
        var field = LazyValue.UNINITIALIZED_BOOLEAN
        val count = AtomicInteger(0)
        runConcurrently(32) {
            LazyValue.ofBoolean(lock, { field }, { field = it }) {
                count.incrementAndGet()
                Thread.sleep(1)
                false
            }
        }
        Assert.assertEquals(1, count.get())
        Assert.assertEquals(0, field.toInt())
    }

    // endregion

    // region ofNullable

    @Test
    fun ofNullable_caches_value_test() {
        var field: Any? = LazyValue.UNINITIALIZED
        var count = 0
        Assert.assertEquals("x", LazyValue.ofNullable({ field }, { field = it }) { count++; "x" })
        Assert.assertEquals("x", LazyValue.ofNullable({ field }, { field = it }) { count++; "y" })
        Assert.assertEquals(1, count)
        Assert.assertEquals("x", field)
    }

    @Test
    fun ofNullable_caches_null_test() {
        var field: Any? = LazyValue.UNINITIALIZED
        var count = 0
        Assert.assertNull(LazyValue.ofNullable<String?>({ field }, { field = it }) { count++; null })
        Assert.assertNull(LazyValue.ofNullable<String?>({ field }, { field = it }) { count++; null })
        Assert.assertEquals(1, count) // null 通过 UNINITIALIZED 哨兵被正确缓存
        Assert.assertNull(field)
    }

    @Test
    fun ofNullable_lock_concurrent_runsOnce_test() {
        val lock = Any()
        var field: Any? = LazyValue.UNINITIALIZED
        val count = AtomicInteger(0)
        runConcurrently(32) {
            LazyValue.ofNullable<String>(lock, { field }, { field = it }) {
                count.incrementAndGet()
                Thread.sleep(1)
                "x"
            }
        }
        Assert.assertEquals(1, count.get())
        Assert.assertEquals("x", field)
    }

    @Test
    fun ofNullable_lock_concurrent_caches_null_test() {
        val lock = Any()
        var field: Any? = LazyValue.UNINITIALIZED
        val count = AtomicInteger(0)
        runConcurrently(32) {
            LazyValue.ofNullable<String?>(lock, { field }, { field = it }) {
                count.incrementAndGet()
                Thread.sleep(1)
                null
            }
        }
        Assert.assertEquals(1, count.get())
        Assert.assertNull(field)
    }

    // endregion

    // region of

    @Test
    fun of_caches_value_test() {
        var field: String? = null
        var count = 0
        Assert.assertEquals("x", LazyValue.of({ field }, { field = it }) { count++; "x" })
        Assert.assertEquals("x", LazyValue.of({ field }, { field = it }) { count++; "y" })
        Assert.assertEquals(1, count)
        Assert.assertEquals("x", field)
    }

    @Test
    fun of_lock_concurrent_runsOnce_test() {
        val lock = Any()
        var field: String? = null
        val count = AtomicInteger(0)
        runConcurrently(32) {
            LazyValue.of<String>(lock, { field }, { field = it }) {
                count.incrementAndGet()
                Thread.sleep(1)
                "x"
            }
        }
        Assert.assertEquals(1, count.get())
        Assert.assertEquals("x", field)
    }

    // endregion

    @Suppress("SameParameterValue")
    private fun runConcurrently(threadCount: Int, action: () -> Unit) {
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val failures = java.util.Collections.synchronizedList(mutableListOf<Throwable>())
        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    startLatch.await()
                    action()
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
        failures.firstOrNull()?.let { throw AssertionError("Exception occurred during concurrent execution", it) }
    }
}
