package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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
    fun isEscapedCharAt_test() {
        Assert.assertFalse("abcd".isEscapedCharAt(3))
        Assert.assertTrue("ab\\d".isEscapedCharAt(3))
        Assert.assertFalse("a\\\\d".isEscapedCharAt(3))
        Assert.assertTrue("\\\\\\d".isEscapedCharAt(3))
    }

    // @Test
    // fun escapeBlank_test() {
    //     Assert.assertEquals("abc", "abc".escapeBlank())
    //     Assert.assertEquals("abc&nbsp;", "abc ".escapeBlank())
    //     Assert.assertEquals("abc&nbsp;&nbsp;", "abc  ".escapeBlank())
    //     Assert.assertEquals("&nbsp;abc&nbsp;&nbsp;", " abc  ".escapeBlank())
    //     Assert.assertEquals("&nbsp;a&nbsp;bc&nbsp;&nbsp;", " a bc  ".escapeBlank())
    // }

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
    fun indicesOf_test() {
        Assert.assertEquals(listOf(0, 2, 4), "ababa".indicesOf('a'))
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
    fun normalizePath_unify_separators_and_trim_tail_test() {
        Assert.assertEquals("a/b/c", "a//b\\c/".normalizePath())
        Assert.assertEquals("", "".normalizePath())
        Assert.assertEquals("a", "a////".normalizePath())
    }

    @Test
    fun convertPath_test() {
        Assert.assertEquals("foo/bar", "foo/bar.txt".convertPath(greedyExtension = false) { b, _ -> b })
        Assert.assertEquals("foo/.txt", "foo/bar.txt".convertPath(greedyExtension = false) { _, e -> e })
        Assert.assertEquals("foo/bar.txt", "foo/bar.txt.bak".convertPath(greedyExtension = false) { b, _ -> b })
        Assert.assertEquals("foo/.bak", "foo/bar.txt.bak".convertPath(greedyExtension = false) { _, e -> e })
        Assert.assertEquals("foo/bar.after", "foo/bar".convertPath(greedyExtension = false) { b, e -> "$b.after$e" })
        Assert.assertEquals("foo/bar.test.after.txt", "foo/bar.test.txt".convertPath(greedyExtension = false) { b, e -> "$b.after$e" })
        Assert.assertEquals(".after", "".convertPath(greedyExtension = false) { b, e -> "$b.after$e" })
        Assert.assertEquals("bar.after", "bar".convertPath(greedyExtension = false) { b, e -> "$b.after$e" })

        Assert.assertEquals("foo/bar", "foo/bar.txt".convertPath(greedyExtension = true) { b, _ -> b })
        Assert.assertEquals("foo/.txt", "foo/bar.txt".convertPath(greedyExtension = true) { _, e -> e })
        Assert.assertEquals("foo/bar", "foo/bar.txt.bak".convertPath(greedyExtension = true) { b, _ -> b })
        Assert.assertEquals("foo/.txt.bak", "foo/bar.txt.bak".convertPath(greedyExtension = true) { _, e -> e })
        Assert.assertEquals("foo/bar.after", "foo/bar".convertPath(greedyExtension = true) { b, e -> "$b.after$e" })
        Assert.assertEquals("foo/bar.after.test.txt", "foo/bar.test.txt".convertPath(greedyExtension = true) { b, e -> "$b.after$e" })
        Assert.assertEquals(".after", "".convertPath(greedyExtension = true) { b, e -> "$b.after$e" })
        Assert.assertEquals("bar.after", "bar".convertPath(greedyExtension = true) { b, e -> "$b.after$e" })
    }

    @Test
    fun regex_and_ant_wrappers_test() {
        Assert.assertTrue("foo/bar".matchesAntPattern("foo/**"))
        Assert.assertTrue("abc".matchesRegex("[a-z]+"))
        Assert.assertFalse("abc".matchesRegex("[0-9]+"))
    }

    // region letIf / letUnless / alsoIf / alsoUnless

    @Test
    fun letIf_letUnless_test() {
        Assert.assertEquals(2, 1.letIf(true) { it + 1 })
        Assert.assertEquals(1, 1.letIf(false) { it + 1 })
        Assert.assertEquals(2, 1.letUnless(false) { it + 1 })
        Assert.assertEquals(1, 1.letUnless(true) { it + 1 })
    }

    @Test
    fun alsoIf_alsoUnless_test() {
        var count = 0
        Assert.assertEquals("a", "a".alsoIf(true) { count++ })
        Assert.assertEquals(1, count)
        "a".alsoIf(false) { count++ }
        Assert.assertEquals(1, count)
        "a".alsoUnless(false) { count++ }
        Assert.assertEquals(2, count)
        "a".alsoUnless(true) { count++ }
        Assert.assertEquals(2, count)
    }

    // endregion

    // region isNotNullOrEmpty

    @Test
    fun isNotNullOrEmpty_test() {
        val s: String? = "abc"
        Assert.assertTrue(s.isNotNullOrEmpty())
        Assert.assertFalse("".isNotNullOrEmpty())
        val sn: String? = null
        Assert.assertFalse(sn.isNotNullOrEmpty())

        val a: Array<Int>? = arrayOf(1, 2)
        Assert.assertTrue(a.isNotNullOrEmpty())
        Assert.assertFalse(emptyArray<Int>().isNotNullOrEmpty())
        val an: Array<Int>? = null
        Assert.assertFalse(an.isNotNullOrEmpty())

        val c: List<Int>? = listOf(1)
        Assert.assertTrue(c.isNotNullOrEmpty())
        Assert.assertFalse(emptyList<Int>().isNotNullOrEmpty())
        val cn: List<Int>? = null
        Assert.assertFalse(cn.isNotNullOrEmpty())
    }

    // endregion

    // region orNull

    @Test
    fun orNull_test() {
        Assert.assertEquals(true, true.orNull())
        Assert.assertNull(false.orNull())
        Assert.assertEquals("abc", "abc".orNull())
        Assert.assertNull("".orNull())
    }

    // endregion

    // region surroundsWith

    @Test
    fun surroundsWith_char_test() {
        Assert.assertTrue("[]".surroundsWith('[', ']'))
        Assert.assertFalse("[".surroundsWith('[', ']')) // 长度不足
        Assert.assertFalse("[]".surroundsWith('(', ')'))
        Assert.assertFalse("[x)".surroundsWith('[', ']'))
        Assert.assertTrue("[X]".surroundsWith('[', ']', ignoreCase = true))
    }

    @Test
    fun surroundsWith_string_test() {
        Assert.assertTrue("<<abc>>".surroundsWith("<<", ">>"))
        Assert.assertFalse("<abc>".surroundsWith("<<", ">>")) // 前缀长度不足
        Assert.assertFalse("<<abc>>".surroundsWith("<<", "}}")) // 后缀不匹配
        Assert.assertTrue("[[abc]]".surroundsWith("[[", "]]", ignoreCase = true))
    }

    // endregion

    // region add / remove prefix / suffix / surrounding

    @Test
    fun add_prefix_suffix_surrounding_test() {
        Assert.assertEquals("preabc", "abc".addPrefix("pre"))
        Assert.assertEquals("abcsuf", "abc".addSuffix("suf"))
        Assert.assertEquals("preabcsuf", "abc".addSurrounding("pre", "suf"))
    }

    @Test
    fun removeSurrounding_test() {
        Assert.assertEquals("b", "abc".removeSurrounding("a", "c"))
        // 与 OrNull 版本不同，缺失前后缀时保持原样
        Assert.assertEquals("abc", "abc".removeSurrounding("x", "y"))
        Assert.assertEquals("bc", "abc".removeSurrounding("a", "y"))
    }

    @Test
    fun removePrefixOrNull_test() {
        Assert.assertEquals("bc", "abc".removePrefixOrNull("a"))
        Assert.assertNull("abc".removePrefixOrNull("x"))
        Assert.assertEquals("BC", "ABC".removePrefixOrNull("a", ignoreCase = true))
        Assert.assertEquals("", "a".removePrefixOrNull("a"))
    }

    @Test
    fun removeSuffixOrNull_test() {
        Assert.assertEquals("ab", "abc".removeSuffixOrNull("c"))
        Assert.assertNull("abc".removeSuffixOrNull("x"))
        Assert.assertEquals("AB", "ABC".removeSuffixOrNull("c", ignoreCase = true))
        Assert.assertEquals("", "a".removeSuffixOrNull("a"))
    }

    @Test
    fun removeSurroundingOrNull_test() {
        Assert.assertEquals("b", "abc".removeSurroundingOrNull("a", "c"))
        Assert.assertNull("abc".removeSurroundingOrNull("x", "c"))
        Assert.assertNull("abc".removeSurroundingOrNull("a", "x"))
        Assert.assertEquals("B", "aBc".removeSurroundingOrNull("a", "c", ignoreCase = true))
    }

    // endregion

    // region exact char checks

    @Test
    fun exactCharChecks_test() {
        Assert.assertTrue('\n'.isExactLineBreak())
        Assert.assertTrue('\r'.isExactLineBreak())
        Assert.assertFalse('a'.isExactLineBreak())

        Assert.assertTrue('a'.isExactLetter())
        Assert.assertTrue('Z'.isExactLetter())
        Assert.assertFalse('1'.isExactLetter())

        Assert.assertTrue('1'.isExactDigit())
        Assert.assertFalse('a'.isExactDigit())

        Assert.assertTrue('a'.isExactWord())
        Assert.assertTrue('1'.isExactWord())
        Assert.assertTrue('_'.isExactWord())
        Assert.assertFalse('-'.isExactWord())
    }

    // endregion

    // region escapeXml

    @Test
    fun escapeXml_test() {
        Assert.assertEquals("abc", "abc".escapeXml())
        Assert.assertEquals("", "".escapeXml())
        Assert.assertEquals("&lt;&gt;&amp;&quot;", "<>&\"".escapeXml())
        Assert.assertEquals("&lt;&gt;&amp;&quot;&#39;", "<>&\"'".escapeXml()) // 单引号转义为 &#39;
    }

    // endregion

    // region truncate

    @Test
    fun truncate_edges_test() {
        Assert.assertEquals("abcdef", "abcdef".truncate(6))
        Assert.assertEquals("abcdef", "abcdef".truncate(100))
        Assert.assertEquals("abcdef", "abcdef".truncate(0)) // limit <= 0 不截断
        Assert.assertEquals("abcdef", "abcdef".truncate(-1))
        Assert.assertEquals("", "".truncate(3))
        Assert.assertEquals("ab..", "abcdef".truncate(2, ellipsis = ".."))
    }

    // endregion

    // region indexOf / lastIndexOf (predicate)

    @Test
    fun indexOf_predicate_test() {
        Assert.assertEquals(2, "abcde".indexOf(0) { it == 'c' })
        Assert.assertEquals(4, "abcde".indexOf(3) { it == 'e' })
        Assert.assertEquals(-1, "abcde".indexOf(0) { it == 'z' })
        Assert.assertEquals(4, "abcde".lastIndexOf(4) { it == 'e' })
        Assert.assertEquals(0, "abcde".lastIndexOf(4) { it == 'a' })
        Assert.assertEquals(-1, "abcde".lastIndexOf(4) { it == 'z' })
    }

    // endregion

    // region collection truncate

    @Test
    fun collection_truncate_test() {
        val list = listOf("a", "b", "c", "d")
        Assert.assertEquals(listOf("a", "b", "..."), list.truncate(2))
        Assert.assertEquals(list, list.truncate(10)) // 超出大小不截断
        Assert.assertEquals(list, list.truncate(-1)) // 负数不截断
        Assert.assertEquals(list, list.truncate(4)) // 恰好等于大小不截断
    }

    // endregion

    // region cast / castOrNull

    @Test
    fun cast_test() {
        val obj: Any? = "abc"
        Assert.assertEquals("abc", obj.cast<String>())
        Assert.assertThrows(ClassCastException::class.java) { obj.cast<Int>() }
    }

    @Test
    fun castOrNull_test() {
        val obj: Any? = "abc"
        Assert.assertEquals("abc", obj.castOrNull<String>())
        Assert.assertNull(obj.castOrNull<Int>())
        Assert.assertNull(null.castOrNull<String>())
    }

    // endregion

    // region boolean / byte / yes-no conversions

    @Test
    fun boolean_byte_conversions_test() {
        Assert.assertEquals(1.toByte(), true.toByte())
        Assert.assertEquals(0.toByte(), false.toByte())
        Assert.assertTrue(1.toByte().toBoolean())
        Assert.assertTrue((-1).toByte().toBoolean())
        Assert.assertFalse(0.toByte().toBoolean())
    }

    @Test
    fun toBooleanYesNo_test() {
        Assert.assertTrue("yes".toBooleanYesNo())
        Assert.assertFalse("no".toBooleanYesNo())
        Assert.assertFalse("Yes".toBooleanYesNo()) // 区分大小写
        Assert.assertFalse(null.toBooleanYesNo())
        Assert.assertEquals(true, "yes".toBooleanYesNoOrNull())
        Assert.assertEquals(false, "no".toBooleanYesNoOrNull())
        Assert.assertNull("maybe".toBooleanYesNoOrNull())
        Assert.assertNull(null.toBooleanYesNoOrNull())
    }

    @Test
    fun toStringOrEmpty_test() {
        Assert.assertEquals("abc", "abc".toStringOrEmpty())
        Assert.assertEquals("123", 123.toStringOrEmpty())
        Assert.assertEquals("", null.toStringOrEmpty())
    }

    // endregion

    // region UUID

    @Test
    fun toUUID_test() {
        val u1 = "abc".toUUID()
        Assert.assertEquals(u1, "abc".toUUID()) // 基于内容，稳定
        Assert.assertFalse(u1 == "abd".toUUID())
        Assert.assertEquals(u1.toString(), "abc".toUuidString())
    }

    // endregion

    // region substringIn / substringInLast 边界

    @Test
    fun substringIn_missingDelimiter_test() {
        Assert.assertEquals("abc", "abc".substringIn('<', '>')) // 默认返回自身
        Assert.assertEquals("none", "abc".substringIn('<', '>', missingDelimiterValue = "none"))
        Assert.assertEquals("none", "a<b".substringIn('<', '>', missingDelimiterValue = "none")) // 有前缀无后缀
        Assert.assertEquals("none", "abc".substringInLast('<', '>', missingDelimiterValue = "none"))
    }

    // endregion

    // region containsBlankLine 边界

    @Test
    fun containsBlankLine_edges_test() {
        Assert.assertFalse("a\nb".containsBlankLine())
        Assert.assertTrue("a\n\nb".containsBlankLine())
        Assert.assertTrue("a\r\rb".containsBlankLine()) // 两个单独的 \r
        Assert.assertTrue("a\r\n\r\nb".containsBlankLine()) // 两个 \r\n
        Assert.assertFalse("a\r\nb".containsBlankLine()) // \r\n 视为一个换行
        Assert.assertFalse("a\r".containsBlankLine()) // 末尾孤立 \r 不应越界
        Assert.assertFalse("a\r\n".containsBlankLine())
    }

    // endregion

    // region path / file / url conversions

    @Test
    fun toFile_toPath_test() {
        Assert.assertEquals(File("foo/bar.txt"), "foo/bar.txt".toFile())
        Assert.assertEquals(Path.of("foo/bar.txt"), "foo/bar.txt".toPath())
        Assert.assertEquals(File("foo/bar.txt"), "foo/bar.txt".toFileOrNull())
        Assert.assertEquals(Path.of("foo/bar.txt"), "foo/bar.txt".toPathOrNull())
    }

    @Test
    fun toPathOrNull_invalid_test() {
        // NUL 字符无法构成路径
        Assert.assertNull("\u0000".toPathOrNull())
    }

    @Test
    fun toFileUrl_and_urlRoundTrip_test() {
        val url = "foo/bar.png".toFileUrl()
        Assert.assertTrue(url.toString().startsWith("file:"))
        Assert.assertTrue(url.toString().contains("foo/bar.png"))
        Assert.assertEquals(url.toFile().toPath(), url.toPath())
    }

    @Test
    fun path_formatted_test() {
        val formatted = Paths.get("a", "b", "c").formatted()
        Assert.assertTrue(formatted.isAbsolute)
        Assert.assertEquals("c", formatted.fileName.toString())
    }

    @Test
    fun path_create_test() {
        val tmp = Files.createTempDirectory("pls-core-test-")
        try {
            val file = tmp.resolve("sub/dir/file.txt")
            file.create()
            Assert.assertTrue(Files.isRegularFile(file))
            Assert.assertTrue(Files.isDirectory(file.parent))
            // 幂等：重复调用不抛异常
            file.create()
            Assert.assertTrue(Files.isRegularFile(file))
        } finally {
            tmp.toFile().deleteRecursively()
        }
    }

    // endregion
}
