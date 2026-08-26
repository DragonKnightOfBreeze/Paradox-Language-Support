package icu.windea.pls.core.match

import org.junit.Assert
import org.junit.Test

/**
 * @see AntFromRegexMatcher
 */
class AntFromRegexMatcherTest {
    @Test
    fun matches_basic() {
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar", "foo/*"))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar/baz", "foo/**"))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/bar/baz", "foo/*"))
    }

    @Test
    fun matches_question_mark() {
        Assert.assertTrue(AntFromRegexMatcher.matches("foo/bar", "foo/b?r"))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo/ba", "foo/b?r"))
    }

    @Test
    fun matches_ignoreCase() {
        Assert.assertTrue(AntFromRegexMatcher.matches("FOO/BAR", "foo/*", ignoreCase = true))
        Assert.assertFalse(AntFromRegexMatcher.matches("FOO/BAR", "foo/*", ignoreCase = false))
    }

    @Test
    fun matches_trimSeparator_and_empty() {
        Assert.assertTrue(AntFromRegexMatcher.matches("/foo/bar/", "foo/**"))
        Assert.assertFalse(AntFromRegexMatcher.matches("foo", ""))
        Assert.assertTrue(AntFromRegexMatcher.matches("foo", "**"))
    }
}
