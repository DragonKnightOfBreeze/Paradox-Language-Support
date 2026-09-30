package icu.windea.pls.core.ui

import org.junit.Assert
import org.junit.Test
import java.awt.Color

/**
 * @see ColorService
 */
class ColorServiceTest {
    // region getColorFromHex

    @Test
    fun getColorFromHex_test() {
        val expectedRed = color(255, 0, 0)
        Assert.assertEquals(expectedRed, ColorService.getColorFromHex("0xff0000")!!)
        Assert.assertEquals(expectedRed, ColorService.getColorFromHex("0xFF0000")!!)
        // 前缀忽略大小写
        Assert.assertEquals(expectedRed, ColorService.getColorFromHex("0XFF0000")!!)
        // 8 位：RGBA
        Assert.assertEquals(color(255, 0, 0, 128), ColorService.getColorFromHex("0xff000080")!!)
        // 3 位：每个半字节重复一次
        Assert.assertEquals(color(0x11, 0x22, 0x33), ColorService.getColorFromHex("0x123")!!)
        // 4 位：RGBA
        Assert.assertEquals(color(0, 0, 0, 0x88), ColorService.getColorFromHex("0x0008")!!)
    }

    @Test
    fun getColorFromHex_test_invalid() {
        Assert.assertNull(ColorService.getColorFromHex("ff0000"))    // 缺少前缀
        Assert.assertNull(ColorService.getColorFromHex(""))
        Assert.assertNull(ColorService.getColorFromHex("0x"))
        Assert.assertNull(ColorService.getColorFromHex("0x12"))      // 长度非法
        Assert.assertNull(ColorService.getColorFromHex("0x12345"))   // 长度非法
        // 非法十六进制字符：应返回 null，而不是抛出异常
        Assert.assertNull(ColorService.getColorFromHex("0xGGGGGG"))
        Assert.assertNull(ColorService.getColorFromHex("0x12G456"))
    }

    // endregion

    // region getNewColorArgFromHex

    @Test
    fun getNewColorArgFromHex_test() {
        // 6 位：不含 alpha
        Assert.assertEquals("0xff0000", ColorService.getNewColorArgFromHex("0xff0000", color(255, 0, 0)))
        // 8 位：含 alpha
        Assert.assertEquals("0xff0000ff", ColorService.getNewColorArgFromHex("0xffffffff", color(255, 0, 0)))
        Assert.assertEquals("0xff000080", ColorService.getNewColorArgFromHex("0x00000080", color(255, 0, 0, 128)))
        // 前缀忽略大小写
        Assert.assertEquals("0xff0000", ColorService.getNewColorArgFromHex("0XFF0000", color(255, 0, 0)))
    }

    @Test
    fun getNewColorArgFromHex_test_invalid() {
        Assert.assertNull(ColorService.getNewColorArgFromHex("ff0000", color(255, 0, 0)))   // 缺少前缀
        Assert.assertNull(ColorService.getNewColorArgFromHex("0x12", color(255, 0, 0)))      // 长度非法
        Assert.assertNull(ColorService.getNewColorArgFromHex("0xGGGGGG", color(255, 0, 0)))  // 非法十六进制字符
    }

    // endregion

    // region getColorFromRgb

    @Test
    fun getColorFromRgb_test() {
        // 整数表示（0..255）
        Assert.assertEquals(color(255, 0, 0), ColorService.getColorFromRgb(listOf("255", "0", "0"))!!)
        Assert.assertEquals(color(255, 0, 0, 128), ColorService.getColorFromRgb(listOf("255", "0", "0", "128"))!!)
        // 超出范围时截断
        Assert.assertEquals(color(255, 0, 0), ColorService.getColorFromRgb(listOf("300", "-5", "0"))!!)
        // 浮点表示（0.0..1.0）
        Assert.assertEquals(color(127, 0, 0), ColorService.getColorFromRgb(listOf("0.5", "0.0", "0.0"))!!)
        Assert.assertEquals(color(255, 0, 0, 127), ColorService.getColorFromRgb(listOf("1.0", "0.0", "0.0", "0.5"))!!)
        // 浮点表示中超出上限（或下限）时截断，而不是回退为默认值
        Assert.assertEquals(color(255, 127, 127), ColorService.getColorFromRgb(listOf("2.0", "0.5", "0.5"))!!)
        Assert.assertEquals(color(0, 0, 0), ColorService.getColorFromRgb(listOf("-0.5", "0.0", "0.0"))!!)
        // alpha 超出 0..255 时截断为 255，而不是抛出异常
        Assert.assertEquals(color(255, 0, 0), ColorService.getColorFromRgb(listOf("255", "0", "0", "300"))!!)
        Assert.assertEquals(color(255, 0, 0), ColorService.getColorFromRgb(listOf("1.0", "0.0", "0.0", "2.0"))!!)
        // 参数个数不合法
        Assert.assertNull(ColorService.getColorFromRgb(listOf("255", "0")))
        Assert.assertNull(ColorService.getColorFromRgb(emptyList()))
    }

