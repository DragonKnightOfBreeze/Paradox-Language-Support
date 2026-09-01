package icu.windea.pls.core

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.text.EscapePatterns
import org.junit.Assert
import org.junit.Test

class TextExtensionsTest {
    // region QuotePattern 委托

    @Test
    fun quotePattern_delegation_smokeTest() {
        Assert.assertTrue("a b".needQuote())
        Assert.assertFalse("abc".needQuote())
        Assert.assertTrue("\"abc\"".isQuoted())
        Assert.assertTrue("\"abc".isLeftQuoted())
        Assert.assertTrue("abc\"".isRightQuoted())
        Assert.assertEquals("\"a b\"", "a b".quote())
        Assert.assertEquals("a b", "\"a b\"".unquote())
        Assert.assertEquals("\"a b\"", "a b".quoteIfNeeded())
    }

    // endregion

    // region EscapePattern 委托

    @Test
    fun escapePattern_delegation_smokeTest() {
        Assert.assertEquals("a\\nb", "a\nb".escape(EscapePatterns.Default))
        Assert.assertEquals("a\nb", "a\\nb".unescape(EscapePatterns.Default))
        Assert.assertEquals("<br>\n", "\n".escape(EscapePatterns.HtmlLineBreak))
    }

    // endregion

    // region String.transformAndKeepQuotes

    @Test
    fun string_transformAndKeepQuotes_smokeTest() {
        Assert.assertEquals("", "".transformAndKeepQuotes { it.truncate(0) })
        Assert.assertEquals("", "".transformAndKeepQuotes { it.truncate(3) })

        Assert.assertEquals("abcdef", "abcdef".transformAndKeepQuotes { it.truncate(0) })
        Assert.assertEquals("\"abcdef", "\"abcdef".transformAndKeepQuotes { it.truncate(0) })
        Assert.assertEquals("abcdef\"", "abcdef\"".transformAndKeepQuotes { it.truncate(0) })
        Assert.assertEquals("\"abcdef\"", "\"abcdef\"".transformAndKeepQuotes { it.truncate(0) })

        Assert.assertEquals("abc...", "abcdef".transformAndKeepQuotes { it.truncate(3) })
        Assert.assertEquals("\"abc...", "\"abcdef".transformAndKeepQuotes { it.truncate(3) })
        Assert.assertEquals("abc...\"", "abcdef\"".transformAndKeepQuotes { it.truncate(3) })
        Assert.assertEquals("\"abc...\"", "\"abcdef\"".transformAndKeepQuotes { it.truncate(3) })

        Assert.assertEquals("abcdef", "abcdef".transformAndKeepQuotes { it.truncate(6) })
        Assert.assertEquals("\"abcdef", "\"abcdef".transformAndKeepQuotes { it.truncate(6) })
        Assert.assertEquals("abcdef\"", "abcdef\"".transformAndKeepQuotes { it.truncate(6) })
        Assert.assertEquals("\"abcdef\"", "\"abcdef\"".transformAndKeepQuotes { it.truncate(6) })

        Assert.assertEquals("abcdef", "abcdef".transformAndKeepQuotes { it.truncate(9) })
        Assert.assertEquals("\"abcdef", "\"abcdef".transformAndKeepQuotes { it.truncate(9) })
        Assert.assertEquals("abcdef\"", "abcdef\"".transformAndKeepQuotes { it.truncate(9) })
        Assert.assertEquals("\"abcdef\"", "\"abcdef\"".transformAndKeepQuotes { it.truncate(9) })
    }

    // endregion


    // region TextRange.unquote

    @Test
    fun textRange_unquote_smokeTest() {
        Assert.assertEquals(TextRange.create(1, 4), TextRange.create(1, 4).unquote("abc"))
        Assert.assertEquals(TextRange.create(2, 6), TextRange.create(1, 7).unquote("\"abc\""))
        Assert.assertEquals(TextRange.EMPTY_RANGE, TextRange.create(1, 7).unquote(""))
    }

    // endregion
}
