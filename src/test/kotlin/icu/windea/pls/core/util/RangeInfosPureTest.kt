package icu.windea.pls.core.util

import org.junit.Assert.*
import org.junit.Test

/**
 * 纯 Kotlin 单元测试：IntRangeInfo / FloatRangeInfo
 *
 * 覆盖点：
 * - from(...) 表达式解析（四种开/闭端点、缺失端点、负数、空表达式/非法表达式）
 * - contains(...) 包含判断（开/闭边界、无下界/无上界、双无界、start>end 情况）
 * - expression/toString 一致性
 */
class RangeInfosPureTest {
    // --------------- IntRangeInfo.from ---------------

    @Test
    fun testInt_from_validClosedClosed() {
        val r = IntRangeInfo.from("[1..10]")!!
        assertEquals(1, r.start)
        assertEquals(10, r.end)
        assertFalse(r.openStart)
        assertFalse(r.openEnd)
        assertEquals("[1..10]", r.expression)
        assertEquals(r.expression, r.toString())
    }

    @Test
    fun testInt_from_validOpenOpen() {
        val r = IntRangeInfo.from("(1..10)")!!
        assertEquals(1, r.start)
        assertEquals(10, r.end)
        assertTrue(r.openStart)
        assertTrue(r.openEnd)
        assertEquals("(1..10)", r.expression)
    }

    @Test
    fun testInt_from_validOpenClosed() {
        val r = IntRangeInfo.from("(1..10]")!!
        assertEquals(1, r.start)
        assertEquals(10, r.end)
        assertTrue(r.openStart)
        assertFalse(r.openEnd)
        assertEquals("(1..10]", r.expression)
    }

    @Test
    fun testInt_from_validClosedOpen() {
        val r = IntRangeInfo.from("[1..10)")!!
        assertEquals(1, r.start)
        assertEquals(10, r.end)
        assertFalse(r.openStart)
        assertTrue(r.openEnd)
        assertEquals("[1..10)", r.expression)
    }

    @Test
    fun testInt_from_missingBothEnds() {
        val r = IntRangeInfo.from("[..]")!!
        assertNull(r.start)
        assertNull(r.end)
        assertFalse(r.openStart)
        assertFalse(r.openEnd)
        assertEquals("[null..null]", r.expression)
    }

    @Test
    fun testInt_from_missingStart() {
        val r = IntRangeInfo.from("[..10]")!!
        assertNull(r.start)
        assertEquals(10, r.end)
        assertEquals("[null..10]", r.expression)
    }

    @Test
    fun testInt_from_missingEnd() {
        val r = IntRangeInfo.from("[1..]")!!
        assertEquals(1, r.start)
        assertNull(r.end)
        assertEquals("[1..null]", r.expression)
    }

    @Test
    fun testInt_from_negativeNumbers() {
        val r = IntRangeInfo.from("[-5..5)")!!
        assertEquals(-5, r.start)
        assertEquals(5, r.end)
        assertFalse(r.openStart)
        assertTrue(r.openEnd)
        assertEquals("[-5..5)", r.expression)
    }

    @Test
    fun testInt_from_invalidExpressions_returnNull() {
        assertNull(IntRangeInfo.from(""))
        assertNull(IntRangeInfo.from("[]"))
        assertNull(IntRangeInfo.from("abc"))
        assertNull(IntRangeInfo.from("[1..10"))
        assertNull(IntRangeInfo.from("1..10]"))
    }

    // --------------- IntRangeInfo.contains ---------------

    @Test
    fun testInt_contains_closedRangeBoundaries() {
        val r = IntRangeInfo.from("[1..10]")!!
        assertTrue(1 in r)
        assertTrue(10 in r)
        assertFalse(0 in r)
        assertFalse(11 in r)
    }

    @Test
    fun testInt_contains_openRangeBoundaries() {
        val r = IntRangeInfo.from("(1..10)")!!
        assertFalse(1 in r)
        assertFalse(10 in r)
        assertTrue(2 in r)
        assertTrue(9 in r)
    }

    @Test
    fun testInt_contains_leftOpenRightClosed() {
        val r = IntRangeInfo.from("(1..10]")!!
        assertFalse(1 in r)
        assertTrue(10 in r)
    }

