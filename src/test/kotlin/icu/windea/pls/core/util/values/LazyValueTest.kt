package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test

/**
 * @see LazyValue
 */
class LazyValueTest {
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

    @Test
    fun ofBoolean_ofNullable_of_test() {
        // ofBoolean：计算一次
        var bField = LazyValue.UNINITIALIZED_BOOLEAN
        var bCount = 0
        Assert.assertTrue(LazyValue.ofBoolean({ bField }, { bField = it }) { bCount++; true })
        Assert.assertTrue(LazyValue.ofBoolean({ bField }, { bField = it }) { bCount++; false })
        Assert.assertEquals(1, bCount)

        // ofNullable：计算一次
        var nField: Any? = LazyValue.UNINITIALIZED
        var nCount = 0
        Assert.assertEquals("x", LazyValue.ofNullable<String>({ nField }, { nField = it }) { nCount++; "x" })
        Assert.assertEquals("x", LazyValue.ofNullable<String>({ nField }, { nField = it }) { nCount++; "y" })
        Assert.assertEquals(1, nCount)

        // of：计算一次
        var f: Int? = null
        var fCount = 0
        Assert.assertEquals(1, LazyValue.of({ f }, { f = it }) { fCount++; 1 })
        Assert.assertEquals(1, LazyValue.of({ f }, { f = it }) { fCount++; 2 })
        Assert.assertEquals(1, fCount)
    }
}
