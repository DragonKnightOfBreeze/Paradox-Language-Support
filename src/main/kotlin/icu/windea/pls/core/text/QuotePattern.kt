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

    /** 根据输入的 [text]，检查是否（绝对）需要首尾的引号。 */
    fun needQuote(text: String): Boolean

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

    /** 如果（绝对）需要首尾的引号，则添加 [text] 周围的引号（考虑转义）。 */
    fun quoteIfNeeded(text: String): String

    /** 添加 [text] 周围的引号（考虑转义）。 */
    fun quote(text: String): String

    /** 去除 [text] 周围的引号（考虑转义）。 */
    fun unquote(text: String): String

    abstract class Base(
        override val quoteChar: Char,
        override val lenient: Boolean = true,
    ) : QuotePattern {
        abstract fun checkChar(char: Char): Boolean

        override fun needQuote(text: String): Boolean {
            val s = text
            if (s.isEmpty() || s == quoteChar.toString()) return true
            var index = -1
            val length = text.length
            while (index < length - 1) {
                val c = text[++index]
                if (lenient && (index == 0 || index == length - 1) && c == quoteChar) continue
                if (checkChar(c)) return true
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
            val start = isLeftQuoted(text)
            val end = isRightQuoted(text)
            if (start && end) return text
            return buildString {
                append(quoteChar)
                var index = -1
                val length = text.length
                while (index < length - 1) {
                    val c = text[++index]
                    if (lenient && start && index == 0) continue
                    if (lenient && end && index == length - 1) continue
                    if (c == quoteChar && !text.isEscapedCharAt(index)) append('\\')
                    append(c)
                }
                append(quoteChar)
            }
        }

        override fun unquote(text: String): String {
            if (text.isEmpty()) return ""
            val start = isLeftQuoted(text)
            val end = isRightQuoted(text)
            if (!start && !end) return text
            var offset = if (start) 1 else 0
            return buildString {
                var index = -1
                val length = text.length
                while (index < length - 1) {
                    val c = text[++index]
                    if (start && index == 0) continue
                    if (end && index == length - 1) continue
                    if (c == quoteChar && text.isEscapedCharAt(index)) deleteCharAt(index - 1 - offset++)
                    append(c)
                }
            }
        }
    }
}
