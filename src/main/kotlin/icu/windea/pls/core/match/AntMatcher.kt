package icu.windea.pls.core.match

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.splitFast
import icu.windea.pls.core.trimFast

/**
 * ANT 路径模式的匹配器。
 *
 * 说明：
 * - `?` 匹配单个子路径中的单个字符。
 * - `*` 匹配单个子路径中的任意个字符。
 * - `**` 匹配任意个子路径。
 *
 * 备注：
 * - 基于非正则算法实现，拥有更加清晰的实现代码，以及通常更好的性能。
 */
@Optimized
object AntMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的 ANT 路径模式（[pattern]）。
     *
     * @param separator 使用的路径分隔符。默认为 `/`。
     * @param ignoreCase 是否忽略大小写。默认为 `false`。
     * @param trimSeparator 是否去除首尾的路径分隔符。默认为 `true`。
     */
    fun matches(input: String, pattern: String, separator: Char = '/', ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
        val input = if (trimSeparator) input.trimFast(separator) else input
        val pattern = if (trimSeparator) pattern.trimFast(separator) else pattern
        return fullMatch(input, pattern, separator, ignoreCase)
    }

    // 与基于正则的实现 `AntFromRegexMatcher` 相比，本实现并非在所有场景下都更快。
    // - 在短路径、缓存已热且只做单模式匹配时，两者接近。
    // - 在深层成员路径、长文件路径、大量唯一模式等场景下通常更快。
    // - 在“依次尝试多个简单模式并提前命中”的场景下，正则实现可能更快。
    // 选择本实现除了性能之外，还考虑纯 Kotlin、无正则依赖、语义清晰、行为可预测、调试简单以及不依赖静态正则缓存等因素。

    private fun fullMatch(input: String, pattern: String, separator: Char, ignoreCase: Boolean): Boolean {
        if (input.isNotEmpty() && pattern.isEmpty()) return false
        if (input.isEmpty() && (pattern == "**" || pattern.all { it == '*' })) return true
        if (pattern == "**") return true
        val inputTokens = if (input.isEmpty()) emptyList() else input.splitFast(separator)
        val patternTokens = if (pattern.isEmpty()) emptyList() else pattern.splitFast(separator)
        fun match(i: Int, pi: Int): Boolean {
            var i0 = i
            var pi0 = pi
            while (i0 < inputTokens.size && pi0 < patternTokens.size) {
                when {
                    patternTokens[pi0] == "**" -> {
                        if (match(i0, pi0 + 1)) return true
                        if (match(i0 + 1, pi0)) return true
                        return false
                    }
                    segmentMatch(inputTokens[i0], patternTokens[pi0], ignoreCase) -> {
                        i0++; pi0++
                    }
                    else -> return false
                }
            }
            while (pi0 < patternTokens.size && patternTokens[pi0] == "**") pi0++
            return i0 == inputTokens.size && pi0 == patternTokens.size
        }
        return match(0, 0)
    }

    private fun segmentMatch(input: String, pattern: String, ignoreCase: Boolean): Boolean {
        var i = 0
        var pi = 0
        var star = -1
        var match = 0
        while (i < input.length) {
            when {
                pi < pattern.length && (pattern[pi] == '?' || pattern[pi].equals(input[i], ignoreCase)) -> {
                    i++; pi++
                }
                pi < pattern.length && pattern[pi] == '*' -> {
                    match = i; star = pi++
                }
                star != -1 -> {
                    i = ++match; pi = star + 1
                }
                else -> return false
            }
        }
        while (pi < pattern.length && pattern[pi] == '*') pi++
        return pi == pattern.length
    }
}
