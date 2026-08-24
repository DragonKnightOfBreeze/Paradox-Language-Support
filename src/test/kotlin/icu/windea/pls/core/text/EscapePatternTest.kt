package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see EscapePattern
 * @see EscapePatterns
 */
class EscapePatternTest {
    private val default = EscapePatterns.Default
    private val html = EscapePatterns.HtmlLineBreak

    // region Default.escape

    @Test
    fun default_escape_specialChars() {
        Assert.assertEquals("\\n", default.escape("\n"))
        Assert.assertEquals("\\r", default.escape("\r"))
        Assert.assertEquals("\\t", default.escape("\t"))
        Assert.assertEquals("\\\\", default.escape("\\"))
    }

    @Test
    fun default_escape_mixed() {
        Assert.assertEquals("a\\nb", default.escape("a\nb"))
        Assert.assertEquals("a\\nb\\tc", default.escape("a\nb\tc"))
        Assert.assertEquals("a\\\\b", default.escape("a\\b"))
    }

    // endregion

    // region Default.unescape

    @Test
    fun default_unescape_specialChars() {
        Assert.assertEquals("\n", default.unescape("\\n"))
        Assert.assertEquals("\r", default.unescape("\\r"))
        Assert.assertEquals("\t", default.unescape("\\t"))
        Assert.assertEquals("\\", default.unescape("\\\\"))
    }

    @Test
    fun default_unescape_unknownEscape() {
        // 未知转义仅去除反斜线
        Assert.assertEquals("x", default.unescape("\\x"))
    }

    @Test
    fun default_unescape_mixed() {
        Assert.assertEquals("a\nb", default.unescape("a\\nb"))
    }

    @Test
    fun default_unescape_trailingBackslash() {
        // 末尾的孤立反斜线保持不变
        Assert.assertEquals("a\\", default.unescape("a\\"))
        Assert.assertEquals("\\", default.unescape("\\"))
    }

    // endregion

    // region HtmlLineBreak.escape

    @Test
    fun html_escape_specialChars() {
        Assert.assertEquals("<br>\n", html.escape("\n"))
        Assert.assertEquals("<br>\n", html.escape("\r"))
        Assert.assertEquals("&emsp;", html.escape("\t"))
    }

    @Test
    fun html_escape_mixed() {
        Assert.assertEquals("a<br>\nb", html.escape("a\nb"))
        Assert.assertEquals("a&emsp;b", html.escape("a\tb"))
    }

    @Test
    fun html_escape_crlf() {
        // `\r\n` 会被分别替换为两个 `<br>\n`
        Assert.assertEquals("a<br>\n<br>\nb", html.escape("a\r\nb"))
    }

    // endregion

    // region HtmlLineBreak.unescape

    @Test
    fun html_unescape_specialTokens() {
        Assert.assertEquals("\n", html.unescape("<br>\n"))
        Assert.assertEquals("\n", html.unescape("<br>\r"))
        Assert.assertEquals("\n", html.unescape("<br>"))
        Assert.assertEquals("\t", html.unescape("&emsp;"))
    }

    @Test
    fun html_unescape_mixed() {
        Assert.assertEquals("a\nb", html.unescape("a<br>\nb"))
        Assert.assertEquals("a\tb", html.unescape("a&emsp;b"))
    }

    @Test
    fun html_unescape_partialTokens() {
        // 不完整的标记保持不变
        Assert.assertEquals("<", html.unescape("<"))
        Assert.assertEquals("&", html.unescape("&"))
        Assert.assertEquals("<b>", html.unescape("<b>"))
        Assert.assertEquals("&emsp", html.unescape("&emsp"))
    }

    // endregion

    // region 无需转义/反转义时的引用相等边界测试

    @Test
    fun noChange_returnsSameInstance_plainText() {
        val text = String(charArrayOf('a', 'b', 'c'))
        Assert.assertSame(text, default.escape(text))
        Assert.assertSame(text, default.unescape(text))
        Assert.assertSame(text, html.escape(text))
        Assert.assertSame(text, html.unescape(text))
    }

    @Test
    fun noChange_returnsSameInstance_empty() {
        val text = String(charArrayOf())
        Assert.assertSame(text, default.escape(text))
        Assert.assertSame(text, default.unescape(text))
        Assert.assertSame(text, html.escape(text))
        Assert.assertSame(text, html.unescape(text))
    }

    // endregion
}
