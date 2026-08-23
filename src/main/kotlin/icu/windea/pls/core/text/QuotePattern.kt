@file:Suppress("unused")

package icu.windea.pls.core.text

import icu.windea.pls.core.isEscapedCharAt

/**
 * 引号模式。
 *
 * 用于处理文本中的引号，涉及各种断言和修改操作。
 *
 * 作为一种模型策略，既包含相关的元数据，也作为对断言和修改逻辑的策略。
 *
 * @see QuotePatterns
 */
interface QuotePattern {
    val quoteChar: Char

    /** 根据输入的 [text]，检查是否（绝对）需要首尾的引号，如果 [lenient] 为 `true`，则处理时会先忽略首尾的引号。 */
    fun needQuote(text: String, lenient: Boolean = true): Boolean

    /** 根据输入的 [text]，检查是否可以添加周围的引号。 */
    fun canQuote(text: String): Boolean

    /** 根据输入的 [text]，检查是否可以去除周围的引号。 */
    fun canUnquote(text: String): Boolean

    /** 根据输入的 [text]，检查是否已经以 [quoteChar] 开始（考虑转义）。 */
    fun isLeftQuoted(text: String): Boolean

    /** 根据输入的 [text]，检查是否已经以 [quoteChar] 结尾（考虑转义）。 */
    fun isRightQuoted(text: String): Boolean

    /** 根据输入的 [text]，检查是否已经被 [quoteChar] 包围（包括仅一侧存在 [quoteChar] 的情况）。 */
    fun isQuoted(text: String): Boolean

    /** 添加 [text] 周围的引号（考虑转义）。如果 [lenient] 为 `true`，则处理时会先忽略首尾的引号。 */
    fun quote(text: String, lenient: Boolean = true): String

    /** 去除 [text] 周围的引号（考虑转义）。 */
    fun unquote(text: String): String

    /** 如果（绝对）需要首尾的引号，则添加 [text] 周围的引号（考虑转义）。 */
    fun quoteIfNeeded(text: String): String

    abstract class Base(override val quoteChar: Char) : QuotePattern {
        abstract fun checkUnquotedChar(char: Char): Boolean

        override fun needQuote(text: String, lenient: Boolean): Boolean {
            val s = text
            if (s.isEmpty() || s == quoteChar.toString()) return true
            val lastIndex = s.lastIndex
            s.forEachIndexed f@{ i, c ->
                if (lenient && (i == 0 || i == lastIndex) && c == quoteChar) return@f
                if (checkUnquotedChar(c)) return true
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

        override fun quote(text: String, lenient: Boolean): String {
            // TODO 3.0.2 optimize memory: do not create build string at all if not necessary
            if (text.isEmpty()) return "$quoteChar$quoteChar"
            val start = isLeftQuoted(text)
            val end = isRightQuoted(text)
            if (start && end) return text
            return buildString {
                append(quoteChar)
                val lastIndex = text.lastIndex
                text.forEachIndexed f@{ i, c ->
                    if (lenient && start && i == 0) return@f
                    if (lenient && end && i == lastIndex) return@f
                    if (c == quoteChar && !text.isEscapedCharAt(i)) append('\\')
                    append(c)
                }
                append(quoteChar)
            }
        }

        override fun unquote(text: String): String {
            // TODO 3.0.2 optimize memory: do not create build string at all if not necessary
            if (text.isEmpty()) return ""
            val start = isLeftQuoted(text)
            val end = isRightQuoted(text)
            if (!start && !end) return text
            var offset = if (start) 1 else 0
            return buildString {
                val lastIndex = text.lastIndex
                text.forEachIndexed f@{ i, c ->
                    if (start && i == 0) return@f
                    if (end && i == lastIndex) return@f
                    if (c == quoteChar && text.isEscapedCharAt(i)) deleteCharAt(i - 1 - offset++)
                    append(c)
                }
            }
        }

        override fun quoteIfNeeded(text: String): String {
            if (isLeftQuoted(text) && isRightQuoted(text)) return text
            if (!needQuote(text)) return text
            return quote(text, lenient = true)
        }
    }
}
