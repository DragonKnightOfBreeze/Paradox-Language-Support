package icu.windea.pls.core.match.similarity

import org.junit.Assert
import org.junit.Test

class SimilarityMatchersTest {
    // PrefixSimilarityMatcher
    @Test
    fun prefix_basic() {
        Assert.assertNotNull(PrefixSimilarityMatcher.match("foo", "fooa", false))
        Assert.assertNotNull(PrefixSimilarityMatcher.match("foo", "foo_b", false))
        Assert.assertNull(PrefixSimilarityMatcher.match("foo", "foa", false))
        Assert.assertNull(PrefixSimilarityMatcher.match("foo", "a_foo", false))
    }

    @Test
    fun prefix_ignoreCase() {
        Assert.assertNotNull(PrefixSimilarityMatcher.match("FOO", "foo_a", true))
        Assert.assertNull(PrefixSimilarityMatcher.match("FOO", "foo_a", false))
    }

    // SnippetSimilarityMatcher
    @Test
    fun snippet_basic() {
        Assert.assertNotNull(SnippetSimilarityMatcher.match("foo_bar", "foo_bar_a", false))
        Assert.assertNotNull(SnippetSimilarityMatcher.match("foo_bar", "foo_a_bar", false))
        Assert.assertNull(SnippetSimilarityMatcher.match("foo_bar", "a_foo_bar", false))
        Assert.assertNull(SnippetSimilarityMatcher.match("foo_bar", "bar_a_foo", false))
    }

    @Test
    fun snippet_ignoreCase_on_first_and_sequence() {
        // 首片段忽略大小写，相对顺序可跨越
        Assert.assertNotNull(SnippetSimilarityMatcher.match("foo_bar", "Foo_A_Bar", true))
        Assert.assertNull(SnippetSimilarityMatcher.match("foo_bar", "Foo_A_Bar", false))
    }

    // TypoSimilarityMatcher (OSA)
    @Test
    fun typo_basic() {
        // 单字符缺失
        val r1 = TypoSimilarityMatcher.match("possible", "possibe", false)
        Assert.assertNotNull(r1)
        Assert.assertTrue(r1!!.score > 0.6)

        // 单字符替换
        val r2 = TypoSimilarityMatcher.match("possible", "possable", false)
        Assert.assertNotNull(r2)
        Assert.assertTrue(r2!!.score > 0.6)

        // 首字符不同 -> 不匹配（新约束）
        val r3 = TypoSimilarityMatcher.match("abc", "xyz", false)
        Assert.assertNull(r3)
    }

    @Test
    fun typo_empty_inputs_return_null() {
        Assert.assertNull(TypoSimilarityMatcher.match("", "abc", false))
        Assert.assertNull(TypoSimilarityMatcher.match("abc", "", false))
    }

    // Score bounds and edge cases
    @Test
    fun prefix_score_bounds() {
        val r1 = PrefixSimilarityMatcher.match("foo", "foo", false)
        Assert.assertNotNull(r1)
        Assert.assertEquals(1.0, r1!!.score, 1e-9)
        val r2 = PrefixSimilarityMatcher.match("foo", "foooo", false)
        Assert.assertNotNull(r2)
        Assert.assertTrue(r2!!.score in 0.0..1.0)
        Assert.assertTrue(r2.score < 1.0)
    }

    @Test
    fun snippet_empty_segments_behavior() {
        // candidate 带有连续分隔符：允许跨越，返回分数 2/3
        val r1 = SnippetSimilarityMatcher.match("foo_bar", "foo__bar", false)
        Assert.assertNotNull(r1)
        Assert.assertEquals(2.0 / 3.0, r1!!.score, 1e-9)

        // input 带有连续分隔符：不允许匹配（当前实现）
        val r2 = SnippetSimilarityMatcher.match("foo__bar", "foo_bar_b", false)
        Assert.assertNull(r2)
    }

    @Test
    fun typo_ignoreCase_first_char_rule() {
        // 首字符大小写不同：ignoreCase=false 不匹配，true 匹配
        Assert.assertNull(TypoSimilarityMatcher.match("Foo", "foo", false))
        Assert.assertNotNull(TypoSimilarityMatcher.match("Foo", "foo", true))
    }

    @Test
    fun typo_score_bounds() {
        val same = TypoSimilarityMatcher.match("abcd", "abcd", false)
        Assert.assertNotNull(same)
        Assert.assertEquals(1.0, same!!.score, 1e-9)
        val add = TypoSimilarityMatcher.match("a", "aXXXX", false)
        Assert.assertNotNull(add)
        Assert.assertTrue(add!!.score in 0.0..1.0)
        Assert.assertTrue(add.score < 1.0)
    }

    // Additional coverage
    @Test
    fun prefix_empty_inputs_return_null() {
        Assert.assertNull(PrefixSimilarityMatcher.match("", "foo", false))
        Assert.assertNull(PrefixSimilarityMatcher.match("foo", "", false))
    }

    @Test
    fun snippet_score_and_strategy() {
        val r = SnippetSimilarityMatcher.match("foo_bar", "foo_bar_b", false)
        Assert.assertNotNull(r)
        Assert.assertEquals(2.0 / 3.0, r!!.score, 1e-9)
        Assert.assertEquals(SimilarityMatchStrategy.SNIPPET, r.strategy)
    }

    @Test
    fun typo_transposition_same_first_char() {
        val r = TypoSimilarityMatcher.match("cab", "cba", false) // one adjacent swap, same first char
        Assert.assertNotNull(r)
        Assert.assertTrue(r!!.score > 0.66 - 1e-9) // ~= 1 - 1/3
    }
}
