package icu.windea.pls

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import org.junit.*
import org.junit.Assert.*

class ExtensionsTest1 {
    @Test
    fun matchesGlobPatternTest() {
        assertTrue("abc".matchesPattern("abc"))
        assertTrue("abc".matchesPattern("*"))
        assertTrue("abc".matchesPattern("ab?"))
        assertTrue("abc".matchesPattern("ab*"))
        assertTrue("abc".matchesPattern("a?c"))
        assertFalse("ab".matchesPattern("a?c"))
        assertFalse("abc".matchesPattern("a?"))
        assertTrue("abc".matchesPattern("a*c"))
        assertFalse("abc".matchesPattern("a*b"))
    }

    @Test
    fun escapeBlankTest() {
        assertEquals("abc", "abc".escapeBlank())
        assertEquals("abc&nbsp;", "abc ".escapeBlank())
        assertEquals("abc&nbsp;&nbsp;", "abc  ".escapeBlank())
        assertEquals("&nbsp;abc&nbsp;&nbsp;", " abc  ".escapeBlank())
        assertEquals("&nbsp;a&nbsp;bc&nbsp;&nbsp;", " a bc  ".escapeBlank())
    }

    @Test
    fun quoteAndUnquoteTest() {
        assertEquals("""" abc\"abc """", """ abc"abc """.quote())
        assertEquals("""" abc\"abc """", """ abc\"abc """.quote())
        assertEquals("""" abc\\\"abc """", """ abc\\"abc """.quote())
        assertEquals("""" abc\\\"abc """", """ abc\\\"abc """.quote())

        assertEquals("""" abc"abc """", """" abc"abc """".quote())
        assertEquals("""" abc\"abc """", """" abc\"abc """".quote())
        assertEquals("""" abc\\"abc """", """" abc\\"abc """".quote())

        assertEquals(""" abc"abc """, """" abc"abc """".unquote())
        assertEquals(""" abc"abc """, """" abc\"abc """".unquote())
        assertEquals(""" abc\\"abc """, """" abc\\"abc """".unquote())
        assertEquals(""" abc\\"abc """, """" abc\\\"abc """".unquote())

        assertEquals(""" abc"abc """, """ abc"abc """.unquote())
        assertEquals(""" abc\"abc """, """ abc\"abc """.unquote())
        assertEquals(""" abc\\"abc """, """ abc\\"abc """.unquote())
    }

    @Test
    fun isQuotedTest() {
        assertFalse("123".isRightQuoted())
        assertTrue("123\"".isRightQuoted())
        assertFalse("123\\\"".isRightQuoted())
        assertTrue("123\\\\\"".isRightQuoted())
        assertTrue("\\\\\"".isRightQuoted())
    }

    @Test
    fun isEscapedCharAt() {
        assertFalse("abcd".isEscapedCharAt(3))
        assertTrue("ab\\d".isEscapedCharAt(3))
        assertFalse("a\\\\d".isEscapedCharAt(3))
        assertTrue("\\\\\\d".isEscapedCharAt(3))
    }

    @Test
    fun getTextFragmentsTest() {
        assertEquals(listOf(TextRange.create(0, 3) to "abc"), "abc".getTextFragments(0))
        assertEquals(listOf(TextRange.create(2, 5) to "abc"), "abc".getTextFragments(2))

        assertEquals(listOf(TextRange.create(0, 3) to "abc", TextRange.create(4, 8) to "\"def"), "abc\\\"def".getTextFragments(0))
        assertEquals(listOf(TextRange.create(2, 5) to "abc", TextRange.create(6, 10) to "\"def"), "abc\\\"def".getTextFragments(2))

        assertEquals(listOf(TextRange.create(0, 3) to "abc", TextRange.create(4, 5) to "\\", TextRange.create(6, 10) to "\"def"), "abc\\\\\\\"def".getTextFragments(0))
        assertEquals(listOf(TextRange.create(2, 5) to "abc", TextRange.create(6, 7) to "\\", TextRange.create(8, 12) to "\"def"), "abc\\\\\\\"def".getTextFragments(2))
    }

    @Test
    fun replaceAndQuoteIfNecessaryTest() {
        assertEquals("def", TextRange.create(0, 3).replaceAndQuoteIfNecessary("abc", "def"))
        assertEquals("\"e\"", TextRange.create(0, 3).replaceAndQuoteIfNecessary("\"b\"", "\"e\""))
        assertEquals("\"dec\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "de"))
        assertEquals("\"d\\\"c\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "d\""))
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
            assertEquals(expected, actual)
        }
        run {
            val expected = listOf(TextRange.create(0, 3) to "foo", TextRange.create(4, 7) to "bar")
            val actual = "foo.bar.suffix".findKeywordsWithRanges(listOf("foo", "bar"))
            assertEquals(expected, actual)
        }
        run {
            val expected = listOf(TextRange.create(0, 3) to "foo", TextRange.create(4, 7) to "bar", TextRange.create(8, 14) to "barbar")
            val actual = "foo.bar.barbar".findKeywordsWithRanges(listOf("foo", "barbar", "bar"))
            assertEquals(expected, actual)
        }
    }
}
