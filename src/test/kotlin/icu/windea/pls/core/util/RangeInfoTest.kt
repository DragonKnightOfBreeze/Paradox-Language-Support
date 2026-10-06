package icu.windea.pls.core.util

import icu.windea.pls.test.dsl.expectScope
import org.junit.Test

/**
 * 纯 Kotlin 单元测试：[IntRangeInfo] / [FloatRangeInfo]。
 *
 * 覆盖点：
 * - [IntRangeInfo.create] / [FloatRangeInfo.create] 表达式解析（四种开/闭端点、缺失端点、负数、空表达式/非法表达式）
 * - [RangeInfo.contains] 包含判断（开/闭边界、无下界/无上界、双无界、start > end 情况）
 * - `expression` / `toString` 一致性
 * - `normalize` 去重
 *
 * @see RangeInfo
 * @see IntRangeInfo
 * @see FloatRangeInfo
 */
class RangeInfoTest {
    // region helpers

    private fun intRange(expression: String): IntRangeInfo = IntRangeInfo.create(expression)!!.normalize()

    private fun floatRange(expression: String): FloatRangeInfo = FloatRangeInfo.create(expression)!!.normalize()

    // endregion

    // region IntRangeInfo.create

    @Test
    fun testInt_from_validClosedClosed() {
        expectScope {
            val r = intRange("[1..10]")
            r.start.expectEquals(1)
            r.end.expectEquals(10)
            r.openStart.expectFalse()
            r.openEnd.expectFalse()
            r.expression.expectEquals("[1..10]")
            r.toString().expectEquals(r.expression)
        }
    }

    @Test
    fun testInt_from_validOpenOpen() {
        expectScope {
            val r = intRange("(1..10)")
            r.start.expectEquals(1)
            r.end.expectEquals(10)
            r.openStart.expectTrue()
            r.openEnd.expectTrue()
            r.expression.expectEquals("(1..10)")
        }
    }

    @Test
    fun testInt_from_validOpenClosed() {
        expectScope {
            val r = intRange("(1..10]")
            r.start.expectEquals(1)
            r.end.expectEquals(10)
            r.openStart.expectTrue()
            r.openEnd.expectFalse()
            r.expression.expectEquals("(1..10]")
        }
    }

    @Test
    fun testInt_from_validClosedOpen() {
        expectScope {
            val r = intRange("[1..10)")
            r.start.expectEquals(1)
            r.end.expectEquals(10)
            r.openStart.expectFalse()
            r.openEnd.expectTrue()
            r.expression.expectEquals("[1..10)")
        }
    }

    @Test
    fun testInt_from_missingBothEnds() {
        expectScope {
            val r = intRange("[..]")
            r.start.expectNull()
            r.end.expectNull()
            r.openStart.expectFalse()
            r.openEnd.expectFalse()
            r.expression.expectEquals("[null..null]")
        }
    }

    @Test
    fun testInt_from_missingStart() {
        expectScope {
            val r = intRange("[..10]")
            r.start.expectNull()
            r.end.expectEquals(10)
            r.expression.expectEquals("[null..10]")
        }
    }

    @Test
    fun testInt_from_missingEnd() {
        expectScope {
            val r = intRange("[1..]")
            r.start.expectEquals(1)
            r.end.expectNull()
            r.expression.expectEquals("[1..null]")
        }
    }

    @Test
    fun testInt_from_negativeNumbers() {
        expectScope {
            val r = intRange("[-5..5)")
            r.start.expectEquals(-5)
            r.end.expectEquals(5)
            r.openStart.expectFalse()
            r.openEnd.expectTrue()
            r.expression.expectEquals("[-5..5)")
        }
    }

    @Test
    fun testInt_from_invalidExpressions_returnNull() {
        expectScope {
            IntRangeInfo.create("").expectNull()
            IntRangeInfo.create("[]").expectNull()
            IntRangeInfo.create("abc").expectNull()
            IntRangeInfo.create("[1..10").expectNull()
            IntRangeInfo.create("1..10]").expectNull()
        }
    }

    // endregion

    // region IntRangeInfo.contains

    @Test
    fun testInt_contains_closedRangeBoundaries() {
        expectScope {
            val r = intRange("[1..10]")
            (1 in r).expectTrue()
            (10 in r).expectTrue()
            (0 in r).expectFalse()
            (11 in r).expectFalse()
        }
    }

    @Test
    fun testInt_contains_openRangeBoundaries() {
        expectScope {
            val r = intRange("(1..10)")
            (1 in r).expectFalse()
            (10 in r).expectFalse()
            (2 in r).expectTrue()
            (9 in r).expectTrue()
        }
    }

    @Test
    fun testInt_contains_leftOpenRightClosed() {
        expectScope {
            val r = intRange("(1..10]")
            (1 in r).expectFalse()
            (10 in r).expectTrue()
        }
    }

    @Test
    fun testInt_contains_leftClosedRightOpen() {
        expectScope {
            val r = intRange("[1..10)")
            (1 in r).expectTrue()
            (10 in r).expectFalse()
        }
    }

    @Test
    fun testInt_contains_unboundedStart() {
        expectScope {
            val r = intRange("[..10]")
            (-100 in r).expectTrue()
            (10 in r).expectTrue()
            (11 in r).expectFalse()
        }
    }

