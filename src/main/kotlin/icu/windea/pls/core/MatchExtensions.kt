@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.match.MatchService
import org.intellij.lang.annotations.Language

/** @see MatchService.matchesPattern */
inline fun String?.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesPattern(this, pattern, ignoreCase)
}

/** @see MatchService.matchesPatterns */
inline fun String?.matchesPatterns(pattern: String, delimiter: Char = ';', ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesPatterns(this, pattern, delimiter, ignoreCase)
}

/** @see MatchService.matchesAntPattern */
inline fun String?.matchesAntPattern(pattern: String, separator: Char = '/', ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return MatchService.matchesAntPattern(this, pattern, separator, ignoreCase, trimSeparator)
}

/** @see MatchService.matchesAntPatterns */
inline fun String?.matchesAntPatterns(pattern: String, delimiter: Char = ';', separator: Char = '/',ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return MatchService.matchesAntPatterns(this, pattern, delimiter, separator, ignoreCase, trimSeparator)
}

/** @see MatchService.matchesRegex */
inline fun String?.matchesRegex(@Language("RegExp") pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesRegex(this, pattern, ignoreCase)
}
