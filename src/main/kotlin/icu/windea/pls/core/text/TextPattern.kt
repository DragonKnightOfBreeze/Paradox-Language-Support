@file:Suppress("unused")

package icu.windea.pls.core.text

import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSuffixOrNull
import icu.windea.pls.core.removeSurroundingOrNull

/**
 * 文本模式。
 *
 * 用于（基于可能的前缀、后缀、分隔符等）进行文本匹配。
 *
 * 作为一种模型策略，既包含用于文本匹配的元数据，也作为对文本匹配逻辑的策略。
 */
interface TextPattern<out R : TextPatternResult> {
    val priority: Int get() = 0
    val orderString: String? get() = null

    fun matches(text: String): R?

    data class Literal(val value: String) : TextPattern<TextPatternResult.Empty> {
        override val priority: Int get() = 100
        override val orderString: String get() = value

        override fun matches(text: String): TextPatternResult.Empty? {
            if (text != value) return null
            return TextPatternResult.Empty
        }
    }

    data class WithPrefix(val prefix: String) : TextPattern<TextPatternResult.Single> {
        override val priority: Int get() = 80
        override val orderString: String get() = prefix

        override fun matches(text: String): TextPatternResult.Single? {
            val result = text.removePrefixOrNull(prefix) ?: return null
            return TextPatternResult.Single(result)
        }
    }

    data class WithSuffix(val suffix: String) : TextPattern<TextPatternResult.Single> {
        override val priority: Int get() = 70

        override fun matches(text: String): TextPatternResult.Single? {
            val result = text.removeSuffixOrNull(suffix) ?: return null
            return TextPatternResult.Single(result)
        }
    }

    data class WithSurrounding(val prefix: String, val suffix: String) : TextPattern<TextPatternResult.Single> {
        override val priority: Int get() = 90
        override val orderString: String get() = prefix

        override fun matches(text: String): TextPatternResult.Single? {
            val result = text.removeSurroundingOrNull(prefix, suffix) ?: return null
            return TextPatternResult.Single(result)
        }
    }

    data class Delimited(val delimiter: String) : TextPattern<TextPatternResult.Pair> {
        override val priority: Int get() = 0

        override fun matches(text: String): TextPatternResult.Pair? {
            val input = text
            val delimiterIndex = input.indexOf(delimiter)
            if (delimiterIndex == -1) return null
            val left = input.substring(0, delimiterIndex)
            val right = input.substring(delimiterIndex + delimiter.length)
            return TextPatternResult.Pair(left, right)
        }
    }

    data class DelimitedWithPrefix(val delimiter: String, val prefix: String) : TextPattern<TextPatternResult.Pair> {
        override val priority: Int get() = 80
        override val orderString: String get() = prefix

        override fun matches(text: String): TextPatternResult.Pair? {
            val input = text.removePrefixOrNull(prefix) ?: return null
            val delimiterIndex = input.indexOf(delimiter)
            if (delimiterIndex == -1) return null
            val left = input.substring(0, delimiterIndex)
            val right = input.substring(delimiterIndex + delimiter.length)
            return TextPatternResult.Pair(left, right)
        }
    }

    data class DelimitedWithSuffix(val delimiter: String, val suffix: String) : TextPattern<TextPatternResult.Pair> {
        override val priority: Int get() = 70

        override fun matches(text: String): TextPatternResult.Pair? {
            val input = text.removeSuffixOrNull(suffix) ?: return null
            val delimiterIndex = input.indexOf(delimiter)
            if (delimiterIndex == -1) return null
            val left = input.substring(0, delimiterIndex)
            val right = input.substring(delimiterIndex + delimiter.length)
            return TextPatternResult.Pair(left, right)
        }
    }

    data class DelimitedWithSurrounding(val delimiter: String, val prefix: String, val suffix: String) : TextPattern<TextPatternResult.Pair> {
        override val priority: Int get() = 90
        override val orderString: String get() = prefix

        override fun matches(text: String): TextPatternResult.Pair? {
            val input = text.removeSurroundingOrNull(prefix, suffix) ?: return null
            val delimiterIndex = input.indexOf(delimiter)
            if (delimiterIndex == -1) return null
            val left = input.substring(0, delimiterIndex)
            val right = input.substring(delimiterIndex + delimiter.length)
            return TextPatternResult.Pair(left, right)
        }
    }

    object Comparator : kotlin.Comparator<TextPattern<*>> {
        override fun compare(a: TextPattern<*>, b: TextPattern<*>): Int {
            return compareValuesBy(a, b, { it.priority }, { it.orderString })
        }
    }
}
