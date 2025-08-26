package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class StdlibExtensionsPureTest {
    @Test
    fun quote_unquote_and_escape() {
        val s = "ab\"c"
        val quoted = s.quote()
        val unquoted = quoted.unquote()
        Assert.assertEquals(s, unquoted)

        val s2 = "a\\\"b" // a, \\, ", b
        val i = s2.indexOf('"')
        Assert.assertTrue(s2.isEscapedCharAt(i))

        val s3 = "a b"
        Assert.assertEquals("\"a b\"", s3.quoteIfNecessary())
        Assert.assertEquals(s3, s3.unquote())
    }

    @Test
    fun substringIn_variants() {
        Assert.assertEquals("x", "a[x]b".substringIn('[', ']'))
        Assert.assertEquals("a[x]b", "a[x]b".substringIn('<', '>'))
        Assert.assertEquals("foo", "a<foo>b".substringIn("<", ">"))

        Assert.assertEquals("d", "a[b]c[d]y".substringInLast('[', ']'))
        Assert.assertEquals("bar", "a<foo>b<bar>c".substringInLast("<", ">"))
    }

    @Test
    fun split_and_contains_blank_lines() {
        Assert.assertEquals(listOf("a", "b", "c"), "a  b\tc".splitByBlank())
        Assert.assertTrue("a b".containsBlank())
        Assert.assertTrue("a\r\nb".containsLineBreak())
        Assert.assertTrue("a\n\nb".containsBlankLine())
    }

    @Test
    fun splitOptimized_and_splitToPair() {
        Assert.assertEquals(listOf("A", "b", "c"), " A, ,b; c ".splitOptimized(',', ';'))
        Assert.assertEquals("a" to "b", "a=b".splitToPair('='))
        Assert.assertNull("a".splitToPair('='))
    }

    @Test
    fun truncate_and_keep_quotes() {
        Assert.assertEquals("abc...", "abcdef".truncate(3))
        Assert.assertEquals("\"abc...\"", "\"abcdef\"".truncateAndKeepQuotes(3))
    }

    @Test
    fun capitalization_and_words() {
        Assert.assertEquals("Foo", "foo".capitalized())
        Assert.assertEquals("bar", "Bar".decapitalized())
        Assert.assertEquals("Hello world foo bar", "hello_world-FOO.bar".toCapitalizedWords())
    }

    @Test
    fun indicesOf_and_comma_delimited() {
        Assert.assertEquals(listOf(0, 2, 4), "ababa".indicesOf('a'))
        Assert.assertEquals("a,b,c", listOf("a", "b", "c").toCommaDelimitedString())
        Assert.assertEquals(listOf("a", "b", "c"), "a,b,, c".toCommaDelimitedStringList())
        Assert.assertEquals(setOf("a", "b", "c"), "a,b,, c".toCommaDelimitedStringSet())
    }

    @Test
    fun indicesOf_string_overloads() {
        // String overload, overlapping matches supported
        Assert.assertEquals(listOf(0, 2), "ababa".indicesOf("aba"))
        // ignoreCase
        Assert.assertEquals(listOf(0, 2), "AbAba".indicesOf("aba", ignoreCase = true))
        // limit
        Assert.assertEquals(listOf(0), "aaaa".indicesOf("aa", limit = 1))
        // startIndex
        Assert.assertEquals(listOf(2), "ababa".indicesOf("aba", startIndex = 1))
        // char overload with limit
        Assert.assertEquals(listOf(0, 1), "aaaa".indicesOf('a', limit = 2))
    }

    @Test
    fun pattern_ant_and_regex() {
        Assert.assertTrue("foo.txt".matchesPattern("*.txt"))
        Assert.assertTrue("bar".matchesPattern("?ar"))

        Assert.assertTrue("foo/bar/baz".matchesAntPattern("foo/**"))
        Assert.assertTrue("foo/bar".matchesAntPattern("foo/*"))
        Assert.assertFalse("foo/bar/baz".matchesAntPattern("foo/*"))

        Assert.assertTrue("abc123".matchesRegex("[a-z]+\\d+"))
        Assert.assertFalse("abc".matchesRegex("[0-9]+"))
    }
}
