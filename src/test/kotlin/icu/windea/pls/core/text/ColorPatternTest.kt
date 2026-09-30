package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test
import java.awt.Color

/**
 * @see ColorPattern
 * @see ColorPatterns
 */
class ColorPatternTest {
    private val hex = ColorPatterns.Hex
    private val rgb = ColorPatterns.Rgb
    private val hsv = ColorPatterns.Hsv
    private val hsv360 = ColorPatterns.Hsv360

    // region metadata

    @Test
    fun metadata_test() {
        Assert.assertEquals("hex", hex.name)
        Assert.assertEquals("rgb", rgb.name)
        Assert.assertEquals("hsv", hsv.name)
        Assert.assertEquals("hsv360", hsv360.name)
        Assert.assertEquals(listOf(hex, rgb, hsv, hsv360), ColorPatterns.patterns)
        Assert.assertTrue(ColorPatterns.byName("hex") === hex)
        Assert.assertTrue(ColorPatterns.byName("rgb") === rgb)
        Assert.assertTrue(ColorPatterns.byName("hsv") === hsv)
        Assert.assertTrue(ColorPatterns.byName("hsv360") === hsv360)
        Assert.assertNull(ColorPatterns.byName("unknown"))
    }

    // endregion

    // region Hex

    @Test
    fun hex_isAvailable_test() {
        Assert.assertTrue(hex.isAvailable("0xff0000"))
        Assert.assertTrue(hex.isAvailable("0xFF0000"))
        Assert.assertTrue(hex.isAvailable("0XFF0000"))
        Assert.assertTrue(hex.isAvailable("0xff000080"))
        Assert.assertTrue(hex.isAvailable("0x123"))
        Assert.assertTrue(hex.isAvailable("0x0008"))
        // 形状不合法
        Assert.assertFalse(hex.isAvailable("ff0000"))    // 缺少前缀
        Assert.assertFalse(hex.isAvailable(""))
        Assert.assertFalse(hex.isAvailable("0x"))
        Assert.assertFalse(hex.isAvailable("0x12"))      // 长度非法
        Assert.assertFalse(hex.isAvailable("0x12345"))   // 长度非法
        Assert.assertFalse(hex.isAvailable("0xGGGGGG"))  // 非法十六进制字符
    }

    @Test
    fun hex_isValid_test() {
        // hex 颜色参数只包含格式要求
        Assert.assertTrue(hex.isValid("0xff0000"))
        Assert.assertTrue(hex.isValid("0x123"))
        Assert.assertFalse(hex.isValid("0xGGGGGG"))
        Assert.assertFalse(hex.isValid("ff0000"))
    }

    @Test
    fun hex_getColor_test() {
        val expectedRed = color(255, 0, 0)
        Assert.assertEquals(expectedRed, hex.getColor("0xff0000")!!)
        Assert.assertEquals(expectedRed, hex.getColor("0xFF0000")!!)
        // 前缀忽略大小写
        Assert.assertEquals(expectedRed, hex.getColor("0XFF0000")!!)
        // 8 位：RGBA
        Assert.assertEquals(color(255, 0, 0, 128), hex.getColor("0xff000080")!!)
        // 3 位：每个半字节重复一次
        Assert.assertEquals(color(0x11, 0x22, 0x33), hex.getColor("0x123")!!)
        // 4 位：RGBA
        Assert.assertEquals(color(0, 0, 0, 0x88), hex.getColor("0x0008")!!)
        // 非法十六进制字符：应返回 null，而不是抛出异常
        Assert.assertNull(hex.getColor("0xGGGGGG"))
        Assert.assertNull(hex.getColor("ff0000"))
        Assert.assertNull(hex.getColor("0x12"))
    }

