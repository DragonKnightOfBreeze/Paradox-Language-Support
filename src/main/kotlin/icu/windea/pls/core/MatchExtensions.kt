@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.match.MatchService
import org.intellij.lang.annotations.Language

/** @see MatchService.matchesPattern */
inline fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesPattern(this, pattern, ignoreCase)
}

/** @see MatchService.matchesPatterns */
inline fun String.matchesPatterns(pattern: String, delimiter: Char = ';', ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesPatterns(this, pattern, delimiter, ignoreCase)
}

/** @see MatchService.matchesAntPattern */
inline fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return MatchService.matchesAntPattern(this, pattern, ignoreCase, trimSeparator)
}

/** @see MatchService.matchesAntPatterns */
inline fun String.matchesAntPatterns(pattern: String, delimiter: Char = ';', ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return MatchService.matchesAntPatterns(this, pattern, delimiter, ignoreCase, trimSeparator)
}

/** @see MatchService.matchesRegexPattern */
inline fun String.matchesRegex(@Language("RegExp") pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesRegexPattern(this, pattern, ignoreCase)
}