    @Test
    fun testInt_contains_unboundedEnd() {
        expectScope {
            val r = intRange("[1..]")
            (1 in r).expectTrue()
            (1000 in r).expectTrue()
            (0 in r).expectFalse()
        }
    }

    @Test
    fun testInt_contains_unboundedBoth_alwaysTrue() {
        expectScope {
            val r = intRange("[..]")
            for (v in listOf(-100, 0, 100)) {
                (v in r).expectTrue()
            }
        }
    }

    @Test
    fun testInt_contains_startGreaterThanEnd_alwaysFalse() {
        expectScope {
            val r = intRange("[10..1]")
            for (v in listOf(0, 5, 10)) {
                (v in r).expectFalse()
            }
        }
    }

    // endregion

    // region FloatRangeInfo.create

    @Test
    fun testFloat_from_validClosedClosed() {
        expectScope {
            val r = floatRange("[1.5..2.5]")
            r.start.expectEquals(1.5f)
            r.end.expectEquals(2.5f)
            r.openStart.expectFalse()
            r.openEnd.expectFalse()
            r.expression.expectEquals("[1.5..2.5]")
        }
    }

    @Test
    fun testFloat_from_validOpenOpen() {
        expectScope {
            val r = floatRange("(1.0..2.0)")
            r.start.expectEquals(1.0f)
            r.end.expectEquals(2.0f)
            r.openStart.expectTrue()
            r.openEnd.expectTrue()
            r.expression.expectEquals("(1.0..2.0)")
        }
    }

    @Test
    fun testFloat_from_missingEndsAndNegative() {
        expectScope {
            val r1 = floatRange("[..1.0]")
            r1.start.expectNull()
            r1.end.expectEquals(1.0f)
            r1.expression.expectEquals("[null..1.0]")

            val r2 = floatRange("[-2.0..]")
            r2.start.expectEquals(-2.0f)
            r2.end.expectNull()
            r2.expression.expectEquals("[-2.0..null]")
        }
    }

    @Test
    fun testFloat_from_invalidExpressions_returnNull() {
        expectScope {
            FloatRangeInfo.create("").expectNull()
            FloatRangeInfo.create("()").expectNull()
            FloatRangeInfo.create("abc").expectNull()
            FloatRangeInfo.create("(1.0..2.0").expectNull()
            FloatRangeInfo.create("1.0..2.0)").expectNull()
        }
    }

    // endregion

    // region FloatRangeInfo.contains

    @Test
    fun testFloat_contains_closedAndOpen() {
        expectScope {
            val r1 = floatRange("[1.5..2.5]")
            (1.5f in r1).expectTrue()
            (2.5f in r1).expectTrue()
            (1.49f in r1).expectFalse()
            (2.51f in r1).expectFalse()

            val r2 = floatRange("(1.5..2.5)")
            (1.5f in r2).expectFalse()
            (2.5f in r2).expectFalse()
            (1.6f in r2).expectTrue()
            (2.4f in r2).expectTrue()
        }
    }

    @Test
    fun testFloat_contains_mixedBounds() {
        expectScope {
            val r = floatRange("(1.0..2.0]")
            (1.0f in r).expectFalse()
            (2.0f in r).expectTrue()
            (1.5f in r).expectTrue()
        }
    }

    @Test
    fun testFloat_contains_unbounded() {
        expectScope {
            val r1 = floatRange("[..1.0]")
            (-100.0f in r1).expectTrue()
            (1.0f in r1).expectTrue()
            (1.0001f in r1).expectFalse()

            val r2 = floatRange("[1.0..]")
            (1.0f in r2).expectTrue()
            (100.0f in r2).expectTrue()
            (0.9999f in r2).expectFalse()
        }
    }

    @Test
    fun testFloat_contains_startGreaterThanEnd_alwaysFalse() {
        expectScope {
            val r = floatRange("[2.0..1.0]")
            for (v in listOf(0.5f, 1.0f, 2.0f)) {
                (v in r).expectFalse()
            }
        }
    }

    // endregion

    // region create / normalize

    @Test
    fun testInt_createAndNormalize() {
        expectScope {
            // `create` always produces a new instance (not interned)
            val c = IntRangeInfo.create("[1..10]").expectNotNull()
            c.expectNotSame(IntRangeInfo.create("[1..10]").expectNotNull())
            c.expectEquals(IntRangeInfo.create("[1..10]").expectNotNull())
            // `normalize` is interned by expression string
            val n = c.normalize()
            n.expectSame(c.normalize())
            n.expectSame(intRange("[1..10]"))
            n.expectEquals(c)
            // invalid expressions return null for `create`
            IntRangeInfo.create("abc").expectNull()
        }
    }

    @Test
    fun testFloat_createAndNormalize() {
        expectScope {
            val c = FloatRangeInfo.create("[1.5..2.5]").expectNotNull()
            c.expectNotSame(FloatRangeInfo.create("[1.5..2.5]").expectNotNull())
            c.expectEquals(FloatRangeInfo.create("[1.5..2.5]").expectNotNull())
            val n = c.normalize()
            n.expectSame(c.normalize())
            n.expectSame(floatRange("[1.5..2.5]"))
            n.expectEquals(c)
            FloatRangeInfo.create("abc").expectNull()
        }
    }

    // endregion
}