    @Test
    fun hex_getColorArgs_test() {
        // 6 位：不含 alpha
        Assert.assertEquals("0xff0000", hex.getColorArgs(color(255, 0, 0), "0xff0000"))
        // 8 位：含 alpha
        Assert.assertEquals("0xff0000ff", hex.getColorArgs(color(255, 0, 0), "0xffffffff"))
        Assert.assertEquals("0xff000080", hex.getColorArgs(color(255, 0, 0, 128), "0x00000080"))
        // 前缀忽略大小写
        Assert.assertEquals("0xff0000", hex.getColorArgs(color(255, 0, 0), "0XFF0000"))
        // 非法参考参数
        Assert.assertNull(hex.getColorArgs(color(255, 0, 0), "ff0000"))
        Assert.assertNull(hex.getColorArgs(color(255, 0, 0), "0x12"))
    }

    // endregion

    // region Rgb

    @Test
    fun rgb_isAvailable_test() {
        Assert.assertTrue(rgb.isAvailable(listOf("255", "0", "0")))
        Assert.assertTrue(rgb.isAvailable(listOf("0.5", "0.0", "0.0")))
        // 超出区间，但仍可用（会被截断）
        Assert.assertTrue(rgb.isAvailable(listOf("255", "0", "0", "300")))
        Assert.assertTrue(rgb.isAvailable(listOf("-0.5", "0.0", "0.0")))
        // 参数个数不合法
        Assert.assertFalse(rgb.isAvailable(emptyList()))
        Assert.assertFalse(rgb.isAvailable(listOf("255", "0")))
        Assert.assertFalse(rgb.isAvailable(listOf("1", "2", "3", "4", "5")))
        // 非数字
        Assert.assertFalse(rgb.isAvailable(listOf("abc", "0", "0")))
        Assert.assertFalse(rgb.isAvailable(listOf("0.5", "abc", "0")))
    }

    @Test
    fun rgb_isValid_test() {
        // 整数表示
        Assert.assertTrue(rgb.isValid(listOf("255", "0", "0")))
        Assert.assertTrue(rgb.isValid(listOf("255", "0", "0", "255")))
        // 浮点表示
        Assert.assertTrue(rgb.isValid(listOf("0.5", "0.0", "0.0")))
        Assert.assertTrue(rgb.isValid(listOf("1.0", "0.0", "0.0", "0.5")))
        // 超出区间
        Assert.assertFalse(rgb.isValid(listOf("256", "0", "0")))
        Assert.assertFalse(rgb.isValid(listOf("-1", "0", "0")))
        Assert.assertFalse(rgb.isValid(listOf("1.5", "0.0", "0.0")))
        Assert.assertFalse(rgb.isValid(listOf("0.5", "0.0", "0.0", "1.5")))
        // 形状不合法
        Assert.assertFalse(rgb.isValid(listOf("abc", "0", "0")))
        Assert.assertFalse(rgb.isValid(listOf("255", "0")))
    }

    @Test
    fun rgb_getColor_test() {
        // 整数表示（0..255）
        Assert.assertEquals(color(255, 0, 0), rgb.getColor(listOf("255", "0", "0"))!!)
        Assert.assertEquals(color(255, 0, 0, 128), rgb.getColor(listOf("255", "0", "0", "128"))!!)
        // 超出范围时截断
        Assert.assertEquals(color(255, 0, 0), rgb.getColor(listOf("300", "-5", "0"))!!)
        // 浮点表示（0.0..1.0）
        Assert.assertEquals(color(127, 0, 0), rgb.getColor(listOf("0.5", "0.0", "0.0"))!!)
        Assert.assertEquals(color(255, 0, 0, 127), rgb.getColor(listOf("1.0", "0.0", "0.0", "0.5"))!!)
        // 浮点表示中超出上限（或下限）时截断，而不是回退为默认值
        Assert.assertEquals(color(255, 127, 127), rgb.getColor(listOf("2.0", "0.5", "0.5"))!!)
        Assert.assertEquals(color(0, 0, 0), rgb.getColor(listOf("-0.5", "0.0", "0.0"))!!)
        // alpha 超出 0..255 时截断为 255，而不是抛出异常
        Assert.assertEquals(color(255, 0, 0), rgb.getColor(listOf("255", "0", "0", "300"))!!)
        Assert.assertEquals(color(255, 0, 0), rgb.getColor(listOf("1.0", "0.0", "0.0", "2.0"))!!)
        // 参数不可用
        Assert.assertNull(rgb.getColor(listOf("255", "0")))
        Assert.assertNull(rgb.getColor(emptyList()))
        Assert.assertNull(rgb.getColor(listOf("abc", "0", "0")))
    }

