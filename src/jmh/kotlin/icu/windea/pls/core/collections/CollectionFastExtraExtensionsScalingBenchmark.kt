package icu.windea.pls.core.collections

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * `CollectionFastExtraExtensions.kt` 中的方法在不同输入规模和相对切片规模下的基准测试。
 *
 * 使用 100、10000、1000000 三档输入规模，分别覆盖短前缀/后缀、
 * 半长前缀/后缀以及末位不匹配等场景。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class CollectionFastExtraExtensionsScalingBenchmark {
    @Param("100", "10000", "1000000")
    var size: Int = 0

    private lateinit var list: List<String>
    private lateinit var prefixOne: List<String>
    private lateinit var prefixHalf: List<String>
    private lateinit var prefixHalfMismatchLast: List<String>
    private lateinit var suffixOne: List<String>
    private lateinit var suffixHalf: List<String>
    private lateinit var suffixHalfMismatchLast: List<String>

    @Setup(Level.Trial)
    fun setup() {
        list = List(size) { "k$it" }
        val half = size / 2
        prefixOne = list.subList(0, 1)
        prefixHalf = list.subList(0, half)
        prefixHalfMismatchLast = List(half) { index ->
            if (index == half - 1) "x" else list[index]
        }
        suffixOne = list.subList(size - 1, size)
        suffixHalf = list.subList(size - half, size)
        suffixHalfMismatchLast = List(half) { index ->
            if (index == half - 1) "x" else list[size - half + index]
        }
    }

    // region removePrefixOrNull

    @Benchmark
    fun removePrefixOrNullOne(blackhole: Blackhole) {
        blackhole.consume(list.removePrefixOrNull(prefixOne))
    }

    @Benchmark
    fun removePrefixOrNullOneByReference(blackhole: Blackhole) {
        blackhole.consume(referenceRemovePrefixOrNull(list, prefixOne))
    }

    @Benchmark
    fun removePrefixOrNullHalf(blackhole: Blackhole) {
        blackhole.consume(list.removePrefixOrNull(prefixHalf))
    }

    @Benchmark
    fun removePrefixOrNullHalfByReference(blackhole: Blackhole) {
        blackhole.consume(referenceRemovePrefixOrNull(list, prefixHalf))
    }

    @Benchmark
    fun removePrefixOrNullHalfMismatchLast(blackhole: Blackhole) {
        blackhole.consume(list.removePrefixOrNull(prefixHalfMismatchLast))
    }

    @Benchmark
    fun removePrefixOrNullHalfMismatchLastByReference(blackhole: Blackhole) {
        blackhole.consume(referenceRemovePrefixOrNull(list, prefixHalfMismatchLast))
    }

    // endregion

    // region removeSuffixOrNull

    @Benchmark
    fun removeSuffixOrNullOne(blackhole: Blackhole) {
        blackhole.consume(list.removeSuffixOrNull(suffixOne))
    }

    @Benchmark
    fun removeSuffixOrNullOneByReference(blackhole: Blackhole) {
        blackhole.consume(referenceRemoveSuffixOrNull(list, suffixOne))
    }

    @Benchmark
    fun removeSuffixOrNullHalf(blackhole: Blackhole) {
        blackhole.consume(list.removeSuffixOrNull(suffixHalf))
    }

    @Benchmark
    fun removeSuffixOrNullHalfByReference(blackhole: Blackhole) {
        blackhole.consume(referenceRemoveSuffixOrNull(list, suffixHalf))
    }

    @Benchmark
    fun removeSuffixOrNullHalfMismatchLast(blackhole: Blackhole) {
        blackhole.consume(list.removeSuffixOrNull(suffixHalfMismatchLast))
    }

    @Benchmark
    fun removeSuffixOrNullHalfMismatchLastByReference(blackhole: Blackhole) {
        blackhole.consume(referenceRemoveSuffixOrNull(list, suffixHalfMismatchLast))
    }

    // endregion
}

private fun referenceRemovePrefixOrNull(list: List<String>, prefix: List<String>): List<String>? {
    if (prefix.isEmpty()) return list
    if (list.size < prefix.size) return null
    for (index in prefix.indices) {
        if (prefix[index] != list[index]) return null
    }
    return list.drop(prefix.size)
}

private fun referenceRemoveSuffixOrNull(list: List<String>, suffix: List<String>): List<String>? {
    if (suffix.isEmpty()) return list
    if (list.size < suffix.size) return null
    for (index in suffix.indices) {
        if (suffix[index] != list[list.size - suffix.size + index]) return null
    }
    return list.dropLast(suffix.size)
}
