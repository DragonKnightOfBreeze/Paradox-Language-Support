package icu.windea.pls.core.collections

import org.junit.*
import org.junit.Assert.*

class ReversibleSequenceTest {
    @Test
    fun test() {
        val s = reversibleSequence { operator ->
            if (operator) {
                yield(1)
                yield(2)
                yield(3)
            } else {
                yield(3)
                yield(2)
                yield(1)
            }
        }
        assertEquals(listOf(1, 2, 3), s.toList())
        assertEquals(listOf(3, 2, 1), s.reversed().toList())
    }

    @Test
    fun testEmpty() {
        val s = sequenceOf<Int>()
        assertEquals(listOf<Int>(), s.toList())
        assertEquals(listOf<Int>(), s.reversed().toList())
    }

    @Test
    fun testUnsupported() {
        val s = sequenceOf(1, 2, 3)
        assertThrows(UnsupportedOperationException::class.java) { s.reversed() }
    }
}
