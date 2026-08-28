package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see QuotePattern
 * @see QuotePatterns
 */
class QuotePatternTest {
    private val quotePattern = QuotePatterns.Default

    @Test
    fun defaultPatternMetadata() {
        Assert.assertEquals('"', quotePattern.quoteChar)
        Assert.assertTrue(quotePattern.lenient)
    }

    // region needQuote

    @Test
    fun needQuote_empty_or_singleQuoteChar() {
        Assert.assertTrue(quotePattern.needQuote(""))
        Assert.assertTrue(quotePattern.needQuote("\""))
    }

    @Test
    fun needQuote_plain() {
        Assert.assertFalse(quotePattern.needQuote("abc"))
        Assert.assertFalse(quotePattern.needQuote("abc_def"))
        Assert.assertFalse(quotePattern.needQuote("abc.def"))
    }

    @Test
    fun needQuote_whitespace() {
        Assert.assertTrue(quotePattern.needQuote("ab c"))
        Assert.assertTrue(quotePattern.needQuote(" abc"))
        Assert.assertTrue(quotePattern.needQuote("abc "))
        Assert.assertTrue(quotePattern.needQuote("a\tb"))
        Assert.assertTrue(quotePattern.needQuote("a\nb"))
    }

    @Test
    fun needQuote_innerQuote() {
        Assert.assertTrue(quotePattern.needQuote("a\"b"))
    }

    @Test
    fun needQuote_quotedIgnored() {
        // lenient 会忽略首尾的引号
        Assert.assertFalse(quotePattern.needQuote("\"abc\""))
        Assert.assertFalse(quotePattern.needQuote("\"abc"))
        Assert.assertFalse(quotePattern.needQuote("abc\""))
        Assert.assertFalse(quotePattern.needQuote("\"\""))
    }

    @Test
    fun needQuote_quotedWithInnerWhitespace() {
        Assert.assertTrue(quotePattern.needQuote("\"a b\""))
    }

    // endregion

    // region canQuote / canUnquote

    @Test
    fun canQuote() {
        Assert.assertTrue(quotePattern.canQuote("abc"))
        Assert.assertTrue(quotePattern.canQuote("\"abc"))
        Assert.assertTrue(quotePattern.canQuote("abc\""))
        Assert.assertFalse(quotePattern.canQuote("\"abc\""))
        Assert.assertFalse(quotePattern.canQuote("\"\""))
    }

    @Test
    fun canUnquote() {
        Assert.assertTrue(quotePattern.canUnquote("\"abc\""))
        Assert.assertTrue(quotePattern.canUnquote("\"abc"))
        Assert.assertTrue(quotePattern.canUnquote("abc\""))
        Assert.assertTrue(quotePattern.canUnquote("\"\""))
        Assert.assertFalse(quotePattern.canUnquote("abc"))
        Assert.assertFalse(quotePattern.canUnquote("\"a b\"")) // 内容仍需引号
    }

    // endregion

    // region isLeftQuoted / isRightQuoted / isQuoted

    @Test
    fun isLeftQuoted() {
        Assert.assertTrue(quotePattern.isLeftQuoted("\"abc"))
        Assert.assertTrue(quotePattern.isLeftQuoted("\"\""))
        Assert.assertFalse(quotePattern.isLeftQuoted("abc"))
        Assert.assertFalse(quotePattern.isLeftQuoted("abc\""))
        Assert.assertFalse(quotePattern.isLeftQuoted(""))
        Assert.assertFalse(quotePattern.isLeftQuoted("\\\"abc")) // 以反斜线开头
    }

    @Test
    fun isRightQuoted() {
        Assert.assertTrue(quotePattern.isRightQuoted("abc\""))
        Assert.assertTrue(quotePattern.isRightQuoted("\"\""))
        Assert.assertFalse(quotePattern.isRightQuoted("\"")) // 长度不足
        Assert.assertFalse(quotePattern.isRightQuoted("abc"))
        Assert.assertFalse(quotePattern.isRightQuoted("abc\\\"")) // 转义的结尾引号
    }

