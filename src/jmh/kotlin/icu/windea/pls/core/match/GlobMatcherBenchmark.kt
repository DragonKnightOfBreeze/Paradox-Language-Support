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
 * 包含短字符串、长字符串和未命中场景，并与预编译正则参考实现对比。
 * 结果用于回归观察和相对比较；绝对值受运行环境影响，不应跨环境直接比较。
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

    // 参考实现：预编译的模式正则，用于对比热路径上的匹配开销
    private val patternRegexes = patterns.associateWith { it.toGlobRegex(false) }
    private val patternRegexesIgnoreCase = patterns.associateWith { it.toGlobRegex(true) }
    private val longPatternRegexes = longPatterns.associateWith { it.toGlobRegex(false) }

    @Benchmark
    @OperationsPerInvocation(SHORT_OPERATIONS)
    fun baseline(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (input in inputs) {
                blackhole.consume(pattern.isNotEmpty() && input.isNotEmpty())
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(SHORT_OPERATIONS)
    fun matches(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (input in inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(SHORT_OPERATIONS)
    fun matchesIgnoreCase(blackhole: Blackhole) {
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
    fun matchesLongInput(blackhole: Blackhole) {
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
    fun matchesMiss(blackhole: Blackhole) {
        for (pattern in missPatterns) {
            for (input in inputs) {
                blackhole.consume(GlobMatcher.matches(input, pattern))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(SHORT_OPERATIONS)
    fun matchesByRegex(blackhole: Blackhole) {
        for ((_, regex) in patternRegexes) {
            for (input in inputs) {
                blackhole.consume(regex.matches(input))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(SHORT_OPERATIONS)
    fun matchesIgnoreCaseByRegex(blackhole: Blackhole) {
        for ((_, regex) in patternRegexesIgnoreCase) {
            for (input in inputs) {
                blackhole.consume(regex.matches(input))
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
    @OperationsPerInvocation(LONG_OPERATIONS)
    fun matchesLongInputByRegex(blackhole: Blackhole) {
        for ((_, regex) in longPatternRegexes) {
            for (input in longInputs) {
                blackhole.consume(regex.matches(input))
            }
        }
    }
}

private fun String.toGlobRegex(ignoreCase: Boolean): Regex {
    val regex = buildString {
        append('^')
        for (c in this@toGlobRegex) {
            when (c) {
                '*' -> append(".*")
                '?' -> append('.')
                else -> append(Regex.escape(c.toString()))
            }
        }
        append('$')
    }
    val options = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
    return Regex(regex, options)
}
