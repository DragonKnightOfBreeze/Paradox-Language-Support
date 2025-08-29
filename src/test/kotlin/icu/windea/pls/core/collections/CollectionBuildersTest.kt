package icu.windea.pls.core.collections

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class CollectionBuildersTest {
    @Test
    fun mutableSet_without_comparator() {
        val s = MutableSet<Int>()
        s.addAll(listOf(3, 1, 2, 2))
        // uniqueness
        assertEquals(setOf(1, 2, 3), s.toSet())
    }

    @Test
    fun mutableSet_with_comparator_tree_order() {
        val s = MutableSet(Comparator.naturalOrder<Int>())
        s.addAll(listOf(3, 1, 2))
        // Should be TreeSet when comparator is provided
        assertTrue(s is TreeSet<*>)
        assertEquals(listOf(1, 2, 3), s.toList())
    }

    @Test
    fun merge_and_mergeTo_skip_null_or_empty() {
        val a = listOf(1, 2)
        val b = emptyList<Int>()
        val c: List<Int>? = null
        val result = merge(a, b, c)
        assertEquals(listOf(1, 2), result)

        val dest = mutableListOf<Int>()
        val ref = mergeTo(dest, a, b, c)
        assertSame(dest, ref)
        assertEquals(listOf(1, 2), dest)
    }
}
