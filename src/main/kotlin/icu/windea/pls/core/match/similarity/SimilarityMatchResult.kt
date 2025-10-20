package icu.windea.pls.core.match.similarity

/**
 * 相似度匹配结果。
 *
 * @property value 匹配项。
 * @property score 匹配分数。
 * @property strategy 匹配策略。
 */
data class SimilarityMatchResult(
    val value: String,
    val score: Double,
    val strategy: SimilarityMatchStrategy,
)
