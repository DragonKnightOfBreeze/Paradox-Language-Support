@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.text.EscapePattern
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns

// QuotePattern

/** @see QuotePattern.needQuote */
inline fun String.needQuote(quotePattern: QuotePattern = QuotePatterns.Default): Boolean = quotePattern.needQuote(this)

/** @see QuotePattern.canQuote */
inline fun String.canQuote(quotePattern: QuotePattern = QuotePatterns.Default): Boolean = quotePattern.canQuote(this)

/** @see QuotePattern.canUnquote */
inline fun String.canUnquote(quotePattern: QuotePattern = QuotePatterns.Default): Boolean = quotePattern.canUnquote(this)

/** @see QuotePattern.isLeftQuoted */
inline fun String.isLeftQuoted(quotePattern: QuotePattern = QuotePatterns.Default): Boolean = quotePattern.isLeftQuoted(this)

/** @see QuotePattern.isRightQuoted */
inline fun String.isRightQuoted(quotePattern: QuotePattern = QuotePatterns.Default): Boolean = quotePattern.isRightQuoted(this)

/** @see QuotePattern.isQuoted */
inline fun String.isQuoted(quotePattern: QuotePattern = QuotePatterns.Default): Boolean = quotePattern.isQuoted(this)

/** @see QuotePattern.quoteIfNeeded */
inline fun String.quoteIfNeeded(quotePattern: QuotePattern = QuotePatterns.Default): String = quotePattern.quoteIfNeeded(this)

/** @see QuotePattern.quote */
inline fun String.quote(quotePattern: QuotePattern = QuotePatterns.Default): String = quotePattern.quote(this)

/** @see QuotePattern.unquote */
inline fun String.unquote(quotePattern: QuotePattern = QuotePatterns.Default): String = quotePattern.unquote(this)

/**
 * 转换引号之间的文本内容，并保留可能存在的周围的引号。
 */
fun String.transformAndKeepQuotes(quotePattern: QuotePattern = QuotePatterns.Default, transform: (String) -> String): String {
    val text = this
    if (text.isEmpty()) return ""
    val leftQuoted = text.isLeftQuoted(quotePattern)
    val rightQuoted = text.isRightQuoted(quotePattern)
    if(!leftQuoted && !rightQuoted) return this
    val startOffset = if (leftQuoted) 1 else 0
    val rightOffset = if (rightQuoted) -1 else 0
    return buildString {
        if(leftQuoted) append(quotePattern.quoteChar)
        append(transform(text.substring(startOffset, text.length + rightOffset)))
        if(rightQuoted) append(quotePattern.quoteChar)
    }
}

/**
 * 去除文本范围首尾的引号。返回处理后的新的文本范围。
 */
fun TextRange.unquote(text: String, quotePattern: QuotePattern = QuotePatterns.Default): TextRange {
    if (text.isEmpty()) return TextRange.EMPTY_RANGE
    val leftQuoted = text.isLeftQuoted(quotePattern)
    val rightQuoted = text.isRightQuoted(quotePattern)
    if(!leftQuoted && !rightQuoted) return this
    val startOffset = if (leftQuoted) startOffset + 1 else startOffset
    val endOffset = if (rightQuoted) endOffset - 1 else endOffset
    return TextRange.create(startOffset, endOffset)
}

// EscapePattern

/** @see EscapePattern.escape */
inline fun String.escape(escapePattern: EscapePattern): String = escapePattern.escape(this)

/** @see EscapePattern.unescape */
inline fun String.unescape(escapePattern: EscapePattern): String = escapePattern.unescape(this)
