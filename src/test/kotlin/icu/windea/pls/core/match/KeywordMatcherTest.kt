package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

/**
 * @see KeywordMatcher
 */
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

    @Test
    fun matches_null_input_test() {
        Assert.assertFalse(KeywordMatcher.matches(null, "foo"))
        Assert.assertFalse(KeywordMatcher.matches(null, arrayOf("foo")))
        Assert.assertFalse(KeywordMatcher.matches(null, listOf("foo")))
    }

    @Test
    fun matches_array_test() {
        Assert.assertTrue(KeywordMatcher.matches("foo", arrayOf("bar", "foo")))
        Assert.assertFalse(KeywordMatcher.matches("baz", arrayOf("bar", "foo")))
    }
}
