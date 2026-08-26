package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test

/**
 * @see ConvertibleValue
 */
class ConvertibleValueTest {
    @Test
    fun to_extension_test() {
        Assert.assertEquals(5, 5.to.value)
    }

    @Test
    fun singletonList_and_OrEmpty_test() {
        Assert.assertEquals(listOf(5), 5.to.singletonList())
        Assert.assertEquals(listOf(5), 5.to.singletonListOrEmpty())
        Assert.assertEquals(emptyList<String?>(), (null as String?).to.singletonListOrEmpty())
    }

    @Test
    fun singletonSet_and_OrEmpty_test() {
        Assert.assertEquals(setOf(5), 5.to.singletonSet())
        Assert.assertEquals(setOf(5), 5.to.singletonSetOrEmpty())
        Assert.assertEquals(emptySet<String?>(), (null as String?).to.singletonSetOrEmpty())
    }

    @Test
    fun singletonMap_test() {
        Assert.assertEquals(mapOf("a" to 1), ("a" to 1).to.singletonMap())
    }

    @Test
    fun singletonSequence_and_OrEmpty_test() {
        Assert.assertEquals(listOf(5), 5.to.singletonSequence().toList())
        Assert.assertEquals(listOf(5), 5.to.singletonSequenceOrEmpty().toList())
        Assert.assertEquals(emptyList<String?>(), (null as String?).to.singletonSequenceOrEmpty().toList())
    }
}
