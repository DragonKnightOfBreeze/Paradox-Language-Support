package icu.windea.pls.core.match.similarity

import org.junit.Assert
import org.junit.Test

class SimilarityMatchServiceTest {
    @Test
    fun service_pipeline_order_prefix_snippet_typo() {
        val input = "foo_bar"
        val candidates = listOf(
            "foo_bar_a", // prefix
            "foo_a_bar", // snippet
            "fop_bar",   // typo (1 substitution)
            "a_foo_bar"  // no match
        )
        val options = SimilarityMatchOptions(
            ignoreCase = true,
            enablePrefixMatch = true,
            enableSnippetMatch = true,
            enableTypoMatch = true,
            typoTopN = 5,
            typoMinScore = 0.6
        )
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        val values = results.map { it.value }
        Assert.assertEquals(listOf("foo_bar_a", "foo_a_bar", "fop_bar"), values)
        // ensure strategies applied
        Assert.assertEquals(SimilarityMatchStrategy.PREFIX, results[0].strategy)
        Assert.assertEquals(SimilarityMatchStrategy.SNIPPET, results[1].strategy)
        Assert.assertEquals(SimilarityMatchStrategy.TYPO, results[2].strategy)
    }

    @Test
    fun service_typo_topN_and_threshold() {
        val input = "possible"
        val candidates = listOf("possibe", "possable", "xyz") // two good, one bad
        val options = SimilarityMatchOptions(
            ignoreCase = false,
            enablePrefixMatch = false,
            enableSnippetMatch = false,
            enableTypoMatch = true,
            typoTopN = 2,
            typoMinScore = 0.6
        )
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        val values = results.map { it.value }
        Assert.assertEquals(2, values.size)
        Assert.assertTrue(values.contains("possibe"))
        Assert.assertTrue(values.contains("possable"))
        Assert.assertFalse(values.contains("xyz"))
        // scores should be >= threshold
        Assert.assertTrue(results.all { it.score >= options.typoMinScore })
    }

    @Test
    fun service_ignoreCase_affects_prefix_and_snippet() {
        val input = "FOO_BAR"
        val candidates = listOf("foo_bar_a", "foo_a_bar")
        // 仅验证前缀/片段在大小写敏感与否的区别，禁用 typo 以避免干扰
        val off = SimilarityMatchOptions(ignoreCase = false, enableTypoMatch = false)
        val on = SimilarityMatchOptions(ignoreCase = true, enableTypoMatch = false)
        val rOff = SimilarityMatchService.findBestMatches(input, candidates, off)
        val rOn = SimilarityMatchService.findBestMatches(input, candidates, on)
        Assert.assertTrue(rOff.isEmpty())
        Assert.assertEquals(listOf("foo_bar_a", "foo_a_bar"), rOn.map { it.value })
    }

    @Test
    fun service_empty_candidates_and_empty_input() {
        val options = SimilarityMatchOptions()
        Assert.assertTrue(SimilarityMatchService.findBestMatches("", emptyList(), options).isEmpty())
        Assert.assertTrue(SimilarityMatchService.findBestMatches("foo", emptyList(), options).isEmpty())
        Assert.assertTrue(SimilarityMatchService.findBestMatches("", listOf("a"), options).isEmpty())
    }

    @Test
    fun service_dedup_and_cross_stage_dedup() {
        val input = "foo"
        val candidates = listOf(
            "fooa", "fooa",        // duplicate
            "foo_a",                // matches prefix and snippet
            "bar"                   // no match
        )
        val options = SimilarityMatchOptions(ignoreCase = false, enableTypoMatch = false)
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        val values = results.map { it.value }
        Assert.assertEquals(listOf("foo_a", "fooa"), values)
    }

    @Test
    fun service_typo_sorting_ties_then_lex() {
        val input = "abcd"
        val candidates = listOf("abcx", "abcy") // both distance=1 => same score=0.75
        val options = SimilarityMatchOptions(
            enablePrefixMatch = false,
            enableSnippetMatch = false,
            enableTypoMatch = true,
            typoMinScore = 0.5,
            typoTopN = 10
        )
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        val values = results.map { it.value }
        Assert.assertEquals(listOf("abcx", "abcy"), values) // should keep order (index in candidates)
    }

