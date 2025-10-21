package icu.windea.pls.core.match.similarity

import icu.windea.pls.core.formatted

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
) {
    fun render(): String {
        return buildString {
            append(value)
            append(" [")
            append(strategy).append(", ").append(score.formatted(-3)).append("% similarity")
            append("]")
        }
    }
}
