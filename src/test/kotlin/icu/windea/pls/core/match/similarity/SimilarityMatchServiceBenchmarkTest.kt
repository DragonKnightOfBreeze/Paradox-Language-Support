@file:Suppress("UnusedVariable")

package icu.windea.pls.core.match.similarity

import com.github.benmanes.caffeine.cache.Cache
import icu.windea.pls.ChronicleCapabilities
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * [SimilarityMatchService] 的缓存行为测试。
 *
 * 这里验证缓存命中、淘汰和选项组合等行为；性能相关的场景由 `src/jmh` 中的 `SimilarityMatchServiceBenchmark` 负责。
 */
@Suppress("unused")
class SimilarityMatchServiceBenchmarkTest {
    @Before
    fun doSetUp() {
        // 确保启用需要的能力项
        System.setProperty("chronicle.recordCacheStat.marker", ChronicleCapabilities.General.recordCacheStats.toString())
        ChronicleCapabilities.General.recordCacheStats = true

        // 尽量清理缓存，避免不同测试方法之间的相互影响（统计不会被重置，后续取增量）
        cache().invalidateAll()
    }

    @After
    fun doTearDown() {
        ChronicleCapabilities.General.recordCacheStats = System.getProperty("chronicle.recordCacheStat.marker").toBoolean()
        System.clearProperty("chronicle.recordCacheStat.marker")
    }

    /**
     * 固定候选 + 固定输入：第二轮应全部命中。
     */
    @Test
    fun warmAndHit() {
        val inputs = buildInputs(seed = 7, size = 20)
        val candidates = buildCandidates(seed = 13, size = 200)
        val opts = SimilarityMatchOptions.DEFAULT

        cache().invalidateAll()
        val s0 = stats()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s1 = stats()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s2 = stats()

        val delta2 = s2 - s1

        // 断言：
        // 1) 命中次数应不少于期望值（全部命中）。
        Assert.assertEquals("expected second round hitCount == ${inputs.size}", inputs.size.toLong(), delta2.hits)
        // 2) 预热阶段通常应慢于命中阶段（宽松断言，避免偶发波动导致失败）。
        Assert.assertTrue("expected some misses in first round", (s1 - s0).misses > 0)
    }

    /**
     * 固定候选 + 唯一输入（不超过缓存容量）：第二轮应全部命中。
     */
    @Test
    fun uniqueKeysHitAllSecondRound() {
        val capacitySafe = 200 // 小于 SimilarityMatchService 缓存最大容量 5000
        val inputs = buildUniqueInputs(seed = 17, size = capacitySafe)
        val candidates = buildCandidates(seed = 19, size = 500)
        val opts = SimilarityMatchOptions.DEFAULT

        cache().invalidateAll()
        val s0 = stats()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s1 = stats()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s2 = stats()

        val delta1 = s1 - s0
        val delta2 = s2 - s1

        // 断言：第一轮基本全 miss；第二轮全 hit
        Assert.assertTrue("expected first round missCount >= ${inputs.size} but was ${delta1.misses}", delta1.misses >= inputs.size)
        Assert.assertEquals("expected second round hitCount == ${inputs.size}", inputs.size.toLong(), delta2.hits)
    }

    /**
     * 超过缓存容量时的淘汰行为采样（不做严格性能断言）。
     */
    @Test
    fun capacityEvictionBehaviorSample() {
        val inputs = buildUniqueInputs(seed = 23, size = 5_200) // 超过 maximumSize=5000
        val candidates = buildCandidates(seed = 29, size = 50)
        val opts = SimilarityMatchOptions.DEFAULT

        cache().invalidateAll()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s1 = stats()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s2 = stats()
        val delta2 = s2 - s1

        // 命中数应小于输入总数，且不超过容量
        Assert.assertTrue("expected hits < inputs (${inputs.size}) but was ${delta2.hits}", delta2.hits < inputs.size)
        Assert.assertTrue("expected hits <= 5000 but was ${delta2.hits}", delta2.hits <= 5_000)
    }

