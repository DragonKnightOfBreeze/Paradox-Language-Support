package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see Tuples
 */
class TuplesTest {
    @Test
    fun tupleOf_test() {
        Assert.assertEquals(Pair(1, "a"), tupleOf(1, "a"))
        Assert.assertEquals(Triple(1, "a", true), tupleOf(1, "a", true))
        Assert.assertEquals(Tuple4(1, 2, 3, 4), tupleOf(1, 2, 3, 4))
    }

    @Test
    fun tuple4_toList_and_toString_test() {
        val t = Tuple4(1, 2, 3, 4)
        Assert.assertEquals(listOf(1, 2, 3, 4), t.toList())
        Assert.assertEquals("(1, 2, 3, 4)", t.toString())
    }
}
