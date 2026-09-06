package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

class TextServiceTest {
    // region convertToBoolean methods

    @Test
    fun convertToBooleanFromYesNo() {
        Assert.assertEquals(true, TextService.convertToBooleanFromYesNo("yes"))
        Assert.assertEquals(false, TextService.convertToBooleanFromYesNo("no"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("Yes"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("NO"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("TRUE"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("False"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("on"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("OFF"))
        Assert.assertEquals(null, TextService.convertToBooleanFromYesNo("<inject>"))
    }

    @Test
    fun convertToBooleanLenient() {
        Assert.assertEquals(true, TextService.convertToBooleanLenient("yes"))
        Assert.assertEquals(false, TextService.convertToBooleanLenient("no"))
        Assert.assertEquals(true, TextService.convertToBooleanLenient("Yes"))
        Assert.assertEquals(false, TextService.convertToBooleanLenient("NO"))
        Assert.assertEquals(true, TextService.convertToBooleanLenient("TRUE"))
        Assert.assertEquals(false, TextService.convertToBooleanLenient("False"))
        Assert.assertEquals(true, TextService.convertToBooleanLenient("on"))
        Assert.assertEquals(false, TextService.convertToBooleanLenient("OFF"))
        Assert.assertEquals(null, TextService.convertToBooleanLenient("<inject>"))
    }

    // endregion

    // region decodeLiteralText

    @Test
    fun decodeLiteralText_noEscapes() {
        val input = "hello world"
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = TextService.decodeLiteralText(input, out, offsets)
        Assert.assertTrue(result)
        Assert.assertEquals("hello world", out.toString())
        // 无转义时 sourceOffsets 为恒等映射
        for (i in offsets.indices) {
            Assert.assertEquals(i, offsets[i])
        }
    }

    @Test
    fun decodeLiteralText_escapedQuote() {
        // `\"` → `"`
        val input = "a\\\"b"
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = TextService.decodeLiteralText(input, out, offsets)
        Assert.assertTrue(result)
        Assert.assertEquals("a\"b", out.toString())
    }

    @Test
    fun decodeLiteralText_escapedBackslash() {
        // `\\` → `\`
        val input = "a\\\\b"
        val out = StringBuilder()
        val result = TextService.decodeLiteralText(input, out, null)
        Assert.assertTrue(result)
        Assert.assertEquals("a\\b", out.toString())
    }

    @Test
    fun decodeLiteralText_unknownEscape() {
        // `\n` 不是此方法识别的转义（仅识别 `\"` 和 `\\`），保留原样
        val input = "a\\nb"
        val out = StringBuilder()
        val result = TextService.decodeLiteralText(input, out, null)
        Assert.assertTrue(result)
        Assert.assertEquals("a\\nb", out.toString())
    }

    @Test
    fun decodeLiteralText_trailingBackslash() {
        // 末尾的 `\` 无后续字符，返回 false 表示解析失败
        val input = "abc\\"
        val out = StringBuilder()
        val result = TextService.decodeLiteralText(input, out, null)
        Assert.assertFalse(result)
    }

    @Suppress("KotlinConstantConditions")
    @Test
    fun decodeLiteralText_emptyString() {
        val input = ""
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = TextService.decodeLiteralText(input, out, offsets)
        Assert.assertTrue(result)
        Assert.assertEquals("", out.toString())
    }

    @Test
    fun decodeLiteralText_multipleEscapes() {
        // `###\"\\\\\"` 包含多个转义序列
        val input = """###\"\\\\"""" // ###\"\\\\\"
        val out = StringBuilder()
        val result = TextService.decodeLiteralText(input, out, null)
        Assert.assertTrue(result)
        Assert.assertEquals("###\"\\\\\"", out.toString())
    }

    @Test
    fun decodeLiteralText_nullOffsets() {
        // sourceOffsets 为 null 时不填充偏移量，但功能正常
        val input = "a\\\"b"
        val out = StringBuilder()
        val result = TextService.decodeLiteralText(input, out, null)
        Assert.assertTrue(result)
        Assert.assertEquals("a\"b", out.toString())
    }

    @Test
    fun decodeLiteralText_offsets_withEscape() {
        // 验证转义后的 sourceOffsets 映射
        val input = "a\\\"b" // 4 chars → output "a"b" 3 chars
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = TextService.decodeLiteralText(input, out, offsets)
        Assert.assertTrue(result)
        Assert.assertEquals("a\"b", out.toString())
        // offsets[0]=0 (a→a), offsets[1]=1 (\"→"), offsets[2]=3 (b→b), offsets[3]=4
        Assert.assertEquals(0, offsets[0])
        Assert.assertEquals(1, offsets[1])
        Assert.assertEquals(3, offsets[2])
        Assert.assertEquals(4, offsets[3])
    }

    // endregion
}
