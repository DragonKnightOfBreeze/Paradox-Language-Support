package icu.windea.pls.localisation.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationQuotePattern
 */
class ParadoxLocalisationQuotePatternTest {
    private val p = QuotePatterns.ParadoxLocalisation

    @Test
    fun metadata() {
        Assert.assertEquals('"', p.quoteChar)
        Assert.assertTrue(p.lenient)
    }

    @Test
    fun needQuote_nonCheck() {
        Assert.assertFalse(p.needQuote("text"))
        Assert.assertFalse(p.needQuote("text\ntext"))
        Assert.assertFalse(p.needQuote("text text"))
        Assert.assertFalse(p.needQuote("text\"text"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("\"text\"", p.quote("text"))
        Assert.assertEquals("text", p.unquote("\"text\""))

        Assert.assertEquals("\"text\ntext\"", p.quote("text\ntext"))
        Assert.assertEquals("text\ntext", p.unquote("\"text\ntext\""))

        Assert.assertEquals("\"text text\"", p.quote("text text"))
        Assert.assertEquals("text text", p.unquote("\"text text\""))
    }
}
