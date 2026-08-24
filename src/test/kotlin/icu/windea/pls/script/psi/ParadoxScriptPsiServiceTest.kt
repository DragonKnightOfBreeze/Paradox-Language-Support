package icu.windea.pls.script.psi

import org.junit.Assert.*
import org.junit.Test

@Suppress("KotlinConstantConditions")
class ParadoxScriptPsiServiceTest {
    // region parseExpressionCharacters

    @Test
    fun parseScriptExpressionCharacters_noEscapes() {
        val input = "hello world"
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, offsets)
        assertTrue(result)
        assertEquals("hello world", out.toString())
        // 无转义时 sourceOffsets 为恒等映射
        for (i in offsets.indices) {
            assertEquals(i, offsets[i])
        }
    }

    @Test
    fun parseScriptExpressionCharacters_escapedQuote() {
        // `\"` → `"`
        val input = "a\\\"b"
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, offsets)
        assertTrue(result)
        assertEquals("a\"b", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_escapedBackslash() {
        // `\\` → `\`
        val input = "a\\\\b"
        val out = StringBuilder()
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("a\\b", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_unknownEscape() {
        // `\n` 不是此方法识别的转义（仅识别 `\"` 和 `\\`），保留原样
        val input = "a\\nb"
        val out = StringBuilder()
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("a\\nb", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_trailingBackslash() {
        // 末尾的 `\` 无后续字符，返回 false 表示解析失败
        val input = "abc\\"
        val out = StringBuilder()
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, null)
        assertFalse(result)
    }

    @Test
    fun parseScriptExpressionCharacters_emptyString() {
        val input = ""
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, offsets)
        assertTrue(result)
        assertEquals("", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_multipleEscapes() {
        // `###\"\\\\\"` 包含多个转义序列
        val input = """###\"\\\\"""" // ###\"\\\\\"
        val out = StringBuilder()
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("###\"\\\\\"", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_nullOffsets() {
        // sourceOffsets 为 null 时不填充偏移量，但功能正常
        val input = "a\\\"b"
        val out = StringBuilder()
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("a\"b", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_offsets_withEscape() {
        // 验证转义后的 sourceOffsets 映射
        val input = "a\\\"b" // 4 chars → output "a"b" 3 chars
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxScriptPsiService.parseExpressionCharacters(input, out, offsets)
        assertTrue(result)
        assertEquals("a\"b", out.toString())
        // offsets[0]=0 (a→a), offsets[1]=1 (\"→"), offsets[2]=3 (b→b), offsets[3]=4
        assertEquals(0, offsets[0])
        assertEquals(1, offsets[1])
        assertEquals(3, offsets[2])
        assertEquals(4, offsets[3])
    }

    // endregion
}
