package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.ConcurrentMap

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
    fun ofMutableMap_and_ofConcurrentMap_test() {
        Assert.assertTrue(SoftValue.ofMutableMap<String, Int>().dereference() is MutableMap<*, *>)
        Assert.assertTrue(SoftValue.ofConcurrentMap<String, Int>().dereference() is ConcurrentMap<*, *>)
    }

    @Test
    fun toString_test() {
        Assert.assertEquals("5", SoftValue { 5 }.toString())
    }
}
