package icu.windea.pls.core.match

import icu.windea.pls.core.splitFast
import icu.windea.pls.core.trimFast

/**
 * 基于 ANT 路径模式的匹配器。
 *
 * 说明：
 * - 使用 `/` 作为路径分隔符。
 * - `?` 匹配单个子路径中的单个字符。
 * - `*` 匹配单个子路径中的任意个字符。
 * - `**` 匹配任意个子路径。
 *
 * 采用非正则算法实现，耗时约为基于正则时的一半。
 */
object AntMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的 ANT 路径模式([pattern])。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param trimSeparator 是否去除首尾的分隔符。
     */
    fun matches(input: String, pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
        return fullMatch(input, pattern, ignoreCase, trimSeparator)
    }

    private fun fullMatch(input: String, pattern: String, ignoreCase: Boolean, trimSeparator: Boolean): Boolean {
        if (input.isNotEmpty() && pattern.isEmpty()) return false
        val input0 = if (trimSeparator) input.trimFast('/') else input
        val pattern0 = if (trimSeparator) pattern.trimFast('/') else pattern
        if (input0.isEmpty() && (pattern0 == "**" || pattern0.all { it == '*' })) return true
        if (pattern0 == "**") return true
        val inputTokens = if (input0.isEmpty()) emptyList() else input0.splitFast('/')
        val patternTokens = if (pattern0.isEmpty()) emptyList() else pattern0.splitFast('/')
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