    @Test
    fun service_prefix_sorted_lex() {
        val input = "foo"
        val candidates = listOf("foob", "fooa")
        val options = SimilarityMatchOptions(ignoreCase = false, enableSnippetMatch = false, enableTypoMatch = false)
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        Assert.assertEquals(listOf("foob", "fooa"), results.map { it.value }) // should keep order (index in candidates)
        Assert.assertTrue(results.all { it.strategy == SimilarityMatchStrategy.PREFIX })
    }

    // --- More typo parameter combination tests ---

    @Test
    fun service_typo_threshold_excludes_all() {
        val input = "abcd"
        // distance 2 => score = 1 - 2/4 = 0.5
        val candidates = listOf("abxx", "abyy")
        val options = SimilarityMatchOptions(
            enablePrefixMatch = false,
            enableSnippetMatch = false,
            enableTypoMatch = true,
            typoMinScore = 0.76, // higher than any of the above
            typoTopN = 5
        )
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        Assert.assertTrue(results.isEmpty())
    }

    @Test
    fun service_typo_threshold_equal_edge_included() {
        val input = "abcd"
        val candidates = listOf("abcd") // score = 1.0
        val options = SimilarityMatchOptions(
            enablePrefixMatch = false,
            enableSnippetMatch = false,
            enableTypoMatch = true,
            typoMinScore = 1.0,
            typoTopN = 1
        )
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        Assert.assertEquals(listOf("abcd"), results.map { it.value })
        Assert.assertEquals(1.0, results.first().score, 1e-9)
    }

    @Test
    fun service_typo_topN_limits_results_with_ties() {
        val input = "abcd"
        // exact + several distance=1 variants => same score among the latter
        val candidates = listOf("abcd", "abca", "abcb", "abcc", "abce")
        val options = SimilarityMatchOptions(
            enablePrefixMatch = false,
            enableSnippetMatch = false,
            enableTypoMatch = true,
            typoMinScore = 0.5,
            typoTopN = 3
        )
        val results = SimilarityMatchService.findBestMatches(input, candidates, options)
        // expect: exact match first, then two lexicographically smallest among distance=1
        Assert.assertEquals(listOf("abcd", "abca", "abcb"), results.map { it.value })
    }

    @Test
    fun service_typo_ignoreCase_affects_first_char_rule() {
        val input = "AbCd"
        val candidates = listOf("abcd", "AbcE")
        val off = SimilarityMatchOptions(
            ignoreCase = false,
            enablePrefixMatch = false,
            enableSnippetMatch = false,
            enableTypoMatch = true,
            typoMinScore = 0.7,
            typoTopN = 5
        )
        val on = off.copy(ignoreCase = true)
        val rOff = SimilarityMatchService.findBestMatches(input, candidates, off)
        val rOn = SimilarityMatchService.findBestMatches(input, candidates, on)
        Assert.assertTrue(rOff.isEmpty())
        Assert.assertEquals(listOf("abcd", "AbcE"), rOn.map { it.value })
    }

    @Test
    fun service_extra_1() {
        val input = "planet_modifer"
        val candidates = listOf("planet_army", "planet_modifier")
        val results = SimilarityMatchService.findBestMatches(input, candidates)
        Assert.assertEquals("planet_modifier", results.firstOrNull()?.value)
    }

    @Test
    fun service_extra_2() {
        val input = "triggerred_planet_modifer"
        val candidates = listOf("triggerred_ship_modifier", "triggerred_planet_modifier", "triggerred_country_modifier")
        val results = SimilarityMatchService.findBestMatches(input, candidates)
        Assert.assertEquals("triggerred_planet_modifier", results.firstOrNull()?.value)
    }

    @Test
    fun service_extra_3() {
        val input = "triggerred_modifier"
        val candidates = listOf("triggerred_ship_modifier", "triggerred_planet_modifier", "triggerred_country_modifier")
        val results = SimilarityMatchService.findBestMatches(input, candidates)
        Assert.assertEquals(listOf("triggerred_ship_modifier", "triggerred_planet_modifier", "triggerred_country_modifier"), results.map { it.value })
    }
}
