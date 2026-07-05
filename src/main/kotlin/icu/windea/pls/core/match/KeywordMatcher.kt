package icu.windea.pls.core.match

/**
 * 关键字的匹配器。
 */
object KeywordMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的关键字（[keyword]）。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param usePattern 如果启用，且指定的子路径是 GLOB 表达式，则使用 GLOB 模式匹配。参见：[GlobMatcher]。
     */
    fun matches(input: String?, keyword: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Boolean {
        if (input == null) return false
        if (usePattern) return GlobMatcher.matches(input, keyword, ignoreCase)
        return input.equals(keyword, ignoreCase)
    }

    /**
     * 判断输入的字符串（[input]）是否匹配指定的任意关键字（[keywords]）。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param usePattern 如果启用，且指定的子路径是 GLOB 表达式，则使用 GLOB 模式匹配。参见：[GlobMatcher]。
     */
    fun matches(input: String?, keywords: Array<out String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Boolean {
        return keywords.any { keyword -> matches(input, keyword, ignoreCase, usePattern) }
    }

    /**
     * 判断输入的字符串（[input]）是否匹配指定的任意关键字（[keywords]）。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param usePattern 如果启用，且指定的子路径是 GLOB 表达式，则使用 GLOB 模式匹配。参见：[GlobMatcher]。
     */
    fun matches(input: String?, keywords: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Boolean {
        return keywords.any { keyword -> matches(input, keyword, ignoreCase, usePattern) }
    }
}
