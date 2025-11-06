@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.match.MatchService

/** @see MatchService.matchesPattern */
inline fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesPattern(this, pattern, ignoreCase)
}

/** @see MatchService.matchesAntPattern */
inline fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return MatchService.matchesAntPattern(this, pattern, ignoreCase, trimSeparator)
}

/** @see MatchService.matchesRegexPattern */
inline fun String.matchesRegex(pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesRegexPattern(this, pattern, ignoreCase)
}
