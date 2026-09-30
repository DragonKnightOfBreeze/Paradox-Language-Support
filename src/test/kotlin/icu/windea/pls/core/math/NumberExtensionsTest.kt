package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal

/**
 * @see formatted
 * @see convertToInt
 * @see convertToLong
 * @see convertToFloat
 * @see convertToDouble
 */
class NumberExtensionsTest {
    // region formatted

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

    @Test
    fun formatted_test_defaultPrecision() {
        // 默认精确度为 -3（保留 3 位小数）
        Assert.assertEquals("0.0", 0.formatted())
        Assert.assertEquals("123.0", 123.formatted())
        Assert.assertEquals("1.235", 1.2345.formatted())
        Assert.assertEquals("1.234", 1.2344.formatted())
        Assert.assertEquals("1.999", 1.999.formatted())
        Assert.assertEquals("-1.235", (-1.2345).formatted())
    }

    @Test
    fun formatted_test_roundingModeIsHalfUp() {
        // 恰好处于中点时向上（远离 0）舍入
        Assert.assertEquals("2.0", 2.4.formatted(0))
        Assert.assertEquals("3.0", 2.5.formatted(0))
        Assert.assertEquals("4.0", 3.5.formatted(0))
        Assert.assertEquals("-3.0", (-2.5).formatted(0))
        Assert.assertEquals("0.0", (-0.4).formatted(0))
        Assert.assertEquals("2", 2.4.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("3", 2.5.formatted(0, isFloatingPoint = false))
        Assert.assertEquals("-3", (-2.5).formatted(0, isFloatingPoint = false))
        Assert.assertEquals("1.3", 1.25.formatted(-1))
        Assert.assertEquals("1.2", 1.24.formatted(-1))
        Assert.assertEquals("-1.3", (-1.25).formatted(-1))
    }

    @Test
    fun formatted_test_positivePrecision() {
        // 舍入到整数部分的第 digits 位（十位、百位……）
        Assert.assertEquals("120", 123.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("130", 125.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("120", 124.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("0", 1.5.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("10", 5.formatted(1, isFloatingPoint = false))
        Assert.assertEquals("100", 149.formatted(2, isFloatingPoint = false))
        Assert.assertEquals("100", 149.999.formatted(2, isFloatingPoint = false))
        Assert.assertEquals("200", 150.formatted(2, isFloatingPoint = false))
        Assert.assertEquals("200", 151.formatted(2, isFloatingPoint = false))
        Assert.assertEquals("-200", (-150).formatted(2, isFloatingPoint = false))
        Assert.assertEquals("1000", 999.formatted(3, isFloatingPoint = false))
        Assert.assertEquals("123000", 123456.formatted(3, isFloatingPoint = false))
        Assert.assertEquals("0", 0.formatted(3, isFloatingPoint = false))
        Assert.assertEquals("0.0", 0.formatted(3, isFloatingPoint = true))
        Assert.assertEquals("120.0", 123.formatted(1, isFloatingPoint = true))
        Assert.assertEquals("12300", 12345L.formatted(2, isFloatingPoint = false))
    }

    @Test
    fun formatted_test_negativePrecision() {
        Assert.assertEquals("1.0", 1.0.formatted(-3))
        Assert.assertEquals("100.0", 100.formatted(-2))
        Assert.assertEquals("1.25", 1.25.formatted(-2))
        Assert.assertEquals("0.0", 0.0004.formatted(-3))
        Assert.assertEquals("0.001", 0.0005.formatted(-3))
        Assert.assertEquals("-0.001", (-0.0005).formatted(-3))
        // isFloatingPoint = false 时不会保留小数部分
        Assert.assertEquals("2", 1.9.formatted(-1, isFloatingPoint = false))
        Assert.assertEquals("-2", (-1.5).formatted(-1, isFloatingPoint = false))
        Assert.assertEquals("124", 123.9.formatted(-1, isFloatingPoint = false))
    }

    @Test
    fun formatted_test_avoidPrecisionLoss() {
        // 通过字符串构造 BigDecimal，避免二进制浮点表示的精度丢失
        Assert.assertEquals("0.1", 0.1f.formatted(-1))
        Assert.assertEquals("0.1", 0.1.formatted(-1))
        Assert.assertEquals("0.3", (0.1 + 0.2).formatted(-2))
        // 1.005 的二进制表示小于字面值，若直接使用 double 构造则会被舍入为 1.00
        Assert.assertEquals("1.01", 1.005.formatted(-2))
        Assert.assertEquals("2.68", 2.675.formatted(-2))
    }

    @Test
    fun formatted_test_bigDecimalAndOtherTypes() {
        Assert.assertEquals("2.68", BigDecimal("2.675").formatted(-2))
        Assert.assertEquals("123.0", BigDecimal("123").formatted(0))
        Assert.assertEquals("120", BigDecimal("123.4567").formatted(1, isFloatingPoint = false))
    }

    // endregion

    // region convertToInt

    @Test
    fun convertToInt_test() {
        Assert.assertEquals(7, (null as Int?).convertToInt(7))
        // 默认值不会被范围限制
        Assert.assertEquals(7, (null as Double?).convertToInt(7, 1..10))
        Assert.assertEquals(5, 5.convertToInt(7))
        Assert.assertEquals(5, 5.convertToInt(7, 1..10))
        Assert.assertEquals(1, 0.convertToInt(7, 1..10))
        Assert.assertEquals(10, 11.convertToInt(7, 1..10))
        // 截断小数部分，而不是四舍五入
        Assert.assertEquals(2, 2.9.convertToInt(0))
        Assert.assertEquals(-2, (-2.9).convertToInt(0))
        Assert.assertEquals(2, 2.5.convertToInt(0))
        Assert.assertEquals(5, 5L.convertToInt(0))
        Assert.assertEquals(3, BigDecimal("3.9").convertToInt(0))
        // transform 接收源类型
        Assert.assertEquals(127, 0.5f.convertToInt(255, 0..255) { it * 255 })
        Assert.assertEquals(255, 2.0f.convertToInt(255, 0..255) { it * 255 })
        Assert.assertEquals(8, 4.convertToInt(0) { it * 2.0 })
    }

    // endregion

    // region convertToLong

    @Test
    fun convertToLong_test() {
        Assert.assertEquals(7L, (null as Int?).convertToLong(7L))
        Assert.assertEquals(7L, (null as Double?).convertToLong(7L, 1L..10L))
        Assert.assertEquals(5L, 5.convertToLong(7L))
        Assert.assertEquals(5L, 5.convertToLong(7L, 1L..10L))
        Assert.assertEquals(1L, 0.convertToLong(7L, 1L..10L))
        Assert.assertEquals(10L, 11.convertToLong(7L, 1L..10L))
        Assert.assertEquals(2L, 2.9.convertToLong(0L))
        Assert.assertEquals(-2L, (-2.9).convertToLong(0L))
        Assert.assertEquals(8L, 4.convertToLong(0L) { it * 2.0 })
    }

    // endregion

    // region convertToFloat

    @Test
    fun convertToFloat_test() {
        Assert.assertEquals(1f, (null as Int?).convertToFloat(1f))
        Assert.assertEquals(0.5f, 0.5.convertToFloat(1f, 0f..1f))
        Assert.assertEquals(1f, 2f.convertToFloat(1f, 0f..1f))
        Assert.assertEquals(0f, (-1f).convertToFloat(1f, 0f..1f))
        // Float 源类型：transform 中的除法是浮点除法
        Assert.assertEquals(0.5f, 180f.convertToFloat(0f) { it / 360 })
        Assert.assertEquals(1f, 0.5f.convertToFloat(0f) { it * 2 })
        // Int 源类型：由于 transform 接收 Int，需显式使用浮点字面量
        Assert.assertEquals(0.5f, 180.convertToFloat(0f, 0f..1f) { it / 360f })
        Assert.assertEquals(0.5f, 50.convertToFloat(0f, 0f..1f) { it / 100f })
        Assert.assertEquals(1f, 360.convertToFloat(0f, 0f..1f) { it / 360f })
        Assert.assertEquals(1f, 200.convertToFloat(0f, 0f..1f) { it / 100f })
        Assert.assertEquals(1f, (null as Int?).convertToFloat(1f, 0f..1f) { it / 2f })
    }

    // endregion

    // region convertToDouble

    @Test
    fun convertToDouble_test() {
        Assert.assertEquals(1.0, (null as Int?).convertToDouble(1.0), 0.0)
        Assert.assertEquals(0.5, 0.5f.convertToDouble(1.0, 0.0..1.0), 0.0)
        Assert.assertEquals(1.0, 2.0.convertToDouble(1.0, 0.0..1.0), 0.0)
        Assert.assertEquals(0.0, (-1.0).convertToDouble(1.0, 0.0..1.0), 0.0)
        Assert.assertEquals(0.5, 0.25f.convertToDouble(0.0) { it * 2 }, 0.0)
        Assert.assertEquals(2.0, 4.convertToDouble(0.0) { it / 2.0 }, 0.0)
    }

    // endregion
}
