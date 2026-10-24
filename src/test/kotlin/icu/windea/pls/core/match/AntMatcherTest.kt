package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

/**
 * @see AntMatcher
 */
class AntMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(AntMatcher.matches("foo/bar", "foo/*"))
        Assert.assertTrue(AntMatcher.matches("foo/bar/baz", "foo/**"))
        Assert.assertFalse(AntMatcher.matches("foo/bar/baz", "foo/*"))
    }

    @Test
    fun matches_question_mark() {
        Assert.assertTrue(AntMatcher.matches("foo/bar", "foo/b?r"))
        Assert.assertFalse(AntMatcher.matches("foo/ba", "foo/b?r"))
    }

    @Test
    fun matches_ignoreCase() {
        Assert.assertTrue(AntMatcher.matches("FOO/BAR", "foo/*", ignoreCase = true))
        Assert.assertFalse(AntMatcher.matches("FOO/BAR", "foo/*", ignoreCase = false))
    }

    @Test
    fun matches_trimSeparator_and_empty() {
        Assert.assertTrue(AntMatcher.matches("/foo/bar/", "foo/**"))
        Assert.assertFalse(AntMatcher.matches("foo", ""))
        Assert.assertTrue(AntMatcher.matches("foo", "**"))
    }

    @Test
    fun matches_complex() {
        // 基本匹配
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/name**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "foo/bar/name**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "/foo/bar/name**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/name**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/**/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/**/bar/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/nam?", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/na?e", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/na*?e", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/*", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/*a*e", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/*/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/*/n?me", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/**/n?me", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/**r/n?me", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/foo/*", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/*/name", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/foo/bar/na?", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/foo/bar/", ignoreCase = false))

        Assert.assertTrue(AntMatcher.matches("enums/enum[e]", "enums/enum[?]", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("enums/enum[a", "enums/enum[?", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("enums/enum[f", "enums/enum[?]", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("enums/enum[e]", "enums/enum[*]", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("enums/enum[e", "enums/enum[*]", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("enums/enum123", "enums/enum[*]", ignoreCase = false))

        // 基本匹配
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/*", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/*/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "*/bar/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/n?me", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/na*", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/*me", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "foo/bar/nam", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "foo/bar/names", ignoreCase = false))

        // -* 匹配
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**/bar/**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/**/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/**/bar/**/name", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "foo/**/baz", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "name", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "*/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**/name", ignoreCase = false))

        // 边界与特殊情况
        Assert.assertTrue(AntMatcher.matches("foo", "foo", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo", "*", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo", "**", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo", "bar", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar", "/**/bar", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar", "**/bar", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/baz", "foo/**/baz", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/baz", "foo/**/qux", ignoreCase = false))

        // 路径分隔符兼容
        Assert.assertTrue(AntMatcher.matches("foo\\bar\\name".replace('\\', '/'), "foo/**/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo\\bar\\baz".replace('\\', '/'), "foo/**/baz", ignoreCase = false))

        // 大小写
        Assert.assertTrue(AntMatcher.matches("FOO/BAR/NAME", "foo/bar/name", ignoreCase = true))
        Assert.assertFalse(AntMatcher.matches("FOO/BAR/NAME", "foo/bar/name", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "FOO/BAR/NAME", ignoreCase = true))

        // 空串
        Assert.assertTrue(AntMatcher.matches("", "", ignoreCase = false))
        Assert.assertTrue(AntMatcher.matches("", "**", ignoreCase = false))
        Assert.assertFalse(AntMatcher.matches("foo", "", ignoreCase = false))
    }
}
