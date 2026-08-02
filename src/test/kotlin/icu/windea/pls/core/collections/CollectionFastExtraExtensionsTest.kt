package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class CollectionFastExtraExtensionsTest {
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
}
