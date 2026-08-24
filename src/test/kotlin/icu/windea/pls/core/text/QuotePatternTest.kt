package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see QuotePattern
 * @see QuotePatterns
 */
class QuotePatternTest {
    private val q = QuotePatterns.Default

    @Test
    fun defaultPatternMetadata() {
        Assert.assertEquals('"', q.quoteChar)
        Assert.assertTrue(q.lenient)
    }

    // region needQuote

    @Test
    fun needQuote_empty() {
        Assert.assertTrue(q.needQuote(""))
    }

    @Test
    fun needQuote_singleQuoteChar() {
        Assert.assertTrue(q.needQuote("\""))
    }

    @Test
    fun needQuote_plainText() {
        Assert.assertFalse(q.needQuote("abc"))
    }

    @Test
    fun needQuote_whitespace() {
        Assert.assertTrue(q.needQuote("ab c"))
        Assert.assertTrue(q.needQuote(" abc"))
        Assert.assertTrue(q.needQuote("abc "))
        Assert.assertTrue(q.needQuote("a\tb"))
        Assert.assertTrue(q.needQuote("a\nb"))
    }

    @Test
    fun needQuote_innerQuote() {
        Assert.assertTrue(q.needQuote("a\"b"))
    }

    @Test
    fun needQuote_quotedIgnored() {
        // lenient 会忽略首尾的引号
        Assert.assertFalse(q.needQuote("\"abc\""))
        Assert.assertFalse(q.needQuote("\"abc"))
        Assert.assertFalse(q.needQuote("abc\""))
        Assert.assertFalse(q.needQuote("\"\""))
    }

    @Test
    fun needQuote_quotedWithInnerWhitespace() {
        Assert.assertTrue(q.needQuote("\"a b\""))
    }

    // endregion

    // region canQuote / canUnquote

    @Test
    fun canQuote() {
        Assert.assertTrue(q.canQuote("abc"))
        Assert.assertTrue(q.canQuote("\"abc"))
        Assert.assertTrue(q.canQuote("abc\""))
        Assert.assertFalse(q.canQuote("\"abc\""))
        Assert.assertFalse(q.canQuote("\"\""))
    }

    @Test
    fun canUnquote() {
        Assert.assertTrue(q.canUnquote("\"abc\""))
        Assert.assertTrue(q.canUnquote("\"abc"))
        Assert.assertTrue(q.canUnquote("abc\""))
        Assert.assertTrue(q.canUnquote("\"\""))
        Assert.assertFalse(q.canUnquote("abc"))
        Assert.assertFalse(q.canUnquote("\"a b\"")) // 内容仍需引号
    }

    // endregion

    // region isLeftQuoted / isRightQuoted / isQuoted

    @Test
    fun isLeftQuoted() {
        Assert.assertTrue(q.isLeftQuoted("\"abc"))
        Assert.assertTrue(q.isLeftQuoted("\"\""))
        Assert.assertFalse(q.isLeftQuoted("abc"))
        Assert.assertFalse(q.isLeftQuoted("abc\""))
        Assert.assertFalse(q.isLeftQuoted(""))
        Assert.assertFalse(q.isLeftQuoted("\\\"abc")) // 以反斜线开头
    }

    @Test
    fun isRightQuoted() {
        Assert.assertTrue(q.isRightQuoted("abc\""))
        Assert.assertTrue(q.isRightQuoted("\"\""))
        Assert.assertFalse(q.isRightQuoted("\"")) // 长度不足
        Assert.assertFalse(q.isRightQuoted("abc"))
        Assert.assertFalse(q.isRightQuoted("abc\\\"")) // 转义的结尾引号
    }

    @Test
    fun isQuoted() {
        Assert.assertTrue(q.isQuoted("\"abc"))
        Assert.assertTrue(q.isQuoted("abc\""))
        Assert.assertTrue(q.isQuoted("\"abc\""))
        Assert.assertFalse(q.isQuoted("abc"))
        Assert.assertFalse(q.isQuoted(""))
    }

    // endregion

    // region quoteIfNeeded

    @Test
    fun quoteIfNeeded_plainNeedsQuote() {
        Assert.assertEquals("\"a b\"", q.quoteIfNeeded("a b"))
    }

    @Test
    fun quoteIfNeeded_plainNoQuoteNeeded() {
        Assert.assertEquals("abc", q.quoteIfNeeded("abc"))
    }

    @Test
    fun quoteIfNeeded_alreadyQuoted() {
        Assert.assertEquals("\"abc\"", q.quoteIfNeeded("\"abc\""))
    }

    @Test
    fun quoteIfNeeded_leftQuotedOnly_noQuoteNeeded() {
        // 左侧已加引号且内容无需引号时，保持原样（不补齐右侧引号）
        Assert.assertEquals("\"abc", q.quoteIfNeeded("\"abc"))
    }

    @Test
    fun quoteIfNeeded_innerQuote() {
        Assert.assertEquals("\"a\\\"b\"", q.quoteIfNeeded("a\"b"))
    }

    @Test
    fun quoteIfNeeded_empty() {
        Assert.assertEquals("\"\"", q.quoteIfNeeded(""))
    }

    // endregion

    // region quote

    @Test
    fun quote_plain() {
        Assert.assertEquals("\"abc\"", q.quote("abc"))
    }

    @Test
    fun quote_empty() {
        Assert.assertEquals("\"\"", q.quote(""))
    }

    @Test
    fun quote_singleQuoteChar() {
        Assert.assertEquals("\"\"", q.quote("\""))
    }

    @Test
    fun quote_leftQuotedOnly() {
        Assert.assertEquals("\"abc\"", q.quote("\"abc"))
    }

    @Test
    fun quote_rightQuotedOnly() {
        Assert.assertEquals("\"abc\"", q.quote("abc\""))
    }

    @Test
    fun quote_fullyQuoted() {
        Assert.assertEquals("\"abc\"", q.quote("\"abc\""))
    }

    @Test
    fun quote_innerQuote() {
        Assert.assertEquals("\"a\\\"b\"", q.quote("a\"b"))
    }

    @Test
    fun quote_escapedInnerQuote() {
        // 已转义的引号不应被二次转义
        Assert.assertEquals("\"a\\\"b\"", q.quote("a\\\"b"))
    }

    // endregion

    // region unquote

    @Test
    fun unquote_plain() {
        Assert.assertEquals("abc", q.unquote("abc"))
    }

    @Test
    fun unquote_empty() {
        Assert.assertEquals("", q.unquote(""))
    }

    @Test
    fun unquote_singleQuoteChar() {
        Assert.assertEquals("", q.unquote("\""))
    }

    @Test
    fun unquote_fullyQuoted() {
        Assert.assertEquals("abc", q.unquote("\"abc\""))
    }

    @Test
    fun unquote_leftQuotedOnly() {
        Assert.assertEquals("abc", q.unquote("\"abc"))
    }

    @Test
    fun unquote_rightQuotedOnly() {
        Assert.assertEquals("abc", q.unquote("abc\""))
    }

    @Test
    fun unquote_emptyQuotes() {
        Assert.assertEquals("", q.unquote("\"\""))
    }

    @Test
    fun unquote_escapedInnerQuote() {
        Assert.assertEquals("a\"b", q.unquote("\"a\\\"b\""))
    }

    @Test
    fun unquote_escapedQuote_keptWhenNotQuoted() {
        // 未被引号包围时，不做任何反转义
        Assert.assertEquals("a\\\"b", q.unquote("a\\\"b"))
    }

    // endregion
}
