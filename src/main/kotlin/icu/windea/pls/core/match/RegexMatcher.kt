package icu.windea.pls.core.match

import icu.windea.pls.core.cache.CacheBuilder

/**
 * 基于正则的匹配器。
 *
 * 对输入直接应用正则表达式（内部带简单缓存）。
 */
object RegexMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的正则（[pattern]）。
     *
     * @param ignoreCase 是否忽略大小写。
     */
    fun matches(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
        return fullMatch(input, pattern, ignoreCase)
    }

    private val regexCache1 = CacheBuilder("maximumSize=10000")
        .build<String, Regex> { key -> key.toRegex() }
    private val regexCache2 = CacheBuilder("maximumSize=10000")
        .build<String, Regex> { key -> key.toRegex(RegexOption.IGNORE_CASE) }

    private fun fullMatch(input: String, pattern: String, ignoreCase: Boolean): Boolean {
        if (pattern.isEmpty() && input.isNotEmpty()) return false
        val cache = if (ignoreCase) regexCache2 else regexCache1
        val path0 = input
        val pattern0 = pattern
        return cache.get(pattern0).matches(path0)
    }
}
