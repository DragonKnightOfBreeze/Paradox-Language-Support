@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.text.QuotePattern

// QuotePattern

/** @see QuotePattern.needQuote */
inline fun String.needQuote(quotePattern: QuotePattern = QuotePattern.Default, lenient: Boolean = true): Boolean = quotePattern.needQuote(this, lenient)

/** @see QuotePattern.canQuote */
inline fun String.canQuote(quotePattern: QuotePattern = QuotePattern.Default): Boolean = quotePattern.canQuote(this)

/** @see QuotePattern.canUnquote */
inline fun String.canUnquote(quotePattern: QuotePattern = QuotePattern.Default): Boolean = quotePattern.canUnquote(this)

/** @see QuotePattern.isLeftQuoted */
inline fun String.isLeftQuoted(quotePattern: QuotePattern = QuotePattern.Default): Boolean = quotePattern.isLeftQuoted(this)

/** @see QuotePattern.isRightQuoted */
inline fun String.isRightQuoted(quotePattern: QuotePattern = QuotePattern.Default): Boolean = quotePattern.isRightQuoted(this)

/** @see QuotePattern.isQuoted */
inline fun String.isQuoted(quotePattern: QuotePattern = QuotePattern.Default): Boolean = quotePattern.isQuoted(this)

/** @see QuotePattern.quote */
inline fun String.quote(quotePattern: QuotePattern = QuotePattern.Default, lenient: Boolean = true): String = quotePattern.quote(this, lenient)

/** @see QuotePattern.unquote */
inline fun String.unquote(quotePattern: QuotePattern = QuotePattern.Default): String = quotePattern.unquote(this)

/** @see QuotePattern.quoteIfNeeded */
inline fun String.quoteIfNeeded(quotePattern: QuotePattern = QuotePattern.Default): String = quotePattern.quoteIfNeeded(this)
