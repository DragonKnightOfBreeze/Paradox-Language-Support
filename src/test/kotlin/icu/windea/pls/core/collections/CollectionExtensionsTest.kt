package icu.windea.pls.core.collections

import org.junit.Assert
import org.junit.Test

class CollectionExtensionsTest {
    @Test
    fun orNull_on_collection() {
        Assert.assertNull(emptyList<String>().orNull())
        val list = listOf("a")
        Assert.assertSame(list, list.orNull())
    }

    @Test
    fun toListOrThis_and_toSetOrThis_identity_and_copy() {
        val list = listOf(1, 2)
        Assert.assertSame(list, list.toListOrThis())

        val set: Set<Int> = linkedSetOf(1, 2)
        val asList = set.toListOrThis()
        Assert.assertEquals(listOf(1, 2), asList)
        // not the same reference because original was not a List
        Assert.assertNotSame(set, asList)

        val asSet = set.toSetOrThis()
        Assert.assertSame(set, asSet)
    }

    @Test
    fun asMutable_safe_cast_on_mutable_collections() {
        val list = mutableListOf(1, 2)
        val m = list.asMutable()
        m.add(3)
        Assert.assertEquals(listOf(1, 2, 3), m)

        val set = mutableSetOf(1, 2)
        val ms = set.asMutable()
        ms.add(3)
        Assert.assertEquals(setOf(1, 2, 3), ms)
    }

    @Test
    fun optimized_for_empty() {
        Assert.assertTrue(emptyList<Int>().optimized().isEmpty())
        Assert.assertEquals(listOf(1), listOf(1).optimized())

        Assert.assertTrue(emptySet<Int>().optimized().isEmpty())
        Assert.assertEquals(setOf(1), setOf(1).optimized())
    }

    @Test
    fun filterIsInstance_and_findIsInstance() {
        val list: List<Any?> = listOf(1, "a", null, "abc", 2)
        val onlyOneChar = list.filterIsInstance<String> { it.length == 1 }
        Assert.assertEquals(listOf("a"), onlyOneChar)

        val firstLen3 = list.findIsInstance<String> { it.length == 3 }
        Assert.assertEquals("abc", firstLen3)

        val none = list.findIsInstance<String> { it.length == 4 }
        Assert.assertNull(none)
    }

    @Test
    fun mapToArray_for_list_and_collection() {
        val arr1 = listOf(1, 2, 3).mapToArray { it * 2 }
        Assert.assertArrayEquals(arrayOf(2, 4, 6), arr1)

        val s = linkedSetOf(1, 2, 3)
        val arr2 = (s as Collection<Int>).mapToArray { it + 1 }
        Assert.assertArrayEquals(arrayOf(2, 3, 4), arr2)
    }

    @Test
    fun pinned_and_pinnedLast() {
        val list = listOf(1, 2, 3, 4)
        Assert.assertEquals(listOf(2, 4, 1, 3), list.pinned { it % 2 == 0 })
        Assert.assertEquals(listOf(1, 3, 2, 4), list.pinnedLast { it % 2 == 0 })
    }

    @Test
    fun process_short_circuit() {
        val list = listOf(1, 2, 3)
        Assert.assertFalse(list.process { it < 3 })
        Assert.assertTrue(list.process { it <= 3 })
    }
}
