package icu.windea.pls.lang.util

import icu.windea.pls.core.text.EscapeType
import org.junit.Assert.*
import org.junit.Test

/**
 * @see ParadoxEscapeManager
 */
class ParadoxEscapeManagerTest {
    // region unescapeScriptText

    @Test
    fun unescapeScriptText_plainText() {
        assertEquals("hello world", ParadoxEscapeManager.unescapeScriptText("hello world"))
    }

    @Test
    fun unescapeScriptText_newline() {
        assertEquals("a\nb", ParadoxEscapeManager.unescapeScriptText("a\\nb"))
    }

    @Test
    fun unescapeScriptText_carriageReturn() {
        assertEquals("a\rb", ParadoxEscapeManager.unescapeScriptText("a\\rb"))
    }

    @Test
    fun unescapeScriptText_tab() {
        assertEquals("a\tb", ParadoxEscapeManager.unescapeScriptText("a\\tb"))
    }

    @Test
    fun unescapeScriptText_escapedBackslash() {
        // `\\` → `\`（转义字符本身）
        assertEquals("a\\b", ParadoxEscapeManager.unescapeScriptText("a\\\\b"))
    }

    @Test
    fun unescapeScriptText_unknownEscape() {
        // 未知转义序列：`\x` → `x`（直接输出转义字符后的字符）
        assertEquals("ax", ParadoxEscapeManager.unescapeScriptText("a\\xb".substring(0, 3)))
    }

    @Test
    fun unescapeScriptText_multipleEscapes() {
        assertEquals("a\nb\tc", ParadoxEscapeManager.unescapeScriptText("a\\nb\\tc"))
    }

    @Test
    fun unescapeScriptText_emptyString() {
        assertEquals("", ParadoxEscapeManager.unescapeScriptText(""))
    }

    @Test
    fun unescapeScriptText_trailingBackslash() {
        // 末尾的 `\` 不匹配任何转义字符，被忽略
        assertEquals("a", ParadoxEscapeManager.unescapeScriptText("a\\"))
    }

    @Test
    fun unescapeScriptText_html_newline() {
        assertEquals("a<br>\nb", ParadoxEscapeManager.unescapeScriptText("a\\nb", EscapeType.Html))
    }

    @Test
    fun unescapeScriptText_html_carriageReturn() {
        assertEquals("a<br>\rb", ParadoxEscapeManager.unescapeScriptText("a\\rb", EscapeType.Html))
    }

    @Test
    fun unescapeScriptText_html_tab() {
        assertEquals("a&emsp;b", ParadoxEscapeManager.unescapeScriptText("a\\tb", EscapeType.Html))
    }

    @Test
    fun unescapeScriptText_inlay_truncatesAtNewline() {
        // Inlay 模式遇到 `\n` 时直接截断
        assertEquals("a", ParadoxEscapeManager.unescapeScriptText("a\\nb", EscapeType.Inlay))
    }

    @Test
    fun unescapeScriptText_inlay_truncatesAtCarriageReturn() {
        // Inlay 模式遇到 `\r` 时直接截断
        assertEquals("a", ParadoxEscapeManager.unescapeScriptText("a\\rb", EscapeType.Inlay))
    }

    @Test
    fun unescapeScriptText_inlay_tabNotTruncated() {
        // Inlay 模式下 `\t` 不会截断，正常输出制表符
        assertEquals("a\tb", ParadoxEscapeManager.unescapeScriptText("a\\tb", EscapeType.Inlay))
    }

    @Test
    fun unescapeScriptText_inlay_textBeforeNewline() {
        assertEquals("prefix", ParadoxEscapeManager.unescapeScriptText("prefix\\nsuffix", EscapeType.Inlay))
    }

    @Test
    fun unescapeScriptText_consecutiveEscapes() {
        // `\n\n` → 两个换行
        assertEquals("\n\n", ParadoxEscapeManager.unescapeScriptText("\\n\\n"))
    }

    // endregion

    // region unescapeLocalisationText

    @Test
    fun unescapeLocalisationText_plainText() {
        assertEquals("hello world", ParadoxEscapeManager.unescapeLocalisationText("hello world"))
    }

    @Test
    fun unescapeLocalisationText_newline() {
        assertEquals("a\nb", ParadoxEscapeManager.unescapeLocalisationText("a\\nb"))
    }

    @Test
    fun unescapeLocalisationText_carriageReturn() {
        assertEquals("a\rb", ParadoxEscapeManager.unescapeLocalisationText("a\\rb"))
    }

    @Test
    fun unescapeLocalisationText_tab() {
        assertEquals("a\tb", ParadoxEscapeManager.unescapeLocalisationText("a\\tb"))
    }

