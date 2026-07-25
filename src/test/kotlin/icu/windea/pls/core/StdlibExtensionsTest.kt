package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class StdlibExtensionsTest {
    // region isIdentifierChar

    @Test
    fun isIdentifierChar_basic() {
        Assert.assertTrue('a'.isIdentifierChar())
        Assert.assertTrue('Z'.isIdentifierChar())
        Assert.assertTrue('1'.isIdentifierChar())
        Assert.assertTrue('_'.isIdentifierChar())
    }

    @Test
    fun isIdentifierChar_nonJavaIdentifier() {
        Assert.assertFalse('@'.isIdentifierChar())
        Assert.assertFalse('#'.isIdentifierChar())
        Assert.assertFalse('!'.isIdentifierChar())
        Assert.assertFalse(' '.isIdentifierChar())
        Assert.assertFalse('-'.isIdentifierChar())
        Assert.assertFalse('+'.isIdentifierChar())
        Assert.assertFalse('/'.isIdentifierChar())
        Assert.assertFalse('\\'.isIdentifierChar())
        Assert.assertFalse('\t'.isIdentifierChar())
        Assert.assertFalse('\n'.isIdentifierChar())
    }

    @Test
    fun isIdentifierChar_extraChars() {
        Assert.assertTrue('.'.isIdentifierChar("."))
        Assert.assertTrue('-'.isIdentifierChar("-"))
        Assert.assertTrue('@'.isIdentifierChar("@"))
        Assert.assertTrue('#'.isIdentifierChar("#"))
        // 将非标识符字符放入 extraChars 则视为有效
        Assert.assertTrue('/'.isIdentifierChar("/"))
        Assert.assertTrue(' '.isIdentifierChar(" "))
        // 多个 extraChars
        Assert.assertTrue('.'.isIdentifierChar(".@#"))
        Assert.assertTrue('@'.isIdentifierChar(".@#"))
        Assert.assertTrue('#'.isIdentifierChar(".@#"))
        // 不在 extraChars 中的非标识符字符仍无效
        Assert.assertFalse('!'.isIdentifierChar(".@#"))
    }

    @Test
    fun isIdentifierChar_dollar() {
        // '$' 是合法的 Java 标识符部分（与预期一致）
        Assert.assertTrue('$'.isIdentifierChar())
    }

    @Test
    fun isIdentifierChar_emptyExtraChars() {
        // extraChars 为空时等价于无参数
        Assert.assertTrue('a'.isIdentifierChar(""))
        Assert.assertFalse('@'.isIdentifierChar(""))
    }

    // endregion

    // region isIdentifier

    @Test
    fun isIdentifier_basic() {
        Assert.assertTrue("a".isIdentifier())
        Assert.assertTrue("Z".isIdentifier())
        Assert.assertTrue("1".isIdentifier())
        Assert.assertTrue("_".isIdentifier())
        Assert.assertTrue("abc123".isIdentifier())
        Assert.assertTrue("hello_world".isIdentifier())
    }

    @Test
    fun isIdentifier_empty() {
        Assert.assertFalse("".isIdentifier())
    }

    @Test
    fun isIdentifier_nonIdentifier() {
        Assert.assertFalse("@".isIdentifier())
        Assert.assertFalse("a@b".isIdentifier())
        Assert.assertFalse("a b".isIdentifier())
        Assert.assertFalse("a-b".isIdentifier())
        Assert.assertFalse("hello world".isIdentifier())
        Assert.assertFalse("key=value".isIdentifier())
    }

    @Test
    fun isIdentifier_extraChars() {
        Assert.assertTrue(".".isIdentifier("."))
        Assert.assertTrue("a.b".isIdentifier("."))
        Assert.assertTrue("a-b".isIdentifier("-"))
        Assert.assertTrue("a@b".isIdentifier("@"))
        Assert.assertTrue("a/b".isIdentifier("/"))
        Assert.assertFalse("a!b".isIdentifier("."))
        Assert.assertFalse("a!b".isIdentifier(".-"))
    }

    @Test
    fun isIdentifier_dollar() {
        // 与 isIdentifierChar 一致，'$' 是合法标识符字符
        Assert.assertTrue("$".isIdentifier())
        Assert.assertTrue("\$abc$".isIdentifier())
        Assert.assertTrue("a\$b\$c".isIdentifier())
    }

    @Test
    fun isIdentifier_mixedWithExtraChars() {
        Assert.assertTrue("a.b-c@d".isIdentifier(".-@"))
        Assert.assertFalse("a.b!c".isIdentifier(".-@"))
    }

    // endregion

    @Test
    fun isQuoted_test() {
        Assert.assertFalse("123".isRightQuoted())
        Assert.assertTrue("123\"".isRightQuoted())
        Assert.assertFalse("123\\\"".isRightQuoted())
        Assert.assertTrue("123\\\\\"".isRightQuoted())
        Assert.assertTrue("\\\\\"".isRightQuoted())
    }

    @Test
    fun quoteAndUnquote_test() {
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
    fun isEscapedCharAt_test() {
        Assert.assertFalse("abcd".isEscapedCharAt(3))
        Assert.assertTrue("ab\\d".isEscapedCharAt(3))
        Assert.assertFalse("a\\\\d".isEscapedCharAt(3))
        Assert.assertTrue("\\\\\\d".isEscapedCharAt(3))
    }

    @Test
    fun escapeBlank_test() {
        Assert.assertEquals("abc", "abc".escapeBlank())
        Assert.assertEquals("abc&nbsp;", "abc ".escapeBlank())
        Assert.assertEquals("abc&nbsp;&nbsp;", "abc  ".escapeBlank())
        Assert.assertEquals("&nbsp;abc&nbsp;&nbsp;", " abc  ".escapeBlank())
        Assert.assertEquals("&nbsp;a&nbsp;bc&nbsp;&nbsp;", " a bc  ".escapeBlank())
    }

    @Test
    fun quote_unquote_and_escape_test() {
        val s = "ab\"c"
        val quoted = s.quote()
        val unquoted = quoted.unquote()
        Assert.assertEquals(s, unquoted)

        val s2 = "a\\\"b" // a, \\, ", b
        val i = s2.indexOf('"')
        Assert.assertTrue(s2.isEscapedCharAt(i))

        val s3 = "a b"
        Assert.assertEquals("\"a b\"", s3.quoteIfNeeded())
        Assert.assertEquals(s3, s3.unquote())
    }

    @Test
    fun substringIn_variants_test() {
        Assert.assertEquals("x", "a[x]b".substringIn('[', ']'))
        Assert.assertEquals("a[x]b", "a[x]b".substringIn('<', '>'))
        Assert.assertEquals("foo", "a<foo>b".substringIn("<", ">"))

        Assert.assertEquals("d", "a[b]c[d]y".substringInLast('[', ']'))
        Assert.assertEquals("bar", "a<foo>b<bar>c".substringInLast("<", ">"))
    }

    @Test
    fun split_and_contains_blank_lines_test() {
        Assert.assertEquals(listOf("a", "b", "c"), "a  b\tc".splitByBlank())
        Assert.assertTrue("a b".containsBlank())
        Assert.assertTrue("a\r\nb".containsLineBreak())
        Assert.assertTrue("a\n\nb".containsBlankLine())
    }

    @Test
    fun splitToPair_test() {
        // Assert.assertEquals(listOf("A", "b", "c"), " A, ,b; c ".splitOptimized(',', ';'))
        Assert.assertEquals("a" to "b", "a=b".splitToPair('='))
        Assert.assertNull("a".splitToPair('='))
    }

    @Test
    fun truncate_and_keep_quotes_test() {
        Assert.assertEquals("abc...", "abcdef".truncate(3))
        Assert.assertEquals("\"abc...\"", "\"abcdef\"".truncateAndKeepQuotes(3))
    }

    @Test
    fun capitalization_and_words_test() {
        Assert.assertEquals("Foo", "foo".capitalized())
        Assert.assertEquals("bar", "Bar".decapitalized())
        Assert.assertEquals("Hello world foo bar", "hello_world-FOO.bar".toCapitalizedWords())
    }

    @Test
    fun indicesOf_and_comma_delimited_test() {
        Assert.assertEquals(listOf(0, 2, 4), "ababa".indicesOf('a'))
        Assert.assertEquals("a,b,c", listOf("a", "b", "c").toCommaDelimitedString())
        Assert.assertEquals(listOf("a", "b", "c"), "a,b,, c".toCommaDelimitedStringList())
        Assert.assertEquals(setOf("a", "b", "c"), "a,b,, c".toCommaDelimitedStringSet())
    }

    @Test
    fun indicesOf_string_overloads_test() {
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
    fun matchesPath_basic_and_strict_test() {
        // equal
        Assert.assertTrue("/a/b".matchesPath("/a/b", acceptSelf = true))
        Assert.assertFalse("/a/b".matchesPath("/a/b", acceptSelf = false))

        // parent-child
        Assert.assertTrue("/a".matchesPath("/a/b"))
        Assert.assertTrue("/a/b".matchesPath("/a/b/c"))
        Assert.assertFalse("/a/b/c".matchesPath("/a/b"))

        // strict: only direct parent
        Assert.assertTrue("/a".matchesPath("/a/b", strict = true))
        Assert.assertFalse("/a".matchesPath("/a/b/c", strict = true))
    }

    @Test
    fun matchesPath_trim_test() {
        // when trim=true, only the receiver is trimmed
        Assert.assertTrue("a/b/".matchesPath("a/b/c", trim = true))
        Assert.assertTrue("a/b".matchesPath("a/b/c", trim = false))
        Assert.assertFalse("a/b/".matchesPath("a-b/c", trim = true))
    }

    @Test
    fun normalizePath_unify_separators_and_trim_tail_test() {
        Assert.assertEquals("a/b/c", "a//b\\c/".normalizePath())
        Assert.assertEquals("", "".normalizePath())
        Assert.assertEquals("a", "a////".normalizePath())
    }

    @Test
    fun convertPath_test() {
        Assert.assertEquals("foo/bar.after.txt", "foo/bar.txt".convertPath { b,e -> "$b.after$e" })
        Assert.assertEquals("foo/bar.after.test.txt", "foo/bar.test.txt".convertPath { b,e -> "$b.after$e" })
        Assert.assertEquals("foo/bar.after", "foo/bar".convertPath { b,e -> "$b.after$e" })
        Assert.assertEquals("bar.after", "bar".convertPath { b,e -> "$b.after$e" })
        Assert.assertEquals(".after", "".convertPath { b,e -> "$b.after$e" })
    }

    @Test
    fun regex_and_ant_wrappers_test() {
        Assert.assertTrue("foo/bar".matchesAntPattern("foo/**"))
        Assert.assertTrue("abc".matchesRegex("[a-z]+"))
        Assert.assertFalse("abc".matchesRegex("[0-9]+"))
    }
}