    @Test
    fun rgb_getColorArgs_test() {
        // 整数表示
        Assert.assertEquals(listOf("255", "0", "0"), rgb.getColorArgs(color(255, 0, 0), listOf("255", "0", "0")))
        Assert.assertEquals(listOf("255", "0", "0", "128"), rgb.getColorArgs(color(255, 0, 0, 128), listOf("255", "0", "0", "128")))
        // 浮点表示（默认精确度保留 3 位小数）
        Assert.assertEquals(listOf("1.0", "0.0", "0.0"), rgb.getColorArgs(color(255, 0, 0), listOf("0.5", "0.0", "0.0")))
        Assert.assertEquals(listOf("0.502", "0.0", "0.0"), rgb.getColorArgs(color(128, 0, 0), listOf("0.5", "0.0", "0.0")))
        Assert.assertEquals(listOf("1.0", "0.0", "0.0", "0.502"), rgb.getColorArgs(color(255, 0, 0, 128), listOf("1.0", "0.0", "0.0", "0.5")))
        // 参数个数不合法
        Assert.assertNull(rgb.getColorArgs(color(255, 0, 0), listOf("255", "0")))
        // 非数字参数
        Assert.assertNull(rgb.getColorArgs(color(255, 0, 0), listOf("0.5", "abc", "0")))
    }

    // endregion

    // region Hsv

    @Test
    fun hsv_isAvailable_test() {
        Assert.assertTrue(hsv.isAvailable(listOf("0.0", "1.0", "1.0")))
        Assert.assertTrue(hsv.isAvailable(listOf("0.0", "1.0", "1.0", "0.5")))
        Assert.assertFalse(hsv.isAvailable(listOf("0.0", "1.0")))
        Assert.assertFalse(hsv.isAvailable(listOf("abc", "1.0", "1.0")))
    }

    @Test
    fun hsv_isValid_test() {
        Assert.assertTrue(hsv.isValid(listOf("0.0", "1.0", "1.0")))
        Assert.assertTrue(hsv.isValid(listOf("0.0", "1.0", "1.0", "0.5")))
        // 超出区间
        Assert.assertFalse(hsv.isValid(listOf("2.0", "1.0", "1.0")))
        Assert.assertFalse(hsv.isValid(listOf("-0.5", "0.0", "0.0")))
        Assert.assertFalse(hsv.isValid(listOf("0.0", "1.0", "1.0", "2.0")))
    }

    @Test
    fun hsv_getColor_test() {
        // 红：h=0, s=1, v=1
        Assert.assertEquals(Color.getHSBColor(0f, 1f, 1f), hsv.getColor(listOf("0.0", "1.0", "1.0"))!!)
        // alpha
        Assert.assertEquals(color(255, 0, 0, 127), hsv.getColor(listOf("0.0", "1.0", "1.0", "0.5"))!!)
        // h/s/v 超出范围时截断
        Assert.assertEquals(Color.getHSBColor(1f, 0f, 1f), hsv.getColor(listOf("2.0", "-1.0", "2.0"))!!)
        // alpha 超出 0..1 时截断为 255，而不是抛出异常
        Assert.assertEquals(color(255, 0, 0), hsv.getColor(listOf("0.0", "1.0", "1.0", "2.0"))!!)
        // 参数不可用
        Assert.assertNull(hsv.getColor(listOf("0.0", "1.0")))
        Assert.assertNull(hsv.getColor(listOf("abc", "abc", "abc")))
    }

    @Test
    fun hsv_getColorArgs_test() {
        Assert.assertEquals(listOf("0.0", "1.0", "1.0"), hsv.getColorArgs(color(255, 0, 0), listOf("0.0", "1.0", "1.0")))
        // 含 alpha
        Assert.assertEquals(listOf("0.0", "1.0", "1.0", "0.502"), hsv.getColorArgs(color(255, 0, 0, 128), listOf("0.0", "1.0", "1.0", "1.0")))
        // 参数个数不合法
        Assert.assertNull(hsv.getColorArgs(color(255, 0, 0), listOf("0.0", "1.0")))
    }

