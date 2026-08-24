package icu.windea.pls.expression.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLiteralNodeQuotePattern
 */
class ParadoxLiteralNodeQuotePatternTest {
    private val p = QuotePatterns.ParadoxLiteralNode

    @Test
    fun metadata() {
        Assert.assertEquals('\'', p.quoteChar)
        Assert.assertTrue(p.lenient)
    }

    @Test
    fun needQuote_plain() {
        Assert.assertFalse(p.needQuote("abc"))
    }

    @Test
    fun needQuote_identifierChars() {
        Assert.assertFalse(p.needQuote("a_b"))
        Assert.assertFalse(p.needQuote("a1"))
        Assert.assertFalse(p.needQuote("a\$b"))
    }

    @Test
    fun needQuote_whitespace() {
        Assert.assertTrue(p.needQuote("a b"))
        Assert.assertTrue(p.needQuote("a\tb"))
    }

    @Test
    fun needQuote_nonIdentifierChars() {
        Assert.assertTrue(p.needQuote("a-b"))
        Assert.assertTrue(p.needQuote("a.b"))
        Assert.assertTrue(p.needQuote("a+b"))
        Assert.assertTrue(p.needQuote("a@b"))
        Assert.assertTrue(p.needQuote("a'b"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("'a b'", p.quote("a b"))
        Assert.assertEquals("a b", p.unquote("'a b'"))
    }
}