    @Test
    fun getColorFromRgb_test_invalid() {
        // 非数字参数：不可用，返回 null
        Assert.assertNull(ColorService.getColorFromRgb(listOf("abc", "0", "0")))
        Assert.assertNull(ColorService.getColorFromRgb(listOf("0.5", "abc", "0")))
    }

    // endregion

    // region getNewColorArgsFromRgb

    @Test
    fun getNewColorArgsFromRgb_test() {
        // 整数表示
        Assert.assertEquals(listOf("255", "0", "0"), ColorService.getNewColorArgsFromRgb(listOf("255", "0", "0"), color(255, 0, 0)))
        Assert.assertEquals(listOf("255", "0", "0", "128"), ColorService.getNewColorArgsFromRgb(listOf("255", "0", "0", "128"), color(255, 0, 0, 128)))
        // 浮点表示（默认精确度保留 3 位小数）
        Assert.assertEquals(listOf("1.0", "0.0", "0.0"), ColorService.getNewColorArgsFromRgb(listOf("0.5", "0.0", "0.0"), color(255, 0, 0)))
        Assert.assertEquals(listOf("0.502", "0.0", "0.0"), ColorService.getNewColorArgsFromRgb(listOf("0.5", "0.0", "0.0"), color(128, 0, 0)))
        Assert.assertEquals(listOf("1.0", "0.0", "0.0", "0.502"), ColorService.getNewColorArgsFromRgb(listOf("1.0", "0.0", "0.0", "0.5"), color(255, 0, 0, 128)))
        // 指定精确度
        Assert.assertEquals(listOf("0.5", "0.5", "0.5"), ColorService.getNewColorArgsFromRgb(listOf("0.5", "0.5", "0.5"), color(128, 128, 128), precision = -1))
        // 参数个数不合法
        Assert.assertNull(ColorService.getNewColorArgsFromRgb(listOf("255", "0"), color(255, 0, 0)))
    }

    @Test
    fun getNewColorArgsFromRgb_test_invalid() {
        // 非数字参数：不可用，返回 null
        Assert.assertNull(ColorService.getNewColorArgsFromRgb(listOf("0.5", "abc", "0"), color(255, 0, 0)))
    }

    // endregion

    // region getColorFromHsv

    @Test
    fun getColorFromHsv_test() {
        // 红：h=0, s=1, v=1
        Assert.assertEquals(Color.getHSBColor(0f, 1f, 1f), ColorService.getColorFromHsv(listOf("0.0", "1.0", "1.0"))!!)
        // alpha
        Assert.assertEquals(color(255, 0, 0, 127), ColorService.getColorFromHsv(listOf("0.0", "1.0", "1.0", "0.5"))!!)
        // h/s/v 超出范围时截断
        Assert.assertEquals(Color.getHSBColor(1f, 0f, 1f), ColorService.getColorFromHsv(listOf("2.0", "-1.0", "2.0"))!!)
        // alpha 超出 0..1 时截断为 255，而不是抛出异常
        Assert.assertEquals(color(255, 0, 0), ColorService.getColorFromHsv(listOf("0.0", "1.0", "1.0", "2.0"))!!)
        // 参数个数不合法
        Assert.assertNull(ColorService.getColorFromHsv(listOf("0.0", "1.0")))
    }

    @Test
    fun getColorFromHsv_test_invalid() {
        // 非数字参数：不可用，返回 null
        Assert.assertNull(ColorService.getColorFromHsv(listOf("abc", "abc", "abc")))
    }

    // endregion

    // region getNewColorArgsFromHsv

    @Test
    fun getNewColorArgsFromHsv_test() {
        Assert.assertEquals(listOf("0.0", "1.0", "1.0"), ColorService.getNewColorArgsFromHsv(listOf("0.0", "1.0", "1.0"), color(255, 0, 0)))
        // 含 alpha
        Assert.assertEquals(listOf("0.0", "1.0", "1.0", "0.502"), ColorService.getNewColorArgsFromHsv(listOf("0.0", "1.0", "1.0", "1.0"), color(255, 0, 0, 128)))
        // 参数个数不合法
        Assert.assertNull(ColorService.getNewColorArgsFromHsv(listOf("0.0", "1.0"), color(255, 0, 0)))
    }

