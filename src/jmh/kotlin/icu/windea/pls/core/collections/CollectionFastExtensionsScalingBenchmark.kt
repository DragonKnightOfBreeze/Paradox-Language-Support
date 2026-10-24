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
 * `CollectionFastExtensions.kt` 中的方法在不同输入规模和相对输出规模下的基准测试。
 *
 * 使用 100、10000、1000000 三档输入规模，观察线性遍历、结果构建和
 * 过滤比例、丢弃比例对 `@Fast` 实现与标准库实现的影响。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class CollectionFastExtensionsScalingBenchmark {
    @Param("100", "10000", "1000000")
    var size: Int = 0

    private lateinit var ints: List<Int>

    @Setup(Level.Trial)
    fun setup() {
        ints = List(size) { it }
    }

    // region forEachFast

    @Benchmark
    fun forEachFast(blackhole: Blackhole) {
        var sum = 0L
        ints.forEachFast { sum += it }
        blackhole.consume(sum)
    }

    @Benchmark
    fun forEach(blackhole: Blackhole) {
        var sum = 0L
        ints.forEach { sum += it }
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

    // endregion

    // region filterFast

    @Benchmark
    fun filterFastHalf(blackhole: Blackhole) {
        blackhole.consume(ints.filterFast { it % 2 == 0 })
    }

    @Benchmark
    fun filterHalf(blackhole: Blackhole) {
        blackhole.consume(ints.filter { it % 2 == 0 })
    }

    @Benchmark
    fun filterFastNone(blackhole: Blackhole) {
        blackhole.consume(ints.filterFast { it < 0 })
    }

    @Benchmark
    fun filterNone(blackhole: Blackhole) {
        blackhole.consume(ints.filter { it < 0 })
    }

    @Benchmark
    fun filterFastAll(blackhole: Blackhole) {
        blackhole.consume(ints.filterFast { it >= 0 })
    }

    @Benchmark
    fun filterAll(blackhole: Blackhole) {
        blackhole.consume(ints.filter { it >= 0 })
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
    fun dropFastHalf(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(size / 2))
    }

    @Benchmark
    fun dropHalf(blackhole: Blackhole) {
        blackhole.consume(ints.drop(size / 2))
    }

    @Benchmark
    fun dropFastNearEnd(blackhole: Blackhole) {
        blackhole.consume(ints.dropFast(size - 1))
    }

    @Benchmark
    fun dropNearEnd(blackhole: Blackhole) {
        blackhole.consume(ints.drop(size - 1))
    }

    // endregion
}
