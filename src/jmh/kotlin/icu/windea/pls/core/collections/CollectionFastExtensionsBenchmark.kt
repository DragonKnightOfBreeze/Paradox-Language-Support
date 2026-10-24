package icu.windea.pls.core.collections

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

private const val SIZE = 64

/**
 * `CollectionFastExtensions.kt` 中的方法的基准测试。
 *
 * 每个 `@Fast` 方法都与对应的标准库/惯用实现对比；结果用于回归观察和相对比较。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class CollectionFastExtensionsBenchmark {
    private val ints = List(SIZE) { it }
    private val single = listOf(1)
    private val nullableInts = List(SIZE) { if (it % 4 == 0) null else it }
    private val nested = List(SIZE) { if (it % 3 == 0) emptyList() else listOf(it, it + 1) }
    private val mixed = List(SIZE) { index ->
        when (index % 3) {
            0 -> "s$index"
            1 -> index
            else -> Any()
        }
    }

    // region forEachFast

    @Benchmark
    fun forEachFast(blackhole: Blackhole) {
        var sum = 0
        ints.forEachFast { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    fun forEach(blackhole: Blackhole) {
        var sum = 0
        ints.forEach { sum += it }
        blackhole.consume(sum)
    }

    // endregion

    // region forEachIndexedFast

    @Benchmark
    fun forEachIndexedFast(blackhole: Blackhole) {
        var sum = 0
        ints.forEachIndexedFast { index, e -> sum += index + e }
        blackhole.consume(sum)
    }

    @Benchmark
    fun forEachIndexed(blackhole: Blackhole) {
        var sum = 0
        ints.forEachIndexed { index, e -> sum += index + e }
        blackhole.consume(sum)
    }

    // endregion

    // region forEachReversedFast

    @Benchmark
    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    fun forEachReversedFast(blackhole: Blackhole) {
        var sum = 0
        ints.forEachReversedFast { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    fun forEachReversed(blackhole: Blackhole) {
        var sum = 0
        ints.asReversed().forEach { sum += it }
        blackhole.consume(sum)
    }

    // endregion

    // region mapFast

    @Benchmark
    fun mapFast(blackhole: Blackhole) {
        blackhole.consume(ints.mapFast { it + 1 })
    }

    @Benchmark
    fun map(blackhole: Blackhole) {
        blackhole.consume(ints.map { it + 1 })
    }

    @Benchmark
    fun mapFastSingle(blackhole: Blackhole) {
        blackhole.consume(single.mapFast { it + 1 })
    }

    @Benchmark
    fun mapSingle(blackhole: Blackhole) {
        blackhole.consume(single.map { it + 1 })
    }

    // endregion

    // region mapNotNullFast

    @Benchmark
    fun mapNotNullFast(blackhole: Blackhole) {
        blackhole.consume(nullableInts.mapNotNullFast { it })
    }

    @Benchmark
    fun mapNotNull(blackhole: Blackhole) {
        blackhole.consume(nullableInts.mapNotNull { it })
    }

    // endregion

    // region flatMapFast

    @Benchmark
    fun flatMapFast(blackhole: Blackhole) {
        blackhole.consume(nested.flatMapFast { it })
    }

    @Benchmark
    fun flatMap(blackhole: Blackhole) {
        @Suppress("SimplifiableCall")
        blackhole.consume(nested.flatMap { it })
    }

    // endregion

    // region filterFast

    @Benchmark
    fun filterFast(blackhole: Blackhole) {
        blackhole.consume(ints.filterFast { it % 2 == 0 })
    }

    @Benchmark
    fun filter(blackhole: Blackhole) {
        blackhole.consume(ints.filter { it % 2 == 0 })
    }

    // endregion

    // region filterNotNullFast

    @Benchmark
    fun filterNotNullFast(blackhole: Blackhole) {
        blackhole.consume(nullableInts.filterNotNullFast())
    }

    @Benchmark
    fun filterNotNull(blackhole: Blackhole) {
        blackhole.consume(nullableInts.filterNotNull())
    }

    // endregion

    // region filterIsInstanceFast

    @Benchmark
    fun filterIsInstanceFast(blackhole: Blackhole) {
        blackhole.consume(mixed.filterIsInstanceFast<String> { it.startsWith("s") })
    }

    @Benchmark
    fun filterIsInstance(blackhole: Blackhole) {
        blackhole.consume(mixed.filterIsInstance<String> { it.startsWith("s") })
    }

    // endregion

    // region findFast

    @Benchmark
    fun findFast(blackhole: Blackhole) {
        blackhole.consume(ints.findFast { it >= 32 })
    }

    @Benchmark
    fun find(blackhole: Blackhole) {
        blackhole.consume(ints.find { it >= 32 })
    }

    // endregion

    // region findLastFast

    @Benchmark
    fun findLastFast(blackhole: Blackhole) {
        blackhole.consume(ints.findLastFast { it < 32 })
    }

    @Benchmark
    fun findLast(blackhole: Blackhole) {
        blackhole.consume(ints.findLast { it < 32 })
    }

    // endregion

    // region allFast

    @Benchmark
    fun allFast(blackhole: Blackhole) {
        blackhole.consume(ints.allFast { it < SIZE })
    }

    @Benchmark
    fun all(blackhole: Blackhole) {
        blackhole.consume(ints.all { it < SIZE })
    }

    // endregion

    // region anyFast

    @Benchmark
    fun anyFast(blackhole: Blackhole) {
        blackhole.consume(ints.anyFast { it == 0 })
    }

    @Benchmark
    fun any(blackhole: Blackhole) {
        blackhole.consume(ints.any { it == 0 })
    }

    // endregion

    // region noneFast

    @Benchmark
    fun noneFast(blackhole: Blackhole) {
        blackhole.consume(ints.noneFast { it < 0 })
    }

    @Benchmark
    fun none(blackhole: Blackhole) {
        blackhole.consume(ints.none { it < 0 })
    }

    // endregion

    // region processFast

    @Benchmark
    fun processFast(blackhole: Blackhole) {
        blackhole.consume(ints.processFast { it < SIZE })
    }

    @Benchmark
    fun process(blackhole: Blackhole) {
        blackhole.consume(ints.process { it < SIZE })
    }

    // endregion

    // region dropFast

    @Benchmark
    fun dropFastZero(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(0))
    }

    @Benchmark
    fun dropZero(blackhole: Blackhole) {
        blackhole.consume(ints.drop(0))
    }

    @Benchmark
    fun dropFastOne(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(1))
    }

    @Benchmark
    fun dropOne(blackhole: Blackhole) {
        blackhole.consume(ints.drop(1))
    }

    @Benchmark
    fun dropFastNearEnd(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(SIZE - 1))
    }

    @Benchmark
    fun dropNearEnd(blackhole: Blackhole) {
        blackhole.consume(ints.drop(SIZE - 1))
    }

    // endregion
}
