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

private const val STRING_OPERATIONS = 11 * 11
private const val LIST_OPERATIONS = 11 * 11

/**
 * [PathMatcher] 的基准测试。
 *
 * 覆盖字符串匹配、列表匹配和相对路径计算；结果仅作为示例，不作为长期性能报告。
 *
 * @see PathMatcher
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
open class PathMatcherBenchmark {
    private val subPaths = listOf(
        "foo",
        "bar",
        "baz",
        "foo_bar",
        "prefix_foo_bar",
        "foo.txt",
        "abc",
        "a_b_c",
        "x",
        "foobar",
        "baZ",
    )

    private val subPatterns = listOf(
        "foo",
        "b*",
        "*bar",
        "foo?bar",
        "*_*",
        "prefix*",
        "*suffix",
        "?",
        "*.*",
        "foo_bar",
        "foo*",
    )

    private val anyKeywords = listOf(
        "any", "none", "any", "ANY", "any", "none", "any", "any", "none", "any", "any",
    )

    private val listInputs = List(11) { index ->
        listOf("root", "foo", "bar", "baz", "leaf$index")
    }

    private val listExactOthers = List(11) { index ->
        listOf("root", "foo", "bar", "baz", "leaf$index")
    }

    private val listPatternOthers = List(11) { index ->
        listOf("root", "f*", "b*", "b?z", "leaf$index")
    }

    private val listAnyOthers = List(11) { index ->
        listOf("root", "any", "bar", "any", "leaf$index")
    }

    private val relativeBases = List(11) { index ->
        listOf("root", "foo", "level$index")
    }

    private val relativePaths = List(11) { index ->
        listOf("root", "foo", "level$index", "child", "leaf")
    }

    @Benchmark
    @OperationsPerInvocation(STRING_OPERATIONS)
    open fun matchesStringExact(blackhole: Blackhole) {
        for (input in subPaths) {
            for (other in subPaths) {
                blackhole.consume(PathMatcher.matches(input, other))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(STRING_OPERATIONS)
    open fun matchesStringPattern(blackhole: Blackhole) {
        for (input in subPaths) {
            for (pattern in subPatterns) {
                blackhole.consume(PathMatcher.matches(input, pattern, usePattern = true))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(11)
    open fun matchesStringAny(blackhole: Blackhole) {
        for ((index, input) in subPaths.withIndex()) {
            blackhole.consume(PathMatcher.matches(input, anyKeywords[index], useAny = true))
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(LIST_OPERATIONS)
    open fun matchesListExact(blackhole: Blackhole) {
        for (input in listInputs) {
            for (other in listExactOthers) {
                blackhole.consume(PathMatcher.matches(input, other))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(LIST_OPERATIONS)
    open fun matchesListPattern(blackhole: Blackhole) {
        for (input in listInputs) {
            for (other in listPatternOthers) {
                blackhole.consume(PathMatcher.matches(input, other, usePattern = true))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(LIST_OPERATIONS)
    open fun matchesListAny(blackhole: Blackhole) {
        for (input in listInputs) {
            for (other in listAnyOthers) {
                blackhole.consume(PathMatcher.matches(input, other, useAny = true))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(LIST_OPERATIONS)
    open fun relative(blackhole: Blackhole) {
        for (base in relativeBases) {
            for (path in relativePaths) {
                blackhole.consume(PathMatcher.relative(base, path))
            }
        }
    }
}
