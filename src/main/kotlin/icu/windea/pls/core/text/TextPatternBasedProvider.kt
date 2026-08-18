@file:Suppress("unused")

package icu.windea.pls.core.text

/**
 * 基于文本模式的提供者。
 *
 * @see TextPattern
 */
class TextPatternBasedProvider<T, R : TextPatternResult>(
    val pattern: TextPattern<R>,
    val action: (text: String, matchResult: R) -> T?,
) {
    fun get(text: String): T? {
        val matchResult = pattern.matches(text) ?: return null
        return action(text, matchResult)
    }
}
