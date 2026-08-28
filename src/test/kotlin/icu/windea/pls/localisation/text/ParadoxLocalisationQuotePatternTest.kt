package icu.windea.pls.localisation.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationQuotePattern
 */
class ParadoxLocalisationQuotePatternTest {
    private val quotePattern = QuotePatterns.ParadoxLocalisation

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
    fun needQuote_noCheck() {
        Assert.assertFalse(quotePattern.needQuote("text\ntext"))
        Assert.assertFalse(quotePattern.needQuote("text text"))
        Assert.assertFalse(quotePattern.needQuote("text\"text"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("\"abc\"", quotePattern.quote("abc"))
        Assert.assertEquals("abc", quotePattern.unquote("\"abc\""))
        Assert.assertEquals("\"abc_def\"", quotePattern.quote("abc_def"))
        Assert.assertEquals("abc_def", quotePattern.unquote("\"abc_def\""))
        Assert.assertEquals("\"abc.def\"", quotePattern.quote("abc.def"))
        Assert.assertEquals("abc.def", quotePattern.unquote("\"abc.def\""))

        Assert.assertEquals("\"text\ntext\"", quotePattern.quote("text\ntext"))
        Assert.assertEquals("text\ntext", quotePattern.unquote("\"text\ntext\""))
        Assert.assertEquals("\"text text\"", quotePattern.quote("text text"))
        Assert.assertEquals("text text", quotePattern.unquote("\"text text\""))
        Assert.assertEquals("\"text\\\"text\"", quotePattern.quote("text\"text"))
        Assert.assertEquals("text\"text", quotePattern.unquote("\"text\\\"text\""))
        Assert.assertEquals("text\"text", quotePattern.unquote("\"text\"text\""))
    }
}
