package icu.windea.pls.config.configExpression

import org.junit.Assert.*
import org.junit.Test

class CwtCardinalityExpressionEdgeCasesTest {
    @Test
    fun resolveNoSeparator_returnsEmptyExpression() {
        val e = CwtCardinalityExpression.resolve("1")
        val empty = CwtCardinalityExpression.resolveEmpty()
        assertEquals(empty.expressionString, e.expressionString)
        assertEquals(empty.min, e.min)
        assertEquals(empty.max, e.max)
        assertEquals(empty.relaxMin, e.relaxMin)
        assertEquals(empty.relaxMax, e.relaxMax)
    }

    @Test
    fun resolveNegativeMin_isClampedToZero() {
        // 当前实现将最小值钳制到 >= 0
        val s = "-1..2"
        val e = CwtCardinalityExpression.resolve(s)
        assertEquals(0, e.min)
        assertEquals(2, e.max)
        assertFalse(e.relaxMin)
        assertFalse(e.relaxMax)
        assertEquals(s, e.toString())
    }

    @Test
    fun resolveInvalidMin_defaultsToZero() {
        val s = "a..2"
        val e = CwtCardinalityExpression.resolve(s)
        assertEquals(0, e.min)
        assertEquals(2, e.max)
        assertFalse(e.relaxMin)
        assertFalse(e.relaxMax)
    }

    @Test
    fun resolveInvalidMax_becomesNull() {
        val s = "1..x"
        val e = CwtCardinalityExpression.resolve(s)
        assertEquals(1, e.min)
        assertNull(e.max)
        assertFalse(e.relaxMin)
        assertFalse(e.relaxMax)
    }

    @Test
    fun resolveInf_caseInsensitive() {
        val s = "1..INF"
        val e = CwtCardinalityExpression.resolve(s)
        assertEquals(1, e.min)
        assertNull(e.max)
        assertFalse(e.relaxMin)
        assertFalse(e.relaxMax)
    }

    @Test
    fun resolveDoubleRelaxMin_parsesAsRelaxAndZero() {
        val s = "~~1..2"
        val e = CwtCardinalityExpression.resolve(s)
        assertTrue(e.relaxMin)
        assertEquals(0, e.min) // "~1" -> not numeric -> defaults to 0
        assertEquals(2, e.max)
    }

    @Test
    fun resolveTripleDots_treatsTailAsInvalidMax() {
        val s = "1...2"
        val e = CwtCardinalityExpression.resolve(s)
        assertEquals(1, e.min)
        assertNull(e.max) // ".2" -> not numeric -> null
        assertFalse(e.relaxMin)
        assertFalse(e.relaxMax)
    }

    @Test
    fun equalsAndHashCode_basedOnOriginalString() {
        val s = "1..x"
        val e1 = CwtCardinalityExpression.resolve(s)
        val e2 = CwtCardinalityExpression.resolve(s)
        assertEquals(e1, e2)
        assertEquals(e1.hashCode(), e2.hashCode())
        assertEquals(s, e1.toString())
    }
}
