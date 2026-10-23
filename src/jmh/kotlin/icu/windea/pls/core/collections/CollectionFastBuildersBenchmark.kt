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
 * [CollectionFastBuilders] 的基准测试。
 *
 * 每个 `@Fast` 构建方法与对应的标准库/惯用实现对比；结果用于回归观察和相对比较。
 *
 * @see CollectionFastBuilders
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class CollectionFastBuildersBenchmark {
    @JvmField
    @Param("0", "1", "4", "64")
    var size: Int = 0

    private lateinit var elements: Array<Any?>

    @Setup(Level.Trial)
    open fun setup() {
        elements = Array<Any?>(size) { it }
    }

    @Benchmark
    open fun buildImmutableList(blackhole: Blackhole) {
        blackhole.consume(buildImmutableList<Any>(size) { it })
    }

    @Benchmark
    open fun buildImmutableListByReference(blackhole: Blackhole) {
        blackhole.consume(List<Any>(size) { it })
    }

    @Benchmark
    open fun buildImmutableSet(blackhole: Blackhole) {
        blackhole.consume(buildImmutableSet<Any>(size) { it })
    }

    @Benchmark
    open fun buildImmutableSetByReference(blackhole: Blackhole) {
        blackhole.consume(buildSet<Any>(size) { for (index in 0 until size) add(index) })
    }

    @Benchmark
    open fun asImmutableList(blackhole: Blackhole) {
        blackhole.consume(elements.asImmutableList<Any>())
    }

    @Benchmark
    open fun asImmutableListByReference(blackhole: Blackhole) {
        blackhole.consume(elements.toList())
    }
}
