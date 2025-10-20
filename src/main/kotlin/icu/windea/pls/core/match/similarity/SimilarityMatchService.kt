package icu.windea.pls.core.match.similarity

object SimilarityMatchService {
    /**
     * 按相似度匹配，从一组候选项（[candidates]）中查询输入项（[input]）的最佳匹配。
     * 可以配置匹配选项（[options]）。
     *
     * 说明：
     * - 策略执行顺序：前缀匹配 → 片段匹配 → 错字匹配。按顺序追加结果并去重。
     * - 前缀匹配与片段匹配：按候选项字典序升序。
     * - 错字匹配：按分数降序。若分数相同，再按候选项字典序升序。
     *
     * 对于错字匹配：
     * - 最多取 [SimilarityMatchOptions.typoTopN] 项
     * - 排除分数低于 [SimilarityMatchOptions.typoMinScore] 的项。
     *
     * @see SimilarityMatcher
     */
    fun findBestMatches(
        input: String,
        candidates: Collection<String>,
        options: SimilarityMatchOptions = SimilarityMatchOptions.DEFAULT,
    ): List<SimilarityMatchResult> {
        if (candidates.isEmpty()) return emptyList()
        if (input.isEmpty()) return emptyList()

        // 去重候选，保持稳定迭代顺序
        val uniqueCandidates = candidates.toSet()
        val picked = mutableMapOf<String, SimilarityMatchResult>()

        // 1) 前缀匹配：按字典序
        if (options.enablePrefixMatch) {
            val matched = uniqueCandidates.asSequence()
                .mapNotNull { PrefixSimilarityMatcher.match(input, it, options.ignoreCase) }
                .sortedWith(compareBy { it.value })
                .toList()
            matched.forEach { picked.putIfAbsent(it.value, it) }
        }

        // 2) 片段匹配：剩余候选，按字典序
        if (options.enableSnippetMatch) {
            val remain = uniqueCandidates.asSequence().filter { it !in picked.keys }
            val matched = remain
                .mapNotNull { SnippetSimilarityMatcher.match(input, it, options.ignoreCase) }
                .sortedWith(compareBy { it.value })
                .toList()
            matched.forEach { picked.putIfAbsent(it.value, it) }
        }

        // 3) 错字匹配：剩余候选，按分数降序，再字典序，限制 TopN 和最小分数
        if (options.enableTypoMatch) {
            val remain = uniqueCandidates.asSequence().filter { it !in picked.keys }
            val matched = remain
                .mapNotNull { TypoSimilarityMatcher.match(input, it, options.ignoreCase) }
                .filter { it.score >= options.typoMinScore }
                .sortedWith(compareByDescending<SimilarityMatchResult> { it.score }.thenBy { it.value })
                .take(options.typoTopN)
                .toList()
            matched.forEach { picked.putIfAbsent(it.value, it) }
        }

        return picked.values.toList()
    }
}
