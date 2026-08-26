package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test

/**
 * @see SoftValue
 */
class SoftValueTest {
    @Test
    fun dereference_caches_value_test() {
        var count = 0
        val v = SoftValue.create { count++; "value" }
        Assert.assertEquals("value", v.dereference())
        Assert.assertEquals("value", v.dereference())
        Assert.assertEquals(1, count) // 未发生 GC 时复用缓存
    }

    @Test
    fun create_and_invoke_test() {
        Assert.assertEquals(1, SoftValue.create { 1 }.dereference())
        Assert.assertEquals(2, SoftValue { 2 }.dereference())
    }

    @Test
    fun toString_test() {
        Assert.assertEquals("5", SoftValue { 5 }.toString())
    }

    @Test
    fun createValue_isInvokedEagerlyOnce_test() {
        var count = 0
        val v = SoftValue.create { count++; "value" } // 构造时即创建一次
        Assert.assertEquals(1, count)
        v.dereference()
        v.dereference()
        Assert.assertEquals(1, count) // 未发生 GC 时复用缓存
    }
}
