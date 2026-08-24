package icu.windea.pls.core

import icu.windea.pls.core.optimizer.OptimizerFactory
import org.junit.Assert
import org.junit.Test

class OptimizerExtensionsTest {
    @Test
    fun optimized_string_smokeTest() {
        Assert.assertEquals("abc", "abc".optimized())
        Assert.assertEquals("", "".optimized())
    }

    @Test
    fun optimized_collections_smokeTest() {
        Assert.assertEquals(listOf("a", "b"), listOf("a", "b").optimized())
        Assert.assertEquals(setOf("a", "b"), setOf("a", "b").optimized())
        Assert.assertEquals(mapOf("a" to 1), mapOf("a" to 1).optimized())
    }

    @Test
    fun optimized_generic_roundTrip_smokeTest() {
        val optimizer = OptimizerFactory.create<String, String>(
            optimizeAction = { "$it!" },
            deoptimizeAction = { it.removeSuffix("!") },
        )
        val r = "abc".optimized(optimizer)
        Assert.assertEquals("abc!", r)
        Assert.assertEquals("abc", r.deoptimized(optimizer))
    }

    @Test
    fun optimizedIfEmpty_smokeTest() {
        val nonEmpty = listOf("a")
        Assert.assertSame(nonEmpty, nonEmpty.optimizedIfEmpty())
        Assert.assertTrue(emptyList<String>().optimizedIfEmpty().isEmpty())
        Assert.assertTrue(emptySet<String>().optimizedIfEmpty().isEmpty())
        Assert.assertTrue(emptyMap<String, Int>().optimizedIfEmpty().isEmpty())
    }
}
