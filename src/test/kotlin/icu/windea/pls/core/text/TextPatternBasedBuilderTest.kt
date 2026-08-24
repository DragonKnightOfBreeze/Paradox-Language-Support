package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see TextPatternBasedBuilder
 */
class TextPatternBasedBuilderTest {
    private fun <R : TextPatternResult> provider(pattern: TextPattern<R>, action: (R) -> String?): TextPatternBasedProvider<String, R> {
        return TextPatternBasedProvider(pattern) { _, r -> action(r) }
    }

    private fun builder(vararg providers: TextPatternBasedProvider<String, *>): TextPatternBasedBuilder<String> {
        return TextPatternBasedBuilder(providers.toList())
    }

    @Test
    fun build_emptyProviders() {
        Assert.assertNull(builder().build("anything"))
    }

    @Test
    fun build_literalMatch() {
        val b = builder(provider(TextPattern.Literal("abc")) { "literal" })
        Assert.assertEquals("literal", b.build("abc"))
        Assert.assertNull(b.build("abd"))
    }

    @Test
    fun build_literalPrecedence() {
        // 字面量模式即使注册顺序靠后，也应优先于非字面量模式匹配
        val b = builder(
            provider(TextPattern.WithPrefix("a")) { "prefix" },
            provider(TextPattern.Literal("abc")) { "literal" },
        )
        Assert.assertEquals("literal", b.build("abc"))
    }

    @Test
    fun build_priorityOrdering() {
        // 非字面量模式按优先级降序匹配：WithSurrounding(90) 先于 WithPrefix(80)
        val b = builder(
            provider(TextPattern.WithPrefix("a")) { "prefix" },
            provider(TextPattern.WithSurrounding("a", "c")) { "surrounding" },
        )
        Assert.assertEquals("surrounding", b.build("abc"))
    }

    @Test
    fun build_samePriority_keepsInsertionOrder() {
        // 相同优先级（以及相同 orderString）时，保持注册顺序，首个非空结果胜出
        val b = builder(
            provider(TextPattern.WithPrefix("a")) { "first" },
            provider(TextPattern.WithPrefix("a")) { "second" },
        )
        Assert.assertEquals("first", b.build("abc"))
    }

    @Test
    fun build_fallsThroughOnNullAction() {
        // 非字面量模式的 action 返回 null 时，回退到下一个匹配的模式
        val b = builder(
            provider(TextPattern.WithSurrounding("a", "c")) { null },
            provider(TextPattern.WithPrefix("a")) { "prefix" },
        )
        Assert.assertEquals("prefix", b.build("abc"))
    }

    @Test
    fun build_literalNullFallsThrough() {
        // 字面量模式的 action 返回 null 时，也应回退到非字面量模式
        val b = builder(
            provider(TextPattern.Literal("abc")) { null },
            provider(TextPattern.WithPrefix("a")) { "prefix" },
        )
        Assert.assertEquals("prefix", b.build("abc"))
    }

    @Test
    fun build_duplicateLiteralValue_lastWins() {
        // 相同字面量值时，后注册的提供者覆盖先注册的（associateBy 语义）
        val b = builder(
            provider(TextPattern.Literal("abc")) { "first" },
            provider(TextPattern.Literal("abc")) { "second" },
        )
        Assert.assertEquals("second", b.build("abc"))
    }

    @Test
    fun build_noMatch() {
        val b = builder(
            provider(TextPattern.Literal("abc")) { "literal" },
            provider(TextPattern.WithPrefix("x")) { "prefix" },
        )
        Assert.assertNull(b.build("def"))
    }
}
