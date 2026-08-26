package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ProcessorFactory
 */
class ProcessorFactoryTest {
    @Test
    fun findProcessor_test() {
        val p = ProcessorFactory.find<Int> { it > 2 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertFalse(p.process(3)) // 找到第一个匹配后终止
        Assert.assertEquals(3, p.result)
    }

    @Test
    fun duplicateProcessor_test() {
        val p = ProcessorFactory.duplicate<Int> { it > 1 }
        Assert.assertFalse(p.duplicated)
        Assert.assertTrue(p.process(2)) // 第一个匹配
        Assert.assertFalse(p.duplicated)
        Assert.assertFalse(p.process(3)) // 第二个匹配 -> duplicated
        Assert.assertTrue(p.duplicated)
    }

    @Test
    fun collectProcessor_test() {
        val p = ProcessorFactory.collect<Int> { it % 2 == 0 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.process(3))
        Assert.assertTrue(p.process(4))
        Assert.assertEquals(listOf(2, 4), p.collection)
    }

    @Test
    fun allProcessor_test() {
        val p = ProcessorFactory.all<Int> { it > 0 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.result)

        val p2 = ProcessorFactory.all<Int> { it > 0 }
        Assert.assertTrue(p2.process(1))
        Assert.assertFalse(p2.process(-1))
        Assert.assertFalse(p2.result)
    }

    @Test
    fun anyProcessor_test() {
        val p = ProcessorFactory.any<Int> { it > 2 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertFalse(p.process(3))
        Assert.assertTrue(p.result)

        val p2 = ProcessorFactory.any<Int> { it > 10 }
        Assert.assertTrue(p2.process(1))
        Assert.assertFalse(p2.result)
    }

    @Test
    fun noneProcessor_test() {
        val p = ProcessorFactory.none<Int> { it > 2 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertFalse(p.process(3))
        Assert.assertFalse(p.result)

        val p2 = ProcessorFactory.none<Int> { it > 10 }
        Assert.assertTrue(p2.process(1))
        Assert.assertTrue(p2.result)
    }
}
