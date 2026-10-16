package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

/**
 * @see AntFromRegexMatcher
 */
@Suppress("DEPRECATION")
class AntFromRegexMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar", "foo/*"))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/baz", "foo/**"))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/baz", "foo/*"))
    }

    @Test
    fun matches_question_mark() {
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar", "foo/b?r"))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/ba", "foo/b?r"))
    }

    @Test
    fun matches_ignoreCase() {
        Assert.assertTrue(AntFromRegexMatcher.matches("FOO/BAR", "foo/*", ignoreCase = true))
        Assert.assertFalse(AntFromRegexMatcher.matches("FOO/BAR", "foo/*", ignoreCase = false))
    }

    @Test
    fun matches_trimSeparator_and_empty() {
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/", "foo/**"))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo", ""))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo", "**"))
    }

    @Test
    fun matches_complex() {
        // 基本匹配
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/name**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "foo/bar/name**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "/foo/bar/name**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/name**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/**/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/**/bar/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/nam?", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/na?e", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/na*?e", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/*", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/*a*e", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/*foo/*/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/*foo/*/n?me", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/*foo/**/n?me", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/name", "/*foo/**r/n?me", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/*", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("/foo/bar/name", "/*/name", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/na?", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("/foo/bar/name", "/foo/bar/", false))

        Assert.assertTrue(AntFromRegexMatcher.matches("enums/enum[e]", "enums/enum[?]", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("enums/enum[a", "enums/enum[?", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("enums/enum[f", "enums/enum[?]", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("enums/enum[e]", "enums/enum[*]", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("enums/enum[e", "enums/enum[*]", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("enums/enum123", "enums/enum[*]", false))

        // 基本匹配
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/*", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/*/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "*/bar/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/n?me", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/na*", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/*me", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/nam", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/name", "foo/bar/names", false))

        // -* 匹配
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "**/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "**/bar/**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "**", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/**/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "foo/**/bar/**/name", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/name", "foo/**/baz", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/name", "name", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/name", "*/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "**/name", false))

        // 边界与特殊情况
        Assert.assertTrue(AntFromRegexMatcher.matches("foo", "foo", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo", "*", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo", "**", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo", "bar", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar", "/**/bar", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar", "**/bar", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/baz", "foo/**/baz", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/baz", "foo/**/qux", false))

        // 路径分隔符兼容
        Assert.assertTrue(AntFromRegexMatcher.matches("foo\\bar\\name".replace('\\', '/'), "foo/**/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo\\bar\\baz".replace('\\', '/'), "foo/**/baz", false))

        // 大小写
        Assert.assertTrue(AntFromRegexMatcher.matches("FOO/BAR/NAME", "foo/bar/name", true))
        Assert.assertFalse(AntFromRegexMatcher.matches("FOO/BAR/NAME", "foo/bar/name", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/name", "FOO/BAR/NAME", true))

        // 空串
        Assert.assertTrue(AntFromRegexMatcher.matches("", "", false))
        Assert.assertTrue(AntFromRegexMatcher.matches("", "**", false))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo", "", false))
    }
}
