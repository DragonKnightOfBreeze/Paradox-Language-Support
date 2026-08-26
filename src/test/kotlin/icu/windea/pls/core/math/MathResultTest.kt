package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

/**
 * @see MathResult
 */
class MathResultTest {
    // region from factory methods

    @Test
    fun from_factories_test() {
        Assert.assertEquals(MathResult(1.0, isFloatingPoint = false), MathResult.from(1))
        Assert.assertEquals(MathResult(1.0, isFloatingPoint = false), MathResult.from(1L))
        Assert.assertEquals(MathResult(1.5, isFloatingPoint = true), MathResult.from(1.5f))
        Assert.assertEquals(MathResult(1.5, isFloatingPoint = true), MathResult.from(1.5))
    }

    @Test
    fun fromString_factories_test() {
        Assert.assertEquals(MathResult.from(123), MathResult.fromIntString("123"))
        Assert.assertNull(MathResult.fromIntString("1.5"))
        Assert.assertNull(MathResult.fromIntString("abc"))

        Assert.assertEquals(MathResult.from(123L), MathResult.fromLongString("123"))
        Assert.assertNull(MathResult.fromLongString("abc"))

        Assert.assertEquals(MathResult.from(1.5f), MathResult.fromFloatString("1.5"))
        Assert.assertNull(MathResult.fromFloatString("abc"))

        Assert.assertEquals(MathResult.from(1.5), MathResult.fromDoubleString("1.5"))
        Assert.assertNull(MathResult.fromDoubleString("abc"))
    }

    // endregion

    // region isFloatingPointValue / normalized

    @Test
    fun isFloatingPointValue_test() {
        Assert.assertTrue(MathResult(2.5).isFloatingPointValue())
        Assert.assertFalse(MathResult(2.0).isFloatingPointValue())
    }

    @Test
    fun normalized_test() {
        // 非浮点：返回 Long
        Assert.assertEquals(2L, MathResult.from(2).normalized())
        Assert.assertTrue(MathResult.from(2).normalized() is Long)
        // 浮点：返回 Double
        Assert.assertEquals(2.5, MathResult.from(2.5).normalized())
        Assert.assertTrue(MathResult.from(2.5).normalized() is Double)
    }

    // endregion

    // region formatted

    @Test
    fun formatted_test() {
        Assert.assertEquals("2", MathResult.from(2).formatted())
        Assert.assertEquals("2.5", MathResult.from(2.5).formatted())
    }

    // endregion
}