    // endregion

    // region getColorFromHsv360

    @Test
    fun getColorFromHsv360_test() {
        // alpha
        Assert.assertEquals(color(0, 255, 255, 128), ColorService.getColorFromHsv360(listOf("180", "100", "100", "128"))!!)
        // h/s/v 超出范围时截断
        Assert.assertEquals(Color.getHSBColor(1f, 1f, 1f), ColorService.getColorFromHsv360(listOf("400", "200", "200"))!!)
        // alpha 超出 0..255 时截断为 255，而不是抛出异常
        Assert.assertEquals(color(0, 255, 255), ColorService.getColorFromHsv360(listOf("180", "100", "100", "300"))!!)
        // 参数个数不合法
        Assert.assertNull(ColorService.getColorFromHsv360(listOf("180", "100")))
    }

    @Test
    fun getColorFromHsv360_test_invalid() {
        // 非数字参数：不可用，返回 null
        Assert.assertNull(ColorService.getColorFromHsv360(listOf("abc", "100", "100")))
    }

    // endregion

    // region getNewColorArgsFromHsv360

    @Test
    fun getNewColorArgsFromHsv360_test() {
        Assert.assertEquals(listOf("0.0", "100.0", "100.0"), ColorService.getNewColorArgsFromHsv360(listOf("180", "100", "100"), color(255, 0, 0)))
        // 含 alpha
        Assert.assertEquals(listOf("0.0", "100.0", "100.0", "128"), ColorService.getNewColorArgsFromHsv360(listOf("180", "100", "100", "128"), color(255, 0, 0, 128)))
        // 参数个数不合法
        Assert.assertNull(ColorService.getNewColorArgsFromHsv360(listOf("180", "100"), color(255, 0, 0)))
    }

    // endregion

    // region isAvailableColorArg / isAvailableColorArgs

    @Test
    fun isAvailableColorArg_test() {
        Assert.assertTrue(ColorService.isAvailableColorArg("0xff0000"))
        Assert.assertTrue(ColorService.isAvailableColorArg("0xFF0000"))
        Assert.assertTrue(ColorService.isAvailableColorArg("0XFF0000"))
        Assert.assertTrue(ColorService.isAvailableColorArg("0xff000080"))
        Assert.assertTrue(ColorService.isAvailableColorArg("0x123"))
        Assert.assertTrue(ColorService.isAvailableColorArg("0x0008"))
        // 形状不合法
        Assert.assertFalse(ColorService.isAvailableColorArg("ff0000"))    // 缺少前缀
        Assert.assertFalse(ColorService.isAvailableColorArg(""))
        Assert.assertFalse(ColorService.isAvailableColorArg("0x"))
        Assert.assertFalse(ColorService.isAvailableColorArg("0x12"))      // 长度非法
        Assert.assertFalse(ColorService.isAvailableColorArg("0x12345"))   // 长度非法
        Assert.assertFalse(ColorService.isAvailableColorArg("0xGGGGGG"))  // 非法十六进制字符
    }

    @Test
    fun isAvailableColorArgs_test() {
        Assert.assertTrue(ColorService.isAvailableColorArgs(listOf("255", "0", "0")))
        Assert.assertTrue(ColorService.isAvailableColorArgs(listOf("0.5", "0.0", "0.0")))
        // 超出区间，但仍可用（会被截断）
        Assert.assertTrue(ColorService.isAvailableColorArgs(listOf("255", "0", "0", "300")))
        Assert.assertTrue(ColorService.isAvailableColorArgs(listOf("-0.5", "0.0", "0.0")))
        // 参数个数不合法
        Assert.assertFalse(ColorService.isAvailableColorArgs(emptyList()))
        Assert.assertFalse(ColorService.isAvailableColorArgs(listOf("255", "0")))
        Assert.assertFalse(ColorService.isAvailableColorArgs(listOf("1", "2", "3", "4", "5")))
        // 非数字
        Assert.assertFalse(ColorService.isAvailableColorArgs(listOf("abc", "0", "0")))
        Assert.assertFalse(ColorService.isAvailableColorArgs(listOf("0.5", "abc", "0")))
    }

    // endregion

    // region isValidColorArg / isValidRgbColorArgs / isValidHsvColorArgs / isValidHsv360ColorArgs

    @Test
    fun isValidColorArg_test() {
        // hex 颜色参数只有格式要求
        Assert.assertTrue(ColorService.isValidColorArg("0xff0000"))
        Assert.assertTrue(ColorService.isValidColorArg("0x123"))
        Assert.assertFalse(ColorService.isValidColorArg("0xGGGGGG"))
        Assert.assertFalse(ColorService.isValidColorArg("ff0000"))
    }

