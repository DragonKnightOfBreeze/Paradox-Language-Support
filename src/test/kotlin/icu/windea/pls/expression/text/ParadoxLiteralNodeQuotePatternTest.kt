package icu.windea.pls.expression.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLiteralNodeQuotePattern
 */
class ParadoxLiteralNodeQuotePatternTest {
    private val quotePattern = QuotePatterns.ParadoxLiteralNode

    @Test
    fun metadata() {
        Assert.assertEquals('\'', quotePattern.quoteChar)
        Assert.assertTrue(quotePattern.lenient)
    }

    @Test
    fun needQuote_empty_or_singleQuoteChar() {
        Assert.assertTrue(quotePattern.needQuote(""))
        Assert.assertTrue(quotePattern.needQuote("\""))
    }

    @Test
    fun needQuote_plain() {
        Assert.assertFalse(quotePattern.needQuote("abc"))
        Assert.assertFalse(quotePattern.needQuote("abc_def"))
        Assert.assertTrue(quotePattern.needQuote("abc.def")) // need quote here
    }


    @Test
    fun needQuote_identifierChars() {
        Assert.assertFalse(quotePattern.needQuote("a_b"))
        Assert.assertFalse(quotePattern.needQuote("a1"))
        Assert.assertFalse(quotePattern.needQuote("a\$b"))
    }

    @Test
    fun needQuote_whitespace() {
        Assert.assertTrue(quotePattern.needQuote("a b"))
        Assert.assertTrue(quotePattern.needQuote("a\tb"))
    }

    @Test
    fun needQuote_nonIdentifierChars() {
        Assert.assertTrue(quotePattern.needQuote("a-b"))
        Assert.assertTrue(quotePattern.needQuote("a.b"))
        Assert.assertTrue(quotePattern.needQuote("a+b"))
        Assert.assertTrue(quotePattern.needQuote("a@b"))
        Assert.assertTrue(quotePattern.needQuote("a'b"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("'a b'", quotePattern.quote("a b"))
        Assert.assertEquals("a b", quotePattern.unquote("'a b'"))
    }
}
