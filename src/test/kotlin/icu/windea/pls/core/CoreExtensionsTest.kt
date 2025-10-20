package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class CoreExtensionsTest {
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
}
