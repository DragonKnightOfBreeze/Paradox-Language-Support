package icu.windea.pls.script.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxScriptQuotePattern
 */
class ParadoxScriptQuotePatternTest {
    private val p = QuotePatterns.ParadoxScript

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
    fun needQuote_whitespace() {
        Assert.assertTrue(p.needQuote("a b"))
        Assert.assertTrue(p.needQuote("a\tb"))
    }

    @Test
    fun needQuote_forcedChars() {
        Assert.assertTrue(p.needQuote("a@b"))
        Assert.assertTrue(p.needQuote("a#b"))
        Assert.assertTrue(p.needQuote("a=b"))
        Assert.assertTrue(p.needQuote("a<b"))
        Assert.assertTrue(p.needQuote("a>b"))
        Assert.assertTrue(p.needQuote("a!b"))
        Assert.assertTrue(p.needQuote("a?b"))
        Assert.assertTrue(p.needQuote("a{b"))
        Assert.assertTrue(p.needQuote("a}b"))
        Assert.assertTrue(p.needQuote("a[b"))
        Assert.assertTrue(p.needQuote("a\"b"))
    }

    @Test
    fun needQuote_nonForcedChars() {
        Assert.assertFalse(p.needQuote("a;b"))
        Assert.assertFalse(p.needQuote("a]b"))
        Assert.assertFalse(p.needQuote("a:b"))
        Assert.assertFalse(p.needQuote("a_b"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("\"a b\"", p.quote("a b"))
        Assert.assertEquals("a b", p.unquote("\"a b\""))
    }
}
