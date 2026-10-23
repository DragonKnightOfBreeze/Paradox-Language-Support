package icu.windea.pls.core

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
 * [StdlibFastExtensions] 中字符串比较方法在高复杂度下的基准测试。
 *
 * 使用 100、10000、1000000 三档字符串长度，覆盖完全相等、末位不匹配
 * 和忽略大小写比较等场景。
 *
 * @see StdlibFastExtensions
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class StdlibFastExtensionsScalingBenchmark {
    @JvmField
    @Param("100", "10000", "1000000")
    var length: Int = 0

    private lateinit var equalA: String
    private lateinit var equalB: String
    private lateinit var mismatchLastA: String
    private lateinit var mismatchLastB: String
    private lateinit var ignoreCaseA: String
    private lateinit var ignoreCaseB: String

    @Setup(Level.Trial)
    open fun setup() {
        equalA = String(CharArray(length) { 'a' })
        equalB = String(CharArray(length) { 'a' })
        mismatchLastA = equalA
        mismatchLastB = String(CharArray(length) { if (it == length - 1) 'b' else 'a' })
        ignoreCaseA = equalA
        ignoreCaseB = String(CharArray(length) { if (it % 2 == 0) 'A' else 'a' })
    }

    @Benchmark
    open fun equalsFastEqual(blackhole: Blackhole) {
        blackhole.consume(equalA.equalsFast(equalB))
    }

    @Benchmark
    open fun equalsEqual(blackhole: Blackhole) {
        blackhole.consume(equalA.equals(equalB))
    }

    @Benchmark
    open fun equalsFastMismatchLast(blackhole: Blackhole) {
        blackhole.consume(mismatchLastA.equalsFast(mismatchLastB))
    }

    @Benchmark
    open fun equalsMismatchLast(blackhole: Blackhole) {
        blackhole.consume(mismatchLastA.equals(mismatchLastB))
    }

    @Benchmark
    open fun equalsFastIgnoreCaseMixed(blackhole: Blackhole) {
        blackhole.consume(ignoreCaseA.equalsFast(ignoreCaseB, ignoreCase = true))
    }

    @Benchmark
    open fun equalsIgnoreCaseMixed(blackhole: Blackhole) {
        blackhole.consume(ignoreCaseA.equals(ignoreCaseB, ignoreCase = true))
    }
}
