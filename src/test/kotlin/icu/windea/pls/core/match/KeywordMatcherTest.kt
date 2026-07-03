package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

class KeywordMatcherTest {
    @Test
    fun matches_string_exact_and_ignoreCase() {
        Assert.assertTrue(KeywordMatcher.matches("foo", "foo", ignoreCase = false, usePattern = false))
        Assert.assertFalse(KeywordMatcher.matches("Foo", "foo", ignoreCase = false, usePattern = false))
        Assert.assertTrue(KeywordMatcher.matches("Foo", "foo", ignoreCase = true, usePattern = false))
    }

    @Test
    fun matches_string_pattern() {
        Assert.assertTrue(KeywordMatcher.matches("foo_bar", "foo_*", ignoreCase = false, usePattern = true))
        Assert.assertFalse(KeywordMatcher.matches("foo_bar", "FOO_*", ignoreCase = false, usePattern = true))
        Assert.assertTrue(KeywordMatcher.matches("foo_bar", "FOO_*", ignoreCase = true, usePattern = true))
        Assert.assertTrue(KeywordMatcher.matches("x", "?", ignoreCase = false, usePattern = true))
        Assert.assertFalse(KeywordMatcher.matches("xy", "?", ignoreCase = false, usePattern = true))
    }

    @Test
    fun matches_smoke() {
        Assert.assertTrue(KeywordMatcher.matches("Foo", "foo"))
        Assert.assertTrue(KeywordMatcher.matches("Foo", listOf("foo", "bar", "")))
    }
}
