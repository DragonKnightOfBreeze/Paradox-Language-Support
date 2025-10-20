package icu.windea.pls.core

import icu.windea.pls.core.match.MatchService

/**
 * @see MatchService.matchesPattern
 */
fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesPattern(this, pattern, ignoreCase)
}

/**
 * @see MatchService.matchesAntPattern
 */
fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    return MatchService.matchesAntPattern(this, pattern, ignoreCase, trimSeparator)
}

/**
 * @see MatchService.matchesRegexPattern
 */
fun String.matchesRegex(pattern: String, ignoreCase: Boolean = false): Boolean {
    return MatchService.matchesRegexPattern(this, pattern, ignoreCase)
}
