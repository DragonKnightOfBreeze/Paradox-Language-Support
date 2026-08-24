package icu.windea.pls.localisation.text

import icu.windea.pls.core.text.EscapePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationEscapePattern
 */
class ParadoxLocalisationEscapePatternTest {
    private val p = EscapePatterns.ParadoxLocalisation

    // region escape

    @Test
    fun escape_specialChars() {
        Assert.assertEquals("\\n", p.escape("\n"))
        Assert.assertEquals("\\r", p.escape("\r"))
        Assert.assertEquals("\\t", p.escape("\t"))
        Assert.assertEquals("\\\\", p.escape("\\"))
    }

    @Test
    fun escape_mixed() {
        Assert.assertEquals("a\\nb", p.escape("a\nb"))
    }

    // endregion

    // region unescape

    @Test
    fun unescape_specialChars() {
        Assert.assertEquals("\n", p.unescape("\\n"))
        Assert.assertEquals("\r", p.unescape("\\r"))
        Assert.assertEquals("\t", p.unescape("\\t"))
        Assert.assertEquals("\\", p.unescape("\\\\"))
    }

    @Test
    fun unescape_unknownEscape() {
        // 未知转义仅去除反斜线
        Assert.assertEquals("x", p.unescape("\\x"))
    }

    @Test
    fun unescape_escapedBracket() {
        Assert.assertEquals("[", p.unescape("[["))
        Assert.assertEquals("a[b", p.unescape("a[[b"))
        Assert.assertEquals("[[", p.unescape("[[[["))
    }

    @Test
    fun unescape_singleBracket() {
        // 单独的 `[` 保持不变（不会被误判为 `[[` 而丢失后续字符）
        Assert.assertEquals("a[b", p.unescape("a[b"))
        Assert.assertEquals("[", p.unescape("["))
    }

    @Test
    fun unescape_trailingBackslash() {
        // 末尾的孤立反斜线保持不变
        Assert.assertEquals("a\\", p.unescape("a\\"))
        Assert.assertEquals("\\", p.unescape("\\"))
    }

    // endregion

    // region 无需转义/反转义时的引用相等

    @Test
    fun noChange_returnsSameInstance() {
        val text = String(charArrayOf('a', 'b', 'c'))
        Assert.assertSame(text, p.escape(text))
        Assert.assertSame(text, p.unescape(text))
    }

    // endregion
}