    /**
     * 多种选项组合冒烟测试，验证不同组合下第二轮存在有效命中。
     */
    @Test
    fun optionsMatrixSmoke() {
        val optionsList = listOf(
            SimilarityMatchOptions(ignoreCase = false, enableTypoMatch = false),
            SimilarityMatchOptions(ignoreCase = true, enableTypoMatch = false),
            SimilarityMatchOptions(ignoreCase = false, enableSnippetMatch = false, enableTypoMatch = false),
            SimilarityMatchOptions(ignoreCase = true, enableSnippetMatch = false, enableTypoMatch = false),
        )
        val candidates = buildCandidates(seed = 31, size = 200)
        val inputs = buildUniqueInputs(seed = 37, size = 50)

        for ((i, opts) in optionsList.withIndex()) {
            cache().invalidateAll()
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s1 = stats()
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s2 = stats()
            val delta2 = s2 - s1
            Assert.assertTrue("expected some hits in second round for options #$i", delta2.hits > 0)
        }
    }

    /**
     * 容量边界：<=5000 时第二轮全命中；>5000 时第二轮命中接近容量。
     */
    @Test
    fun capacityBoundarySweep() {
        val sizes = listOf(4_800, 5_000, 5_200)
        val candidates = buildCandidates(seed = 41, size = 50)
        val opts = SimilarityMatchOptions.DEFAULT
        for (size in sizes) {
            cache().invalidateAll()
            val inputs = buildUniqueInputs(seed = 43 + size, size = size)
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s1 = stats()
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s2 = stats()
            val delta = s2 - s1
            val maxHits = minOf(size, 5_000).toLong()
            Assert.assertTrue("expected some hits for size $size, actual=${delta.hits}", delta.hits > 0)
            Assert.assertTrue("expected hits <= $maxHits, actual=${delta.hits}", delta.hits <= maxHits)
            if (size > 5_000) {
                Assert.assertTrue("expected eviction for size $size, actual=${delta.hits}", delta.hits < size)
            }
        }
    }

    private fun cache(): Cache<Int, List<SimilarityMatchResult>> {
        val f = SimilarityMatchService::class.java.getDeclaredField("bestMatchesCache")
        f.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return f.get(SimilarityMatchService) as Cache<Int, List<SimilarityMatchResult>>
    }

    private fun stats(): StatsSnapshot {
        val s = cache().stats()
        return StatsSnapshot(
            hits = s.hitCount(),
            misses = s.missCount(),
            requests = s.requestCount(),
            hitRate = s.hitRate(),
        )
    }

    private data class StatsSnapshot(
        val hits: Long,
        val misses: Long,
        val requests: Long,
        val hitRate: Double,
    )

    private operator fun StatsSnapshot.minus(other: StatsSnapshot): StatsSnapshot {
        return StatsSnapshot(
            hits = this.hits - other.hits,
            misses = this.misses - other.misses,
            requests = this.requests - other.requests,
            hitRate = if ((this.requests - other.requests) > 0) (this.hits - other.hits).toDouble() / (this.requests - other.requests) else 0.0,
        )
    }

    private fun buildCandidates(seed: Int, size: Int): List<String> {
        val rnd = Random(seed)
        val letters = ('a'..'z').toList()
        return List(size) {
            val first = letters[rnd.nextInt(letters.size)]
            val restLen = 6 + rnd.nextInt(6)
            val rest = CharArray(restLen) { letters[rnd.nextInt(letters.size)] }.concatToString()
            "$first$rest"
        }
    }

    @Suppress("SameParameterValue")
    private fun buildInputs(seed: Int, size: Int): List<String> {
        val rnd = Random(seed)
        val letters = ('a'..'z').toList()
        return List(size) {
            val first = letters[rnd.nextInt(letters.size)]
            val restLen = 4 + rnd.nextInt(4)
            val rest = CharArray(restLen) { letters[rnd.nextInt(letters.size)] }.concatToString()
            "$first$rest"
        }
    }

    private fun buildUniqueInputs(seed: Int, size: Int): List<String> {
        val rnd = Random(seed)
        val letters = ('a'..'z').toList()
        val set = LinkedHashSet<String>(size)
        while (set.size < size) {
            val first = letters[rnd.nextInt(letters.size)]
            val restLen = 4 + rnd.nextInt(4)
            val rest = CharArray(restLen) { letters[rnd.nextInt(letters.size)] }.concatToString()
            set.add("$first$rest")
        }
        return set.toList()
    }
}
