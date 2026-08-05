package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class CollectionFastExtraExtensionsTest {
    // region removePrefixOrNull

    @Test
    fun removePrefixOrNull_basic_and_edges_test() {
        val base = listOf(1, 2, 3)

        // empty prefix -> return this (identity)
        assertSame(base, base.removePrefixOrNull(emptyList()))

        // longer prefix -> null
        assertNull(base.removePrefixOrNull(listOf(1, 2, 3, 4)))

        // exact match -> empty list
        assertEquals(emptyList<Int>(), base.removePrefixOrNull(listOf(1, 2, 3)))

        // proper prefix -> tail
        assertEquals(listOf(3), base.removePrefixOrNull(listOf(1, 2)))

        // proper prefix -> tail
        assertEquals(listOf(2, 3), base.removePrefixOrNull(listOf(1)))

        // mismatch at first element
        assertNull(base.removePrefixOrNull(listOf(0)))

        // mismatch inside
        assertNull(base.removePrefixOrNull(listOf(1, 0)))

        // with wildcard
        assertEquals(listOf("c"), listOf("a", "b", "c").removePrefixOrNull(listOf("a", "*"), wildcard = "*"))
    }

    @Test
    fun removePrefixOrNull_same_reference_test() {
        val base = listOf(1, 2, 3)
        // identical reference prefix -> empty list
        val result = base.removePrefixOrNull(base)
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun removePrefixOrNull_wildcard_null_test() {
        // wildcard 为 null 时不做通配
        val base = listOf("a", "b", "c")
        assertNull(base.removePrefixOrNull(listOf("a", "*"), wildcard = null))
    }

    @Test
    fun removePrefixOrNull_single_element_test() {
        assertEquals(emptyList<Int>(), listOf(1).removePrefixOrNull(listOf(1)))
        assertEquals(listOf(2), listOf(1, 2).removePrefixOrNull(listOf(1)))
        assertNull(listOf(1).removePrefixOrNull(listOf(2)))
    }

    @Test
    fun removePrefixOrNull_immutable_result_test() {
        val result = listOf(1, 2, 3).removePrefixOrNull(listOf(1))
        assertNotNull(result)
        assertThrows(UnsupportedOperationException::class.java) {
            (result as MutableList).add(999)
        }
    }

    // endregion

    // region removeSuffixOrNull

    @Test
    fun removeSuffixOrNull_basic_and_edges_test() {
        val base = listOf(1, 2, 3)

        // empty suffix -> return this (identity)
        assertSame(base, base.removeSuffixOrNull(emptyList()))

        // longer suffix -> null
        assertNull(base.removeSuffixOrNull(listOf(0, 1, 2, 3)))

        // exact match -> empty list
        assertEquals(emptyList<Int>(), base.removeSuffixOrNull(listOf(1, 2, 3)))

        // proper suffix -> head
        assertEquals(listOf(1), base.removeSuffixOrNull(listOf(2, 3)))

        // proper suffix -> head
        assertEquals(listOf(1, 2), base.removeSuffixOrNull(listOf(3)))

        // mismatch at last element
        assertNull(base.removeSuffixOrNull(listOf(4)))

        // mismatch inside
        assertNull(base.removeSuffixOrNull(listOf(2, 4)))

        // with wildcard
        assertEquals(listOf("a"), listOf("a", "b", "c").removeSuffixOrNull(listOf("b", "*"), wildcard = "*"))
    }

    @Test
    fun removeSuffixOrNull_same_reference_test() {
        val base = listOf(1, 2, 3)
        val result = base.removeSuffixOrNull(base)
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun removeSuffixOrNull_wildcard_null_test() {
        val base = listOf("a", "b", "c")
        assertNull(base.removeSuffixOrNull(listOf("*", "c"), wildcard = null))
    }

    @Test
    fun removeSuffixOrNull_immutable_result_test() {
        val result = listOf(1, 2, 3).removeSuffixOrNull(listOf(3))
        assertNotNull(result)
        assertThrows(UnsupportedOperationException::class.java) {
            (result as MutableList).add(999)
        }
    }

    // endregion
}
