package icu.windea.pls.core.match.similarity

/**
 * 相似度匹配选项。
 *
 * @property ignoreCase 是否忽略大小写。
 * @property typoTopN 使用错字匹配时，选取的最相似项的个数。
 * @property typoMinScore 使用错字匹配时，选取的相似项的最小分数。
 * @property enablePrefixMatch 是否使用前缀匹配，参见 [PrefixSimilarityMatcher]。
 * @property enableSnippetMatch 是否使用片段匹配，参见 [SnippetSimilarityMatcher]。
 * @property enableTypoMatch 是否使用错字匹配，参见 [TypoSimilarityMatcher]。
 */
data class SimilarityMatchOptions(
    val ignoreCase: Boolean = false,
    val typoTopN: Int = 5,
    val typoMinScore: Double = 0.6,
    val enablePrefixMatch: Boolean = true,
    val enableSnippetMatch: Boolean = true,
    val enableTypoMatch: Boolean = true,
) {
    companion object {
        val DEFAULT = SimilarityMatchOptions()
    }
}
