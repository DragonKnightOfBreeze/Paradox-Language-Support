package icu.windea.pls.core.match.similarity

import java.text.DecimalFormat

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
    private val df = DecimalFormat("#0.0%")

    fun render(): String {
        return buildString {
            append(value)
            append(" [")
            append(strategy).append(", ").append(df.format(score)).append(" similarity")
            append("]")
        }
    }
}
