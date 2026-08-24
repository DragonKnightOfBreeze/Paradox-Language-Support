package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see TextPatternBasedProvider
 */
class TextPatternBasedProviderTest {
    @Test
    fun get_matches() {
        val provider = TextPatternBasedProvider<String, TextPatternResult.Single>(
            TextPattern.WithPrefix("prefix-")
        ) { _, r -> "value=${r.value}" }
        Assert.assertEquals("value=abc", provider.get("prefix-abc"))
    }

    @Test
    fun get_passesOriginalText() {
        val provider = TextPatternBasedProvider<String, TextPatternResult.Single>(
            TextPattern.WithPrefix("prefix-")
        ) { text, _ -> text }
        Assert.assertEquals("prefix-abc", provider.get("prefix-abc"))
    }

    @Test
    fun get_passesMatchResult() {
        val provider = TextPatternBasedProvider<String, TextPatternResult.Pair>(
            TextPattern.Delimited(":")
        ) { _, r -> "${r.left}|${r.right}" }
        Assert.assertEquals("a|b", provider.get("a:b"))
    }

    @Test
    fun get_notMatching_actionNotInvoked() {
        var invoked = false
        val provider = TextPatternBasedProvider<String, TextPatternResult.Single>(
            TextPattern.WithPrefix("prefix-")
        ) { _, _ -> invoked = true; "result" }
        Assert.assertNull(provider.get("abc"))
        Assert.assertFalse(invoked)
    }

    @Test
    fun get_actionReturnsNull() {
        val provider = TextPatternBasedProvider<String, TextPatternResult.Single>(
            TextPattern.WithPrefix("prefix-")
        ) { _, _ -> null }
        Assert.assertNull(provider.get("prefix-abc"))
    }
}
