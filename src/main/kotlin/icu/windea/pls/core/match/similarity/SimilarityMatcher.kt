package icu.windea.pls.core.match.similarity

/**
 * 相似度匹配器。
 */
interface SimilarityMatcher {
    /**
     * 判断输入的字符串（[input]）是否匹配指定的候选项([candidate])。
     *
     * @param ignoreCase 是否忽略大小写。
     */
    fun match(input: String, candidate: String, ignoreCase: Boolean = false): SimilarityMatchResult?
}
