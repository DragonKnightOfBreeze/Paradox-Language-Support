package icu.windea.pls.core.match.similarity

import com.google.common.hash.Hashing
import icu.windea.pls.core.util.CacheBuilder
import java.util.*
import kotlin.math.roundToInt

object SimilarityMatchService {
    /**
     * 按相似度匹配，从一组候选项（[candidates]）中查询输入项（[input]）的最佳匹配。
     * 可以配置匹配选项（[options]）。
     *
     * 说明：
     * - 策略执行顺序：前缀匹配 → 片段匹配 → 错字匹配。按顺序追加结果并去重。
     * - 结果排序：策略（正序） → 分数（倒序） → 保持在候选项列表中的顺序。
     * - 至少要求首字母精确匹配（可忽略大小写）。
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

        // 过滤并去重候选，保持稳定迭代顺序（至少要求首字母精确匹配）
        val c = input.first()
        val finalCandidates = candidates.distinct().filter { it.isNotEmpty() && it.first().equals(c, options.ignoreCase) }
        if (options.cached) return doFindBestMatchesFromCache(input, finalCandidates, options)
        return doFindBestMatches(input, finalCandidates, options)
    }


    private val bestMatchesCache = CacheBuilder("maximumSize=5000, expireAfterAccess=30m").build<Int, List<SimilarityMatchResult>>()

    private fun doFindBestMatchesFromCache(input: String, finalCandidates: List<String>, options: SimilarityMatchOptions): List<SimilarityMatchResult> {
        val cacheKey = doGetBestMatchesCacheKey(input, finalCandidates, options)
        return bestMatchesCache.get(cacheKey) { doFindBestMatches(input, finalCandidates, options) }
    }

    @Suppress("UnstableApiUsage")
    private fun doGetBestMatchesCacheKey(input: String, finalCandidates: List<String>, options: SimilarityMatchOptions): Int {
        // 使用 Guava Murmur3-128
        val hasher = Hashing.murmur3_128().newHasher()
        hasher.putInt(1) // VERSION
        val input0 = if (options.ignoreCase) input.lowercase(Locale.ROOT) else input
        hasher.putInt(input0.length).putString(input0, Charsets.UTF_8)
        hasher.putInt(finalCandidates.size)
        for (candidate in finalCandidates) {
            val candidate0 = if (options.ignoreCase) candidate.lowercase() else candidate
            hasher.putInt(candidate0.length).putString(candidate0, Charsets.UTF_8)
        }
        var mask = 0
        if (options.ignoreCase) mask = mask or 1
        if (options.enablePrefixMatch) mask = mask or 2
        if (options.enableSnippetMatch) mask = mask or 4
        if (options.enableTypoMatch) mask = mask or 8
        hasher.putByte(mask.toByte())
        hasher.putInt(options.typoTopN)
        hasher.putInt((options.typoMinScore * 1000).roundToInt())
        return hasher.hash().asInt() // 这里直接使用 `asInt()` 即可，因为 Murmur3-128 使用 `IntHashCode`
    }

    private fun doFindBestMatches(input: String, finalCandidates: List<String>, options: SimilarityMatchOptions): List<SimilarityMatchResult> {
        val picked = mutableMapOf<String, SimilarityMatchResult>()
        if (options.enablePrefixMatch) {
            // 前缀匹配
            val matched = finalCandidates.asSequence()
                .mapNotNull { PrefixSimilarityMatcher.match(input, it, options.ignoreCase) }
                .sortedWith(compareByDescending { it.score })
                .toList()
            matched.forEach { picked.putIfAbsent(it.value, it) }
        }
        if (options.enableSnippetMatch) {
            // 片段匹配
            val remain = finalCandidates.asSequence().filter { it !in picked.keys }
            val matched = remain
                .mapNotNull { SnippetSimilarityMatcher.match(input, it, options.ignoreCase) }
                .sortedWith(compareByDescending { it.score })
                .toList()
            matched.forEach { picked.putIfAbsent(it.value, it) }
        }
        if (options.enableTypoMatch) {
            // 错字匹配
            val remain = finalCandidates.asSequence().filter { it !in picked.keys }
            val matched = remain
                .mapNotNull { TypoSimilarityMatcher.match(input, it, options.ignoreCase) }
                .filter { it.score >= options.typoMinScore }
                .sortedWith(compareByDescending { it.score })
                .take(options.typoTopN)
                .toList()
            matched.forEach { picked.putIfAbsent(it.value, it) }
        }
        return picked.values.toList()
    }
}
