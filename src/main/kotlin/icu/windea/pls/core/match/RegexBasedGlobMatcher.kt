package icu.windea.pls.core.match

import icu.windea.pls.core.cache.CacheBuilder

/**
 * GLOB 模式的匹配器。
 *
 * 说明：
 * - `?` 匹配单个字符。
 * - `*` 匹配任意个字符。
 *
 * 备注：
 * - 基于正则实现，已废弃，建议改为使用 [GlobMatcher]。
 */
@Deprecated("Use `GlobMatcher` instead", ReplaceWith("GlobMatcher", "icu.windea.pls.core.match.GlobMatcher"))
object RegexBasedGlobMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的 GLOB 模式（[pattern]）。
     *
     * @param ignoreCase 是否忽略大小写。默认为 `false`。
     */
    fun matches(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
        return fullMatch(input, pattern, ignoreCase)
    }

    // 使用正则缓存以优化匹配性能（根据是否忽略大小写，使用不同的缓存）。

    private val regexCache1 = CacheBuilder("maximumSize=10000")
        .build<String, Regex> { key -> key.toRegex(setOf(RegexOption.DOT_MATCHES_ALL)) }
    private val regexCache2 = CacheBuilder("maximumSize=10000")
        .build<String, Regex> { key -> key.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) }

    private fun fullMatch(input: String, pattern: String, ignoreCase: Boolean): Boolean {
        if (pattern.isEmpty() && input.isNotEmpty()) return false
        if (pattern == "*") return true
        val cache = if (ignoreCase) regexCache2 else regexCache1
        val regexString = pattern.toRegexString()
        return cache.get(regexString).matches(input)
    }

    private fun String.toRegexString(): String {
        val s = this
        return buildString {
            append("\\Q")
            for (c in s) {
                when (c) {
                    '*' -> append("\\E.*\\Q")
                    '?' -> append("\\E.\\Q")
                    else -> append(c)
                }
            }
            append("\\E")
        }.replace("\\E\\Q", "")
    }
}
