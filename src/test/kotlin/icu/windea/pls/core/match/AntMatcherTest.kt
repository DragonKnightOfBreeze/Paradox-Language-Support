package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

class AntMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(AntMatcher.matches("foo/bar/baz", "foo/**"))
        Assert.assertTrue(AntMatcher.matches("foo/bar", "foo/*"))
        Assert.assertFalse(AntMatcher.matches("foo/bar/baz", "foo/*"))
    }

    @Test
    fun matches() {
        // 基本匹配
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/name**", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "foo/bar/name**", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "/foo/bar/name**", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/name**", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/name", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/**", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/**/name", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/**/bar/name", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/**", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/nam?", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/na?e", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/na*?e", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/*", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/bar/*a*e", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/*/name", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/*/n?me", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/**/n?me", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar/name", "/*foo/**r/n?me", false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/foo/*", false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/*/name", false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/foo/bar/na?", false))
        Assert.assertFalse(AntMatcher.matches("/foo/bar/name", "/foo/bar/", false))

        Assert.assertTrue(AntMatcher.matches("enums/enum[e]", "enums/enum[?]", false))
        Assert.assertTrue(AntMatcher.matches("enums/enum[a", "enums/enum[?", false))
        Assert.assertFalse(AntMatcher.matches("enums/enum[f", "enums/enum[?]", false))
        Assert.assertTrue(AntMatcher.matches("enums/enum[e]", "enums/enum[*]", false))
        Assert.assertFalse(AntMatcher.matches("enums/enum[e", "enums/enum[*]", false))
        Assert.assertFalse(AntMatcher.matches("enums/enum123", "enums/enum[*]", false))

        // 基本匹配
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/name", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/*", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/*/name", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "*/bar/name", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/n?me", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/na*", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/bar/*me", false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "foo/bar/nam", false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "foo/bar/names", false))

        // -* 匹配
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/**", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**/name", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**/bar/**", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "**", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/**/name", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "foo/**/bar/**/name", false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/name", "foo/**/baz", false))

        // 边界与特殊情况
        Assert.assertTrue(AntMatcher.matches("foo", "foo", false))
        Assert.assertTrue(AntMatcher.matches("foo", "*", false))
        Assert.assertTrue(AntMatcher.matches("foo", "**", false))
        Assert.assertFalse(AntMatcher.matches("foo", "bar", false))
        Assert.assertTrue(AntMatcher.matches("/foo/bar", "/**/bar", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar", "**/bar", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/baz", "foo/**/baz", false))
        Assert.assertFalse(AntMatcher.matches("foo/bar/baz", "foo/**/qux", false))

        // 路径分隔符兼容
        Assert.assertTrue(AntMatcher.matches("foo\\bar\\name".replace('\\', '/'), "foo/**/name", false))
        Assert.assertTrue(AntMatcher.matches("foo\\bar\\baz".replace('\\', '/'), "foo/**/baz", false))

        // 大小写
        Assert.assertTrue(AntMatcher.matches("FOO/BAR/NAME", "foo/bar/name", true))
        Assert.assertFalse(AntMatcher.matches("FOO/BAR/NAME", "foo/bar/name", false))
        Assert.assertTrue(AntMatcher.matches("foo/bar/name", "FOO/BAR/NAME", true))

        // 空串
        Assert.assertTrue(AntMatcher.matches("", "", false))
        Assert.assertTrue(AntMatcher.matches("", "**", false))
        Assert.assertFalse(AntMatcher.matches("foo", "", false))
    }
}
