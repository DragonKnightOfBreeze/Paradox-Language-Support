package icu.windea.pls.core.match.similarity

/**
 * 基于前缀匹配的相似度匹配器。
 *
 * 说明：
 * - 分数计算：输入项长度 / 候选项长度
 *
 * 示例：
 * - `foo` 匹配 `fooa` `foo_b`，不匹配 `foa` `a_foo`
 */
object PrefixSimilarityMatcher : SimilarityMatcher {
    override fun match(input: String, candidate: String, ignoreCase: Boolean): SimilarityMatchResult? {
        if (input.isEmpty() || candidate.isEmpty()) return null // unsupported
        val r = candidate.startsWith(input, ignoreCase)
        if (!r) return null
        val score = input.length.toDouble() / candidate.length
        val strategy = SimilarityMatchStrategy.PREFIX
        return SimilarityMatchResult(candidate, score, strategy)
    }
}

/**
 * 基于片段匹配的相似度匹配器。
 *
 * 说明：
 * - 通过特定的分隔符（`_` `.`），将输入项和候选项切分为片段。
 * - 要求输入从候选开头按顺序匹配，可以跨越若干个缺失片段，但不允许前置片段。
 * - 第一个片段必须精确匹配（可忽略大小写）。
 * - 分数计算：匹配片段数 / 总片段数
 *
 * 示例：
 * - `foo_bar` 匹配 `foo_bar_a` `foo_a_bar`，不匹配 `a_foo_bar` `bar_a_foo`
 */
object SnippetSimilarityMatcher : SimilarityMatcher {
    override fun match(input: String, candidate: String, ignoreCase: Boolean): SimilarityMatchResult? {
        if (input.isEmpty() || candidate.isEmpty()) return null // unsupported
        val inputSnippets = input.split('_', '.')
        val candidateSnippets = candidate.split('_', '.')
        if (inputSnippets.isEmpty() || candidateSnippets.isEmpty()) return null // unsupported
        if (!inputSnippets.first().equals(candidateSnippets.first(), ignoreCase)) return null // require exact matched first snippet
        // 从候选开头按顺序匹配输入片段，允许跨越缺失片段
        var i = 1
        var j = 1
        while (i < inputSnippets.size && j < candidateSnippets.size) {
            val a = inputSnippets[i]
            val b = candidateSnippets[j]
            if (a.equals(b, ignoreCase)) i++
            j++
        }
        val matched = (i == inputSnippets.size)
        if (!matched) return null
        val score = inputSnippets.size.toDouble() / candidateSnippets.size
        val strategy = SimilarityMatchStrategy.SNIPPET
        return SimilarityMatchResult(candidate, score, strategy)
    }
}

/**
 * 基于错字匹配的相似度匹配器。
 *
 * 说明：
 * - 采用 Damerau–Levenshtein（支持单次邻位交换），对剩余候选计算距离。
 * - 第一个字符必须精确匹配（可忽略大小写）。
 */
object TypoSimilarityMatcher : SimilarityMatcher {
    override fun match(input: String, candidate: String, ignoreCase: Boolean): SimilarityMatchResult? {
        if (input.isEmpty() || candidate.isEmpty()) return null // unsupported
        val a = if (ignoreCase) input.lowercase() else input
        val b = if (ignoreCase) candidate.lowercase() else candidate
        if (a[0] != b[0]) return null // require exact matched first char
        val dist = damerauOsA(a, b)
        val denom = maxOf(a.length, b.length).coerceAtLeast(1)
        val score = 1.0 - dist.toDouble() / denom
        val strategy = SimilarityMatchStrategy.TYPO
        return SimilarityMatchResult(candidate, score, strategy)
    }

    // Optimal String Alignment（Damerau–Levenshtein 的常用变体），支持单次邻位交换
    private fun damerauOsA(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        if (m == 0) return n
        if (n == 0) return m
        val d = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) d[i][0] = i
        for (j in 0..n) d[0][j] = j
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var v = minOf(
                    d[i - 1][j] + 1,       // deletion
                    d[i][j - 1] + 1,       // insertion
                    d[i - 1][j - 1] + cost // substitution
                )
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    v = minOf(v, d[i - 2][j - 2] + cost) // transposition
                }
                d[i][j] = v
            }
        }
        return d[m][n]
    }
}
