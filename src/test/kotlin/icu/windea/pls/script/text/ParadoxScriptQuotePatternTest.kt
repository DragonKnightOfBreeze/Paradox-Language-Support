package icu.windea.pls.script.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxScriptQuotePattern
 */
class ParadoxScriptQuotePatternTest {
    private val quotePattern = QuotePatterns.ParadoxScript

    @Test
    fun metadata() {
        Assert.assertEquals('"', quotePattern.quoteChar)
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
        Assert.assertFalse(quotePattern.needQuote("abc.def"))
    }

    @Test
    fun needQuote_whitespace() {
        Assert.assertTrue(quotePattern.needQuote("a b"))
        Assert.assertTrue(quotePattern.needQuote("a\tb"))
    }

    @Test
    fun needQuote_forcedChars() {
        Assert.assertTrue(quotePattern.needQuote("a@b"))
        Assert.assertTrue(quotePattern.needQuote("a#b"))
        Assert.assertTrue(quotePattern.needQuote("a=b"))
        Assert.assertTrue(quotePattern.needQuote("a<b"))
        Assert.assertTrue(quotePattern.needQuote("a>b"))
        Assert.assertTrue(quotePattern.needQuote("a!b"))
        Assert.assertTrue(quotePattern.needQuote("a?b"))
        Assert.assertTrue(quotePattern.needQuote("a{b"))
        Assert.assertTrue(quotePattern.needQuote("a}b"))
        Assert.assertTrue(quotePattern.needQuote("a[b"))
        Assert.assertTrue(quotePattern.needQuote("a\"b"))
    }

    @Test
    fun needQuote_nonForcedChars() {
        Assert.assertFalse(quotePattern.needQuote("a;b"))
        Assert.assertFalse(quotePattern.needQuote("a]b"))
        Assert.assertFalse(quotePattern.needQuote("a:b"))
        Assert.assertFalse(quotePattern.needQuote("a_b"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("\"a b\"", quotePattern.quote("a b"))
        Assert.assertEquals("a b", quotePattern.unquote("\"a b\""))
    }
}
