package icu.windea.pls.config.configExpression

import org.junit.Assert.*
import org.junit.Test

class CwtCardinalityExpressionTest {
    @Test
    fun resolveEmpty() {
        val e = CwtCardinalityExpression.resolveEmpty()
        assertEquals("", e.expressionString)
        assertEquals(0, e.min)
        assertNull(e.max)
        assertFalse(e.relaxMin)
        assertFalse(e.relaxMax)
        assertEquals(e, CwtCardinalityExpression.resolve(""))
    }

    @Test
    fun resolveBasicRanges() {
        run {
            val s = "0..1"
            val e = CwtCardinalityExpression.resolve(s)
            assertEquals(0, e.min)
            assertEquals(1, e.max)
            assertFalse(e.relaxMin)
            assertFalse(e.relaxMax)
            assertEquals(s, e.toString())
        }
        run {
            val s = "0..inf"
            val e = CwtCardinalityExpression.resolve(s)
            assertEquals(0, e.min)
            assertNull(e.max)
            assertFalse(e.relaxMin)
            assertFalse(e.relaxMax)
        }
    }

    @Test
    fun resolveRelaxedRanges() {
        run {
            val s = "~1..10"
            val e = CwtCardinalityExpression.resolve(s)
            assertEquals(1, e.min)
            assertEquals(10, e.max)
            assertTrue(e.relaxMin)
            assertFalse(e.relaxMax)
        }
        run {
            val s = "1..~10"
            val e = CwtCardinalityExpression.resolve(s)
            assertEquals(1, e.min)
            assertEquals(10, e.max)
            assertFalse(e.relaxMin)
            assertTrue(e.relaxMax)
        }
        run {
            val s = "~1..~inf"
            val e = CwtCardinalityExpression.resolve(s)
            assertEquals(1, e.min)
            assertNull(e.max)
            assertTrue(e.relaxMin)
            assertTrue(e.relaxMax)
        }
    }
}
