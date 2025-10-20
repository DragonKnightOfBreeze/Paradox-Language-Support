package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

class PathMatcherTest {
    // matches(String, String)
    @Test
    fun matches_string_exact_and_ignoreCase() {
        Assert.assertTrue(PathMatcher.matches("foo", "foo", ignoreCase = false))
        Assert.assertFalse(PathMatcher.matches("Foo", "foo", ignoreCase = false))
        Assert.assertTrue(PathMatcher.matches("Foo", "foo", ignoreCase = true))
    }

    @Test
    fun matches_string_any_and_pattern() {
        // useAny
        Assert.assertFalse(PathMatcher.matches("bar", "any", ignoreCase = false, useAny = false))
        Assert.assertTrue(PathMatcher.matches("bar", "any", ignoreCase = false, useAny = true))
        // usePattern
        Assert.assertTrue(PathMatcher.matches("foo_bar", "foo_*", ignoreCase = false, usePattern = true))
        Assert.assertFalse(PathMatcher.matches("foo_bar", "FOO_*", ignoreCase = false, usePattern = true))
        Assert.assertTrue(PathMatcher.matches("foo_bar", "FOO_*", ignoreCase = true, usePattern = true))
        Assert.assertTrue(PathMatcher.matches("x", "?", ignoreCase = false, usePattern = true))
        Assert.assertFalse(PathMatcher.matches("xy", "?", ignoreCase = false, usePattern = true))
    }

    // matches(List<String>, List<String>)
    @Test
    fun matches_list_basic_and_mixed_flags() {
        val a = listOf("foo", "bar")
        val b = listOf("foo", "bar")
        val c = listOf("foo", "baz")
        val p = listOf("foo", "b*")
        val any = listOf("foo", "any")

        Assert.assertTrue(PathMatcher.matches(a, b, ignoreCase = false))
        Assert.assertFalse(PathMatcher.matches(a, c, ignoreCase = false))

        // pattern on second segment
        Assert.assertTrue(PathMatcher.matches(a, p, ignoreCase = false, usePattern = true))
        // any on second segment
        Assert.assertFalse(PathMatcher.matches(a, any, ignoreCase = false, useAny = false))
        Assert.assertTrue(PathMatcher.matches(a, any, ignoreCase = false, useAny = true))

        // ignoreCase across list
        val upper = listOf("Foo", "Bar")
        Assert.assertFalse(PathMatcher.matches(upper, b, ignoreCase = false))
        Assert.assertTrue(PathMatcher.matches(upper, b, ignoreCase = true))
    }

    // relative(List<String>, List<String>)
    @Test
    fun relative_basic_and_equal_and_mismatch() {
        val base = listOf("foo")
        val path = listOf("foo", "bar", "x")
        val mismatch = listOf("bar", "foo")

        // base is prefix of path -> return next segment
        Assert.assertEquals("bar", PathMatcher.relative(base, path))
        // equal -> empty string
        Assert.assertEquals("", PathMatcher.relative(path, path))
        // base longer -> null
        Assert.assertNull(PathMatcher.relative(path, base))
        // mismatch -> null
        Assert.assertNull(PathMatcher.relative(base, mismatch))
    }

    @Test
    fun relative_with_any_and_pattern_and_ignoreCase() {
        val base = listOf("Foo")
        val path = listOf("foo", "bar", "x")
        // ignoreCase: other[0] = "foo" 与 base[0] = "Foo" 匹配
        Assert.assertEquals("bar", PathMatcher.relative(base, path, ignoreCase = true))

        // any 与 pattern 仅能用于 other 参数
        val otherWithAny = listOf("any", "bar", "x")
        Assert.assertEquals("bar", PathMatcher.relative(listOf("foo"), otherWithAny, useAny = true))

        val otherWithPattern = listOf("f*", "bar", "x")
        Assert.assertEquals("bar", PathMatcher.relative(listOf("foo"), otherWithPattern, usePattern = true))
    }
}
