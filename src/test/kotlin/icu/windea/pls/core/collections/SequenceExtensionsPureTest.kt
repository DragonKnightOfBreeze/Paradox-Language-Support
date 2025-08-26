package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class SequenceExtensionsPureTest {
    @Test
    fun filterIsInstance_and_findIsInstance() {
        val seq1: Sequence<Any?> = sequenceOf(1, "a", null, "abc", 2)
        val onlyOneChar = seq1.filterIsInstance<String> { it.length == 1 }.toList()
        assertEquals(listOf("a"), onlyOneChar)

        val seq2: Sequence<Any?> = sequenceOf(1, "a", null, "abc", 2)
        val firstLen3 = seq2.findIsInstance<String> { it.length == 3 }
        assertEquals("abc", firstLen3)

        val seq3: Sequence<Any?> = sequenceOf(1, "a", null, "abc", 2)
        val none = seq3.findIsInstance<String> { it.length == 4 }
        assertNull(none)
    }

    @Test
    fun mapToArray_and_process() {
        val arr = sequenceOf(1, 2, 3).mapToArray { it * 2 }
        assertArrayEquals(arrayOf(2, 4, 6), arr)

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
