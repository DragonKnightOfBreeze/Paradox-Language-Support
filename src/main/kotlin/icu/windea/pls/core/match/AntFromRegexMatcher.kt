package icu.windea.pls.core.match

import icu.windea.pls.core.trimFast
import icu.windea.pls.core.util.CacheBuilder

/**
 * 基于 ANT 路径模式的匹配器。
 *
 * 说明：
 * - 使用 `/` 作为路径分隔符。
 * - `?` 匹配单个子路径中的单个字符。
 * - `*` 匹配单个子路径中的任意个字符。
 * - `**` 匹配任意个子路径。
 *
 * 基于正则实现，已废弃，建议改用 [AntMatcher]。
 */
@Deprecated(message = "", replaceWith = ReplaceWith("AntMatcher"))
object AntFromRegexMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的 ANT 路径模式([pattern])。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param trimSeparator 是否去除首尾的分隔符。
     */
    fun matches(input: String, pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
        return fullMatch(input, pattern, ignoreCase, trimSeparator)
    }

    private val regexCache1 = CacheBuilder("maximumSize=10000")
        .build<String, Regex> { key -> key.toRegex() }

    private val regexCache2 = CacheBuilder("maximumSize=10000")
        .build<String, Regex> { key -> key.toRegex(RegexOption.IGNORE_CASE) }

    private fun fullMatch(input: String, pattern: String, ignoreCase: Boolean, trimSeparator: Boolean): Boolean {
        if (input.isNotEmpty() && pattern.isEmpty()) return false
        val input0 = if (trimSeparator) input.trimFast('/') else input
        val pattern0 = if (trimSeparator) pattern.trimFast('/') else pattern
        if (input0.isEmpty() && (pattern0 == "**" || pattern0.all { it == '*' })) return true
        if (pattern0 == "**") return true
        val cache = if (ignoreCase) regexCache2 else regexCache1
        val regex0 = pattern0.antPatternToRegexString()
        return cache.get(regex0).matches(input0)
    }

    private fun String.antPatternToRegexString(): String {
        val s = this
        var r = buildString {
            append("\\Q")
            var i = 0
            while (i < s.length) {
                val c = s[i]
                when {
                    c == '*' -> {
                        val nc = s.getOrNull(i + 1)
                        if (nc == '*') {
                            i++
                            append("\\E.*\\Q")
                        } else {
                            append("\\E[^/]*\\Q")
                        }
                    }
                    c == '?' -> append("\\E[^/]\\Q")
                    else -> append(c)
                }
                i++
            }
            append("\\E")
        }
        r = r.replace("\\E\\Q", "")
        r = r.replace("/\\E.*\\Q/", "\\E(/[^/]*)*\\Q")
        return r
    }
}