    @Test
    fun testInt_contains_leftClosedRightOpen() {
        val r = IntRangeInfo.from("[1..10)")!!
        assertTrue(1 in r)
        assertFalse(10 in r)
    }

    @Test
    fun testInt_contains_unboundedStart() {
        val r = IntRangeInfo.from("[..10]")!!
        assertTrue((-100) in r)
        assertTrue(10 in r)
        assertFalse(11 in r)
    }

    @Test
    fun testInt_contains_unboundedEnd() {
        val r = IntRangeInfo.from("[1..]")!!
        assertTrue(1 in r)
        assertTrue(1000 in r)
        assertFalse(0 in r)
    }

    @Test
    fun testInt_contains_unboundedBoth_alwaysTrue() {
        val r = IntRangeInfo.from("[..]")!!
        for (v in listOf(-100, 0, 100)) {
            assertTrue(v in r)
        }
    }

    @Test
    fun testInt_contains_startGreaterThanEnd_alwaysFalse() {
        val r = IntRangeInfo.from("[10..1]")!!
        for (v in listOf(0, 5, 10)) {
            assertFalse(v in r)
        }
    }

    // --------------- FloatRangeInfo.from ---------------

    @Test
    fun testFloat_from_validClosedClosed() {
        val r = FloatRangeInfo.from("[1.5..2.5]")!!
        assertEquals(1.5f, r.start)
        assertEquals(2.5f, r.end)
        assertFalse(r.openStart)
        assertFalse(r.openEnd)
        assertEquals("[1.5..2.5]", r.expression)
    }

    @Test
    fun testFloat_from_validOpenOpen() {
        val r = FloatRangeInfo.from("(1.0..2.0)")!!
        assertEquals(1.0f, r.start)
        assertEquals(2.0f, r.end)
        assertTrue(r.openStart)
        assertTrue(r.openEnd)
        assertEquals("(1.0..2.0)", r.expression)
    }

    @Test
    fun testFloat_from_missingEndsAndNegative() {
        val r1 = FloatRangeInfo.from("[..1.0]")!!
        assertNull(r1.start)
        assertEquals(1.0f, r1.end)
        assertEquals("[null..1.0]", r1.expression)

        val r2 = FloatRangeInfo.from("[-2.0..]")!!
        assertEquals(-2.0f, r2.start)
        assertNull(r2.end)
        assertEquals("[-2.0..null]", r2.expression)
    }

    @Test
    fun testFloat_from_invalidExpressions_returnNull() {
        assertNull(FloatRangeInfo.from(""))
        assertNull(FloatRangeInfo.from("()"))
        assertNull(FloatRangeInfo.from("abc"))
        assertNull(FloatRangeInfo.from("(1.0..2.0"))
        assertNull(FloatRangeInfo.from("1.0..2.0)"))
    }

    // --------------- FloatRangeInfo.contains ---------------

    @Test
    fun testFloat_contains_closedAndOpen() {
        val r1 = FloatRangeInfo.from("[1.5..2.5]")!!
        assertTrue(1.5f in r1)
        assertTrue(2.5f in r1)
        assertFalse(1.49f in r1)
        assertFalse(2.51f in r1)

        val r2 = FloatRangeInfo.from("(1.5..2.5)")!!
        assertFalse(1.5f in r2)
        assertFalse(2.5f in r2)
        assertTrue(1.6f in r2)
        assertTrue(2.4f in r2)
    }

    @Test
    fun testFloat_contains_mixedBounds() {
        val r = FloatRangeInfo.from("(1.0..2.0]")!!
        assertFalse(1.0f in r)
        assertTrue(2.0f in r)
        assertTrue(1.5f in r)
    }

    @Test
    fun testFloat_contains_unbounded() {
        val r1 = FloatRangeInfo.from("[..1.0]")!!
        assertTrue((-100.0f) in r1)
        assertTrue(1.0f in r1)
        assertFalse(1.0001f in r1)

        val r2 = FloatRangeInfo.from("[1.0..]")!!
        assertTrue(1.0f in r2)
        assertTrue(100.0f in r2)
        assertFalse(0.9999f in r2)
    }

    @Test
    fun testFloat_contains_startGreaterThanEnd_alwaysFalse() {
        val r = FloatRangeInfo.from("[2.0..1.0]")!!
        for (v in listOf(0.5f, 1.0f, 2.0f)) {
            assertFalse(v in r)
        }
    }
}
