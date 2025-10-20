package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

class RegexMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(RegexMatcher.matches("abc123", "[a-z]+\\d+"))
        Assert.assertFalse(RegexMatcher.matches("abc", "[0-9]+"))
    }
}
