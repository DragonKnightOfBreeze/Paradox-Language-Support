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

    // NOTE 3.0.2 for `needQuote`, need to check boundary characters specially due to low-level parser implementation

    @Test
    fun needQuote_plain() {
        Assert.assertFalse(quotePattern.needQuote("abc"))
        // middle
        Assert.assertFalse(quotePattern.needQuote("abc_def"))
        Assert.assertFalse(quotePattern.needQuote("abc.def"))
        // leading
        Assert.assertFalse(quotePattern.needQuote("_abc"))
        Assert.assertFalse(quotePattern.needQuote(".abc"))
        // tailing
        Assert.assertFalse(quotePattern.needQuote("abc_"))
        Assert.assertFalse(quotePattern.needQuote("abc."))
    }

    @Test
    fun needQuote_whitespaces_notChecked() {
        // middle
        Assert.assertFalse(quotePattern.needQuote("a b"))
        Assert.assertFalse(quotePattern.needQuote("a\tb"))
        // leading
        Assert.assertFalse(quotePattern.needQuote(" a"))
        Assert.assertFalse(quotePattern.needQuote("\ta"))
        // tailing
        Assert.assertFalse(quotePattern.needQuote("a "))
        Assert.assertFalse(quotePattern.needQuote("a\t"))
    }

    @Test
    fun needQuote_quoteChar_notChecked() {
        // middle
        Assert.assertFalse(quotePattern.needQuote("a\"b"))
        // leading
        Assert.assertFalse(quotePattern.needQuote("\"a"))
        // tailing
        Assert.assertFalse(quotePattern.needQuote("a\""))
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
