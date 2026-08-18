@file:Suppress("unused")

package icu.windea.pls.core.text

/**
 * 文本模式的匹配结果。
 *
 * @see TextPattern
 */
sealed interface TextPatternResult {
    data object Empty : TextPatternResult

    data class Single(val value: String) : TextPatternResult

    data class Pair(val left: String, val right: String) : TextPatternResult
}