    @Test
    fun unescapeLocalisationText_escapedBackslash() {
        assertEquals("a\\b", ParadoxEscapeManager.unescapeLocalisationText("a\\\\b"))
    }

    @Test
    fun unescapeLocalisationText_doubleLeftBracket() {
        // `[[` → `[`（本地化文本中的方括号转义）
        assertEquals("[", ParadoxEscapeManager.unescapeLocalisationText("[["))
    }

    @Test
    fun unescapeLocalisationText_singleLeftBracket() {
        // 单独的 `[` 后跟非 `[` 字符时，`[` 保留
        assertEquals("[a", ParadoxEscapeManager.unescapeLocalisationText("[a"))
    }

    @Test
    fun unescapeLocalisationText_doubleLeftBracketInText() {
        assertEquals("a[b", ParadoxEscapeManager.unescapeLocalisationText("a[[b"))
    }

    @Test
    fun unescapeLocalisationText_mixedEscapes() {
        // 同时包含 `\\`、`\n` 和 `[[`
        assertEquals("a\nb[c\\d", ParadoxEscapeManager.unescapeLocalisationText("a\\nb[[c\\\\d"))
    }

    @Test
    fun unescapeLocalisationText_emptyString() {
        assertEquals("", ParadoxEscapeManager.unescapeLocalisationText(""))
    }

    @Test
    fun unescapeLocalisationText_trailingBackslash() {
        assertEquals("a", ParadoxEscapeManager.unescapeLocalisationText("a\\"))
    }

    @Test
    fun unescapeLocalisationText_trailingLeftBracket() {
        // 末尾的 `[` 没有后续字符，不输出（isLeftBracket 在循环结束时仍为 true）
        assertEquals("a", ParadoxEscapeManager.unescapeLocalisationText("a["))
    }

    @Test
    fun unescapeLocalisationText_html_newline() {
        assertEquals("a<br>\nb", ParadoxEscapeManager.unescapeLocalisationText("a\\nb", EscapeType.Html))
    }

    @Test
    fun unescapeLocalisationText_html_tab() {
        assertEquals("a&emsp;b", ParadoxEscapeManager.unescapeLocalisationText("a\\tb", EscapeType.Html))
    }

    @Test
    fun unescapeLocalisationText_inlay_truncatesAtNewline() {
        assertEquals("a", ParadoxEscapeManager.unescapeLocalisationText("a\\nb", EscapeType.Inlay))
    }

    @Test
    fun unescapeLocalisationText_inlay_bracketEscapeStillWorks() {
        // Inlay 模式下 `[[` 转义仍然生效
        assertEquals("[", ParadoxEscapeManager.unescapeLocalisationText("[[", EscapeType.Inlay))
    }

    @Test
    fun unescapeLocalisationText_consecutiveDoubleBrackets() {
        // `[[[[` → `[[`（两次 `[[` 转义）
        assertEquals("[[", ParadoxEscapeManager.unescapeLocalisationText("[[[["))
    }

    // endregion

    // region parseScriptExpressionCharacters

    @Test
    fun parseScriptExpressionCharacters_noEscapes() {
        val input = "hello world"
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, offsets)
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
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, offsets)
        assertTrue(result)
        assertEquals("a\"b", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_escapedBackslash() {
        // `\\` → `\`
        val input = "a\\\\b"
        val out = StringBuilder()
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("a\\b", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_unknownEscape() {
        // `\n` 不是此方法识别的转义（仅识别 `\"` 和 `\\`），保留原样
        val input = "a\\nb"
        val out = StringBuilder()
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("a\\nb", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_trailingBackslash() {
        // 末尾的 `\` 无后续字符，返回 false 表示解析失败
        val input = "abc\\"
        val out = StringBuilder()
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, null)
        assertFalse(result)
    }

    @Test
    fun parseScriptExpressionCharacters_emptyString() {
        val input = ""
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, offsets)
        assertTrue(result)
        assertEquals("", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_multipleEscapes() {
        // `###\"\\\\\"` 包含多个转义序列
        val input = """###\"\\\\"""" // ###\"\\\\\"
        val out = StringBuilder()
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("###\"\\\\\"", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_nullOffsets() {
        // sourceOffsets 为 null 时不填充偏移量，但功能正常
        val input = "a\\\"b"
        val out = StringBuilder()
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, null)
        assertTrue(result)
        assertEquals("a\"b", out.toString())
    }

    @Test
    fun parseScriptExpressionCharacters_offsets_withEscape() {
        // 验证转义后的 sourceOffsets 映射
        val input = "a\\\"b" // 4 chars → output "a"b" 3 chars
        val out = StringBuilder()
        val offsets = IntArray(input.length + 1)
        val result = ParadoxEscapeManager.parseScriptExpressionCharacters(input, out, offsets)
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
