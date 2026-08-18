@file:Suppress("unused")

package icu.windea.pls.core.text

import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.removeSuffixOrNull
import icu.windea.pls.core.removeSurroundingOrNull

/**
 * 文本模式。
 */
sealed interface TextPattern<out R : TextPatternResult> {
    fun matches(text: String): R?

    data class Literal(val value: String) : TextPattern<TextPatternResult.Empty> {
        override fun matches(text: String): TextPatternResult.Empty? {
            if (text != value) return null
            return TextPatternResult.Empty
        }
    }

    data class WithPrefix(val prefix: String) : TextPattern<TextPatternResult.Single> {
        override fun matches(text: String): TextPatternResult.Single? {
            val result = text.removePrefixOrNull(text) ?: return null
            return TextPatternResult.Single(result)
        }
    }

    data class WithSuffix(val suffix: String) : TextPattern<TextPatternResult.Single> {
        override fun matches(text: String): TextPatternResult.Single? {
            val result = text.removeSuffixOrNull(text) ?: return null
            return TextPatternResult.Single(result)
        }
    }

    data class WithSurrounding(val prefix: String, val suffix: String) : TextPattern<TextPatternResult.Single> {
        override fun matches(text: String): TextPatternResult.Single? {
            val result = text.removeSurroundingOrNull(prefix, suffix) ?: return null
            return TextPatternResult.Single(result)
        }
    }

    data class Delimited(val delimiter: String) : TextPattern<TextPatternResult.Pair> {
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
            return compareValuesBy(a, b, { selectPriority(it) }, { selectLiteralOrPrefix(it) })
        }

        private fun selectPriority(pattern: TextPattern<TextPatternResult>): Int {
            return when (pattern) {
                is Literal -> 100
                is WithPrefix -> 80
                is WithSuffix -> 70
                is WithSurrounding -> 90
                is Delimited -> 0
                is DelimitedWithPrefix -> 80
                is DelimitedWithSuffix -> 70
                is DelimitedWithSurrounding -> 90
            }
        }

        private fun selectLiteralOrPrefix(pattern: TextPattern<TextPatternResult>): String {
            return when (pattern) {
                is Literal -> pattern.value
                is WithPrefix -> pattern.prefix
                is WithSuffix -> ""
                is WithSurrounding -> pattern.prefix
                is Delimited -> ""
                is DelimitedWithPrefix -> pattern.prefix
                is DelimitedWithSuffix -> ""
                is DelimitedWithSurrounding -> pattern.prefix
            }
        }
    }
}
