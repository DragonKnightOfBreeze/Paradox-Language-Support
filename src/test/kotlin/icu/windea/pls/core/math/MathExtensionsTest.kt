package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

class MathExtensionsTest {
    @Test
    fun formatted_test_isInteger() {
        Assert.assertEquals("0", 0.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("1", 1.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("0", 0.0.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("1", 1.0.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("123", 123.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("120", 123.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("123", 123.formatted(-1, isFloatingPoint = false))
        Assert.assertEquals("123", 123.formatted(-2, isFloatingPoint = false))
        Assert.assertEquals("123", 123.0.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("120", 123.0.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("123", 123.0.formatted(-1, isFloatingPoint = false))
        Assert.assertEquals("123", 123.0.formatted(-2, isFloatingPoint = false))
        Assert.assertEquals("123", 123.4567.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("120", 123.4567.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("123", 123.4567.formatted(-1, isFloatingPoint = false))
        Assert.assertEquals("123", 123.4567.formatted(-2, isFloatingPoint = false))
        Assert.assertEquals("123", 123.4567.formatted(-3, isFloatingPoint = false))
        Assert.assertEquals("123", 123.4567.formatted(-4, isFloatingPoint = false))
    }

    @Test
    fun formatted_test_isFloatingPoint() {
        Assert.assertEquals("0.0", 0.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("1.0", 1.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("0.0", 0.0.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("1.0", 1.0.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("120.0", 123.formatted(1, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.formatted(-1, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.formatted(-2, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.0.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("120.0", 123.0.formatted(1, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.0.formatted(-1, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.0.formatted(-2, isFloatingPoint = true))
        Assert.assertEquals("123.0", 123.4567.formatted(0, isFloatingPoint = true))
        Assert.assertEquals("120.0", 123.4567.formatted(1, isFloatingPoint = true))
        Assert.assertEquals("123.5", 123.4567.formatted(-1, isFloatingPoint = true))
        Assert.assertEquals("123.46", 123.4567.formatted(-2, isFloatingPoint = true))
        Assert.assertEquals("123.457", 123.4567.formatted(-3, isFloatingPoint = true))
        Assert.assertEquals("123.4567", 123.4567.formatted(-4, isFloatingPoint = true))
        Assert.assertEquals("123.4567", 123.4567.formatted(-5, isFloatingPoint = true))

    }
}
