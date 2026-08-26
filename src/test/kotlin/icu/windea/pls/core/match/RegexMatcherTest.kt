package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

/**
 * @see RegexMatcher
 */
class RegexMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(RegexMatcher.matches("abc123", "[a-z]+\\d+"))
        Assert.assertFalse(RegexMatcher.matches("abc", "[0-9]+"))
    }

    @Test
    fun matches_ignoreCase() {
        Assert.assertTrue(RegexMatcher.matches("ABC", "[a-z]+", ignoreCase = true))
        Assert.assertFalse(RegexMatcher.matches("ABC", "[a-z]+", ignoreCase = false))
    }

    @Test
    fun matches_edge() {
        Assert.assertFalse(RegexMatcher.matches("abc", "")) // 空模式不匹配非空输入
        Assert.assertTrue(RegexMatcher.matches("", ""))
        Assert.assertFalse(RegexMatcher.matches("abc", "b")) // 全匹配
        Assert.assertTrue(RegexMatcher.matches("abc", ".*b.*"))
    }
}
