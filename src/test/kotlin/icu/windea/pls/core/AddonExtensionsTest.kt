package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AddonExtensionsTest {
    @Test
    fun runOnce_runsAndGuards_test() {
        val marker = AtomicBoolean(false)
        var count = 0
        Assert.assertEquals(1, runOnce(marker) { count++; 1 })
        Assert.assertEquals(1, count)
        // 再次调用返回 null 且不再执行
        Assert.assertNull(runOnce(marker) { count++; 2 })
        Assert.assertEquals(1, count)
        Assert.assertTrue(marker.get())
    }

    @Test
    fun withErrorRef_success_test() {
        val errorRef = AtomicReference<Throwable>(null)
        val result = withErrorRef(errorRef) { "ok" }
        Assert.assertEquals("ok", result.getOrThrow())
        Assert.assertNull(errorRef.get())
    }

    @Test
    fun withErrorRef_failure_test() {
        val errorRef = AtomicReference<Throwable>(null)
        val e = RuntimeException("boom")
        val result = withErrorRef(errorRef) { throw e }
        Assert.assertTrue(result.isFailure)
        Assert.assertSame(e, errorRef.get())
    }

    @Test
    fun withState_setsAndClears_test() {
        val state = ThreadLocal<Boolean>()
        var inside: Boolean? = null
        val r = state.withState {
            inside = state.get()
            "done"
        }
        Assert.assertEquals("done", r)
        Assert.assertEquals(true, inside)
        Assert.assertNull(state.get())
    }

    @Test
    fun withState_clearsOnException_test() {
        val state = ThreadLocal<Boolean>()
        Assert.assertThrows(RuntimeException::class.java) {
            state.withState { throw RuntimeException("boom") }
        }
        Assert.assertNull(state.get())
    }
}
