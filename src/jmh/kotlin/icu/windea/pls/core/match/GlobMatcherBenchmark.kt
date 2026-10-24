package icu.windea.pls.core.match

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * [GlobMatcher] 的基准测试。
 *
 * 包含短字符串、长字符串和未命中场景。结果用于回归观察和相对比较；
 * 绝对值受运行环境影响，不应跨环境直接比较。正则实现见 [RegexBasedGlobMatcherBenchmark]。
 *
 * @see GlobMatcher
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class GlobMatcherBenchmark {
    @Benchmark
    @OperationsPerInvocation(GlobMatcherBenchmarkData.SHORT_OPERATIONS)
    fun baseline(blackhole: Blackhole) {
        for (pattern in GlobMatcherBenchmarkData.patterns) {
            for (input in GlobMatcherBenchmarkData.inputs) {
                blackhole.consume(pattern.isNotEmpty() && input.isNotEmpty())
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(GlobMatcherBenchmarkData.SHORT_OPERATIONS)
    fun matches(blackhole: Blackhole) {
        for (pattern in GlobMatcherBenchmarkData.patterns) {
            for (input in GlobMatcherBenchmarkData.inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(GlobMatcherBenchmarkData.SHORT_OPERATIONS)
    fun matchesIgnoreCase(blackhole: Blackhole) {
        for (pattern in GlobMatcherBenchmarkData.patterns) {
            for (input in GlobMatcherBenchmarkData.inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern, ignoreCase = true))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(GlobMatcherBenchmarkData.LONG_OPERATIONS)
    fun matchesLongInput(blackhole: Blackhole) {
        for (pattern in GlobMatcherBenchmarkData.longPatterns) {
            for (input in GlobMatcherBenchmarkData.longInputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(GlobMatcherBenchmarkData.MISS_OPERATIONS)
    fun matchesMiss(blackhole: Blackhole) {
        for (pattern in GlobMatcherBenchmarkData.missPatterns) {
            for (input in GlobMatcherBenchmarkData.inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }
}
