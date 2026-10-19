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

private const val OPERATIONS = 11 * 11

/**
 * [AntMatcher] 的基准测试。
 *
 * 每个基准方法都会遍历 11 个模式与 11 个路径的组合，因此需要通过
 * [OperationsPerInvocation] 将单次调用折算为单次匹配操作。
 *
 * @see AntMatcher
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
@OperationsPerInvocation(OPERATIONS)
open class AntMatcherBenchmark {
    private val patterns = listOf(
        "foo/bar/baz",
        "foo/*/baz",
        "foo/**/baz",
        "**/baz",
        "foo/bar?baz",
        "foo/*/bar/**/baz",
        "**",
        "foo/bar/*",
        "foo/bar/**",
        "foo/bar/ba*",
        "foo/bar/ba?",
    )

    private val paths = listOf(
        "foo/bar/baz",
        "foo/abc/baz",
        "foo/bar/abc/baz",
        "foo/bar/baz/qux",
        "foo/barbaz",
        "foo/bar/xyz/baz",
        "foo/bar/abc/def/baz",
        "foo/bar/ba",
        "foo/bar/baa",
        "foo/bar/baz",
        "foo/bar/baZ",
    )

    @Benchmark
    open fun baseline(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (path in paths) {
                blackhole.consume(pattern.isNotEmpty() && path.isNotEmpty())
            }
        }
    }

    @Benchmark
    open fun matches(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (path in paths) {
                blackhole.consume(AntMatcher.matches(path, pattern))
            }
        }
    }

    @Benchmark
    open fun matchesIgnoreCase(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (path in paths) {
                blackhole.consume(AntMatcher.matches(path, pattern, ignoreCase = true))
            }
        }
    }
}
