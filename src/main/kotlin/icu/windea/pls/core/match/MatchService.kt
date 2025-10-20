package icu.windea.pls.core.match

object MatchService {
    /**
     * 判断输入的字符串（input）是否匹配指定的 GLOB 模式(pattern)。
     *
     * @param ignoreCase 是否忽略大小写。
     *
     * @see GlobMatcher
     */
    fun matchesPattern(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
        return GlobMatcher.matches(input, pattern, ignoreCase)
    }

    /**
     * 判断输入的字符串（input）是否匹配指定的 ANT 路径模式(pattern)。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param trimSeparator 是否去除首尾的分隔符。
     *
     * @see AntMatcher
     */
    fun matchesAntPattern(input: String, pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
        return AntMatcher.matches(input, pattern, ignoreCase, trimSeparator)
    }

    /**
     * 判断输入的字符串（input）是否匹配指定的正则（pattern）。
     *
     * @param ignoreCase 是否忽略大小写。
     *
     * @see RegexMatcher
     */
    fun matchesRegexPattern(input: String, pattern: String, ignoreCase: Boolean = false): Boolean {
        return RegexMatcher.matches(input, pattern, ignoreCase)
    }
}