    @Test
    fun isValidRgbColorArgs_test() {
        // 整数表示
        Assert.assertTrue(ColorService.isValidRgbColorArgs(listOf("255", "0", "0")))
        Assert.assertTrue(ColorService.isValidRgbColorArgs(listOf("255", "0", "0", "255")))
        // 浮点表示
        Assert.assertTrue(ColorService.isValidRgbColorArgs(listOf("0.5", "0.0", "0.0")))
        Assert.assertTrue(ColorService.isValidRgbColorArgs(listOf("1.0", "0.0", "0.0", "0.5")))
        // 超出区间
        Assert.assertFalse(ColorService.isValidRgbColorArgs(listOf("256", "0", "0")))
        Assert.assertFalse(ColorService.isValidRgbColorArgs(listOf("-1", "0", "0")))
        Assert.assertFalse(ColorService.isValidRgbColorArgs(listOf("1.5", "0.0", "0.0")))
        Assert.assertFalse(ColorService.isValidRgbColorArgs(listOf("0.5", "0.0", "0.0", "1.5")))
        // 形状不合法
        Assert.assertFalse(ColorService.isValidRgbColorArgs(listOf("abc", "0", "0")))
        Assert.assertFalse(ColorService.isValidRgbColorArgs(listOf("255", "0")))
    }

    @Test
    fun isValidHsvColorArgs_test() {
        Assert.assertTrue(ColorService.isValidHsvColorArgs(listOf("0.0", "1.0", "1.0")))
        Assert.assertTrue(ColorService.isValidHsvColorArgs(listOf("0.0", "1.0", "1.0", "0.5")))
        // 超出区间
        Assert.assertFalse(ColorService.isValidHsvColorArgs(listOf("2.0", "1.0", "1.0")))
        Assert.assertFalse(ColorService.isValidHsvColorArgs(listOf("-0.5", "0.0", "0.0")))
        Assert.assertFalse(ColorService.isValidHsvColorArgs(listOf("0.0", "1.0", "1.0", "2.0")))
        // 形状不合法
        Assert.assertFalse(ColorService.isValidHsvColorArgs(listOf("abc", "0", "0")))
    }

    @Test
    fun isValidHsv360ColorArgs_test() {
        Assert.assertTrue(ColorService.isValidHsv360ColorArgs(listOf("180", "100", "100")))
        Assert.assertTrue(ColorService.isValidHsv360ColorArgs(listOf("0", "100", "100", "255")))
        Assert.assertTrue(ColorService.isValidHsv360ColorArgs(listOf("360", "100", "100")))
        // 超出区间
        Assert.assertFalse(ColorService.isValidHsv360ColorArgs(listOf("400", "100", "100")))
        Assert.assertFalse(ColorService.isValidHsv360ColorArgs(listOf("180", "200", "100")))
        Assert.assertFalse(ColorService.isValidHsv360ColorArgs(listOf("180", "100", "300")))
        Assert.assertFalse(ColorService.isValidHsv360ColorArgs(listOf("180", "100", "100", "300")))
        // 参数需为整数
        Assert.assertFalse(ColorService.isValidHsv360ColorArgs(listOf("180.0", "100", "100")))
        // 形状不合法
        Assert.assertFalse(ColorService.isValidHsv360ColorArgs(listOf("180", "100")))
    }

    // endregion

    // region regression

    @Test
    fun getColorFromHsv360_test_hue() {
        // 180 / 360 应为浮点除法，得到 hue = 0.5，而不是整数除法得到的 0
        Assert.assertEquals(Color.getHSBColor(0.5f, 1f, 1f), ColorService.getColorFromHsv360(listOf("180", "100", "100")))
        Assert.assertEquals(Color.getHSBColor(0f, 1f, 1f), ColorService.getColorFromHsv360(listOf("0", "100", "100")))
        Assert.assertEquals(Color.getHSBColor(1f, 1f, 1f), ColorService.getColorFromHsv360(listOf("360", "100", "100")))
    }

    @Test
    fun getColorFromHsv360_test_saturationAndValue() {
        // 50 / 100 应为浮点除法，得到 0.5
        Assert.assertEquals(Color.getHSBColor(0f, 0.5f, 1f), ColorService.getColorFromHsv360(listOf("0", "50", "100")))
        Assert.assertEquals(Color.getHSBColor(0f, 1f, 0.5f), ColorService.getColorFromHsv360(listOf("0", "100", "50")))
    }

    // endregion

    private fun color(r: Int, g: Int, b: Int, a: Int = 255): Color = Color(r, g, b, a)
}
