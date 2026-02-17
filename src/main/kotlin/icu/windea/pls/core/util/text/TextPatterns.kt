package icu.windea.pls.core.util.text

import icu.windea.pls.core.removeSurroundingOrNull

/**
 * 文本模式。
 */
sealed interface TextPattern<out R : TextPatternMatchResult> {
    fun matches(text: String): R?

    data class Literal(val value: String) : TextPattern<TextPatternMatchResult.Empty> {
        override fun matches(text: String): TextPatternMatchResult.Empty? {
            return if (text == value) TextPatternMatchResult.Empty else null
        }
    }

    data class Parameterized(
        val prefix: String,
        val suffix: String
    ) : TextPattern<TextPatternMatchResult.Single> {
        override fun matches(text: String): TextPatternMatchResult.Single? {
            val payload = text.removeSurroundingOrNull(prefix, suffix) ?: return null
            return TextPatternMatchResult.Single(payload)
        }
    }

    data class Delimited(
        val prefix: String,
        val suffix: String,
        val delimiter: String
    ) : TextPattern<TextPatternMatchResult.Pair> {
        override fun matches(text: String): TextPatternMatchResult.Pair? {
            val payload = text.removeSurroundingOrNull(prefix, suffix) ?: return null
            val delimiterIndex = payload.indexOf(delimiter)
            if (delimiterIndex == -1) return null
            val left = payload.substring(0, delimiterIndex)
            val right = payload.substring(delimiterIndex + delimiter.length)
            return TextPatternMatchResult.Pair(left, right)
        }
    }

    data class WithPrefix(val prefix: String) : TextPattern<TextPatternMatchResult.Single> {
        override fun matches(text: String): TextPatternMatchResult.Single? {
            if (!text.startsWith(prefix)) return null
            return TextPatternMatchResult.Single(text.substring(prefix.length))
        }
    }

    companion object {
        @JvmStatic
        fun from(value: String) = Literal(value)

        @JvmStatic
        fun from(prefix: String, suffix: String) = Parameterized(prefix, suffix)

        @JvmStatic
        fun from(prefix: String, suffix: String, delimiter: String) = Delimited(prefix, suffix, delimiter)

        @JvmStatic
        fun withPrefix(prefix: String) = WithPrefix(prefix)
    }
}

/**
 * 文本模式的匹配结果。
 *
 * @see TextPattern
 */
sealed interface TextPatternMatchResult {
    data object Empty : TextPatternMatchResult

    data class Single(val value: String) : TextPatternMatchResult

    data class Pair(val left: String, val right: String) : TextPatternMatchResult
}

/**
 * 基于文本模式的提供者。
 *
 * @see TextPattern
 */
class TextPatternBasedProvider<T, R : TextPatternMatchResult>(
    val pattern: TextPattern<R>,
    val action: (text: String, matchResult: R) -> T?
) {
    fun get(text: String): T? {
        val matchResult = pattern.matches(text) ?: return null
        return action(text, matchResult)
    }
}

/**
 * 基于一组文本模式的构建器。
 *
 * @see TextPattern
 * @see TextPatternBasedProvider
 */
class TextPatternBasedBuilder<T>(providers: List<TextPatternBasedProvider<T, out TextPatternMatchResult>>) {
    private val providerComparator = compareByDescending<TextPatternBasedProvider<T, *>> {
        when (it.pattern) {
            is TextPattern.Literal -> it.pattern.value
            is TextPattern.Parameterized -> it.pattern.prefix
            is TextPattern.Delimited -> it.pattern.prefix
            is TextPattern.WithPrefix -> it.pattern.prefix
        }
    }.thenBy {
        when (it.pattern) {
            is TextPattern.Literal -> 0
            is TextPattern.Parameterized -> 2
            is TextPattern.Delimited -> 1
            is TextPattern.WithPrefix -> 3
        }
    }

    private val literalProviderMap = providers.asSequence()
        .filter { it.pattern is TextPattern.Literal }
        .associateBy { (it.pattern as TextPattern.Literal).value }
    private val nonLiteralProviders = providers.asSequence()
        .filterNot { it.pattern is TextPattern.Literal }
        .sortedWith(providerComparator)
        .toList()

    fun build(text: String): T? {
        val literalProvider = literalProviderMap[text]
        if (literalProvider != null) {
            return literalProvider.get(text)
        }
        for (provider in nonLiteralProviders) {
            return provider.get(text) ?: continue
        }
        return null
    }
}

