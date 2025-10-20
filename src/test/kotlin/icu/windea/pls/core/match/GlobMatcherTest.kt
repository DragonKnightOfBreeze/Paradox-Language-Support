package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

class GlobMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(GlobMatcher.matches("abc", "abc"))
        Assert.assertTrue(GlobMatcher.matches("abc", "*"))
        Assert.assertTrue(GlobMatcher.matches("abc", "ab?"))
        Assert.assertTrue(GlobMatcher.matches("abc", "ab*"))
        Assert.assertTrue(GlobMatcher.matches("abc", "a?c"))
        Assert.assertFalse(GlobMatcher.matches("ab", "a?c"))
        Assert.assertFalse(GlobMatcher.matches("abc", "a?"))
        Assert.assertTrue(GlobMatcher.matches("abc", "a*c"))
        Assert.assertFalse(GlobMatcher.matches("abc", "a*b"))

        Assert.assertTrue(GlobMatcher.matches("foo.txt", "*.txt"))
        Assert.assertTrue(GlobMatcher.matches("bar", "?ar"))
    }

    @Test
    fun matches() {
        Assert.assertTrue(GlobMatcher.matches("", "", false))

        Assert.assertFalse(GlobMatcher.matches("", "?", false))
        Assert.assertTrue(GlobMatcher.matches("f", "?", false))
        Assert.assertFalse(GlobMatcher.matches("foo", "?", false))

        Assert.assertTrue(GlobMatcher.matches("", "*", false))
        Assert.assertTrue(GlobMatcher.matches("f", "*", false))
        Assert.assertTrue(GlobMatcher.matches("foo", "*", false))

        Assert.assertTrue(GlobMatcher.matches("foo_bar", "foo_*", false))
        Assert.assertTrue(GlobMatcher.matches("foo_bar", "*_bar", false))
        Assert.assertTrue(GlobMatcher.matches("foo_bar", "foo*bar", false))
        Assert.assertTrue(GlobMatcher.matches("foo_bar", "foo?bar", false))


        Assert.assertFalse(GlobMatcher.matches("prefix_foo_bar", "foo_*", false))
        Assert.assertTrue(GlobMatcher.matches("prefix_foo_bar", "*_bar", false))
        Assert.assertFalse(GlobMatcher.matches("prefix_foo_bar", "foo*bar", false))
        Assert.assertFalse(GlobMatcher.matches("prefix_foo_bar", "foo?bar", false))
    }
}
