package icu.windea.pls.cwt.text

import icu.windea.pls.core.text.QuotePatterns
import org.junit.Assert
import org.junit.Test

/**
 * @see CwtQuotePattern
 */
class CwtQuotePatternTest {
    private val quotePattern = QuotePatterns.Cwt

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

    // NOTE 3.0.2 for `needQuote`, need to check boundary characters specially due to low-level lexer implementation

    @Test
    fun needQuote_plain() {
        Assert.assertFalse(quotePattern.needQuote("abc"))
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
    fun needQuote_whitespaces() {
        // middle
        Assert.assertTrue(quotePattern.needQuote("a b"))
        Assert.assertTrue(quotePattern.needQuote("a\tb"))
        // leading
        Assert.assertTrue(quotePattern.needQuote(" a"))
        Assert.assertTrue(quotePattern.needQuote("\ta"))
        // tailing
        Assert.assertTrue(quotePattern.needQuote("a "))
        Assert.assertTrue(quotePattern.needQuote("a\t"))
    }

    @Test
    fun needQuote_quoteChar() {
        // middle
        Assert.assertTrue(quotePattern.needQuote("a\"b"))
        // leading
        Assert.assertFalse(quotePattern.needQuote("\"a"))
        // tailing
        Assert.assertFalse(quotePattern.needQuote("a\""))
    }

    @Test
    fun needQuote_specialChars() {
        // middle
        Assert.assertFalse(quotePattern.needQuote("a_b"))
        Assert.assertTrue(quotePattern.needQuote("a#b"))
        Assert.assertTrue(quotePattern.needQuote("a=b"))
        Assert.assertTrue(quotePattern.needQuote("a{b"))
        Assert.assertTrue(quotePattern.needQuote("a}b"))
        Assert.assertFalse(quotePattern.needQuote("a[b"))
        Assert.assertFalse(quotePattern.needQuote("a]b"))
        Assert.assertFalse(quotePattern.needQuote("a<b"))
        Assert.assertFalse(quotePattern.needQuote("a>b"))
        Assert.assertFalse(quotePattern.needQuote("a!b"))
        Assert.assertFalse(quotePattern.needQuote("a?b"))
        Assert.assertFalse(quotePattern.needQuote("a@b"))
        Assert.assertFalse(quotePattern.needQuote("a;b"))
        Assert.assertFalse(quotePattern.needQuote("a:b"))
        // leading
        Assert.assertFalse(quotePattern.needQuote("_a"))
        Assert.assertTrue(quotePattern.needQuote("#a"))
        Assert.assertTrue(quotePattern.needQuote("=a"))
        Assert.assertTrue(quotePattern.needQuote("{a"))
        Assert.assertTrue(quotePattern.needQuote("}a"))
        Assert.assertFalse(quotePattern.needQuote("[a"))
        Assert.assertFalse(quotePattern.needQuote("]a"))
        Assert.assertFalse(quotePattern.needQuote("<a"))
        Assert.assertFalse(quotePattern.needQuote(">a"))
        Assert.assertTrue(quotePattern.needQuote("!a"))
        Assert.assertTrue(quotePattern.needQuote("?a"))
        Assert.assertFalse(quotePattern.needQuote("@a"))
        Assert.assertFalse(quotePattern.needQuote(";a"))
        Assert.assertFalse(quotePattern.needQuote(":a"))
        // tailing
        Assert.assertFalse(quotePattern.needQuote("a_"))
        Assert.assertTrue(quotePattern.needQuote("a#"))
        Assert.assertTrue(quotePattern.needQuote("a="))
        Assert.assertTrue(quotePattern.needQuote("a{"))
        Assert.assertTrue(quotePattern.needQuote("a}"))
        Assert.assertFalse(quotePattern.needQuote("a["))
        Assert.assertFalse(quotePattern.needQuote("a]"))
        Assert.assertFalse(quotePattern.needQuote("a<"))
        Assert.assertFalse(quotePattern.needQuote("a>"))
        Assert.assertTrue(quotePattern.needQuote("a!"))
        Assert.assertTrue(quotePattern.needQuote("a?"))
        Assert.assertFalse(quotePattern.needQuote("a@"))
        Assert.assertFalse(quotePattern.needQuote("a;"))
        Assert.assertFalse(quotePattern.needQuote("a:"))
    }

    @Test
    fun quoteAndUnquote() {
        Assert.assertEquals("\"a#b\"", quotePattern.quote("a#b"))
        Assert.assertEquals("a#b", quotePattern.unquote("\"a#b\""))
    }
}
