package icu.windea.pls.csv.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxCsvQuotePattern
 */
class ParadoxCsvQuotePatternTest {
    private val p = QuotePatterns.ParadoxCsv

    @Test
    fun metadata() {
        Assert.assertEquals('"', p.quoteChar)
        Assert.assertTrue(p.lenient)
    }

    @Test
    fun needQuote_plain() {
        Assert.assertFalse(p.needQuote("abc"))
    }

    @Test
    fun needQuote_whitespaceAllowed() {
        Assert.assertFalse(p.needQuote("a b"))
        Assert.assertFalse(p.needQuote("a\tb"))
    }

    @Test
    fun needQuote_forcedChars() {
        Assert.assertTrue(p.needQuote("a#b"))
        Assert.assertTrue(p.needQuote("a;b"))
        Assert.assertTrue(p.needQuote("a\"b"))
    }

    @Test
    fun needQuote_nonForcedChars() {
        Assert.assertFalse(p.needQuote("a=b"))
        Assert.assertFalse(p.needQuote("a@b"))
        Assert.assertFalse(p.needQuote("a{b"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("\"a#b\"", p.quote("a#b"))
        Assert.assertEquals("a#b", p.unquote("\"a#b\""))
    }
}
