package icu.windea.pls.core.match.similarity

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val INPUT_COUNT = 100

/**
 * [SimilarityMatchService] 的性能基准。
 *
 * 缓存命中/淘汰等行为断言位于 `SimilarityMatchServiceBenchmarkTest`。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
open class SimilarityMatchServiceBenchmark {
    @JvmField
    @Param("2000", "5000", "10000")
    var candidateSize: Int = 0

    private lateinit var candidates: List<String>
    private lateinit var inputs: List<String>
    private lateinit var options: SimilarityMatchOptions
    private lateinit var directOptions: SimilarityMatchOptions

    @Setup(Level.Trial)
    open fun setup() {
        candidates = buildCandidates(seed = 13 + candidateSize, size = candidateSize)
        inputs = buildInputs(seed = 7, size = INPUT_COUNT)
        options = SimilarityMatchOptions.DEFAULT
        directOptions = options.copy(cached = false)

        // 预热缓存，使 warm 基准测量的是缓存命中路径。
        for (input in inputs) {
            SimilarityMatchService.findBestMatches(input, candidates, options)
        }
    }

    @Benchmark
    @OperationsPerInvocation(INPUT_COUNT)
    open fun findBestMatchesWarm(blackhole: Blackhole) {
        for (input in inputs) {
            blackhole.consume(SimilarityMatchService.findBestMatches(input, candidates, options))
        }
    }

    @Benchmark
    @OperationsPerInvocation(INPUT_COUNT)
    open fun findBestMatchesDirect(blackhole: Blackhole) {
        for (input in inputs) {
            blackhole.consume(SimilarityMatchService.findBestMatches(input, candidates, directOptions))
        }
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
            val first = letters[rnd.nextInt(letters.size)]
            val restLen = 4 + rnd.nextInt(4)
            val rest = CharArray(restLen) { letters[rnd.nextInt(letters.size)] }.concatToString()
            "$first$rest"
        }
    }
}
