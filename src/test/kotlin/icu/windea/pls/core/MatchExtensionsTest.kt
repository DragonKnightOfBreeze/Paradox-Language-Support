package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class MatchExtensionsTest {
    @Test
    fun matchesPattern_smokeTest() {
        Assert.assertTrue("foo.txt".matchesPattern("*.txt"))
        Assert.assertTrue("FOO.TXT".matchesPattern("*.txt", ignoreCase = true))
        Assert.assertFalse("foo.txt".matchesPattern("*.md"))
        Assert.assertFalse("FOO.TXT".matchesPattern("*.txt")) // 默认区分大小写
    }

    @Test
    fun matchesPatterns_smokeTest() {
        Assert.assertTrue("foo".matchesPatterns("foo;bar"))
        Assert.assertFalse("baz".matchesPatterns("foo;bar"))
        Assert.assertTrue("foo".matchesPatterns(" foo ; bar ")) // 分隔后忽略首尾空白
        Assert.assertTrue("FOO".matchesPatterns("foo;bar", ignoreCase = true))
    }

    @Test
    fun matchesAntPattern_smokeTest() {
        Assert.assertTrue("foo/bar/baz".matchesAntPattern("foo/**"))
        Assert.assertTrue("foo/bar".matchesAntPattern("foo/*"))
        Assert.assertFalse("foo/bar/baz".matchesAntPattern("foo/*"))
        Assert.assertTrue("FOO/BAR".matchesAntPattern("foo/*", ignoreCase = true))
    }

    @Test
    fun matchesAntPatterns_smokeTest() {
        Assert.assertTrue("foo/x".matchesAntPatterns("foo/**;bar/**"))
        Assert.assertFalse("baz/x".matchesAntPatterns("foo/**;bar/**"))
        Assert.assertTrue("foo/x".matchesAntPatterns(" foo/** ; bar/** "))
    }

    @Test
    fun matchesRegex_smokeTest() {
        Assert.assertTrue("abc".matchesRegex("[a-z]+"))
        Assert.assertFalse("abc".matchesRegex("[0-9]+"))
        Assert.assertTrue("ABC".matchesRegex("[a-z]+", ignoreCase = true))
    }

    @Test
    fun nullInput_smokeTest() {
        val s: String? = null
        Assert.assertFalse(s.matchesPattern("*.txt"))
        Assert.assertFalse(s.matchesPatterns("foo"))
        Assert.assertFalse(s.matchesAntPattern("foo/**"))
        Assert.assertFalse(s.matchesAntPatterns("foo/**"))
        Assert.assertFalse(s.matchesRegex("[a-z]+"))
    }
}