    @Test
    fun isQuoted() {
        Assert.assertTrue(quotePattern.isQuoted("\"abc"))
        Assert.assertTrue(quotePattern.isQuoted("abc\""))
        Assert.assertTrue(quotePattern.isQuoted("\"abc\""))
        Assert.assertFalse(quotePattern.isQuoted("abc"))
        Assert.assertFalse(quotePattern.isQuoted(""))
    }

    // endregion

    // region quoteIfNeeded

    @Test
    fun quoteIfNeeded_plainNeedsQuote() {
        Assert.assertEquals("\"a b\"", quotePattern.quoteIfNeeded("a b"))
    }

    @Test
    fun quoteIfNeeded_plainNoQuoteNeeded() {
        Assert.assertEquals("abc", quotePattern.quoteIfNeeded("abc"))
    }

    @Test
    fun quoteIfNeeded_alreadyQuoted() {
        Assert.assertEquals("\"abc\"", quotePattern.quoteIfNeeded("\"abc\""))
    }

    @Test
    fun quoteIfNeeded_leftQuotedOnly_noQuoteNeeded() {
        // 左侧已加引号且内容无需引号时，保持原样（不补齐右侧引号）
        Assert.assertEquals("\"abc", quotePattern.quoteIfNeeded("\"abc"))
    }

    @Test
    fun quoteIfNeeded_innerQuote() {
        Assert.assertEquals("\"a\\\"b\"", quotePattern.quoteIfNeeded("a\"b"))
    }

    @Test
    fun quoteIfNeeded_empty() {
        Assert.assertEquals("\"\"", quotePattern.quoteIfNeeded(""))
    }

    // endregion

    // region quote

    @Test
    fun quote_plain() {
        Assert.assertEquals("\"abc\"", quotePattern.quote("abc"))
    }

    @Test
    fun quote_empty() {
        Assert.assertEquals("\"\"", quotePattern.quote(""))
    }

    @Test
    fun quote_singleQuoteChar() {
        Assert.assertEquals("\"\"", quotePattern.quote("\""))
    }

    @Test
    fun quote_leftQuotedOnly() {
        Assert.assertEquals("\"abc\"", quotePattern.quote("\"abc"))
    }

    @Test
    fun quote_rightQuotedOnly() {
        Assert.assertEquals("\"abc\"", quotePattern.quote("abc\""))
    }

    @Test
    fun quote_fullyQuoted() {
        Assert.assertEquals("\"abc\"", quotePattern.quote("\"abc\""))
    }

    @Test
    fun quote_innerQuote() {
        Assert.assertEquals("\"a\\\"b\"", quotePattern.quote("a\"b"))
    }

    @Test
    fun quote_escapedInnerQuote() {
        // 已转义的引号不应被二次转义
        Assert.assertEquals("\"a\\\"b\"", quotePattern.quote("a\\\"b"))
    }

    // endregion

    // region unquote

    @Test
    fun unquote_plain() {
        Assert.assertEquals("abc", quotePattern.unquote("abc"))
    }

    @Test
    fun unquote_empty() {
        Assert.assertEquals("", quotePattern.unquote(""))
    }

    @Test
    fun unquote_singleQuoteChar() {
        Assert.assertEquals("", quotePattern.unquote("\""))
    }

    @Test
    fun unquote_fullyQuoted() {
        Assert.assertEquals("abc", quotePattern.unquote("\"abc\""))
    }

    @Test
    fun unquote_leftQuotedOnly() {
        Assert.assertEquals("abc", quotePattern.unquote("\"abc"))
    }

    @Test
    fun unquote_rightQuotedOnly() {
        Assert.assertEquals("abc", quotePattern.unquote("abc\""))
    }

    @Test
    fun unquote_emptyQuotes() {
        Assert.assertEquals("", quotePattern.unquote("\"\""))
    }

    @Test
    fun unquote_escapedInnerQuote() {
        Assert.assertEquals("a\"b", quotePattern.unquote("\"a\\\"b\""))
    }

    @Test
    fun unquote_escapedQuote_keptWhenNotQuoted() {
        // 未被引号包围时，不做任何反转义
        Assert.assertEquals("a\\\"b", quotePattern.unquote("a\\\"b"))
    }

    // endregion
}