    // endregion

    // region Hsv360

    @Test
    fun hsv360_isAvailable_test() {
        Assert.assertTrue(hsv360.isAvailable(listOf("180", "100", "100")))
        Assert.assertTrue(hsv360.isAvailable(listOf("180", "100", "100", "128")))
        // 超出区间，但仍可用
        Assert.assertTrue(hsv360.isAvailable(listOf("400", "200", "200")))
        Assert.assertFalse(hsv360.isAvailable(listOf("180", "100")))
        Assert.assertFalse(hsv360.isAvailable(listOf("abc", "100", "100")))
    }

    @Test
    fun hsv360_isValid_test() {
        Assert.assertTrue(hsv360.isValid(listOf("180", "100", "100")))
        Assert.assertTrue(hsv360.isValid(listOf("0", "100", "100", "255")))
        Assert.assertTrue(hsv360.isValid(listOf("360", "100", "100")))
        // 超出区间
        Assert.assertFalse(hsv360.isValid(listOf("400", "100", "100")))
        Assert.assertFalse(hsv360.isValid(listOf("180", "200", "100")))
        Assert.assertFalse(hsv360.isValid(listOf("180", "100", "300")))
        Assert.assertFalse(hsv360.isValid(listOf("180", "100", "100", "300")))
        // 参数需为整数
        Assert.assertFalse(hsv360.isValid(listOf("180.0", "100", "100")))
        // 形状不合法
        Assert.assertFalse(hsv360.isValid(listOf("180", "100")))
    }

    @Test
    fun hsv360_getColor_test() {
        // alpha
        Assert.assertEquals(color(0, 255, 255, 128), hsv360.getColor(listOf("180", "100", "100", "128"))!!)
        // h/s/v 超出范围时截断
        Assert.assertEquals(Color.getHSBColor(1f, 1f, 1f), hsv360.getColor(listOf("400", "200", "200"))!!)
        // alpha 超出 0..255 时截断为 255，而不是抛出异常
        Assert.assertEquals(color(0, 255, 255), hsv360.getColor(listOf("180", "100", "100", "300"))!!)
        // 参数不可用
        Assert.assertNull(hsv360.getColor(listOf("180", "100")))
        Assert.assertNull(hsv360.getColor(listOf("abc", "100", "100")))
    }

    @Test
    fun hsv360_getColorArgs_test() {
        Assert.assertEquals(listOf("0.0", "100.0", "100.0"), hsv360.getColorArgs(color(255, 0, 0), listOf("180", "100", "100")))
        // 含 alpha
        Assert.assertEquals(listOf("0.0", "100.0", "100.0", "128"), hsv360.getColorArgs(color(255, 0, 0, 128), listOf("180", "100", "100", "128")))
        // 参数个数不合法
        Assert.assertNull(hsv360.getColorArgs(color(255, 0, 0), listOf("180", "100")))
    }

    // endregion

    // region regression

    @Test
    fun hsv360_getColor_test_hue() {
        // 180 / 360 应为浮点除法，得到 hue = 0.5，而不是整数除法得到的 0
        Assert.assertEquals(Color.getHSBColor(0.5f, 1f, 1f), hsv360.getColor(listOf("180", "100", "100")))
        Assert.assertEquals(Color.getHSBColor(0f, 1f, 1f), hsv360.getColor(listOf("0", "100", "100")))
        Assert.assertEquals(Color.getHSBColor(1f, 1f, 1f), hsv360.getColor(listOf("360", "100", "100")))
    }

    @Test
    fun hsv360_getColor_test_saturationAndValue() {
        // 50 / 100 应为浮点除法，得到 0.5
        Assert.assertEquals(Color.getHSBColor(0f, 0.5f, 1f), hsv360.getColor(listOf("0", "50", "100")))
        Assert.assertEquals(Color.getHSBColor(0f, 1f, 0.5f), hsv360.getColor(listOf("0", "100", "50")))
    }

    // endregion

    private fun color(r: Int, g: Int, b: Int, a: Int = 255): Color = Color(r, g, b, a)
}
