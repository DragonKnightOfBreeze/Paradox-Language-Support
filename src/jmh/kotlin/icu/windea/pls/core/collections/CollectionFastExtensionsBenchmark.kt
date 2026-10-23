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
 * [CollectionFastExtensions] 的基准测试。
 *
 * 每个 `@Fast` 方法都与对应的标准库/惯用实现对比；结果用于回归观察和相对比较。
 *
 * @see CollectionFastExtensions
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class CollectionFastExtensionsBenchmark {
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

    @Benchmark
    open fun forEachFast(blackhole: Blackhole) {
        var sum = 0
        ints.forEachFast { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    open fun forEach(blackhole: Blackhole) {
        var sum = 0
        ints.forEach { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    open fun forEachIndexedFast(blackhole: Blackhole) {
        var sum = 0
        ints.forEachIndexedFast { index, e -> sum += index + e }
        blackhole.consume(sum)
    }

    @Benchmark
    open fun forEachIndexed(blackhole: Blackhole) {
        var sum = 0
        ints.forEachIndexed { index, e -> sum += index + e }
        blackhole.consume(sum)
    }

    @Benchmark
    open fun forEachReversedFast(blackhole: Blackhole) {
        var sum = 0
        ints.forEachReversedFast { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    open fun forEachReversed(blackhole: Blackhole) {
        var sum = 0
        ints.asReversed().forEach { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    open fun mapFast(blackhole: Blackhole) {
        blackhole.consume(ints.mapFast { it + 1 })
    }

    @Benchmark
    open fun map(blackhole: Blackhole) {
        blackhole.consume(ints.map { it + 1 })
    }

    @Benchmark
    open fun mapFastSingle(blackhole: Blackhole) {
        blackhole.consume(single.mapFast { it + 1 })
    }

    @Benchmark
    open fun mapSingle(blackhole: Blackhole) {
        blackhole.consume(single.map { it + 1 })
    }

    @Benchmark
    open fun mapNotNullFast(blackhole: Blackhole) {
        blackhole.consume(nullableInts.mapNotNullFast { it })
    }

    @Benchmark
    open fun mapNotNull(blackhole: Blackhole) {
        blackhole.consume(nullableInts.mapNotNull { it })
    }

    @Benchmark
    open fun flatMapFast(blackhole: Blackhole) {
        blackhole.consume(nested.flatMapFast { it })
    }

    @Benchmark
    open fun flatMap(blackhole: Blackhole) {
        blackhole.consume(nested.flatMap { it })
    }

    @Benchmark
    open fun filterFast(blackhole: Blackhole) {
        blackhole.consume(ints.filterFast { it % 2 == 0 })
    }

    @Benchmark
    open fun filter(blackhole: Blackhole) {
        blackhole.consume(ints.filter { it % 2 == 0 })
    }

    @Benchmark
    open fun filterNotNullFast(blackhole: Blackhole) {
        blackhole.consume(nullableInts.filterNotNullFast())
    }

    @Benchmark
    open fun filterNotNull(blackhole: Blackhole) {
        blackhole.consume(nullableInts.filterNotNull())
    }

    @Benchmark
    open fun filterIsInstanceFast(blackhole: Blackhole) {
        blackhole.consume(mixed.filterIsInstanceFast<String> { it.startsWith("s") })
    }

    @Benchmark
    open fun filterIsInstance(blackhole: Blackhole) {
        blackhole.consume(mixed.filterIsInstance<String> { it.startsWith("s") })
    }

    @Benchmark
    open fun findFast(blackhole: Blackhole) {
        blackhole.consume(ints.findFast { it >= 32 })
    }

    @Benchmark
    open fun find(blackhole: Blackhole) {
        blackhole.consume(ints.find { it >= 32 })
    }

    @Benchmark
    open fun findLastFast(blackhole: Blackhole) {
        blackhole.consume(ints.findLastFast { it < 32 })
    }

    @Benchmark
    open fun findLast(blackhole: Blackhole) {
        blackhole.consume(ints.findLast { it < 32 })
    }

    @Benchmark
    open fun allFast(blackhole: Blackhole) {
        blackhole.consume(ints.allFast { it < SIZE })
    }

    @Benchmark
    open fun all(blackhole: Blackhole) {
        blackhole.consume(ints.all { it < SIZE })
    }

    @Benchmark
    open fun anyFast(blackhole: Blackhole) {
        blackhole.consume(ints.anyFast { it == 0 })
    }

    @Benchmark
    open fun any(blackhole: Blackhole) {
        blackhole.consume(ints.any { it == 0 })
    }

    @Benchmark
    open fun noneFast(blackhole: Blackhole) {
        blackhole.consume(ints.noneFast { it < 0 })
    }

    @Benchmark
    open fun none(blackhole: Blackhole) {
        blackhole.consume(ints.none { it < 0 })
    }

    @Benchmark
    open fun processFast(blackhole: Blackhole) {
        blackhole.consume(ints.processFast { it < SIZE })
    }

    @Benchmark
    open fun process(blackhole: Blackhole) {
        blackhole.consume(ints.process { it < SIZE })
    }

    @Benchmark
    open fun dropFastZero(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(0))
    }

    @Benchmark
    open fun dropZero(blackhole: Blackhole) {
        blackhole.consume(ints.drop(0))
    }

    @Benchmark
    open fun dropFastOne(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(1))
    }

    @Benchmark
    open fun dropOne(blackhole: Blackhole) {
        blackhole.consume(ints.drop(1))
    }

    @Benchmark
    open fun dropFastNearEnd(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(SIZE - 1))
    }

    @Benchmark
    open fun dropNearEnd(blackhole: Blackhole) {
        blackhole.consume(ints.drop(SIZE - 1))
    }
}
