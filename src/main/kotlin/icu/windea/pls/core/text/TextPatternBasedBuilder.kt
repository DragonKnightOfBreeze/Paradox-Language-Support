@file:Suppress("unused")

package icu.windea.pls.core.text

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized

/**
 * 基于一组文本模式的构建器。
 *
 * 构建时，首先会尝试获取对应的字面量模式（[TextPattern.Literal]）以进行快速匹配，然后再尝试依次匹配排序后的一组模式。
 *
 * @see TextPattern
 * @see TextPatternBasedProvider
 */
@Optimized
class TextPatternBasedBuilder<T>(
    val providers: List<TextPatternBasedProvider<T, *>>
) {
    private val literalProviderMap by lazy { computeLiteralProviderMap().optimized() }
    private val nonLiteralProviders by lazy { computeNonLiteralProviders().optimized() }

    private fun computeLiteralProviderMap(): Map<String, TextPatternBasedProvider<T, *>> {
        // fast match for literal patterns
        return providers.filterFast { it.pattern is TextPattern.Literal }
            .associateBy { (it.pattern as TextPattern.Literal).value }
    }

    private fun computeNonLiteralProviders(): List<TextPatternBasedProvider<T, *>> {
        // sorted by patterns descending (priority + literal/prefix)
        return providers.filterFast { it.pattern !is TextPattern.Literal }
            .sortedWith { a, b -> TextPattern.Comparator.compare(b.pattern, a.pattern) }
    }

    fun build(text: String): T? {
        val literalProvider = literalProviderMap[text]
        if (literalProvider != null) {
            return literalProvider.get(text)
        }
        nonLiteralProviders.forEachFast f@{ provider ->
            return provider.get(text) ?: return@f
        }
        return null
    }
}
