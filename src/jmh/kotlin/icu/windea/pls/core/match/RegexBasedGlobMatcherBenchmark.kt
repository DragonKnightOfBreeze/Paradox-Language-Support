@file:Suppress("DEPRECATION")

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
 * [RegexBasedGlobMatcher] 的基准测试。
 *
 * 包含短字符串、长字符串和未命中场景，与 [GlobMatcherBenchmark] 使用相同的输入数据。
 * 正则实现会缓存编译后的 [Regex]，JMH 预热迭代会填充缓存。
 *
 * @see RegexBasedGlobMatcher
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
class RegexBasedGlobMatcherBenchmark {
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
                blackhole.consume(RegexBasedGlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(GlobMatcherBenchmarkData.SHORT_OPERATIONS)
    fun matchesIgnoreCase(blackhole: Blackhole) {
        for (pattern in GlobMatcherBenchmarkData.patterns) {
            for (input in GlobMatcherBenchmarkData.inputs) {
                blackhole.consume(RegexBasedGlobMatcher.matches(input, pattern, ignoreCase = true))
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
                blackhole.consume(RegexBasedGlobMatcher.matches(input, pattern))
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
                blackhole.consume(RegexBasedGlobMatcher.matches(input, pattern))
            }
        }
    }
}
