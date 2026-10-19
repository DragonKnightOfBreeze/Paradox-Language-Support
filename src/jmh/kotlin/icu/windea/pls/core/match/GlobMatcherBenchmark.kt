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

private const val SHORT_OPERATIONS = 11 * 11
private const val LONG_PATH_COUNT = 8
private const val LONG_PATTERN_COUNT = 8
private const val LONG_OPERATIONS = LONG_PATH_COUNT * LONG_PATTERN_COUNT
private const val MISS_OPERATIONS = 11 * 5

/**
 * [GlobMatcher] 的基准测试。
 *
 * 包含短字符串、长字符串和未命中场景；结果仅作为示例，不作为长期性能报告。
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
open class GlobMatcherBenchmark {
    private val patterns = listOf(
        "*",
        "foo*",
        "*bar",
        "foo*bar",
        "foo?bar",
        "*_*",
        "prefix*",
        "*suffix",
        "a*b*c",
        "*.*",
        "foo*bar*baz",
    )

    private val inputs = listOf(
        "foo",
        "bar",
        "foobar",
        "foo_bar",
        "prefix_foo_bar",
        "foo.txt",
        "a/b/c",
        "abc",
        "a_b_c",
        "foobarbaz",
        "x",
    )

    private val longPatterns = listOf(
        "*",
        "prefix*",
        "*suffix",
        "prefix*suffix",
        "*a*a*a*",
        "*a?b?c*",
        "*middle*",
        "prefix*_*suffix",
    )

    private val longInputs = List(LONG_PATH_COUNT) { index ->
        val body = "a".repeat(32 + index * 8)
        "prefix_${body}_middle_${body}_suffix_$index"
    }

    private val missPatterns = listOf(
        "none*",
        "*not_found",
        "foo?bar?baz",
        "prefix",
        "*@*",
    )

    @Benchmark
    @OperationsPerInvocation(SHORT_OPERATIONS)
    open fun baseline(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (input in inputs) {
                blackhole.consume(pattern.isNotEmpty() && input.isNotEmpty())
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(SHORT_OPERATIONS)
    open fun matches(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (input in inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(SHORT_OPERATIONS)
    open fun matchesIgnoreCase(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (input in inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern, ignoreCase = true))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(LONG_OPERATIONS)
    open fun matchesLongInput(blackhole: Blackhole) {
        for (pattern in longPatterns) {
            for (input in longInputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(MISS_OPERATIONS)
    open fun matchesMiss(blackhole: Blackhole) {
        for (pattern in missPatterns) {
            for (input in inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }
}
