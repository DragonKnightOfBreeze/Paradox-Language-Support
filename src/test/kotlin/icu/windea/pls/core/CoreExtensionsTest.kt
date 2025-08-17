package icu.windea.pls.core

import com.intellij.openapi.util.TextRange
import org.junit.Assert
import org.junit.Test

class CoreExtensionsTest {
    @Test
    fun matchesGlobPatternTest() {
        Assert.assertTrue("abc".matchesPattern("abc"))
        Assert.assertTrue("abc".matchesPattern("*"))
        Assert.assertTrue("abc".matchesPattern("ab?"))
        Assert.assertTrue("abc".matchesPattern("ab*"))
        Assert.assertTrue("abc".matchesPattern("a?c"))
        Assert.assertFalse("ab".matchesPattern("a?c"))
        Assert.assertFalse("abc".matchesPattern("a?"))
        Assert.assertTrue("abc".matchesPattern("a*c"))
        Assert.assertFalse("abc".matchesPattern("a*b"))
    }

    @Test
    fun escapeBlankTest() {
        Assert.assertEquals("abc", "abc".escapeBlank())
        Assert.assertEquals("abc&nbsp;", "abc ".escapeBlank())
        Assert.assertEquals("abc&nbsp;&nbsp;", "abc  ".escapeBlank())
        Assert.assertEquals("&nbsp;abc&nbsp;&nbsp;", " abc  ".escapeBlank())
        Assert.assertEquals("&nbsp;a&nbsp;bc&nbsp;&nbsp;", " a bc  ".escapeBlank())
    }

    @Test
    fun quoteAndUnquoteTest() {
        Assert.assertEquals("""" abc\"abc """", """ abc"abc """.quote())
        Assert.assertEquals("""" abc\"abc """", """ abc\"abc """.quote())
        Assert.assertEquals("""" abc\\\"abc """", """ abc\\"abc """.quote())
        Assert.assertEquals("""" abc\\\"abc """", """ abc\\\"abc """.quote())

        Assert.assertEquals("""" abc"abc """", """" abc"abc """".quote())
        Assert.assertEquals("""" abc\"abc """", """" abc\"abc """".quote())
        Assert.assertEquals("""" abc\\"abc """", """" abc\\"abc """".quote())

        Assert.assertEquals(""" abc"abc """, """" abc"abc """".unquote())
        Assert.assertEquals(""" abc"abc """, """" abc\"abc """".unquote())
        Assert.assertEquals(""" abc\\"abc """, """" abc\\"abc """".unquote())
        Assert.assertEquals(""" abc\\"abc """, """" abc\\\"abc """".unquote())

        Assert.assertEquals(""" abc"abc """, """ abc"abc """.unquote())
        Assert.assertEquals(""" abc\"abc """, """ abc\"abc """.unquote())
        Assert.assertEquals(""" abc\\"abc """, """ abc\\"abc """.unquote())
    }

    @Test
    fun isQuotedTest() {
        Assert.assertFalse("123".isRightQuoted())
        Assert.assertTrue("123\"".isRightQuoted())
        Assert.assertFalse("123\\\"".isRightQuoted())
        Assert.assertTrue("123\\\\\"".isRightQuoted())
        Assert.assertTrue("\\\\\"".isRightQuoted())
    }

    @Test
    fun isEscapedCharAt() {
        Assert.assertFalse("abcd".isEscapedCharAt(3))
        Assert.assertTrue("ab\\d".isEscapedCharAt(3))
        Assert.assertFalse("a\\\\d".isEscapedCharAt(3))
        Assert.assertTrue("\\\\\\d".isEscapedCharAt(3))
    }

    @Test
    fun getTextFragmentsTest() {
        Assert.assertEquals(listOf(TextRange.create(0, 3) to "abc"), "abc".getTextFragments(0))
        Assert.assertEquals(listOf(TextRange.create(2, 5) to "abc"), "abc".getTextFragments(2))

        Assert.assertEquals(listOf(TextRange.create(0, 3) to "abc", TextRange.create(4, 8) to "\"def"), "abc\\\"def".getTextFragments(0))
        Assert.assertEquals(listOf(TextRange.create(2, 5) to "abc", TextRange.create(6, 10) to "\"def"), "abc\\\"def".getTextFragments(2))

        Assert.assertEquals(listOf(TextRange.create(0, 3) to "abc", TextRange.create(4, 5) to "\\", TextRange.create(6, 10) to "\"def"), "abc\\\\\\\"def".getTextFragments(0))
        Assert.assertEquals(listOf(TextRange.create(2, 5) to "abc", TextRange.create(6, 7) to "\\", TextRange.create(8, 12) to "\"def"), "abc\\\\\\\"def".getTextFragments(2))
    }

    @Test
    fun replaceAndQuoteIfNecessaryTest() {
        Assert.assertEquals("def", TextRange.create(0, 3).replaceAndQuoteIfNecessary("abc", "def"))
        Assert.assertEquals("\"e\"", TextRange.create(0, 3).replaceAndQuoteIfNecessary("\"b\"", "\"e\""))
        Assert.assertEquals("\"dec\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "de"))
        Assert.assertEquals("\"d\\\"c\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "d\""))
    }

    @Test
    fun getTextFragmentsTest1() {
        val s = """###\\\\\\\\
custom_tooltip = {}"""
        println(s.getTextFragments(0))
    }

    @Test
    fun findKeywordsWithRangesTest() {
        run {
            val expected = listOf(TextRange.create(0, 3) to "foo")
            val actual = "foo.bar.suffix".findKeywordsWithRanges(listOf("foo"))
            Assert.assertEquals(expected, actual)
        }
        run {
            val expected = listOf(TextRange.create(0, 3) to "foo", TextRange.create(4, 7) to "bar")
            val actual = "foo.bar.suffix".findKeywordsWithRanges(listOf("foo", "bar"))
            Assert.assertEquals(expected, actual)
        }
        run {
            val expected = listOf(TextRange.create(0, 3) to "foo", TextRange.create(4, 7) to "bar", TextRange.create(8, 14) to "barbar")
            val actual = "foo.bar.barbar".findKeywordsWithRanges(listOf("foo", "barbar", "bar"))
            Assert.assertEquals(expected, actual)
        }
    }
}
