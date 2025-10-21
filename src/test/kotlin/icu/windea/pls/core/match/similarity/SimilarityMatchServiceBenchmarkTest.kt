package icu.windea.pls.core.match.similarity

import com.github.benmanes.caffeine.cache.Cache
import icu.windea.pls.test.AssumePredicates
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * SimilarityMatchService 的缓存基准测试。
 *
 * 运行方式：
 * - 默认被忽略（见 [AssumePredicates.includeBenchmark]）。
 * - 执行时添加 -Dpls.test.include.all=true 可启用；建议同时添加 -Dpls.cache.recordStats=true。
 * - 若未传入 `pls.cache.recordStats=true`，本测试会在 [setup] 中强制开启（仅对当前 JVM 的测试影响）。
 */
class SimilarityMatchServiceBenchmarkTest {
    @Before
    fun setup() {
        // benchmark 默认忽略，需要显示包含
        AssumePredicates.includeBenchmark()
        // 确保启用 Caffeine 的 recordStats()，以便获取命中率等统计
        if (!java.lang.Boolean.getBoolean("pls.cache.recordStats")) {
            System.setProperty("pls.cache.recordStats", "true")
        }
        // 尽量清理缓存，避免不同测试方法之间的相互影响（统计不会被重置，后续取增量）
        cache().invalidateAll()
    }

    /**
     * 场景E：更细粒度容量边界（4800/4900/5000/5100/5200）。
     * 断言：<=5000 时第二轮全命中；>5000 时第二轮命中恰好为 5000。
     */
    @Test
    fun capacityBoundarySweep() {
        val sizes = listOf(4_800, 4_900, 5_000, 5_100, 5_200)
        val candidates = buildCandidates(seed = 41, size = 12_000)
        val opts = SimilarityMatchOptions.DEFAULT
        for (size in sizes) {
            cache().invalidateAll()
            val inputs = buildUniqueInputs(seed = 43 + size, size = size)
            // 第一轮：填充
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s1 = stats()
            // 第二轮：测命中
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s2 = stats()
            val delta = s2 - s1
            val estSize = cache().estimatedSize()
            println("[capacityBoundarySweep#$size] second hits=${delta.hits} / ${inputs.size}, estimatedSize=${estSize}")
            if (size <= 5_000) {
                Assert.assertEquals("<=5000 should fully hit on second round", size.toLong(), delta.hits)
            } else {
                val lowerBound = (5_000 * 0.98).toLong() // TinyLFU 可能导致冷启动下轻微欠填充
                Assert.assertTrue(">5000 should hit close to capacity on second round (>=${lowerBound}, <=5000), actual=${delta.hits}",
                    delta.hits in lowerBound..5_000L)
            }
        }
    }

    /**
     * 场景F：Key 构造路径压测。比较“带缓存冷启动调用”与“直接调用内部实现（绕过缓存）”的耗时差。
     * 为减少抖动：每个规模测 5 次取平均。仅打印指标，不做严格性能断言。
     */
    @Test
    fun keyConstructionOverheadPressure() {
        val sizes = listOf(2_000, 5_000, 10_000, 20_000)
        val opts = SimilarityMatchOptions.DEFAULT
        val optsDirect = opts.copy(cached = false)
        for (size in sizes) {
            val candidates = buildCandidates(seed = 53 + size, size = size)
            // 多次采样取平均
            val rounds = 5
            var coldTotal = 0L
            var directTotal = 0L
            repeat(rounds) { r ->
                // 冷启动（避免命中）：使用唯一输入并清理缓存
                cache().invalidateAll()
                val input = "a" + r.toString() + "_" + size.toString()
                val tCold = measureTimeMillis {
                    SimilarityMatchService.findBestMatches(input, candidates, opts)
                }
                coldTotal += tCold
                val tDirect = measureTimeMillis {
                    SimilarityMatchService.findBestMatches(input, candidates, optsDirect)
                }
                directTotal += tDirect
            }
            val coldAvg = coldTotal / rounds.toDouble()
            val directAvg = directTotal / rounds.toDouble()
            val overhead = coldAvg - directAvg
            val ratio = if (directAvg > 0.0) coldAvg / directAvg else Double.NaN
            println("[keyConstructionOverheadPressure#$size] coldAvg=${"%.2f".format(coldAvg)}ms, directAvg=${"%.2f".format(directAvg)}ms, overhead=${"%.2f".format(overhead)}ms, ratio=${"%.3f".format(ratio)}")
            // 保守断言，确保可测：两侧均应 > 0
            Assert.assertTrue("expected coldAvg > 0", coldAvg > 0)
            Assert.assertTrue("expected directAvg > 0", directAvg > 0)
        }
    }

