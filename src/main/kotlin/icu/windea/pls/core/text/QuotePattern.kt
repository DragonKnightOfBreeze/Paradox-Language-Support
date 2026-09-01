@file:Suppress("unused")

package icu.windea.pls.core.text

import icu.windea.pls.core.isEscapedCharAt

/**
 * 引号模式。
 *
 * 用于处理文本中的引号，涉及各种断言和修改操作。
 *
 * 包含相关的元数据，同时也作为对断言和修改逻辑的策略。
 *
 * @see QuotePatterns
 */
interface QuotePattern {
    /** 使用的引号字符。 */
    val quoteChar: Char
    /** 处理时是否会先忽略首尾的引号。 */
    val lenient: Boolean

    /** 根据输入的 [text]，检查是否需要首尾的引号。如果 [lenient] 为 `true`，则会忽略首尾的引号。 */
    fun needQuote(text: String): Boolean

    /** 根据输入的 [text]，检查是否可以添加周围的引号。 */
    fun canQuote(text: String): Boolean

    /** 根据输入的 [text]，检查是否可以去除周围的引号。 */
    fun canUnquote(text: String): Boolean

    /** 根据输入的 [text]，检查是否已经以 [quoteChar] 开始。 */
    fun isLeftQuoted(text: String): Boolean

    /** 根据输入的 [text]，检查是否已经以 [quoteChar] 结尾（考虑转义）。 */
    fun isRightQuoted(text: String): Boolean

    /** 根据输入的 [text]，检查是否已经被 [quoteChar] 包围（包括仅一侧存在 [quoteChar] 的情况）。 */
    fun isQuoted(text: String): Boolean

    /** 如果需要首尾的引号，则添加 [text] 周围的引号。如果需要添加，则应同时考虑转义其中的引号。如果 [lenient] 为 `true`，则会忽略首尾的引号。 */
    fun quoteIfNeeded(text: String): String

    /** 添加 [text] 周围的引号。如果需要添加，则应同时考虑转义其中的引号。如果 [lenient] 为 `true`，则会忽略首尾的引号。 */
    fun quote(text: String): String

    /** 去除 [text] 周围的引号。如果需要去除，则应同时考虑反转义其中的引号。 */
    fun unquote(text: String): String

    abstract class Base(
        override val quoteChar: Char,
        override val lenient: Boolean = true,
    ) : QuotePattern {
        abstract fun checkChar(text: String, start: Int, end: Int, index: Int, char: Char): Boolean

        override fun needQuote(text: String): Boolean {
            if (text.isEmpty() || text == quoteChar.toString()) return true
            val leftQuoted = isLeftQuoted(text)
            val rightQuoted = isRightQuoted(text)
            val startOffset = if (lenient && leftQuoted) 1 else 0
            val endOffset = if (lenient && rightQuoted) -1 else 0
            val start = startOffset
            val end = text.length - 1 + endOffset
            var index = start - 1
            while (index < end) {
                val c = text[++index]
                if (checkChar(text, start, end, index, c)) return true
            }
            return false
        }

        override fun canQuote(text: String): Boolean {
            return !isLeftQuoted(text) || !isRightQuoted(text)
        }

        override fun canUnquote(text: String): Boolean {
            return (isLeftQuoted(text) || isRightQuoted(text)) && !needQuote(text)
        }

        override fun isLeftQuoted(text: String): Boolean {
            return text.startsWith(quoteChar)
        }

        override fun isRightQuoted(text: String): Boolean {
            val length = text.length
            return length > 1 && text.endsWith(quoteChar) && !text.isEscapedCharAt(length - 1)
        }

        override fun isQuoted(text: String): Boolean {
            return isLeftQuoted(text) || isRightQuoted(text)
        }

        override fun quoteIfNeeded(text: String): String {
            if (isLeftQuoted(text) && isRightQuoted(text)) return text
            if (!needQuote(text)) return text
            return quote(text)
        }

        override fun quote(text: String): String {
            if (text.isEmpty()) return "$quoteChar$quoteChar"
            val leftQuoted = isLeftQuoted(text)
            val rightQuoted = isRightQuoted(text)
            if (leftQuoted && rightQuoted) return text
            val startOffset = if (lenient && leftQuoted) 1 else 0
            val endOffset = if (lenient && rightQuoted) -1 else 0
            val start = startOffset
            val end = text.length - 1 + endOffset
            var index = start - 1
            val builder = StringBuilder()
            builder.append(quoteChar)
            while (index < end) {
                val c = text[++index]
                if (c == quoteChar && !text.isEscapedCharAt(index)) builder.append('\\')
                builder.append(c)
            }
            builder.append(quoteChar)
            return builder.toString()
        }

        override fun unquote(text: String): String {
            if (text.isEmpty()) return ""
            val leftQuoted = isLeftQuoted(text)
            val rightQuoted = isRightQuoted(text)
            if (!leftQuoted && !rightQuoted) return text
            val startOffset = if (leftQuoted) 1 else 0
            val endOffset = if (rightQuoted) -1 else 0
            val start = startOffset
            val end = text.length - 1 + endOffset
            var index = start - 1
            val builder = StringBuilder()
            var offset = if (leftQuoted) 1 else 0
            while (index < end) {
                val c = text[++index]
                if (c == quoteChar && text.isEscapedCharAt(index)) builder.deleteCharAt(index - 1 - offset++)
                builder.append(c)
            }
            return builder.toString()
        }
    }
}
