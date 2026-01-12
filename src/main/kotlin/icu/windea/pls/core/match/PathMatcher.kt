package icu.windea.pls.core.match

/**
 * 子路径级别的匹配器。
 */
object PathMatcher {
    /**
     * 判断输入的子路径（[input]）是否匹配指定的子路径（[other]）。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param usePattern 如果启用，且指定的子路径是 GLOB 表达式，则使用 GLOB 模式匹配。参见：[GlobMatcher]。
     * @param useAny 如果启用，且指定的子路径是 `any`，则直接匹配。
     */
    fun matches(input: String, other: String, ignoreCase: Boolean = false, usePattern: Boolean = false, useAny: Boolean = false): Boolean {
        if (useAny && other == "any") return true
        if (usePattern) return GlobMatcher.matches(input, other, ignoreCase)
        return input.equals(other, ignoreCase)
    }

    /**
     * 判断输入的子路径列表（[input]）是否匹配指定的子路径列表（[other]）。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param usePattern 如果启用，且指定的子路径是 GLOB 表达式，则使用 GLOB 模式匹配。参见：[GlobMatcher]。
     * @param useAny 如果启用，且指定的子路径是 `any`，则直接匹配。
     */
    fun matches(input: List<String>, other: List<String>, ignoreCase: Boolean = false, usePattern: Boolean = false, useAny: Boolean = false): Boolean {
        if (input.size != other.size) return false // 路径过短或路径长度不一致
        for ((index, otherPath) in other.withIndex()) {
            val inputPath = input[index]
            val r = matches(inputPath, otherPath, ignoreCase, usePattern, useAny)
            if (!r) return false
        }
        return true
    }

    /**
     * 得到输入的子路径列表（[input]）相对于指定的子路径列表（[other]）的第一个子路径。如果两者完全匹配，则返回空字符串。
     *
     * @param ignoreCase 是否忽略大小写。
     * @param usePattern 如果启用，且指定的子路径是 GLOB 表达式，则使用 GLOB 模式匹配。参见：[GlobMatcher]。
     * @param useAny 如果启用，且指定的子路径是 `any`，则直接匹配。
     */
    fun relative(input: List<String>, other: List<String>, ignoreCase: Boolean = false, usePattern: Boolean = false, useAny: Boolean = false): String? {
        if (input.size > other.size) return null
        for ((index, inputPath) in input.withIndex()) {
            val otherPath = other[index]
            val r = matches(inputPath, otherPath, ignoreCase, usePattern, useAny)
            if (!r) return null
        }
        if (input.size == other.size) return ""
        return other[input.size]
    }
}
