package icu.windea.pls.core.util.text

import icu.windea.pls.core.removeSurroundingOrNull

/**
 * 文本模式。
 */
sealed interface TextPattern<out R : TextPatternMatchResult> {
    val prefix: String? get() = null
    val suffix: String? get() = null
    val delimiter: String? get() = null

    fun matches(text: String): R?

    operator fun component1(): String = prefix ?: error("Prefix is not available for pattern: $this")
    operator fun component2(): String = suffix ?: error("Suffix is not available for pattern: $this")
    operator fun component3(): String = delimiter ?: error("Delimiter is not available for pattern: $this")

    data class Literal(val value: String) : TextPattern<TextPatternMatchResult.Empty> {
        override fun matches(text: String): TextPatternMatchResult.Empty? {
            return if (text == value) TextPatternMatchResult.Empty else null
        }
    }

    data class Parameterized(
        override val prefix: String,
        override val suffix: String
    ) : TextPattern<TextPatternMatchResult.Single> {
        override fun matches(text: String): TextPatternMatchResult.Single? {
            val payload = text.removeSurroundingOrNull(prefix, suffix) ?: return null
            return TextPatternMatchResult.Single(payload)
        }
    }

    data class Delimited(
        override val prefix: String,
        override val suffix: String,
        override val delimiter: String
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

    data class WithPrefix(override val prefix: String) : TextPattern<TextPatternMatchResult.Single> {
        override fun matches(text: String): TextPatternMatchResult.Single? {
            if (!text.startsWith(prefix)) return null
            return TextPatternMatchResult.Single(text.substring(prefix.length))
        }
    }

    companion object {
        @JvmStatic
        fun from(value: String): TextPattern<TextPatternMatchResult.Empty> = Literal(value)

        @JvmStatic
        fun from(prefix: String, suffix: String): TextPattern<TextPatternMatchResult.Single> = Parameterized(prefix, suffix)

        @JvmStatic
        fun from(prefix: String, suffix: String, delimiter: String): TextPattern<TextPatternMatchResult.Pair> = Delimited(prefix, suffix, delimiter)

        @JvmStatic
        fun withPrefix(prefix: String): TextPattern<TextPatternMatchResult.Single> = WithPrefix(prefix)
    }
}

/**
 * 文本模式的匹配结果。
 */
sealed interface TextPatternMatchResult {
    data object Empty : TextPatternMatchResult

    data class Single(val value: String) : TextPatternMatchResult

    data class Pair(val left: String, val right: String) : TextPatternMatchResult
}

/**
 * 基于一组文本模式的构建器。
 */
class TextPatternBasedBuilder<T>(rules: List<Rule<T, out TextPatternMatchResult>>) {
    data class Rule<T, R : TextPatternMatchResult>(
        val pattern: TextPattern<R>,
        val priority: Double = 0.0,
        val build: (text: String, matchResult: R) -> T
    )

    private val literalRuleMap = rules
        .asSequence()
        .filter { it.pattern is TextPattern.Literal }
        .associateBy { (it.pattern as TextPattern.Literal).value }

    private val nonLiteralRules = rules
        .asSequence()
        .filterNot { it.pattern is TextPattern.Literal }
        .sortedByDescending { it.priority }
        .toList()

    fun build(text: String): T? {
        val literalRule = literalRuleMap[text]
        if (literalRule != null) {
            @Suppress("UNCHECKED_CAST")
            val typedRule = literalRule as Rule<T, TextPatternMatchResult.Empty>
            return typedRule.build(text, TextPatternMatchResult.Empty)
        }

        for (rule in nonLiteralRules) {
            val matchResult = rule.pattern.matches(text) ?: continue
            @Suppress("UNCHECKED_CAST")
            val typedRule = rule as Rule<T, TextPatternMatchResult>
            @Suppress("UNCHECKED_CAST")
            return typedRule.build.invoke(text, matchResult)
        }
        return null
    }
}

@Suppress("unused")
typealias TextPattenBasedBuilder<T> = TextPatternBasedBuilder<T>
