package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

/**
 * @see RegexBasedGlobMatcher
 */
@Suppress("DEPRECATION")
class RegexBasedGlobMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(RegexBasedGlobMatcher.matches("abc", "abc"))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("abc", "*"))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("abc", "ab?"))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("abc", "ab*"))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("abc", "a?c"))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("ab", "a?c"))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("abc", "a?"))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("abc", "a*c"))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("abc", "a*b"))

        Assert.assertTrue(RegexBasedGlobMatcher.matches("foo.txt", "*.txt"))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("bar", "?ar"))
    }

    @Test
    fun matches_ignoreCase() {
        Assert.assertTrue(RegexBasedGlobMatcher.matches("FOO_BAR", "foo_*", ignoreCase = true))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("FOO_BAR", "foo_*", ignoreCase = false))
    }

    @Test
    fun matches() {
        Assert.assertTrue(RegexBasedGlobMatcher.matches("", "", ignoreCase = false))

        Assert.assertFalse(RegexBasedGlobMatcher.matches("", "?", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("f", "?", ignoreCase = false))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("foo", "?", ignoreCase = false))

        Assert.assertTrue(RegexBasedGlobMatcher.matches("", "*", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("f", "*", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("foo", "*", ignoreCase = false))

        Assert.assertTrue(RegexBasedGlobMatcher.matches("foo_bar", "foo_*", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("foo_bar", "*_bar", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("foo_bar", "foo*bar", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("foo_bar", "foo?bar", ignoreCase = false))

        Assert.assertFalse(RegexBasedGlobMatcher.matches("prefix_foo_bar", "foo_*", ignoreCase = false))
        Assert.assertTrue(RegexBasedGlobMatcher.matches("prefix_foo_bar", "*_bar", ignoreCase = false))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("prefix_foo_bar", "foo*bar", ignoreCase = false))
        Assert.assertFalse(RegexBasedGlobMatcher.matches("prefix_foo_bar", "foo?bar", ignoreCase = false))
    }
}
