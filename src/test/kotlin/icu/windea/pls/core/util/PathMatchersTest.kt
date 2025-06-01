package icu.windea.pls.core.util

import org.junit.*
import kotlin.system.measureTimeMillis

class PathMatchersTest {
    @Suppress("DEPRECATION")
    @Test
    fun antBenchmarkTest() {
        val patterns = listOf(
            "foo/bar/baz",
            "foo/*/baz",
            "foo/**/baz",
            "**/baz",
            "foo/bar?baz",
            "foo/*/bar/**/baz",
            "**",
            "foo/bar/*",
            "foo/bar/**",
            "foo/bar/ba*",
            "foo/bar/ba?",
        )
        val paths = listOf(
            "foo/bar/baz",
            "foo/abc/baz",
            "foo/bar/abc/baz",
            "foo/bar/baz/qux",
            "foo/barbaz",
            "foo/bar/xyz/baz",
            "foo/bar/abc/def/baz",
            "foo/bar/ba",
            "foo/bar/baa",
            "foo/bar/baz",
            "foo/bar/baZ",
        )
        val iterations = 500_000
        var dummy = 0

        // 正则实现
        val t1 = measureTimeMillis {
            repeat(iterations) {
                for (pattern in patterns) {
                    for (path in paths) {
                        // 注意必须在这个循环中直接调用匹配方法，否则测试结果可能会显示基于正则的实现更快
                        if (PatternMatchers.AntFromRegexMatcher.matches(path, pattern)) dummy++
                    }
                }
            }
        }

        // 纯Kotlin实现
        val t2 = measureTimeMillis {
            repeat(iterations) {
                for (pattern in patterns) {
                    for (path in paths) {
                        if (PatternMatchers.AntMatcher.matches(path, pattern)) dummy++
                    }
                }
            }
        }
        println("Regex impl: $t1 ms")
        println("Pure Kotlin impl: $t2 ms")
        println("(ignore $dummy)")

        // Regex impl: 14058 ms
        // Pure Kotlin impl: 6657 ms
        // (ignore 51500000)
    }

    @Test
    fun antTest() {
        // 基本匹配
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/name**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "foo/bar/name**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "/foo/bar/name**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/name**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/**/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/**/bar/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/nam?", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/na?e", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/na*?e", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/*", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/*a*e", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/b*r/*a*e", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/*foo/*/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/*foo/*/n?me", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/*foo/**/n?me", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/*foo/**r/n?me", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/*", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/*/name", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/na?", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("/foo/bar/name", "/foo/bar/", false))

        Assert.assertTrue(PatternMatchers.AntMatcher.matches("enums/enum[e]", "enums/enum[?]", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("enums/enum[a", "enums/enum[?", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("enums/enum[f", "enums/enum[?]", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("enums/enum[e]", "enums/enum[*]", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("enums/enum[e", "enums/enum[*]", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("enums/enum123", "enums/enum[*]", false))

        // 基本匹配
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/*", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/*/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "*/bar/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/n?me", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/na*", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/*me", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/nam", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/bar/names", false))

        // ** 匹配
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "**/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "**/bar/**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "**", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/**/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/**/bar/**/name", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("foo/bar/name", "foo/**/baz", false))

        // 边界与特殊情况
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo", "foo", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo", "*", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo", "**", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("foo", "bar", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("/foo/bar", "/**/bar", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar", "**/bar", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/baz", "foo/**/baz", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("foo/bar/baz", "foo/**/qux", false))

        // 路径分隔符兼容
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo\\bar\\name".replace('\\', '/'), "foo/**/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo\\bar\\baz".replace('\\', '/'), "foo/**/baz", false))

        // 大小写
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("FOO/BAR/NAME", "foo/bar/name", true))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("FOO/BAR/NAME", "foo/bar/name", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("foo/bar/name", "FOO/BAR/NAME", true))

        // 空串
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("", "", false))
        Assert.assertTrue(PatternMatchers.AntMatcher.matches("", "**", false))
        Assert.assertFalse(PatternMatchers.AntMatcher.matches("foo", "", false))
    }

}
