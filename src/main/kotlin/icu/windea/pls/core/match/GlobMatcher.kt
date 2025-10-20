package icu.windea.pls.core.match

/**
 * 基于 GLOB 模式的匹配器。
 *
 * 说明：
 * - `?` 匹配单个字符。
 * - `*` 匹配任意个字符。
 */
object GlobMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的 GLOB 模式([pattern])。
     *
     * @param ignoreCase 是否忽略大小写。
     */
    fun matches(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
        return fullMatch(input, pattern, ignoreCase)
    }

    private fun fullMatch(input: String, pattern: String, ignoreCase: Boolean): Boolean {
        if (pattern.isEmpty() && input.isNotEmpty()) return false
        if (pattern == "*") return true
        return segmentMatch(input, pattern, ignoreCase)
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
