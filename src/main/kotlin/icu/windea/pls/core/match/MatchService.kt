package icu.windea.pls.core.match

import icu.windea.pls.core.orNull
import org.intellij.lang.annotations.Language

object MatchService {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的 GLOB 模式（[pattern]）。
     *
     * @param ignoreCase 是否忽略大小写。
     *
     * @see GlobMatcher
     */
    fun matchesPattern(input: String?, pattern: String, ignoreCase: Boolean = false): Boolean {
        if (input == null) return false
        return GlobMatcher.matches(input, pattern, ignoreCase)
    }

    /**
     * 判断输入的字符串（[input]）是否匹配指定的由 [delimiter] 分隔后的任意 GLOB 模式（[patterns]）。忽略分隔后的首尾空白。
     *
     * @param delimiter 通配符的分隔符。
     * @param ignoreCase 是否忽略大小写。
     *
     * @see GlobMatcher
     */
    fun matchesPatterns(input: String?, patterns: String, delimiter: Char = ';', ignoreCase: Boolean = false): Boolean {
        if (input == null) return false
        val sequence = patterns.splitToSequence(delimiter).mapNotNull { it.trim().orNull() }
        return sequence.any { pattern -> GlobMatcher.matches(input, pattern, ignoreCase) }
    }

    /**
     * 判断输入的字符串（[input]）是否匹配指定的 ANT 路径模式（[pattern]）。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param trimSeparator 是否去除首尾的路径分隔符。
     *
     * @see AntMatcher
     */
    fun matchesAntPattern(input: String?, pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
        if (input == null) return false
        return AntMatcher.matches(input, pattern, ignoreCase, trimSeparator)
    }

    /**
     * 判断输入的字符串（[input]）是否匹配指定的由 [delimiter] 分隔后的任意 ANT 路径模式（[patterns]）。忽略分隔后的首尾空白。
     *
     * @param delimiter 通配符的分隔符。
     * @param ignoreCase 是否忽略大小写。
     * @param trimSeparator 是否去除首尾的路径分隔符。
     *
     * @see AntMatcher
     */
    fun matchesAntPatterns(input: String?, patterns: String, delimiter: Char = ';', ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
        if (input == null) return false
        val sequence = patterns.splitToSequence(delimiter).mapNotNull { it.trim().orNull() }
        return sequence.any { pattern -> AntMatcher.matches(input, pattern, ignoreCase, trimSeparator) }
    }

    /**
     * 判断输入的字符串（[input]）是否匹配指定的正则（[pattern]）。
     *
     * @param ignoreCase 是否忽略大小写。
     *
     * @see RegexMatcher
     */
    fun matchesRegex(input: String?, @Language("RegExp") pattern: String, ignoreCase: Boolean = false): Boolean {
        if (input == null) return false
        return RegexMatcher.matches(input, pattern, ignoreCase)
    }
}
