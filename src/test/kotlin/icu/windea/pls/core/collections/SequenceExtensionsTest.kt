package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class SequenceExtensionsTest {
    @Test
    fun filterIsInstance_test() {
        val seq1: Sequence<Any?> = sequenceOf(1, "a", null, "abc", 2)
        val onlyOneChar = seq1.filterIsInstance<String> { it.length == 1 }.toList()
        assertEquals(listOf("a"), onlyOneChar)
    }

    @Test
    fun filterIsInstanceTo_test() {
        val dest = mutableListOf<String>()
        val seq: Sequence<Any?> = sequenceOf(1, "a", "abc", 2)
        val ref = seq.filterIsInstanceTo(dest) { it.length == 3 }
        assertSame(dest, ref)
        assertEquals(listOf("abc"), dest)
    }

    @Test
    fun findIsInstance_test() {
        val seq: Sequence<Any?> = sequenceOf(1, "a", null, "abc", 2)

        val firstLen3 = seq.findIsInstance<String> { it.length == 3 }
        assertEquals("abc", firstLen3)

        val none = seq.findIsInstance<String> { it.length == 4 }
        assertNull(none)
    }

    @Test
    fun process_test() {
        val seq = sequenceOf(1, 2, 3)
        var sum = 0
        val cont = seq.process {
            sum += it
            true
        }
        assertTrue(cont)
        assertEquals(6, sum)

        sum = 0
        val short = sequenceOf(1, 2, 3).process {
            sum += it
            false
        }
        assertFalse(short)
        assertEquals(1, sum)
    }
}