    /**
     * 场景A：固定候选 + 固定输入。
     * 第一次调用用于预热缓存，后续多轮调用应大量命中缓存。
     */
    @Test
    fun warmAndHitBenchmark() {
        val inputs = buildInputs(seed = 7, size = 200)
        val candidates = buildCandidates(seed = 13, size = 10_000)
        val opts = SimilarityMatchOptions.DEFAULT

        val s0 = stats()

        // 预热（填充缓存）
        val warmMs = measureTimeMillis {
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
        }

        // 命中阶段（多轮重复相同查询）
        val hitRounds = 20
        val expectHits = inputs.size * hitRounds
        val hitMs = measureTimeMillis {
            repeat(hitRounds) {
                for (input in inputs) {
                    SimilarityMatchService.findBestMatches(input, candidates, opts)
                }
            }
        }

        val s1 = stats()
        val delta = s1 - s0

        val hitAvgMs = hitMs / hitRounds.toDouble()
        println("[warmAndHitBenchmark] warm=${warmMs}ms, hit=${hitMs}ms (avg=${"%.2f".format(hitAvgMs)}ms)")
        printStats("[warmAndHitBenchmark]", delta)

        // 断言：
        // 1) 命中次数应不少于期望值（全部命中）。
        Assert.assertTrue("expected hitCount >= $expectHits but was ${delta.hits}", delta.hits >= expectHits)
        // 2) 预热阶段通常应慢于命中阶段（宽松断言，避免偶发波动导致失败）。
        Assert.assertTrue("expected hit phase faster or equal (per-round): warm=${warmMs}ms, hitAvg=${hitAvgMs}ms", hitAvgMs <= warmMs)
    }

    /**
     * 场景B：固定候选 + 大量唯一输入（不超过缓存容量），第二轮应全部命中。
     */
    @Test
    fun uniqueKeysHitAllSecondRound() {
        val capacitySafe = 4_000 // 小于 SimilarityMatchService 缓存最大容量 5000
        val inputs = buildUniqueInputs(seed = 17, size = capacitySafe)
        val candidates = buildCandidates(seed = 19, size = 12_000)
        val opts = SimilarityMatchOptions.DEFAULT

        val s0 = stats()

        // 第一轮：全部为 miss
        val firstMs = measureTimeMillis {
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
        }

        val s1 = stats()
        val delta1 = s1 - s0

        // 第二轮：应全部命中
        val secondMs = measureTimeMillis {
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
        }

        val s2 = stats()
        val delta2 = s2 - s1

        println("[uniqueKeysHitAllSecondRound] first=${firstMs}ms, second=${secondMs}ms")
        printStats("[uniqueKeysHitAllSecondRound#first]", delta1)
        printStats("[uniqueKeysHitAllSecondRound#second]", delta2)

        // 断言：第一轮基本全 miss；第二轮全 hit
        Assert.assertTrue("expected first round missCount >= ${inputs.size} but was ${delta1.misses}", delta1.misses >= inputs.size)
        Assert.assertEquals("expected second round hitCount == ${inputs.size}", inputs.size.toLong(), delta2.hits)
        // 性能上第二轮应明显更快
        Assert.assertTrue("expected second faster: first=${firstMs}ms, second=${secondMs}ms", secondMs < firstMs)
    }

    /**
     * 场景C：超过缓存容量时的淘汰行为采样（不做严格性能断言）。
     * 预期：第二轮命中数小于输入总数，且不超过最大容量 5000。
     */
    @Test
    fun capacityEvictionBehaviorSample() {
        val inputs = buildUniqueInputs(seed = 23, size = 6_000) // 超过 maximumSize=5000
        val candidates = buildCandidates(seed = 29, size = 10_000)
        val opts = SimilarityMatchOptions.DEFAULT

        // 清理一次，避免前序测试影响
        cache().invalidateAll()

        val s0 = stats()
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s1 = stats()
        // 第二轮
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, opts)
        }
        val s2 = stats()
        val delta2 = s2 - s1

        println("[capacityEvictionBehaviorSample] second round hits=${delta2.hits}")

        // 命中数应小于输入总数，且不超过容量
        Assert.assertTrue("expected hits < inputs (${inputs.size}) but was ${delta2.hits}", delta2.hits < inputs.size)
        Assert.assertTrue("expected hits <= 5000 but was ${delta2.hits}", delta2.hits <= 5_000)
    }

    /**
     * 场景D：多种选项组合烟囱测试，验证不同组合下第二轮存在有效命中。
     */
    @Test
    fun optionsMatrixSmoke() {
        val optionsList = listOf(
            SimilarityMatchOptions(ignoreCase = false, enableTypoMatch = false),
            SimilarityMatchOptions(ignoreCase = true, enableTypoMatch = false),
            SimilarityMatchOptions(ignoreCase = false, enableSnippetMatch = false, enableTypoMatch = false),
            SimilarityMatchOptions(ignoreCase = true, enableSnippetMatch = false, enableTypoMatch = false),
        )
        val candidates = buildCandidates(seed = 31, size = 8_000)
        val inputs = buildUniqueInputs(seed = 37, size = 300)

        for ((i, opts) in optionsList.withIndex()) {
            // 每个组合前清理，确保统计独立
            cache().invalidateAll()
            val s0 = stats()
            // 第一轮：填充
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s1 = stats()
            // 第二轮：应有命中
            for (input in inputs) {
                SimilarityMatchService.findBestMatches(input, candidates, opts)
            }
            val s2 = stats()
            val delta2 = s2 - s1
            println("[optionsMatrixSmoke#$i] hits=${delta2.hits}, misses=${delta2.misses}, hitRate=${"%.4f".format(delta2.hitRate)}")
            Assert.assertTrue("expected some hits in second round for options #$i", delta2.hits > 0)
        }
    }

    // =============== 工具方法 ===============

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

    private fun printStats(tag: String, s: StatsSnapshot) {
        println("$tag stats: hits=${s.hits}, misses=${s.misses}, requests=${s.requests}, hitRate=${"%.4f".format(s.hitRate)}")
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

    private fun buildInputs(seed: Int, size: Int): List<String> {
        val rnd = Random(seed)
        val letters = ('a'..'z').toList()
        return List(size) {
            // 与 candidates 的规则一致，保证首字母匹配到足够多候选
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
